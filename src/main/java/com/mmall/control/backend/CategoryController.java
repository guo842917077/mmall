package com.mmall.control.backend;

import com.mmall.common.Constant;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.User;
import com.mmall.service.ICategoryService;
import com.mmall.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;

/**
 * Created by apple on 2018/11/14.
 * 分类管理
 */
@Controller
@RequestMapping("/manage/category")
public class CategoryController {
    @Autowired
    IUserService mUserService;

    @Autowired
    ICategoryService mCategoryService;

    @RequestMapping(value = "addCategory.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse addCategory(HttpSession session, String categoryName, @RequestParam(value = "parentId", defaultValue = "0") int parentId) {
        User user = (User) session.getAttribute(Constant.CURRENT_USER);
        if (user != null) {
            ServerResponse response = mUserService.checkIsAdmin(user);
            if (response.isSuccess()) {
                // 添加分类操作
                return mCategoryService.addCategory(categoryName, parentId);
            } else {
                return response;
            }
        } else {
            return ServerResponse.createByErrorMessage("当前用户未登录");
        }
    }

    @RequestMapping(value = "set_category_name.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse setCategoryName(HttpSession session, String categoryName, int parentId) {
        // 检查用户是否存在 权限是否合格
        User user = (User) session.getAttribute(Constant.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createByErrorMessage("当前用户未登录");
        }
        ServerResponse response = mUserService.checkIsAdmin(user);
        if (!response.isSuccess()) {
            //提示用户没有权限
            return response;
        }
        return mCategoryService.updateCategory(categoryName, parentId);
    }
}
