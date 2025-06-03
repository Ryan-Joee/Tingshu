package com.ryan.service;

import com.ryan.query.AlbumIndexQuery;
import com.ryan.vo.AlbumSearchResponseVo;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

    /**
     * 关键字补全
     * @param keyword 关键字
     */
    Set<String> autoCompleteSuggest(String keyword) throws IOException;

    /**
     * 获取专辑详情信息
     *
     * @param albumId 专辑id
     * @return Map<String, Object>
     */
    Map<String, Object> getAlbumDetail(Long albumId);
}
