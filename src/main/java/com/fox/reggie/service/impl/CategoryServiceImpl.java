package com.fox.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fox.reggie.common.ex.CustomException;
import com.fox.reggie.common.ex.GlobalExceptionHandler;
import com.fox.reggie.entity.Category;
import com.fox.reggie.entity.Dish;
import com.fox.reggie.entity.Setmeal;
import com.fox.reggie.mapper.CategoryMapper;
import com.fox.reggie.service.CategoryService;
import com.fox.reggie.service.DishService;
import com.fox.reggie.service.SetmealService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper,Category> implements CategoryService{

    @Autowired
    private DishService dishService;
    @Autowired
    private SetmealService setmealService;

//    在CategoryServiceImpl中实现remove方法

    /**
     * 根据id删除分类
     * @param id
     */
    @Override
    public void remove(Long id) {
        //添加查询条件，根据分类id进行查询菜品数据
        LambdaQueryWrapper<Dish> dishLambdaQueryWrapper = new LambdaQueryWrapper<>();
        dishLambdaQueryWrapper.eq(Dish::getCategoryId,id);
        //如果已经关联，抛出一个业务异常
        int count1 = dishService.count(dishLambdaQueryWrapper);
        if (count1>0){
            throw new CustomException("当前分类下关联了菜品，不能删除");
        }
        //已经关联菜品，抛出一个业务异常
        LambdaQueryWrapper<Setmeal> setmealLambdaQueryWrapper = new LambdaQueryWrapper<>();
        //查询当前分类是否关联了套餐，如果已经关联，抛出一个业务异常
        setmealLambdaQueryWrapper.eq(Setmeal::getCategoryId,id);
        int count2 = setmealService.count(setmealLambdaQueryWrapper);
        //已经关联套餐，抛出一个业务异常
        if (count2>0){
            throw new CustomException("当前分类下关联了套餐，不能删除");
        }
        //正常删除分类
        super.removeById(id);
    }
}
