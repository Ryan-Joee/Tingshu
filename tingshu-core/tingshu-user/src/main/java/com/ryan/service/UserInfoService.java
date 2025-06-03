package com.ryan.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ryan.entity.UserInfo;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 用户 服务类
 * </p>
 *
 * @author ryan
 * @since 2025-04-24
 */
public interface UserInfoService extends IService<UserInfo> {

    /**
     * 获取用户是否需要购买的标识
     * @param albumId 专辑id
     * @param needPayTrackIdList 需要师傅的声音id集合
     */
    Map<Long, Boolean> getUserShowPaidMarkOrNot(Long albumId, List<Long> needPayTrackIdList);
}
