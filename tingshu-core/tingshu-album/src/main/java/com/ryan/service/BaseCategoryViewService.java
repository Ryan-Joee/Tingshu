package com.ryan.service;

import com.ryan.entity.BaseCategoryView;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ryan.vo.CategoryVo;

import java.util.List;

/**
 * <p>
 * VIEW 服务类
 * </p>
 *
 * @author ryan
 * @since 2025-04-26
 */
public interface BaseCategoryViewService extends IService<BaseCategoryView> {


    List<CategoryVo> getAllCategoryList(Long category1Id);
}
