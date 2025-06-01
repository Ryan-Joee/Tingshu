package com.ryan;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

//@FeignClient(value = "tingshu-album", fallback = AlbumInfoFallBack.class)
@FeignClient(value = "tingshu-search")
public interface SearchFeignClient {
    @GetMapping("api/search/albumInfo/onSaleAlbum/{albumId}")
    void onSaleAlbum(@PathVariable Long albumId);

    @GetMapping("api/search/albumInfo/offSaleAlbum/{albumId}")
    void offSaleAlbum(@PathVariable Long albumId);

}
