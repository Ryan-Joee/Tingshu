package com.ryan.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ryan.entity.TrackInfo;
import com.ryan.vo.AlbumTrackListVo;
import com.tencentcloudapi.common.exception.TencentCloudSDKException;

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
     void saveTrackInfo(TrackInfo trackInfo) throws TencentCloudSDKException;

    /**
     * 修改声音信息
     * @param trackInfo 声音信息
     */
    void updateTrackInfoById(TrackInfo trackInfo) throws TencentCloudSDKException;

    /**
     * 删除声音信息
     * @param trackId 声音id
     */
    void deleteTrackInfo(Long trackId) throws TencentCloudSDKException;

    /**
     * 获取专辑声音详情
     * @param pageParam 分页详情
     * @param albumId 专辑id
     * @return IPage<TrackTempVo>
     */
    IPage<AlbumTrackListVo> getAlbumDetailTrackByPage(IPage<AlbumTrackListVo> pageParam, Long albumId);
}
