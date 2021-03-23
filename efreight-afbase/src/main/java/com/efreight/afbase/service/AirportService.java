package com.efreight.afbase.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.efreight.afbase.entity.Airport;
import com.efreight.afbase.entity.view.AirportCitySearch;
import com.efreight.afbase.entity.view.AirportCountrySearch;
import com.efreight.afbase.entity.view.AirportSearch;

import java.util.List;


public interface AirportService extends IService<Airport> {
	IPage<Airport> getListPage(Page page, Airport bean);
	
	List<Airport> isHaved(String ap_code,String nation_code);
	List<Airport> checkCode(Airport bean);
	void importData(List<Airport> list);

    Airport getAirportCityNameENByApCode(String apCode);
    Airport getAirport(String cityCode);

    int saveAirport(Airport airport);

    int editAirport(Airport airport);

    List<Airport> listCity();

    Airport viewCity(String cityCode);

    /**
     * 根据关键字进行搜索
     * @param key 关键字 机场代码，中文名，英文名
     * @return
     */
    List<AirportSearch> search(String key);

    /**
     * 根据关键字搜索国家信息
     * @param searchKey 关键字
     * @return
     */
    List<AirportCountrySearch> searchCountry(String searchKey);

    /**
     * 根据关键字搜索城市信息
     * @param searchKey 关系字
     * @return
     */
    List<AirportCitySearch> searchCity(String searchKey);

    List<Airport> queryNationWithNationCodeThreeIsNotNull();

    List<Airport> airportListForApi(String enKey, String cnKey);
}
