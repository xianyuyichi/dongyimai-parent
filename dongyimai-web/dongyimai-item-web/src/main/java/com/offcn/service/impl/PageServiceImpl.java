package com.offcn.service.impl;

import com.alibaba.fastjson.JSON;
import com.offcn.sellergoods.entity.Result;
import com.offcn.sellergoods.feign.GoodsFeign;
import com.offcn.sellergoods.feign.ItemCatFeign;
import com.offcn.sellergoods.feign.ItemFeign;
import com.offcn.sellergoods.group.GoodsEntity;
import com.offcn.sellergoods.pojo.Goods;
import com.offcn.sellergoods.pojo.GoodsDesc;
import com.offcn.sellergoods.pojo.Item;
import com.offcn.sellergoods.pojo.ItemCat;
import com.offcn.service.PageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PageServiceImpl implements PageService {
    @Autowired
    private ItemCatFeign itemCatFeign;
    @Autowired
    private GoodsFeign goodsFeign;
    @Autowired
    private TemplateEngine templateEngine;
    @Value("${pagepath}")
    private String pagepath;

    private Map<String,Object> getData(Long spuId){
        Map<String,Object> dataMap  = new HashMap<>();
        Result<GoodsEntity> result = goodsFeign.findById(spuId);
        GoodsEntity goodsEntity = result.getData();
        Goods goods = goodsEntity.getGoods();
        GoodsDesc goodsDesc = goodsEntity.getGoodsDesc();
        List<Item> itemList = goodsEntity.getItemList();
        String specificationItems = goodsDesc.getSpecificationItems();
        List<Map> list = JSON.parseArray(specificationItems, Map.class);
        String itemImagesJSON = goodsDesc.getItemImages();
        List<Map> itemImages = JSON.parseArray(itemImagesJSON, Map.class);
        dataMap.put("goods", goods);
        dataMap.put("goodsDesc", goodsDesc);
        dataMap.put("specificationList",list);
        dataMap.put("imageList",itemImages);
        dataMap.put("itemList",itemList);
        dataMap.put("category1",itemCatFeign.findById(goods.getCategory1Id()).getData());
        dataMap.put("category2",itemCatFeign.findById(goods.getCategory2Id()).getData());
        dataMap.put("category3",itemCatFeign.findById(goods.getCategory3Id()).getData());
        return dataMap;
    }


    @Override
    public void createPageHtml(Long spuId) {
        Map<String,Object> map = this.getData(spuId);
        Context context = new Context();
        context.setVariables(map);
        File dir = new File(pagepath);
        if(!dir.exists()){
            dir.mkdirs();
        }
        //html文件对象创建
        File file = new File(dir,spuId+".html");

        try {
            PrintWriter printWriter = new PrintWriter(file,"utf-8");
            templateEngine.process("item",context,printWriter);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }
}
