package com.efreight.sc.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.efreight.sc.entity.VIoCategory;
import com.efreight.sc.dao.VIoCategoryMapper;
import com.efreight.sc.entity.VIoCategory;
import com.efreight.sc.service.VIoCategoryService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * VIEW 服务实现类
 * </p>
 *
 * @author xiaobo
 * @since 2020-09-18
 */
@Service
public class VIoCategoryServiceImpl extends ServiceImpl<VIoCategoryMapper, VIoCategory> implements VIoCategoryService {

    @Override
    public List<VIoCategory> getList(String categoryName) {
        LambdaQueryWrapper<VIoCategory> vIoCategoryWrapper = Wrappers.<VIoCategory>lambdaQuery();
        vIoCategoryWrapper.eq(VIoCategory::getCategoryName, categoryName);
        return list(vIoCategoryWrapper);
    }
}
