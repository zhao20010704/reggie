package com.fox.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fox.reggie.entity.Dish;
import com.fox.reggie.entity.dto.DishDto;


public interface DishService extends IService<Dish> {
    /**
     * 增加菜品信息
     * 新增菜品，同时插入菜品对应的口味数据，需要操作两张表：dish、dish_flavor
     * @param dishDto
     */
    void saveWhitFlavor(DishDto dishDto);
    /**
     * 根据id查询菜品信息和对应的口味信息
     * @param id
     * @return
     */
    public DishDto getByIdWithFlavor(Long id);
    /**
     * 更新菜品信息，同时更新对应的口味信息
     * @param dishDto
     */
    public void updateWithFlavor(DishDto dishDto);

    /**
     * 批量删除菜品信息，同时删除对应的口味信息
     * @param id
     */
    void removeWithFlavor(Long[] id);
}
