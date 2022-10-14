package com.fox.reggie.entity.dto;

import com.fox.reggie.entity.Setmeal;
import com.fox.reggie.entity.SetmealDish;
import lombok.Data;

import java.util.List;

@Data
public class SetmealDto extends Setmeal {
    private List<SetmealDish> setmealDishes; //套餐关联菜品列表
    private String categoryName;//套餐分类名称
}