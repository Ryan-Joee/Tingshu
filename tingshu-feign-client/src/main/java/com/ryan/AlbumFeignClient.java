package com.ryan;

import com.ryan.entity.AlbumAttributeValue;
import com.ryan.entity.AlbumInfo;
import com.ryan.result.RetVal;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

//@FeignClient(value = "tingshu-album", fallback = AlbumInfoFallBack.class)
@FeignClient(value = "tingshu-album")
public interface AlbumFeignClient {
    @GetMapping("/api/album/albumInfo/getAlbumInfoById/{albumId}")
    RetVal<AlbumInfo> getAlbumInfoById(@PathVariable Long albumId);

    @GetMapping("/api/album/albumInfo/getAlbumInfoPropertyValue/{albumId}")
    List<AlbumAttributeValue> getAlbumInfoPropertyValue(@PathVariable Long albumId);

}
