package com.ryan.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ryan.entity.AlbumInfo;

/**
 * <p>
 * 专辑信息 服务类
 * </p>
 *
 * @author ryan
 * @since 2025-04-26
 */
public interface AlbumInfoService extends IService<AlbumInfo> {

    // 新增专辑
    void saveAlbumInfo(AlbumInfo albumInfo);

    // 根据id查询专辑信息
    AlbumInfo getAlbumInfoById(Long albumId);

    // 修改专辑信息
    void updateAlbumInfo(AlbumInfo albumInfo);

    // 删除专辑信息
    void deleteAlbumInfo(Long albumId);

    // 是否订阅
    boolean isSubscribe(Long albumId);
}
