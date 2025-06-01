package com.ryan.service.impl;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.aggregations.Aggregate;
import co.elastic.clients.elasticsearch._types.query_dsl.TermsQueryField;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import com.alibaba.fastjson.JSONObject;
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

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    /**
     * 下架专辑
     * @param albumId 专辑id
     */
    @Override
    public void offSaleAlbum(Long albumId) {
       albumRepository.deleteById(albumId);
    }

    @Autowired
    private ElasticsearchClient elasticsearchClient;
    /**
     * 获取主页频道数据
     * @param category1Id 一级分类id
     */
    @Override
    public List<Map<String, Object>> getChannelData(Long category1Id) throws IOException {
        List<BaseCategory3> category3List = categoryFeignClient.getCategory3ListByCategory1Id(category1Id).getData();
        //1.建立三级分类id和三级分类对象的映射
        Map<Long, BaseCategory3> category3Map = category3List.stream()
                .collect(Collectors.toMap(BaseCategory3::getId, baseCategory3 -> baseCategory3));
        //2.把三级分类id转换为FieldValue类型给搜索的时候用
        List<FieldValue> category3FieldValueList = category3List.stream()
                //FieldValue.of的作用是把id包装成为_kind和_value类型
                .map(BaseCategory3::getId).map(x -> FieldValue.of(x)).collect(Collectors.toList());
        //3.根据三级分类id查询并构建聚合信息--看搜索语句
        SearchResponse<AlbumInfoIndex> response = elasticsearchClient.search(s -> s
                .index("albuminfo")
                .size(0)
                //类似于mysql中category3Id in (1,2,3)
                .query(q -> q.terms(t -> t.field("category3Id")
                        .terms(new TermsQueryField.Builder().value(category3FieldValueList).build())))
                .aggregations("category3IdAgg", parentA -> parentA.terms(t -> t.field("category3Id"))
                        .aggregations("topSixHotScoreAgg", childA -> childA.topHits(t -> t.size(6)
                                .sort(sort -> sort.field(f -> f.field("hotScore").order(SortOrder.Desc)))))
                ), AlbumInfoIndex.class);
        //4.解析聚合信息到对象中
        Aggregate category3IdAgg = response.aggregations().get("category3IdAgg");
        List<Map<String, Object>> topAlbumInfoIndexMapList = category3IdAgg.lterms().buckets().array().stream().map(bucket -> {
            Long category3Id = bucket.key();
            Aggregate topSixHotScoreAgg = bucket.aggregations().get("topSixHotScoreAgg");
            List<AlbumInfoIndex> topAlbumInfoIndexList = topSixHotScoreAgg.topHits().hits().hits().stream().map(hit ->
                    JSONObject.parseObject(hit.source().toString(), AlbumInfoIndex.class)).collect(Collectors.toList());
            Map<String, Object> retMap = new HashMap<>();
            retMap.put("baseCategory3", category3Map.get(category3Id));
            retMap.put("list", topAlbumInfoIndexList);
            return retMap;
        }).collect(Collectors.toList());
        return topAlbumInfoIndexMapList;
    }
}
