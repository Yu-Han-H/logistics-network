package com.logistics.service;

import com.logistics.dto.AdjacencyDTO;
import com.logistics.dto.GraphDTO;
import com.logistics.structure.WeightedGraph;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 图的查询和重建。图从 CityService 的城市列表派生，不单独存一份可写数据。
 *
 * <p>TODO 郝晟尧实现：
 * <ul>
 *   <li>按 CityService.version() 缓存 WeightedGraph；版本变了就重建；</li>
 *   <li>graph() 返回 G6 用的节点/边；</li>
 *   <li>adjacency() 返回完整邻接表；</li>
 *   <li>currentGraph() 给 MstService/PathService 用。</li>
 * </ul>
 */
@Service
public class GraphService {

    private final CityService cityService;

    public GraphService(CityService cityService) {
        this.cityService = cityService;
    }

    public GraphDTO graph() {
        throw todo("郝晟尧");
    }

    public List<AdjacencyDTO> adjacency() {
        throw todo("郝晟尧");
    }

    public WeightedGraph currentGraph() {
        throw todo("郝晟尧");
    }

    public void rebuild() {
        throw todo("郝晟尧");
    }

    /**
     * 图版本和城市数据版本保持一致。
     */
    public int version() {
        return cityService.version();
    }

    private UnsupportedOperationException todo(String owner) {
        return new UnsupportedOperationException("TODO(" + owner + "): 待实现");
    }
}
