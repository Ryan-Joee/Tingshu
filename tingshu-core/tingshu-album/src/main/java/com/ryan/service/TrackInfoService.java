package com.ryan.service;

import com.ryan.entity.TrackInfo;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 声音信息 服务类
 * </p>
 *
 * @author ryan
 * @since 2025-04-26
 */
public interface TrackInfoService extends IService<TrackInfo> {

     /**
      * 新增声音
      * @param trackInfo 声音信息
      */
     void saveTrackInfo(TrackInfo trackInfo);

    /**
     * 修改声音信息
     * @param trackInfo 声音信息
     */
    void updateTrackInfoById(TrackInfo trackInfo);

    /**
     * 删除声音信息
     * @param trackId 声音id
     */
    void deleteTrackInfo(Long trackId);
}
