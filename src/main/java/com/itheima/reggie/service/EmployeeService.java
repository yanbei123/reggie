package com.itheima.reggie.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.Employee;

import javax.servlet.http.HttpServletRequest;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: 燕北
 * @Date: 2022/06/23/16:07
 * @Description: 员工管理
 */
public interface EmployeeService extends IService<Employee> {

    //登录功能，返回提示的msg
    public R<Employee> login(HttpServletRequest request, Employee employee);

    //退出登录功能
    public R<String> logout(HttpServletRequest request);

    //保存功能，新增员工
    public R<String> saveEmp(Employee employee);

    //分页功能
    public R<Page<Employee>> page(int page, int pageSize, String name);

    //修改用户信息
    public R<String> updateEmp(Employee employee);

    //根据id查询用户信息
    public R<Employee> getByEmpId(Long id);
}
