package com.efreight.afbase.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.efreight.afbase.entity.City;
import com.efreight.afbase.entity.view.AirportCitySearch;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author wangxx
 * @since 2019-07-17
 */
public interface CityService extends IService<City> {
	IPage<City> getListPage(Page page, City bean);

	int saveCity(City city);

	int editCity(City city);

	void removeCityById(String cityId);
	
	void importData(List<City> list);

    List<AirportCitySearch> searchCity(String key);
}
