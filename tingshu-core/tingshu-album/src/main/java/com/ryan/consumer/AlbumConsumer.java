package com.ryan.consumer;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ryan.constant.KafkaConstant;
import com.ryan.constant.SystemConstant;
import com.ryan.entity.AlbumStat;
import com.ryan.entity.TrackStat;
import com.ryan.service.AlbumStatService;
import com.ryan.service.TrackInfoService;
import com.ryan.service.TrackStatService;
import com.ryan.vo.TrackStatMqVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class AlbumConsumer {
    @Autowired
    private AlbumStatService albumStatService;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private TrackStatService trackStatService;
    @Autowired
    private TrackInfoService trackInfoService;
    @KafkaListener(topics = KafkaConstant.UPDATE_TRACK_STAT_QUEUE)
    public void updateStat(String dataJson) {
        TrackStatMqVo trackStatVo = JSON.parseObject(dataJson, TrackStatMqVo.class);
        //业务去重
        String key = trackStatVo.getBusinessNo();
        boolean isExist = redisTemplate.opsForValue().setIfAbsent(key, 1, 1, TimeUnit.HOURS);
        if (isExist) {
            //更新声音统计信息
            String statType = trackStatVo.getStatType();
            LambdaQueryWrapper<TrackStat> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(TrackStat::getTrackId,trackStatVo.getTarckId());
            wrapper.eq(TrackStat::getStatType,statType);
            TrackStat trackStat = trackStatService.getOne(wrapper);
            trackStat.setStatNum(trackStat.getStatNum()+trackStatVo.getCount());
            trackStatService.updateById(trackStat);
            if(statType.equals(SystemConstant.PLAY_NUM_TRACK)){
                //更新专辑统计信息
                LambdaQueryWrapper<AlbumStat> albumWrapper = new LambdaQueryWrapper<>();
                albumWrapper.eq(AlbumStat::getAlbumId,trackStatVo.getAlbumId());
                //播放量
                albumWrapper.eq(AlbumStat::getStatType,SystemConstant.PLAY_NUM_ALBUM);
                AlbumStat albumStat = albumStatService.getOne(albumWrapper);
                //专辑的播放量 = 声音播放量的总和
                albumStat.setStatNum(albumStat.getStatNum()+trackStatVo.getCount());
                albumStatService.updateById(albumStat);
            }
        }
    }
}
