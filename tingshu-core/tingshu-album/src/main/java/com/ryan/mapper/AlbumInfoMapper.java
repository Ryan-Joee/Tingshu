package com.ryan.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.ryan.entity.AlbumInfo;
import com.ryan.query.AlbumInfoQuery;
import com.ryan.vo.AlbumTempVo;
import feign.Param;

/**
 * <p>
 * 专辑信息 Mapper 接口
 * </p>
 *
 * @author ryan
 * @since 2025-04-26
 */
public interface AlbumInfoMapper extends BaseMapper<AlbumInfo> {

    IPage<AlbumTempVo> getUserAlbumByPage(@Param("pageParam")IPage<AlbumTempVo> pageParam, @Param("albumInfoQuery") AlbumInfoQuery albumInfoQuery);
}
