package com.ryan.service.impl;

import com.alibaba.fastjson.JSON;
import com.ryan.constant.KafkaConstant;
import com.ryan.constant.SystemConstant;
import com.ryan.entity.UserCollect;
import com.ryan.entity.UserListenProcess;
import com.ryan.service.KafkaService;
import com.ryan.service.ListenService;
import com.ryan.util.AuthContextHolder;
import com.ryan.util.MongoUtil;
import com.ryan.vo.TrackStatMqVo;
import com.ryan.vo.UserListenProcessVo;
import org.bson.types.ObjectId;
import org.joda.time.DateTime;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class ListenServiceImpl implements ListenService {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private KafkaService kafkaService;
    @Override
    public void updatePlaySecond(UserListenProcessVo userListenProcessVo) {
        Long userId = AuthContextHolder.getUserId();
        //1.在mongodb中查询用户播放记录
        Query query = Query.query(Criteria.where("userId").is(userId).and("trackId").is(userListenProcessVo.getTrackId()));
        UserListenProcess userListenProcess = mongoTemplate.findOne(query, UserListenProcess.class, MongoUtil.getCollectionName(MongoUtil.MongoCollectionEnum.USER_LISTEN_PROCESS, userId));
        if(null == userListenProcess) {
            //2.如果没有播放记录就往mongodb中添加一条
            userListenProcess = new UserListenProcess();
            BeanUtils.copyProperties(userListenProcessVo, userListenProcess);
            userListenProcess.setId(ObjectId.get().toString());
            userListenProcess.setUserId(userId);
            userListenProcess.setIsShow(1);
            userListenProcess.setCreateTime(new Date());
            userListenProcess.setUpdateTime(new Date());
            //构造mongo存储信息与集合名称
            mongoTemplate.save(userListenProcess, MongoUtil.getCollectionName(MongoUtil.MongoCollectionEnum.USER_LISTEN_PROCESS, userId));
        } else {
            //3.否则在mongodb中更新一个播放记录信息
            userListenProcess.setUpdateTime(new Date());
            userListenProcess.setBreakSecond(userListenProcessVo.getBreakSecond());
            mongoTemplate.save(userListenProcess, MongoUtil.getCollectionName(MongoUtil.MongoCollectionEnum.USER_LISTEN_PROCESS, userId));
        }

        //4.记录专辑与声音同一个用户同一个声音每天只记录一次播放量
        String key = "user:track:" + new DateTime().toString("yyyyMMdd") + ":" + userListenProcessVo.getTrackId();
        Boolean isExist = redisTemplate.opsForValue().getBit(key, userId);
        if(!isExist) {
            redisTemplate.opsForValue().setBit(key, userId, true);
            //设置键一天后过期
            LocalDateTime localDateTime = LocalDateTime.now().plusDays(1).plusHours(0).plusMinutes(0).plusSeconds(0).plusNanos(0);
            long timeout = ChronoUnit.SECONDS.between(LocalDateTime.now(),localDateTime);
            redisTemplate.expire(key, timeout, TimeUnit.SECONDS);
            //发送消息，更新播放量统计
            TrackStatMqVo trackStatVo = new TrackStatMqVo();
            trackStatVo.setBusinessNo(UUID.randomUUID().toString().replaceAll("-",""));
            trackStatVo.setAlbumId(userListenProcessVo.getAlbumId());
            trackStatVo.setTarckId(userListenProcessVo.getTrackId());
            trackStatVo.setStatType(SystemConstant.PLAY_NUM_ALBUM);
            trackStatVo.setCount(1);
            kafkaService.sendMessage(KafkaConstant.UPDATE_TRACK_STAT_QUEUE, JSON.toJSONString(trackStatVo));
        }
    }

    @Override
    public BigDecimal getLastPlaySecond(Long trackId) {
        Long userId = AuthContextHolder.getUserId();
        Query query = Query.query(Criteria.where("userId").is(userId).and("trackId").is(trackId));
        UserListenProcess userListenProcess = mongoTemplate.findOne(query, UserListenProcess.class,
                MongoUtil.getCollectionName(MongoUtil.MongoCollectionEnum.USER_LISTEN_PROCESS, userId));
        if(null != userListenProcess) return userListenProcess.getBreakSecond();
        return new BigDecimal(0);
    }

    @Override
    public Map<String, Object> getRecentlyPlay() {
        Long userId = AuthContextHolder.getUserId();
        Query query = Query.query(Criteria.where("userId").is(userId));
        Sort sort = Sort.by(Sort.Direction.DESC, "updateTime");
        query.with(sort);
        UserListenProcess userListenProcess = mongoTemplate.findOne(query, UserListenProcess.class, MongoUtil.getCollectionName(MongoUtil.MongoCollectionEnum.USER_LISTEN_PROCESS, userId));
        if(null == userListenProcess) {
            return null;
        }
        Map<String, Object> map = new HashMap<>();
        map.put("albumId", userListenProcess.getAlbumId());
        map.put("trackId", userListenProcess.getTrackId());
        return map;
    }

    @Override
    public boolean collectTrack(Long trackId) {
        Long userId = AuthContextHolder.getUserId();
        Query query = Query.query(Criteria.where("userId").is(userId).and("trackId").is(trackId));
        long count = mongoTemplate.count(query, MongoUtil.getCollectionName(MongoUtil.MongoCollectionEnum.USER_COLLECT, userId));
        if(count == 0) {
            //在mongodb中添加用户收藏的信息
            UserCollect userCollect = new UserCollect();
            userCollect.setId(ObjectId.get().toString());
            userCollect.setUserId(userId);
            userCollect.setTrackId(trackId);
            userCollect.setCreateTime(new Date());
            mongoTemplate.save(userCollect, MongoUtil.getCollectionName(MongoUtil.MongoCollectionEnum.USER_COLLECT, userId));

            //发送消息，更新声音统计数量加1
            TrackStatMqVo trackStatMqVo = new TrackStatMqVo();
            trackStatMqVo.setBusinessNo(UUID.randomUUID().toString().replaceAll("-",""));
            trackStatMqVo.setTarckId(trackId);
            trackStatMqVo.setStatType(SystemConstant.COLLECT_NUM_TRACK);
            trackStatMqVo.setCount(1);
            kafkaService.sendMessage(KafkaConstant.UPDATE_TRACK_STAT_QUEUE, JSON.toJSONString(trackStatMqVo));
            return true;
        } else {
            mongoTemplate.remove(query, MongoUtil.getCollectionName(MongoUtil.MongoCollectionEnum.USER_COLLECT, userId));
            //发送消息，更新声音统计数量减1
            TrackStatMqVo trackStatMqVo = new TrackStatMqVo();
            trackStatMqVo.setBusinessNo(UUID.randomUUID().toString().replaceAll("-",""));
            trackStatMqVo.setTarckId(trackId);
            trackStatMqVo.setStatType(SystemConstant.COLLECT_NUM_TRACK);
            trackStatMqVo.setCount(-1);
            kafkaService.sendMessage(KafkaConstant.UPDATE_TRACK_STAT_QUEUE, JSON.toJSONString(trackStatMqVo));
            return false;
        }
    }
}
