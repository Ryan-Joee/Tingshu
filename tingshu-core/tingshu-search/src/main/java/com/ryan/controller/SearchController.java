package com.ryan.controller;

import com.ryan.query.AlbumIndexQuery;
import com.ryan.result.RetVal;
import com.ryan.service.SearchService;
import com.ryan.vo.AlbumSearchResponseVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Tag(name = "搜索专辑管理")
@RestController
@RequestMapping("/api/search/albumInfo")
public class SearchController {
    @Autowired
    private SearchService searchService;

    @Operation(summary = "上架专辑")
    @GetMapping("onSaleAlbum/{albumId}")
    public void onSaleAlbum(@PathVariable Long albumId) {
        searchService.onSaleAlbum(albumId);
    }

    @Operation(summary = "批量上架专辑")
    @GetMapping("batchOnSaleAlbum")
    public String batchOnSaleAlbum() {
        for (long i = 1; i < 1577; i++) {
            searchService.onSaleAlbum(i);
        }
        return "success";
    }

    @Operation(summary = "下架专辑")
    @GetMapping("offSaleAlbum/{albumId}")
    public void offSaleAlbum(@PathVariable Long albumId) {
        searchService.offSaleAlbum(albumId);
    }

    @Operation(summary = "获取主页频道数据")
    @GetMapping("getChannelData/{category1Id}")
    public RetVal<List<Map<String, Object>>> getChannelData(@PathVariable Long category1Id) throws IOException {
        List<Map<String, Object>> channelData = searchService.getChannelData(category1Id);
        return RetVal.ok(channelData);
    }

    @Operation(summary = "专辑搜索")
    @PostMapping
    public RetVal  search(@RequestBody AlbumIndexQuery albumIndexQuery) throws IOException {
        AlbumSearchResponseVo searchResponseVo = searchService.search(albumIndexQuery);
        return RetVal.ok(searchResponseVo);
    }




}
