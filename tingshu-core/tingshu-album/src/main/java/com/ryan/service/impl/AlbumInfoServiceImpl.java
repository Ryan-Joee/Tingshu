package com.ryan.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ryan.constant.RedisConstant;
import com.ryan.constant.SystemConstant;
import com.ryan.entity.AlbumAttributeValue;
import com.ryan.entity.AlbumInfo;
import com.ryan.entity.AlbumStat;
import com.ryan.mapper.AlbumInfoMapper;
import com.ryan.service.AlbumAttributeValueService;
import com.ryan.service.AlbumInfoService;
import com.ryan.service.AlbumStatService;
import com.ryan.util.AuthContextHolder;
import com.ryan.util.SleepUtils;
import org.jetbrains.annotations.NotNull;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 * 专辑信息 服务实现类
 * </p>
 *
 * @author ryan
 * @since 2025-04-26
 */
@Service
public class AlbumInfoServiceImpl extends ServiceImpl<AlbumInfoMapper, AlbumInfo> implements AlbumInfoService {

    @Autowired
    private AlbumAttributeValueService albumAttributeValueService;

    @Autowired
    private AlbumStatService albumStatService;

    // 面试题：什么是事务？
    // 新增专辑
    @Transactional
    @Override
    public void saveAlbumInfo(AlbumInfo albumInfo) {
        Long userId = AuthContextHolder.getUserId();
        albumInfo.setUserId(userId);
        albumInfo.setStatus(SystemConstant.ALBUM_APPROVED);
        // 付费专辑：前5集免费
        if (!SystemConstant.FREE_ALBUM.equals(albumInfo.getPayType())) {
            // 表示设定前5集免费听
            albumInfo.setTracksForFree(5);
        }
        save(albumInfo);
        // 保存专辑标签属性
        List<AlbumAttributeValue> albumPropertyValueList = albumInfo.getAlbumPropertyValueList();
        if (!CollectionUtils.isEmpty(albumPropertyValueList)) {
            for (AlbumAttributeValue albumAttributeValue : albumPropertyValueList) {
                // 设置专辑id 是否能拿到id？

                albumAttributeValue.setAlbumId(albumInfo.getId());
                // 如果是如下的保存方式，会在每次for循环都对数据库进行保存操作，这样可能会大量消耗数据库资源
                // albumAttributeValueService.save(albumAttributeValue);
            }
            // 改进方法： 使用批量保存
            albumAttributeValueService.saveBatch(albumPropertyValueList);
        }
        // 保存专辑的统计信息
        List<AlbumStat> albumStatList = buildAlbumStatData(albumInfo.getId());
        albumStatService.saveBatch(albumStatList);
        // TODO 后面
    }


    // 根据id查询专辑信息
    @Override
    public AlbumInfo getAlbumInfoById(Long albumId) {
//        AlbumInfo albumInfo = getAlbumInfoFromDB(albumId);
//        AlbumInfo albumInfo = getAlbumInfoFromRedis(albumId);
//        AlbumInfo albumInfo = getAlbumInfoFromRedisWithThreadLocal(albumId);
        AlbumInfo albumInfo = getAlbumInfoFromRedisson(albumId);
        return albumInfo;
    }

    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private RBloomFilter bloomFilter;

    private AlbumInfo getAlbumInfoFromRedisson(Long albumId) {
        String cacheKey = RedisConstant.ALBUM_INFO_PREFIX + albumId;
        AlbumInfo albumInfoRedis  = (AlbumInfo) redisTemplate.opsForValue().get(cacheKey);
        String lockKey="lock-" + albumId;
        RLock lock = redissonClient.getLock(lockKey);
        if (albumInfoRedis == null) {
            lock.lock();
            try {
                // 先判断布隆过滤器中是否存在albumId
                // 布隆里没有那数据库绝对没有；布隆里有那数据库可能有-> 查数据库
                boolean flag = bloomFilter.contains(albumId);
                if (flag) {
                    AlbumInfo albumInfoFromDB = getAlbumInfoFromDB(albumId);
                    redisTemplate.opsForValue().set(cacheKey, albumInfoFromDB);
                    return albumInfoFromDB;
                }
            } finally {
                lock.unlock();
            }
        }
        return albumInfoRedis;
    }


    // 添加分布式锁
    ThreadLocal<String> threadLocal = new ThreadLocal<>();
    private AlbumInfo getAlbumInfoFromRedisWithThreadLocal(Long albumId) {
        String cacheKey = RedisConstant.ALBUM_INFO_PREFIX + albumId;
        AlbumInfo albumInfoRedis = (AlbumInfo) redisTemplate.opsForValue().get(cacheKey);
        //锁的粒度太大
        String lockKey="lock-"+albumId;
        if (albumInfoRedis == null) {
            String token = threadLocal.get();
            boolean accquireLock = false;
            if (!StringUtils.isEmpty(token)) {
                //已经拿到过锁了
                accquireLock = true;
            } else {
                token = UUID.randomUUID().toString();
                accquireLock = redisTemplate.opsForValue().setIfAbsent(lockKey, token, 3, TimeUnit.SECONDS);
            }
            if (accquireLock) {
                //doBusiness
                AlbumInfo albumInfoDb = getAlbumInfoFromDB(albumId);
                redisTemplate.opsForValue().set(cacheKey, albumInfoDb);
                String luaScript = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
                DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
                redisScript.setScriptText(luaScript);
                redisScript.setResultType(Long.class);
                redisTemplate.execute(redisScript, Arrays.asList(lockKey), token);
                //擦屁股
                threadLocal.remove();
                return albumInfoDb;
            } else {
                while (true) {
                    SleepUtils.millis(50);
                    boolean retryAccquireLock = redisTemplate.opsForValue().setIfAbsent(lockKey, token, 3, TimeUnit.SECONDS);
                    if (retryAccquireLock) {
                        threadLocal.set(token);
                        break;
                    }
                }
                return getAlbumInfoFromRedisWithThreadLocal(albumId);
            }
        }
        return albumInfoRedis;
    }

    @Autowired
    private RedisTemplate redisTemplate;
    // 加入redis缓存 => 先查redis，redis中没有再查数据库，减少查询数据库的次数
    private AlbumInfo getAlbumInfoFromRedis(Long albumId) {
        String cacheKey = RedisConstant.ALBUM_INFO_PREFIX + albumId;
        AlbumInfo albumInfoRedis  = (AlbumInfo) redisTemplate.opsForValue().get(cacheKey);
        if (albumInfoRedis == null) {
            AlbumInfo albumInfoFromDB = getAlbumInfoFromDB(albumId);
            redisTemplate.opsForValue().set(cacheKey, albumInfoFromDB);
            return albumInfoFromDB;
        }
        return albumInfoRedis;
    }

    @NotNull
    private AlbumInfo getAlbumInfoFromDB(Long albumId) {
        AlbumInfo albumInfo = getById(albumId);
        LambdaQueryWrapper<AlbumAttributeValue> wrapper = new LambdaQueryWrapper<>();
        // wrapper.eq() => 用于构建 SQL 中的 WHERE column = value 条件
        wrapper.eq(AlbumAttributeValue::getAlbumId, albumId);
        List<AlbumAttributeValue> albumAttributeValueList = albumAttributeValueService.list(wrapper);
        albumInfo.setAlbumPropertyValueList(albumAttributeValueList);
        return albumInfo;
    }

    // 根据id修改专辑信息
    @Override
    public void updateAlbumInfo(AlbumInfo albumInfo) {
        // 修改专辑基本信息
        updateById(albumInfo);
        // 删除原有专辑标签属性信息
        LambdaQueryWrapper<AlbumAttributeValue> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AlbumAttributeValue::getAlbumId, albumInfo.getId());
        albumAttributeValueService.remove(wrapper);
        // 保存专辑标签属性
        List<AlbumAttributeValue> albumPropertyValueList = albumInfo.getAlbumPropertyValueList();
        if (!CollectionUtils.isEmpty(albumPropertyValueList)) {
            for (AlbumAttributeValue albumAttributeValue : albumPropertyValueList) {
                // 设置专辑id
                albumAttributeValue.setAlbumId(albumInfo.getId());
            }
            albumAttributeValueService.saveBatch(albumPropertyValueList);
        }
        // TODO 还有其它事情要做
    }

    // 删除专辑信息
    @Override
    public void deleteAlbumInfo(Long albumId) {
        // 1. 先删除专辑信息
        removeById(albumId);
        // 2. 删除专辑标签属性信息
        LambdaQueryWrapper<AlbumAttributeValue> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AlbumAttributeValue::getAlbumId, albumId);
        albumAttributeValueService.remove(wrapper);
        // 3. 删除专辑的统计信息
        albumStatService.remove(new LambdaQueryWrapper<AlbumStat>().eq(AlbumStat::getAlbumId, albumId));
        // TODO 还有事情要做
    }


    // 初始化专辑的信息
    private List<AlbumStat> buildAlbumStatData(Long albumId) {
        ArrayList<AlbumStat> albumStatList = new ArrayList<>();
        initAlbumStat(albumId, albumStatList, SystemConstant.PLAY_NUM_ALBUM);
        initAlbumStat(albumId, albumStatList, SystemConstant.SUBSCRIBE_NUM_ALBUM);
        initAlbumStat(albumId, albumStatList, SystemConstant.BUY_NUM_ALBUM);
        initAlbumStat(albumId, albumStatList, SystemConstant.COMMENT_NUM_ALBUM);
        return albumStatList;
    }

    private static void initAlbumStat(Long albumId, ArrayList<AlbumStat> albumStatList, String statType) {
        AlbumStat albumStat = new AlbumStat();
        albumStat.setAlbumId(albumId);
        albumStat.setStatType(statType);
        albumStat.setStatNum(0);
        albumStatList.add(albumStat);
    }
}
