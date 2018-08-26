package com.mmall.control.backend;

import com.mmall.common.Constant;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.User;
import com.mmall.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpSession;

/**
 * Created by guo on 2018/8/6.
 */
@Controller
@RequestMapping("/manager/user")
public class UserManagerController {
    @Autowired
    IUserService iUserService;

    /**
     * 后台管理员登录
     *
     * @param username 用户名
     * @param password 用户密码
     * @param session  session
     * @return
     */
    @RequestMapping(value = "login.do", method = RequestMethod.POST)
    public ServerResponse<User> login(String username, String password, HttpSession session) {
        ServerResponse<User> response = iUserService.login(username, password);
        if (response.isSuccess()) {
            User user = response.getData();
            if (user.getRole() == Constant.Role.ROLE_ADMIN) {
                session.setAttribute(Constant.CURRENT_USER, user);
                return response;
            } else {
                return ServerResponse.createByErrorMessage("当前用户不是管理员，无法登录");
            }
        }
        return response;
    }
}
