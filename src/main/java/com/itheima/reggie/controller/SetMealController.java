package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.R;
import com.itheima.reggie.dto.DishDto;
import com.itheima.reggie.dto.SetmealDto;
import com.itheima.reggie.entity.Setmeal;
import com.itheima.reggie.service.SetMealService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    /**
     * 新增套餐
     * @param setmealDto
     * @return
     */
    @CachePut(value = "setMealCache", key = "'setmeal' + #setmeal.categoryId + '_1'")
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
    public R<Page<SetmealDto>> page(Integer page,Integer pageSize,String name){
        return setMealService.pageSetMeal(page,pageSize,name);
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
    @Cacheable(value = "setMealCache",key = "'setmeal' + #setmeal.categoryId + '_1'")
    @GetMapping("/list")
    public R<List<Setmeal>> list(Setmeal setmeal){
        return setMealService.listSetMeal(setmeal);
    }

    //根据id查询套餐信息
    @GetMapping("/{id}")
    public R<SetmealDto> getById(@PathVariable Long id){
        log.info("id:{}",id);
        //根据id查询当前套餐信息
        SetmealDto setmealDto = setMealService.getMeal(id);
        return R.success(setmealDto);
    }

    //修改套餐数据
    @CacheEvict(value = "setMealCache", allEntries = true)
    @PutMapping
    public R<String> modifySetMeal(@RequestBody SetmealDto setmealDto){
        return setMealService.modifySetMeal(setmealDto);
    }

    /**
     * 批量或单个修改状态信息
     * @param status
     * @param ids
     * @return
     */
    @CacheEvict(value = "setMealCache", allEntries = true)
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
        return setMealService.dish(setmealId);
    }

}
