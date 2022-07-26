package com.itheima.reggie.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.Category;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: 燕北
 * @Date: 2022/06/25/11:21
 * @Description:
 */
public interface CategoryService extends IService<Category> {

    //分页
    public R<Page<Category>> pageCategory(int page, int pageSize);

    //根据id删除分类
    public R<String> deleteById(Long id);

    //修改分类信息
    public R<String> updateByCategory(Category category);

    //查询分类数据
    public R<List<Category>> listCategory(Category category);

    //添加分类
    public R<String> saveCategory(Category category);
}
