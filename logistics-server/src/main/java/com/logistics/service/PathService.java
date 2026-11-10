package com.logistics.service;

import com.logistics.algorithm.mst.MstResult;
import com.logistics.algorithm.mst.PrimMST;
import com.logistics.algorithm.path.DijkstraShortestPath;
import com.logistics.dto.CityDTO;
import com.logistics.dto.PathDTO;
import com.logistics.dto.PathNodeDTO;
import com.logistics.exception.CityNotFoundException;
import com.logistics.model.Edge;
import com.logistics.structure.WeightedGraph;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * 最短路径业务。
 *
 * <p>mode=graph 在原图上跑 Dijkstra；mode=tree 在 MST 上找唯一路径（约定 3.4）。
 * 结果按 graphVersion 缓存（约定 5.4）。
 */
@Service
public class PathService {

    private final GraphService graphService;
    private final CityService cityService;

    /** 缓存 key = mode:from:to；版本变化时整体清空。 */
    private final Map<String, DijkstraShortestPath.PathResult> cache = new HashMap<>();
    private int cachedVersion = Integer.MIN_VALUE;

    public PathService(GraphService graphService, CityService cityService) {
        this.graphService = graphService;
        this.cityService = cityService;
    }

    public PathDTO shortest(int fromId, int toId, String mode) {
        String normalized = normalizeMode(mode);

        Map<Integer, String> names = cityNames();
        if (!names.containsKey(fromId)) {
            throw new CityNotFoundException(fromId);
        }
        if (!names.containsKey(toId)) {
            throw new CityNotFoundException(toId);
        }

        WeightedGraph graph = graphService.currentGraph();
        int version = graphService.version();
        DijkstraShortestPath.PathResult result =
            resultFor(normalized, graph, fromId, toId, version);

        List<PathNodeDTO> path = new ArrayList<>(result.getPath().size());
        for (int cityId : result.getPath()) {
            path.add(new PathNodeDTO(cityId, names.getOrDefault(cityId, "")));
        }

        return new PathDTO(
            normalized,
            result.isFound(),
            fromId,
            names.get(fromId),
            toId,
            names.get(toId),
            result.getCost(),
            result.getHops(),
            path,
            result.getElapsedNanos(),
            version);
    }

    /** 只接受 graph / tree，默认 graph。 */
    private String normalizeMode(String mode) {
        if (mode == null || mode.isBlank()) {
            return "graph";
        }
        String normalized = mode.trim().toLowerCase(Locale.ROOT);
        if (!"graph".equals(normalized) && !"tree".equals(normalized)) {
            throw new IllegalArgumentException("mode 只支持 graph 或 tree：" + mode);
        }
        return normalized;
    }

    /** 命中缓存直接返回；版本变了先清空。 */
    private synchronized DijkstraShortestPath.PathResult resultFor(
        String mode, WeightedGraph graph, int fromId, int toId, int version) {

        if (version != cachedVersion) {
            cachedVersion = version;
            cache.clear();
        }

        String key = mode + ":" + fromId + ":" + toId;
        DijkstraShortestPath.PathResult cached = cache.get(key);
        if (cached != null) {
            return cached;
        }

        DijkstraShortestPath.PathResult result = "graph".equals(mode)
            ? new DijkstraShortestPath().shortestPath(graph, fromId, toId)
            : treePath(graph, fromId, toId);
        cache.put(key, result);
        return result;
    }

    /**
     * 在 MST 上跑 Dijkstra：先用 MST 的边建一棵树图，再调用同一个 Dijkstra。
     *
     * <p>MST 是树，两点之间路径唯一；这样既满足“基于生成树拓扑结构”，
     * 又保证 tree 模式确实走的是 Dijkstra 算法。图不连通时返回 found=false。
     */
    private DijkstraShortestPath.PathResult treePath(WeightedGraph graph, int fromId, int toId) {
        MstResult mst = new PrimMST().primMST(graph);

        WeightedGraph tree = new WeightedGraph();
        for (int cityId : graph.vertexIds()) {
            tree.addVertex(cityId);
        }
        for (Edge edge : mst.getEdges()) {
            tree.addEdge(edge.getStartId(), edge.getEndId(), edge.getWeight());
        }

        return new DijkstraShortestPath().shortestPath(tree, fromId, toId);
    }

    private Map<Integer, String> cityNames() {
        List<CityDTO> cities = cityService.list();
        Map<Integer, String> names = new HashMap<>(cities.size() * 2);
        for (CityDTO city : cities) {
            names.put(city.id(), city.name());
        }
        return names;
    }
}
