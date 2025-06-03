package com.ryan.controller;

import com.ryan.login.TingshuLogin;
import com.ryan.result.RetVal;
import com.ryan.service.ListenService;
import com.ryan.vo.UserListenProcessVo;
import io.swagger.v3.oas.annotations.Operation;
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
    @Operation(summary = "最近播放")
    @GetMapping("getRecentlyPlay")
    public RetVal getRecentlyPlay() {
        listenService.getRecentlyPlay();
        return RetVal.ok();
    }

}
