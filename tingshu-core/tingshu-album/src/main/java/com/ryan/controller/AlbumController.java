package com.ryan.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ryan.entity.AlbumAttributeValue;
import com.ryan.entity.AlbumInfo;
import com.ryan.login.TingshuLogin;
import com.ryan.mapper.AlbumInfoMapper;
import com.ryan.mapper.AlbumStatMapper;
import com.ryan.query.AlbumInfoQuery;
import com.ryan.result.RetVal;
import com.ryan.service.AlbumAttributeValueService;
import com.ryan.service.AlbumInfoService;
import com.ryan.util.AuthContextHolder;
import com.ryan.vo.AlbumStatVo;
import com.ryan.vo.AlbumTempVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 * 一级分类表 前端控制器
 * </p>
 *
 * @author ryan
 * @since 2025-04-26
 */
@Tag(name = "专辑管理")
@RestController
@RequestMapping(value = "/api/album/albumInfo")
public class AlbumController {

    @Autowired
    private AlbumInfoService albumInfoService;

    @Autowired
    private AlbumInfoMapper albumInfoMapper;

    // http://127.0.0.1/api/album/albumInfo/saveAlbumInfo
    @TingshuLogin
    @Operation(summary = "新增专辑")
    @PostMapping("saveAlbumInfo")
    public RetVal saveAlbumInfo(@RequestBody AlbumInfo albumInfo) { // 因为前端传数据过来是json，由json转成对象，需要加上@RequestBody注解
        albumInfoService.saveAlbumInfo(albumInfo);
        return RetVal.ok();
    }

    // http://127.0.0.1/api/album/albumInfo/getUserAlbumByPage/1/10
    @TingshuLogin
    @Operation(summary = "分页查询专辑")
    @PostMapping("getUserAlbumByPage/{pageNum}/{pageSize}")
    public RetVal getUserAlbumByPage(
            @Parameter(name = "pageNum", description = "当前页码", required = true)
            @PathVariable Long pageNum,
            @Parameter(name = "pageSize", description = "每页记录数", required = true)
            @PathVariable Long pageSize,
            @Parameter(name = "albumInfoQuery", description = "查询对象", required = false)
            @RequestBody AlbumInfoQuery albumInfoQuery
            ) {
        Long userId = AuthContextHolder.getUserId();
        albumInfoQuery.setUserId(userId);

        IPage<AlbumTempVo> pageParam = new Page<>(pageNum, pageSize);
        pageParam =  albumInfoMapper.getUserAlbumByPage(pageParam, albumInfoQuery);
        System.out.println("查询结果: " + pageParam.getRecords());
        return RetVal.ok(pageParam);
    }

    // http://127.0.0.1/api/album/albumInfo/getAlbumInfoById/1600
    @Operation(summary = "根据id查询专辑信息")
    @GetMapping("getAlbumInfoById/{albumId}")
    public RetVal<AlbumInfo> getAlbumInfoById(@PathVariable Long albumId) {
        AlbumInfo albumInfo = albumInfoService.getAlbumInfoById(albumId);
        return RetVal.ok(albumInfo);
    }

    // http://127.0.0.1/api/album/albumInfo/updateAlbumInfo
    @TingshuLogin
    @Operation(summary = "修改专辑信息")
    @PutMapping("updateAlbumInfo")
    public RetVal updateAlbumInfo(@RequestBody AlbumInfo albumInfo) {
        albumInfoService.updateAlbumInfo(albumInfo);
        return RetVal.ok();
    }

    // http://127.0.0.1/api/album/albumInfo/deleteAlbumInfo/1602
    // 从请求路径中获取专辑id
    @Operation(summary = "删除专辑信息")
    @DeleteMapping("deleteAlbumInfo/{albumId}")
    public RetVal deleteAlbumInfo(@PathVariable Long albumId) {
        albumInfoService.deleteAlbumInfo(albumId);
        return RetVal.ok();
    }

    /** 以下内容属于搜索模块 **/
    @Autowired
    private AlbumAttributeValueService albumAttributeValueService;
    @Operation(summary = "根据albumId查询专辑属性信息")
    @GetMapping("getAlbumInfoPropertyValue/{albumId}")
    public List<AlbumAttributeValue> getAlbumInfoPropertyValue(@PathVariable Long albumId) {
        albumInfoService.deleteAlbumInfo(albumId);
        LambdaQueryWrapper<AlbumAttributeValue> wrapper = Wrappers.lambdaQuery(AlbumAttributeValue.class)
                .eq(AlbumAttributeValue::getAlbumId, albumId);
        List<AlbumAttributeValue> attributeValueList = albumAttributeValueService.list(wrapper);
        return attributeValueList;
    }

    @Autowired
    private AlbumStatMapper albumStatMapper;

    /** 以下内容属于专辑详情模块 **/
    @Operation(summary = "获取专辑统计信息")
    @GetMapping("getAlbumStatInfo/{albumId}")
    public RetVal<AlbumStatVo> getAlbumStatInfo(@PathVariable Long albumId) {
        AlbumStatVo albumStatVo=albumStatMapper.getAlbumStatInfo(albumId);
        return RetVal.ok(albumStatVo);
    }

    // http://127.0.0.1/api/album/albumInfo/isSubscribe/1611
    @TingshuLogin
    @Operation(summary = "是否订阅")
    @GetMapping("isSubscribe/{albumId}")
    public RetVal isSubscribe(@PathVariable Long albumId) {
        boolean flag = albumInfoService.isSubscribe(albumId);
        return RetVal.ok(flag);
    }


}

