package com.mmall.service.impl;

import com.mmall.common.Constant;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.common.TokenCache;
import com.mmall.dao.UserMapper;
import com.mmall.pojo.User;
import com.mmall.service.IUserService;
import com.mmall.utils.Md5Util;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Created by guo on 2018/7/30.
 */
@Service("iUserService")
public class UserServiceImp implements IUserService {
    @Autowired
    private UserMapper userMapper;

    @Override
    public ServerResponse<User> login(String username, String password) {
        int resultCount = userMapper.checkUsername(username);
        if (resultCount == 0) {
            return ServerResponse.createByErrorMessage("当前用户不存在");
        }
        // 密码MD5加密
        String md5Password = Md5Util.MD5EncodeUtf8(password);
        User user = userMapper.selectLogin(username, md5Password);
        if (user == null) {
            return ServerResponse.createByErrorMessage("密码错误");
        }
        //不返回密码
        user.setPassword(StringUtils.EMPTY);
        return ServerResponse.createBySuccess("登录成功", user);

    }

    /**
     * 注册新用户
     *
     * @param user 用户内容的封装
     * @return
     */
    public ServerResponse<String> register(User user) {
        ServerResponse validResponse = this.checkValid(user.getUsername(), Constant.USERNAME);
        if (!validResponse.isSuccess()) {
            return validResponse;
        }
        validResponse = this.checkValid(user.getEmail(), Constant.EMAIL);
        if (!validResponse.isSuccess()) {
            return validResponse;
        }
        //设置用户类型为普通用户
        user.setRole(Constant.Role.ROLE_CUSTOMER);
        //md5加密
        user.setPassword(Md5Util.MD5EncodeUtf8(user.getPassword()));
        int resultCount = userMapper.insert(user);
        if (resultCount == 0) {
            return ServerResponse.createByErrorMessage("注册失败");
        }
        return ServerResponse.createBySuccessMsg("注册成功");
    }

    /**
     * 校验注册的数据是否合法
     *
     * @param value 用户登录账户信息
     * @param type  账户类型
     * @return 用户不存在的时候返回true
     */
    @Override
    public ServerResponse<String> checkValid(String value, String type) {
        if (org.apache.commons.lang.StringUtils.isNotBlank(type)) {
            if (Constant.USERNAME.equals(type)) {
                int resultCount = userMapper.checkUsername(value);
                if (resultCount > 0) {
                    return ServerResponse.createByErrorMessage("用户名已存在");
                }
            }
            if (Constant.EMAIL.equals(type)) {
                int resultCount = userMapper.checkEmail(value);
                if (resultCount > 0) {
                    return ServerResponse.createByErrorMessage("email已存在");
                }
            }
        } else {
            return ServerResponse.createByErrorMessage("参数type类型错误 : ");
        }
        return ServerResponse.createBySuccessMsg("校验成功");
    }

    /**
     * 查找当前用户的找回信息
     *
     * @param username
     * @return
     */
    @Override
    public ServerResponse selectQuestion(String username) {
        ServerResponse validResponse = this.checkValid(username, Constant.USERNAME);
        if (validResponse.isSuccess()) {
            return ServerResponse.createByErrorMessage("用户名不存在，请先注册");
        }
        String question = userMapper.selectQuestionByUserName(username);
        if (StringUtils.isNotBlank(question)) {
            return ServerResponse.createBySuccess(question);
        } else {
            return ServerResponse.createByErrorMessage("找回密码问题不存在");
        }
    }

    /**
     * 忘记密码后，请求通过问题答案修改密码，需要先进行问题验证，先让用户输入问题和答案。验证合格后
     * 用户给你一个UUID的令牌，拿着这个令牌去修改密码。设置缓存是为了防止用户拿到令牌后，不去修改密码。
     * 修改密码功能拿到这个UUID令牌后，和缓存中当前用户的UUID令牌对比，如果一致，可以修改
     * 这里设置缓存主要是为了防止用户连续输入账户的敏感信息。用户输入一次后，为当前用户缓存一个
     * 密码问题验证合格的token，那么下次用户修改密码时，只需要拿着token来校验就可以。不需要
     *
     * @param username
     * @param question
     * @param answer
     * @return
     */
    @Override
    public ServerResponse<String> forgetQuestionCheckAnswer(String username, String question, String answer) {
        int resultCount = userMapper.checkAnswer(username, question, answer);
        //如果resultCount 大于0  当前问答属于该用户
        if (resultCount > 0) {
            String forgetToken = UUID.randomUUID().toString();
            TokenCache.setKey(TokenCache.TOKEN_PREFIX + username, forgetToken);
            return ServerResponse.createBySuccess(forgetToken);
        }
        return ServerResponse.createByErrorMessage("问题的答案错误");
    }

    /**
     * 根据token修改密码
     *
     * @param username    用户米
     * @param newPassword 新密码
     * @param token       token
     * @return
     */
    @Override
    public ServerResponse<String> forgetResetPassword(String username, String newPassword, String token) {
        if (StringUtils.isBlank(token)) {
            return ServerResponse.createByErrorMessage("参数错误，token不存在");
        }
        ServerResponse validResponse = this.checkValid(username, Constant.USERNAME);
        if (validResponse.isSuccess()) {
            return ServerResponse.createByErrorMessage("用户名不存在，请先注册");
        }
        String tokenCache = TokenCache.getKey(TokenCache.TOKEN_PREFIX);
        if (StringUtils.isBlank(tokenCache)) {
            return ServerResponse.createByErrorMessage("token无效，或者过期");
        }
        if (StringUtils.equals(tokenCache, token)) {
            //使用MD5更改新的密码
            String md5Password = Md5Util.MD5EncodeUtf8(newPassword);
            int rowCount = userMapper.updataPasswordByPassword(username, md5Password);
            if (rowCount > 0) {
                ServerResponse.createBySuccessMsg("修改密码成功");
            } else {
                return ServerResponse.createByErrorMessage("token错误，请重新获取重置密码的token");
            }
        }
        return ServerResponse.createByErrorMessage("修改密码失败");
    }

    /**
     * 登录状态下修改密码
     *
     * @param passwordOld 旧密码
     * @param passwordNew 新密码
     * @param user        登录的用户
     *                    防止横向越权，要校验一下这个用户的旧密码，一定要指定是这个用户，通过查询count1，如果不指定id，那么结果就是true--count>0
     */
    @Override
    public ServerResponse<String> resetPassword(String passwordOld, String passwordNew, User user) {
        int resultCount = userMapper.checkPassword(Md5Util.MD5EncodeUtf8(passwordOld), user.getId());
        if (resultCount == 0) {
            ServerResponse.createByErrorMessage("旧密码错误");
        }
        user.setPassword(Md5Util.MD5EncodeUtf8(passwordNew));
        int updateRow = userMapper.updateByPrimaryKeySelective(user);
        if (updateRow > 0) {
            return ServerResponse.createBySuccessMsg("密码更新成功");
        }
        return ServerResponse.createByErrorMessage("密码更新失败");
    }

    /**
     * 更新用户信息
     *
     * @param user
     * @return
     */
    @Override
    public ServerResponse<User> updateInfomation(User user) {
        int resultCount = userMapper.checkEmailByUserId(user.getEmail(), user.getId());
        if (resultCount > 0) {
            return ServerResponse.createByErrorMessage("邮箱已被注册，请更换email");
        }
        User updateUser = new User();
        updateUser.setId(user.getId());
        updateUser.setEmail(user.getEmail());
        updateUser.setPassword(user.getPassword());
        updateUser.setPhone(user.getPhone());
        updateUser.setQuestion(user.getQuestion());
        int updateCount = userMapper.updateByPrimaryKeySelective(updateUser);
        if (updateCount > 0) {
            return ServerResponse.createBySuccess("用户信息更新成功", updateUser);
        }
        return ServerResponse.createByErrorMessage("用户信息更新失败");
    }


    public ServerResponse<User> getInfomation(Integer userId) {
        User user = userMapper.selectByPrimaryKey(userId);
        if (user == null) {
            return ServerResponse.createByErrorMessage("找不到当前用户");
        }
        user.setPassword(StringUtils.EMPTY);
        return null;
    }

}

