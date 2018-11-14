package com.mmall.service.impl;

import com.github.pagehelper.StringUtil;
import com.mmall.common.ServerResponse;
import com.mmall.dao.CategoryMapper;
import com.mmall.pojo.Category;
import com.mmall.service.ICategoryService;
import com.sun.tools.javac.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by guo on 2018/11/15.
 */
@Service("iCategoryService")
public class CategoryServiceImp implements ICategoryService {
    @Autowired
    CategoryMapper mCategoryMapper;

    /**
     * 添加分类
     *
     * @param categoryName 分类名称
     * @param parentId     分类ID
     */
    @Override
    public ServerResponse<String> addCategory(String categoryName, Integer parentId) {
        if (StringUtil.isEmpty(categoryName) || parentId == null) {
            return ServerResponse.createByErrorMessage("参数错误");
        }
        Category newCategory = new Category();
        newCategory.setParentId(parentId);
        newCategory.setName(categoryName);
        int row = mCategoryMapper.insert(newCategory);
        if (row > 0) {
            return ServerResponse.createBySuccessMsg("新种类添加成功");
        } else {
            return ServerResponse.createByErrorMessage("种类添加失败");
        }
    }
    /**
     * 替换种类的名称
     *
     * @param categoryName 分类名称
     * @param parentId     分类ID
     */
    @Override
    public ServerResponse<String> updateCategory(String categoryName, Integer parentId) {
        if (StringUtil.isEmpty(categoryName) || parentId == null) {
            return ServerResponse.createByErrorMessage("参数错误");
        }
        Category newCategory = new Category();
        newCategory.setParentId(parentId);
        newCategory.setName(categoryName);
        int row = mCategoryMapper.updateByPrimaryKey(newCategory);
        if (row > 0) {
            return ServerResponse.createBySuccessMsg("种名称设置成功");
        } else {
            return ServerResponse.createByErrorMessage("种名称设置失败");
        }
    }
}
