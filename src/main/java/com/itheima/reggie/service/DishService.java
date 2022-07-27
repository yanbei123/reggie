package com.itheima.reggie.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.itheima.reggie.common.R;
import com.itheima.reggie.dto.DishDto;
import com.itheima.reggie.entity.Dish;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: 燕北
 * @Date: 2022/06/25/16:27
 * @Description:
 */
public interface DishService extends IService<Dish> {

    //分页菜品信息
    public R<Page<DishDto>> pageDish(int page, int pageSize, String name);

    //根据菜品分类id进行查询
    public R<List<DishDto>> listDish(Dish dish);

    //新增菜品，同事插入菜品对应的口味数据，需要操作表：dish、dish_flavor
    public void saveWithFlavor(DishDto dishDto);

    //根据id查询菜品信息和对应的口味信息
    public DishDto getByIdWithFlavor(Long id);

    //更新菜品，同时更新菜品对应的口味数据
    public void updateWithFlavor(DishDto dishDto);

    //根据ids批量或单个删除菜品
    public void deleteByIds(List<Long> ids);

    //删除菜品，同时删除口味信息，如果删除菜品时存在套餐则直接返回删除失败
    public Boolean deleteWithFlavour(List<Long> ids);

    //根据ids批量或单个修改菜品状态
    public void updateMultiply(Integer status,List<Long> ids);

    //修改菜品
    public R<String> updateDishDto(DishDto dishDto);

    //根据id修改状态信息,可批量
    public R<String> updateStatus(Integer status, List<Long> ids);

    //根据id删除菜品
    public R<String> deleteDish(List<Long> ids);
}
