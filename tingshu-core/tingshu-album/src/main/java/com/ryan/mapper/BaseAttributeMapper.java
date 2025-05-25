package com.ryan.mapper;

import com.ryan.entity.BaseAttribute;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import feign.Param;

import java.util.List;

/**
 * <p>
 * 属性表 Mapper 接口
 * </p>
 *
 * @author ryan
 * @since 2025-04-26
 */
public interface BaseAttributeMapper extends BaseMapper<BaseAttribute> {

    List<BaseAttribute> getPropertyByCategory1Id(@Param("category1Id") Long category1Id);
}
