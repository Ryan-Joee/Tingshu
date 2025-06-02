package com.ryan.service;

import com.ryan.query.AlbumIndexQuery;
import com.ryan.vo.AlbumSearchResponseVo;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface SearchService {
    /**
     * 上架专辑
     * @param albumId 专辑id
     */
    void onSaleAlbum(Long albumId);

    /**
     * 下架专辑
     * @param albumId 专辑id
     */
    void offSaleAlbum(Long albumId);

    /**
     * 获取主页频道数据
     * @param category1Id 一级分类id
     */
    List<Map<String, Object>> getChannelData(Long category1Id) throws IOException;

    /**
     * 专辑搜索
     * @param albumIndexQuery 专辑信息
     * @return AlbumSearchResponseVo
     */
    AlbumSearchResponseVo search(AlbumIndexQuery albumIndexQuery) throws IOException;
}
