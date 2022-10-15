package com.offcn.user.controller;

import com.alibaba.fastjson.JSON;
import com.offcn.sellergoods.entity.PageResult;
import com.offcn.sellergoods.entity.Result;
import com.offcn.sellergoods.entity.StatusCode;
import com.offcn.sellergoods.utils.BCrypt;
import com.offcn.sellergoods.utils.JwtUtil;
import com.offcn.sellergoods.utils.PhoneFormatCheckUtils;
import com.offcn.sellergoods.utils.TokenDecode;
import com.offcn.user.pojo.Address;
import com.offcn.user.pojo.User;
import com.offcn.user.service.UserService;
import io.swagger.annotations.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.print.attribute.HashAttributeSet;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/****
 * @Author:ujiuye
 * @Description:
 * @Date 2021/2/1 14:19
 *****/
@Api(tags = "UserController")
@RestController
@RequestMapping("/user")
@CrossOrigin
public class UserController {

    @Autowired
    private UserService userService;
    @Autowired
    private TokenDecode tokenDecode;

    /***
     * User分页条件搜索实现
     * @param user
     * @param page
     * @param size
     * @return
     */
    @ApiOperation(value = "User条件分页查询",notes = "分页条件查询User方法详情",tags = {"UserController"})
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "path", name = "page", value = "当前页", required = true),
            @ApiImplicitParam(paramType = "path", name = "size", value = "每页显示条数", required = true)
    })
    @PostMapping(value = "/search/{page}/{size}" )
    public Result<PageResult<User>> findPage(@RequestBody(required = false) @ApiParam(name = "User对象",value = "传入JSON数据",required = false) User user, @PathVariable  int page, @PathVariable  int size){
        //调用UserService实现分页条件查询User
        PageResult<User> pageResult = userService.findPage(user, page, size);
        return new Result(true, StatusCode.OK,"查询成功",pageResult);
    }

    /***
     * User分页搜索实现
     * @param page:当前页
     * @param size:每页显示多少条
     * @return
     */
    @ApiOperation(value = "User分页查询",notes = "分页查询User方法详情",tags = {"UserController"})
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "path", name = "page", value = "当前页", required = true),
            @ApiImplicitParam(paramType = "path", name = "size", value = "每页显示条数", required = true)
    })
    @GetMapping(value = "/search/{page}/{size}" )
    public Result<PageResult<User>> findPage(@PathVariable  int page, @PathVariable  int size){
        //调用UserService实现分页查询User
        PageResult<User> pageResult = userService.findPage(page, size);
        return new Result<PageResult<User>>(true,StatusCode.OK,"查询成功",pageResult);
    }

    /***
     * 多条件搜索品牌数据
     * @param user
     * @return
     */
    @ApiOperation(value = "User条件查询",notes = "条件查询User方法详情",tags = {"UserController"})
    @PostMapping(value = "/search" )
    public Result<List<User>> findList(@RequestBody(required = false) @ApiParam(name = "User对象",value = "传入JSON数据",required = false) User user){
        //调用UserService实现条件查询User
        List<User> list = userService.findList(user);
        return new Result<List<User>>(true,StatusCode.OK,"查询成功",list);
    }

    /***
     * 根据ID删除品牌数据
     * @param id
     * @return
     */
    @ApiOperation(value = "User根据ID删除",notes = "根据ID删除User方法详情",tags = {"UserController"})
    @ApiImplicitParam(paramType = "path", name = "id", value = "主键ID", required = true, dataType = "Long")
    @DeleteMapping(value = "/{id}" )
    public Result delete(@PathVariable Long id){
        //调用UserService实现根据主键删除
        userService.delete(id);
        return new Result(true,StatusCode.OK,"删除成功");
    }

    /***
     * 修改User数据
     * @param user
     * @param id
     * @return
     */
    @ApiOperation(value = "User根据ID修改",notes = "根据ID修改User方法详情",tags = {"UserController"})
    @ApiImplicitParam(paramType = "path", name = "id", value = "主键ID", required = true, dataType = "Long")
    @PutMapping(value="/{id}")
    public Result update(@RequestBody @ApiParam(name = "User对象",value = "传入JSON数据",required = false) User user,@PathVariable Long id){
        //设置主键值
        user.setId(id);
        //调用UserService实现修改User
        userService.update(user);
        return new Result(true,StatusCode.OK,"修改成功");
    }

    /***
     * 新增User数据
     * @param user
     * @return
     */
    @ApiOperation(value = "User添加",notes = "添加User方法详情",tags = {"UserController"})
    @PostMapping("/add")
    public Result add(@RequestBody User user,String smscode){
        boolean checkSmsCode = userService.checkSmsCode(user.getPhone(), smscode);
        if(checkSmsCode==false){
            return new Result(false,StatusCode.ERROR ,"验证码输入错误！");
        }
        try {
            userService.add(user);
            return new Result(true,StatusCode.OK, "增加成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,StatusCode.ERROR, "增加失败");
        }
    }

    /***
     * 根据ID查询User数据
     * @param id
     * @return
     */
    @ApiOperation(value = "User根据ID查询",notes = "根据ID查询User方法详情",tags = {"UserController"})
    @ApiImplicitParam(paramType = "path", name = "id", value = "主键ID", required = true, dataType = "Long")
    @PreAuthorize("hasAnyAuthority('admin')")
    @GetMapping("/{id}")
    public Result<User> findById(@PathVariable Long id){
        //调用UserService实现根据主键查询User
        User user = userService.findById(id);
        return new Result<User>(true,StatusCode.OK,"查询成功",user);
    }

    /***
     * 查询User全部数据
     * @return
     */
    @ApiOperation(value = "查询所有User",notes = "查询所User有方法详情",tags = {"UserController"})
    @GetMapping
    public Result<List<User>> findAll(){
        //调用UserService实现查询所有User
        List<User> list = userService.findAll();
        return new Result<List<User>>(true, StatusCode.OK,"查询成功",list) ;
    }

    /**
     * 发送短信验证码
     * @param phone
     * @return
     */
    @GetMapping("/sendCode")
    public Result sendCode(String phone){
        //判断手机号格式
        if(!PhoneFormatCheckUtils.isPhoneLegal(phone)){
            return new Result(false, StatusCode.ERROR,"手机号格式不正确");
        }
        try {
            userService.createSmsCode(phone);//生成验证码
            return new Result(true,StatusCode.OK, "验证码发送成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,StatusCode.ERROR, "验证码发送失败");
        }
    }

    /**
     * 根据用户名查询用户
     */
    @GetMapping("/load/{username}")
    public Result<User> findByUsername(@PathVariable("username") String username){
        User user =  userService.findByUsername(username);
        return new Result<>(user);
    }


    /***
     *用户登录
     */
   /* @RequestMapping(value = "/login")
    public Result login(String username, String password, HttpServletResponse response){
        //查询用户信息
        User user =  userService.findByUsername(username);

        if(user!=null && BCrypt.checkpw(password,user.getPassword())){
            //添加用户信息
            Map<String,Object> info = new HashMap<>();
            info.put("username",user.getUsername());
            info.put("role","admin");
            String token = JwtUtil.createJWT(UUID.randomUUID().toString(), JSON.toJSONString(info), null);
            //创建cookie对象,将token封装到cookie中
            Cookie cookie = new Cookie("token",token);
            cookie.setPath("/");
            //将cookie返回到客户端
            response.addCookie(cookie);
            return new Result(true,StatusCode.OK,"登录成功！",token);
        }
        return  new Result(false,StatusCode.LOGINERROR,"账号或者密码错误！");
    }*/


    /***
     * 增加用户积分
     * @param points:要添加的积分
     */
    @GetMapping(value = "/points/add")
    public Result addPoints(Integer points){
        //获取用户名
        Map<String, String> userMap = tokenDecode.getUserInfo();
        String username = userMap.get("username");

        //添加积分
        userService.addUserPoints(username,points);
        return new Result(true,StatusCode.OK,"添加积分成功！");
    }

}
