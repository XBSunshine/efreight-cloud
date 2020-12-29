package com.efreight.sc.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.efreight.sc.dao.TcCategoryMapper;
import com.efreight.sc.entity.view.TcCategory;
import com.efreight.sc.service.TcCategoryService;


@Service
public class TcCategoryServiceImpl extends ServiceImpl<TcCategoryMapper, TcCategory> implements TcCategoryService {

	@Override
	public List<TcCategory> getListByCategoryName(String categoryName) {
		LambdaQueryWrapper<TcCategory> wrapper = Wrappers.<TcCategory>lambdaQuery();
        wrapper.eq(TcCategory::getCategoryName, categoryName);
        return list(wrapper);
	}
	

}
