package com.fox.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fox.reggie.entity.Dish;
import com.fox.reggie.entity.dto.DishDto;
import com.fox.reggie.entity.DishFlavor;
import com.fox.reggie.mapper.DishMapper;
import com.fox.reggie.service.DishFlavorService;
import com.fox.reggie.service.DishService;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class DishServiceImpl extends ServiceImpl<DishMapper, Dish> implements DishService {

    @Autowired
    private DishFlavorService dishFlavorService;
    /**
     * 具体步骤
     * 1、保存菜品基本信息 ;
     * 2、获取保存的菜品ID ;
     * 3、获取菜品口味列表，遍历列表，为菜品口味对象属性dishId赋值;
     * 4、批量保存菜品口味列表;
     * @param dishDto
     */
    @Override
    public void saveWhitFlavor(DishDto dishDto) {
        log.info("菜品基本信息 dishDto:{}",dishDto);
//        1、保存菜品基本信息 ;
        this.save(dishDto);
//        2、获取保存的菜品ID ;
        Long dishId = dishDto.getId();
        log.info("dishId:{}",dishId);
//        3、获取菜品口味列表，遍历列表，为菜品口味对象属性dishId赋值;
        List<DishFlavor> flavors = dishDto.getFlavors();
        //正则表达式，flavors 遍历菜品口味中全部信息，如果有相同的dishId则return item(表示无意义,跳过该信息),没有则自身赋值为传输过来的菜品口味信息，直至遍历结束
        flavors = flavors.stream().map((item)->{
            item.setDishId(dishId);
            return item;
        }).collect(Collectors.toList());
//        4、批量保存菜品口味列表;
        dishFlavorService.saveBatch(flavors);
    }

    /**
     * @param id
     * @return
     * 实现步骤
     * 1.根据ID查询菜品的基本信息
     * 2.根据菜品的ID查询菜品口味列表数据
     * 3.组装数据并返回
     */
    @Override
    public DishDto getByIdWithFlavor(Long id) {
        //查询菜品基本信息，从dish表查询
        Dish dish = this.getById(id);

        DishDto dishDto = new DishDto();
        BeanUtils.copyProperties(dish,dishDto);

        //查询当前菜品对应的口味信息，从dish_flavor表查询
        LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DishFlavor::getDishId,dish.getId());
        List<DishFlavor> flavors = dishFlavorService.list(queryWrapper);
        dishDto.setFlavors(flavors);

        return dishDto;
    }

    /**
     * 在该方法中，我们既需要更新dish菜品基本信息表，还需要更新dish_flavor菜品口味表。
     * 而页面再操作时，关于菜品的口味，有修改，有新增，也有可能删除，我们应该如何更新菜品口味信息呢，
     * 其实，无论菜品口味信息如何变化，我们只需要保持一个原则： 先删除，后添加。
     * @param dishDto
     */
    @Override
    public void updateWithFlavor(DishDto dishDto) {
        //更改信息
        this.updateById(dishDto);
        //清理当前菜品对应口味数据---dish_flavor表的delete操作
        LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper();
        queryWrapper.eq(DishFlavor::getDishId,dishDto.getId());

        dishFlavorService.remove(queryWrapper);
        //添加当前提交过来的口味数据---dish_flavor表的insert操作
        List<DishFlavor> flavors = dishDto.getFlavors();
        flavors = flavors.stream().map((item) -> {
            item.setDishId(dishDto.getId());
            return item;
        }).collect(Collectors.toList());

        dishFlavorService.saveBatch(flavors);
    }

    @Override
    public void removeWithFlavor(Long[] id) {
        //删除信息
        List<Long> list = Arrays.asList(id);
        this.removeByIds(list);
        //清理当前菜品对应口味数据---dish_flavor表的delete操作
        for (int i = 0; i < list.size(); i++) {
            LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper();
            queryWrapper.eq(DishFlavor::getDishId,id[i]);
            log.info("id[:{}]:{}",i,id[i]);
            dishFlavorService.remove(queryWrapper);
        }


    }
}