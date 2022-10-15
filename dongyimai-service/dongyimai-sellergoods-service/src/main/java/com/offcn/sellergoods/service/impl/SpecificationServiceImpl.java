package com.offcn.sellergoods.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.offcn.sellergoods.dao.SpecificationMapper;
import com.offcn.sellergoods.dao.SpecificationOptionMapper;
import com.offcn.sellergoods.entity.PageResult;
import com.offcn.sellergoods.group.SpecEntity;
import com.offcn.sellergoods.pojo.Specification;
import com.offcn.sellergoods.pojo.SpecificationOption;
import com.offcn.sellergoods.service.SpecificationOptionService;
import com.offcn.sellergoods.service.SpecificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;

/****
 * @Author:ujiuye
 * @Description:Specification业务层接口实现类
 * @Date 2021/2/1 14:19
 *****/
@Service
public class SpecificationServiceImpl extends ServiceImpl<SpecificationMapper,Specification> implements SpecificationService {

    @Autowired
    private SpecificationOptionService specificationOptionService;
    @Autowired
    private SpecificationOptionMapper specificationOptionMapper;

    /**
     * Specification条件+分页查询
     * @param specification 查询条件
     * @param page 页码
     * @param size 页大小
     * @return 分页结果
     */
    @Override
    public PageResult<Specification> findPage(Specification specification, int page, int size){
         Page<Specification> mypage = new Page<>(page, size);
        QueryWrapper<Specification> queryWrapper = this.createQueryWrapper(specification);
        IPage<Specification> iPage = this.page(mypage, queryWrapper);
        return new PageResult<Specification>(iPage.getTotal(),iPage.getRecords());
    }

    /**
     * Specification分页查询
     * @param page
     * @param size
     * @return
     */
    @Override
    public PageResult<Specification> findPage(int page, int size){
        Page<Specification> mypage = new Page<>(page, size);
        IPage<Specification> iPage = this.page(mypage, new QueryWrapper<Specification>());

        return new PageResult<Specification>(iPage.getTotal(),iPage.getRecords());
    }

    /**
     * Specification条件查询
     * @param specification
     * @return
     */
    @Override
    public List<Specification> findList(Specification specification){
        //构建查询条件
        QueryWrapper<Specification> queryWrapper = this.createQueryWrapper(specification);
        //根据构建的条件查询数据
        return this.list(queryWrapper);
    }


    /**
     * Specification构建查询对象
     * @param specification
     * @return
     */
    public QueryWrapper<Specification> createQueryWrapper(Specification specification){
        QueryWrapper<Specification> queryWrapper = new QueryWrapper<>();
        if(specification!=null){
            // 主键
            if(!StringUtils.isEmpty(specification.getId())){
                 queryWrapper.eq("id",specification.getId());
            }
            // 名称
            if(!StringUtils.isEmpty(specification.getSpecName())){
                 queryWrapper.eq("spec_name",specification.getSpecName());
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
        this.removeById(id);
        LambdaQueryWrapper<SpecificationOption> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SpecificationOption::getSpecId,id);
        specificationOptionMapper.delete(wrapper);
    }

    /**
     * 修改Specification
     * @param specEntity
     */
    @Override
    public void update(SpecEntity specEntity){
        if(!ObjectUtils.isEmpty(specEntity)){
            //更新规格
            Specification specification = specEntity.getSpecification();
            this.updateById(specification);
            //更新规格选项
            LambdaQueryWrapper<SpecificationOption> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(SpecificationOption::getSpecId,specification.getId());
            specificationOptionMapper.delete(wrapper);
            this.saveSpecOption(specEntity);
        }
    }

    /**
     * 增加SpecEntity
     * @param specEntity
     */
    @Override
    public void add(SpecEntity specEntity){
        //获取规格对象
        Specification specification = specEntity.getSpecification();
        this.save(specification);
        this.saveSpecOption(specEntity);
    }

    private void saveSpecOption(SpecEntity specEntity){
        //获取规格选项集合对象
        List<SpecificationOption> specificationOptionList = specEntity.getSpecificationOptionList();
        //获取规格的编号
        Long id = specEntity.getSpecification().getId();
        for (SpecificationOption specificationOption : specificationOptionList) {
            specificationOption.setSpecId(id);
        }
        //批量保存规格选项
        specificationOptionService.saveBatch(specificationOptionList);
    }

    /**
     * 根据ID查询Specification
     * @param id
     * @return
     */
    @Override
    public SpecEntity findById(Long id){
        SpecEntity specEntity = new SpecEntity();
        Specification specification = this.getById(id);
        specEntity.setSpecification(specification);
        //获取规格选项
        LambdaQueryWrapper<SpecificationOption> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SpecificationOption::getSpecId,id);
        List<SpecificationOption> list = specificationOptionService.list(wrapper);
        specEntity.setSpecificationOptionList(list);
        return  specEntity;
    }

    /**
     * 查询Specification全部数据
     * @return
     */
    @Override
    public List<Specification> findAll() {
        return this.list(new QueryWrapper<Specification>());
    }

    @Override
    public List<Map<String,Object>> selectOptions() {
        QueryWrapper<Specification> wrapper = new QueryWrapper<>();
        wrapper.select("id","spec_name as text");
        List<Map<String, Object>> maps = this.listMaps(wrapper);
        return maps;
    }
}
