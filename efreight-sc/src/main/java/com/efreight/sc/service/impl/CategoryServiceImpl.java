package com.efreight.sc.service.impl;

import com.efreight.sc.entity.Category;
import com.efreight.sc.dao.CategoryMapper;
import com.efreight.sc.service.CategoryService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * CS 基础信息 参数列表 服务实现类
 * </p>
 *
 * @author xiaobo
 * @since 2020-03-03
 */
@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {

}
