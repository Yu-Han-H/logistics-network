package com.logistics.service;

import com.logistics.algorithm.mst.KruskalMST;
import com.logistics.algorithm.mst.MstResult;
import com.logistics.algorithm.mst.PrimMST;
import com.logistics.dto.CityDTO;
import com.logistics.dto.MstDTO;
import com.logistics.dto.MstEdgeDTO;
import com.logistics.model.Edge;
import com.logistics.structure.WeightedGraph;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * 最小生成树业务。
 *
 * <p>algorithm 只接受 prim / kruskal；结果统一转成 MstDTO（约定 3.3）。
 * 计算结果按 graphVersion 缓存（约定 5.4）。
 */
@Service
public class MstService {

    private final GraphService graphService;
    private final CityService cityService;

    /** 缓存对应的数据版本。 */
    private int cachedVersion = Integer.MIN_VALUE;

    /** 两种算法的结果各缓存一份。 */
    private MstResult cachedPrim;
    private MstResult cachedKruskal;

    public MstService(GraphService graphService, CityService cityService) {
        this.graphService = graphService;
        this.cityService = cityService;
    }

    public MstDTO compute(String algorithm) {
        String normalized = normalizeAlgorithm(algorithm);

        WeightedGraph graph = graphService.currentGraph();
        int version = graphService.version();
        MstResult result = resultFor(normalized, graph, version);

        Map<Integer, String> names = cityNames();

        List<MstEdgeDTO> edges = new ArrayList<>(result.getEdges().size());
        for (Edge edge : result.getEdges()) {
            int fromId = edge.getStartId();
            int toId = edge.getEndId();
            edges.add(new MstEdgeDTO(
                fromId, names.getOrDefault(fromId, ""),
                toId, names.getOrDefault(toId, ""),
                edge.getWeight()));
        }

        return new MstDTO(
            normalized,
            result.getNodeCount(),
            result.getEdgeCount(),
            result.isConnected(),
            result.getTotalWeight(),
            edges,
            result.getElapsedNanos(),
            version);
    }

    /** 只接受 prim / kruskal，大小写和首尾空格做容错。 */
    private String normalizeAlgorithm(String algorithm) {
        if (algorithm == null || algorithm.isBlank()) {
            return MstResult.PRIM;
        }
        String normalized = algorithm.trim().toLowerCase(Locale.ROOT);
        if (!MstResult.PRIM.equals(normalized) && !MstResult.KRUSKAL.equals(normalized)) {
            throw new IllegalArgumentException("algorithm 只支持 prim 或 kruskal：" + algorithm);
        }
        return normalized;
    }

    /** 命中缓存直接返回；版本变了先清空。 */
    private synchronized MstResult resultFor(String algorithm, WeightedGraph graph, int version) {
        if (version != cachedVersion) {
            cachedVersion = version;
            cachedPrim = null;
            cachedKruskal = null;
        }

        if (MstResult.PRIM.equals(algorithm)) {
            if (cachedPrim == null) {
                cachedPrim = new PrimMST().primMST(graph);
            }
            return cachedPrim;
        }
        if (cachedKruskal == null) {
            cachedKruskal = new KruskalMST().kruskalMST(graph);
        }
        return cachedKruskal;
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
