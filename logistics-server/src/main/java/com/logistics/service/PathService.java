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
import com.logistics.util.DistanceUtil;
import org.springframework.stereotype.Service;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
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
     * 在 MST 上找 from → to 的唯一路径：建树 → BFS 记前驱 → 回溯算代价。
     *
     * <p>MST 是树，两点之间路径唯一；图不连通时返回 found=false。
     */
    private DijkstraShortestPath.PathResult treePath(WeightedGraph graph, int fromId, int toId) {
        long startNanos = System.nanoTime();

        if (fromId == toId) {
            return new DijkstraShortestPath.PathResult(
                true, 0.0, 0, List.of(fromId), System.nanoTime() - startNanos);
        }

        MstResult mst = new PrimMST().primMST(graph);

        Map<Integer, List<Edge>> tree = new HashMap<>();
        for (Edge edge : mst.getEdges()) {
            tree.computeIfAbsent(edge.getStartId(), key -> new ArrayList<>()).add(edge);
            tree.computeIfAbsent(edge.getEndId(), key -> new ArrayList<>()).add(edge);
        }

        Map<Integer, Integer> parent = new HashMap<>();
        Map<Integer, Double> weightFromParent = new HashMap<>();
        Deque<Integer> queue = new ArrayDeque<>();

        parent.put(fromId, -1);
        queue.add(fromId);
        boolean reached = false;

        while (!queue.isEmpty()) {
            int current = queue.poll();
            if (current == toId) {
                reached = true;
                break;
            }
            for (Edge edge : tree.getOrDefault(current, List.of())) {
                int next = edge.other(current);
                if (parent.containsKey(next)) {
                    continue;
                }
                parent.put(next, current);
                weightFromParent.put(next, edge.getWeight());
                queue.add(next);
            }
        }

        if (!reached) {
            return new DijkstraShortestPath.PathResult(
                false, null, 0, List.of(), System.nanoTime() - startNanos);
        }

        LinkedList<Integer> path = new LinkedList<>();
        double cost = 0.0;
        for (int cursor = toId; cursor != -1; cursor = parent.get(cursor)) {
            path.addFirst(cursor);
            Double weight = weightFromParent.get(cursor);
            if (weight != null) {
                cost += weight;
            }
        }

        return new DijkstraShortestPath.PathResult(
            true, DistanceUtil.round2(cost), path.size() - 1, path,
            System.nanoTime() - startNanos);
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
