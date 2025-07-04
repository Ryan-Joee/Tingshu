package com.ryan.service;

import com.ryan.entity.TrackInfo;
import com.tencentcloudapi.common.exception.TencentCloudSDKException;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * <p>
 * 声音统计 服务类
 * </p>
 *
 * @author ryan
 * @since 2025-04-26
 */
public interface VodService {

    // 上传声音
    Map<String, Object> uploadTrack(MultipartFile file) throws Exception;

    // 获取声音的详细信息
    void getTrackMediaInfo(TrackInfo trackInfo) throws TencentCloudSDKException;

    // 删除声音
    void removeTrack(String mediaFileId) throws TencentCloudSDKException;
}
