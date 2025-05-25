package com.ryan.mapper;

import com.ryan.entity.BaseCategoryView;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ryan.vo.CategoryVo;

import java.util.List;

/**
 * <p>
 * VIEW Mapper 接口
 * </p>
 *
 * @author ryan
 * @since 2025-04-26
 */
public interface BaseCategoryViewMapper extends BaseMapper<BaseCategoryView> {

    List<CategoryVo> getAllCategoryList();
}
