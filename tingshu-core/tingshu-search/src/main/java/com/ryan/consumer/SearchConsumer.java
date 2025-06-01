package com.ryan.consumer;

import com.ryan.constant.KafkaConstant;
import com.ryan.service.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class SearchConsumer {
    @Autowired
    private SearchService searchService;
    // 专辑上架
    @KafkaListener(topics = KafkaConstant.ONSALE_ALBUM_QUEUE)
    public void onSaleAlbum(Long albumId) {
        if (null != albumId) {
            searchService.onSaleAlbum(albumId);
        }
    }
    //专辑下架
    @KafkaListener(topics = KafkaConstant.OFFSALE_ALBUM_QUEUE)
    public void offSaleAlbum(Long albumId) {
        if (null != albumId) {
            searchService.offSaleAlbum(albumId);
        }
    }
}
