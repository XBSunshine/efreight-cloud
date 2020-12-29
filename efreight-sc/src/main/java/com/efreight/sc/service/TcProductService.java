package com.efreight.sc.service;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.efreight.sc.entity.TcProduct;

public interface TcProductService extends IService<TcProduct>{
	
	IPage gePageList(Page page, TcProduct bean);
	void saveProduct(TcProduct bean);
	void modifyProduct(TcProduct bean);
	void deleteById(Integer productId);
	
	TcProduct view(Integer productId);
	

}
