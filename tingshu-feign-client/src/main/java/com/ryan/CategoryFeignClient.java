package com.ryan;

import com.ryan.entity.BaseCategory3;
import com.ryan.entity.BaseCategoryView;
import com.ryan.result.RetVal;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

//@FeignClient(value = "tingshu-album", fallback = AlbumInfoFallBack.class)
@FeignClient(value = "tingshu-album")
public interface CategoryFeignClient {
    @GetMapping("/api/album/category/getCategoryView/{category3Id}")
    BaseCategoryView getCategoryView(@PathVariable Long category3Id);

    @GetMapping("/api/album/category/getCategory3ListByCategory1Id/{category1Id}")
    RetVal<List<BaseCategory3>> getCategory3ListByCategory1Id(@PathVariable Long category1Id);
}
