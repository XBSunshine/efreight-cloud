package com.efreight.afbase.service;

import com.efreight.afbase.entity.Airport;
import com.efreight.afbase.entity.Tact;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.efreight.afbase.entity.exportExcel.TactExcel;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author wangxx
 * @since 2019-07-17
 */
public interface TactService extends IService<Tact> {

	IPage<Tact> getListPage(Page page, Tact bean);

	int deleteTactById(Integer tactId);

	int saveTack(Tact tact);

	int updateTack(Tact tact);
	boolean checkAppid(String appid);

	Tact getTactForBillMake(Tact bean);

	List<TactExcel> queryListForExcel(Tact bean);
}
