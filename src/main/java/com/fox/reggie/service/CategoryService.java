package com.fox.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fox.reggie.entity.Category;

public interface CategoryService extends IService<Category> {
    //在CategoryService中扩展remove方法
    //根据ID删除分类
    void remove(Long id);
}
