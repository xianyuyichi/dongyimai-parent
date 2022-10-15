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
            //创建一个查询构建者对象
            NativeSearchQueryBuilder builder = new NativeSearchQueryBuilder();

            String keywords = searchMap.get("keywords");
            if (StringUtils.hasText(keywords)) {
                //根据关键字到es中去匹配title,brand,category字段
                builder.withQuery(QueryBuilders.multiMatchQuery(keywords,"title","brand","category"));
            }

            //根据分类分组查询 (terms:分组的名称;  field:根据那个一字段分组
            builder.addAggregation(AggregationBuilders.terms("categoryGroup").field("category"));

            //根据品牌分组查询
            builder.addAggregation(AggregationBuilders.terms("brandGroup").field("brand"));

            //根据规格分组查询
            builder.addAggregation(AggregationBuilders.terms("specGroup").field("spec.keyword"));

            //构建一个混合查询的条件(分类名称,品牌,规格)
            BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
            //获取前端传递选择的分类值
            String category = searchMap.get("category");
            if(StringUtils.hasText(category)) {
                boolQuery.filter(QueryBuilders.termQuery("category", category));
            }
            String brand = searchMap.get("brand");
            if(StringUtils.hasText(brand)){
                boolQuery.filter(QueryBuilders.termQuery("brand",brand));
            }

            //判断前端传递根据规格进行查询条件
            for (String key : searchMap.keySet()) {
                if(StringUtils.hasText(key) && key.startsWith("spec_")){ //spec_网络
                    String substring = key.substring(key.lastIndexOf("_") + 1);
                    //termQuery: key: (map名称.key值.keyword===>value)参数1: es获取map中的value,  参数2:前端的搜索条件
                    boolQuery.filter(QueryBuilders.termQuery("specMap."+substring+".keyword", searchMap.get(key)));
                }
            }

            //根据价格区间查询
            String priceStr = searchMap.get("price");
            if(StringUtils.hasText(priceStr)){
                String[] split = priceStr.split("-");
                if(split[1].equals("*")){
                    //3000元以上
                    boolQuery.filter(QueryBuilders.rangeQuery("price").gte(split[0]));
                }else { // 500-1000
                    boolQuery.filter(QueryBuilders.rangeQuery("price")
                            .from(split[0],true).to(split[1],true));
                }
            }

            builder.withFilter(boolQuery);

            //实现分页
            int pageNum = 1;
            int pageSize = 3;
            String pageNum_ = searchMap.get("pageNum");
            if(StringUtils.hasText(pageNum_)){
                pageNum = Integer.parseInt(pageNum_);
            }
            builder.withPageable(PageRequest.of(pageNum,pageSize));

            //根据价格排序查询
            String sortRule = searchMap.get("sortRule");
            String sortField = searchMap.get("sortField");
            if(StringUtils.hasText(sortField) && StringUtils.hasText(sortRule)){
                builder.withSort(SortBuilders.fieldSort(sortField).order("DESC".equals(sortRule)? SortOrder.DESC:SortOrder.ASC));
            }

            //高亮显示设置
            //设置将哪一个字段设置为高亮字段
            builder.withHighlightFields(new HighlightBuilder.Field("title"));
            builder.withHighlightBuilder(new HighlightBuilder().preTags("<span style='color:red'>").postTags("</span>"));

            //创建查询对象
            NativeSearchQuery query = builder.build();
            //查询到的结果
            SearchHits<SkuInfo> searchHits = template.search(query, SkuInfo.class);
            //封装分页对象
            SearchPage<SkuInfo> searchPage = SearchHitSupport.searchPageFor(searchHits, query.getPageable());
            //从分页对象中获取总页数
            int totalPages = searchPage.getTotalPages();
            //获取总条数
            long totalHits = searchHits.getTotalHits();


            //获取查询到的数据
            List<SearchHit<SkuInfo>> hits = searchHits.getSearchHits();
            List<SkuInfo> list =new ArrayList<>();
            //处理高亮显示的结果
            for (SearchHit<SkuInfo> hit : hits) {
                //获取es中设置为高亮的字段
                Map<String, List<String>> highlightFields = hit.getHighlightFields();
                //获取高亮字段对应的值
                List<String> title = highlightFields.get("title");
                //重新设置查询的SkuInfo中的title为高亮的值
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
            //获取分组结果(分类的分组结果)
            Terms terms = searchHits.getAggregations().get("categoryGroup");
            List<String> categoryList = new ArrayList<>();
            for (Terms.Bucket bucket : terms.getBuckets()) {
                String s = bucket.getKeyAsString();
                categoryList.add(s);
            }

            //获取分组结果(品牌的分组结果)
            Terms terms2 = searchHits.getAggregations().get("brandGroup");
            List<String> brandList = new ArrayList<>();
            for (Terms.Bucket bucket : terms2.getBuckets()) {
                String s = bucket.getKeyAsString();
                brandList.add(s);
            }

            //获取分组结果(规格的分组结果)
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
