package com.itheima.reggie.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.itheima.reggie.common.R;
import com.itheima.reggie.dto.DishDto;
import com.itheima.reggie.dto.SetmealDto;
import com.itheima.reggie.entity.Setmeal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: 燕北
 * @Date: 2022/06/25/16:28
 * @Description:
 */
public interface SetMealService extends IService<Setmeal> {
    /**
     * 新增套餐，同时需要保存套餐和菜品的关联关系
     * @param setmealDto
     */
    public void saveWithDish(SetmealDto setmealDto);

    /**
     * 删除套餐
     * @param ids
     */
    public void removeWithDish(List<Long> ids);

    /**
     * 根据id查询套餐
     * @param id
     * @return
     */
    public SetmealDto getMeal(Long id);

    //批量更改状态
    public void updateMultiply(Integer status, List<Long> ids);

    //分页
    public R<Page<SetmealDto>> pageSetMeal(Integer page, Integer pageSize, String name);

    //根据条件查询套餐数据
    public R<List<Setmeal>> listSetMeal(Setmeal setmeal);

    //修改套餐数据
    public R<String> modifySetMeal(SetmealDto setmealDto);

    //根据id查询套餐内菜品信息
    public R<List<DishDto>> dish(Long setmealId);
}
