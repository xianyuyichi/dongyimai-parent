package com.offcn.order.service.impl;

import com.offcn.order.group.Cart;
import com.offcn.order.pojo.OrderItem;
import com.offcn.order.service.CartService;
import com.offcn.sellergoods.entity.Result;
import com.offcn.sellergoods.feign.ItemFeign;
import com.offcn.sellergoods.pojo.Item;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class CartServiceImpl implements CartService {

    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private ItemFeign itemFeign;

    private static final String KEY = "cartlist";

    @Override
    public List<Cart> addGoodsToCartList(List<Cart> cartList, Long itemId, Integer num) {
        Result<Item> result = itemFeign.findById(itemId);
        //获取商品(SKU)
        Item item = result.getData();
        if(item == null){
            throw new RuntimeException("该商品不存在");
        }
        if(!"1".equals(item.getStatus())){
            throw new RuntimeException("该商品属于非上架商品");
        }

        //从用户添加到购物车中的商品中获取这个商品对应的卖家的id
        String sellerId = item.getSellerId();
        Cart cart = this.searchCartBySellerId(cartList, sellerId);
        if(cart == null){
            //创建购物车对象
            cart = new Cart();
            //设置购物车对象中的卖家id
            cart.setSellerId(sellerId);
            //设置购物车对象中的卖家名称
            cart.setSellerName(item.getSeller());

            //创建一个购物车明细的集合对象
            List<OrderItem> orderItemList = new ArrayList<>();
            //创建一个购物车明细对象
            OrderItem orderItem = this.createOrderItem(item, num);

            //将明细对象添加到明细集合中
            orderItemList.add(orderItem);
            //设置购物车的明细集合
            cart.setOrderItemList(orderItemList);
            //将这个购物车对象添加到购物车列表中
            cartList.add(cart);
        }else{
            //购物车明细对象
            OrderItem orderItem = this.searchOrderItemInCart(cart, itemId);
            if(orderItem == null){
                //创建一个购物车明细对象
                orderItem = this.createOrderItem(item, num);
                //将明细对象添加到集合中
                cart.getOrderItemList().add(orderItem);
            }else{
                orderItem.setNum(orderItem.getNum()+num);
                orderItem.setTotalFee(new BigDecimal(orderItem.getPrice().doubleValue()*orderItem.getNum()));
            }
        }
        return cartList;
    }

    //为购物车明细对象填充属性值
    private OrderItem createOrderItem(Item item,Integer num){
        OrderItem orderItem = new OrderItem();
        //设置SKU的id
        orderItem.setItemId(item.getId());
        //设置SPU的id
        orderItem.setGoodsId(item.getGoodsId());
        //设置购物车的商品的数量
        orderItem.setNum(num);

        orderItem.setTitle(item.getTitle());
        orderItem.setPrice(item.getPrice());
        //购物车明细的总价
        orderItem.setTotalFee(new BigDecimal(orderItem.getPrice().doubleValue()*orderItem.getNum()));
        orderItem.setPicPath(item.getImage());
        orderItem.setSellerId(item.getSellerId());
        return orderItem;
    }


    //根据用户添加到购物车的商品所对应的卖家id,判断这个卖家是否在购物车中
    private Cart searchCartBySellerId(List<Cart> cartList,String sellerId){
        //从购物车集合中遍历所有的购物车对象
        for (Cart cart : cartList) {
            //从购物车对象中获取卖家的id
            String cartSellerId = cart.getSellerId();
            if(sellerId.equals(cartSellerId)){
                return cart;
            }
        }
        return null;
    }

    //根据商品的id,到购物车明细集合中判断之前是否有对应的这个商品
    private OrderItem searchOrderItemInCart(Cart cart,Long itemId){
        List<OrderItem> orderItemList = cart.getOrderItemList();
        if(!CollectionUtils.isEmpty(orderItemList)){
            for (OrderItem orderItem : orderItemList) {
                if(orderItem.getItemId().equals(itemId)){
                    return orderItem;
                }
            }
        }
        return null;
    }


    @Override
    public List<Cart> findCartListFromRedis(String username) {
        List<Cart> cartList = (List<Cart>) redisTemplate.boundHashOps(KEY).get(username);
        if(CollectionUtils.isEmpty(cartList)){
            cartList = new ArrayList<>();
        }
        return cartList;
    }

    @Override
    public void saveCartListToRedis(String username, List<Cart> cartList) {
        redisTemplate.boundHashOps(KEY).put(username,cartList);
    }
}
