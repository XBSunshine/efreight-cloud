package com.efreight.afbase.service;

import com.efreight.afbase.entity.AfOrder;
import com.efreight.afbase.entity.CargoGoodsnames;

import java.util.List;

import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author qipm
 * @since 2020-12-17
 */
public interface CargoGoodsnamesService extends IService<CargoGoodsnames> {

	 List<CargoGoodsnames> querylist(CargoGoodsnames bean);
	 boolean doSave(CargoGoodsnames bean);
	 boolean doUpdate(CargoGoodsnames bean);
	 boolean doDelete(CargoGoodsnames bean);
	 String downloadTemplate();
	 void doImport(List<CargoGoodsnames> data);
}
