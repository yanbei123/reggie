package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.R;
import com.itheima.reggie.dto.DishDto;
import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.service.DishService;
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
 * @Date: 2022/06/27/16:30
 * @Description: 菜品口味
 */
@RestController
@Slf4j
@RequestMapping("/dish")
public class DishController {

    @Autowired
    private DishService dishService;

    /**
     * 新增菜品
     * @param dishDto
     * @return
     */
    @PostMapping
    @CachePut(value = "dishCache",key = "'dish_' + #dishDto.categoryId + '_' + #dishDto.status")
    public R<String> save(@RequestBody DishDto dishDto){
        log.info(dishDto.toString());
        dishService.saveWithFlavor(dishDto);
        return R.success("新增菜品成功");
    }

    /**
     * 分页
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    public R<Page<DishDto>> page(int page,int pageSize,String name){
        return dishService.pageDish(page,pageSize,name);
    }

    /**
     * 根据id查询菜品信息和对应的口味信息
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public R<DishDto> get(@PathVariable Long id){
        DishDto dishDto = dishService.getByIdWithFlavor(id);
        return R.success(dishDto);
    }

    /**
     * 修改菜品  ==> 由于修改菜品时，仅涉及到一个分类id，所以可以直接删除缓存中的当前id信息
     * @param dishDto
     * @return
     */
    @CacheEvict(value = "dishCache",key = "'dish_' + #dishDto.categoryId + '_' + #dishDto.status")
    @PutMapping
    public R<String> update(@RequestBody DishDto dishDto){
        return dishService.updateDishDto(dishDto);
    }

    /**
     * 根据id修改状态信息
     * @param
     * @return
     */
    @CacheEvict(value = "dishCache", allEntries = true)
    @PostMapping("/status/{status}")
    public R<String> updateStatus(@PathVariable Integer status, @RequestParam List<Long> ids){
        return dishService.updateStatus(status,ids);
    }

    /**
     * 根据ids删除菜品
     * @param ids
     * @return
     */
    //@CacheEvict(value = "dishCache",key = "")  //key = "dish_dish.categoryId_dish.status"
    @CacheEvict(value = "dishCache", allEntries = true)
    @DeleteMapping
    public R<String> delete(@RequestParam("ids") List<Long> ids){

        return dishService.deleteDish(ids);
    }

    /**
     * 根据菜品分类id进行查询
     * @param dish
     * @return
     */
    @Cacheable(value = "dishCache",key = "'dish_' + #dish.categoryId + '_' + #dish.status")
    @GetMapping("/list")
    public R<List<DishDto>> list(Dish dish){
        return dishService.listDish(dish);
    }
}
