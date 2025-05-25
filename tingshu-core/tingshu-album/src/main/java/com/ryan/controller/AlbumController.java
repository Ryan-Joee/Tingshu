package com.ryan.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ryan.entity.AlbumInfo;
import com.ryan.login.TingshuLogin;
import com.ryan.mapper.AlbumInfoMapper;
import com.ryan.query.AlbumInfoQuery;
import com.ryan.result.RetVal;
import com.ryan.service.AlbumInfoService;
import com.ryan.util.AuthContextHolder;
import com.ryan.vo.AlbumTempVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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
    public RetVal getAlbumInfoById(@PathVariable Long albumId) {
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

}

