package com.fox.reggie.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fox.reggie.common.R;
import com.fox.reggie.entity.Orders;
import com.fox.reggie.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("order")
@Slf4j
public class OrderController {

    @Autowired
    private OrderService orderService;

    /**
     * 用户下单
     * @param orders
     * @return
     */
    @PostMapping("/submit")
    public R<String> submit(@RequestBody Orders orders){
        log.info("订单数据：{}",orders);
        orderService.submit(orders);
        return R.success("下单成功");
    }
    /**
     * 订单列表
     */
    @GetMapping("userPage")
    public R<Page> page(int page,int pageSize){
        ///构造分页构造器对象
        Page pageInfo = new Page(page,pageSize);
        //条件构造器
        LambdaQueryWrapper<Orders> queryWrapper = new LambdaQueryWrapper<>();
        //添加过滤条件
        //添加排序条件
        queryWrapper.orderByDesc(Orders::getCheckoutTime);

        //执行分页查询
        orderService.page(pageInfo,queryWrapper);
        return R.success(pageInfo);
    }
    @GetMapping("page")
    public R<Page> page(int page,int pageSize,Long number){
        ///构造分页构造器对象
        Page pageInfo = new Page(page,pageSize);
        //条件构造器
        LambdaQueryWrapper<Orders> queryWrapper = new LambdaQueryWrapper<>();
        //添加过滤条件
        queryWrapper.like(number!=null,Orders::getId,number);
        //添加排序条件
        queryWrapper.orderByDesc(Orders::getCheckoutTime);

        //执行分页查询
        orderService.page(pageInfo,queryWrapper);
        return R.success(pageInfo);
    }
    @PutMapping
    public R<String> statue(@RequestBody Orders orders){
        orderService.updateById(orders);
        return R.success("派送成功");
    }

}
