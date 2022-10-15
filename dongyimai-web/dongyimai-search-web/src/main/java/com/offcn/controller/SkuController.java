package com.offcn.controller;

import com.offcn.search.feign.SkuFeign;
import com.offcn.search.pojo.SkuInfo;
import com.offcn.sellergoods.entity.Page;
import org.elasticsearch.index.engine.Engine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@Controller
@RequestMapping("/search")
public class SkuController {

    @Autowired
    private SkuFeign skuFeign;

    /**
     * 搜索
     * @param searchMap
     * @return
     */
    @GetMapping(value = "/list")
    public String search(@RequestParam(required = false) Map searchMap, Model model){
        String url = this.setUrl(searchMap);
        Object pageNum = searchMap.get("pageNum");
        if(ObjectUtils.isEmpty(pageNum)){
            searchMap.put("pageNum","1");
        }
        //dongyimai-search-service微服务
        Map resultMap = skuFeign.search(searchMap);
        Page<SkuInfo> infoPage = new Page<SkuInfo>(
                Long.valueOf(resultMap.get("total").toString()),
                Integer.valueOf(resultMap.get("pageNum").toString()),
                Integer.valueOf(resultMap.get("pageSize").toString())
        );
        model.addAttribute("url",url);
        model.addAttribute("searchMap",searchMap);
        model.addAttribute("result",resultMap);
        model.addAttribute("page",infoPage);
        return "search";
    }

    private String setUrl(Map<String, String> searchMap) {
        String url = "/search/list";

        if(searchMap!=null && searchMap.size()>0){
            url+="?";
            for (Map.Entry<String, String> stringStringEntry : searchMap.entrySet()) {
                String key = stringStringEntry.getKey();// keywords / brand  / category
                String value = stringStringEntry.getValue();//华为  / 华为  / 笔记本

                if("sortRule".equals(key) || "sortField".equals(key)){
                    continue;
                }

                if(key.equals("pageNum")){
                    continue;
                }

                url+=key+"="+value+"&";
            }

            //去掉多余的&
            if(url.lastIndexOf("&")!=-1){
                url =  url.substring(0,url.lastIndexOf("&"));
            }

        }
        return url;
    }
}
