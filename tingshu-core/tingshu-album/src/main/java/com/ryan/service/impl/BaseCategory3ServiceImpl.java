package com.ryan.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.ryan.entity.BaseCategory2;
import com.ryan.entity.BaseCategory3;
import com.ryan.mapper.BaseCategory3Mapper;
import com.ryan.service.BaseCategory2Service;
import com.ryan.service.BaseCategory3Service;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 三级分类表 服务实现类
 * </p>
 *
 * @author ryan
 * @since 2025-04-26
 */
@Service
public class BaseCategory3ServiceImpl extends ServiceImpl<BaseCategory3Mapper, BaseCategory3> implements BaseCategory3Service {

    @Autowired
    private BaseCategory2Service category2Service;

    @Autowired
    private BaseCategory3Service category3Service;
    /**
     * 通过一级分类id三级分类列表
     * @param category1Id 一级分类id
     * @return List<BaseCategory3>
     */
    @Override
    public List<BaseCategory3> getCategory3ListByCategory1Id(Long category1Id) {
        // 根据一级分类id查询二级分类信息
        LambdaQueryWrapper<BaseCategory2> baseCategory2Wrapper = Wrappers.lambdaQuery(BaseCategory2.class)
                .eq(BaseCategory2::getCategory1Id, category1Id)
                .select(BaseCategory2::getId)
                .orderByDesc(BaseCategory2::getOrderNum);
        List<BaseCategory2> category2List = category2Service.list(baseCategory2Wrapper);
        // 根据二级分类id查找三级分类信息
        List<Long> category2IdList = category2List.stream().map(BaseCategory2::getId).collect(Collectors.toList());
        LambdaQueryWrapper<BaseCategory3> baseCategory3Wrapper = Wrappers.lambdaQuery(BaseCategory3.class)
                .in(BaseCategory3::getCategory2Id, category2IdList)
                .eq(BaseCategory3::getIsTop, 1)
                .last("limit 8");
        return category3Service.list(baseCategory3Wrapper);
    }
}
