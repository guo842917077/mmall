package com.mmall.service;

import com.mmall.common.ServerResponse;
import com.mmall.pojo.User;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpSession;

/**
 * Created by guo on 2018/7/30.
 * 登录接口方法
 */
public interface IUserService {
    ServerResponse<User> login(String username, String password);

    ServerResponse<String> register(User user);

    /**
     * 校验注册信息是否合法
     *
     * @param value 注册用户名
     * @param type  注册用户类型
     * @return
     */
    ServerResponse<String> checkValid(String value, String type);

    /**
     * 选择用户验证问题
     */
    ServerResponse selectQuestion(String username);

    ServerResponse<String> forgetQuestionCheckAnswer(String username, String question, String answer);

    /**
     * 使用token重置新的密码
     *
     * @param username
     * @param newPassword
     * @param tokenString
     * @return
     */
    ServerResponse<String> forgetResetPassword(String username, String newPassword, String tokenString);

    ServerResponse<String> resetPassword(String passwordOld, String passwordNew, User user);

    ServerResponse<User> updateInfomation(User user);

    ServerResponse<User> getInfomation(Integer userId);
}
