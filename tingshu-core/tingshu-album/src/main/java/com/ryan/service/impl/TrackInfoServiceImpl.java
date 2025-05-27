package com.ryan.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ryan.constant.SystemConstant;
import com.ryan.entity.AlbumInfo;
import com.ryan.entity.TrackInfo;
import com.ryan.entity.TrackStat;
import com.ryan.mapper.TrackInfoMapper;
import com.ryan.service.AlbumInfoService;
import com.ryan.service.TrackInfoService;
import com.ryan.service.TrackStatService;
import com.ryan.service.VodService;
import com.ryan.util.AuthContextHolder;
import com.tencentcloudapi.common.exception.TencentCloudSDKException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * 声音信息 服务实现类
 * </p>
 *
 * @author ryan
 * @since 2025-04-26
 */
@Service
public class TrackInfoServiceImpl extends ServiceImpl<TrackInfoMapper, TrackInfo> implements TrackInfoService {

    @Autowired
    private VodService vodService;

    @Autowired
    private AlbumInfoService albumInfoService;

    @Autowired
    private TrackStatService trackStatService;

    /**
     * 新增声音
     * @param trackInfo 声音信息
     */
    @Transactional
    @Override
    public void saveTrackInfo(TrackInfo trackInfo) throws TencentCloudSDKException {
        trackInfo.setUserId(AuthContextHolder.getUserId());
        trackInfo.setStatus(SystemConstant.TRACK_APPROVED); // 设置专辑状态为 已通过
        vodService.getTrackMediaInfo(trackInfo);
        // 查询专辑中声音编号最大的值
        LambdaQueryWrapper<TrackInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TrackInfo::getAlbumId, trackInfo.getAlbumId());
        wrapper.orderByAsc(TrackInfo::getOrderNum); // 根据专辑里的排序编号降序排序
        wrapper.select(TrackInfo::getOrderNum);
        wrapper.last("limit 1");
        TrackInfo maxOrderNumTrackInfo = getOne(wrapper);
        int orderNum = 1;
        if (maxOrderNumTrackInfo != null) {
            orderNum = maxOrderNumTrackInfo.getOrderNum() + 1;
        }
        trackInfo.setOrderNum(orderNum);
        // 保存声音
        save(trackInfo);
        // 更新专辑声音的个数
        AlbumInfo albumInfo = albumInfoService.getById(trackInfo.getAlbumId());
        int includeTrackCount = albumInfo.getIncludeTrackCount() + 1;
        albumInfo.setIncludeTrackCount(includeTrackCount);
        albumInfoService.updateById(albumInfo);
        // 初始化声音的统计信息
        List<TrackStat> trackStatList = buildTrackStatData(trackInfo.getId());
        trackStatService.saveBatch(trackStatList);
    }

    /**
     * 修改声音信息
     * @param trackInfo 声音信息
     */
    @Override
    public void updateTrackInfoById(TrackInfo trackInfo) throws TencentCloudSDKException {
        vodService.getTrackMediaInfo(trackInfo);
        updateById(trackInfo);
    }

    /**
     * 删除声音信息
     * @param trackId 声音id
     */
    @Transactional
    @Override
    public void deleteTrackInfo(Long trackId) throws TencentCloudSDKException {
        // 更新专辑声音个数
        TrackInfo trackInfo = getById(trackId);
        AlbumInfo albumInfo = albumInfoService.getById(trackInfo.getAlbumId());
        int includeTrackCount = albumInfo.getIncludeTrackCount() - 1;
        albumInfo.setIncludeTrackCount(includeTrackCount);
        albumInfoService.updateById(albumInfo);
        removeById(trackId);
        // 删除统计信息
        trackStatService.remove(new LambdaQueryWrapper<TrackStat>()
                .eq(TrackStat::getTrackId, trackId));
        // 删除声音
        vodService.removeTrack(trackInfo.getMediaFileId());
    }

    // 初始化专辑的信息
    private List<TrackStat> buildTrackStatData(Long trackId) {
        List<TrackStat> trackStatList = new ArrayList<>();
        initTrackStat(trackId, trackStatList, SystemConstant.PLAY_NUM_ALBUM);
        initTrackStat(trackId, trackStatList, SystemConstant.SUBSCRIBE_NUM_ALBUM);
        initTrackStat(trackId, trackStatList, SystemConstant.BUY_NUM_ALBUM);
        initTrackStat(trackId, trackStatList, SystemConstant.COMMENT_NUM_ALBUM);
        return trackStatList;
    }

    private static void initTrackStat(Long trackId, List<TrackStat> trackStatList, String statType) {
        TrackStat trackStat = new TrackStat();
        trackStat.setTrackId(trackId);
        trackStat.setStatType(statType);
        trackStat.setStatNum(0);
        trackStatList.add(trackStat);
    }
}
