package com.efreight.afbase.controller;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.efreight.afbase.entity.Aircraft;
import com.efreight.afbase.service.AircraftService;
import com.efreight.common.security.util.MessageInfo;
import com.efreight.common.security.util.SecurityUtils;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 * AF 基础信息 飞机类型码表 前端控制器
 * </p>
 *
 * @author xiaobo
 * @since 2020-04-23
 */
@RestController
@RequestMapping("/aircraft")
@AllArgsConstructor
@Slf4j
public class AircraftController {

    private final AircraftService aircraftService;

    /**
     * 根据机型获取飞机详情
     *
     * @param aircraftType
     * @return
     */
    @GetMapping("/getOneByType/{aircraftType}")
    public MessageInfo getOneByType(@PathVariable("aircraftType") String aircraftType) {
        try {
            Aircraft aircraft = aircraftService.getOneByType(aircraftType);
            return MessageInfo.ok(aircraft);
        } catch (Exception e) {
            log.info(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

    /**
     * 列表查询
     *
     * @param page
     * @param aircraft
     * @return
     */
    @GetMapping
    public MessageInfo page(Page page, Aircraft aircraft) {
        try {
            IPage result = aircraftService.getPage(page, aircraft);
            return MessageInfo.ok(result);
        } catch (Exception e) {
            log.info(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

    @PostMapping("/add")
	public MessageInfo saveAircraft(@RequestBody Aircraft aircraft){
		try{
			aircraft.setCreatorId(SecurityUtils.getUser().getId());
			aircraft.setCreatorName(SecurityUtils.getUser().getUsername()+" "+SecurityUtils.getUser().getUserEmail());
			aircraft.setCreateTime(LocalDateTime.now());
			aircraftService.save(aircraft);
			return MessageInfo.ok();
		}catch (Exception e){
			return MessageInfo.failed(e.getMessage());
		}
	}
    @PostMapping("/edit")
   	public MessageInfo editAircraft(@RequestBody Aircraft aircraft){
   		try{
   			aircraft.setEditorId(SecurityUtils.getUser().getId());
   			aircraft.setEditorName(SecurityUtils.getUser().getUsername()+" "+SecurityUtils.getUser().getUserEmail());
   			aircraft.setEditTime(LocalDateTime.now());
   			aircraftService.updateById(aircraft);
   			return MessageInfo.ok();
   		}catch (Exception e){
   			return MessageInfo.failed(e.getMessage());
   		}
   	}
    
	@GetMapping("/getAircraft")
	public MessageInfo getAircraft(String aircraftId){
		try{
			Aircraft aircraft = aircraftService.getById(aircraftId);
			return MessageInfo.ok(aircraft);
		}catch (Exception e){
			return MessageInfo.failed(e.getMessage());
		}
	}
	
    
}

