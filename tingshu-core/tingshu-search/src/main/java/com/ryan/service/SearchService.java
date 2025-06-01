package com.ryan.service;

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
}
