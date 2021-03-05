package com.efreight.afbase.service.impl;


import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.efreight.afbase.entity.exportExcel.AwbNumberExcel;
import com.efreight.afbase.entity.exportExcel.IncomeExcel;
import com.efreight.common.core.utils.ExportExcel;
import com.efreight.common.core.utils.WebUtils;
import lombok.AllArgsConstructor;

import com.efreight.afbase.entity.AwbNumber;
import com.efreight.afbase.entity.LogBean;
import com.efreight.afbase.dao.AwbNumberMapper;
import com.efreight.afbase.service.AwbNumberService;
import com.efreight.afbase.service.LogService;
import com.efreight.common.security.util.SecurityUtils;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import org.springframework.beans.BeanUtils;
import com.alibaba.druid.util.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class AwbNumberServiceImpl extends ServiceImpl<AwbNumberMapper, AwbNumber> implements AwbNumberService {
    private final AwbNumberMapper awbNumberMapper;
    private final LogService logService;

    @Override
    public IPage<AwbNumber> getListPage(Page page, AwbNumber bean) {
        QueryWrapper<AwbNumber> queryWrapper = new QueryWrapper<>();
        if (bean.getAwbNumber() != null && !"".equals(bean.getAwbNumber())) {
            queryWrapper.like("awb_number", "%" + bean.getAwbNumber() + "%");
        }
        if (bean.getAwbFromName() != null && !"".equals(bean.getAwbFromName())) {
            queryWrapper.like("awb_from_name", "%" + bean.getAwbFromName() + "%");
        }
        if (bean.getDepartureStation() != null && !"".equals(bean.getDepartureStation())) {
            queryWrapper.eq("departure_station", bean.getDepartureStation());
        }
        if (bean.getAwbStatus() != null && !"".equals(bean.getAwbStatus())) {
            queryWrapper.eq("awb_status", bean.getAwbStatus());
        }
        if (bean.getAwbFromType() != null && !"".equals(bean.getAwbFromType())) {
            queryWrapper.eq("awb_from_type", bean.getAwbFromType());
        }

        queryWrapper.eq("org_id", SecurityUtils.getUser().getOrgId());
        queryWrapper.orderByDesc("creat_time","awb_number");
        return baseMapper.selectPage(page, queryWrapper);
    }

    @Override
    public void exportExcel(AwbNumber bean) {
        QueryWrapper<AwbNumber> queryWrapper = new QueryWrapper<>();
        if (bean.getAwbNumber() != null && !"".equals(bean.getAwbNumber())) {
            queryWrapper.like("awb_number", "%" + bean.getAwbNumber() + "%");
        }
        if (bean.getAwbFromName() != null && !"".equals(bean.getAwbFromName())) {
            queryWrapper.like("awb_from_name", "%" + bean.getAwbFromName() + "%");
        }
        if (bean.getDepartureStation() != null && !"".equals(bean.getDepartureStation())) {
            queryWrapper.eq("departure_station", bean.getDepartureStation());
        }
        if (bean.getAwbStatus() != null && !"".equals(bean.getAwbStatus())) {
            queryWrapper.eq("awb_status", bean.getAwbStatus());
        }
        if (bean.getAwbFromType() != null && !"".equals(bean.getAwbFromType())) {
            queryWrapper.eq("awb_from_type", bean.getAwbFromType());
        }

        queryWrapper.eq("org_id", SecurityUtils.getUser().getOrgId());
        queryWrapper.orderByDesc("creat_time");
        List<AwbNumber> list = baseMapper.selectList(queryWrapper);

        List<AwbNumberExcel> result = list.stream().map(awbNumber -> {
            AwbNumberExcel awbNumberExcel = new AwbNumberExcel();
            BeanUtils.copyProperties(awbNumber, awbNumberExcel);
            awbNumberExcel.setAwbFromTypeAndName(awbNumber.getAwbFromType() + "-" + awbNumber.getAwbFromName());
            return awbNumberExcel;
        }).collect(Collectors.toList());

        ExportExcel<AwbNumberExcel> ex = new ExportExcel<AwbNumberExcel>();
        String[] headers = {"主运单号","始发港","运单来源", "主单状态","创建人","创建时间"};
        ex.exportExcel(WebUtils.getResponse(), "导出EXCEL", headers, result, "Export");
    }

    @Override
    public List<AwbNumber> selectOneYearAwbList(ArrayList<String> al) {
        QueryWrapper<AwbNumber> queryWrapper = new QueryWrapper<>();

        queryWrapper.eq("org_id", SecurityUtils.getUser().getOrgId());
        queryWrapper.ge("creat_time", LocalDateTime.now().plusYears(-1));
        queryWrapper.in("awb_number", al);
        return baseMapper.selectList(queryWrapper);
    }

    @Override
    public List<AwbNumber> selectTwoYearAwbList(ArrayList<String> al) {
        QueryWrapper<AwbNumber> queryWrapper = new QueryWrapper<>();

        queryWrapper.eq("org_id", SecurityUtils.getUser().getOrgId());
        queryWrapper.lt("creat_time", LocalDateTime.now().plusYears(-1));
        queryWrapper.in("awb_number", al);
        return baseMapper.selectList(queryWrapper);
    }

    @Override
    public List<AwbNumber> queryList(AwbNumber bean) {

        return baseMapper.queryList(SecurityUtils.getUser().getOrgId(), bean.getAwbFromType(), bean.getCoopMnemonicV(), bean.getCoopNameV(),bean.getAwbFromName());
    }

    @Override
    public IPage<AwbNumber> getSelectListPage(Page page, AwbNumber bean) {
        return baseMapper.getSelectListPage(page, SecurityUtils.getUser().getOrgId(), bean.getAwbFromType(), bean.getCoopMnemonicV(), bean.getCoopNameV());
    }

    @Override
    public List<Map<String, Object>> selectCategory(String category) {
    	if("航线签单".equals(category)) {
    		 return baseMapper.selectCategorySign("出口产品");
    	}else {
    		 return baseMapper.selectCategory(category);
    	}
       
    }
    @Override
    public List<Map<String, Object>> selectCategory2(String category,String businessScope) {
    	return baseMapper.selectCategory2(category,businessScope);
    }
    @Override
    public List<Map<String, Object>> selectCategoryPro(String category,String departureStation) {
    	return baseMapper.selectCategoryPro(category,departureStation);
    }

    @Override
    public List<Map<String, Object>> selectWarehouse(String warehouse) {
        return baseMapper.selectWarehouse(SecurityUtils.getUser().getOrgId(), warehouse);
    }

    @Override
    public List<Map<String, Object>> selectCarrier(String awb3) {
        return baseMapper.selectCarrier(awb3);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean saveAwbNumber(AwbNumber bean, ArrayList<Map<String, Object>> al) {
        bean.setCreatTime(new Date());
        bean.setCreatorName(SecurityUtils.getUser().getUserCname() + " " + SecurityUtils.getUser().getUserEmail());
        bean.setCreatorId(SecurityUtils.getUser().getId());
        bean.setOrgId(SecurityUtils.getUser().getOrgId());
        for (int i = 0; i < al.size(); i++) {
            bean.setAwbNumber(al.get(i).get("awbNumber").toString());
            awbNumberMapper.insertAwbNumber(bean);
        }

        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean doLock(AwbNumber bean) {
        bean.setLockTime(new Date());
        bean.setLockerName(SecurityUtils.getUser().getUserCname() + " " + SecurityUtils.getUser().getUserEmail());
        bean.setLockerId(SecurityUtils.getUser().getId());

        bean.setOrgId(SecurityUtils.getUser().getOrgId());
        bean.setAwbIds("'" + bean.getAwbIds().replaceAll(",", "','") + "'");
        awbNumberMapper.doLock(bean);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean doCancelLock(AwbNumber bean) {
        String awbs[] = bean.getAwbIds().split(",");
        for (int i = 0; i < awbs.length; i++) {
            //日志
            try {
                AwbNumber awb = baseMapper.selectById(awbs[i]);
                LogBean logBean = new LogBean();
                logBean.setBusinessScope("AE");
                logBean.setLogType("主单日志");
                logBean.setNodeName("取消锁定");
                logBean.setPageName("主单号管理");
                logBean.setPageFunction("取消锁定");
                logBean.setLogRemark("取消锁定，主单号：<" + awb.getAwbNumber() + ">");

                logBean.setAwbNumber(awb.getAwbNumber());
                logBean.setAwbUuid(awb.getAwbUuid());
                logService.saveLog(logBean);
            } catch (Exception e) {
            }

        }
        bean.setOrgId(SecurityUtils.getUser().getOrgId());
        bean.setAwbIds("'" + bean.getAwbIds().replaceAll(",", "','") + "'");
        awbNumberMapper.doCancelLock(bean);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean doDelete(AwbNumber bean) {
        String awbs[] = bean.getAwbIds().split(",");
        for (int i = 0; i < awbs.length; i++) {
            //日志
            try {
                AwbNumber awb = baseMapper.selectById(awbs[i]);
                LogBean logBean = new LogBean();
                logBean.setBusinessScope("AE");
                logBean.setLogType("主单日志");
                logBean.setNodeName("删除单号");
                logBean.setPageName("主单号管理");
                logBean.setPageFunction("删除单号");
                logBean.setLogRemark("删除主单号：<" + awb.getAwbNumber() + ">");

                logBean.setAwbNumber(awb.getAwbNumber());
                logBean.setAwbUuid(awb.getAwbUuid());
                logService.saveLog(logBean);
            } catch (Exception e) {
            }

        }
        bean.setOrgId(SecurityUtils.getUser().getOrgId());
        bean.setAwbIds("'" + bean.getAwbIds().replaceAll(",", "','") + "'");
        awbNumberMapper.doDelete(bean);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean bookAwbList(AwbNumber bean) {
        bean.setReservedTime(new Date());
        bean.setOrgId(SecurityUtils.getUser().getOrgId());
        bean.setReservedUser(SecurityUtils.getUser().getUserCname() + " " + SecurityUtils.getUser().getUserEmail());
        bean.setReservedUserId(SecurityUtils.getUser().getId());
//		bean.setAwb8("'"+bean.getAwb8().replaceAll(",", "','")+"'");
        bean.setAwbIds("'" + bean.getAwbIds().replaceAll(",", "','") + "'");
        awbNumberMapper.updateAwbNumber(bean);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean cancelBook(AwbNumber bean) {
        String awbs[] = bean.getAwbIds().split(",");
        for (int i = 0; i < awbs.length; i++) {
            //日志
            try {
                AwbNumber awb = baseMapper.selectById(awbs[i]);
                LogBean logBean = new LogBean();
                logBean.setBusinessScope("AE");
                logBean.setLogType("主单日志");
                logBean.setNodeName("取消预订");
                logBean.setPageName("主单号管理");
                logBean.setPageFunction("取消预订");
                logBean.setLogRemark("取消预订，原代理：<" + awb.getReservedCoopName() + ">");

                logBean.setAwbNumber(awb.getAwbNumber());
                logBean.setAwbUuid(awb.getAwbUuid());
                logService.saveLog(logBean);
            } catch (Exception e) {
            }

        }

        bean.setOrgId(SecurityUtils.getUser().getOrgId());
        bean.setAwbIds("'" + bean.getAwbIds().replaceAll(",", "','") + "'");
        awbNumberMapper.cancelBook(bean);
        return true;
    }

    @Override
    public List<Map<String, Object>> selectVCategory(String category) {
        return baseMapper.selectVCategory(category);
    }

    @Override
    public IPage<LogBean> awbLogPage(Page page, LogBean bean) {
        bean.setOrgId(SecurityUtils.getUser().getOrgId());
        return baseMapper.awbLogPage(page, bean);
    }

}
