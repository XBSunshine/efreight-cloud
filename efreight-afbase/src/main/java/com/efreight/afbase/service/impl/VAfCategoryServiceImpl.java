package com.efreight.afbase.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.efreight.afbase.entity.view.VAfCategory;
import com.efreight.afbase.dao.VAfCategoryMapper;
import com.efreight.afbase.service.VAfCategoryService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * VIEW 服务实现类
 * </p>
 *
 * @author xiaobo
 * @since 2020-02-28
 */
@Service
public class VAfCategoryServiceImpl extends ServiceImpl<VAfCategoryMapper, VAfCategory> implements VAfCategoryService {

    @Override
    public List<VAfCategory> getList(String categoryName) {
        LambdaQueryWrapper<VAfCategory> wrapper = Wrappers.<VAfCategory>lambdaQuery();
        wrapper.eq(VAfCategory::getCategoryName, categoryName);
        return list(wrapper);
    }
    @Override
    public List<VAfCategory> getscList(String categoryName) {
    	
    	return baseMapper.getscList(categoryName);
    }
	@Override
	public List<Map> invoiceType() {
		return baseMapper.invoiceType();
	}
}
