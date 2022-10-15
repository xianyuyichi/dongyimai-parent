package com.offcn.order.controller;

import com.offcn.order.group.Cart;
import com.offcn.order.service.CartService;
import com.offcn.sellergoods.entity.Result;
import com.offcn.sellergoods.entity.StatusCode;
import com.offcn.sellergoods.utils.TokenDecode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/cart")
public class CartController {

    @Autowired
    private CartService cartService;
    @Autowired
    private TokenDecode tokenDecode;


    @RequestMapping("/addGoodsToCartList")
    public Result addGoodsToCartList(Long itemId, Integer num){
        String username = tokenDecode.getUserInfo().get("username");
        try {
            //查询当前用户购物车列表
            List<Cart> cartList = cartService.findCartListFromRedis(username);
            //添加商品到购物车中
            cartList = cartService.addGoodsToCartList(cartList, itemId, num);
            //保存购物车到redis
            cartService.saveCartListToRedis(username,cartList);
            return new Result(true, StatusCode.OK, "添加成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, StatusCode.ERROR, "添加失败");
        }
    }

    /**
     * 购物车列表
     *
     * @return
     */
    @RequestMapping("/findCartList")
    public List<Cart> findCartList() {
        String username = tokenDecode.getUserInfo().get("username");
        return cartService.findCartListFromRedis(username);//从redis中提取
    }
}
