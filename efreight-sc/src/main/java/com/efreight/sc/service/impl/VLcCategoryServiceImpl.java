package com.efreight.sc.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.efreight.sc.entity.VLcCategory;
import com.efreight.sc.dao.VLcCategoryMapper;
import com.efreight.sc.service.VLcCategoryService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * VIEW 服务实现类
 * </p>
 *
 * @author xiaobo
 * @since 2020-07-22
 */
@Service
public class VLcCategoryServiceImpl extends ServiceImpl<VLcCategoryMapper, VLcCategory> implements VLcCategoryService {

    @Override
    public List<VLcCategory> getList(String categoryName) {
        LambdaQueryWrapper<VLcCategory> vLcCategoryWrapper = Wrappers.<VLcCategory>lambdaQuery();
        vLcCategoryWrapper.eq(VLcCategory::getCategoryName, categoryName);
        return list(vLcCategoryWrapper);
    }
}
