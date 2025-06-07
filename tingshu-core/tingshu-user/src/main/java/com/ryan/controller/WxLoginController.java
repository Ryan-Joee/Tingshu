package com.ryan.controller;

import cn.binarywang.wx.miniapp.api.WxMaService;
import cn.binarywang.wx.miniapp.bean.WxMaJscode2SessionResult;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ryan.constant.KafkaConstant;
import com.ryan.constant.RedisConstant;
import com.ryan.entity.UserInfo;
import com.ryan.login.TingshuLogin;
import com.ryan.result.RetVal;
import com.ryan.service.KafkaService;
import com.ryan.service.UserInfoService;
import com.ryan.util.AuthContextHolder;
import com.ryan.vo.UserInfoVo;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 * vip服务配置表 前端控制器
 * </p>
 *
 * @author ryan
 * @since 2025-04-24
 */
@RestController
@RequestMapping("/api/user/wxLogin")
public class WxLoginController {

    /**
     * 注入的WxMaService是通过WeChatLoginConfig配置类获取的
     */
    @Autowired
    private WxMaService wxMaService;

    @Autowired
    private UserInfoService userInfoService;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private KafkaService kafkaService;

    @Operation(summary = "小程序授权登录")
    @GetMapping("wxLogin/{code}")
    public RetVal wxLogin(@PathVariable String code) throws Exception {
        // 调用微信提供的API，获取用户的openId
        WxMaJscode2SessionResult sessionInfo = wxMaService.getUserService().getSessionInfo(code);
        String openId = sessionInfo.getOpenid();

        // 从数据库中查询用户信息是否存在
        // select * from user_info where wx_open_id = odo3j4q2KskkbbW-krfE-cAxUnzU
        // 在这里的Wrapper相当于查询语句中的 where 条件判断
        LambdaQueryWrapper<UserInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserInfo::getWxOpenId, openId);
        UserInfo userInfo = userInfoService.getOne(wrapper);

        // 如果认证失败，不存在，往数据库中添加用户信息
        if (userInfo == null) {
             userInfo = new UserInfo();
             userInfo.setNickname("听友" + System.currentTimeMillis());
             userInfo.setAvatarUrl("https://oss.aliyuncs.com/aliyun_id_photo_bucket/default_handsome.jpg");
             userInfo.setWxOpenId(openId);
             // 是否为会员：默认不是vip
             userInfo.setIsVip(0);
             userInfoService.save(userInfo);
            //注册成功，初始化用户账户
            kafkaService.sendMessage(KafkaConstant.USER_REGISTER_QUEUE, userInfo.getId()+"");
        }
        // 然后往redis中存储用户信息
        String uuid = UUID.randomUUID().toString().replace("-", "");
        String userKey = RedisConstant.USER_LOGIN_KEY_PREFIX + uuid;
        redisTemplate.opsForValue()
                .set(userKey, userInfo, RedisConstant.USER_LOGIN_KEY_TIMEOUT, TimeUnit.SECONDS);
        Map<String, Object> retMap = new HashMap<>();
        retMap.put("token", uuid);
        return RetVal.ok(retMap);
    }

    // http://127.0.0.1/api/user/wxLogin/getUserInfo
    @TingshuLogin
    @Operation(summary = "获取用户个人信息")
    @GetMapping("getUserInfo")
    public RetVal wxLogin() {
        // 这个AuthContextHolder.getUserId()是在LoginAspect中设置的UserId
        Long userId = AuthContextHolder.getUserId();
        UserInfo userInfo = userInfoService.getById(userId);
        UserInfoVo userInfoVo = new UserInfoVo();
        BeanUtils.copyProperties(userInfo, userInfoVo);
        return RetVal.ok(userInfoVo);
    }
}
