package com.ryan.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ryan.entity.BaseAttribute;
import com.ryan.entity.BaseCategory1;
import com.ryan.entity.BaseCategory3;
import com.ryan.entity.BaseCategoryView;
import com.ryan.login.TingshuLogin;
import com.ryan.mapper.BaseAttributeMapper;
import com.ryan.result.RetVal;
import com.ryan.service.BaseCategory1Service;
import com.ryan.service.BaseCategory3Service;
import com.ryan.service.BaseCategoryViewService;
import com.ryan.vo.CategoryVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * <p>
 * 一级分类表 前端控制器
 * </p>
 *
 * @author ryan
 * @since 2025-04-26
 */
@Tag(name = "分类管理")
@RestController
@RequestMapping(value = "/api/album/category")
public class CategoryController {

    @Autowired
    private BaseCategoryViewService categoryViewService;

    @Autowired
    private BaseAttributeMapper propertyKeyMapper;

    // http://127.0.0.1/api/album/category/getAllCategoryList
    @TingshuLogin(required = true)
    @Operation(summary = "获取全部分类信息")
    @GetMapping("getAllCategoryList")
    public RetVal wxLogin() {
        List<CategoryVo> categoryVoList =  categoryViewService.getAllCategoryList(null);
        return RetVal.ok(categoryVoList);
    }

    // http://127.0.0.1/api/album/category/getPropertyByCategory1Id/2
    @Operation(summary = "根据一级分类Id查询属性信息")
    @GetMapping("getPropertyByCategory1Id/{category1Id}")
    public RetVal getPropertyByCategory1Id(@PathVariable Long category1Id) {
        List<BaseAttribute> categoryPropertyList = propertyKeyMapper.getPropertyByCategory1Id(category1Id);
        return RetVal.ok(categoryPropertyList);
    }

    @Operation(summary = "通过三级分类id查询分类信息")
    @GetMapping("getCategoryView/{category3Id}")
    public BaseCategoryView getCategoryView(@PathVariable Long category3Id) {
        BaseCategoryView categoryView = categoryViewService.getById(category3Id);
        return categoryView;
    }

    @Autowired
    private BaseCategory3Service category3Service;
    @Operation(summary = "根据一级分类id三级分类列表")
    @GetMapping("getCategory3ListByCategory1Id/{category1Id}")
    public RetVal<List<BaseCategory3>> getCategory3ListByCategory1Id(@PathVariable Long category1Id) {
        List<BaseCategory3> category3List = category3Service.getCategory3ListByCategory1Id(category1Id);
        return RetVal.ok(category3List);
    }

    @Operation(summary = "根据一级分类id全部分类信息")
    @GetMapping("getCategoryByCategory1Id/{category1Id}")
    public RetVal getCategoryByCategory1Id(@PathVariable Long category1Id) {
        List<CategoryVo> allCategoryList = categoryViewService.getAllCategoryList(category1Id);
        if (!CollectionUtils.isEmpty(allCategoryList)) {
            return RetVal.ok(allCategoryList.get(0));
        }
        return RetVal.ok();
    }

    @Autowired
    private BaseCategory1Service category1Service;
    @Operation(summary = "查询所有的一级分类")
    @GetMapping("getCategory1")
    public RetVal<List<BaseCategory1>> getCategory1() {
        LambdaQueryWrapper<BaseCategory1> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(BaseCategory1::getOrderNum);
        List<BaseCategory1> category1List = category1Service.list(wrapper);
        return RetVal.ok(category1List);
    }
}
