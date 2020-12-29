package com.efreight.afbase.controller;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.efreight.afbase.entity.Airport;
import com.efreight.afbase.entity.view.AirportCitySearch;
import com.efreight.afbase.entity.view.AirportCountrySearch;
import com.efreight.afbase.entity.view.AirportSearch;
import com.efreight.afbase.service.AirportService;
import com.efreight.common.security.util.MessageInfo;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @author wangxx
 * @since 2019-07-17
 */
@RestController
@AllArgsConstructor
@RequestMapping("/airport")
@Slf4j
public class AirportController {
    private final AirportService service;

    /**
     * 分页查询信息
     *
     * @param page 分页对象
     * @return 分页对象
     */
    @GetMapping("/page")
    public MessageInfo getListPage(Page page, Airport bean) {
        return MessageInfo.ok(service.getListPage(page, bean));
    }

    /**
     * 通过三字码查找城市英文名称
     *
     * @param apCode
     * @return
     */
    @GetMapping("/getAirportName")
    public MessageInfo getAirportCityNameENByApCode(String apCode) {
        try {
            Airport airport = service.getAirportCityNameENByApCode(apCode);
            return MessageInfo.ok(airport);
        } catch (Exception e) {
            return MessageInfo.failed(e.getMessage());
        }
    }

    @GetMapping("/getAirport")
    public MessageInfo getAirport(String cityCode) {
        try {
            Airport airport = service.getAirport(cityCode);
            return MessageInfo.ok(airport);
        } catch (Exception e) {
            return MessageInfo.failed(e.getMessage());
        }
    }

    @PostMapping("/doSave")
    @PreAuthorize("@pms.hasPermission('sys_base_airport_add')")
    public MessageInfo saveAirport(@RequestBody Airport airport) {
        try {
            int result = service.saveAirport(airport);
            return MessageInfo.ok(result);
        } catch (Exception e) {
            return MessageInfo.failed(e.getMessage());
        }
    }

    @PostMapping("/checkCode")
    public MessageInfo checkCode(@RequestBody Airport airport) {
//		try{
        return MessageInfo.ok(service.checkCode(airport));
//		}catch (Exception e){
//			log.info(e.getMessage());
//			return MessageInfo.failed(e.getMessage());
//		}
    }

    @PostMapping("/doUpdate")
    @PreAuthorize("@pms.hasPermission('sys_base_airport_edit')")
    public MessageInfo editAirport(@RequestBody Airport airport) {
        try {
            int result = service.editAirport(airport);
            return MessageInfo.ok(result);
        } catch (Exception e) {
            return MessageInfo.failed(e.getMessage());
        }
    }

    /**
     * 获取城市列表（'CN'）
     *
     * @return
     */
    @GetMapping("/city")
    public MessageInfo listCity() {
        try {
            List<Airport> list = service.listCity();
            return MessageInfo.ok(list);
        } catch (Exception e) {
            log.error(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

    @GetMapping("search/{key}")
    public MessageInfo search(@PathVariable("key") String key){
        List<AirportSearch> searchResults = service.search(key);
        return MessageInfo.ok(searchResults);
    }

    /**
     * 根据城市代码获取城市（'CN'）
     *
     * @return
     */
    @GetMapping("/city/{cityCode}")
    public MessageInfo<Airport> viewCity(@PathVariable("cityCode") String cityCode) {
        try {
            Airport airport = service.viewCity(cityCode);
            return MessageInfo.ok(airport);
        } catch (Exception e) {
            log.error(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

    @GetMapping("searchCountry/{key}")
    public MessageInfo searchCountry(@PathVariable("key") String key){
        List<AirportCountrySearch> searchResults = service.searchCountry(key);
        return MessageInfo.ok(searchResults);
    }

    /**
     * 货源地
     * @param key
     * @return
     */
    @GetMapping("searchCity/{key}")
    public MessageInfo searchCity(@PathVariable("key") String key){
        List<AirportCitySearch> searchResults = service.searchCity(key);
        return MessageInfo.ok(searchResults);
    }

    /**
     * 查询国家（返回国家名称和国家三字码）
     * @return
     */
    @GetMapping("/queryNationWithNationCodeThreeIsNotNull")
    public MessageInfo queryNationWithNationCodeThreeIsNotNull(){
        try {
          List<Airport> list =  service.queryNationWithNationCodeThreeIsNotNull();
          return MessageInfo.ok(list);
        }catch (Exception e){
            log.error(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

}

