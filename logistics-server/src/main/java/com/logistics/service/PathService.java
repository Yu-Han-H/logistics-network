package com.logistics.service;

import com.logistics.dto.PathDTO;
import org.springframework.stereotype.Service;

/**
 * 最短路径业务。
 *
 * <p>TODO 郝晟尧实现：
 * <ul>
 *   <li>mode 只接受 graph / tree；graph 在原图上跑，tree 在 MST 上跑；</li>
 *   <li>调 DijkstraShortestPath，把 PathResult 的 id 列表补成 PathNodeDTO；</li>
 *   <li>补 graphVersion；不可达返回 found=false。</li>
 * </ul>
 */
@Service
public class PathService {

    private final GraphService graphService;
    private final CityService cityService;

    public PathService(GraphService graphService, CityService cityService) {
        this.graphService = graphService;
        this.cityService = cityService;
    }

    public PathDTO shortest(int fromId, int toId, String mode) {
        throw todo("郝晟尧");
    }

    private UnsupportedOperationException todo(String owner) {
        return new UnsupportedOperationException("TODO(" + owner + "): 待实现");
    }
}
