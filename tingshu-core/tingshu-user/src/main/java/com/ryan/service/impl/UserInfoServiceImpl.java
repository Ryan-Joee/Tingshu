package com.ryan.service.impl;

import com.ryan.entity.UserInfo;
import com.ryan.mapper.UserInfoMapper;
import com.ryan.service.UserInfoService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 用户 服务实现类
 * </p>
 *
 * @author ryan
 * @since 2025-04-24
 */
@Service
public class UserInfoServiceImpl extends ServiceImpl<UserInfoMapper, UserInfo> implements UserInfoService {

}
