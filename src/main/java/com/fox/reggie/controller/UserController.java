package com.fox.reggie.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fox.reggie.common.R;
import com.fox.reggie.entity.User;
import com.fox.reggie.service.UserService;
import com.fox.reggie.untils.SMSUtils;
import com.fox.reggie.untils.ValidateCodeUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Map;

@RequestMapping("user")
@Slf4j
@RestController
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 用户登录
     * 1、过滤器更改 完成
     * 2、验证码传输
     * 3、验证码验证、页面跳转
     *
     */
    /**
     * 2、验证码传输
     * 2.1、分析前端页面，响应/user/sendMsg请求
     * 2.2、通过user获取手机号
     * 2.3、生成随机的4位验证码/6位验证吗
     * 2.4、调用阿里云提供的短信服务API完成发送短信
     * 2.5、需要将生成的验证码保存到Session
     */
    @PostMapping("sendMsg")
    public R<String> sendMsg(@RequestBody User user, HttpSession session){
        //获取手机号
        String phone = user.getPhone();
        //如果手机号不为空
        if (StringUtils.isNotEmpty(phone)){
            //生成随机的4位验证码/6位验证吗
            String code = ValidateCodeUtils.generateValidateCode4String(4).toString();
            log.info("code,{}",code);
            //调用阿里云提供的短信服务API完成发送短信
            //收费，最后所有功能结束时再使用，或者使用邮箱验证
            SMSUtils.sendMessage("瑞吉外卖","",phone,code);

            //需要将生成的验证码保存到Session
            session.setAttribute(phone,code);
            return R.success(code);
        }
        return R.error("短信生成失败");
    }
    /**
     * 3、验证码验证、页面跳转
     * 3.1、获取前端传递的手机号和验证码
     * 3.2、从Session中获取到手机号对应的正确的验证码
     * 3.3、进行验证码的比对 , 如果比对失败, 直接返回错误信息
     * 3.4、如果比对成功, 需要根据手机号查询当前用户, 如果用户不存在, 则自动注册一个新用户
     * 3.5、将登录用户的ID存储Session中
     */
    @PostMapping("login")
    public R<User> login(@RequestBody Map map, HttpSession session){
        log.info(map.toString());
//        3.1、获取前端传递的手机号和验证码
        String phone = map.get("phone").toString();
        String code = map.get("code").toString();
//        3.2、从Session中获取到手机号对应的正确的验证码
        Object codeInSession = session.getAttribute(phone);
//        3.3、进行验证码的比对 , 如果比对失败, 直接返回错误信息
        if(codeInSession != null && codeInSession.equals(code)) {
//        3.4、如果比对成功, 需要根据手机号查询当前用户, 如果用户不存在, 则自动注册一个新用户
            LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(User::getPhone,phone);
            User user = userService.getOne(queryWrapper);
            if(user == null) {
                user = new User();
                user.setPhone(phone);
                user.setStatus(1);
                userService.save(user);
            }

//        3.5、将登录用户的ID存储Session中
            session.setAttribute("user",user.getId());
            return R.success(user);
        }
        return R.error("登录失败");
    }
    /**
     * 退出登录
     */
    @PostMapping("loginout")
    public R<String> logout(HttpServletRequest request){
        //1、清理Session中保存的当前登录员工的id
        request.getSession().removeAttribute("user");
        return R.success("退出成功");
    }
}
