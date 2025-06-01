package com.ryan.service.impl;

import com.ryan.AlbumFeignClient;
import com.ryan.CategoryFeignClient;
import com.ryan.UserFeignClient;
import com.ryan.entity.*;
import com.ryan.exception.TingshuException;
import com.ryan.repository.AlbumRepository;
import com.ryan.result.ResultCodeEnum;
import com.ryan.service.SearchService;
import com.ryan.vo.UserInfoVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Service
public class SearchServiceImpl implements SearchService {

    @Autowired
    private AlbumRepository albumRepository;

    @Autowired
    private AlbumFeignClient albumFeignClient;

    @Autowired
    private CategoryFeignClient categoryFeignClient;

    @Autowired
    private UserFeignClient userFeignClient;

    /**
     * 上架专辑
     * @param albumId 专辑id
     */
    @Override
    public void onSaleAlbum(Long albumId) {
        AlbumInfoIndex albumInfoIndex = new AlbumInfoIndex();
        // 根据albumId查询albumInfo信息
        AlbumInfo albumInfo = albumFeignClient.getAlbumInfoById(albumId).getData();
        if (albumInfo == null) {
            throw new TingshuException(ResultCodeEnum.DATA_ERROR);
        }
        BeanUtils.copyProperties(albumInfo, albumInfoIndex);
        // 根据albumId查询专辑属性值
        List<AlbumAttributeValue> albumPropertyValueList = albumFeignClient.getAlbumInfoPropertyValue(albumId);
        if (!CollectionUtils.isEmpty(albumPropertyValueList)) {
            List<AttributeValueIndex> valueIndexList = albumPropertyValueList.stream().map(albumPropertyValue -> {
                AttributeValueIndex attributeValueIndex = new AttributeValueIndex();
                BeanUtils.copyProperties(albumPropertyValue, attributeValueIndex);
                return attributeValueIndex;
            }).collect(Collectors.toList());
            albumInfoIndex.setAttributeValueIndexList(valueIndexList);
        }
        // 根据三级分类id查询专辑的分类信息
        BaseCategoryView categoryView = categoryFeignClient.getCategoryView(albumInfo.getCategory3Id());
        if (categoryView == null) {
            throw new TingshuException(ResultCodeEnum.DATA_ERROR);
        }
        albumInfoIndex.setCategory1Id(categoryView.getCategory1Id());
        albumInfoIndex.setCategory2Id(categoryView.getCategory2Id());
        // 根据用户id查询用户信息
        UserInfoVo userInfo = userFeignClient.getUserById(albumInfo.getUserId()).getData();
        albumInfoIndex.setAnnouncerName(userInfo.getNickname());
        //更新统计量与得分，默认随机，方便测试
        int num1 = new Random().nextInt(1000);
        int num2 = new Random().nextInt(100);
        int num3 = new Random().nextInt(50);
        int num4 = new Random().nextInt(300);
        albumInfoIndex.setPlayStatNum(num1);
        albumInfoIndex.setSubscribeStatNum(num2);
        albumInfoIndex.setBuyStatNum(num3);
        albumInfoIndex.setCommentStatNum(num4);
        //计算公式：未实现 使用模拟数据
        double hotScore = num1 * 0.2 + num2 * 0.3 + num3 * 0.4 + num4 * 0.1;
        albumInfoIndex.setHotScore(hotScore);
         albumRepository.save(albumInfoIndex);
    }
}
