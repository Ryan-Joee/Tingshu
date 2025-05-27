package com.ryan.service.impl;

import com.qcloud.vod.VodUploadClient;
import com.qcloud.vod.model.VodUploadRequest;
import com.qcloud.vod.model.VodUploadResponse;
import com.ryan.config.VodProperties;
import com.ryan.entity.TrackInfo;
import com.ryan.service.VodService;
import com.ryan.util.UploadFileUtil;
import com.tencentcloudapi.common.Credential;
import com.tencentcloudapi.common.exception.TencentCloudSDKException;
import com.tencentcloudapi.vod.v20180717.VodClient;
import com.tencentcloudapi.vod.v20180717.models.DeleteMediaRequest;
import com.tencentcloudapi.vod.v20180717.models.DescribeMediaInfosRequest;
import com.tencentcloudapi.vod.v20180717.models.DescribeMediaInfosResponse;
import com.tencentcloudapi.vod.v20180717.models.MediaInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Service
public class VodServiceImpl implements VodService {

    @Autowired
    private VodProperties vodProperties;

    @Override
    // 上传声音的业务实现，调用腾讯云的Vod
    public Map<String, Object> uploadTrack(MultipartFile file) throws Exception {
        // 声音上传的临时文件
        String tempPath = UploadFileUtil.uploadTempPath(vodProperties.getTempPath(), file);
        VodUploadClient client = new VodUploadClient(vodProperties.getSecretId(), vodProperties.getSecretKey());
        VodUploadRequest request = new VodUploadRequest();
        request.setMediaFilePath(tempPath);
        VodUploadResponse response = client.upload(vodProperties.getRegion(), request);
        Map<String, Object> map = new HashMap<>();
        map.put("mediaFileId", response.getFileId());
        map.put("mediaUrl", response.getMediaUrl());
        return map;
    }

    @Override
    // 获取声音的详细信息
    public void getTrackMediaInfo(TrackInfo trackInfo) throws TencentCloudSDKException {
        Credential cred = new Credential(vodProperties.getSecretId(), vodProperties.getSecretKey());
        // 实例化一个http选项，可选的，没有特殊需求可以跳过
        VodClient client = new VodClient(cred, vodProperties.getRegion());
        // 实例化一个请求对象，每个接口都会对应一个request对象
        DescribeMediaInfosRequest req = new DescribeMediaInfosRequest();
        String[] mediaFileIds = {trackInfo.getMediaFileId()};
        req.setFileIds(mediaFileIds);
        // 返回resp的是一个DescribeMediaInfosResponse的实例，与请求对象对应
        DescribeMediaInfosResponse resp = client.DescribeMediaInfos(req);
        if (resp.getMediaInfoSet().length > 0) {
            MediaInfo mediaInfo = resp.getMediaInfoSet()[0];
            // 设置持续时间setMediaDuration
            trackInfo.setMediaDuration(BigDecimal.valueOf(mediaInfo.getMetaData().getDuration()));
            trackInfo.setMediaType(mediaInfo.getBasicInfo().getType());
        }
    }

    // 删除声音
    @Override
    public void removeTrack(String mediaFileId) throws TencentCloudSDKException {
        Credential cred = new Credential(vodProperties.getSecretId(), vodProperties.getSecretKey());
        VodClient client = new VodClient(cred, vodProperties.getRegion());
        DeleteMediaRequest req = new DeleteMediaRequest();
        req.setFileId(mediaFileId);
        client.DeleteMedia(req);
    }
}