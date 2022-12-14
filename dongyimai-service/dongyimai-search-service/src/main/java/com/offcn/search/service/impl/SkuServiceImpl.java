package com.offcn.search.service.impl;

import com.alibaba.fastjson.JSON;
import com.offcn.search.dao.SkuEsMapper;
import com.offcn.search.pojo.SkuInfo;
import com.offcn.search.service.SkuService;
import com.offcn.sellergoods.entity.Result;
import com.offcn.sellergoods.feign.ItemFeign;
import com.offcn.sellergoods.pojo.Item;
import javafx.scene.chart.BubbleChart;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.*;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class SkuServiceImpl implements SkuService {

    @Autowired
    private ItemFeign itemFeign;
    @Autowired
    private SkuEsMapper skuEsMapper;
    @Autowired
    private ElasticsearchRestTemplate template;


    @Override
    public void importSku() {
        Result<List<Item>> result = itemFeign.findByStatus("1");
        List<Item> data = result.getData();
        String json = JSON.toJSONString(data);
        List<SkuInfo> skuInfos = JSON.parseArray(json, SkuInfo.class);
        skuInfos.forEach(skuInfo -> {
            String jsonStr = skuInfo.getSpec();
            Map<String, Object> map = JSON.parseObject(jsonStr, Map.class);
            skuInfo.setSpecMap(map);
        });
        skuEsMapper.saveAll(skuInfos);
    }

    @Override
    public Map search(Map<String, String> searchMap) {
        Map<String,Object> map = new HashMap<>();
        if (searchMap != null) {
            //?????????????????????????????????
            NativeSearchQueryBuilder builder = new NativeSearchQueryBuilder();

            String keywords = searchMap.get("keywords");
            if (StringUtils.hasText(keywords)) {
                //??????????????????es????????????title,brand,category??????
                builder.withQuery(QueryBuilders.multiMatchQuery(keywords,"title","brand","category"));
            }

            //???????????????????????? (terms:???????????????;  field:???????????????????????????
            builder.addAggregation(AggregationBuilders.terms("categoryGroup").field("category"));

            //????????????????????????
            builder.addAggregation(AggregationBuilders.terms("brandGroup").field("brand"));

            //????????????????????????
            builder.addAggregation(AggregationBuilders.terms("specGroup").field("spec.keyword"));

            //?????????????????????????????????(????????????,??????,??????)
            BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
            //????????????????????????????????????
            String category = searchMap.get("category");
            if(StringUtils.hasText(category)) {
                boolQuery.filter(QueryBuilders.termQuery("category", category));
            }
            String brand = searchMap.get("brand");
            if(StringUtils.hasText(brand)){
                boolQuery.filter(QueryBuilders.termQuery("brand",brand));
            }

            //????????????????????????????????????????????????
            for (String key : searchMap.keySet()) {
                if(StringUtils.hasText(key) && key.startsWith("spec_")){ //spec_??????
                    String substring = key.substring(key.lastIndexOf("_") + 1);
                    //termQuery: key: (map??????.key???.keyword===>value)??????1: es??????map??????value,  ??????2:?????????????????????
                    boolQuery.filter(QueryBuilders.termQuery("specMap."+substring+".keyword", searchMap.get(key)));
                }
            }

            //????????????????????????
            String priceStr = searchMap.get("price");
            if(StringUtils.hasText(priceStr)){
                String[] split = priceStr.split("-");
                if(split[1].equals("*")){
                    //3000?????????
                    boolQuery.filter(QueryBuilders.rangeQuery("price").gte(split[0]));
                }else { // 500-1000
                    boolQuery.filter(QueryBuilders.rangeQuery("price")
                            .from(split[0],true).to(split[1],true));
                }
            }

            builder.withFilter(boolQuery);

            //????????????
            int pageNum = 1;
            int pageSize = 3;
            String pageNum_ = searchMap.get("pageNum");
            if(StringUtils.hasText(pageNum_)){
                pageNum = Integer.parseInt(pageNum_);
            }
            builder.withPageable(PageRequest.of(pageNum,pageSize));

            //????????????????????????
            String sortRule = searchMap.get("sortRule");
            String sortField = searchMap.get("sortField");
            if(StringUtils.hasText(sortField) && StringUtils.hasText(sortRule)){
                builder.withSort(SortBuilders.fieldSort(sortField).order("DESC".equals(sortRule)? SortOrder.DESC:SortOrder.ASC));
            }

            //??????????????????
            //?????????????????????????????????????????????
            builder.withHighlightFields(new HighlightBuilder.Field("title"));
            builder.withHighlightBuilder(new HighlightBuilder().preTags("<span style='color:red'>").postTags("</span>"));

            //??????????????????
            NativeSearchQuery query = builder.build();
            //??????????????????
            SearchHits<SkuInfo> searchHits = template.search(query, SkuInfo.class);
            //??????????????????
            SearchPage<SkuInfo> searchPage = SearchHitSupport.searchPageFor(searchHits, query.getPageable());
            //?????????????????????????????????
            int totalPages = searchPage.getTotalPages();
            //???????????????
            long totalHits = searchHits.getTotalHits();


            //????????????????????????
            List<SearchHit<SkuInfo>> hits = searchHits.getSearchHits();
            List<SkuInfo> list =new ArrayList<>();
            //???????????????????????????
            for (SearchHit<SkuInfo> hit : hits) {
                //??????es???????????????????????????
                Map<String, List<String>> highlightFields = hit.getHighlightFields();
                //??????????????????????????????
                List<String> title = highlightFields.get("title");
                //?????????????????????SkuInfo??????title???????????????
                if(CollectionUtils.isEmpty(title)){
                    hit.getContent().setTitle(hit.getContent().getTitle());
                }else{
                    hit.getContent().setTitle(title.get(0)); //<span style='color'>skuInfo.title </span>
                }
                SkuInfo skuInfo = hit.getContent();
                list.add(skuInfo);
            }
/*

            for (SearchHit<SkuInfo> hit : hits) {
                SkuInfo skuInfo = hit.getContent();
                list.add(skuInfo);
            }*/



           /* List<SkuInfo> list = searchHits.getSearchHits().stream()
                    .map(hit -> hit.getContent()).collect(Collectors.toList());*/
            //??????????????????(?????????????????????)
            Terms terms = searchHits.getAggregations().get("categoryGroup");
            List<String> categoryList = new ArrayList<>();
            for (Terms.Bucket bucket : terms.getBuckets()) {
                String s = bucket.getKeyAsString();
                categoryList.add(s);
            }

            //??????????????????(?????????????????????)
            Terms terms2 = searchHits.getAggregations().get("brandGroup");
            List<String> brandList = new ArrayList<>();
            for (Terms.Bucket bucket : terms2.getBuckets()) {
                String s = bucket.getKeyAsString();
                brandList.add(s);
            }

            //??????????????????(?????????????????????)
            Map<String, Set<String>> specMap = new HashMap<>();
            Terms terms3 = searchHits.getAggregations().get("specGroup");
            Set<String> specJSONList = new HashSet<>();
            for (Terms.Bucket bucket : terms3.getBuckets()) {
                String s = bucket.getKeyAsString();
                specJSONList.add(s);
            }
            for (String s : specJSONList) {
                Map<String,String> jsonMap =JSON.parseObject(s,Map.class);
                for (Map.Entry<String, String> entry : jsonMap.entrySet()) {
                    String key = entry.getKey();
                    String value = entry.getValue();
                    Set<String> values = specMap.get(key);
                    if(values == null){
                        values = new HashSet<>();
                    }
                    values.add(value);
                    specMap.put(key,values);
                }
            }

            map.put("pages",totalPages);
            map.put("total",totalHits);
            map.put("pageNum", pageNum);
            map.put("pageSize", pageSize);
            map.put("list",list);
            map.put("categoryList",categoryList);
            map.put("brandList",brandList);
            map.put("specMap",specMap);
        }
        System.out.println(map);
        return map;
    }
}
