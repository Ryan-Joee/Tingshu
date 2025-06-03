package com.ryan.mapper;

import com.ryan.entity.AlbumStat;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ryan.vo.AlbumStatVo;
import org.apache.ibatis.annotations.Param;

/**
 * <p>
 * 专辑统计 Mapper 接口
 * </p>
 *
 * @author ryan
 * @since 2025-04-26
 */
public interface AlbumStatMapper extends BaseMapper<AlbumStat> {

    /**
     * 获取专辑统计信息
     * @param albumId 专辑id
     * @return AlbumSateVo
     */
    AlbumStatVo getAlbumStatInfo(@Param("albumId") Long albumId);
}
