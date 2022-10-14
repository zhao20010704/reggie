package com.fox.reggie.entity.dto;

import com.fox.reggie.entity.Dish;
import com.fox.reggie.entity.DishFlavor;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 菜品，菜品口味综合实体类
 */
@Data
public class DishDto extends Dish {
    private List<DishFlavor> flavors = new ArrayList<>();
    private String categoryName;
    private Integer copies;
}
