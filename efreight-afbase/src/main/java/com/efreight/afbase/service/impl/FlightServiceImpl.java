package com.efreight.afbase.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.efreight.afbase.dao.FlightMapper;
import com.efreight.afbase.entity.Flight;
import com.efreight.afbase.entity.FlightDetail;
import com.efreight.afbase.entity.FlightMerge;
import com.efreight.afbase.service.FlightService;
import com.efreight.common.security.util.SecurityUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
public class FlightServiceImpl extends ServiceImpl<FlightMapper, Flight> implements FlightService {
    //	private final FlightMapper flightMapper;
    @Override
    public IPage<Flight> getListPage(Page page, Flight bean) {
//		QueryWrapper<Flight> queryWrapper = new QueryWrapper<>();

        return baseMapper.getListPage(page, bean);
    }

    @Override
    public List<Flight> getFlightList(Flight bean) {
        //测试编码
        if (bean.getFlightDate() == null || "".equals(bean.getFlightDate())) {
            bean.setFlightDate(LocalDate.now());
        }
        List<Flight> fList = new ArrayList<>();
        List<FlightMerge> flightMerges = baseMapper.getFlightList(bean);
        System.out.println("flightMerges========:" + flightMerges);
        if (flightMerges != null && flightMerges.size() > 0) {
            for (int i = 0; i < flightMerges.size(); i++) {
                FlightMerge fm = flightMerges.get(i);
                if (fm != null && fm.getPanelLevel() == 0) {
                    Flight f = new Flight();
                    f.setFlightNumber(fm.getFlightNumber());
                    f.setWeekNum(fm.getWeekNum());
                    f.setDepartureStation(fm.getDepartureStation());
                    f.setTransitStation(fm.getTransitStation());
                    f.setArrivalStation(fm.getArrivalStation());
                    f.setAircraftTypeBn(fm.getAircraftTypeBn());
                    f.setAircraftTypePc(fm.getAircraftTypePc());
                    f.setTakeoffTime(fm.getTakeoffTime());
                    f.setArrivalTime(fm.getArrivalTime());
                    f.setId(UUID.randomUUID().toString());
                    fList.add(f);
                }
            }
            if (fList != null && fList.size() > 0) {
                for (int j = 0; j < fList.size(); j++) {
                    String fligntNumber = fList.get(j).getFlightNumber();
                    List<FlightDetail> cbgds = new ArrayList<>();
                    for (int i = 0; i < flightMerges.size(); i++) {
                        FlightMerge cbgm = flightMerges.get(i);
                        if (cbgm != null && cbgm.getPanelLevel() == 1 && cbgm.getFlightNumber().equals(fligntNumber)) {
                            FlightDetail cbgd = new FlightDetail();
                            cbgd.setFlightNumber(cbgm.getFlightNumber());
                            cbgd.setWeekNum(cbgm.getWeekNum());
                            cbgd.setDepartureStation(cbgm.getDepartureStation());
                            cbgd.setTransitStation(cbgm.getTransitStation());
                            cbgd.setArrivalStation(cbgm.getArrivalStation());
                            cbgd.setAircraftTypeBn(cbgm.getAircraftTypeBn());
                            cbgd.setAircraftTypePc(cbgm.getAircraftTypePc());
                            cbgd.setTakeoffTime(cbgm.getTakeoffTime());
                            cbgd.setArrivalTime(cbgm.getArrivalTime());
                            cbgd.setId(UUID.randomUUID().toString());
                            cbgds.add(cbgd);
                        }
                    }
                    fList.get(j).setFlightDetails(cbgds);
                }
            }
        }
        return fList;
    }

    @Override
    public IPage<Flight> getFlightListPage(Page page, Flight bean) {
        if (bean.getFlightDate() == null) {
            bean.setFlightDate(LocalDate.now());
        }
        IPage<Flight> flightPage = baseMapper.getFlightListPage(page, bean);
        if (flightPage != null && flightPage.getSize() > 0) {
            List<Flight> flightList = flightPage.getRecords();
            for (int i = 0; i < flightList.size(); i++) {
                Flight flight = flightList.get(i);
                flight.setId(UUID.randomUUID().toString());
                if (bean.getFlightDate() == null) {
                    flight.setFlightDate(LocalDate.now());
                } else {
                    flight.setFlightDate(bean.getFlightDate());
                }
                List<FlightDetail> fds = baseMapper.getFlightDetailByFlightNumber(flight);
                if (fds != null && fds.size() > 0) {
                    for (int j = 0; j < fds.size(); j++) {
                        fds.get(j).setId(UUID.randomUUID().toString());
                    }
                }
                flightList.get(i).setFlightDetails(fds);
            }
            flightPage.setRecords(flightList);
        }
        return flightPage;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean addFlightAndDetail(Flight bean) throws ParseException {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        bean.setCreateTime(df.parse(df.format(new Date())));
        bean.setCreatorId(SecurityUtils.getUser().getId());
        bean.setCreatorName(SecurityUtils.getUser().getUserCname() + " " + SecurityUtils.getUser().getUserEmail());
        bean.setEditTime(df.parse(df.format(new Date())));
        bean.setEditorId(SecurityUtils.getUser().getId());
        bean.setEditorName(SecurityUtils.getUser().getUserCname() + " " + SecurityUtils.getUser().getUserEmail());
        bean.setFlightNumber(bean.getFlightNumber().toUpperCase());
        bean.setDepartureStation(bean.getDepartureStation().toUpperCase());
        baseMapper.insert(bean);
        List<FlightDetail> flightDetails = bean.getFlightDetails();
        if (flightDetails != null && flightDetails.size() > 0) {
            for (int i = 0; i < flightDetails.size(); i++) {
                FlightDetail fd = flightDetails.get(i);
                fd.setFlightId(bean.getFlightId());
                if (fd.getArrivalTime1() != null && fd.getArrivalTime1() == true) {
                    fd.setArrivalTime(fd.getArrivalTime() + " " + "+1");
                }
                if (fd.getCutoffTime1() != null && fd.getCutoffTime1() == true) {
                    fd.setCutoffTime(fd.getCutoffTime() + " " + "-1");
                }
                fd.setArrivalStation(fd.getArrivalStation().toUpperCase());
                if (fd.getTransitStation() != null && !"".equals(fd.getTransitStation())) {
                    fd.setTransitStation(fd.getTransitStation().toUpperCase());
                }
                baseMapper.insertFlightDatail(fd);
            }
        }
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeFlightAndDetailById(String flightId) {
        baseMapper.deleteById(flightId);
        baseMapper.deleteFlightDetailById(flightId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean doUpdate(Flight bean) throws ParseException {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        bean.setEditTime(df.parse(df.format(new Date())));
        bean.setEditorId(SecurityUtils.getUser().getId());
        bean.setEditorName(SecurityUtils.getUser().getUserCname() + " " + SecurityUtils.getUser().getUserEmail());
        bean.setFlightNumber(bean.getFlightNumber().toUpperCase());
        bean.setDepartureStation(bean.getDepartureStation().toUpperCase());
        UpdateWrapper<Flight> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("flight_id", bean.getFlightId());
        baseMapper.update(bean, updateWrapper);
        baseMapper.deleteFlightDetailById(bean.getFlightId().toString());
        List<FlightDetail> flightDetails = bean.getFlightDetails();
        if (flightDetails != null && flightDetails.size() > 0) {
            for (int i = 0; i < flightDetails.size(); i++) {
                FlightDetail fd = flightDetails.get(i);
                fd.setFlightId(bean.getFlightId());
                if (fd.getArrivalTime1() != null && fd.getArrivalTime1() == true) {
                    fd.setArrivalTime(fd.getArrivalTime() + " " + "+1");
                }
                if (fd.getCutoffTime1() != null && fd.getCutoffTime1() == true) {
                    fd.setCutoffTime(fd.getCutoffTime() + " " + "-1");
                }
                fd.setArrivalStation(fd.getArrivalStation().toUpperCase());
                if (fd.getTransitStation() != null && !"".equals(fd.getTransitStation())) {
                    fd.setTransitStation(fd.getTransitStation().toUpperCase());
                }
                baseMapper.insertFlightDatail(fd);
            }
        }
        return true;
    }

    @Override
    public Integer isHavedFlight(String carrierCode) {
        return baseMapper.isHavedFlight(carrierCode);
    }

    @Override
    public Integer isHavedDepartureStation(String departureStation) {
        return baseMapper.isHavedDepartureStation(departureStation);
    }

    @Override
    public Integer isHavedTransitStation(String transitStation) {
        return baseMapper.isHavedTransitStation(transitStation);
    }

    @Override
    public Integer isHavedArrivalStation(String arrivalStation) {
        return baseMapper.isHavedArrivalStation(arrivalStation);
    }
}
