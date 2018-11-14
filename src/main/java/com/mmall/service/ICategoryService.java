package com.mmall.service;

import com.mmall.common.ServerResponse;
import org.springframework.stereotype.Service;

/**
 * Created by apple on 2018/11/15.
 */

public interface ICategoryService {
    ServerResponse<String> addCategory(String categoryName, Integer parentId);
    // 更新分类名称
    ServerResponse<String> updateCategory(String categoryName, Integer parentId);
}
