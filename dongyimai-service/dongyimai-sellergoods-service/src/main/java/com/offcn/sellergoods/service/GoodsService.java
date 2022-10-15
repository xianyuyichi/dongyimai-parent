package com.offcn.sellergoods.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.offcn.sellergoods.entity.PageResult;
import com.offcn.sellergoods.group.GoodsEntity;
import com.offcn.sellergoods.pojo.Goods;

import java.util.List;

/****
 * @Author:ujiuye
 * @Description:Goods业务层接口
 * @Date 2021/2/1 14:19
 *****/

public interface GoodsService extends IService<Goods> {

    /***
     * Goods多条件分页查询
     * @param goods
     * @param page
     * @param size
     * @return
     */
    PageResult<Goods> findPage(Goods goods, int page, int size);

    /***
     * Goods分页查询
     * @param page
     * @param size
     * @return
     */
    PageResult<Goods> findPage(int page, int size);

    /***
     * Goods多条件搜索方法
     * @param goods
     * @return
     */
    List<Goods> findList(Goods goods);

    /***
     * 删除Goods
     * @param id
     */
    void delete(Long id);

    /***
     * 修改Goods数据
     * @param goods
     */
    void update(GoodsEntity goods);

    /**
     * 根据ID查询Goods
     * @param id
     * @return
     */
     GoodsEntity findById(Long id);

    /***
     * 查询所有Goods
     * @return
     */
    List<Goods> findAll();

    /***
     * 新增Goods
     * @param goodsEntity
     */
    void add(GoodsEntity goodsEntity);

    /***
     * 商品审核
     * @param goodsId
     */
    void audit(Long goodsId);

    /***
     * 商品上架
     * @param goodsId
     */
    void put(Long goodsId);

    /***
     * 商品下架
     * @param goodsId
     */
    void pull(Long goodsId);

    /**
     * 批量上架
     * @param ids
     * @return
     */
    int putMany(Long[] ids);



}
