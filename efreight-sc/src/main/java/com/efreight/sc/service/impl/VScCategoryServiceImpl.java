package com.efreight.sc.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.efreight.sc.entity.view.VScCategory;
import com.efreight.sc.dao.VScCategoryMapper;
import com.efreight.sc.service.VScCategoryService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * VIEW 服务实现类
 * </p>
 *
 * @author xiaobo
 * @since 2020-03-03
 */
@Service
public class VScCategoryServiceImpl extends ServiceImpl<VScCategoryMapper, VScCategory> implements VScCategoryService {

    @Override
    public List<VScCategory> getListByCategoryName(String categoryName) {
        LambdaQueryWrapper<VScCategory> wrapper = Wrappers.<VScCategory>lambdaQuery();
        wrapper.eq(VScCategory::getCategoryName, categoryName);
        return list(wrapper);
    }
}
