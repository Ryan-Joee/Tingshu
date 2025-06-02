package com.ryan.service.impl;

import com.ryan.entity.BaseCategoryView;
import com.ryan.mapper.BaseCategoryViewMapper;
import com.ryan.service.BaseCategoryViewService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ryan.vo.CategoryVo;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>
 * VIEW 服务实现类
 * </p>
 *
 * @author ryan
 * @since 2025-04-26
 */
@Service
public class BaseCategoryViewServiceImpl extends ServiceImpl<BaseCategoryViewMapper, BaseCategoryView> implements BaseCategoryViewService {

    // @Override
    public List<CategoryVo> getAllCategoryList1() {
        // 1. 查询所有的分类信息
        // list()方法：mp内置的方法，自动生成 SQL：执行 SELECT * FROM [当前实体对应的表名]。
        // 它是根据返回值类型BaseCategoryView，去找这个类型上的@TableName("base_category_view")注解，来确定自己是要查询哪个表
        List<BaseCategoryView> allCategoryView = list();
        // 2. 找到所有的一级分类
        Map<Long, List<BaseCategoryView>> category1Map = allCategoryView.stream().collect(Collectors.groupingBy(BaseCategoryView::getCategory1Id));
        ArrayList<CategoryVo> categoryVoList = new ArrayList<>();
        for (Map.Entry<Long, List<BaseCategoryView>> category1Entry : category1Map.entrySet()) {
            Long category1Id = category1Entry.getKey();
            List<BaseCategoryView> category1List = category1Entry.getValue();
            CategoryVo category1Vo = new CategoryVo();
            category1Vo.setCategoryId(category1Id);
            // 这里的category1List.get(0)最好是判断一下是否为空并判断是否大于0
            category1Vo.setCategoryName(category1List.get(0).getCategory1Name());
            // 3. 找到所有的二级分类
            Map<Long, List<BaseCategoryView>> category2Map = category1List.stream().collect(Collectors.groupingBy(BaseCategoryView::getCategory2Id));
            List<CategoryVo> category1Children = category2Map.entrySet().stream().map(category2Entry -> {
                Long category2Id = category2Entry.getKey();
                List<BaseCategoryView> category2List = category2Entry.getValue();
                CategoryVo category2Vo = new CategoryVo();
                category2Vo.setCategoryId(category2Id);
                category2Vo.setCategoryName(category2List.get(0).getCategory2Name());
                // 4. 找到所有的三级分类
                Map<Long, List<BaseCategoryView>> category3Map = category2List.stream().collect(Collectors.groupingBy(BaseCategoryView::getCategory3Id));
                List<CategoryVo> category2Children = category3Map.entrySet().stream().map(category3Entry -> {
                    Long category3Id = category3Entry.getKey();
                    List<BaseCategoryView> category3List = category3Entry.getValue();
                    CategoryVo category3Vo = new CategoryVo();
                    category3Vo.setCategoryId(category3Id);
                    category3Vo.setCategoryName(category3List.get(0).getCategory3Name());
                    return category3Vo;
                }).collect(Collectors.toList());
                category2Vo.setCategoryChild(category2Children);
                return category2Vo;
            }).collect(Collectors.toList());
            category1Vo.setCategoryChild(category1Children);
            categoryVoList.add(category1Vo);
        }
        return categoryVoList;
    }


    @Override
    public List<CategoryVo> getAllCategoryList(Long category1Id) {
        return baseMapper.getAllCategoryList(category1Id);
    }

}
