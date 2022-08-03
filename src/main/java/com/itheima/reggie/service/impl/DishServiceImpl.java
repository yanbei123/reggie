package com.itheima.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.reggie.common.CustomException;
import com.itheima.reggie.common.R;
import com.itheima.reggie.dto.DishDto;
import com.itheima.reggie.entity.Category;
import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.entity.DishFlavor;
import com.itheima.reggie.entity.SetmealDish;
import com.itheima.reggie.mapper.DishMapper;
import com.itheima.reggie.service.CategoryService;
import com.itheima.reggie.service.DishFlavorService;
import com.itheima.reggie.service.DishService;
import com.itheima.reggie.service.SetMealDishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: 燕北
 * @Date: 2022/06/25/16:27
 * @Description:
 */
@Service
@Slf4j
public class DishServiceImpl extends ServiceImpl<DishMapper, Dish> implements DishService {

    @Autowired
    private DishService dishService;

    @Autowired
    private DishFlavorService dishFlavorService;

    @Autowired
    private SetMealDishService setMealDishService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 分页
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @Override
    public R<Page<DishDto>> pageDish(int page, int pageSize, String name) {
        //构造分页器对象
        Page<Dish> pageInfo = new Page<>(page,pageSize);
        Page<DishDto> dishDtoPage = new Page<>();

        //条件过滤器
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();

        //过滤条件
        queryWrapper.like(name != null,Dish::getName,name);
        queryWrapper.orderByDesc(Dish::getUpdateTime);

        //执行分页查询
        dishService.page(pageInfo,queryWrapper);

        //对象拷贝，忽略records属性
        BeanUtils.copyProperties(pageInfo,dishDtoPage,"records");

        List<Dish> records = pageInfo.getRecords();
        List<DishDto> list = records.stream().map((item) -> {
            DishDto dishDto = new DishDto();

            BeanUtils.copyProperties(item, dishDto);

            Long categoryId = item.getCategoryId();//分类id
            //根据id查询分类对象
            Category category = categoryService.getById(categoryId);
            if (category != null){
                String categoryName = category.getName();
                dishDto.setCategoryName(categoryName);
            }

            return dishDto;
        }).collect(Collectors.toList());

        dishDtoPage.setRecords(list);
        return R.success(dishDtoPage);
    }

    /**
     * 根据菜品分类id进行查询
     * @param dish
     * @return
     */
    @Override
    public R<List<DishDto>> listDish(Dish dish) {
        //条件构造器
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        //查询条件
        queryWrapper.eq(dish.getCategoryId() != null,Dish::getCategoryId,dish.getCategoryId());
        //添加条件，仅添加在启售中的菜品
        queryWrapper.eq(Dish::getStatus,1);
        //查询结果排序
        queryWrapper.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);
        //执行查询
        List<Dish> list = dishService.list(queryWrapper);

        List<DishDto> dishDtoList = list.stream().map((item) -> {
            DishDto dishDto = new DishDto();

            BeanUtils.copyProperties(item, dishDto);

            Long categoryId = item.getCategoryId();//分类id
            //根据id查询分类对象
            Category category = categoryService.getById(categoryId);
            if (category != null){
                String categoryName = category.getName();
                dishDto.setCategoryName(categoryName);
            }

            //当前菜品的id
            Long dishId = item.getId();
            LambdaQueryWrapper<DishFlavor> lambdaQueryWrapper = new LambdaQueryWrapper<>();
            lambdaQueryWrapper.eq(DishFlavor::getDishId,dishId);
            //select * from dish_flavor where dish_id = ?
            List<DishFlavor> dishFlavorList = dishFlavorService.list(lambdaQueryWrapper);
            dishDto.setFlavors(dishFlavorList);
            return dishDto;
        }).collect(Collectors.toList());

        return R.success(dishDtoList);
    }

    /**
     * 新增菜品，同时保存对应的口味数据
     * @param dishDto
     */
    @Transactional
    public void saveWithFlavor(DishDto dishDto) {
        //保存基本的菜品信息到菜品表dish
        this.save(dishDto);
        Long dishId = dishDto.getId();   //菜品id

        //菜品口味
        List<DishFlavor> flavors = dishDto.getFlavors();
        flavors = flavors.stream().map((item) -> {
            item.setDishId(dishId);
            return item;
        }).collect(Collectors.toList());

        //保存菜品口味数据到菜品口味表dish_flavor
        dishFlavorService.saveBatch(flavors);

    }

    /**
     * 根据id查询菜品信息和对应的口味信息
     * @param id
     * @return
     */
    public DishDto getByIdWithFlavor(Long id) {
        //查询菜品的基本信息，从dish表查询
        Dish dish = this.getById(id);

        DishDto dishDto = new DishDto();
        BeanUtils.copyProperties(dish,dishDto);

        //查询当前菜品对应的口味信息
        LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DishFlavor::getDishId,dish.getId());
        List<DishFlavor> flavors = dishFlavorService.list(queryWrapper);
        dishDto.setFlavors(flavors);

        return dishDto;
    }

    /**
     * 更新菜品，同时更新对应的口味数据
     * @param dishDto
     */
    @Transactional
    public void updateWithFlavor(DishDto dishDto) {
        //更新dish表基本信息
        this.updateById(dishDto);
        //清理当前菜品对应的口味数据
        LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DishFlavor::getDishId,dishDto.getId());

        dishFlavorService.remove(queryWrapper);
        //添加当前提交过来的口味数据
        List<DishFlavor> flavors = dishDto.getFlavors();

        flavors = flavors.stream().map((item) -> {
            item.setDishId(dishDto.getId());
            return item;
        }).collect(Collectors.toList());

        dishFlavorService.saveBatch(flavors);
    }

    /**
     * 根据id进行删除(单个/批量)
     * @param ids
     */
    @Override
    @Transactional
    public void deleteByIds(List<Long> ids) {
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(ids != null,Dish::getId,ids);
        List<Dish> list = this.list(queryWrapper);
        for (Dish dish :
                list) {
            //获取id对应的状态
            Integer status = dish.getStatus();
            //状态为0表示不在售卖
            if (status == 0){
                this.removeById(dish.getId());
            }else {
                throw new CustomException("存在售卖中的菜品，删除失败");
            }
        }
    }

    /**
     * 删除菜品，同时删除口味数据
     * 如果存在套餐，则删除失败
     * @param ids
     */
    public Boolean deleteWithFlavour(List<Long> ids) {
        //判断ids是否存在于某一套餐中
        LambdaQueryWrapper<SetmealDish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(ids != null,SetmealDish::getDishId, ids);
        //查询套餐列表
        List<SetmealDish> list = setMealDishService.list(queryWrapper);
        if (list.size() == 0){
            //不存在于套餐中，删除菜品和口味数据
            //删除菜品
            dishService.deleteByIds(ids);
            LambdaQueryWrapper<DishFlavor> flavorLambdaQueryWrapper = new LambdaQueryWrapper<>();
            flavorLambdaQueryWrapper.in(DishFlavor::getDishId,ids);
            //删除口味
            dishFlavorService.remove(flavorLambdaQueryWrapper);
            return true;
        }
        //存在，返回删除失败
        return false;
    }

    /**
     * 根据id进行单个或批量操作(修改菜品状态)
     * @param status
     * @param ids
     */
    @Override
    public void updateMultiply(Integer status,List<Long> ids) {
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        //Dish::getId是否在ids中
        queryWrapper.in(ids != null,Dish::getId,ids);
        List<Dish> list = dishService.list(queryWrapper);
        if (list.size() != 0){
            //存在菜品,根据菜品id更改菜品状态
            for (Dish dish : list) {
                dish.setStatus(status);
                dishService.updateById(dish);
            }
        }else {
            throw new CustomException("未选中任何菜品，更新状态异常");
        }
    }

    /**
     * 修改菜品
     * @param dishDto
     * @return
     */
    @Override
    public R<String> updateDishDto(DishDto dishDto) {
        log.info(dishDto.toString());
        dishService.updateWithFlavor(dishDto);
        //拼接要删除的key
        /*String key = "dish_" + dishDto.getCategoryId() + "_" + dishDto.getStatus();
        redisTemplate.delete(key);*/
        return R.success("修改菜品成功");
    }

    /**
     * 根据id修改状态信息
     * @param
     * @return
     */
    @Override
    public R<String> updateStatus(Integer status, List<Long> ids) {
        log.info("status:{}",status);
        log.info("ids:{}",ids);
        //判断id是否存在
        if (ids != null){
            dishService.updateMultiply(status,ids);
            return R.success("状态信息修改成功");
        }
        return R.error("状态信息修改异常");
    }

    /**
     * 根据id删除菜品
     * @param ids
     * @return
     */
    @Override
    public R<String> deleteDish(List<Long> ids) {
        log.info("ids:{}",ids);
        //判断id是否存在
        if (ids != null){
            Boolean flag = dishService.deleteWithFlavour(ids);
            //Boolean flag = false;
            if (flag){
                return R.success("删除菜品成功");
            }
            return R.error("菜品存在套餐中，删除失败");
        }
        return R.error("删除菜品异常");
    }
}
