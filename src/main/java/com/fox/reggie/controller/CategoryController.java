package com.fox.reggie.controller;


import com.alibaba.druid.sql.visitor.functions.If;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fox.reggie.common.R;
import com.fox.reggie.entity.Category;
import com.fox.reggie.service.CategoryService;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("category")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    /**
     * 新增菜品、套餐
     * @param category
     * @return
     * 步骤
     * 1、页面(backend/page/category/list.html)发送ajax请求，将新增分类窗口输入的数据以json形式提交到服务端
     * 2、服务端Controller接收页面提交的数据并调用Service将数据进行保存
     * 3、Service调用Mapper操作数据库，保存数据
     */
    @PostMapping
    public R<String> save(@RequestBody Category category){
        log.info("新增分类，category信息：{}",category.toString());
        //Service调用Mapper操作数据库，保存数据
        categoryService.save(category);
        return R.success("增加分类成功");
    }

    /**
     *
     * @param page
     * @param pageSize
     * @return
     */
    /**
     * 执行步骤
     * 1、构造分页构造器
     * 2、构造条件构造器
     * 3、执行查询
     */
    @GetMapping("page")
    public R<Page> page(int page, int pageSize){
        log.info("page={},pageSize={}",page,pageSize);
        //1、构造分页构造器
        Page<Category> pageInfo = new Page<>(page,pageSize);
        //2.1、构造条件构造器
        LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();
        //2.2、添加排序条件 以sort为序
        queryWrapper.orderByAsc(Category::getSort);
        //3、执行查询
        categoryService.page(pageInfo,queryWrapper);
        return R.success(pageInfo);
    }

    /**
     * 修改菜品
     * @param category
     * @return
     */
    @PutMapping
    public R<String> update(@RequestBody Category category) {
        log.info("新增菜品，category信息：{}", category.toString());
        //Service调用Mapper操作数据库，保存数据
        categoryService.updateById(category);
        return R.success("修改成功");
    }

    /**
     *
     * @param id
     * @return
     */
    @DeleteMapping
    public R<String> delete(Long id){
        log.info("删除分类id：{}", id);
        categoryService.remove(id);
        return R.success("删除成功");
    }

    /**
     * 菜品分类 下拉框
     * @param category
     * @return
     */
    @GetMapping("/list")
    public R<List<Category>> list(Category category){
        log.info("category:{}",category);
        //1.1、构造条件构造器
        LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();
        //1.2、添加参数
        queryWrapper.eq(category.getType()!=null,Category::getType,category.getType());
        //1.2、添加排序条件 以sort为序
        queryWrapper.orderByAsc(Category::getSort).orderByDesc(Category::getUpdateTime);
        //2、查询展示
        List<Category> list = categoryService.list(queryWrapper);
        return R.success(list);
    }
}
