package com.fox.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fox.reggie.entity.Setmeal;
import com.fox.reggie.entity.dto.SetmealDto;


public interface SetmealService extends IService<Setmeal> {
    /**
     * 新增套餐，同时需要保存套餐和菜品的关联关系
     * @param setmealDto
     */
    public void saveWithDish(SetmealDto setmealDto);
    /**
     * 根据id查询套餐信息和对应的菜品信息
     * @param id
     */
    public SetmealDto getByIdWithFlavor(Long id);

    /**
     * 更新套餐，同时需要更新套餐和菜品的关联关系
     * @param setmealDto
     */
    public void updateWithFlavor(SetmealDto setmealDto);
    /**
     * 批量删除套餐信息，同时删除对应的菜品信息
     * @param ids
     */
    void removeWithDish(Long[] ids);
}
