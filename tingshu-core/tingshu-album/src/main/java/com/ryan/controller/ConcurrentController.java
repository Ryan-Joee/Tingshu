package com.ryan.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.ryan.entity.AlbumInfo;
import com.ryan.service.AlbumInfoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.redisson.api.RBloomFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "并发管理接口")
@RestController
@RequestMapping(value = "/api/album")
public class ConcurrentController {
    @Autowired
    private RBloomFilter bloomFilter;

    @Autowired
    private AlbumInfoService albumInfoService;

    @Operation(summary = "初始化布隆过滤器")
    @GetMapping("init")
    public String init() {
        // 查询数据库里专辑ID
        LambdaQueryWrapper<AlbumInfo> wrapper = Wrappers.lambdaQuery(AlbumInfo.class)
                .select(AlbumInfo::getId);
        List<AlbumInfo> albumInfoList = albumInfoService.list(wrapper);
        for (AlbumInfo albumInfo : albumInfoList) {
            Long albumInfoId = albumInfo.getId();
            // 将id放到布隆过滤器里
            bloomFilter.add(albumInfoId);
        }
        return null;
    }
}
