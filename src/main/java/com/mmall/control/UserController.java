package com.mmall.control;

import com.mmall.common.Constant;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.User;
import com.mmall.service.IUserService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ValueConstants;

import javax.servlet.http.HttpSession;

/**
 * Created by guo on 2018/7/30.
 * 登录接口
 * 1.在类上使用RequestMapping注解可以让整个类下的方法都在这个作用域下
 * 2.开发过程是先开发control，再开发service
 * <p>
 * <p>
 * Result Maps collection already contains value for :Mybatis启动的时候保存，检查Mapper.xml是否id 重复
 */
@Controller
@RequestMapping("/user/")
public class UserController {
    @Autowired
    private IUserService iUserService;

    /**
     * 用户登录
     * 使用RequestMapping指定访问的路径
     * 使用ResponseBody直接，自动将放回值序列化成 在dispatcher-servlet中声明的序列化类型
     *
     * @param username 用户名
     * @param password 密码
     * @param email    地址
     * @return
     */
    @RequestMapping(value = "login.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<User> login(String username, String password, String email, HttpSession session) {
        //service---mybatis----dao
        ServerResponse<User> response = iUserService.login(username, password);
        if (response.isSuccess()) {
            session.setAttribute(Constant.CURRENT_USER, response.getData());
        }
        return response;
    }

    /**
     * 等出接口，从session中移除登录信息
     *
     * @param session
     * @return
     */
    @RequestMapping(value = "logout.do", method =RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> logout(HttpSession session) {
        if (session.getAttribute(Constant.CURRENT_USER) != null) {
            session.removeAttribute(Constant.CURRENT_USER);
        }
        return ServerResponse.createBySuccess();
    }

    /**
     * 注册方法
     *
     * @param user 注册对象
     * @return
     */
    @RequestMapping(value = "register.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> register(User user) {
        return iUserService.register(user);
    }

    /**
     * 校验用户类是否合法
     *
     * @param value
     * @param type  根据type校验使用哪个sql语句校验
     * @return
     */
    @RequestMapping(value = "checkValid.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> checkValid(String value, String type) {
        return iUserService.checkValid(value, type);
    }

    /**
     * 获取当前用户的登录信息
     *
     * @param session
     * @return
     */
    @RequestMapping(value = "getUserInfo.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<User> getUserInfo(HttpSession session) {
        User user = (User) session.getAttribute(Constant.CURRENT_USER);
        if (user != null) {
            return ServerResponse.createBySuccess(user);
        }
        return ServerResponse.createByErrorMessage("用户未登录，无法获取当前的用户信息");
    }

    /**
     * 整个修改密码业务的流程是forget_get_question--forget_check_answer.do-----forget_reset_password.do
     * 当用户忘记密码时，首先查询当前用户的问题
     * @param username
     * @return
     */
    @RequestMapping(value = "forget_get_question.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> forGetQuestion(String username) {
        //todo forget question function
        return iUserService.selectQuestion(username);
    }

    /**
     * 当用户申请通过问题修改密码时，校验用户的问题是否合理
     * 并且获取token
     *
     * @param username 用户名
     * @param question 问题
     * @param answer   答案
     * @return
     */
    @RequestMapping(value = "forget_check_answer.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> forgetQuestionCheckAnswer(String username, String question, String answer) {
        return iUserService.forgetQuestionCheckAnswer(username, question, answer);
    }

    /**
     * 根据token修改密码
     *
     * @param username    用户名
     * @param newPassword 新密码
     * @param tokenString token值
     * @return
     */
    @RequestMapping(value = "forget_reset_password.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> forgetResetPassword(String username, String newPassword, String tokenString) {
        return iUserService.forgetResetPassword(username, newPassword, tokenString);
    }

    /**
     * 登录状态重置密码
     */
    @RequestMapping(value = "reset_password.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> resetPassword(HttpSession session, String passwordOld, String passwordNew) {
        User user = (User) session.getAttribute(Constant.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createByErrorMessage("用户未登录");
        }
        return iUserService.resetPassword(passwordOld, passwordNew, user);
    }

    /**
     * 更新用户信息
     *
     * @param session
     * @param user    为防止越权问题，首先判断用户是否登录，并且把用户的id设置成当前登录的id。（防止有人使用一个普通账号，调用这个接口更新更高级的信息）
     */
    @RequestMapping(value = "update_information.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<User> updateInfomation(HttpSession session, User user) {
        User currentUser = (User) session.getAttribute(Constant.CURRENT_USER);
        if (currentUser == null) {
            return ServerResponse.createByErrorMessage("用户未登录");
        }
        user.setId(currentUser.getId());
        user.setUsername(currentUser.getUsername());
        ServerResponse<User> response = iUserService.updateInfomation(user);
        if (response.isSuccess()) {
            response.getData().setUsername(currentUser.getUsername());
            session.setAttribute(Constant.CURRENT_USER, response.getData());
        }
        return response;
    }

    /**
     * 返回前台用户的详细信息
     *
     * @param session 当前登录用户
     * @return
     */
    @RequestMapping(value = "get_infomation.do", method = RequestMethod.GET)
    @ResponseBody
    public ServerResponse<User> getInfomation(HttpSession session) {
        User currentUser = (User) session.getAttribute(Constant.CURRENT_USER);
        if (currentUser == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "未登录，需要强制登录status=10");
        }
        return iUserService.getInfomation(currentUser.getId());
    }
}
