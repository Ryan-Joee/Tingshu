package com.ryan;

import com.ryan.entity.BaseCategoryView;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

//@FeignClient(value = "tingshu-album", fallback = AlbumInfoFallBack.class)
@FeignClient(value = "tingshu-album")
public interface CategoryFeignClient {
    @GetMapping("/api/album/category/getCategoryView/{category3Id}")
    BaseCategoryView getCategoryView(@PathVariable Long category3Id);
}
