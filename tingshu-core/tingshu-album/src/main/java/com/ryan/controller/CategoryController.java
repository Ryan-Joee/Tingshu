package com.ryan.controller;

import com.ryan.entity.BaseAttribute;
import com.ryan.login.TingshuLogin;
import com.ryan.mapper.BaseAttributeMapper;
import com.ryan.result.RetVal;
import com.ryan.service.BaseCategoryViewService;
import com.ryan.vo.CategoryVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
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
        List<CategoryVo> categoryVoList =  categoryViewService.getAllCategoryList();
        return RetVal.ok(categoryVoList);
    }

    // http://127.0.0.1/api/album/category/getPropertyByCategory1Id/2
    @Operation(summary = "根据一级分类Id查询属性信息")
    @GetMapping("getPropertyByCategory1Id/{category1Id}")
    public RetVal getPropertyByCategory1Id(@PathVariable Long category1Id) {
        List<BaseAttribute> categoryPropertyList = propertyKeyMapper.getPropertyByCategory1Id(category1Id);
        return RetVal.ok(categoryPropertyList);
    }
}
