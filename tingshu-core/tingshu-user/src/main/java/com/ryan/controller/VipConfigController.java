package com.ryan.controller;

import com.ryan.entity.VipServiceConfig;
import com.ryan.result.RetVal;
import com.ryan.service.VipServiceConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * <p>
 * 用户 前端控制器
 * </p>
 *
 * @author ryan
 * @since 2025-04-24
 */
@Tag(name = "vip服务管理接口")
@RestController
@RequestMapping("/api/user/vipConfig")
public class VipConfigController {

    @Autowired
    private VipServiceConfigService vipServiceConfigService;

    @Operation(summary = "获取所有的VIP配置")
    @GetMapping("findAllVipConfig")
    public RetVal<List<VipServiceConfig>> findAllVipConfig() {
        List<VipServiceConfig> list = vipServiceConfigService.list();
        return RetVal.ok(list);
    }

    // http://127.0.0.1/api/user/vipConfig/findAllVipConfig
}
