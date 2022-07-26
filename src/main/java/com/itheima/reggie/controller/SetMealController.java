package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.CustomException;
import com.itheima.reggie.common.R;
import com.itheima.reggie.dto.DishDto;
import com.itheima.reggie.dto.SetmealDto;
import com.itheima.reggie.entity.Category;
import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.entity.Setmeal;
import com.itheima.reggie.entity.SetmealDish;
import com.itheima.reggie.service.CategoryService;
import com.itheima.reggie.service.DishService;
import com.itheima.reggie.service.SetMealDishService;
import com.itheima.reggie.service.SetMealService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: 燕北
 * @Date: 2022/07/01/16:52
 * @Description: 套餐管理
 */
@Slf4j
@RestController
@RequestMapping("/setmeal")
public class SetMealController {

    @Autowired
    private SetMealService setMealService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private SetMealDishService setMealDishService;

    @Autowired
    private DishService dishService;

    /**
     * 新增套餐
     * @param setmealDto
     * @return
     */
    @PostMapping
    public R<String> save(@RequestBody SetmealDto setmealDto){
        log.info("套餐信息:{}",setmealDto);
        setMealService.saveWithDish(setmealDto);
        return R.success("套餐添加成功");
    }

    /**
     * 分页操作
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    public R<Page> page(Integer page,Integer pageSize,String name){
        //分页构造器
        Page<Setmeal> pageInfo = new Page<>(page,pageSize);
        Page<SetmealDto> dtoPage = new Page<>();
        //条件构造器
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        //查询条件,name不为空
        queryWrapper.like(name != null,Setmeal::getName,name);
        //排序条件
        queryWrapper.orderByDesc(Setmeal::getUpdateTime);

        //分页方法
        setMealService.page(pageInfo,queryWrapper);

        //对象拷贝
        BeanUtils.copyProperties(pageInfo,dtoPage,"records");
        List<Setmeal> records = pageInfo.getRecords();

        List<SetmealDto> list = records.stream().map((item) -> {
            SetmealDto setmealDto = new SetmealDto();
            BeanUtils.copyProperties(item,setmealDto);
            //分类id
            Long categoryId = item.getCategoryId();
            //根据id查询分类对象
            Category category = categoryService.getById(categoryId);
            if (category != null) {
                //分类名称
                String categoryName = category.getName();
                setmealDto.setCategoryName(categoryName);
            }
            return setmealDto;
        }).collect(Collectors.toList());

        dtoPage.setRecords(list);
        return R.success(dtoPage);
    }

    /**
     * 删除/批量删除套餐
     * @param ids
     * @return
     */
    @DeleteMapping
    public R<String> delete(@RequestParam List<Long> ids){
        log.info("ids:{}",ids);

        setMealService.removeWithDish(ids);

        return R.success("套餐数据删除成功");
    }

    /**
     * 根据条件查询套餐数据
     * @param setmeal
     * @return
     */
    @GetMapping("/list")
    public R<List<Setmeal>> list(Setmeal setmeal){
        //条件构造器
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        //构造条件
        queryWrapper.eq(setmeal.getCategoryId() != null,Setmeal::getCategoryId,setmeal.getCategoryId());
        queryWrapper.eq(setmeal.getStatus() != null,Setmeal::getStatus,setmeal.getStatus());
        //排序
        queryWrapper.orderByDesc(Setmeal::getUpdateTime);
        //查询套餐分类
        List<Setmeal> list = setMealService.list(queryWrapper);

        return R.success(list);
    }

    //根据id查询套餐信息
    @GetMapping("/{id}")
    public R<SetmealDto> update(@PathVariable Long id){
        log.info("id:{}",id);
        //根据id查询当前套餐信息
        SetmealDto setmealDto = setMealService.getMeal(id);
        return R.success(setmealDto);
    }

    //修改套餐数据
    @PutMapping
    public R<String> modifySetMeal(@RequestBody SetmealDto setmealDto){
        log.info("setMealDto:{}",setmealDto);
        if (setmealDto == null){
            return R.error("修改异常");
        }
        if (setmealDto.getSetmealDishes() == null){
            return R.error("套餐中没有菜品信息，请添加菜品");
        }
        List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();
        Long setmealDtoId = setmealDto.getId();

        //清空表格信息
        LambdaQueryWrapper<SetmealDish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SetmealDish::getSetmealId,setmealDtoId);
        setMealDishService.remove(queryWrapper);

        //填充表格
        for (SetmealDish setmealDish :
                setmealDishes) {
            setmealDish.setSetmealId(setmealDtoId);
        }
        setMealDishService.saveBatch(setmealDishes);
        setMealService.updateById(setmealDto);
        return R.success("套餐修改成功");
    }

    /**
     * 批量或单个修改状态信息
     * @param status
     * @param ids
     * @return
     */
    @PostMapping("/status/{status}")
    public R<String> updateStatus(@PathVariable Integer status, @RequestParam List<Long> ids){
        log.info("status:{}",status);
        log.info("ids:{}",ids);
        //判断ids是否存在
        if (ids != null){
            setMealService.updateMultiply(status,ids);
            return R.success("状态修改成功");
        }
        return R.error("修改状态失败");
    }

    @GetMapping("/dish/{id}")
    public R<List<DishDto>> dish(@PathVariable("id") Long setmealId){
        LambdaQueryWrapper<SetmealDish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SetmealDish::getSetmealId,setmealId);
        //获取菜品信息
        List<SetmealDish> dishList = setMealDishService.list(queryWrapper);

        List<DishDto> dishDtos = dishList.stream().map((item) -> {
            DishDto dishDto = new DishDto();
            //对象拷贝
            BeanUtils.copyProperties(item, dishDto);
            //获取dish信息
            Long dishId = item.getDishId();
            Dish dish = dishService.getById(dishId);
            BeanUtils.copyProperties(dish, dishDto);
            return dishDto;
        }).collect(Collectors.toList());
        return R.success(dishDtos);
    }

}
