package com.ryan.controller;

import com.ryan.entity.UserInfo;
import com.ryan.login.TingshuLogin;
import com.ryan.result.RetVal;
import com.ryan.service.UserInfoService;
import com.ryan.vo.UserInfoVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 用户 前端控制器
 * </p>
 *
 * @author ryan
 * @since 2025-04-24
 */
@Tag(name = "用户管理接口")
@RestController
@RequestMapping("/api/user/userInfo")
public class UserInfoController {

    @Autowired
    private UserInfoService userInfoService;

    @Operation(summary = "获取用户个人信息")
    @GetMapping("getUserById/{userId}")
    public RetVal<UserInfoVo> getUserById(@PathVariable Long userId) {
        UserInfo userInfo = userInfoService.getById(userId);
        UserInfoVo userInfoVo = new UserInfoVo();
        if (userInfo != null) {
            BeanUtils.copyProperties(userInfo, userInfoVo);
        }
        return RetVal.ok(userInfoVo);
    }

    @TingshuLogin
    @Operation(summary = "获取用户是否需要购买的标识")
    @PostMapping("getUserShowPaidMarkOrNot/{albumId}")
    public RetVal<Map<Long, Boolean>> getUserShowPaidMarkOrNot(@PathVariable Long albumId, @RequestBody List<Long> needPayTrackIdList) {
        Map<Long, Boolean> retMap = userInfoService.getUserShowPaidMarkOrNot(albumId, needPayTrackIdList);
        return RetVal.ok(retMap);
    }

}
