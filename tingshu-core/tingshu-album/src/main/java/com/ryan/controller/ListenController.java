package com.ryan.controller;

import com.ryan.login.TingshuLogin;
import com.ryan.mapper.TrackInfoMapper;
import com.ryan.result.RetVal;
import com.ryan.service.ListenService;
import com.ryan.vo.TrackStatVo;
import com.ryan.vo.UserListenProcessVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

/**
 * <p>
 * 一级分类表 前端控制器
 * </p>
 *
 * @author ryan
 * @since 2025-04-26
 */
@Tag(name = "听专辑管理接口")
@RestController
@RequestMapping(value = "/api/album/progress")
public class ListenController {

    @Autowired
    private ListenService listenService;

    @TingshuLogin
    @Operation(summary = "3更新播放进度")
    @PostMapping("updatePlaySecond")
    public RetVal updatePlaySecond(@RequestBody UserListenProcessVo userListenProcessVo) {
        listenService.updatePlaySecond(userListenProcessVo);
        return RetVal.ok();
    }

    @TingshuLogin
    @Operation(summary = "1最近播放")
    @GetMapping("/getRecentlyPlay")
    public RetVal<Map<String,Object>> getRecentlyPlay() {
        Map<String,Object> retMap=listenService.getRecentlyPlay();
        return RetVal.ok(retMap);
    }

    @TingshuLogin
    @Operation(summary = "2获取上次声音播放进度")
    @GetMapping("/getLastPlaySecond/{trackId}")
    public RetVal<BigDecimal> getLastPlaySecond(@PathVariable Long trackId) {
        BigDecimal second=listenService.getLastPlaySecond(trackId);
        return RetVal.ok(second);
    }

    @Autowired
    private TrackInfoMapper trackInfoMapper;
    @Operation(summary = "4.获取声音统计信息")
    @GetMapping("getTrackStatistics/{trackId}")
    public RetVal<TrackStatVo> getTrackStatistics(@PathVariable Long trackId) {
        TrackStatVo trackStatVo = trackInfoMapper.getTrackStatistics(trackId);
        return RetVal.ok(trackStatVo);
    }

    @TingshuLogin
    @Operation(summary = "5收藏声音")
    @GetMapping("collectTrack/{trackId}")
    public RetVal<Boolean> collectTrack(@PathVariable Long trackId) {
        boolean flag=listenService.collectTrack(trackId);
        return RetVal.ok(flag);
    }

}
