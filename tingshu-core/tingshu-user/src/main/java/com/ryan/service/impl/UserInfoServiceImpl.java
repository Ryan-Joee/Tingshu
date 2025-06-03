package com.ryan.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ryan.entity.UserInfo;
import com.ryan.entity.UserPaidAlbum;
import com.ryan.entity.UserPaidTrack;
import com.ryan.mapper.UserInfoMapper;
import com.ryan.service.UserInfoService;
import com.ryan.service.UserPaidAlbumService;
import com.ryan.service.UserPaidTrackService;
import com.ryan.util.AuthContextHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    /**
     * 获取用户是否需要购买的标识
     * @param albumId 专辑id
     * @param needPayTrackIdList 需要师傅的声音id集合
     */
    @Autowired
    private UserPaidAlbumService userPaidAlbumService;
    @Autowired
    private UserPaidTrackService userPaidTrackService;
    @Override
    public Map<Long, Boolean> getUserShowPaidMarkOrNot(Long albumId, List<Long> needPayTrackIdList) {
        Long userId = AuthContextHolder.getUserId();
        LambdaQueryWrapper<UserPaidAlbum> paidAlbumWrapper = new LambdaQueryWrapper();
        paidAlbumWrapper.eq(UserPaidAlbum::getUserId, userId);
        paidAlbumWrapper.eq(UserPaidAlbum::getAlbumId, albumId);
        //查询用户购买的专辑
        UserPaidAlbum userPaidAlbum = userPaidAlbumService.getOne(paidAlbumWrapper);
        Map<Long, Boolean> showPaidMarkMap = new HashMap<>();
        if (null != userPaidAlbum) {
            needPayTrackIdList.forEach(trackId -> {
                //已购买声音 不用显示付费标识
                showPaidMarkMap.put(trackId, false);
            });
            return showPaidMarkMap;
        } else {
            LambdaQueryWrapper<UserPaidTrack> paidTrackWrapper = new LambdaQueryWrapper();
            paidTrackWrapper.eq(UserPaidTrack::getUserId, userId);
            paidTrackWrapper.in(UserPaidTrack::getTrackId, needPayTrackIdList);
            //查询用户购买过的声音
            List<UserPaidTrack> userPaidTrackList = userPaidTrackService.list(paidTrackWrapper);
            List<Long> paidTrackIdList = userPaidTrackList.stream().map(UserPaidTrack::getTrackId).collect(Collectors.toList());
            needPayTrackIdList.forEach(trackId -> {
                if (paidTrackIdList.contains(trackId)) {
                    //已购买声音 不用显示付费标识
                    showPaidMarkMap.put(trackId,false);
                } else {
                    //未购买声音
                    showPaidMarkMap.put(trackId,true);
                }
            });
//            return showPaidMarkMap;
        }
         return showPaidMarkMap;
    }
}
