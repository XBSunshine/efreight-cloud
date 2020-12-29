package com.efreight.sc.service;

import java.util.List;

import com.baomidou.mybatisplus.extension.service.IService;
import com.efreight.sc.entity.view.TcCategory;

public interface TcCategoryService extends IService<TcCategory>{
	 List<TcCategory> getListByCategoryName(String categoryName);
}
