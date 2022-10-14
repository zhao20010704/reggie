package com.fox.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fox.reggie.common.R;
import com.fox.reggie.entity.*;
import com.fox.reggie.entity.dto.SetmealDto;
import com.fox.reggie.service.CategoryService;
import com.fox.reggie.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@Slf4j
@RequestMapping("setmeal")
public class SetMealController {
    @Autowired
    private SetmealService setmealService;
    @Autowired
    private CategoryService categoryService;
    /**
     * 需求分析
     * 1、新增套餐
     * 2、套餐分页，
     * 3、修改套餐
     * 4、批量删除，批量状态变化
     */
    /**
     * 1、新增套餐
     * 1.1、套餐分类展示，菜品展示
     * 1.2、上传图片 完成
     * 1.3、数据传输
     */
    /**
     * 新增套餐
     * @param setmealDto
     * @return
     */
    @PostMapping
    public R<String> save(@RequestBody SetmealDto setmealDto){
        log.info("setmelDto:{}",setmealDto);

        setmealService.saveWithDish(setmealDto);

        return R.success("新增套餐成功");
    }
    /**
     * 2、分页查询管理
     * 2.1、构造分页构造器
     * 2.2、构造条件构造器
     * 2.3、对象拷贝 将数据库内已经存在的菜品图片进行上传添加
     * 2.4、执行查询
     */
    /**
     * 分页查询管理
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("page")
    public R<Page> page(int page, int pageSize, String name){
        log.info("page:{},pageSize:{},name:{}",page,pageSize,name);
//        2.1、构造分页构造器
        Page<Setmeal> pageInfo = new Page<>(page,pageSize);
        Page<SetmealDto> dtoPage = new Page<>();
//        2.2、构造条件构造器
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper();
        //添加过滤条件
        queryWrapper.like(name!=null,Setmeal::getName,name);
        //添加排序条件
        queryWrapper.orderByDesc(Setmeal::getUpdateTime);
        //执行分页查询
        setmealService.page(pageInfo,queryWrapper);
//        2.3、对象拷贝 将数据库内已经存在的菜品图片进行上传添加
        BeanUtils.copyProperties(pageInfo,dtoPage,"records");
        List<Setmeal> records = pageInfo.getRecords();

        List<SetmealDto> list = records.stream().map((item) -> {
            SetmealDto setmealDto = new SetmealDto();
            //对象拷贝
            BeanUtils.copyProperties(item,setmealDto);
            //分类id
            Long categoryId = item.getCategoryId();
            //根据分类id查询分类对象
            Category category = categoryService.getById(categoryId);
            if(category != null){
                //分类名称
                String categoryName = category.getName();
                setmealDto.setCategoryName(categoryName);
            }
            return setmealDto;
        }).collect(Collectors.toList());

        dtoPage.setRecords(list);
        return R.success(dtoPage);
    }
    /**
     * 3、修改套餐
     * 3.1、得到点击套餐信息在修改套餐页面上
     * 3.2、修改信息处理
     */
    /**
     * 得到点击套餐信息在修改套餐页面上
     * @param id
     * @return
     */
    @GetMapping("{id}")
    public R<Setmeal> get(@PathVariable Long id){
        log.info("id:{}",id);
        SetmealDto setmealDto = setmealService.getByIdWithFlavor(id);
        return R.success(setmealDto);
    }

    /**
     * 修改信息处理
     * @param setmealDto
     * @return
     */
    @PutMapping
    public R<String> update(@RequestBody SetmealDto setmealDto){
        log.info("套餐更新......");
        setmealService.updateWithFlavor(setmealDto);
        return R.success("套餐更新成功");
    }

    /**
     * 4、批量删除，批量状态变化
     * 4.1、删除
     * 4.2、状态
     */
    /**
     * 删除套餐
     * @param ids
     * @return
     */
    @DeleteMapping
    public R<String> delete(Long[] ids){
        log.info("删除套餐id：{}", ids);
        setmealService.removeWithDish(ids);
        return R.success("删除套餐成功");
    }
    @PostMapping("status/{status}")
    public R<String> Status(@PathVariable Integer status,Long[] ids){
        log.info("ids：{}",ids);
        List<Long> list = Arrays.asList(ids);
        //构造条件构造器
        LambdaUpdateWrapper<Setmeal> updateWrapper = new LambdaUpdateWrapper<>();
        //添加过滤条件
        updateWrapper.set(Setmeal::getStatus,status).in(Setmeal::getId,list);
        setmealService.update(updateWrapper);
        return R.success("菜品状态修改成功");
    }
    @GetMapping("list")
    public R<List<Setmeal>> list(Setmeal setmeal){
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(setmeal.getCategoryId() != null,Setmeal::getCategoryId,setmeal.getCategoryId());
        queryWrapper.eq(setmeal.getStatus() != null,Setmeal::getStatus,setmeal.getStatus());
        queryWrapper.orderByDesc(Setmeal::getUpdateTime);

        List<Setmeal> list = setmealService.list(queryWrapper);
        return R.success(list);
    }

}
