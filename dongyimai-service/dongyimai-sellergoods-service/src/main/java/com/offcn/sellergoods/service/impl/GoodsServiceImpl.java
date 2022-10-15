package com.offcn.sellergoods.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.offcn.sellergoods.dao.BrandMapper;
import com.offcn.sellergoods.dao.GoodsMapper;
import com.offcn.sellergoods.dao.ItemCatMapper;
import com.offcn.sellergoods.dao.ItemMapper;
import com.offcn.sellergoods.entity.PageResult;
import com.offcn.sellergoods.group.GoodsEntity;
import com.offcn.sellergoods.pojo.*;
import com.offcn.sellergoods.service.GoodsDescService;
import com.offcn.sellergoods.service.GoodsService;
import com.offcn.sellergoods.service.ItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.*;

/****
 * @Author:ujiuye
 * @Description:Goods业务层接口实现类
 * @Date 2021/2/1 14:19
 *****/
@Service
@Transactional
public class GoodsServiceImpl extends ServiceImpl<GoodsMapper,Goods> implements GoodsService {

    @Autowired
    private ItemCatMapper itemCatMapper;
    @Autowired
    private BrandMapper brandMapper;
    @Autowired
    private ItemService itemService;
    @Autowired
    private GoodsDescService goodsDescService;
    /**
     * Goods条件+分页查询
     * @param goods 查询条件
     * @param page 页码
     * @param size 页大小
     * @return 分页结果
     */
    @Override
    public PageResult<Goods> findPage(Goods goods, int page, int size){
         Page<Goods> mypage = new Page<>(page, size);
        QueryWrapper<Goods> queryWrapper = this.createQueryWrapper(goods);
        IPage<Goods> iPage = this.page(mypage, queryWrapper);
        return new PageResult<Goods>(iPage.getTotal(),iPage.getRecords());
    }

    /**
     * Goods分页查询
     * @param page
     * @param size
     * @return
     */
    @Override
    public PageResult<Goods> findPage(int page, int size){
        Page<Goods> mypage = new Page<>(page, size);
        IPage<Goods> iPage = this.page(mypage, new QueryWrapper<Goods>());

        return new PageResult<Goods>(iPage.getTotal(),iPage.getRecords());
    }

    /**
     * Goods条件查询
     * @param goods
     * @return
     */
    @Override
    public List<Goods> findList(Goods goods){
        //构建查询条件
        QueryWrapper<Goods> queryWrapper = this.createQueryWrapper(goods);
        //根据构建的条件查询数据
        return this.list(queryWrapper);
    }


    /**
     * Goods构建查询对象
     * @param goods
     * @return
     */
    public QueryWrapper<Goods> createQueryWrapper(Goods goods){
        QueryWrapper<Goods> queryWrapper = new QueryWrapper<>();
        if(goods!=null){
            // 主键
            if(!StringUtils.isEmpty(goods.getId())){
                 queryWrapper.eq("id",goods.getId());
            }
            // 商家ID
            if(!StringUtils.isEmpty(goods.getSellerId())){
                 queryWrapper.eq("seller_id",goods.getSellerId());
            }
            // SPU名
            if(!StringUtils.isEmpty(goods.getGoodsName())){
                 queryWrapper.eq("goods_name",goods.getGoodsName());
            }
            // 默认SKU
            if(!StringUtils.isEmpty(goods.getDefaultItemId())){
                 queryWrapper.eq("default_item_id",goods.getDefaultItemId());
            }
            // 状态
            if(!StringUtils.isEmpty(goods.getAuditStatus())){
                 queryWrapper.eq("audit_status",goods.getAuditStatus());
            }
            // 是否上架
            if(!StringUtils.isEmpty(goods.getIsMarketable())){
                 queryWrapper.eq("is_marketable",goods.getIsMarketable());
            }
            // 品牌
            if(!StringUtils.isEmpty(goods.getBrandId())){
                 queryWrapper.eq("brand_id",goods.getBrandId());
            }
            // 副标题
            if(!StringUtils.isEmpty(goods.getCaption())){
                 queryWrapper.eq("caption",goods.getCaption());
            }
            // 一级类目
            if(!StringUtils.isEmpty(goods.getCategory1Id())){
                 queryWrapper.eq("category1_id",goods.getCategory1Id());
            }
            // 二级类目
            if(!StringUtils.isEmpty(goods.getCategory2Id())){
                 queryWrapper.eq("category2_id",goods.getCategory2Id());
            }
            // 三级类目
            if(!StringUtils.isEmpty(goods.getCategory3Id())){
                 queryWrapper.eq("category3_id",goods.getCategory3Id());
            }
            // 小图
            if(!StringUtils.isEmpty(goods.getSmallPic())){
                 queryWrapper.eq("small_pic",goods.getSmallPic());
            }
            // 商城价
            if(!StringUtils.isEmpty(goods.getPrice())){
                 queryWrapper.eq("price",goods.getPrice());
            }
            // 分类模板ID
            if(!StringUtils.isEmpty(goods.getTypeTemplateId())){
                 queryWrapper.eq("type_template_id",goods.getTypeTemplateId());
            }
            // 是否启用规格
            if(!StringUtils.isEmpty(goods.getIsEnableSpec())){
                 queryWrapper.eq("is_enable_spec",goods.getIsEnableSpec());
            }
            // 是否删除
            if(!StringUtils.isEmpty(goods.getIsDelete())){
                 queryWrapper.eq("is_delete",goods.getIsDelete());
            }
        }
        return queryWrapper;
    }

    /**
     * 删除
     * @param id
     */
    @Override
    public void delete(Long id){
        Goods goods = this.getById(id);
        if("1".equals(goods.getIsMarketable())){
            throw new RuntimeException("该商品需要先下架");
        }
        goods.setIsDelete("1");
        goods.setAuditStatus("0");
        this.updateById(goods);
    }

    /**
     * 修改Goods
     * @param goods
     */
    @Override
    public void update(GoodsEntity goods){
        //更新商品
        this.updateById(goods.getGoods());
        //更新商品扩展信息
        goodsDescService.updateById(goods.getGoodsDesc());
        //根据goods_id删除item表对应数据
        LambdaQueryWrapper<Item> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Item::getGoodsId,goods.getGoods().getId());
        itemService.remove(wrapper);
        //添加item数据
        this.saveItem(goods);
    }

    /**
     * 根据ID查询Goods
     * @param id
     * @return
     */
    @Override
    public GoodsEntity findById(Long id){
        GoodsEntity goodsEntity = new GoodsEntity();
        goodsEntity.setGoods(this.getById(id));
        goodsEntity.setGoodsDesc(goodsDescService.getById(id));
        LambdaQueryWrapper<Item> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Item::getGoodsId,id);
        List<Item> list = itemService.list(wrapper);
        goodsEntity.setItemList(list);
        return goodsEntity;
    }

    /**
     * 查询Goods全部数据
     * @return
     */
    @Override
    public List<Goods> findAll() {
        return this.list(new QueryWrapper<Goods>());
    }

    @Override
    public void add(GoodsEntity goodsEntity) {
        //从复合对象中获取商品对象(SPU)
        Goods goods = goodsEntity.getGoods();
        //设置为未审核的商品
        goods.setAuditStatus("0");
        this.save(goods);
        //从复合对象中获取商品扩展对象
        GoodsDesc goodsDesc = goodsEntity.getGoodsDesc();
        //设置扩展对象中的商品id
        goodsDesc.setGoodsId(goods.getId());
        goodsDescService.save(goodsDesc);
        this.saveItem(goodsEntity);
    }

    @Override
    public void audit(Long goodsId) {
        Goods goods = this.getById(goodsId);
        if(goods == null || "1".equals(goods.getIsDelete())){
            throw new RuntimeException("该商品不存在");
        }
        //已审核
        goods.setAuditStatus("1");
        this.updateById(goods);
    }

    @Override
    public void put(Long goodsId) {
        Goods goods = this.getById(goodsId);
        if(goods == null || "1".equals(goods.getIsDelete())){
            throw new RuntimeException("该商品不存在");
        }
        if(!("1".equals(goods.getAuditStatus()))){
            throw new RuntimeException("该商品未审核");
        }
        goods.setIsMarketable("1");
        this.updateById(goods);
    }

    @Override
    public void pull(Long goodsId) {
        Goods goods = this.getById(goodsId);
        if(goods == null || "1".equals(goods.getIsDelete())){
            throw new RuntimeException("该商品不存在");
        }
        goods.setIsMarketable("0");
        this.updateById(goods);
    }

    @Override
    public int putMany(Long[] ids) {
        LambdaQueryWrapper<Goods> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Goods::getIsDelete,"0").eq(Goods::getAuditStatus,"1").in(Goods::getId, Arrays.asList(ids));
        Goods goods = new Goods();
        goods.setIsMarketable("1");
        int i = this.baseMapper.update(goods, wrapper);
        /* for (Long id : ids) {
           *//* Goods goods = this.getById(id);
            if(goods == null || "1".equals(goods.getIsDelete())){
                throw new RuntimeException("该商品不存在");
            }
            if(!("1".equals(goods.getAuditStatus()))){
                throw new RuntimeException("该商品未审核");
            }
            goods.setIsMarketable("1");
            this.updateById(goods);*//*


        }*/
        return i;
    }

    private void saveItem(GoodsEntity goodsEntity){
        //获取SKU信息
        List<Item> itemList = goodsEntity.getItemList();
        Goods goods = goodsEntity.getGoods();
        //判断是否开启规格选项
        if("1".equals(goods.getIsEnableSpec())){
            if(!CollectionUtils.isEmpty(itemList)){
                for (Item item : itemList) {
                    String name = goods.getGoodsName();
                    String specJson = item.getSpec();
                    Map map = JSON.parseObject(specJson, Map.class);
                    //获取规格选项值
                    Collection values = map.values();
                    for (Object value : values) {
                        name += value + " "; //华为 16G 移动4G
                    }
                    item.setTitle(name);
                    this.setItemValue(goodsEntity,item);
                }
                itemService.saveBatch(itemList);
            }
        }else{
            Item item = new Item();
            item.setTitle(goods.getGoodsName());
            item.setPrice(goods.getPrice());
            item.setNum(9999);
            item.setStatus("1");
            item.setSpec("{}");
            this.setItemValue(goodsEntity,item);
            itemService.save(item);
        }
    }

    private void setItemValue(GoodsEntity goodsEntity,Item item){
        String itemImagesJson = goodsEntity.getGoodsDesc().getItemImages();
        if(StringUtils.hasText(itemImagesJson)) {
            List<Map> list = JSON.parseArray(itemImagesJson, Map.class);
            String url = (String) list.get(0).get("url");
            item.setImage(url);
        }
        item.setCategoryId(goodsEntity.getGoods().getCategory3Id());
        item.setCreateTime(new Date());
        item.setUpdateTime(new Date());
        item.setGoodsId(goodsEntity.getGoods().getId());
        //根据商品分类的id查询分类名称
        ItemCat itemCat = itemCatMapper.selectById(item.getCategoryId());
        item.setCategory(itemCat.getName());
        //根据品牌的id查询品牌的名称
        Brand brand = brandMapper.selectById(goodsEntity.getGoods().getBrandId());
        item.setBrand(brand.getName());
    }
}
