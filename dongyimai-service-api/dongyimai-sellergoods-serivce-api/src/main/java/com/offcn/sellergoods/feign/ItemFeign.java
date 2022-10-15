package com.offcn.sellergoods.feign;

import com.offcn.sellergoods.entity.Result;
import com.offcn.sellergoods.pojo.Item;
import com.offcn.sellergoods.pojo.ItemCat;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient("dym-sellergoods")
@RequestMapping("/item")
public interface ItemFeign {

    @GetMapping("/status/{status}")
    Result<List<Item>> findByStatus(@PathVariable String status);

    /***
     * 根据ID查询Item数据
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    Result<Item> findById(@PathVariable Long id);


    /***
     * 库存递减
     * @param username
     * @return
     */
    @PostMapping(value = "/decr/count")
    Result decrCount(@RequestParam(value = "username") String username);
}
