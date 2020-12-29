package com.efreight.afbase.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.efreight.afbase.dao.SurchargeMapper;
import com.efreight.afbase.dao.TactMapper;
import com.efreight.afbase.entity.*;
import com.efreight.afbase.entity.exportExcel.SurchargeExcel;
import com.efreight.afbase.service.AirportService;
import com.efreight.afbase.service.CarrierService;
import com.efreight.afbase.service.SurchargeService;
import com.efreight.afbase.service.TactService;
import com.efreight.common.security.service.EUserDetails;
import com.efreight.common.security.util.SecurityUtils;
import lombok.AllArgsConstructor;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author wangxx
 * @since 2019-07-17
 */
@Service
@AllArgsConstructor
public class SurchargeServiceImpl extends ServiceImpl<SurchargeMapper, Surcharge> implements SurchargeService {

    private final SurchargeMapper surchargeMapper;

    @Override
    public IPage<Surcharge> getListPage(Page page, Surcharge bean) {
        bean.setOrgId(SecurityUtils.getUser().getOrgId());
        IPage<Surcharge> pa = baseMapper.getList(page, bean);
        return pa;
    }

    @Override
    public List<Airport> getDepartureStationList() {
        List<Airport> list = baseMapper.getDepartureStationList();
        list.stream().forEach(airport -> {
            airport.setApNameCn(airport.getApCode()+","+airport.getApNameCn());
        });
        return list;
    }

    @Override
    public List<Nation> getNationCodesList() {
        List<Nation> list = baseMapper.getNationCodesList();
        list.stream().forEach(nation -> {
            nation.setNationName(nation.getNationCode()+","+nation.getNationName());
        });
        return list;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int saveSurcharge(Surcharge surcharge) {

        String departureStation="";
        if (surcharge.getDepartureStations()!=null) {
            for (int i = 0; i < surcharge.getDepartureStations().size(); i++) {
                if (departureStation.length()==0) {
                    departureStation=""+surcharge.getDepartureStations().get(i);
                } else {
                    departureStation=departureStation+","+surcharge.getDepartureStations().get(i);
                }
            }
        }
        surcharge.setDepartureStation(departureStation);

        String arrivalStation="";
        if (surcharge.getArrivalStations()!=null) {
            for (int i = 0; i < surcharge.getArrivalStations().size(); i++) {
                if (arrivalStation.length()==0) {
                    arrivalStation=""+surcharge.getArrivalStations().get(i);
                } else {
                    arrivalStation=arrivalStation+","+surcharge.getArrivalStations().get(i);
                }
            }
        }
        surcharge.setArrivalStation(arrivalStation);

        EUserDetails userDetail = SecurityUtils.getUser();
        surcharge.setOrgId(userDetail.getOrgId());
        surcharge.setCreatorId(userDetail.getId());
        surcharge.setCreatorName(buildName(userDetail));
        surcharge.setCreateTime(LocalDateTime.now());
        return surchargeMapper.insert(surcharge);
    }

    private String buildName(EUserDetails userDetail) {
        StringBuilder builder = new StringBuilder();
        builder.append(userDetail.getUserCname());
        builder.append(" ");
        builder.append(userDetail.getUserEmail());
        return builder.toString();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteSurchargeById(Integer surchargeId) {
        if (null == surchargeId) {
            return 0;
        }
        return surchargeMapper.deleteById(surchargeId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateSurcharge(Surcharge surcharge) {
        if (null == surcharge.getSurchargeId()) {
            throw new IllegalArgumentException("数据参数异常!");
        }
        String departureStation="";
        if (surcharge.getDepartureStations()!=null) {
            for (int i = 0; i < surcharge.getDepartureStations().size(); i++) {
                if (departureStation.length()==0) {
                    departureStation=""+surcharge.getDepartureStations().get(i);
                } else {
                    departureStation=departureStation+","+surcharge.getDepartureStations().get(i);
                }
            }
        }
        surcharge.setDepartureStation(departureStation);

        String arrivalStation="";
        if (surcharge.getArrivalStations()!=null) {
            for (int i = 0; i < surcharge.getArrivalStations().size(); i++) {
                if (arrivalStation.length()==0) {
                    arrivalStation=""+surcharge.getArrivalStations().get(i);
                } else {
                    arrivalStation=arrivalStation+","+surcharge.getArrivalStations().get(i);
                }
            }
        }
        surcharge.setArrivalStation(arrivalStation);

        String arrivalNationCodes="";
        if (surcharge.getArrivalNationCodes()!=null) {
            for (int i = 0; i < surcharge.getArrivalNationCodes().size(); i++) {
                if (arrivalNationCodes.length()==0) {
                    arrivalNationCodes=""+surcharge.getArrivalNationCodes().get(i);
                } else {
                    arrivalNationCodes=arrivalNationCodes+","+surcharge.getArrivalNationCodes().get(i);
                }
            }
        }
        surcharge.setArrivalNationCode(arrivalNationCodes);

        EUserDetails userDetail = SecurityUtils.getUser();
        surcharge.setEditorId(userDetail.getId());
        surcharge.setEditorName(buildName(userDetail));
        surcharge.setEditTime(LocalDateTime.now());

        return surchargeMapper.updateById(surcharge);
    }

    @Override
    public List<Surcharge> getSurchargeForBillMake(Surcharge bean) {
        bean.setOrgId(SecurityUtils.getUser().getOrgId());
        List<Surcharge> list = baseMapper.getSurchargeForBillMake(bean);
        return list;
    }

    @Override
    public List<SurchargeExcel> queryListForExcel(Surcharge bean) {
        bean.setOrgId(SecurityUtils.getUser().getOrgId());
        List<SurchargeExcel> pa = baseMapper.queryListForExcel(bean);
        pa.stream().forEach(surchargeExcel -> {
            if(surchargeExcel.getEndDate() == null || "".equals(surchargeExcel.getEndDate())){
                surchargeExcel.setEndDate("2099-12-31");
            }else{
                surchargeExcel.setEndDate(surchargeExcel.getEndDate().split(" ")[0]);
            }
            if(surchargeExcel.getBeginDate() != null && !"".equals(surchargeExcel.getBeginDate())){
                surchargeExcel.setBeginDate(surchargeExcel.getBeginDate().split(" ")[0]);
            }
        });
        return pa;
    }
}
