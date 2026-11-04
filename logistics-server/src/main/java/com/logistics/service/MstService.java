package com.logistics.service;

import com.logistics.dto.MstDTO;
import org.springframework.stereotype.Service;

/**
 * 最小生成树业务。
 *
 * <p>TODO 郝晟尧实现：
 * <ul>
 *   <li>algorithm 只接受 prim / kruskal，其他值抛 IllegalArgumentException；</li>
 *   <li>调 PrimMST / KruskalMST，把 MstResult 的 Edge 转成 MstEdgeDTO（补城市名）；</li>
 *   <li>补 graphVersion；算法层不依赖 DTO。</li>
 * </ul>
 */
@Service
public class MstService {

    private final GraphService graphService;
    private final CityService cityService;

    public MstService(GraphService graphService, CityService cityService) {
        this.graphService = graphService;
        this.cityService = cityService;
    }

    public MstDTO compute(String algorithm) {
        throw todo("郝晟尧");
    }

    private UnsupportedOperationException todo(String owner) {
        return new UnsupportedOperationException("TODO(" + owner + "): 待实现");
    }
}
