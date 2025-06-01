package com.ryan.service;

import com.ryan.entity.BaseCategory3;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * 三级分类表 服务类
 * </p>
 *
 * @author ryan
 * @since 2025-04-26
 */
public interface BaseCategory3Service extends IService<BaseCategory3> {

    /**
     * 通过一级分类id三级分类列表
     * @param category1Id 一级分类id
     * @return List<BaseCategory3>
     */
    List<BaseCategory3> getCategory3ListByCategory1Id(Long category1Id);
}
