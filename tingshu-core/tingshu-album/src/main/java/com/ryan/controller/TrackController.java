package com.ryan.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ryan.cache.TingshuCache;
import com.ryan.entity.AlbumInfo;
import com.ryan.entity.TrackInfo;
import com.ryan.login.TingshuLogin;
import com.ryan.mapper.TrackInfoMapper;
import com.ryan.query.TrackInfoQuery;
import com.ryan.result.RetVal;
import com.ryan.service.AlbumInfoService;
import com.ryan.service.TrackInfoService;
import com.ryan.service.VodService;
import com.ryan.util.AuthContextHolder;
import com.ryan.vo.AlbumTrackListVo;
import com.ryan.vo.TrackTempVo;
import com.tencentcloudapi.common.exception.TencentCloudSDKException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 一级分类表 前端控制器
 * </p>
 *
 * @author ryan
 * @since 2025-04-26
 */
@Tag(name = "声音管理")
@RestController
@RequestMapping(value = "/api/album/trackInfo")
public class TrackController {

    @Autowired
    private AlbumInfoService albumInfoService;

    @Autowired
    private VodService vodService;

    // http://127.0.0.1/api/album/trackInfo/findAlbumByUserId
    @TingshuLogin
    @Operation(summary = "根据用户ID查询用户的专辑信息")
    @GetMapping("findAlbumByUserId")
    public RetVal findAlbumByUserId() {
        // 获取用户Id
        Long userId = AuthContextHolder.getUserId();
        LambdaQueryWrapper<AlbumInfo> wrapper = new LambdaQueryWrapper<>();
        // 获取查询条件 => where userId = ?
        // 下面的这两句代码 => SELECT id, album_title FROM album_info WHERE user_id = ?
        wrapper.eq(AlbumInfo::getUserId, userId);
        wrapper.select(AlbumInfo::getId, AlbumInfo::getAlbumTitle);
        List<AlbumInfo> albumInfoList = albumInfoService.list(wrapper);
        return RetVal.ok(albumInfoList);
    }

    // http://127.0.0.1/api/album/trackInfo/uploadTrack
    @Operation(summary = "上传声音")
    @PostMapping("uploadTrack")
    public RetVal uploadTrack(MultipartFile file) throws Exception {
        Map<String, Object> retMap = vodService.uploadTrack(file);
        return RetVal.ok(retMap);
    }

    // http://127.0.0.1/api/album/trackInfo/saveTrackInfo
    // 保存声音
    @Autowired
    private TrackInfoService trackInfoService;

    @TingshuLogin
    @Operation(summary = "新增声音")
    @PostMapping("saveTrackInfo")
    public RetVal saveTrackInfo(@RequestBody TrackInfo trackInfo) throws TencentCloudSDKException {
        trackInfoService.saveTrackInfo(trackInfo);
        return RetVal.ok();
    }

    @Autowired
    private TrackInfoMapper trackInfoMapper;

    // http://127.0.0.1/api/album/trackInfo/findUserTrackPage/1/10
    @TingshuLogin
    @Operation(summary = "分页查询声音")
    @PostMapping("findUserTrackPage/{pageNum}/{pageSize}")
    public RetVal findUserTrackPage(
            @PathVariable Long pageNum,
            @PathVariable Long pageSize,
            @RequestBody TrackInfoQuery trackInfoQuery
    ) {
        Long userId = AuthContextHolder.getUserId();
        trackInfoQuery.setUserId(userId);

        IPage<TrackTempVo> pageParam = new Page<>(pageNum, pageSize);
        pageParam =  trackInfoMapper.findUserTrackPage(pageParam, trackInfoQuery);
        return RetVal.ok(pageParam);
    }

    @TingshuCache(value = "trackInfo", enableBloom = false)
    @Operation(summary = "根据id获取声音信息")
    @GetMapping("getTrackInfoById/{trackId}")
    public RetVal getTrackInfoById(@PathVariable Long trackId) {
        TrackInfo trackInfo = trackInfoService.getById(trackId);
        return RetVal.ok(trackInfo);
    }

    @TingshuLogin
    @Operation(summary = "修改声音信息")
    @PutMapping("updateTrackInfoById")
    public RetVal updateTrackInfoById(@RequestBody TrackInfo trackInfo) throws TencentCloudSDKException {
        trackInfoService.updateTrackInfoById(trackInfo);
        return RetVal.ok();
    }

    @TingshuLogin
    @Operation(summary = "删除声音信息")
    @DeleteMapping("deleteTrackInfo/{trackId}")
    public RetVal deleteTrackInfo(@PathVariable Long trackId) throws TencentCloudSDKException {
        trackInfoService.deleteTrackInfo(trackId);
        return RetVal.ok();
    }

    @TingshuLogin
    @Operation(summary = "分页查询声音")
    @GetMapping("getAlbumDetailTrackByPage/{albumId}/{pageNum}/{pageSize}")
    public RetVal getAlbumDetailTrackByPage(
            @PathVariable Long albumId,
            @PathVariable Long pageNum,
            @PathVariable Long pageSize
    ) {
        IPage<AlbumTrackListVo> pageParam = new Page<>(pageNum, pageSize);
        pageParam = trackInfoService.getAlbumDetailTrackByPage(pageParam, albumId);
        return RetVal.ok(pageParam);
    }

}

