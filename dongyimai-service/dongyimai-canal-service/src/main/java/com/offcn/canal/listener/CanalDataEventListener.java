package com.offcn.canal.listener;

import com.alibaba.fastjson.JSON;
import com.alibaba.otter.canal.protocol.CanalEntry;
import com.offcn.content.feign.ContentFeign;
import com.offcn.content.pojo.Content;
import com.offcn.item.feign.PageFeign;
import com.offcn.sellergoods.entity.Result;
import com.xpand.starter.canal.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.List;

@CanalEventListener
public class CanalDataEventListener {

    @Autowired
    private ContentFeign contentFeign;
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private PageFeign pageFeign;

  /*  *//***
     * 增加数据监听
     * @param eventType
     * @param rowData
     *//*
    @InsertListenPoint
    public void onEventInsert(CanalEntry.EventType eventType, CanalEntry.RowData rowData) {
        System.out.println(eventType.name()+","+rowData.toString());
    }

    *//***
     * 修改数据监听
     * @param rowData
     *//*
    @UpdateListenPoint
    public void onEventUpdate(CanalEntry.RowData rowData) {
        System.out.println("UpdateListenPoint");
        rowData.getAfterColumnsList().forEach((c) -> System.out.println("By--Annotation: " + c.getName() + " ::   " + c.getValue()));
    }

    *//***
     * 删除数据监听
     * @param eventType
     *//*
    @DeleteListenPoint
    public void onEventDelete(CanalEntry.EventType eventType) {
        System.out.println("DeleteListenPoint");
    }*/

    /***
     * 自定义数据修改监听
     * @param eventType
     * @param rowData
     */
    @ListenPoint(destination = "example", schema = "dongyimaidb", table = {"tb_content_category", "tb_content"},eventType = {
            CanalEntry.EventType.UPDATE,
            CanalEntry.EventType.DELETE,
            CanalEntry.EventType.INSERT})
    public void onEventCustomUpdate(CanalEntry.EventType eventType, CanalEntry.RowData rowData) {
        List<CanalEntry.Column> columnsList = rowData.getAfterColumnsList();
        for (CanalEntry.Column column : columnsList) {
            if("category_id".equals(column.getName())){
                //广告分类的id
                String value = column.getValue();
                Result<List<Content>> result = contentFeign.findByCategory(Long.valueOf(value));
                //获取广告信息
                List<Content> list = result.getData();
                redisTemplate.opsForValue().set("content_"+value, JSON.toJSONString(list));
                break;
            }
        }
    }

    @ListenPoint(destination = "example",
            schema = "dongyimaidb",
            table = {"tb_goods"},
            eventType = {CanalEntry.EventType.UPDATE, CanalEntry.EventType.INSERT, CanalEntry.EventType.DELETE})
    public void onEventCustomSpu(CanalEntry.EventType eventType, CanalEntry.RowData rowData) {

        //判断操作类型
        if (eventType == CanalEntry.EventType.DELETE) {
            String goodsId = "";
            List<CanalEntry.Column> beforeColumnsList = rowData.getBeforeColumnsList();
            for (CanalEntry.Column column : beforeColumnsList) {
                if (column.getName().equals("id")) {
                    goodsId = column.getValue();//goodsId
                    break;
                }
            }
            //todo 删除静态页

        }else{
            //新增 或者 更新
            List<CanalEntry.Column> afterColumnsList = rowData.getAfterColumnsList();
            String goodsId = "";
            for (CanalEntry.Column column : afterColumnsList) {
                if (column.getName().equals("id")) {
                    goodsId = column.getValue();
                    break;
                }
            }
            //更新 生成静态页
            pageFeign.createHtml(Long.valueOf(goodsId));
        }
    }
}
