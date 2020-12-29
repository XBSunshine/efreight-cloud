package com.efreight.afbase.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.efreight.afbase.entity.Carrier;
import com.baomidou.mybatisplus.extension.service.IService;
import com.efreight.afbase.entity.view.CarrierSearch;

import java.text.ParseException;
import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author wangxx
 * @since 2019-07-17
 */
public interface CarrierService extends IService<Carrier> {

    IPage<Carrier> queryPage(Page page, Carrier carrier);

    Carrier queryOne(Integer id);

    void importData(List<Carrier> list);

    Boolean addCarrier(Carrier bean) throws ParseException;

    Boolean doUpdate(Carrier bean) throws ParseException;

    void removeCarrierById(String carrierId);

    List<Carrier> isHaved(String carrierCode);

    List<Carrier> isHaved1(String carrierPrefix);
	
	Carrier queryOne(String carrierCode);

    List<Carrier> getCarrierList();

    List<CarrierSearch> search(String searchKey);
}
