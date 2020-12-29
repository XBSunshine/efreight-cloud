package com.efreight.afbase.service.impl;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Assert;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.efreight.afbase.dao.AfAwbPrintShipperConsigneeMapper;
import com.efreight.afbase.dao.AfOrderMapper;
import com.efreight.afbase.entity.*;
import com.efreight.afbase.dao.AwbPrintMapper;
import com.efreight.afbase.entity.procedure.AfPAwbPrintForMawbPrintProcedure;
import com.efreight.afbase.entity.procedure.AfPAwbPrintProcedure;
import com.efreight.afbase.entity.shipping.APIType;
import com.efreight.afbase.service.*;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.efreight.afbase.utils.PDFUtils;
import com.efreight.afbase.utils.RemoteSendUtils;
import com.efreight.common.core.feign.RemoteServiceToHRS;
import com.efreight.common.security.service.EUserDetails;
import com.efreight.common.security.util.MessageInfo;
import com.efreight.common.security.util.SecurityUtils;
import com.efreight.common.security.vo.OrgInterfaceVo;
import com.itextpdf.text.Document;
import com.itextpdf.text.pdf.*;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.apache.commons.lang.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * AF 操作管理 运单制单 服务实现类
 * </p>
 *
 * @author xiaobo
 * @since 2019-10-11
 */
@Service
@AllArgsConstructor
public class AwbPrintServiceImpl extends ServiceImpl<AwbPrintMapper, AwbPrint> implements AwbPrintService {

    private final AwbPrintChargesOtherService awbPrintChargesOtherService;
    private final AwbPrintSizeService awbPrintSizeService;
    private final AfShipperLetterService afShipperLetterService;
    private final AfOrderService afOrderService;
    private final AfOrderMapper orderMapper;
    private final CarrierService carrierService;
    private final AwbNumberService awbNumberService;
    private final AirportService airportService;
    private final AfAwbPrintShipperConsigneeMapper afAwbPrintShipperConsigneeMapper;
    private final AfOrderShipperConsigneeService afOrderShipperConsigneeService;
    private final AwbPrintMapper awbPrintMapper;
    private final LogService logService;
    private final RemoteServiceToHRS remoteServiceToHRS;

    @Override
    public void delete(Integer awbPrintId) {
        baseMapper.deleteById(awbPrintId);
    }

    public void deleteForUnloadOrder(String awbUuid) {
        LambdaQueryWrapper<AwbPrint> wrapper = Wrappers.<AwbPrint>lambdaQuery();
        wrapper.eq(AwbPrint::getAwbUuid, awbUuid);
        baseMapper.delete(wrapper);
    }

    @Override
    public void finish(AwbPrint awbPrint) {
        awbPrint.setAwbStatus("已完成");
        modify(awbPrint);
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void modify(AwbPrint awbPrint) {
        AwbPrint print = baseMapper.selectById(awbPrint.getAwbPrintId());
        if ("已完成".equals(print.getAwbStatus())) {
            throw new RuntimeException("单证已完成，无法修改");
        }
        awbPrint.setEditorId(SecurityUtils.getUser().getId());
        awbPrint.setEditorName(SecurityUtils.getUser().getUserCname() + " " + SecurityUtils.getUser().getUserEmail());
        awbPrint.setEditTime(LocalDateTime.now());
        baseMapper.updateById(awbPrint);

        //修改费用
        awbPrintChargesOtherService.deleteByAwbPrintId(awbPrint.getAwbPrintId());
        awbPrint.getAwbPrintChargesOtherList().stream().forEach(awbPrintChargesOther -> {
            awbPrintChargesOther.setAwbPrintId(Integer.parseInt(awbPrint.getAwbPrintId()));
            awbPrintChargesOther.setOrgId(SecurityUtils.getUser().getOrgId());
        });

        awbPrintChargesOtherService.saveBatch(awbPrint.getAwbPrintChargesOtherList());


        //修改尺寸
        awbPrintSizeService.deleteByAwbPrintId(awbPrint.getAwbPrintId());
        awbPrint.getAwbPrintSizeList().stream().forEach(awbPrintSize -> {
            awbPrintSize.setAwbPrintId(Integer.parseInt(awbPrint.getAwbPrintId()));
            awbPrintSize.setOrgId(SecurityUtils.getUser().getOrgId());
        });

        awbPrintSizeService.saveBatch(awbPrint.getAwbPrintSizeList());

        //收发货人保存
        //根据AwbPrintId查询af_awb_print_shipper_consignee表，看是否存在收发货人信息，存在则更新，不存在则新增
        AfAwbPrintShipperConsignee apsc1 = baseMapper.getAwbPrintShipperConsigneeInfoByAwbPrintId(SecurityUtils.getUser().getOrgId(), Integer.parseInt(awbPrint.getAwbPrintId()), 0);
        if (apsc1 != null) {//存在，则进行更新
            AfOrderShipperConsignee afOrderShipperConsignee = awbPrint.getAfOrderShipperConsignee1();
            if (afOrderShipperConsignee != null) {

                apsc1.setOrgId(SecurityUtils.getUser().getOrgId());
                apsc1.setScType(0);
                apsc1.setScName(afOrderShipperConsignee.getScName());
                apsc1.setScAddress(afOrderShipperConsignee.getScAddress());
                apsc1.setScCode(afOrderShipperConsignee.getScCode());
                apsc1.setScCodeType(afOrderShipperConsignee.getScCodeType());
                apsc1.setAeoCode(afOrderShipperConsignee.getAeoCode());
                apsc1.setNationCode(afOrderShipperConsignee.getNationCode());
                apsc1.setStateCode(afOrderShipperConsignee.getStateCode());
                apsc1.setCityCode(afOrderShipperConsignee.getCityCode());
                apsc1.setCityName(afOrderShipperConsignee.getCityName());
                apsc1.setPostCode(afOrderShipperConsignee.getPostCode());
                apsc1.setTelNumber(afOrderShipperConsignee.getTelNumber());
                apsc1.setFaxNumber(afOrderShipperConsignee.getFaxNumber());
                apsc1.setScPrintRemark(afOrderShipperConsignee.getScPrintRemark());
                apsc1.setScMnemonic(afOrderShipperConsignee.getScMnemonic());
                apsc1.setEditTime(LocalDateTime.now());
                apsc1.setEditorId(SecurityUtils.getUser().getId());
                apsc1.setEditorName(SecurityUtils.getUser().getUserCname() + " " + SecurityUtils.getUser().getUserEmail());

                afAwbPrintShipperConsigneeMapper.updateById(apsc1);
            }
        } else {
            AfOrderShipperConsignee afOrderShipperConsignee = awbPrint.getAfOrderShipperConsignee1();
            if (afOrderShipperConsignee != null) {
                AfAwbPrintShipperConsignee afAwbPrintShipperConsignee = new AfAwbPrintShipperConsignee();

                afAwbPrintShipperConsignee.setAwbPrintId(Integer.parseInt(awbPrint.getAwbPrintId()));
                afAwbPrintShipperConsignee.setOrgId(SecurityUtils.getUser().getOrgId());
                afAwbPrintShipperConsignee.setScType(0);
                afAwbPrintShipperConsignee.setScName(afOrderShipperConsignee.getScName());
                afAwbPrintShipperConsignee.setScAddress(afOrderShipperConsignee.getScAddress());
                afAwbPrintShipperConsignee.setScCode(afOrderShipperConsignee.getScCode());
                afAwbPrintShipperConsignee.setScCodeType(afOrderShipperConsignee.getScCodeType());
                afAwbPrintShipperConsignee.setAeoCode(afOrderShipperConsignee.getAeoCode());
                afAwbPrintShipperConsignee.setNationCode(afOrderShipperConsignee.getNationCode());
                afAwbPrintShipperConsignee.setStateCode(afOrderShipperConsignee.getStateCode());
                afAwbPrintShipperConsignee.setCityCode(afOrderShipperConsignee.getCityCode());
                afAwbPrintShipperConsignee.setCityName(afOrderShipperConsignee.getCityName());
                afAwbPrintShipperConsignee.setPostCode(afOrderShipperConsignee.getPostCode());
                afAwbPrintShipperConsignee.setTelNumber(afOrderShipperConsignee.getTelNumber());
                afAwbPrintShipperConsignee.setFaxNumber(afOrderShipperConsignee.getFaxNumber());
                afAwbPrintShipperConsignee.setScPrintRemark(afOrderShipperConsignee.getScPrintRemark());
                afAwbPrintShipperConsignee.setScMnemonic(afOrderShipperConsignee.getScMnemonic());
                afAwbPrintShipperConsignee.setCreateTime(LocalDateTime.now());
                afAwbPrintShipperConsignee.setCreatorId(SecurityUtils.getUser().getId());
                afAwbPrintShipperConsignee.setCreatorName(SecurityUtils.getUser().getUserCname() + " " + SecurityUtils.getUser().getUserEmail());
                afAwbPrintShipperConsignee.setEditTime(LocalDateTime.now());
                afAwbPrintShipperConsignee.setEditorId(SecurityUtils.getUser().getId());
                afAwbPrintShipperConsignee.setEditorName(SecurityUtils.getUser().getUserCname() + " " + SecurityUtils.getUser().getUserEmail());

                afAwbPrintShipperConsigneeMapper.insert(afAwbPrintShipperConsignee);
            }
        }

        AfAwbPrintShipperConsignee apsc2 = baseMapper.getAwbPrintShipperConsigneeInfoByAwbPrintId(SecurityUtils.getUser().getOrgId(), Integer.parseInt(awbPrint.getAwbPrintId()), 1);
        if (apsc2 != null) {//存在，则进行更新
            AfOrderShipperConsignee afOrderShipperConsignee = awbPrint.getAfOrderShipperConsignee2();
            if (afOrderShipperConsignee != null) {

                apsc2.setOrgId(SecurityUtils.getUser().getOrgId());
                apsc2.setScType(1);
                apsc2.setScName(afOrderShipperConsignee.getScName());
                apsc2.setScAddress(afOrderShipperConsignee.getScAddress());
                apsc2.setScCode(afOrderShipperConsignee.getScCode());
                apsc2.setScCodeType(afOrderShipperConsignee.getScCodeType());
                apsc2.setAeoCode(afOrderShipperConsignee.getAeoCode());
                apsc2.setNationCode(afOrderShipperConsignee.getNationCode());
                apsc2.setStateCode(afOrderShipperConsignee.getStateCode());
                apsc2.setCityCode(afOrderShipperConsignee.getCityCode());
                apsc2.setCityName(afOrderShipperConsignee.getCityName());
                apsc2.setPostCode(afOrderShipperConsignee.getPostCode());
                apsc2.setTelNumber(afOrderShipperConsignee.getTelNumber());
                apsc2.setFaxNumber(afOrderShipperConsignee.getFaxNumber());
                apsc2.setScPrintRemark(afOrderShipperConsignee.getScPrintRemark());
                apsc2.setScMnemonic(afOrderShipperConsignee.getScMnemonic());
                apsc2.setEditTime(LocalDateTime.now());
                apsc2.setEditorId(SecurityUtils.getUser().getId());
                apsc2.setEditorName(SecurityUtils.getUser().getUserCname() + " " + SecurityUtils.getUser().getUserEmail());

                afAwbPrintShipperConsigneeMapper.updateById(apsc2);
            }
        } else {
            AfOrderShipperConsignee afOrderShipperConsignee = awbPrint.getAfOrderShipperConsignee2();
            if (afOrderShipperConsignee != null) {
                AfAwbPrintShipperConsignee afAwbPrintShipperConsignee = new AfAwbPrintShipperConsignee();

                afAwbPrintShipperConsignee.setAwbPrintId(Integer.parseInt(awbPrint.getAwbPrintId()));
                afAwbPrintShipperConsignee.setOrgId(SecurityUtils.getUser().getOrgId());
                afAwbPrintShipperConsignee.setScType(1);
                afAwbPrintShipperConsignee.setScName(afOrderShipperConsignee.getScName());
                afAwbPrintShipperConsignee.setScAddress(afOrderShipperConsignee.getScAddress());
                afAwbPrintShipperConsignee.setScCode(afOrderShipperConsignee.getScCode());
                afAwbPrintShipperConsignee.setScCodeType(afOrderShipperConsignee.getScCodeType());
                afAwbPrintShipperConsignee.setAeoCode(afOrderShipperConsignee.getAeoCode());
                afAwbPrintShipperConsignee.setNationCode(afOrderShipperConsignee.getNationCode());
                afAwbPrintShipperConsignee.setStateCode(afOrderShipperConsignee.getStateCode());
                afAwbPrintShipperConsignee.setCityCode(afOrderShipperConsignee.getCityCode());
                afAwbPrintShipperConsignee.setCityName(afOrderShipperConsignee.getCityName());
                afAwbPrintShipperConsignee.setPostCode(afOrderShipperConsignee.getPostCode());
                afAwbPrintShipperConsignee.setTelNumber(afOrderShipperConsignee.getTelNumber());
                afAwbPrintShipperConsignee.setFaxNumber(afOrderShipperConsignee.getFaxNumber());
                afAwbPrintShipperConsignee.setScPrintRemark(afOrderShipperConsignee.getScPrintRemark());
                afAwbPrintShipperConsignee.setScMnemonic(afOrderShipperConsignee.getScMnemonic());
                afAwbPrintShipperConsignee.setCreateTime(LocalDateTime.now());
                afAwbPrintShipperConsignee.setCreatorId(SecurityUtils.getUser().getId());
                afAwbPrintShipperConsignee.setCreatorName(SecurityUtils.getUser().getUserCname() + " " + SecurityUtils.getUser().getUserEmail());
                afAwbPrintShipperConsignee.setEditTime(LocalDateTime.now());
                afAwbPrintShipperConsignee.setEditorId(SecurityUtils.getUser().getId());
                afAwbPrintShipperConsignee.setEditorName(SecurityUtils.getUser().getUserCname() + " " + SecurityUtils.getUser().getUserEmail());

                afAwbPrintShipperConsigneeMapper.insert(afAwbPrintShipperConsignee);
            }
        }

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String insert(AwbPrint awbPrint) {
        //校验如果制过主单，不可再继续制单
        if (awbPrint.getOrderId() == null) {
            throw new RuntimeException("请传入订单信息");
        }
        LambdaQueryWrapper<AwbPrint> wrapper = Wrappers.<AwbPrint>lambdaQuery();
        wrapper.eq(AwbPrint::getOrgId, SecurityUtils.getUser().getOrgId()).eq(AwbPrint::getAwbType, 0).eq(AwbPrint::getOrderId, awbPrint.getOrderId());
        AwbPrint print = getOne(wrapper);
        if (print != null) {
            throw new RuntimeException("该订单已经制主单");
        }

        awbPrint.setOrgId(SecurityUtils.getUser().getOrgId());
        awbPrint.setCreateTime(LocalDateTime.now());
        awbPrint.setCreatorId(SecurityUtils.getUser().getId());
        awbPrint.setCreatorName(SecurityUtils.getUser().getUserCname() + " " + SecurityUtils.getUser().getUserEmail());

        awbPrint.setEditorId(SecurityUtils.getUser().getId());
        awbPrint.setEditorName(SecurityUtils.getUser().getUserCname() + " " + SecurityUtils.getUser().getUserEmail());
        awbPrint.setEditTime(LocalDateTime.now());
        awbPrint.setAwbStatus("已暂存");
        awbPrint.setAwbType(0);
        baseMapper.insert(awbPrint);

        //保存费用
        awbPrint.getAwbPrintChargesOtherList().stream().forEach(awbPrintChargesOther -> {
            awbPrintChargesOther.setAwbPrintId(Integer.parseInt(awbPrint.getAwbPrintId()));
            awbPrintChargesOther.setOrgId(SecurityUtils.getUser().getOrgId());
        });

        awbPrintChargesOtherService.saveBatch(awbPrint.getAwbPrintChargesOtherList());

        //保存尺寸
        awbPrint.getAwbPrintSizeList().stream().forEach(awbPrintSize -> {
            awbPrintSize.setAwbPrintId(Integer.parseInt(awbPrint.getAwbPrintId()));
            awbPrintSize.setOrgId(SecurityUtils.getUser().getOrgId());
        });
        awbPrintSizeService.saveBatch(awbPrint.getAwbPrintSizeList());

        //收发货人保存
        AfOrderShipperConsignee afOrderShipperConsignee = awbPrint.getAfOrderShipperConsignee1();
        if (afOrderShipperConsignee != null) {
            AfAwbPrintShipperConsignee afAwbPrintShipperConsignee = new AfAwbPrintShipperConsignee();

            afAwbPrintShipperConsignee.setOrgId(SecurityUtils.getUser().getOrgId());
            afAwbPrintShipperConsignee.setAwbPrintId(Integer.parseInt(awbPrint.getAwbPrintId()));
            afAwbPrintShipperConsignee.setScType(0);
            afAwbPrintShipperConsignee.setScName(afOrderShipperConsignee.getScName());
            afAwbPrintShipperConsignee.setScAddress(afOrderShipperConsignee.getScAddress());
            afAwbPrintShipperConsignee.setScCode(afOrderShipperConsignee.getScCode());
            afAwbPrintShipperConsignee.setScCodeType(afOrderShipperConsignee.getScCodeType());
            afAwbPrintShipperConsignee.setAeoCode(afOrderShipperConsignee.getAeoCode());
            afAwbPrintShipperConsignee.setNationCode(afOrderShipperConsignee.getNationCode());
            afAwbPrintShipperConsignee.setStateCode(afOrderShipperConsignee.getStateCode());
            afAwbPrintShipperConsignee.setCityCode(afOrderShipperConsignee.getCityCode());
            afAwbPrintShipperConsignee.setCityName(afOrderShipperConsignee.getCityName());
            afAwbPrintShipperConsignee.setPostCode(afOrderShipperConsignee.getPostCode());
            afAwbPrintShipperConsignee.setTelNumber(afOrderShipperConsignee.getTelNumber());
            afAwbPrintShipperConsignee.setFaxNumber(afOrderShipperConsignee.getFaxNumber());
            afAwbPrintShipperConsignee.setScPrintRemark(afOrderShipperConsignee.getScPrintRemark());
            afAwbPrintShipperConsignee.setScMnemonic(afOrderShipperConsignee.getScMnemonic());
            afAwbPrintShipperConsignee.setCreateTime(LocalDateTime.now());
            afAwbPrintShipperConsignee.setCreatorId(SecurityUtils.getUser().getId());
            afAwbPrintShipperConsignee.setCreatorName(SecurityUtils.getUser().getUserCname() + " " + SecurityUtils.getUser().getUserEmail());

            afAwbPrintShipperConsigneeMapper.insert(afAwbPrintShipperConsignee);
        }
        AfOrderShipperConsignee afOrderShipperConsignee2 = awbPrint.getAfOrderShipperConsignee2();
        if (afOrderShipperConsignee2 != null) {
            AfAwbPrintShipperConsignee afAwbPrintShipperConsignee = new AfAwbPrintShipperConsignee();

            afAwbPrintShipperConsignee.setOrgId(SecurityUtils.getUser().getOrgId());
            afAwbPrintShipperConsignee.setAwbPrintId(Integer.parseInt(awbPrint.getAwbPrintId()));
            afAwbPrintShipperConsignee.setScType(1);
            afAwbPrintShipperConsignee.setScName(afOrderShipperConsignee2.getScName());
            afAwbPrintShipperConsignee.setScAddress(afOrderShipperConsignee2.getScAddress());
            afAwbPrintShipperConsignee.setScCode(afOrderShipperConsignee2.getScCode());
            afAwbPrintShipperConsignee.setScCodeType(afOrderShipperConsignee2.getScCodeType());
            afAwbPrintShipperConsignee.setAeoCode(afOrderShipperConsignee2.getAeoCode());
            afAwbPrintShipperConsignee.setNationCode(afOrderShipperConsignee2.getNationCode());
            afAwbPrintShipperConsignee.setStateCode(afOrderShipperConsignee2.getStateCode());
            afAwbPrintShipperConsignee.setCityCode(afOrderShipperConsignee2.getCityCode());
            afAwbPrintShipperConsignee.setCityName(afOrderShipperConsignee2.getCityName());
            afAwbPrintShipperConsignee.setPostCode(afOrderShipperConsignee2.getPostCode());
            afAwbPrintShipperConsignee.setTelNumber(afOrderShipperConsignee2.getTelNumber());
            afAwbPrintShipperConsignee.setFaxNumber(afOrderShipperConsignee2.getFaxNumber());
            afAwbPrintShipperConsignee.setScPrintRemark(afOrderShipperConsignee2.getScPrintRemark());
            afAwbPrintShipperConsignee.setScMnemonic(afOrderShipperConsignee2.getScMnemonic());
            afAwbPrintShipperConsignee.setCreateTime(LocalDateTime.now());
            afAwbPrintShipperConsignee.setCreatorId(SecurityUtils.getUser().getId());
            afAwbPrintShipperConsignee.setCreatorName(SecurityUtils.getUser().getUserCname() + " " + SecurityUtils.getUser().getUserEmail());

            afAwbPrintShipperConsigneeMapper.insert(afAwbPrintShipperConsignee);
        }

        return awbPrint.getAwbPrintId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String insertHawb(AwbPrint awbPrint) {
        awbPrint.setOrgId(SecurityUtils.getUser().getOrgId());
        awbPrint.setCreateTime(LocalDateTime.now());
        awbPrint.setCreatorId(SecurityUtils.getUser().getId());
        awbPrint.setCreatorName(SecurityUtils.getUser().getUserCname() + " " + SecurityUtils.getUser().getUserEmail());

        awbPrint.setEditorId(SecurityUtils.getUser().getId());
        awbPrint.setEditorName(SecurityUtils.getUser().getUserCname() + " " + SecurityUtils.getUser().getUserEmail());
        awbPrint.setEditTime(LocalDateTime.now());
        awbPrint.setAwbStatus("已暂存");
        awbPrint.setAwbType(1);
        baseMapper.insert(awbPrint);

        //保存费用
        awbPrint.getAwbPrintChargesOtherList().stream().forEach(awbPrintChargesOther -> {
            awbPrintChargesOther.setAwbPrintId(Integer.parseInt(awbPrint.getAwbPrintId()));
            awbPrintChargesOther.setOrgId(SecurityUtils.getUser().getOrgId());
        });

        awbPrintChargesOtherService.saveBatch(awbPrint.getAwbPrintChargesOtherList());

        //保存尺寸
        awbPrint.getAwbPrintSizeList().stream().forEach(awbPrintSize -> {
            awbPrintSize.setAwbPrintId(Integer.parseInt(awbPrint.getAwbPrintId()));
            awbPrintSize.setOrgId(SecurityUtils.getUser().getOrgId());
        });
        awbPrintSizeService.saveBatch(awbPrint.getAwbPrintSizeList());

        //收发货人保存
        AfOrderShipperConsignee afOrderShipperConsignee = awbPrint.getAfOrderShipperConsignee1();
        if (afOrderShipperConsignee != null) {
            AfAwbPrintShipperConsignee afAwbPrintShipperConsignee = new AfAwbPrintShipperConsignee();

            afAwbPrintShipperConsignee.setOrgId(SecurityUtils.getUser().getOrgId());
            afAwbPrintShipperConsignee.setAwbPrintId(Integer.parseInt(awbPrint.getAwbPrintId()));
            afAwbPrintShipperConsignee.setScType(0);
            afAwbPrintShipperConsignee.setScName(afOrderShipperConsignee.getScName());
            afAwbPrintShipperConsignee.setScAddress(afOrderShipperConsignee.getScAddress());
            afAwbPrintShipperConsignee.setScCode(afOrderShipperConsignee.getScCode());
            afAwbPrintShipperConsignee.setScCodeType(afOrderShipperConsignee.getScCodeType());
            afAwbPrintShipperConsignee.setAeoCode(afOrderShipperConsignee.getAeoCode());
            afAwbPrintShipperConsignee.setNationCode(afOrderShipperConsignee.getNationCode());
            afAwbPrintShipperConsignee.setStateCode(afOrderShipperConsignee.getStateCode());
            afAwbPrintShipperConsignee.setCityCode(afOrderShipperConsignee.getCityCode());
            afAwbPrintShipperConsignee.setCityName(afOrderShipperConsignee.getCityName());
            afAwbPrintShipperConsignee.setPostCode(afOrderShipperConsignee.getPostCode());
            afAwbPrintShipperConsignee.setTelNumber(afOrderShipperConsignee.getTelNumber());
            afAwbPrintShipperConsignee.setFaxNumber(afOrderShipperConsignee.getFaxNumber());
            afAwbPrintShipperConsignee.setScPrintRemark(afOrderShipperConsignee.getScPrintRemark());
            afAwbPrintShipperConsignee.setScMnemonic(afOrderShipperConsignee.getScMnemonic());
            afAwbPrintShipperConsignee.setCreateTime(LocalDateTime.now());
            afAwbPrintShipperConsignee.setCreatorId(SecurityUtils.getUser().getId());
            afAwbPrintShipperConsignee.setCreatorName(SecurityUtils.getUser().getUserCname() + " " + SecurityUtils.getUser().getUserEmail());

            afAwbPrintShipperConsigneeMapper.insert(afAwbPrintShipperConsignee);
        }
        AfOrderShipperConsignee afOrderShipperConsignee2 = awbPrint.getAfOrderShipperConsignee2();
        if (afOrderShipperConsignee2 != null) {
            AfAwbPrintShipperConsignee afAwbPrintShipperConsignee = new AfAwbPrintShipperConsignee();

            afAwbPrintShipperConsignee.setOrgId(SecurityUtils.getUser().getOrgId());
            afAwbPrintShipperConsignee.setAwbPrintId(Integer.parseInt(awbPrint.getAwbPrintId()));
            afAwbPrintShipperConsignee.setScType(1);
            afAwbPrintShipperConsignee.setScName(afOrderShipperConsignee2.getScName());
            afAwbPrintShipperConsignee.setScAddress(afOrderShipperConsignee2.getScAddress());
            afAwbPrintShipperConsignee.setScCode(afOrderShipperConsignee2.getScCode());
            afAwbPrintShipperConsignee.setScCodeType(afOrderShipperConsignee2.getScCodeType());
            afAwbPrintShipperConsignee.setAeoCode(afOrderShipperConsignee2.getAeoCode());
            afAwbPrintShipperConsignee.setNationCode(afOrderShipperConsignee2.getNationCode());
            afAwbPrintShipperConsignee.setStateCode(afOrderShipperConsignee2.getStateCode());
            afAwbPrintShipperConsignee.setCityCode(afOrderShipperConsignee2.getCityCode());
            afAwbPrintShipperConsignee.setCityName(afOrderShipperConsignee2.getCityName());
            afAwbPrintShipperConsignee.setPostCode(afOrderShipperConsignee2.getPostCode());
            afAwbPrintShipperConsignee.setTelNumber(afOrderShipperConsignee2.getTelNumber());
            afAwbPrintShipperConsignee.setFaxNumber(afOrderShipperConsignee2.getFaxNumber());
            afAwbPrintShipperConsignee.setScPrintRemark(afOrderShipperConsignee2.getScPrintRemark());
            afAwbPrintShipperConsignee.setScMnemonic(afOrderShipperConsignee2.getScMnemonic());
            afAwbPrintShipperConsignee.setCreateTime(LocalDateTime.now());
            afAwbPrintShipperConsignee.setCreatorId(SecurityUtils.getUser().getId());
            afAwbPrintShipperConsignee.setCreatorName(SecurityUtils.getUser().getUserCname() + " " + SecurityUtils.getUser().getUserEmail());

            afAwbPrintShipperConsigneeMapper.insert(afAwbPrintShipperConsignee);
        }

        return awbPrint.getAwbPrintId();
    }


    @Override
    public AwbPrint view(Integer awbPrintId) {
        AwbPrint awbPrint = baseMapper.selectById(awbPrintId);
        //主单：查询运单的收发货人信息，不存在，则查询订单的收发货人信息，分单：查询分单收发货人信息
        if (awbPrint == null) {
            throw new RuntimeException("运单不存在");
        }
        AfOrderShipperConsignee afOrderShipperConsignee1 = baseMapper.getShipperConsigneeInfoByAwbPrintId(SecurityUtils.getUser().getOrgId(), awbPrintId, 0);
        AfOrderShipperConsignee afOrderShipperConsignee2 = baseMapper.getShipperConsigneeInfoByAwbPrintId(SecurityUtils.getUser().getOrgId(), awbPrintId, 1);
        if (awbPrint.getAwbType() == 0) {
            if (afOrderShipperConsignee1 == null && StrUtil.isNotBlank(awbPrint.getOrderUuid())) {
                afOrderShipperConsignee1 = baseMapper.getShipperConsigneeInfo(SecurityUtils.getUser().getOrgId(), awbPrint.getOrderUuid(), 0);
            }

            if (afOrderShipperConsignee2 == null && StrUtil.isNotBlank(awbPrint.getOrderUuid())) {
                afOrderShipperConsignee2 = baseMapper.getShipperConsigneeInfo(SecurityUtils.getUser().getOrgId(), awbPrint.getOrderUuid(), 1);
            }
        }
        if (afOrderShipperConsignee1 == null) {
            afOrderShipperConsignee1 = new AfOrderShipperConsignee();
        }
        if (afOrderShipperConsignee2 == null) {
            afOrderShipperConsignee2 = new AfOrderShipperConsignee();
        }
        awbPrint.setAfOrderShipperConsignee1(afOrderShipperConsignee1);
        awbPrint.setAfOrderShipperConsignee2(afOrderShipperConsignee2);
        return awbPrint;
    }

    @Override
    public AwbPrint viewByAwbUuid(String awbUuid) {
        LambdaQueryWrapper<AwbPrint> wrapper = Wrappers.<AwbPrint>lambdaQuery();
        wrapper.eq(AwbPrint::getOrgId, SecurityUtils.getUser().getOrgId()).eq(AwbPrint::getAwbUuid, awbUuid).eq(AwbPrint::getAwbType, 0);
        return getOne(wrapper);
    }

    @Override
    public List<AwbPrint> hawbListByAwbUuid(String awbUuid) {
        LambdaQueryWrapper<AwbPrint> wrapper = Wrappers.<AwbPrint>lambdaQuery();
        wrapper.eq(AwbPrint::getOrgId, SecurityUtils.getUser().getOrgId()).eq(AwbPrint::getAwbUuid, awbUuid).eq(AwbPrint::getAwbType, 1);
        return list(wrapper);
    }

    @Override
    public AwbPrint viewByOrderUuid(String orderUuid) {
        LambdaQueryWrapper<AwbPrint> wrapper = Wrappers.<AwbPrint>lambdaQuery();
        wrapper.eq(AwbPrint::getOrderUuid, orderUuid).eq(AwbPrint::getAwbType, 0);
        return getOne(wrapper);
    }

    @Override
    public List<AwbPrint> hawbListByOrderUuid(String orderUuid) {
        LambdaQueryWrapper<AwbPrint> wrapper = Wrappers.<AwbPrint>lambdaQuery();
        wrapper.eq(AwbPrint::getOrderUuid, orderUuid).eq(AwbPrint::getAwbType, 1);
        return list(wrapper);
    }

    @Override
    public IPage getPage(Page page, AwbPrint awbPrint) {
        awbPrint.setOrgId(SecurityUtils.getUser().getOrgId());
        IPage<AwbPrint> iPage = baseMapper.getPage(page, awbPrint);
        iPage.getRecords().stream().forEach(record -> {
            LambdaQueryWrapper<AfOrder> orderWrapper = Wrappers.<AfOrder>lambdaQuery();
            orderWrapper.eq(AfOrder::getOrgId, SecurityUtils.getUser().getOrgId()).eq(AfOrder::getAwbId, record.getAwbId());
            List<Integer> orderIdList = afOrderService.list(orderWrapper).stream().map(AfOrder::getOrderId).collect(Collectors.toList());


            LambdaQueryWrapper<AfShipperLetter> shipperLetterWrapper = Wrappers.<AfShipperLetter>lambdaQuery();
            shipperLetterWrapper.eq(AfShipperLetter::getOrgId, SecurityUtils.getUser().getOrgId()).eq(AfShipperLetter::getSlType, "HAWB").in(AfShipperLetter::getOrderId, orderIdList);
            List<AfShipperLetter> shipperLetterList = afShipperLetterService.list(shipperLetterWrapper);
            ArrayList<AwbPrint> awbPrints = new ArrayList<>();
            shipperLetterList.stream().forEach(afShipperLetter -> {
                LambdaQueryWrapper<AwbPrint> awbPrintWrapper = Wrappers.<AwbPrint>lambdaQuery();
                awbPrintWrapper.eq(AwbPrint::getSlId, afShipperLetter.getSlId()).eq(AwbPrint::getAwbType, 1);
                AwbPrint print = baseMapper.selectOne(awbPrintWrapper);
                if (print != null) {
                    awbPrints.add(print);
                } else {
                    AwbPrint newPrint = new AwbPrint();
                    newPrint.setAwbPrintId("hawb-" + afShipperLetter.getSlId());
                    newPrint.setAwbStatus("待制单");
                    newPrint.setAwbType(1);
                    newPrint.setSlId(afShipperLetter.getSlId());
                    newPrint.setHawbNumber(afShipperLetter.getHawbNumber());
                    newPrint.setDepartureStation(record.getDepartureStation());
                    newPrint.setArrivalStation(record.getArrivalStation());
                    newPrint.setFlightNumber(record.getFlightNumber());
                    newPrint.setFlightDate(record.getFlightDate());
                    newPrint.setAwbUuid(record.getAwbUuid());
                    newPrint.setAwbPieces(afShipperLetter.getPlanPieces());
                    newPrint.setAwbGrossWeight(afShipperLetter.getPlanWeight());

                    LambdaQueryWrapper<AfOrder> afOrderLambdaQueryWrapper = Wrappers.<AfOrder>lambdaQuery();
                    afOrderLambdaQueryWrapper.eq(AfOrder::getOrderId, afShipperLetter.getOrderId()).eq(AfOrder::getOrgId, SecurityUtils.getUser().getOrgId());
                    AfOrder order = afOrderService.getOne(afOrderLambdaQueryWrapper);
                    if (order != null) {
                        newPrint.setAwbChargeWeight(new BigDecimal(order.getPlanChargeWeight()));
                        newPrint.setAwbVolume(new BigDecimal(order.getPlanVolume()));
                    }

                    awbPrints.add(newPrint);
                }
            });

            record.setHawbs(awbPrints);
            record.setAwbPrintIdCopy(StrUtil.isNotBlank(record.getAwbPrintId()) ? Integer.parseInt(record.getAwbPrintId()) : null);
            record.setAwbPrintId("mawb-" + record.getAwbId());
            record.setHawbNumber(record.getAwbNumber());
            record.setAwbType(0);
        });

        return iPage;
    }

    @Override
    public AwbPrint callAfPAwbPrint(AfPAwbPrintProcedure afPAwbPrintProcedure) {
        afPAwbPrintProcedure.setOrgId(SecurityUtils.getUser().getOrgId());

        AwbPrint awbPrint = baseMapper.callAfPAwbPrint(afPAwbPrintProcedure);
        //如果是主单制作，则查询订单是否存在收发货人信息
        if ("CREATE_MAWB".equals(afPAwbPrintProcedure.getAwbPrintType())) {
            AfOrderShipperConsignee afOrderShipperConsignee1 = baseMapper.getShipperConsigneeInfo(SecurityUtils.getUser().getOrgId(), afPAwbPrintProcedure.getOrderUuid(), 0);//获取发货人信息
            AfOrderShipperConsignee afOrderShipperConsignee2 = baseMapper.getShipperConsigneeInfo(SecurityUtils.getUser().getOrgId(), afPAwbPrintProcedure.getOrderUuid(), 1);//获取收货人信息
            if (afOrderShipperConsignee1 != null) {
                awbPrint.setAfOrderShipperConsignee1(afOrderShipperConsignee1);
            }
            if (afOrderShipperConsignee2 != null) {
                awbPrint.setAfOrderShipperConsignee2(afOrderShipperConsignee2);
            }
        }
        return awbPrint;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void getHawbInfo(String orderUuid) {
        //调用存储过程获取分单信息
        AfPAwbPrintProcedure afPAwbPrintProcedure = new AfPAwbPrintProcedure();
        afPAwbPrintProcedure.setAwbPrintType("CREATE_HAWB");
        afPAwbPrintProcedure.setOrderUuid(orderUuid);
        afPAwbPrintProcedure.setOrgId(SecurityUtils.getUser().getOrgId());
        AwbPrint awbPrint = baseMapper.callAfPAwbPrint(afPAwbPrintProcedure);

        //收发货人信息、通知人信息、唛头等属于主单的信息，不写入
        awbPrint.setConsigneeId(null);
        awbPrint.setConsigneeAddress("");
        awbPrint.setShipperId(null);
        awbPrint.setShipperAddress("");
        awbPrint.setRemarkMark("");
        awbPrint.setTransitStation("");

        awbPrint.setOrgId(SecurityUtils.getUser().getOrgId());
        awbPrint.setCreateTime(LocalDateTime.now());
        awbPrint.setCreatorId(SecurityUtils.getUser().getId());
        awbPrint.setCreatorName(SecurityUtils.getUser().getUserCname() + " " + SecurityUtils.getUser().getUserEmail());

        awbPrint.setEditorId(SecurityUtils.getUser().getId());
        awbPrint.setEditorName(SecurityUtils.getUser().getUserCname() + " " + SecurityUtils.getUser().getUserEmail());
        awbPrint.setEditTime(LocalDateTime.now());
        awbPrint.setAwbStatus("已暂存");
        awbPrint.setAwbType(1);


        //根据orderUuid从af_shipper_letter表获取分单信息
        LambdaQueryWrapper<AfShipperLetter> shipperLetterWrapper = Wrappers.<AfShipperLetter>lambdaQuery();
        shipperLetterWrapper.eq(AfShipperLetter::getOrgId, SecurityUtils.getUser().getOrgId()).eq(AfShipperLetter::getSlType, "HAWB").eq(AfShipperLetter::getOrderUuid, orderUuid);
        List<AfShipperLetter> shipperLetterList = afShipperLetterService.list(shipperLetterWrapper);
        if (shipperLetterList != null && shipperLetterList.size() > 0) {
            Integer index = 1;
            for (int i = 0; i < shipperLetterList.size(); i++) {
                awbPrint.setSlId(shipperLetterList.get(i).getSlId());
                if ("".equals(shipperLetterList.get(i).getHawbNumber())) {
                    awbPrint.setHawbNumber("UNKNOWN HAWB " + index);
                    index++;
                } else {
                    awbPrint.setHawbNumber(shipperLetterList.get(i).getHawbNumber());
                }
                awbPrint.setAwbPieces(shipperLetterList.get(i).getPlanPieces());
                awbPrint.setAwbGrossWeight(shipperLetterList.get(i).getPlanWeight());
                awbPrint.setAwbVolume(shipperLetterList.get(i).getPlanVolume());
                awbPrint.setPayMethod(shipperLetterList.get(i).getPaymentMethod());
                awbPrint.setHandingInformation(shipperLetterList.get(i).getHandlingInfo());
                awbPrint.setGoodsDescription(shipperLetterList.get(i).getGoodsNameCn());
                awbPrint.setRemarkMark(shipperLetterList.get(i).getShippingMarks());

                //临时解决
                String destination = "";
                if (StrUtil.isNotBlank(shipperLetterList.get(i).getArrivalStation()) && StrUtil.isNotBlank(shipperLetterList.get(i).getTransitStation())) {
                    awbPrint.setArrivalStation(shipperLetterList.get(i).getTransitStation());
                    awbPrint.setTransitStation(shipperLetterList.get(i).getArrivalStation());
                    destination = shipperLetterList.get(i).getArrivalStation();
                } else if (StrUtil.isNotBlank(shipperLetterList.get(i).getArrivalStation())) {
                    awbPrint.setArrivalStation(shipperLetterList.get(i).getArrivalStation());
                    destination = shipperLetterList.get(i).getArrivalStation();
                } else if (StrUtil.isNotBlank(shipperLetterList.get(i).getTransitStation())) {
                    awbPrint.setArrivalStation(shipperLetterList.get(i).getTransitStation());
                    destination = shipperLetterList.get(i).getTransitStation();
                }


                //设置目的港全称
                if (StrUtil.isNotBlank(destination)) {
                    Airport apCode = airportService.getAirportCityNameENByApCode(destination);
                    if (apCode != null) {
                        awbPrint.setArrivalStationName(StrUtil.isBlank(apCode.getCityNameEn()) ? "" : apCode.getCityNameEn().toUpperCase());
                    } else {
                        awbPrint.setArrivalStationName("");
                    }
                } else {
                    awbPrint.setArrivalStationName("");
                }

                //设置收发货人
                LambdaQueryWrapper<AfOrderShipperConsignee> afOrderShipperWrapper = Wrappers.<AfOrderShipperConsignee>lambdaQuery();
                afOrderShipperWrapper.eq(AfOrderShipperConsignee::getOrgId, SecurityUtils.getUser().getOrgId()).eq(AfOrderShipperConsignee::getSlId, shipperLetterList.get(i).getSlId()).eq(AfOrderShipperConsignee::getScType, 0);
                AfOrderShipperConsignee shipper = afOrderShipperConsigneeService.getOne(afOrderShipperWrapper);
                awbPrint.setShipperAddress(shipper.getScPrintRemark());

                LambdaQueryWrapper<AfOrderShipperConsignee> afOrderConsigneeWrapper = Wrappers.<AfOrderShipperConsignee>lambdaQuery();
                afOrderConsigneeWrapper.eq(AfOrderShipperConsignee::getOrgId, SecurityUtils.getUser().getOrgId()).eq(AfOrderShipperConsignee::getSlId, shipperLetterList.get(i).getSlId()).eq(AfOrderShipperConsignee::getScType, 1);
                AfOrderShipperConsignee consignee = afOrderShipperConsigneeService.getOne(afOrderConsigneeWrapper);
                awbPrint.setConsigneeAddress(consignee.getScPrintRemark());

                //设置尺寸标准格式
                if (StrUtil.isNotBlank(shipperLetterList.get(i).getPlanDimensions())) {
                    StringBuffer size = new StringBuffer();
                    Arrays.asList(shipperLetterList.get(i).getPlanDimensions().split(";")).stream().forEach(awbSize -> {
                        if (StrUtil.isBlank(size.toString())) {
                            size.append(awbSize.split("\\*")[0] + "*" + awbSize.split("\\*")[1] + "*" + awbSize.split("\\*")[2].split("/")[0] + " CM/" + awbSize.split("\\*")[2].split("/")[1]);
                        } else {
                            size.append("\n").append(awbSize.split("\\*")[0] + "*" + awbSize.split("\\*")[1] + "*" + awbSize.split("\\*")[2].split("/")[0] + " CM/" + awbSize.split("\\*")[2].split("/")[1]);
                        }
                    });
                    awbPrint.setAwbSizes(size.toString());
                } else {
                    awbPrint.setAwbSizes("");
                }
                baseMapper.insert(awbPrint);
            }
        } else {
            throw new RuntimeException("该订单为直单，无可用的分单信息");
        }
    }

    @Override
    public Map<String, Object> sendAmsCheckHasSend(Integer awbPrintId) {
        Map<String,Object> map = new HashMap();
        AwbPrint print = awbPrintMapper.getAwbPrintById(SecurityUtils.getUser().getOrgId(), awbPrintId);
        map.put("awbPrint",print);
        if (print.getApiStatus()!=null && print.getApiStatus()!=2) {//已经发送过且成功了
            map.put("hasSend",true);
        } else {//未发送过或发送过未成功
            map.put("hasSend",false);
        }
        return map;
    }

    @Override
    public String getAmsDataCheck(String type, String awbNumber, String letterId) {
        //校验必填信息
        return this.baseMapper.getAmsDataCheck(type, awbNumber, letterId, SecurityUtils.getUser().getId());
    }

    private OrgInterfaceVo getShippingBillConfig(Integer orgId, String apiType) {
        MessageInfo<OrgInterfaceVo> messageInfo = remoteServiceToHRS.getShippingBillConfig(orgId, apiType);
        if (null == messageInfo || messageInfo.getCode() != 0) {
            throw new RuntimeException("运单配置信息获取失败");
        }
        OrgInterfaceVo orgInterfaceVo = messageInfo.getData();
        Assert.notNull(orgInterfaceVo, "未获取到运单配置信息");
        return orgInterfaceVo;
    }
    @Override
    public Map<String, Object> sendAmsData(String type, String awbNumber, String letterId) {

        boolean flag=true;
        Map<String,Object> map = new HashMap();
        EUserDetails user = SecurityUtils.getUser();
        String apiType = APIType.AE_DZ_AWB;
        OrgInterfaceVo config = getShippingBillConfig(user.getOrgId(), apiType);
        //获取总单数据XMl
        String mawbXML = "";
        //获取分单数据xml
        String hawbXML = "";
        if(!"hawb".equals(type)){//只主单或所有
            mawbXML = orderMapper.getNewMAWBXML(awbNumber, user.getId(), apiType);;
        }
        if("all".equals(type) || StringUtils.isNotEmpty(letterId)){//awbUuid
            hawbXML = orderMapper.getNewHAWBXML(awbNumber, letterId, user.getId(), apiType);
        }
        StringBuilder builder = new StringBuilder();
        builder.append("<data>");
        if (StringUtils.isNotBlank(mawbXML)) {
            mawbXML=mawbXML.substring(0,mawbXML.indexOf("<CargoTerminal>"))+"<IsOverride>1</IsOverride>"+  mawbXML.substring(mawbXML.indexOf("<CargoTerminal>"));
            builder.append(mawbXML);
        }
        if (StringUtils.isNotBlank(hawbXML)) {
            if (mawbXML.length()>0) {
                builder= new StringBuilder(builder.toString().replace("</AirwayBill>",
                        "<ConsolidationList>"+hawbXML.substring(hawbXML.indexOf("<ConsolidationList>")+19, hawbXML.indexOf("</ConsolidationList>"))+"</ConsolidationList>"));
                builder.append("</AirwayBill>");
            } else {
                hawbXML=hawbXML.substring(0,hawbXML.indexOf("<CargoTerminal>"))+"<IsOverride>1</IsOverride>"+  hawbXML.substring(hawbXML.indexOf("<CargoTerminal>"));
                builder.append(hawbXML);
            }
        }

        builder.append("</data>");

        ResponseEntity<String> responseEntity = RemoteSendUtils.sendToThirdUrl(config,builder.toString());
        String objStr = responseEntity.getBody();
        JSONObject jsonO = JSONObject.parseObject(objStr);
        String message  = jsonO.getString("messageInfo");
        map.put("status","success");
        map.put("message","发送AMS成功");

        if (!HttpStatus.OK.equals(responseEntity.getStatusCode())) {
            map.put("message","发送AMS第三方接口调用异常：" + message);
            map.put("status","error");
            flag = false;
        }
        if("01".equals(jsonO.getString("code"))) {
            LogBean logBean = new LogBean();
            logBean.setHasMwb(type);
            logBean.setAwbNumber(awbNumber);
            logBean.setLetterIds(letterId+"");
            logBean.setPageFunction("发送AMS");
            logBean.setLogRemarkLarge(builder.toString());
            logBean.setPageName("AE订单");
            logBean.setBusinessScope("AE");
            logService.saveLog(logBean);

            //更新制单表发送状态 manifest_status
            this.baseMapper.updateAwbPrintStatus(type,awbNumber,letterId,flag);
        }else{
            map.put("message","发送ams 报文异常：" + message);
            map.put("status","exception");
            flag = false;
        }
        return map;
    }
//    private Boolean insertLogAfterSendAms(LogBean logBean) {
//        try{
//            String uuid = logBean.getAwbUuid();
//            AfOrder bean = orderMapper.getByAwbUuid(SecurityUtils.getUser().getOrgId(), uuid);
//            logBean.setPageName("AE订单");
//            logBean.setBusinessScope("AE");
//
//            logBean.setAwbNumber(bean.getAwbNumber());
//            logService.saveLog(logBean);
//
//            return true;
//        }catch (Exception e){
//            e.printStackTrace();
//            return false;
//        }
//
//    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String awbDownloadWithPDF(AfPAwbPrintProcedure afPAwbPrintProcedure) {
        if (afPAwbPrintProcedure.getAwbPrint() != null) {
            modify(afPAwbPrintProcedure.getAwbPrint());
        }
        if (SecurityUtils.getUser() == null) {
            //兼容外部访问
            AfOrder order = afOrderService.getOrderByUUID(afPAwbPrintProcedure.getOrderUuid());
            afPAwbPrintProcedure.setOrgId(order.getOrgId());
        } else {
            afPAwbPrintProcedure.setOrgId(SecurityUtils.getUser().getOrgId());
        }
        AfPAwbPrintForMawbPrintProcedure afPAwbPrintForMawbPrintProcedure = baseMapper.callAfPAwbPrintForMawbPrint(afPAwbPrintProcedure);

        //寻找模板，航司是否配置打印模板
        String templateFilePath = "";
        boolean flag = false;
        if (StrUtil.isNotBlank(afPAwbPrintForMawbPrintProcedure.getTxtAWBPrefix())) {

            LambdaQueryWrapper<Carrier> carrierWrapper = Wrappers.<Carrier>lambdaQuery();

            carrierWrapper.eq(Carrier::getCarrierPrefix, afPAwbPrintForMawbPrintProcedure.getTxtAWBPrefix());
            Carrier one = carrierService.getOne(carrierWrapper);
            if (one != null) {
                if (afPAwbPrintProcedure.getPrintType() == 1) {
                    if (StrUtil.isNotBlank(one.getCarrierMawbModOver())) {
                        String url = one.getCarrierMawbModOver().split(",")[1];
                        flag = true;
                        downloadFile(url, PDFUtils.filePath + "/PDFtemplate/temp/" + url.split("/")[url.split("/").length - 1]);
                        templateFilePath = PDFUtils.filePath + "/PDFtemplate/temp/" + url.split("/")[url.split("/").length - 1];
                    }
                } else if (afPAwbPrintProcedure.getPrintType() == 2) {
                    if (StrUtil.isNotBlank(one.getCarrierMawbModFormat())) {
                        String url = one.getCarrierMawbModFormat().split(",")[1];
                        flag = true;
                        downloadFile(url, PDFUtils.filePath + "/PDFtemplate/temp/" + url.split("/")[url.split("/").length - 1]);
                        templateFilePath = PDFUtils.filePath + "/PDFtemplate/temp/" + url.split("/")[url.split("/").length - 1];
                    }
                }
            }

        }
        if (!flag) {
            if (afPAwbPrintProcedure.getPrintType() == 1) {
                templateFilePath = PDFUtils.filePath + "/PDFtemplate/MAWB.pdf";
            } else if (afPAwbPrintProcedure.getPrintType() == 2) {
                templateFilePath = PDFUtils.filePath + "/PDFtemplate/MAWB-FORMAT.pdf";
            }
        }
        String newFilePath = fillTemplate(afPAwbPrintForMawbPrintProcedure, templateFilePath, PDFUtils.filePath + "/PDFtemplate/temp/mawb", PDFUtils.filePath);
        return newFilePath;

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String hawbDownloadWithPDF(AfPAwbPrintProcedure afPAwbPrintProcedure) {
        if (afPAwbPrintProcedure.getAwbPrint() != null) {
            modify(afPAwbPrintProcedure.getAwbPrint());
        }
        if (SecurityUtils.getUser() == null) {
            //兼容外部访问
            AfOrder order = afOrderService.getOrderByUUID(afPAwbPrintProcedure.getOrderUuid());
            afPAwbPrintProcedure.setOrgId(order.getOrgId());
        } else {
            afPAwbPrintProcedure.setOrgId(SecurityUtils.getUser().getOrgId());
        }
        AfPAwbPrintForMawbPrintProcedure afPAwbPrintForMawbPrintProcedure = baseMapper.callAfPAwbPrintForHawbPrint(afPAwbPrintProcedure);
        LambdaQueryWrapper<AwbNumber> awbNumberWrapper = Wrappers.<AwbNumber>lambdaQuery();
        awbNumberWrapper.eq(AwbNumber::getAwbUuid, afPAwbPrintProcedure.getAwbUuid());
        AwbNumber awbNumber = awbNumberService.getOne(awbNumberWrapper);
        //寻找模板，航司是否配置打印模板
        String templateFilePath = "";
        boolean flag = false;
        if (awbNumber != null && StrUtil.isNotBlank(awbNumber.getAwbNumber())) {

            LambdaQueryWrapper<Carrier> carrierWrapper = Wrappers.<Carrier>lambdaQuery();

            carrierWrapper.eq(Carrier::getCarrierPrefix, awbNumber.getAwbNumber().split("-")[0]);
            Carrier one = carrierService.getOne(carrierWrapper);
            if (one != null) {
                if (afPAwbPrintProcedure.getPrintType() == 1) {
                    if (StrUtil.isNotBlank(one.getCarrierHawbModOver())) {
                        String url = one.getCarrierHawbModOver().split(",")[1];
                        flag = true;
                        downloadFile(url, PDFUtils.filePath + "/PDFtemplate/temp/" + url.split("/")[url.split("/").length - 1]);
                        templateFilePath = PDFUtils.filePath + "/PDFtemplate/temp/" + url.split("/")[url.split("/").length - 1];
                    }
                } else if (afPAwbPrintProcedure.getPrintType() == 2) {
                    if (StrUtil.isNotBlank(one.getCarrierHawbModFormat())) {
                        String url = one.getCarrierHawbModFormat().split(",")[1];
                        flag = true;
                        downloadFile(url, PDFUtils.filePath + "/PDFtemplate/temp/" + url.split("/")[url.split("/").length - 1]);
                        templateFilePath = PDFUtils.filePath + "/PDFtemplate/temp/" + url.split("/")[url.split("/").length - 1];
                    }
                }
            }

        }
        if (!flag) {
            if (afPAwbPrintProcedure.getPrintType() == 1) {
                templateFilePath = PDFUtils.filePath + "/PDFtemplate/HAWB.pdf";
            } else if (afPAwbPrintProcedure.getPrintType() == 2) {
                templateFilePath = PDFUtils.filePath + "/PDFtemplate/HAWB-FORMAT.pdf";
            }
        }
        String newFilePath = fillTemplate(afPAwbPrintForMawbPrintProcedure, templateFilePath, PDFUtils.filePath + "/PDFtemplate/temp/hawb", PDFUtils.filePath);
        return newFilePath;
    }

    public static boolean downloadFile(String fileURL, String fileName) {
        try {
            String path = fileName.substring(0, fileName.lastIndexOf("/"));
            File file = new File(path);
            if (!file.exists()) {
                file.mkdirs();
            }
            URL url = new URL(fileURL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            DataInputStream in = new DataInputStream(connection.getInputStream());
            DataOutputStream out = new DataOutputStream(new FileOutputStream(fileName));
            byte[] buffer = new byte[4096];
            int count = 0;
            while ((count = in.read(buffer)) > 0) {
                out.write(buffer, 0, count);
            }
            out.close();
            in.close();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public HttpServletResponse download(String path, HttpServletResponse response) {
        try {
            // path是指欲下载的文件的路径。
            File file = new File(path);
            //如果文件不存在
            if (!file.exists()) {
                throw new RuntimeException(path + "文件不存在");
            }
            // 取得文件名。
            String filename = file.getName();
            // 取得文件的后缀名。
            String ext = filename.substring(filename.lastIndexOf(".") + 1).toUpperCase();

            // 以流的形式下载文件。
            InputStream fis = new BufferedInputStream(new FileInputStream(path));
            byte[] buffer = new byte[fis.available()];
            fis.read(buffer);
            fis.close();
            response.reset();
            // 设置response的Header
            response.setHeader("Content-Disposition", "attachment; filename=" + filename);
            response.addHeader("Content-Length", "" + file.length());
            response.setContentType("application/octet-stream");
//            response.setContentType("application/pdf;charset=utf-8");
            response.setCharacterEncoding("UTF-8");
            response.setContentLength((int) file.length());
            OutputStream toClient = new BufferedOutputStream(response.getOutputStream());
            toClient.write(buffer);
            toClient.flush();
            toClient.close();
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex.getMessage());
        }
        return response;
    }

    public static String makeFileName(String filename) {  //2.jpg
        //为防止文件覆盖的现象发生，要为上传文件产生一个唯一的文件名
        return filename + "_" + UUID.randomUUID().toString() + ".pdf";
    }

    public static String makePath(String filename, String savePath) {
        //得到文件名的hashCode的值，得到的就是filename这个字符串对象在内存中的地址
        String hashDir = makeHashDir(filename);
        //构造新的保存目录
        String dir = savePath + hashDir;  //upload\2\3  upload\3\5
        //File既可以代表文件也可以代表目录
        File file = new File(dir);
        //如果目录不存在
        if (!file.exists()) {
            //创建目录
            file.mkdirs();
        }
        return dir;
    }

    public static String makeHashDir(String filename) {
        //得到文件名的hashCode的值，得到的就是filename这个字符串对象在内存中的地址
        int hashcode = filename.hashCode();
        int dir1 = hashcode & 0xf;  //0--15
        int dir2 = (hashcode & 0xf0) >> 4;  //0-15
        //构造新的保存目录
        String hashDir = "/" + dir1 + "/" + dir2;  //upload/2/3  upload/3/5
        return hashDir;
    }

    public static String fillTemplate(AfPAwbPrintForMawbPrintProcedure afPAwbPrintForMawbPrintProcedure, String templateFilePath, String savePath, String replacePath) {
        String saveFilename = makeFileName(afPAwbPrintForMawbPrintProcedure.getTxtAWBTop());
        //得到文件的保存目录
        String newPDFPath = makePath(saveFilename, savePath) + "/" + saveFilename;

        try {

            Map<String, String> valueData = new HashMap<>();
            //月份全部大写

            if (afPAwbPrintForMawbPrintProcedure != null) {
                valueData.put("txtAWBTop", afPAwbPrintForMawbPrintProcedure.getTxtAWBTop() == null ? "" : afPAwbPrintForMawbPrintProcedure.getTxtAWBTop());
                valueData.put("txtAWBBottom", afPAwbPrintForMawbPrintProcedure.getTxtAWBBottom() == null ? "" : afPAwbPrintForMawbPrintProcedure.getTxtAWBBottom());
                valueData.put("txtAWB_Prefix", afPAwbPrintForMawbPrintProcedure.getTxtAWBPrefix() == null ? "" : afPAwbPrintForMawbPrintProcedure.getTxtAWBPrefix());//主单号前三位
                valueData.put("txtOrigin_Code", afPAwbPrintForMawbPrintProcedure.getTxtOriginCode() == null ? "" : afPAwbPrintForMawbPrintProcedure.getTxtOriginCode());//始发港
                valueData.put("txtAWB_Suffix", afPAwbPrintForMawbPrintProcedure.getTxtAWBSuffix() == null ? "" : afPAwbPrintForMawbPrintProcedure.getTxtAWBSuffix());//主单号后八位
                valueData.put("txtCarrier_Name", afPAwbPrintForMawbPrintProcedure.getTxtCarrierName() == null ? "" : afPAwbPrintForMawbPrintProcedure.getTxtCarrierName());//航空公司
                valueData.put("txtShipper_Name", afPAwbPrintForMawbPrintProcedure.getTxtShipperName() == null ? "" : afPAwbPrintForMawbPrintProcedure.getTxtShipperName());//发货人
                valueData.put("txtConsignee_Name", afPAwbPrintForMawbPrintProcedure.getTxtConsigneeName() == null ? "" : afPAwbPrintForMawbPrintProcedure.getTxtConsigneeName());//收货人
                valueData.put("txtAgent_Name", afPAwbPrintForMawbPrintProcedure.getTxtAgentName() == null ? "" : afPAwbPrintForMawbPrintProcedure.getTxtAgentName());//代理名称
                valueData.put("txtAgent_Iata_Code", afPAwbPrintForMawbPrintProcedure.getTxtAgentIataCode() == null ? "" : afPAwbPrintForMawbPrintProcedure.getTxtAgentIataCode());//代理IATA代码
                valueData.put("txtAgent_Account", afPAwbPrintForMawbPrintProcedure.getTxtAgentAccount() == null ? "" : afPAwbPrintForMawbPrintProcedure.getTxtAgentAccount());//代理Account代码
                valueData.put("txtDeparture", afPAwbPrintForMawbPrintProcedure.getTxtDeparture() == null ? "" : afPAwbPrintForMawbPrintProcedure.getTxtDeparture());//始发港英文全称
                valueData.put("txtTo1", afPAwbPrintForMawbPrintProcedure.getTxtTo1() == null ? "" : afPAwbPrintForMawbPrintProcedure.getTxtTo1());//
                valueData.put("txtFlight1_Carr", afPAwbPrintForMawbPrintProcedure.getTxtFlight1Carr() == null ? "" : afPAwbPrintForMawbPrintProcedure.getTxtFlight1Carr());//第一承运人
                valueData.put("txtTo2", afPAwbPrintForMawbPrintProcedure.getTxtTo2() == null ? "" : afPAwbPrintForMawbPrintProcedure.getTxtTo2());//中转港1
                valueData.put("txtBy2", afPAwbPrintForMawbPrintProcedure.getTxtBy2() == null ? "" : afPAwbPrintForMawbPrintProcedure.getTxtBy2());//中转港1航班两字码
                valueData.put("txtTo3", afPAwbPrintForMawbPrintProcedure.getTxtTo3() == null ? "" : afPAwbPrintForMawbPrintProcedure.getTxtTo3());//中转港2
                valueData.put("txtBy3", afPAwbPrintForMawbPrintProcedure.getTxtBy3() == null ? "" : afPAwbPrintForMawbPrintProcedure.getTxtBy3());//中转港2航班两字码
                valueData.put("txtDestination", afPAwbPrintForMawbPrintProcedure.getTxtDestination() == null ? "" : afPAwbPrintForMawbPrintProcedure.getTxtDestination());//目的港
                valueData.put("txtAccountingInfo_Text", afPAwbPrintForMawbPrintProcedure.getTxtAccountingInfoText() == null ? "" : afPAwbPrintForMawbPrintProcedure.getTxtAccountingInfoText());//AccountingInfomation
                valueData.put("txtFlight2_Carr", afPAwbPrintForMawbPrintProcedure.getTxtFlight2Carr() == null ? "" : afPAwbPrintForMawbPrintProcedure.getTxtFlight2Carr());//航班号
                valueData.put("txtFlight3_Carr", afPAwbPrintForMawbPrintProcedure.getTxtFlight3Carr() == null ? "" : afPAwbPrintForMawbPrintProcedure.getTxtFlight3Carr());//航班日期
                valueData.put("txtDefault_CurrCode", afPAwbPrintForMawbPrintProcedure.getTxtDefaultCurrCode() == null ? "" : afPAwbPrintForMawbPrintProcedure.getTxtDefaultCurrCode());//币种
                valueData.put("txtPC_WtgPP", afPAwbPrintForMawbPrintProcedure.getTxtPCWtgPP() == null ? "" : afPAwbPrintForMawbPrintProcedure.getTxtPCWtgPP());//到付预付运费方式
                valueData.put("txtPC_OthPP", afPAwbPrintForMawbPrintProcedure.getTxtPCOthPP() == null ? "" : afPAwbPrintForMawbPrintProcedure.getTxtPCOthPP());//到付预付杂费方式
                valueData.put("txtHandlingInfo_Text", afPAwbPrintForMawbPrintProcedure.getTxtHandlingInfoText() == null ? "" : afPAwbPrintForMawbPrintProcedure.getTxtHandlingInfoText());//
                valueData.put("txtGoods_Desc1", afPAwbPrintForMawbPrintProcedure.getTxtGoodsDesc1() == null ? "" : afPAwbPrintForMawbPrintProcedure.getTxtGoodsDesc1());//品名
                valueData.put("txtGoods_Volume", afPAwbPrintForMawbPrintProcedure.getTxtGoodsVolume() == null ? "" : afPAwbPrintForMawbPrintProcedure.getTxtGoodsVolume());//体积
                valueData.put("txtRCP_Pcs1", afPAwbPrintForMawbPrintProcedure.getTxtRCPPcs1() == null ? "" : afPAwbPrintForMawbPrintProcedure.getTxtRCPPcs1());//件数
                valueData.put("txtTotal_Rcp", afPAwbPrintForMawbPrintProcedure.getTxtTotalRcp() == null ? "" : afPAwbPrintForMawbPrintProcedure.getTxtTotalRcp());//小件数
                valueData.put("txtGross_Wtg1", afPAwbPrintForMawbPrintProcedure.getTxtGrossWtg1() == null ? "" : afPAwbPrintForMawbPrintProcedure.getTxtGrossWtg1());//毛重
                valueData.put("txtDefault_WgtCode1", afPAwbPrintForMawbPrintProcedure.getTxtDefaultWgtCode1() == null ? "" : afPAwbPrintForMawbPrintProcedure.getTxtDefaultWgtCode1());//重量单位
                valueData.put("txtChg_Wtg1", afPAwbPrintForMawbPrintProcedure.getTxtChgWtg1() == null ? "" : afPAwbPrintForMawbPrintProcedure.getTxtChgWtg1());//计重
                valueData.put("txtRate_Class1", afPAwbPrintForMawbPrintProcedure.getTxtRateClass1() == null ? "" : afPAwbPrintForMawbPrintProcedure.getTxtRateClass1());//运价等级
                valueData.put("txtRate_Chg_Dis1", afPAwbPrintForMawbPrintProcedure.getTxtRateChgDis1() == null ? "" : afPAwbPrintForMawbPrintProcedure.getTxtRateChgDis1());//费率
                valueData.put("txtTotal_Chg1", afPAwbPrintForMawbPrintProcedure.getTxtTotalChg1() == null ? "" : afPAwbPrintForMawbPrintProcedure.getTxtTotalChg1());//运费合计
                //TOTO 暂时没有
                valueData.put("txtGoods_Size", afPAwbPrintForMawbPrintProcedure.getTxtGoodsSize() == null ? "" : afPAwbPrintForMawbPrintProcedure.getTxtGoodsSize());//唛头
                valueData.put("txtTotal_Wtg_Chg_PP", afPAwbPrintForMawbPrintProcedure.getTxtTotalWtgChgPP() == null ? "" : afPAwbPrintForMawbPrintProcedure.getTxtTotalWtgChgPP());//预付-重量价值费
                valueData.put("txtTotal_Wtg_Chg_CC", afPAwbPrintForMawbPrintProcedure.getTxtTotalWtgChgCC() == null ? "" : afPAwbPrintForMawbPrintProcedure.getTxtTotalWtgChgCC());//到付-重量价值费
                valueData.put("txtChg_Due_Carr_PP", afPAwbPrintForMawbPrintProcedure.getTxtChgDueCarrPP() == null ? "" : afPAwbPrintForMawbPrintProcedure.getTxtChgDueCarrPP());//预付杂费金额
                valueData.put("txtChg_Due_Carr_CC", afPAwbPrintForMawbPrintProcedure.getTxtChgDueCarrCC() == null ? "" : afPAwbPrintForMawbPrintProcedure.getTxtChgDueCarrCC());//到付杂费金额
                valueData.put("txtShipperRemark1", afPAwbPrintForMawbPrintProcedure.getTxtShipperRemark1() == null ? "" : afPAwbPrintForMawbPrintProcedure.getTxtShipperRemark1());//发货人或代理签字
                valueData.put("txtShipperRemark2", afPAwbPrintForMawbPrintProcedure.getTxtShipperRemark2() == null ? "" : afPAwbPrintForMawbPrintProcedure.getTxtShipperRemark2());//承运代理公司名称
                valueData.put("txtTotalPP", afPAwbPrintForMawbPrintProcedure.getTxtTotalPP() == null ? "" : afPAwbPrintForMawbPrintProcedure.getTxtTotalPP());//预付总金额
                valueData.put("txtTotalCC", afPAwbPrintForMawbPrintProcedure.getTxtTotalCC() == null ? "" : afPAwbPrintForMawbPrintProcedure.getTxtTotalCC());//到付总金额
                valueData.put("txtOtherCharges1", afPAwbPrintForMawbPrintProcedure.getTxtOtherCharges1() == null ? "" : afPAwbPrintForMawbPrintProcedure.getTxtOtherCharges1());//其他杂费

                //补全之前没显示的内容            2018年6月13日15:09:32 by fjh
                valueData.put("txtAWBBarCode", afPAwbPrintForMawbPrintProcedure.getTxtAWBBarCode() == null ? "" : afPAwbPrintForMawbPrintProcedure.getTxtAWBBarCode());//条形码
                valueData.put("txtChgsCode", afPAwbPrintForMawbPrintProcedure.getTxtChgsCode() == null ? "" : afPAwbPrintForMawbPrintProcedure.getTxtChgsCode());//CHGS代码
                valueData.put("txtCVD_Carriage", afPAwbPrintForMawbPrintProcedure.getTxtCVDCarriage() == null ? "" : afPAwbPrintForMawbPrintProcedure.getTxtCVDCarriage());//声明价值
                valueData.put("txtCVD_Custom", afPAwbPrintForMawbPrintProcedure.getTxtCVDCustom() == null ? "" : afPAwbPrintForMawbPrintProcedure.getTxtCVDCustom());//海关声明价值
                valueData.put("txtCVD_Insurance", afPAwbPrintForMawbPrintProcedure.getTxtCVDInsurance() == null ? "" : afPAwbPrintForMawbPrintProcedure.getTxtCVDInsurance());//保险价值
                valueData.put("txtItem_Num1", afPAwbPrintForMawbPrintProcedure.getTxtItemNum1() == null ? "" : afPAwbPrintForMawbPrintProcedure.getTxtItemNum1());//商品名编号
                valueData.put("txtVal_Chg_PP", afPAwbPrintForMawbPrintProcedure.getTxtValChgPP() == null ? "" : afPAwbPrintForMawbPrintProcedure.getTxtValChgPP());//预付-声明价值费
                valueData.put("txtVal_Chg_CC", afPAwbPrintForMawbPrintProcedure.getTxtValChgCC() == null ? "" : afPAwbPrintForMawbPrintProcedure.getTxtValChgCC());//到付-声明价值费
                valueData.put("txtTax_Chg_PP", afPAwbPrintForMawbPrintProcedure.getTxtTaxChgPP() == null ? "" : afPAwbPrintForMawbPrintProcedure.getTxtTaxChgPP());//预付-税款
                valueData.put("txtTax_Chg_CC", afPAwbPrintForMawbPrintProcedure.getTxtTaxChgCC() == null ? "" : afPAwbPrintForMawbPrintProcedure.getTxtTaxChgCC());//到付-税款
                valueData.put("txtChg_Due_Agt_PP", afPAwbPrintForMawbPrintProcedure.getTxtChgDueAgtPP() == null ? "" : afPAwbPrintForMawbPrintProcedure.getTxtChgDueAgtPP());//预付-代理人的其它费用总额
                valueData.put("txtChg_Due_Agt_CC", afPAwbPrintForMawbPrintProcedure.getTxtChgDueAgtCC() == null ? "" : afPAwbPrintForMawbPrintProcedure.getTxtChgDueAgtCC());//到付-代理人的其它费用总额
                valueData.put("txtCCR", afPAwbPrintForMawbPrintProcedure.getTxtCCR() == null ? "" : afPAwbPrintForMawbPrintProcedure.getTxtCCR());//汇率
                valueData.put("txtCDC", afPAwbPrintForMawbPrintProcedure.getTxtCDC() == null ? "" : afPAwbPrintForMawbPrintProcedure.getTxtCDC());//到付费用(目的国货币)
                valueData.put("txtChg_Dest", afPAwbPrintForMawbPrintProcedure.getTxtChgDest() == null ? "" : afPAwbPrintForMawbPrintProcedure.getTxtChgDest());//目的国收费
                valueData.put("txtTot_Coll", afPAwbPrintForMawbPrintProcedure.getTxtTotColl() == null ? "" : afPAwbPrintForMawbPrintProcedure.getTxtTotColl());//到付费用总计
            }

            //pdf填充数据以及下载
            loadPDF(templateFilePath, newPDFPath, valueData, false);

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Exception : " + e.getMessage());
        }
        return newPDFPath.replace(replacePath, "");
    }

    @SneakyThrows
    public static File loadPDF(String templatePath, String newPDFPath, Map<String, String> valueData, boolean flage) {
        File file = new File(newPDFPath);
        if (!file.exists()) {
            file = new File(newPDFPath);
        }
        FileOutputStream out = new FileOutputStream(file);// 输出流
        PdfReader reader = new PdfReader(templatePath);// 读取pdf模板
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        PdfStamper stamper = new PdfStamper(reader, bos);
        AcroFields form = stamper.getAcroFields();


        // 给表单添加中文字体 这里采用系统字体。不设置的话，中文可能无法显示
        BaseFont bf = BaseFont.createFont("STSong-Light", "UniGB-UCS2-H", BaseFont.NOT_EMBEDDED);

        form.addSubstitutionFont(bf);

        for (String key : valueData.keySet()) {
            form.setField(key, valueData.get(key));
        }
//        int pageNo = form.getFieldPositions("txtAWBBarCode").get(0).page;
//        Rectangle signRect = form.getFieldPositions("txtAWBBarCode").get(0).position;
//        float x = signRect.getLeft();
//        float y = signRect.getBottom();
//        // 读图片
//        Image image = Image.getInstance(PDFUtils.filePath+"/image.png");
//        // 获取操作的页面
//        PdfContentByte under = stamper.getOverContent(pageNo);
//        // 根据域的大小缩放图片
//        image.scaleToFit(signRect.getWidth(), signRect.getHeight());
//        // 添加图片
//        image.setAbsolutePosition(x, y);
//        under.addImage(image);

        stamper.setFormFlattening(true);// 如果为false那么生成的PDF文件还能编辑，一定要设为true
        stamper.close();

        Document doc = new Document();

        PdfCopy copy = new PdfCopy(doc, out);
        doc.open();
        PdfImportedPage importPage = copy.getImportedPage(new PdfReader(bos.toByteArray()), 1);
        copy.addPage(importPage);
        doc.close();
        return file;
    }
}
