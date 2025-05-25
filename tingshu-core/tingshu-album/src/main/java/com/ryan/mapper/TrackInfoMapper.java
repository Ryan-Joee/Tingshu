package com.ryan.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.ryan.entity.TrackInfo;
import com.ryan.query.TrackInfoQuery;
import com.ryan.vo.TrackTempVo;
import feign.Param;

/**
 * <p>
 * 声音信息 Mapper 接口
 * </p>
 *
 * @author ryan
 * @since 2025-04-26
 */
public interface TrackInfoMapper extends BaseMapper<TrackInfo> {

    /**
     * 分页查询声音
     * @param pageParam 分页参数
     * @param trackInfoQuery 声音分页查询条件
     * @return IPage<TrackTempVo>
     */
    IPage<TrackTempVo> findUserTrackPage(@Param("pageParam") IPage<TrackTempVo> pageParam,
                                         @Param("trackInfoQuery") TrackInfoQuery trackInfoQuery);
}
