package com.offcn.search.dao;

import com.offcn.search.pojo.SkuInfo;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

@Repository
public interface SkuEsMapper extends ElasticsearchRepository<SkuInfo,Long> {

}
