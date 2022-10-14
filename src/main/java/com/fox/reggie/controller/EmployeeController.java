package com.fox.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fox.reggie.common.R;
import com.fox.reggie.entity.Employee;
import com.fox.reggie.service.EmployeeService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@Slf4j
@RestController
@RequestMapping("employee")
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;

    /**
     * 员工登录
     * @param request
     * @param employee
     * @return
     * 1、将页面提交的密码password进行md5加密处理
     * 2、根据页面提交的用户名username查询数据库
     * 3、如果没有查询到则返回登录失败结果
     * 4、密码比对，如果不一致则返回登录失败结果
     * 5、查看员工状态，如果为已禁用状态，则返回员工已禁用结果
     * 6、登录成功，将员工id存入Session并返回登录成功结果
     */
    @PostMapping("login")
    public R<Employee> login(HttpServletRequest request, @RequestBody Employee employee){

        //1、将页面提交的密码password进行md5加密处理
        String password = employee.getPassword();
        password = DigestUtils.md5DigestAsHex(password.getBytes());
        //2、根据页面提交的用户名username查询数据库
        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Employee::getUsername,employee.getUsername());
        Employee emp = employeeService.getOne(queryWrapper);
        //3、如果没有查询到则返回登录失败结果
        if (emp==null){
            return R.error("未找到用户信息");
        }
        //4、密码比对，如果不一致则返回登录失败结果
        if (!emp.getPassword().equals(password)){
            return R.error("用户名或密码错误");
        }
        //5、查看员工状态，如果为已禁用状态，则返回员工已禁用结果
        if (emp.getStatus().equals(0)){
            return R.error("该账户被封禁");
        }

        //6、登录成功，将员工id存入Session并返回登录成功结果
        request.getSession().setAttribute("employee",emp.getId());
        return R.success(emp);
    }

    /**
     * 员工退出登录
     * @param request
     * @return
     * 1、清理Session中保存的当前登录员工的id
     */
    @PostMapping("/logout")
    public R<String> logout(HttpServletRequest request){
        //1、清理Session中保存的当前登录员工的id
        request.getSession().removeAttribute("employee");
        return R.success("退出成功");
    }

    /**
     * 新增员工
     * @param request
     * @param employee
     * @return
     */
    @PostMapping
    public R<String> save(HttpServletRequest request, @RequestBody Employee employee){
        log.info("新增员工，员工信息：{}",employee.toString());
        //初始密码123456,进行md5加密处理
        employee.setPassword(DigestUtils.md5DigestAsHex("123456".getBytes()));
//        //补充其他信息
//        employee.setCreateTime(LocalDateTime.now());
//        employee.setUpdateTime(LocalDateTime.now());
//        Long empId =  (Long) request.getSession().getAttribute("employee");
//
//        employee.setCreateUser(empId);
//        employee.setUpdateUser(empId);

        employeeService.save(employee);

        return R.success("新增员工成功");
    }

    /**
     * 分页查询员工
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    /**
     1、构造分页构造器
     2、构造条件构造器
     3、执行查询
     */
    @GetMapping("page")
    public R<Page> page(int page, int pageSize, String name){
        log.info("page={},pageSize={},name={}",page,pageSize,name);
        //1、构造分页构造器
        Page pageInfo = new Page(page,pageSize);
        //2、构造条件构造器
        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper();
        //2.1添加过滤条件 模糊查询
        queryWrapper.like(StringUtils.isNotEmpty(name),Employee::getUsername,name);
        //2.2添加排序条件 以更新时间为序
        queryWrapper.orderByDesc(Employee::getUpdateTime);
        //3、执行查询
        employeeService.page(pageInfo,queryWrapper);
        return R.success(pageInfo);
    }

    /**
     * 员工状态更改
     * @param request
     * @param employee
     * @return
     */
    /**
     * 禁用流程
     1、得到当前登录用户id
     2、填充更新信息
     */
    @PutMapping
    public R<String> update(HttpServletRequest request,@RequestBody Employee employee){
        log.info("employee:{}",employee);

        long id = Thread.currentThread().getId();
        log.info("线程id为：{}",id);
//        //1、得到当前登录用户id
//        Long empId = (Long)request.getSession().getAttribute("employee");
////        2、填充更新信息
//        employee.setUpdateUser(empId);
//        employee.setUpdateTime(LocalDateTime.now());
        employeeService.updateById(employee);
        return R.success("员工修改信息成功");
    }
    @GetMapping("{id}")
    public R<Employee> getById(@PathVariable Long id){
        log.info("根据id查询员工信息...");
        Employee employee = employeeService.getById(id);
        if (employee == null){
            return R.error("用户信息不存在");
        }
        return R.success(employee);
    }
}
