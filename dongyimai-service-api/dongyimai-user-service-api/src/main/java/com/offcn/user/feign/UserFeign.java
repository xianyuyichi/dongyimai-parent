package com.offcn.user.feign;

import com.offcn.sellergoods.entity.Result;
import com.offcn.user.pojo.User;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient("dym-user")
@RequestMapping("/user")
public interface UserFeign {

    /***
     * 根据username查询用户信息
     * @param username
     * @return
     */
    @GetMapping("/load/{username}")
    Result<User> findByUsername(@PathVariable String username);

    /***
     * 添加用户积分
     * @param points
     * @return
     */
    @GetMapping(value = "/points/add")
    Result addPoints(@RequestParam(value = "points")Integer points);
}
