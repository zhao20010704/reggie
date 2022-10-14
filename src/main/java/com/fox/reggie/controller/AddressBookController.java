package com.fox.reggie.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.fox.reggie.common.BaseContext;
import com.fox.reggie.common.R;
import com.fox.reggie.entity.AddressBook;
import com.fox.reggie.service.AddressBookService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 移动端地址管理
 */
@RestController
@Slf4j
@RequestMapping("addressBook")
public class AddressBookController {
    @Autowired
    private AddressBookService addressBookService;
    /**
     * 1、新增收货地址
     * 2、地址管理,页面展示
     * 3、修改地址
     * 4、删除地址
     * 5、设置默认地址
     */
    /**
     * 1、新增收货地址
     *  需要记录当前是哪个用户的地址(关联当前登录用户)
     */
    @PostMapping
    public R<AddressBook> save(@RequestBody AddressBook addressBook){
        log.info("addressBook:{}",addressBook);
        addressBook.setUserId(BaseContext.getCurrentId());
        addressBookService.save(addressBook);
        return R.success(addressBook);
    }

    /**
     * 2、地址管理,列表展示
     * @param addressBook
     * @return
     */
    @GetMapping("list")
    public R<List<AddressBook>> list(AddressBook addressBook){
        addressBook.setUserId(BaseContext.getCurrentId());
        log.info("addressBook:{}", addressBook);

        //条件构造器
        LambdaQueryWrapper<AddressBook> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(null != addressBook.getUserId(), AddressBook::getUserId, addressBook.getUserId());
        //SQL:select * from address_book where user_id = ? order by update_time desc
        queryWrapper.orderByDesc(AddressBook::getUpdateTime);


        return R.success(addressBookService.list(queryWrapper));
    }
    /**
     * 3、修改地址
     * 3.1、根据id,跳转找到地址信息
     * 3.2、修改信息
     */
    /**
     * 3.1 根据id,跳转找到地址信息
     * @param id
     * @return
     */
    @GetMapping("{id}")
    public R<AddressBook> get(@PathVariable Long id){
        AddressBook addressBook = addressBookService.getById(id);
        if (addressBook != null) {
            return R.success(addressBook);
        } else {
            return R.error("没有找到该对象");
        }
    }

    /**
     * 修改信息
     * @param addressBook
     * @return
     */
    @PutMapping
    public R<String> update(@RequestBody AddressBook addressBook){
        addressBookService.updateById(addressBook);
        return R.success("修改地址成功");
    }
    /**
     * 4、删除地址
     */
    @DeleteMapping
    public R<String> remove(Long ids){
        addressBookService.removeById(ids);
        return R.success("删除地址成功");
    }

    /**
     * 设置默认地址
     * @param addressBook
     * @return
     */

    @PutMapping("default")
    public R<AddressBook> setDefault(@RequestBody AddressBook addressBook){
        //遍历全部地址，将他们的defaule全部设为0
        LambdaUpdateWrapper<AddressBook> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(AddressBook::getUserId, BaseContext.getCurrentId());
        wrapper.set(AddressBook::getIsDefault, 0);
        //SQL:update address_book set is_default = 0 where user_id = ?
        addressBookService.update(wrapper);
        //选中的地址default设置为1
        addressBook.setIsDefault(1);
        //SQL:update address_book set is_default = 1 where id = ?
        addressBookService.updateById(addressBook);
        return R.success(addressBook);
    }
    /**
     * 获取默认地址
     */
    @GetMapping("default")
    public R<AddressBook> getDefault(HttpServletRequest servletRequest){

        Long id = (Long) servletRequest.getSession().getAttribute("user");

        LambdaQueryWrapper<AddressBook> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AddressBook::getUserId,id);
        queryWrapper.eq(AddressBook::getIsDefault,1);

        AddressBook addressBook = addressBookService.getOne(queryWrapper);
        return  R.success(addressBook);
    }
}
