package com.logistics.service;

import com.logistics.dto.AdjacencyDTO;
import com.logistics.dto.CityDTO;
import com.logistics.dto.GraphDTO;
import com.logistics.dto.GraphEdgeDTO;
import com.logistics.dto.GraphNodeDTO;
import com.logistics.dto.NeighborDTO;
import com.logistics.model.Edge;
import com.logistics.structure.WeightedGraph;
import com.logistics.util.DistanceUtil;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 图的查询和重建。
 *
 * <p>图从 CityService 的城市列表派生，不单独存一份可写数据；
 * 按 CityService.version() 缓存，版本变了才重建（约定 3.2）。
 */
@Service
public class GraphService {

    private final CityService cityService;

    /** 缓存的图；null 表示还没建过。 */
    private volatile WeightedGraph cachedGraph;

    /** 缓存对应的数据版本。 */
    private volatile int cachedVersion = Integer.MIN_VALUE;

    public GraphService(CityService cityService) {
        this.cityService = cityService;
    }

    /**
     * G6 用的整图：节点 id 转字符串，边端点也转字符串（api.md 5.5）。
     */
    public GraphDTO graph() {
        WeightedGraph graph = currentGraph();
        Map<Integer, CityDTO> cityMap = cityById();

        List<GraphNodeDTO> nodes = new ArrayList<>(graph.vertexCount());
        for (int cityId : graph.vertices()) {
            CityDTO city = cityMap.get(cityId);
            nodes.add(new GraphNodeDTO(
                String.valueOf(cityId),
                city == null ? "" : city.name(),
                city == null ? 0.0 : city.longitude(),
                city == null ? 0.0 : city.latitude()));
        }

        // 边按 (startId, endId) 排序，保证每次输出顺序一致
        List<Edge> edges = new ArrayList<>(graph.edges());
        edges.sort(Comparator.comparingInt((Edge edge) -> edge.getStartId())
            .thenComparingInt(edge -> edge.getEndId()));

        List<GraphEdgeDTO> edgeDtos = new ArrayList<>(edges.size());
        for (Edge edge : edges) {
            edgeDtos.add(new GraphEdgeDTO(
                String.valueOf(edge.getStartId()),
                String.valueOf(edge.getEndId()),
                edge.getWeight()));
        }

        return new GraphDTO(nodes, edgeDtos, graph.vertexCount(), graph.edgeCount());
    }

    /**
     * 完整邻接表：每个城市一行，邻居按 toId 升序，补上城市名（api.md 5.4）。
     */
    public List<AdjacencyDTO> adjacency() {
        WeightedGraph graph = currentGraph();
        Map<Integer, String> names = cityNames();

        List<AdjacencyDTO> rows = new ArrayList<>(graph.vertexCount());
        for (int cityId : graph.vertices()) {
            List<Edge> neighbors = new ArrayList<>(graph.neighbors(cityId));
            neighbors.sort(Comparator.comparingInt((Edge edge) -> edge.other(cityId)));

            List<NeighborDTO> neighborDtos = new ArrayList<>(neighbors.size());
            for (Edge edge : neighbors) {
                int toId = edge.other(cityId);
                neighborDtos.add(new NeighborDTO(
                    toId, names.getOrDefault(toId, ""), edge.getWeight()));
            }

            rows.add(new AdjacencyDTO(cityId, names.getOrDefault(cityId, ""), neighborDtos));
        }
        return rows;
    }

    /**
     * 当前图的快照，给 MstService / PathService 用。
     *
     * <p>返回的是缓存对象，调用方只读，不要 addEdge/removeVertex。
     */
    public WeightedGraph currentGraph() {
        int version = cityService.version();
        WeightedGraph graph = cachedGraph;
        if (graph != null && cachedVersion == version) {
            return graph;
        }
        synchronized (this) {
            if (cachedGraph == null || cachedVersion != version) {
                cachedGraph = buildGraph();
                cachedVersion = version;
            }
            return cachedGraph;
        }
    }

    /** 强制重建，测试或需要立即刷新的场景用。 */
    public void rebuild() {
        synchronized (this) {
            int version = cityService.version();
            cachedGraph = buildGraph();
            cachedVersion = version;
        }
    }

    /** 图版本 = 城市数据版本，不要另起一个版本号（约定 5.4）。 */
    public int version() {
        return cityService.version();
    }

    /** 按城市列表建完全图：任意两城之间都有边，权值用 DistanceUtil 计算。 */
    private WeightedGraph buildGraph() {
        List<CityDTO> cities = cityService.list();

        WeightedGraph graph = new WeightedGraph();
        for (CityDTO city : cities) {
            graph.addVertex(city.id());
        }
        for (int i = 0; i < cities.size(); i++) {
            for (int j = i + 1; j < cities.size(); j++) {
                CityDTO a = cities.get(i);
                CityDTO b = cities.get(j);
                graph.addEdge(a.id(), b.id(), DistanceUtil.distance(
                    a.longitude(), a.latitude(), b.longitude(), b.latitude()));
            }
        }
        return graph;
    }

    /** 一次性取出 id → 名称，避免拼 DTO 时反复遍历城市列表。 */
    private Map<Integer, String> cityNames() {
        List<CityDTO> cities = cityService.list();
        Map<Integer, String> names = new HashMap<>(cities.size() * 2);
        for (CityDTO city : cities) {
            names.put(city.id(), city.name());
        }
        return names;
    }

    /** 一次性取出 id → 城市完整对象，给 G6 节点补经纬度。 */
    private Map<Integer, CityDTO> cityById() {
        List<CityDTO> cities = cityService.list();
        Map<Integer, CityDTO> byId = new HashMap<>(cities.size() * 2);
        for (CityDTO city : cities) {
            byId.put(city.id(), city);
        }
        return byId;
    }
}
