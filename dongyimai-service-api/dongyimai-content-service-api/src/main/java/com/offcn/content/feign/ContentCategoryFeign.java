package com.offcn.content.feign;

import com.offcn.content.pojo.ContentCategory;
import com.offcn.sellergoods.entity.PageResult;
import com.offcn.sellergoods.entity.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/****
 * @Author:ujiuye
 * @Description:
 * @Date 2021/2/1 14:19
 *****/
@FeignClient(value="dym-content")
@RequestMapping("/contentCategory")
public interface ContentCategoryFeign {

    /***
     * ContentCategory分页条件搜索实现
     * @param contentCategory
     * @param page
     * @param size
     * @return
     */
    @PostMapping("/search/{page}/{size}" )
    Result<PageResult<ContentCategory>> findPage(@RequestBody(required = false) ContentCategory contentCategory, @PathVariable  int page, @PathVariable  int size);

    /***
     * ContentCategory分页搜索实现
     * @param page:当前页
     * @param size:每页显示多少条
     * @return
     */
    @GetMapping("/search/{page}/{size}" )
    Result<PageResult<ContentCategory>> findPage(@PathVariable  int page, @PathVariable  int size);

    /***
     * 多条件搜索品牌数据
     * @param contentCategory
     * @return
     */
    @PostMapping("/search" )
    Result<List<ContentCategory>> findList(@RequestBody(required = false) ContentCategory contentCategory);

    /***
     * 根据ID删除品牌数据
     * @param id
     * @return
     */
    @DeleteMapping("/{id}" )
    Result delete(@PathVariable Long id);

    /***
     * 修改ContentCategory数据
     * @param contentCategory
     * @param id
     * @return
     */
    @PutMapping("/{id}")
    Result update(@RequestBody ContentCategory contentCategory,@PathVariable Long id);

    /***
     * 新增ContentCategory数据
     * @param contentCategory
     * @return
     */
    @PostMapping
    Result add(@RequestBody ContentCategory contentCategory);

    /***
     * 根据ID查询ContentCategory数据
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    Result<ContentCategory> findById(@PathVariable Long id);

    /***
     * 查询ContentCategory全部数据
     * @return
     */
    @GetMapping
    Result<List<ContentCategory>> findAll();
}