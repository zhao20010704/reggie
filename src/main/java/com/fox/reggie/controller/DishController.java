package com.fox.reggie.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fox.reggie.common.R;
import com.fox.reggie.entity.Category;
import com.fox.reggie.entity.Dish;
import com.fox.reggie.entity.DishFlavor;
import com.fox.reggie.entity.dto.DishDto;

import com.fox.reggie.service.CategoryService;
import com.fox.reggie.service.DishFlavorService;
import com.fox.reggie.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@Slf4j
@RequestMapping("dish")
public class DishController {

    @Autowired
    private DishService dishService;
    @Autowired
    private DishFlavorService dishFlavorService;
    @Autowired
    private CategoryService categoryService;
    /**
     * 基本流程
     * 1、新增菜品功能
     * 2、分页管理
     * 3、删除、修改
     * 4、批量操作
     * 5、优化
     */

    /**
     * 新增菜品
     * 1.1、上传图片 CommonController内完成
     * 1.2、菜品分类 下拉框 CategoryController内完成
     * 1.3、页面信息传输
     */
    @PostMapping
    public R<String> save(@RequestBody DishDto dishDto){
        log.info("菜品信息 dishDto:{}",dishDto);

        dishService.saveWhitFlavor(dishDto);

        return R.success("新增菜品成功");
    }

    /**
     * 分页查询菜品
     * @param page
     * @param pageSize
     * @param name
     * @return
     * 步骤
        1、构造分页构造器
        2、构造条件构造器
        2.1对象拷贝 将数据库内已经存在的菜品图片进行上传添加
        3、执行查询
     */
    @GetMapping("page")
    public R<Page> page(int page, int pageSize, String name){
        log.info("page={},pageSize={},name={}",page,pageSize,name);
        ///构造分页构造器对象
        Page<Dish> pageInfo = new Page<>(page,pageSize);
        Page<DishDto> dishDtoPage = new Page<>();

        //条件构造器
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        //添加过滤条件
        queryWrapper.like(name != null,Dish::getName,name);
        //添加排序条件
        queryWrapper.orderByDesc(Dish::getUpdateTime);

        //执行分页查询
        dishService.page(pageInfo,queryWrapper);

        //对象拷贝
        BeanUtils.copyProperties(pageInfo,dishDtoPage,"records");
        List<Dish> records = pageInfo.getRecords();
        List<DishDto> list = records.stream().map((item) -> {
            DishDto dishDto = new DishDto();
            BeanUtils.copyProperties(item,dishDto);
            Long categoryId = item.getCategoryId();//分类id
            //根据id查询分类对象
            Category category = categoryService.getById(categoryId);

            if(category != null){
                String categoryName = category.getName();
                dishDto.setCategoryName(categoryName);
            }
            return dishDto;
        }).collect(Collectors.toList());
        dishDtoPage.setRecords(list);

        return R.success(dishDtoPage);
    }

    /**
     * 删除菜品
     * @param id
     * @return
     */
    @DeleteMapping
    public R<String> delete(Long[] id){

        log.info("删除菜品id：{}", id);
        //categoryService.remove(id);
        dishService.removeWithFlavor(id);
        return R.success("删除成功");
    }

    /**
     * 菜品状态修改，批量修改
     * @param status
     * @param id
     * @return
     */
    @PostMapping("status/{status}")
    public R<String> Status(@PathVariable Integer status,Long[] id){
        log.info("id：{}",id);
        List<Long> list = Arrays.asList(id);
        //构造条件构造器
        LambdaUpdateWrapper<Dish> updateWrapper = new LambdaUpdateWrapper<>();
        //添加过滤条件
        updateWrapper.set(Dish::getStatus,status).in(Dish::getId,list);
        dishService.update(updateWrapper);

        return R.success("菜品状态修改成功");
    }

    /**
     * 通过id得到菜品信息
     * @param id
     * @return
     */
    @GetMapping("{id}")
    public R<DishDto> get(@PathVariable Long id){
        DishDto dishDto = dishService.getByIdWithFlavor(id);
        return R.success(dishDto);
    }

    /**
     * 更新菜品
     * @param dishDto
     * @return
     */
    @PutMapping
    public R<String> update(@RequestBody DishDto dishDto){
        log.info("菜品信息 dishDto:{}",dishDto);
        dishService.updateWithFlavor(dishDto);
        return R.success("更新菜品成功");
    }
    /**
     * 新增菜品分类，
     */
//    @GetMapping("list")
//    public R<List<Dish>> list(Dish dish){
//        log.info("dish:{}",dish);
//        //1.1、构造条件构造器
//        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
//        //1.2、添加参数
//        queryWrapper.eq(dish.getCategoryId()!=null,Dish::getCategoryId,dish.getCategoryId());
//        //1.2、添加排序条件 以sort为序
//        queryWrapper.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);
//        //2、查询展示
//        List<Dish> list = dishService.list(queryWrapper);
//        return R.success(list);
//    }
    @GetMapping("/list")
    public R<List<DishDto>> list(Dish dish){
        //构造查询条件
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(dish.getCategoryId() != null ,Dish::getCategoryId,dish.getCategoryId());
        //添加条件，查询状态为1（起售状态）的菜品
        queryWrapper.eq(Dish::getStatus,1);

        //添加排序条件
        queryWrapper.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);

        List<Dish> list = dishService.list(queryWrapper);

        List<DishDto> dishDtoList = list.stream().map((item) -> {
            DishDto dishDto = new DishDto();

            BeanUtils.copyProperties(item,dishDto);

            Long categoryId = item.getCategoryId();//分类id
            //根据id查询分类对象
            Category category = categoryService.getById(categoryId);

            if(category != null){
                String categoryName = category.getName();
                dishDto.setCategoryName(categoryName);
            }

            //当前菜品的id
            Long dishId = item.getId();
            LambdaQueryWrapper<DishFlavor> lambdaQueryWrapper = new LambdaQueryWrapper<>();
            lambdaQueryWrapper.eq(DishFlavor::getDishId,dishId);
            //SQL:select * from dish_flavor where dish_id = ?
            List<DishFlavor> dishFlavorList = dishFlavorService.list(lambdaQueryWrapper);
            dishDto.setFlavors(dishFlavorList);
            return dishDto;
        }).collect(Collectors.toList());

        return R.success(dishDtoList);
    }

}
