package com.logistics.service;

import com.logistics.dto.CityDTO;
import com.logistics.dto.CityRequest;
import com.logistics.dto.SortedCitiesDTO;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 城市业务。唯一写入口：增删改城市、维护 graphVersion、维护链表和图的一致性。
 *
 * <p>TODO 梁敬谦实现：
 * <ul>
 *   <li>启动时读 resources/data/cities.txt，生成 id = 1000 + 行号；</li>
 *   <li>持有双向循环链表和当前图，写操作后 version++；</li>
 *   <li>新增/删除/改坐标时级联更新图（文档 4.7）；</li>
 *   <li>名称唯一、坐标范围、id 生成等校验。</li>
 * </ul>
 */
@Service
public class CityService {

    public List<CityDTO> list() {
        throw todo("梁敬谦");
    }

    public CityDTO get(int id) {
        throw todo("梁敬谦");
    }

    public CityDTO add(CityRequest request) {
        throw todo("梁敬谦");
    }

    public CityDTO update(int id, CityRequest request) {
        throw todo("梁敬谦");
    }

    public void delete(int id) {
        throw todo("梁敬谦");
    }

    public SortedCitiesDTO sorted(Integer referenceId) {
        throw todo("梁敬谦");
    }

    public void reset() {
        throw todo("梁敬谦");
    }

    /**
     * 当前数据版本。任何写操作后加 1，MST/Path 缓存按它失效。
     */
    public int version() {
        throw todo("梁敬谦");
    }

    private UnsupportedOperationException todo(String owner) {
        return new UnsupportedOperationException("TODO(" + owner + "): 待实现");
    }
}
