package com.itheima.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.Employee;
import com.itheima.reggie.mapper.EmployeeMapper;
import com.itheima.reggie.service.EmployeeService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import javax.servlet.http.HttpServletRequest;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: 燕北
 * @Date: 2022/06/23/16:08
 * @Description: 员工管理
 */
@Service
@Slf4j
public class EmployeeServiceImpl extends ServiceImpl<EmployeeMapper, Employee> implements EmployeeService {

    @Autowired
    private EmployeeService employeeService;

    /**
     * 登录功能
     * @param request
     * @param employee
     * @return
     */
    @Override
    public R<Employee> login(HttpServletRequest request, Employee employee) {
        //1.将密码进行md5加密
        String password = employee.getPassword();
        password = DigestUtils.md5DigestAsHex(password.getBytes());

        //2.对username进行数据库查询
        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Employee::getUsername,employee.getUsername());
        Employee emp = employeeService.getOne(queryWrapper);

        //3.没有查询到直接返回，给出错误信息
        if (emp == null){
            return R.error("登录失败,该用户不存在");
        }

        //4.判断密码是否正确
        if (!emp.getPassword().equals(password)){
            return R.error("登录失败,密码错误");
        }

        //5.判断用户是否禁用
        if (emp.getStatus() == 0){
            return R.error("登录失败,账户已被禁用");
        }

        //6.登录成功,保存id进session并返回
        request.getSession().setAttribute("employee",emp.getId());
        return R.success(emp);
    }

    /**
     * 退出登录功能
     * @param request
     * @return
     */
    @Override
    public R<String> logout(HttpServletRequest request) {
        request.getSession().removeAttribute("employee");
        return R.success("退出成功");
    }

    /**
     * 保存功能，新增员工
     * @param employee
     * @return
     */
    @Override
    public R<String> saveEmp(Employee employee) {
        log.info("新增员工信息:{}",employee.toString());
        //设置初始密码123456，进行md5加密
        employee.setPassword(DigestUtils.md5DigestAsHex("123456".getBytes()));
        //页面中没有自己填写密码的部分，若存在可以通过传过来的get方法拿到数据
        //employee.setPassword(DigestUtils.md5DigestAsHex(employee.getPassword().getBytes()));

        boolean save = employeeService.save(employee);
        if (save){
            return R.success("新增员工成功");
        }
        return R.error("新增员工失败");
    }

    /**
     * 分页功能
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @Override
    public R<Page<Employee>> page(int page, int pageSize, String name) {
        //构造分页构造器
        Page<Employee> pageInfo = new Page<>(page,pageSize);

        //添加过滤条件
        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(StringUtils.isNotEmpty(name),Employee::getName,name);
        queryWrapper.orderByDesc(Employee::getUpdateTime);

        employeeService.page(pageInfo,queryWrapper);
        return R.success(pageInfo);
    }

    /**
     * 修改用户信息
     * @param employee
     * @return
     */
    @Override
    public R<String> updateEmp(Employee employee) {
        log.info(employee.toString());

        boolean res = employeeService.updateById(employee);
        if (res){
            return R.success("员工信息修改成功");
        }
        return R.success("员工信息修改失败");
    }

    /**
     * 根据id查询用户信息
     * @param id
     * @return
     */
    @Override
    public R<Employee> getByEmpId(Long id) {
        //这里应该是对前段数据进行了修改，项目中很多id参数均为ids，应该是后期便于扩充至批量操作
        log.info("根据id查询员工信息...");
        Employee employee = employeeService.getById(id);
        if (employee != null){
            return R.success(employee);
        }
        return R.error("没有查询到相应用户信息");
    }
}
