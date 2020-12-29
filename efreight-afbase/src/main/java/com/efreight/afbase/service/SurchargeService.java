package com.efreight.afbase.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.efreight.afbase.entity.Airport;
import com.efreight.afbase.entity.Nation;
import com.efreight.afbase.entity.Surcharge;
import com.efreight.afbase.entity.Tact;
import com.efreight.afbase.entity.exportExcel.SurchargeExcel;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author wangxx
 * @since 2019-07-17
 */
public interface SurchargeService extends IService<Surcharge> {

	IPage<Surcharge> getListPage(Page page, Surcharge bean);

	List<Airport> getDepartureStationList();

	List<Nation> getNationCodesList();

	int saveSurcharge(Surcharge surcharge);

	int deleteSurchargeById(Integer surchargeId);

	int updateSurcharge(Surcharge surcharge);

	List<Surcharge> getSurchargeForBillMake(Surcharge surcharge);

	List<SurchargeExcel> queryListForExcel(Surcharge bean);

}
