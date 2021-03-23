package com.efreight.afbase.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.efreight.afbase.entity.*;
import com.efreight.afbase.dao.CustomsDeclarationMapper;
import com.efreight.afbase.service.*;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.efreight.common.core.utils.FormatUtils;
import com.efreight.common.core.utils.JxlsUtils;
import com.efreight.common.security.util.SecurityUtils;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * <p>
 * AF 报关单 服务实现类
 * </p>
 *
 * @author xiaobo
 * @since 2020-12-07
 */
@Service
@AllArgsConstructor
public class CustomsDeclarationServiceImpl extends ServiceImpl<CustomsDeclarationMapper, CustomsDeclaration> implements CustomsDeclarationService {

    private final CustomsDeclarationDetailService customsDeclarationDetailService;

    private final TariffUnitService tariffUnitService;

    private final AirportService airportService;

    private final WarehouseService warehouseService;

    private final PrmCategoryEciqService prmCategoryEciqService;

    private final VPrmCategoryService vPrmCategoryService;

    private final CurrencyService currencyService;

    @Override
    public IPage getPage(Page page, CustomsDeclaration customsDeclaration) {
        //拼接查询条件
        Wrapper wrapper = this.getWrapper(customsDeclaration);
        IPage result = page(page, wrapper);
        this.fixResult(result.getRecords());
        return result;
    }

    private void fixResult(List<CustomsDeclaration> records) {
        records.stream().forEach(customsDeclaration -> {
            //关别
            if (StrUtil.isNotBlank(customsDeclaration.getCiqQreaCode())) {
                LambdaQueryWrapper<VPrmCategory> vPrmCategoryWrapper = Wrappers.<VPrmCategory>lambdaQuery();
                vPrmCategoryWrapper.eq(VPrmCategory::getCategoryType, 19).eq(VPrmCategory::getEdicode1, customsDeclaration.getCiqQreaCode());
                VPrmCategory vPrmCategory = vPrmCategoryService.getOne(vPrmCategoryWrapper);
                if (vPrmCategory != null) {
                    customsDeclaration.setCiqQreaName(vPrmCategory.getParamText());
                }
            }
            //运输方式
            if (StrUtil.isNotBlank(customsDeclaration.getTransportMode())) {
                LambdaQueryWrapper<VPrmCategory> vPrmCategoryWrapper = Wrappers.<VPrmCategory>lambdaQuery();
                vPrmCategoryWrapper.eq(VPrmCategory::getCategoryType, 20).eq(VPrmCategory::getEdicode1, customsDeclaration.getTransportMode());
                VPrmCategory vPrmCategory = vPrmCategoryService.getOne(vPrmCategoryWrapper);
                if (vPrmCategory != null) {
                    customsDeclaration.setTransportModeName(vPrmCategory.getParamText());
                }
            }
            //监管方式
            if (StrUtil.isNotBlank(customsDeclaration.getTradeMode())) {
                LambdaQueryWrapper<VPrmCategory> vPrmCategoryWrapper = Wrappers.<VPrmCategory>lambdaQuery();
                vPrmCategoryWrapper.eq(VPrmCategory::getCategoryType, 21).eq(VPrmCategory::getEdicode1, customsDeclaration.getTradeMode());
                VPrmCategory vPrmCategory = vPrmCategoryService.getOne(vPrmCategoryWrapper);
                if (vPrmCategory != null) {
                    customsDeclaration.setTradeModeName(vPrmCategory.getParamText());
                }
            }
            //征免性质
            if (StrUtil.isNotBlank(customsDeclaration.getCutMode())) {
                LambdaQueryWrapper<VPrmCategory> vPrmCategoryWrapper = Wrappers.<VPrmCategory>lambdaQuery();
                vPrmCategoryWrapper.eq(VPrmCategory::getCategoryType, 22).eq(VPrmCategory::getEdicode1, customsDeclaration.getCutMode());
                VPrmCategory vPrmCategory = vPrmCategoryService.getOne(vPrmCategoryWrapper);
                if (vPrmCategory != null) {
                    customsDeclaration.setCutModeName(vPrmCategory.getParamText());
                }
            }
            //贸易国别
            if (StrUtil.isNotBlank(customsDeclaration.getCountryTrade())) {
                LambdaQueryWrapper<Airport> airportWrapper = Wrappers.<Airport>lambdaQuery();
                airportWrapper.eq(Airport::getApStatus, 1).eq(Airport::getNationCodeThree, customsDeclaration.getCountryTrade());
                List<Airport> airports = airportService.list(airportWrapper);
                if (!airports.isEmpty()) {
                    customsDeclaration.setCountryTradeName(airports.get(0).getNationNameCn());
                }
            }
            //运抵国
            if (StrUtil.isNotBlank(customsDeclaration.getCountryDepartureArrival())) {
                LambdaQueryWrapper<Airport> airportWrapper = Wrappers.<Airport>lambdaQuery();
                airportWrapper.eq(Airport::getApStatus, 1).eq(Airport::getNationCodeThree, customsDeclaration.getCountryDepartureArrival());
                List<Airport> airports = airportService.list(airportWrapper);
                if (!airports.isEmpty()) {
                    customsDeclaration.setCountryDepartureArrivalName(airports.get(0).getNationNameCn());
                }
            }
            //指运港
            if (StrUtil.isNotBlank(customsDeclaration.getCountryDepartureArrival1())) {
                LambdaQueryWrapper<Airport> airportWrapper = Wrappers.<Airport>lambdaQuery();
                airportWrapper.eq(Airport::getApStatus, 1).eq(Airport::getNationCodeThree, customsDeclaration.getCountryDepartureArrival1());
                List<Airport> airports = airportService.list(airportWrapper);
                if (!airports.isEmpty()) {
                    customsDeclaration.setCountryDepartureArrival1Name(airports.get(0).getNationNameCn());
                }
            }
            //离境口岸
            if (StrUtil.isNotBlank(customsDeclaration.getExportImportPort())) {
                LambdaQueryWrapper<VPrmCategory> vPrmCategoryWrapper = Wrappers.<VPrmCategory>lambdaQuery();
                vPrmCategoryWrapper.eq(VPrmCategory::getCategoryType, 29).eq(VPrmCategory::getEdicode1, customsDeclaration.getExportImportPort());
                VPrmCategory vPrmCategory = vPrmCategoryService.getOne(vPrmCategoryWrapper);
                if (vPrmCategory != null) {
                    customsDeclaration.setExportImportPortName(vPrmCategory.getParamText());
                }
            }
        });
    }

    private Wrapper getWrapper(CustomsDeclaration customsDeclaration) {
        LambdaQueryWrapper<CustomsDeclaration> wrapper = Wrappers.<CustomsDeclaration>lambdaQuery();
        if (StrUtil.isNotBlank(customsDeclaration.getAwbNumber())) {
            wrapper.like(CustomsDeclaration::getAwbNumber, customsDeclaration.getAwbNumber());
        }
        if (StrUtil.isNotBlank(customsDeclaration.getHawbNumber())) {
            wrapper.like(CustomsDeclaration::getHawbNumber, customsDeclaration.getHawbNumber());
        }
        if (StrUtil.isNotBlank(customsDeclaration.getCustomsNumberPreEntry())) {
            wrapper.like(CustomsDeclaration::getCustomsNumberPreEntry, customsDeclaration.getCustomsNumberPreEntry());
        }

        if (customsDeclaration.getCreateTimeStart() != null) {
            wrapper.ge(CustomsDeclaration::getCreateTime, customsDeclaration.getCreateTimeStart());
        }
        if (customsDeclaration.getCreateTimeEnd() != null) {
            wrapper.le(CustomsDeclaration::getCreateTime, customsDeclaration.getCreateTimeEnd());
        }
        wrapper.eq(CustomsDeclaration::getBusinessScope, customsDeclaration.getBusinessScope())
                .eq(CustomsDeclaration::getOrgId, SecurityUtils.getUser().getOrgId())
                .orderByDesc(CustomsDeclaration::getCreateTime);
        return wrapper;
    }

    @Override
    public CustomsDeclaration view(Integer customsDeclarationId) {
        return getById(customsDeclarationId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public synchronized Integer insert(CustomsDeclaration customsDeclaration) {
        //保存报关单
        customsDeclaration.setCreateTime(LocalDateTime.now());
        customsDeclaration.setCreatorId(SecurityUtils.getUser().getId());
        customsDeclaration.setCreatorName(SecurityUtils.getUser().buildOptName());
        customsDeclaration.setEditorId(customsDeclaration.getCreatorId());
        customsDeclaration.setEditorName(customsDeclaration.getCreatorName());
        customsDeclaration.setEditTime(customsDeclaration.getCreateTime());
        customsDeclaration.setOrgId(SecurityUtils.getUser().getOrgId());
        save(customsDeclaration);
        //保存报关单明细
        customsDeclaration.getDetailList().stream().forEach(customsDeclarationDetail -> {
            customsDeclarationDetail.setCustomsDeclarationId(customsDeclaration.getCustomsDeclarationId());
            customsDeclarationDetail.setOrgId(SecurityUtils.getUser().getOrgId());
        });

        customsDeclarationDetailService.saveBatch(customsDeclaration.getDetailList());
        return customsDeclaration.getCustomsDeclarationId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void modify(CustomsDeclaration customsDeclaration) {
        //校验
        if (getById(customsDeclaration.getCustomsDeclarationId()) == null) {
            throw new RuntimeException("报关单不存在");
        }
        //修改报关单
        customsDeclaration.setEditTime(LocalDateTime.now());
        customsDeclaration.setEditorId(SecurityUtils.getUser().getId());
        customsDeclaration.setEditorName(SecurityUtils.getUser().buildOptName());

        updateById(customsDeclaration);
        //修改报关单明细
        customsDeclarationDetailService.updateCustomsDeclarationDetail(customsDeclaration.getCustomsDeclarationId(), customsDeclaration.getDetailList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Integer customsDeclarationId) {
        //校验
        if (getById(customsDeclarationId) == null) {
            throw new RuntimeException("报关单不存在");
        }
        //删除
        removeById(customsDeclarationId);
        LambdaQueryWrapper<CustomsDeclarationDetail> detailLambdaQueryWrapper = Wrappers.<CustomsDeclarationDetail>lambdaQuery();
        detailLambdaQueryWrapper.eq(CustomsDeclarationDetail::getCustomsDeclarationId, customsDeclarationId).eq(CustomsDeclarationDetail::getOrgId, SecurityUtils.getUser().getOrgId());
        customsDeclarationDetailService.remove(detailLambdaQueryWrapper);
    }

    @Override
    public CustomsDeclaration total(CustomsDeclaration customsDeclaration) {
        Wrapper wrapper = this.getWrapper(customsDeclaration);
        list(wrapper).forEach(item -> {

        });
        CustomsDeclaration total = new CustomsDeclaration();
        return total;
    }

    @Override
    public void exportExcel(Integer customsDeclarationId) {
        CustomsDeclaration customsDeclaration = getById(customsDeclarationId);
        //关别
        if (StrUtil.isNotBlank(customsDeclaration.getCiqQreaCode())) {
            LambdaQueryWrapper<VPrmCategory> vPrmCategoryWrapper = Wrappers.<VPrmCategory>lambdaQuery();
            vPrmCategoryWrapper.eq(VPrmCategory::getCategoryType, 19).eq(VPrmCategory::getEdicode1, customsDeclaration.getCiqQreaCode());
            VPrmCategory vPrmCategory = vPrmCategoryService.getOne(vPrmCategoryWrapper);
            if (vPrmCategory != null) {
                customsDeclaration.setCiqQreaName(vPrmCategory.getParamText());
            }
        }
        //运输方式
        if (StrUtil.isNotBlank(customsDeclaration.getTransportMode())) {
            LambdaQueryWrapper<VPrmCategory> vPrmCategoryWrapper = Wrappers.<VPrmCategory>lambdaQuery();
            vPrmCategoryWrapper.eq(VPrmCategory::getCategoryType, 20).eq(VPrmCategory::getEdicode1, customsDeclaration.getTransportMode());
            VPrmCategory vPrmCategory = vPrmCategoryService.getOne(vPrmCategoryWrapper);
            if (vPrmCategory != null) {
                customsDeclaration.setTransportModeName(vPrmCategory.getParamText());
            }
        }
        //提单号
        if (StrUtil.isNotBlank(customsDeclaration.getAwbNumber()) && StrUtil.isNotBlank(customsDeclaration.getHawbNumber())) {
            customsDeclaration.setAwbNumber(customsDeclaration.getAwbNumber() + "_" + customsDeclaration.getHawbNumber());
        } else if (StrUtil.isNotBlank(customsDeclaration.getHawbNumber())) {
            customsDeclaration.setAwbNumber(customsDeclaration.getHawbNumber());
        } else if (StrUtil.isNotBlank(customsDeclaration.getAwbNumber())) {
            customsDeclaration.setAwbNumber(customsDeclaration.getAwbNumber());
        }
        //监管方式
        if (StrUtil.isNotBlank(customsDeclaration.getTradeMode())) {
            LambdaQueryWrapper<VPrmCategory> vPrmCategoryWrapper = Wrappers.<VPrmCategory>lambdaQuery();
            vPrmCategoryWrapper.eq(VPrmCategory::getCategoryType, 21).eq(VPrmCategory::getEdicode1, customsDeclaration.getTradeMode());
            VPrmCategory vPrmCategory = vPrmCategoryService.getOne(vPrmCategoryWrapper);
            if (vPrmCategory != null) {
                customsDeclaration.setTradeModeName(vPrmCategory.getParamText());
            }
        }
        //征免性质
        if (StrUtil.isNotBlank(customsDeclaration.getCutMode())) {
            LambdaQueryWrapper<VPrmCategory> vPrmCategoryWrapper = Wrappers.<VPrmCategory>lambdaQuery();
            vPrmCategoryWrapper.eq(VPrmCategory::getCategoryType, 22).eq(VPrmCategory::getEdicode1, customsDeclaration.getCutMode());
            VPrmCategory vPrmCategory = vPrmCategoryService.getOne(vPrmCategoryWrapper);
            if (vPrmCategory != null) {
                customsDeclaration.setCutModeName(vPrmCategory.getParamText());
            }
        }
        //贸易国别
        if (StrUtil.isNotBlank(customsDeclaration.getCountryTrade())) {
            LambdaQueryWrapper<Airport> airportWrapper = Wrappers.<Airport>lambdaQuery();
            airportWrapper.eq(Airport::getApStatus, 1).eq(Airport::getNationCodeThree, customsDeclaration.getCountryTrade());
            List<Airport> airports = airportService.list(airportWrapper);
            if (!airports.isEmpty()) {
                customsDeclaration.setCountryTradeName(airports.get(0).getNationNameCn());
            }
        }
        //运抵国
        if (StrUtil.isNotBlank(customsDeclaration.getCountryDepartureArrival())) {
            LambdaQueryWrapper<Airport> airportWrapper = Wrappers.<Airport>lambdaQuery();
            airportWrapper.eq(Airport::getApStatus, 1).eq(Airport::getNationCodeThree, customsDeclaration.getCountryDepartureArrival());
            List<Airport> airports = airportService.list(airportWrapper);
            if (!airports.isEmpty()) {
                customsDeclaration.setCountryDepartureArrivalName(airports.get(0).getNationNameCn());
            }
        }
        //指运港
        if (StrUtil.isNotBlank(customsDeclaration.getCountryDepartureArrival1())) {
            LambdaQueryWrapper<Airport> airportWrapper = Wrappers.<Airport>lambdaQuery();
            airportWrapper.eq(Airport::getApStatus, 1).eq(Airport::getNationCodeThree, customsDeclaration.getCountryDepartureArrival1());
            List<Airport> airports = airportService.list(airportWrapper);
            if (!airports.isEmpty()) {
                customsDeclaration.setCountryDepartureArrival1Name(airports.get(0).getNationNameCn());
            }
        }
        //启运港
        if (StrUtil.isNotBlank(customsDeclaration.getPortDepartureArrival())) {
            LambdaQueryWrapper<Airport> airportWrapper = Wrappers.<Airport>lambdaQuery();
            airportWrapper.eq(Airport::getApStatus, 1).eq(Airport::getNationCodeThree, customsDeclaration.getPortDepartureArrival());
            List<Airport> airports = airportService.list(airportWrapper);
            if (!airports.isEmpty()) {
                customsDeclaration.setPortDepartureArrivalName(airports.get(0).getNationNameCn());
            }
        }
        //货物存放地点
        if (customsDeclaration.getWarehouseId() != null) {
            LambdaQueryWrapper<Warehouse> warehouseWrapper = Wrappers.<Warehouse>lambdaQuery();
            warehouseWrapper.eq(Warehouse::getOrgId, SecurityUtils.getUser().getOrgId()).eq(Warehouse::getWarehouseId, customsDeclaration.getWarehouseId()).eq(Warehouse::getWarehouseStatus,1);
            List<Warehouse> warehouses = warehouseService.list(warehouseWrapper);
            if (!warehouses.isEmpty()) {
                customsDeclaration.setWarehouseNameCn(warehouses.get(0).getWarehouseNameCn());
            }
        }
        //离境口岸
        if (StrUtil.isNotBlank(customsDeclaration.getExportImportPort())) {
            LambdaQueryWrapper<VPrmCategory> vPrmCategoryWrapper = Wrappers.<VPrmCategory>lambdaQuery();
            vPrmCategoryWrapper.eq(VPrmCategory::getCategoryType, 29).eq(VPrmCategory::getEdicode1, customsDeclaration.getExportImportPort());
            VPrmCategory vPrmCategory = vPrmCategoryService.getOne(vPrmCategoryWrapper);
            if (vPrmCategory != null) {
                customsDeclaration.setExportImportPortName(vPrmCategory.getParamText());
            }
        }
        //包装种类
        if (StrUtil.isNotBlank(customsDeclaration.getPackageType())) {
            LambdaQueryWrapper<VPrmCategory> vPrmCategoryWrapper = Wrappers.<VPrmCategory>lambdaQuery();
            vPrmCategoryWrapper.eq(VPrmCategory::getCategoryType, 24).eq(VPrmCategory::getEdicode1, customsDeclaration.getPackageType());
            VPrmCategory vPrmCategory = vPrmCategoryService.getOne(vPrmCategoryWrapper);
            if (vPrmCategory != null) {
                customsDeclaration.setPackageTypeName(vPrmCategory.getParamText());
            }
        }
        //件数
        if (customsDeclaration.getPieces() != null) {
            customsDeclaration.setPiecesStr(FormatUtils.formatWithQWF(BigDecimal.valueOf(customsDeclaration.getPieces()), 0));
        }
        //毛重
        if (customsDeclaration.getGrossWeight() != null) {
            customsDeclaration.setGrossWeightStr(FormatUtils.formatWithQWF(customsDeclaration.getGrossWeight(), 5));
        }
        //净重
        if (customsDeclaration.getNetWeight() != null) {
            customsDeclaration.setNetWeightStr(FormatUtils.formatWithQWF(customsDeclaration.getNetWeight(), 5));
        }
        //成交方式
        if (StrUtil.isNotBlank(customsDeclaration.getTransMode())) {
            LambdaQueryWrapper<VPrmCategory> vPrmCategoryWrapper = Wrappers.<VPrmCategory>lambdaQuery();
            vPrmCategoryWrapper.eq(VPrmCategory::getCategoryType, 25).eq(VPrmCategory::getEdicode1, customsDeclaration.getTransMode());
            VPrmCategory vPrmCategory = vPrmCategoryService.getOne(vPrmCategoryWrapper);
            if (vPrmCategory != null) {
                customsDeclaration.setTransModeName(vPrmCategory.getParamText());
            }
        }
        //运费
        if (customsDeclaration.getFeeRate() != null) {
            customsDeclaration.setFeeRateStr(FormatUtils.formatWithQWF(customsDeclaration.getFeeRate(), 5));
        }
        //保费
        if (customsDeclaration.getInsurRate() != null) {
            customsDeclaration.setInsurRateStr(FormatUtils.formatWithQWF(customsDeclaration.getInsurRate(), 5));
        }
        //杂费
        if (customsDeclaration.getOtherRate() != null) {
            customsDeclaration.setOtherRateStr(FormatUtils.formatWithQWF(customsDeclaration.getOtherRate(), 5));
        }
        //随附单证
        if (StrUtil.isNotBlank(customsDeclaration.getEdocCode())) {
            StringBuffer buffer = new StringBuffer();
            Arrays.stream(customsDeclaration.getEdocCode().split(",")).forEach(code -> {
                LambdaQueryWrapper<VPrmCategory> vPrmCategoryWrapper = Wrappers.<VPrmCategory>lambdaQuery();
                vPrmCategoryWrapper.eq(VPrmCategory::getCategoryType, 26).eq(VPrmCategory::getEdicode1, code);
                VPrmCategory vPrmCategory = vPrmCategoryService.getOne(vPrmCategoryWrapper);
                if (vPrmCategory != null) {
                    if (buffer.length() == 0) {
                        buffer.append(vPrmCategory.getParamText());
                    } else {
                        buffer.append(",").append(vPrmCategory.getParamText());
                    }
                }
            });
            customsDeclaration.setEdocCodeName(buffer.toString());
        }
        //特殊关系确认
        if (customsDeclaration.getSpecFlag1()) {
            customsDeclaration.setSpecFlag1Str("是");
        } else {
            customsDeclaration.setSpecFlag1Str("否");
        }
        //价格影响确认
        if (customsDeclaration.getSpecFlag2()) {
            customsDeclaration.setSpecFlag2Str("是");
        } else {
            customsDeclaration.setSpecFlag2Str("否");
        }
        //支持特权使用费确认
        if (customsDeclaration.getSpecFlag3()) {
            customsDeclaration.setSpecFlag3Str("是");
        } else {
            customsDeclaration.setSpecFlag3Str("否");
        }
        //自提自缴
        if (customsDeclaration.getSpecFlag4()) {
            customsDeclaration.setSpecFlag4Str("是");
        } else {
            customsDeclaration.setSpecFlag4Str("否");
        }
        //申报单位
        customsDeclaration.setCustomsAgentCode(customsDeclaration.getCustomsAgentCode() + " " + customsDeclaration.getCustomsAgentName());
        List<CustomsDeclarationDetail> detailList = customsDeclarationDetailService.listByCustomsDeclarationId(customsDeclarationId);
        detailList.stream().forEach(detail -> {
            //数量及单位
            String quantity1Str = "";
            if (detail.getQuantity1() != null) {
                quantity1Str += FormatUtils.formatWithQWF(detail.getQuantity1(), 5);
            }
            if (StrUtil.isNotBlank(detail.getUnit1())) {
                LambdaQueryWrapper<TariffUnit> tariffUnitWrapper = Wrappers.<TariffUnit>lambdaQuery();
                tariffUnitWrapper.eq(TariffUnit::getUnitCode, detail.getUnit1());
                TariffUnit tariffUnit = tariffUnitService.getOne(tariffUnitWrapper);
                if (tariffUnit != null) {
                    quantity1Str += " " + tariffUnit.getUnitName();
                }
            }
            detail.setQuantity1Str(quantity1Str);
            String quantity2Str = "";
            if (detail.getQuantity2() != null) {
                quantity2Str += FormatUtils.formatWithQWF(detail.getQuantity2(), 5);
            }
            if (StrUtil.isNotBlank(detail.getUnit2())) {
                LambdaQueryWrapper<TariffUnit> tariffUnitWrapper = Wrappers.<TariffUnit>lambdaQuery();
                tariffUnitWrapper.eq(TariffUnit::getUnitCode, detail.getUnit2());
                TariffUnit tariffUnit = tariffUnitService.getOne(tariffUnitWrapper);
                if (tariffUnit != null) {
                    quantity2Str += " " + tariffUnit.getUnitName();
                }
            }
            detail.setQuantity2Str(quantity2Str);
            String quantity3Str = "";
            if (detail.getQuantity3() != null) {
                quantity3Str += FormatUtils.formatWithQWF(detail.getQuantity3(), 5);
            }
            if (StrUtil.isNotBlank(detail.getUnit3())) {
                LambdaQueryWrapper<TariffUnit> tariffUnitWrapper = Wrappers.<TariffUnit>lambdaQuery();
                tariffUnitWrapper.eq(TariffUnit::getUnitCode, detail.getUnit3());
                TariffUnit tariffUnit = tariffUnitService.getOne(tariffUnitWrapper);
                if (tariffUnit != null) {
                    quantity3Str += " " + tariffUnit.getUnitName();
                }
            }
            detail.setQuantity3Str(quantity3Str);
            //总价
            if (detail.getDeclTotal() != null) {
                detail.setDeclTotalStr(FormatUtils.formatWithQWF(detail.getDeclTotal(), 4));
            }
            //单价
            if (detail.getDeclPrice() != null) {
                detail.setDeclPriceStr(FormatUtils.formatWithQWF(detail.getDeclPrice(), 4));
            }

            //原产国
            if (StrUtil.isNotBlank(detail.getCountryOrigin())) {
                LambdaQueryWrapper<Airport> airportWrapper = Wrappers.<Airport>lambdaQuery();
                airportWrapper.eq(Airport::getApStatus, 1).eq(Airport::getNationCodeThree, detail.getCountryOrigin());
                List<Airport> airports = airportService.list(airportWrapper);
                if (!airports.isEmpty()) {
                    detail.setCountryOriginName(airports.get(0).getNationNameCn());
                }
            }
            //目的国
            if (StrUtil.isNotBlank(detail.getCountryDestination())) {
                LambdaQueryWrapper<Airport> airportWrapper = Wrappers.<Airport>lambdaQuery();
                airportWrapper.eq(Airport::getApStatus, 1).eq(Airport::getNationCodeThree, detail.getCountryDestination());
                List<Airport> airports = airportService.list(airportWrapper);
                if (!airports.isEmpty()) {
                    detail.setCountryDestinationName(airports.get(0).getNationNameCn());
                }
            }
            //境内目的地
            if (StrUtil.isNotBlank(detail.getDistrictCode())) {
                LambdaQueryWrapper<PrmCategoryEciq> prmCategoryEciqWrapper = Wrappers.<PrmCategoryEciq>lambdaQuery();
                prmCategoryEciqWrapper.eq(PrmCategoryEciq::getAreaCode, detail.getDistrictCode());
                PrmCategoryEciq prmCategoryEciq = prmCategoryEciqService.getOne(prmCategoryEciqWrapper);
                if (prmCategoryEciq != null) {
                    detail.setDistrictCodeName(prmCategoryEciq.getAreaNameCn());
                }
            }
            //征免性质
            if (StrUtil.isNotBlank(detail.getCutMode())) {
                LambdaQueryWrapper<VPrmCategory> vPrmCategoryWrapper = Wrappers.<VPrmCategory>lambdaQuery();
                vPrmCategoryWrapper.eq(VPrmCategory::getCategoryType, 27).eq(VPrmCategory::getEdicode1, detail.getCutMode());
                VPrmCategory vPrmCategory = vPrmCategoryService.getOne(vPrmCategoryWrapper);
                if (vPrmCategory != null) {
                    detail.setCutModeName(vPrmCategory.getParamText());
                }
            }
        });
        customsDeclaration.setDetailList(detailList);
        HashMap<String, Object> map = new HashMap<>();
        map.put("data", customsDeclaration);
        JxlsUtils.exportExcelWithLocalModel(JxlsUtils.modelRootPath + "CUSTOMS_DECLARATION_"+customsDeclaration.getBusinessScope()+".xls", map);
    }
}
