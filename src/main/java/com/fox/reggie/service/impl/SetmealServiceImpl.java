package com.fox.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fox.reggie.common.ex.CustomException;
import com.fox.reggie.entity.Setmeal;
import com.fox.reggie.entity.SetmealDish;
import com.fox.reggie.entity.dto.SetmealDto;
import com.fox.reggie.mapper.SetmealMapper;
import com.fox.reggie.service.SetmealDishService;
import com.fox.reggie.service.SetmealService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SetmealServiceImpl extends ServiceImpl<SetmealMapper, Setmeal> implements SetmealService {

    @Autowired
    private SetmealDishService setmealDishService;

    /**
     * 新增套餐，同时需要保存套餐和菜品的关联关系
     * @param setmealDto
     */
    @Override
    public void saveWithDish(SetmealDto setmealDto) {
        //保存套餐的基本信息，操作setmeal，执行insert操作
        this.save(setmealDto);

        List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();
        setmealDishes.stream().map((item) -> {
            item.setSetmealId(setmealDto.getId());
            return item;
        }).collect(Collectors.toList());

        //保存套餐和菜品的关联信息，操作setmeal_dish,执行insert操作
        setmealDishService.saveBatch(setmealDishes);
    }

    /**
     * 根据id查询套餐信息和对应的菜品信息
     * @param id
     */
    @Override
    public SetmealDto getByIdWithFlavor(Long id) {
        //查询套餐基本信息，从setmeal表查询
        Setmeal setmeal = this.getById(id);

        SetmealDto setmealDto = new SetmealDto();
        BeanUtils.copyProperties(setmeal,setmealDto);
        //查询当前套餐对应的餐品信息，从setmealDish表查询
        LambdaQueryWrapper<SetmealDish> queryWrapper = new LambdaQueryWrapper<>();

        queryWrapper.eq(SetmealDish::getSetmealId,setmeal.getId());

        List<SetmealDish> setmealDishes = setmealDishService.list(queryWrapper);

        setmealDto.setSetmealDishes(setmealDishes);

        return setmealDto;
    }

    /**
     * 更新套餐，同时需要更新套餐和菜品的关联关系
     * @param setmealDto
     */
    @Override
    public void updateWithFlavor(SetmealDto setmealDto) {
        //更改信息
        this.updateById(setmealDto);
        //清理当前菜品对应口味数据---setmealDish表的delete操作
        LambdaQueryWrapper<SetmealDish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SetmealDish::getSetmealId,setmealDto.getId());
        setmealDishService.remove(queryWrapper);
        //添加当前提交过来的口味数据---setmealDish表的insert操作
        List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();
        setmealDishes = setmealDishes.stream().map((item)->{
            item.setSetmealId(setmealDto.getId());
            return item;
        }).collect(Collectors.toList());
        setmealDishService.saveBatch(setmealDishes);
    }

    /**
     * 删除套餐，同时删除套餐关联数据
     * @param ids
     */
    @Override
    public void removeWithDish(Long[] ids){
        //select count(*) from setmeal where id in (1,2,3) and status = 1
        //查询套餐状态，确定是否可用删除
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper();
        queryWrapper.in(Setmeal::getId,ids);
        queryWrapper.eq(Setmeal::getStatus,1);
        //如果不能删除，抛出一个业务异常
        int count = this.count(queryWrapper);
        if (count>0){
            throw new CustomException("套餐正在售卖，不能删除");
        }
        //如果可以删除，先删除套餐表中的数据---setmeal
        this.removeById(ids);
        //搜索套餐关联数据
        //delete from setmeal_dish where setmeal_id in (1,2,3)
        LambdaQueryWrapper<SetmealDish> queryWrapper1 = new LambdaQueryWrapper<>();
        queryWrapper1.in(SetmealDish::getSetmealId,ids);
        //删除关联数据
        setmealDishService.removeById(queryWrapper1);
    }
}
