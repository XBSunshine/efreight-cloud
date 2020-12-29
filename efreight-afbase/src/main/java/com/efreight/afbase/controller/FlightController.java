package com.efreight.afbase.controller;


import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.efreight.afbase.entity.Aircraft;
import com.efreight.afbase.entity.Flight;
import com.efreight.afbase.entity.FlightDetail;
import com.efreight.afbase.service.AircraftService;
import com.efreight.afbase.service.FlightService;
import com.efreight.common.security.util.MessageInfo;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.text.ParseException;
import java.util.List;


@RestController
@AllArgsConstructor
@RequestMapping("/flight")
@Slf4j
public class FlightController {

    private final FlightService service;
    private final AircraftService aircraftService;

    /**
     * 分页查询信息
     *
     * @param page 分页对象
     * @return 分页对象
     */
    @GetMapping("/page")
    public MessageInfo getListPage(Page page, Flight bean) {
        return MessageInfo.ok(service.getListPage(page, bean));
    }


    @GetMapping("/getFlightList")
    public MessageInfo getFlightList(Flight bean) {
        try {
            List<Flight> list = service.getFlightList(bean);
            return MessageInfo.ok(list);
        } catch (Exception e) {
            log.info(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

    /**
     * 分页查询信息
     *
     * @param page 分页对象
     * @return 分页对象
     */
    @GetMapping("/getFlightListPage")
    public MessageInfo getFlightListPage(Page page, Flight bean) {
        return MessageInfo.ok(service.getFlightListPage(page, bean));
    }

    /**
     * 添加
     *
     * @param bean 实体
     * @return success/false
     */
    @PostMapping(value = "/doSave")
    @PreAuthorize("@pms.hasPermission('sys_base_flight_add')")
    public MessageInfo addFlightAndDetail(@Valid @RequestBody Flight bean) throws ParseException {
        //检验生效日期
        Assert.notNull(bean.getBeginDate(), "生产起始日期不能为空");
        Assert.notNull(bean.getEndDate(), "生产结束日期不能为空");

        //校验存在始发港
        Integer rows1 = service.isHavedDepartureStation(bean.getDepartureStation().toUpperCase());
        if (rows1 == 0) {
            return MessageInfo.failed("始发港: " + bean.getDepartureStation() + " 不存在");
        }
        List<FlightDetail> flightDetails = bean.getFlightDetails();
        if (flightDetails != null && flightDetails.size() > 0) {
            for (int i = 0; i < flightDetails.size(); i++) {
                FlightDetail fd = flightDetails.get(i);
                //校验航次不能为空
                if (fd.getWeekNum() == null || "".equals(fd.getWeekNum())) {
                    return MessageInfo.failed("航次不能为空");
                }
                //校验存在中转港
                if (fd.getTransitStation() != null && !"".equals(fd.getTransitStation())) {
                    Integer rows2 = service.isHavedTransitStation(fd.getTransitStation().toUpperCase());
                    if (rows2 == 0) {
                        return MessageInfo.failed("中转港: " + fd.getTransitStation() + " 不存在");
                    }
                }
                //校验目的港不能为空
                if (fd.getArrivalStation() == null || "".equals(fd.getArrivalStation())) {
                    return MessageInfo.failed("目的港不能为空");
                }
                //校验存在目的港
                Integer rows3 = service.isHavedArrivalStation(fd.getArrivalStation().toUpperCase());
                if (rows3 == 0) {
                    return MessageInfo.failed("目的港: " + fd.getArrivalStation() + " 不存在");
                }
                //校验客机/货机不能为空
                if (fd.getAircraftTypePc() == null || "".equals(fd.getAircraftTypePc())) {
                    return MessageInfo.failed("客机/货机不能为空");
                }
                //校验宽体/窄体不能为空
                if (fd.getAircraftTypeBn() == null || "".equals(fd.getAircraftTypeBn())) {
                    return MessageInfo.failed("宽体/窄体不能为空");
                }
                //校验截单时间不能为空
//                if (fd.getCutoffTime() == null || "".equals(fd.getCutoffTime())) {
//                    return MessageInfo.failed("截单时间不能为空");
//                }
                //校验起飞时间不能为空
                if (fd.getTakeoffTime() == null || "".equals(fd.getTakeoffTime())) {
                    return MessageInfo.failed("起飞时间不能为空");
                }
                //校验到达时间不能为空
                if (fd.getArrivalTime() == null || "".equals(fd.getArrivalTime())) {
                    return MessageInfo.failed("到达时间不能为空");
                }
                //检验机型
                if (StrUtil.isNotBlank(fd.getAircraftTypeRemark())) {
                    Aircraft aircraft = aircraftService.getOneByType(fd.getAircraftTypeRemark());
                    if (aircraft == null) {
                        return MessageInfo.failed("机型不存在");
                    } else {
                        if (!("k".equals(fd.getAircraftTypeBn())?"宽体":"窄体").equals(aircraft.getAircraftTypePc())) {
                            return MessageInfo.failed("机型与宽体/窄体不相符");
                        }
                    }
                }
            }
        }
        return MessageInfo.ok(service.addFlightAndDetail(bean));
    }

    /**
     * 删除
     *
     * @param flightId
     * @return
     */
    @DeleteMapping("/{flightId}")
    @PreAuthorize("@pms.hasPermission('sys_base_flight_del')")
    public MessageInfo delete(@PathVariable("flightId") String flightId) {
        try {
            service.removeFlightAndDetailById(flightId);
            return MessageInfo.ok();
        } catch (Exception e) {
            log.info(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

    /**
     * 修改
     *
     * @param bean 实体
     * @return success/false
     */
    @PostMapping(value = "/doUpdate")
    @PreAuthorize("@pms.hasPermission('sys_base_flight_edit')")
    public MessageInfo doUpdate(@Valid @RequestBody Flight bean) throws ParseException {
        //检验生效日期
        Assert.notNull(bean.getBeginDate(), "生产起始日期不能为空");
        Assert.notNull(bean.getEndDate(), "生产结束日期不能为空");

        //校验存在始发港
        Integer rows1 = service.isHavedDepartureStation(bean.getDepartureStation().toUpperCase());
        if (rows1 == 0) {
            return MessageInfo.failed("始发港: " + bean.getDepartureStation() + " 不存在");
        }
        List<FlightDetail> flightDetails = bean.getFlightDetails();
        if (flightDetails != null && flightDetails.size() > 0) {
            for (int i = 0; i < flightDetails.size(); i++) {
                FlightDetail fd = flightDetails.get(i);
                //校验航次不能为空
                if (fd.getWeekNum() == null || "".equals(fd.getWeekNum())) {
                    return MessageInfo.failed("航次不能为空");
                }
                //校验存在中转港
                if (fd.getTransitStation() != null && !"".equals(fd.getTransitStation())) {
                    Integer rows2 = service.isHavedTransitStation(fd.getTransitStation().toUpperCase());
                    if (rows2 == 0) {
                        return MessageInfo.failed("中转港: " + fd.getTransitStation() + " 不存在");
                    }
                }
                //校验目的港不能为空
                if (fd.getArrivalStation() == null || "".equals(fd.getArrivalStation())) {
                    return MessageInfo.failed("目的港不能为空");
                }
                //校验存在目的港
                Integer rows3 = service.isHavedArrivalStation(fd.getArrivalStation().toUpperCase());
                if (rows3 == 0) {
                    return MessageInfo.failed("目的港: " + fd.getArrivalStation() + " 不存在");
                }
                //校验客机/货机不能为空
                if (fd.getAircraftTypePc() == null || "".equals(fd.getAircraftTypePc())) {
                    return MessageInfo.failed("客机/货机不能为空");
                }
                //校验宽体/窄体不能为空
                if (fd.getAircraftTypeBn() == null || "".equals(fd.getAircraftTypeBn())) {
                    return MessageInfo.failed("宽体/窄体不能为空");
                }
                //校验截单时间不能为空
//                if (fd.getCutoffTime() == null || "".equals(fd.getCutoffTime())) {
//                    return MessageInfo.failed("截单时间不能为空");
//                }
                //校验起飞时间不能为空
                if (fd.getTakeoffTime() == null || "".equals(fd.getTakeoffTime())) {
                    return MessageInfo.failed("起飞时间不能为空");
                }
                //校验到达时间不能为空
                if (fd.getArrivalTime() == null || "".equals(fd.getArrivalTime())) {
                    return MessageInfo.failed("到达时间不能为空");
                }
                //检验机型
                if (StrUtil.isNotBlank(fd.getAircraftTypeRemark())) {
                    Aircraft aircraft = aircraftService.getOneByType(fd.getAircraftTypeRemark());
                    if (aircraft == null) {
                        return MessageInfo.failed("机型不存在");
                    } else {
                        if (!("k".equals(fd.getAircraftTypeBn())?"宽体":"窄体").equals(aircraft.getAircraftTypePc())) {
                            return MessageInfo.failed("机型与宽体/窄体不相符");
                        }
                    }
                }
            }
        }
        return MessageInfo.ok(service.doUpdate(bean));
    }
}

