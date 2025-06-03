package com.ryan.service.impl;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.aggregations.Aggregate;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.NestedQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.TermsQueryField;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.elasticsearch.core.search.Suggester;
import co.elastic.clients.elasticsearch.core.search.Suggestion;
import com.alibaba.fastjson.JSONObject;
import com.ryan.AlbumFeignClient;
import com.ryan.CategoryFeignClient;
import com.ryan.UserFeignClient;
import com.ryan.constant.RedisConstant;
import com.ryan.entity.*;
import com.ryan.exception.TingshuException;
import com.ryan.query.AlbumIndexQuery;
import com.ryan.repository.AlbumRepository;
import com.ryan.repository.SuggestRepository;
import com.ryan.result.ResultCodeEnum;
import com.ryan.service.SearchService;
import com.ryan.util.PinYinUtils;
import com.ryan.vo.AlbumInfoIndexVo;
import com.ryan.vo.AlbumSearchResponseVo;
import com.ryan.vo.AlbumStatVo;
import com.ryan.vo.UserInfoVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.suggest.Completion;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ThreadPoolExecutor;
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

    @Autowired
    private SuggestRepository suggestRepository;

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
        // 专辑自动补全的内容
        SuggestIndex suggestIndex = new SuggestIndex();
        suggestIndex.setId(UUID.randomUUID().toString().replaceAll("-", ""));
        suggestIndex.setTitle(albumInfoIndex.getAlbumTitle());
        suggestIndex.setKeyword(new Completion(new String[]{albumInfoIndex.getAlbumTitle()}));
        suggestIndex.setKeywordPinyin(new Completion(new String[]{PinYinUtils.toHanyuPinyin(albumInfoIndex.getAlbumTitle())}));
        suggestIndex.setKeywordSequence(new Completion(new String[]{PinYinUtils.getFirstLetter(albumInfoIndex.getAlbumTitle())}));
        suggestRepository.save(suggestIndex);
        // 专辑主播名称自动补全
        if (!StringUtils.isEmpty(albumInfoIndex.getAnnouncerName())) {
            SuggestIndex announcerSuggestIndex = new SuggestIndex();
            announcerSuggestIndex.setId(UUID.randomUUID().toString().replaceAll("-", ""));
            announcerSuggestIndex.setTitle(albumInfoIndex.getAnnouncerName());
            announcerSuggestIndex.setKeyword(new Completion(new String[]{albumInfoIndex.getAnnouncerName()}));
            announcerSuggestIndex.setKeywordPinyin(new Completion(new String[]{PinYinUtils.toHanyuPinyin(albumInfoIndex.getAnnouncerName())}));
            announcerSuggestIndex.setKeywordSequence(new Completion(new String[]{PinYinUtils.getFirstLetter(albumInfoIndex.getAnnouncerName())}));
            suggestRepository.save(announcerSuggestIndex);
        }
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

    /**
     * 专辑搜索
     * @param albumIndexQuery 专辑信息
     * @return AlbumSearchResponseVo
     */
    @Override
    public AlbumSearchResponseVo search(AlbumIndexQuery albumIndexQuery) throws IOException {
        //1.构建dsl语句
        SearchRequest request = buildQueryDsl(albumIndexQuery);
        //2.执行搜索语句
        SearchResponse<AlbumInfoIndex> response = elasticsearchClient.search(request, AlbumInfoIndex.class);
        //3.解析返回结果信息
        AlbumSearchResponseVo responseVo = parseSearchResult(response);
        responseVo.setPageSize(albumIndexQuery.getPageSize());
        responseVo.setPageNo(albumIndexQuery.getPageNo());
        // 获取总页数
        long totalPages = (responseVo.getTotal() + albumIndexQuery.getPageSize() - 1) / albumIndexQuery.getPageSize();
        responseVo.setTotalPages(totalPages);
        return responseVo;
    }

    /**
     * 关键字补全
     * @param keyword 关键字
     */
    @Override
    public Set<String> autoCompleteSuggest(String keyword) throws IOException {
        //1.构建查询suggestinfo索引的suggester
        Suggester suggester = new Suggester.Builder()
                //还可以考虑是否加入fuzzy
                .suggesters("suggestionKeyword", s -> s
                        .prefix(keyword)
                        .completion(c -> c
                                .field("keyword")
                                .size(10)
                                .skipDuplicates(true)))
                .suggesters("suggestionKeywordSequence", s -> s
                        .prefix(keyword)
                        .completion(c -> c
                                .field("keywordSequence")
                                .size(10)
                                .skipDuplicates(true)))
                .suggesters("suggestionKeywordPinyin", s -> s
                        .prefix(keyword)
                        .completion(c -> c
                                .field("keywordPinyin")
                                .size(10)
                                .skipDuplicates(true))).build();
        System.out.println(suggester.toString());
        SearchResponse<SuggestIndex> response = elasticsearchClient.search(s -> s
                .index("suggestinfo")
                .suggest(suggester), SuggestIndex.class);
        //2.解析索引里面的信息-不可重复
        Set<String> suggestTitleList = analysisResponse(response);
        //3.以title关键字查询同上面叠加
        if (suggestTitleList.size() < 10) {
            SearchResponse<SuggestIndex> responseSuggest = elasticsearchClient.search(s -> s
                    .index("suggestinfo")
                    .size(10)
                    .query(q -> q.match(m -> m.field("title").query(keyword))), SuggestIndex.class);
            List<Hit<SuggestIndex>> suggestHits = responseSuggest.hits().hits();
            for (Hit<SuggestIndex> suggestHit : suggestHits) {
                suggestTitleList.add(suggestHit.source().getTitle());
                int total = suggestTitleList.size();
                //最多自动补全10个信息
                if (total >= 10) break;
            }
        }
        return suggestTitleList;
    }

    @Autowired
    private ThreadPoolExecutor myPoolExecutor;
    /**
     * 获取专辑详情信息
     *
     * @param albumId 专辑id
     * @return Map<String, Object>
     */
    @Override
  /*  public Map<String, Object> getAlbumDetail(Long albumId) {
        Map<String, Object> result = new HashMap<>();
        CompletableFuture<Void> albumStatFuture = CompletableFuture.runAsync(() -> {
            RetVal<AlbumStatVo> albumStatVoRetVal = albumFeignClient.getAlbumStatInfo(albumId);//未写
            AlbumStatVo albumStatVo = albumStatVoRetVal.getData();
            result.put("albumStatVo", albumStatVo);
        }, myPoolExecutor);

        CompletableFuture<AlbumInfo> albumFuture = CompletableFuture.supplyAsync(() -> {
            AlbumInfo albumInfo = albumFeignClient.getAlbumInfoById(albumId).getData();//已写
            result.put("albumInfo", albumInfo);
            return albumInfo;
        }, myPoolExecutor);

        CompletableFuture<Void> categoryViewFuture = albumFuture.thenAcceptAsync(albumInfo -> {
            BaseCategoryView baseCategoryView = categoryFeignClient.getCategoryView(albumInfo.getCategory3Id());//已写
            result.put("baseCategoryView", baseCategoryView);
        }, myPoolExecutor);

        CompletableFuture<Void> announcerFuture = albumFuture.thenAcceptAsync(albumInfo -> {
            UserInfoVo userInfoVo = userFeignClient.getUserById(albumInfo.getUserId()).getData();//已写
            result.put("announcer", userInfoVo);
        }, myPoolExecutor);

        CompletableFuture.allOf(albumFuture,
                albumStatFuture,
                categoryViewFuture,
                announcerFuture).join();
        return result;
    }*/
    public Map<String, Object> getAlbumDetail(Long albumId) {
        Map<String, Object> retMap = new HashMap<>();
        // 1. 专辑基本信息
        AlbumInfo albumInfo = albumFeignClient.getAlbumInfoById(albumId).getData();
        retMap.put("albumInfo", albumInfo);
        // 2. 专辑统计信息
        AlbumStatVo albumStatVo = albumFeignClient.getAlbumStatInfo(albumId).getData();
        retMap.put("albumStatVo", albumStatVo);
        // 3. 专辑分类信息
        BaseCategoryView categoryView = categoryFeignClient.getCategoryView(albumInfo.getCategory3Id());
        retMap.put("baseCategoryView", categoryView);
        // 4. 用户基本信息
        UserInfoVo userInfoVo = userFeignClient.getUserById(albumInfo.getUserId()).getData();
        retMap.put("announcer", userInfoVo);
        return retMap;
    }

    /**
     * 更新排行榜列表
     */
    @Autowired
    private RedisTemplate redisTemplate;
    @Override
    public void updateRanking() throws IOException {
        //排行榜，按分类维度统计
        List<BaseCategory1> category1List = categoryFeignClient.getCategory1().getData();
        if (!CollectionUtils.isEmpty(category1List)) {
            for (BaseCategory1 baseCategory1 : category1List) {
                //统计维度：热度:hotScore、播放量:playStatNum、订阅量:subscribeStatNum、购买量:buyStatNum、评论数:albumCommentStatNum
                String[] rankingTypeList = new String[]{"hotScore", "playStatNum", "subscribeStatNum", "buyStatNum", "commentStatNum"};
                for (String rankingType : rankingTypeList) {
                    SearchResponse<AlbumInfoIndex> response = elasticsearchClient.search(s -> s
                            .index("albuminfo")
                            .query(q->q.bool(b->b.filter(f->f.term(
                                    t->t.field("category1Id").value(baseCategory1.getId())))))
                            .size(10)
                            .sort(t -> t.field(f -> f.field(rankingType).order(SortOrder.Desc))), AlbumInfoIndex.class);

                    //解析查询列表
                    List<AlbumInfoIndex> albumList = new ArrayList<>();
                    response.hits().hits().stream().forEach(hit -> {
                        AlbumInfoIndex album = hit.source();
                        albumList.add(album);
                    });
                    //将排行榜数据记录redis
                    redisTemplate.boundHashOps(RedisConstant.RANKING_KEY_PREFIX + baseCategory1.getId()).put(rankingType, albumList);
                }
            }
        }
    }

    private Set<String> analysisResponse(SearchResponse response) {
        Set<String> suggestList = new HashSet<>();
        Map<String, List<Suggestion<SuggestIndex>>> suggestMap = response.suggest();
        suggestMap.entrySet().stream().forEach(suggestEntry -> {
            List<Suggestion<SuggestIndex>> suggestValueList = suggestEntry.getValue();
            suggestValueList.stream().forEach(suggestValue -> {
                List<String> collect = suggestValue.completion().options().stream().map(it -> {
                    return it.source().getTitle();
                }).collect(Collectors.toList());
                suggestList.addAll(collect);
            });
        });
        return suggestList;
    }

    private SearchRequest buildQueryDsl(AlbumIndexQuery albumIndexQuery) {
        //2.构建bool查询
        BoolQuery.Builder boolQuery = new BoolQuery.Builder();
        //3.构建should关键字查询
        String keyword = albumIndexQuery.getKeyword();
        if (!StringUtils.isEmpty(keyword)) {
            boolQuery.should(s -> s.match(m -> m.field("albumTitle").query(keyword)));
            boolQuery.should(s -> s.match(m -> m.field("albumIntro").query(keyword)));
            boolQuery.should(s -> s.match(m -> m.field("announcerName").query(keyword)));
        }
        //4.根据一级分类id查询
        Long category1Id = albumIndexQuery.getCategory1Id();
        if (null != category1Id) {
            boolQuery.filter(f -> f.term(t -> t.field("category1Id").value(category1Id)));
        }
        //4.根据二级分类id查询
        Long category2Id = albumIndexQuery.getCategory2Id();
        if (null != category2Id) {
            boolQuery.filter(f -> f.term(t -> t.field("category2Id").value(category2Id)));
        }
        //4.根据三级分类id查询
        Long category3Id = albumIndexQuery.getCategory3Id();
        if (null != category3Id) {
            boolQuery.filter(f -> f.term(t -> t.field("category3Id").value(category3Id)));
        }
        //5.根据分类属性嵌套过滤
        List<String> propertyList = albumIndexQuery.getAttributeList();
        if (!CollectionUtils.isEmpty(propertyList)) {
            for (String property : propertyList) {
                //property长的类似于这种格式-->15:32
                String[] propertySplit = StringUtils.split(property, ":");
                if (propertySplit != null && propertySplit.length == 2) {
                    Query nestedQuery = NestedQuery.of(n -> n
                            .path("attributeValueIndexList")
                            .query(q -> q.bool(b -> b
                                    .must(m -> m.term(t -> t.field("attributeValueIndexList.attributeId").value(propertySplit[0])))
                                    .must(m -> m.term(t -> t.field("attributeValueIndexList.valueId").value(propertySplit[1])))
                            )))._toQuery();
                    boolQuery.filter(nestedQuery);
                }
            }
        }
        //1.构建最外层的query
        Query query = boolQuery.build()._toQuery();
        //6.构建分页与高亮信息
        Integer pageNo = albumIndexQuery.getPageNo();
        Integer pageSize = albumIndexQuery.getPageSize();
        SearchRequest.Builder requestBuilder = new SearchRequest.Builder()
                .index("albuminfo")
                .from((pageNo - 1) * pageSize)
                .size(pageSize)
                .query(query)
                .highlight(param -> param.fields("albumTitle", filed -> filed
                        .preTags("<font color='red'>")
                        .postTags("</font>")))
                .source(param -> param.filter(filed -> filed.excludes("attributeValueIndexList", "hotScore")));
        //7.构建排序信息 order=1:asc
        String order = albumIndexQuery.getOrder();
        String orderField = "hotScore";
        String sortType = "desc";
        //如果传递了排序参数
        if (StringUtils.isEmpty(order)) {
            String[] orderSplit = StringUtils.split(order, ":");
            if (orderSplit != null && orderSplit.length == 2) {
                switch (orderSplit[0]) {
                    case "1":
                        orderField = "hotScore";
                        break;
                    case "2":
                        orderField = "playStatNum";
                        break;
                    case "3":
                        orderField = "createTime";
                        break;
                }
                sortType = orderSplit[1];
            }
        }
        if (StringUtils.isEmpty(keyword)) {
            String sortTypeParam = sortType;
            String sortField = orderField;
            requestBuilder.sort(s -> s.field(f -> f.field(sortField)
                    .order("asc".equals(sortTypeParam) ? SortOrder.Asc : SortOrder.Desc)));
        }
        SearchRequest request = requestBuilder.build();
        System.out.println("拼接的DSL语句:" + request.toString());
        return request;
    }

    private AlbumSearchResponseVo parseSearchResult(SearchResponse<AlbumInfoIndex> response) {
        AlbumSearchResponseVo searchResponse = new AlbumSearchResponseVo();
        //1.获取总记录数
        searchResponse.setTotal(response.hits().total().value());
        //2.解析查询列表
        List<Hit<AlbumInfoIndex>> searchAlbumInfoHits = response.hits().hits();
        List<AlbumInfoIndexVo> albumInfoIndexVoList = new ArrayList<>();
        for (Hit<AlbumInfoIndex> searchAlbumInfoHit : searchAlbumInfoHits) {
            AlbumInfoIndexVo albumInfoIndexVo = new AlbumInfoIndexVo();
            BeanUtils.copyProperties(searchAlbumInfoHit.source(), albumInfoIndexVo);
            //处理高亮
            if (null != searchAlbumInfoHit.highlight().get("albumTitle")) {
                albumInfoIndexVo.setAlbumTitle(searchAlbumInfoHit.highlight().get("albumTitle").get(0));
            }
            albumInfoIndexVoList.add(albumInfoIndexVo);
        }
        searchResponse.setList(albumInfoIndexVoList);
        return searchResponse;
    }
}
