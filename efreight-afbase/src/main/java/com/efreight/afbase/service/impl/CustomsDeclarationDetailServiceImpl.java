package com.efreight.afbase.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.efreight.afbase.entity.*;
import com.efreight.afbase.dao.CustomsDeclarationDetailMapper;
import com.efreight.afbase.service.*;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.efreight.common.security.util.SecurityUtils;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
public class CustomsDeclarationDetailServiceImpl extends ServiceImpl<CustomsDeclarationDetailMapper, CustomsDeclarationDetail> implements CustomsDeclarationDetailService {

    private final TariffDetailsService tariffDetailsService;

    private final TariffUnitService tariffUnitService;

    private final AirportService airportService;

    private final PrmCategoryEciqService prmCategoryEciqService;


    @Override
    public List<CustomsDeclarationDetail> listByCustomsDeclarationId(Integer customsDeclarationId) {
        LambdaQueryWrapper<CustomsDeclarationDetail> wrapper = Wrappers.<CustomsDeclarationDetail>lambdaQuery();
        wrapper.eq(CustomsDeclarationDetail::getCustomsDeclarationId, customsDeclarationId).eq(CustomsDeclarationDetail::getOrgId, SecurityUtils.getUser().getOrgId()).orderByAsc(CustomsDeclarationDetail::getItemNumber);
        List<CustomsDeclarationDetail> list = list(wrapper);
        list.stream().forEach(customsDeclarationDetail -> {
            ArrayList<TariffDetails> detailForProductList = new ArrayList<TariffDetails>();
            if (StrUtil.isNotBlank(customsDeclarationDetail.getProductCode())) {
                LambdaQueryWrapper<TariffDetails> tariffDetailsWrapper = Wrappers.<TariffDetails>lambdaQuery();
                tariffDetailsWrapper.eq(TariffDetails::getProductCode, customsDeclarationDetail.getProductCode());
                TariffDetails tariffDetails = tariffDetailsService.getOne(tariffDetailsWrapper);
                if (tariffDetails != null) {
                    detailForProductList.add(tariffDetails);
                }
            }
            customsDeclarationDetail.setProducts(detailForProductList);

            ArrayList<TariffUnit> detailForUnit1List = new ArrayList<TariffUnit>();
            if (StrUtil.isNotBlank(customsDeclarationDetail.getUnit1())) {
                LambdaQueryWrapper<TariffUnit> tariffUnitWrapper = Wrappers.<TariffUnit>lambdaQuery();
                tariffUnitWrapper.eq(TariffUnit::getUnitCode, customsDeclarationDetail.getUnit1());
                TariffUnit tariffUnit = tariffUnitService.getOne(tariffUnitWrapper);
                if (tariffUnit != null) {
                    detailForUnit1List.add(tariffUnit);
                }
            }
            customsDeclarationDetail.setUnit1s(detailForUnit1List);
            ArrayList<TariffUnit> detailForUnit2List = new ArrayList<TariffUnit>();
            if (StrUtil.isNotBlank(customsDeclarationDetail.getUnit2())) {
                LambdaQueryWrapper<TariffUnit> tariffUnitWrapper = Wrappers.<TariffUnit>lambdaQuery();
                tariffUnitWrapper.eq(TariffUnit::getUnitCode, customsDeclarationDetail.getUnit2());
                TariffUnit tariffUnit = tariffUnitService.getOne(tariffUnitWrapper);
                if (tariffUnit != null) {
                    detailForUnit2List.add(tariffUnit);
                }
            }
            customsDeclarationDetail.setUnit2s(detailForUnit2List);
            ArrayList<TariffUnit> detailForUnit3List = new ArrayList<TariffUnit>();
            if (StrUtil.isNotBlank(customsDeclarationDetail.getUnit3())) {
                LambdaQueryWrapper<TariffUnit> tariffUnitWrapper = Wrappers.<TariffUnit>lambdaQuery();
                tariffUnitWrapper.eq(TariffUnit::getUnitCode, customsDeclarationDetail.getUnit3());
                TariffUnit tariffUnit = tariffUnitService.getOne(tariffUnitWrapper);
                if (tariffUnit != null) {
                    detailForUnit3List.add(tariffUnit);
                }
            }
            customsDeclarationDetail.setUnit3s(detailForUnit3List);
            ArrayList<Airport> detailForCountryOriginList = new ArrayList<Airport>();
            if (StrUtil.isNotBlank(customsDeclarationDetail.getCountryOrigin())) {
                LambdaQueryWrapper<Airport> airportWrapper = Wrappers.<Airport>lambdaQuery();
                airportWrapper.eq(Airport::getNationCodeThree, customsDeclarationDetail.getCountryOrigin());
                List<Airport> airports = airportService.list(airportWrapper);
                if (airports.size() > 0) {
                    detailForCountryOriginList.add(airports.get(0));
                }
            }
            customsDeclarationDetail.setCountryOrigins(detailForCountryOriginList);
            ArrayList<Airport> detailForCountryDestinationList = new ArrayList<Airport>();
            if (StrUtil.isNotBlank(customsDeclarationDetail.getCountryDestination())) {
                LambdaQueryWrapper<Airport> airportWrapper = Wrappers.<Airport>lambdaQuery();
                airportWrapper.eq(Airport::getNationCodeThree, customsDeclarationDetail.getCountryDestination());
                List<Airport> airports = airportService.list(airportWrapper);
                if (airports.size() > 0) {
                    detailForCountryDestinationList.add(airports.get(0));
                }
            }
            customsDeclarationDetail.setCountryDestinations(detailForCountryDestinationList);

            ArrayList<PrmCategoryEciq> detailForDistrictCodeList = new ArrayList<PrmCategoryEciq>();
            if (StrUtil.isNotBlank(customsDeclarationDetail.getDistrictCode())) {
                LambdaQueryWrapper<PrmCategoryEciq> prmCategoryEciqWrapper = Wrappers.<PrmCategoryEciq>lambdaQuery();
                prmCategoryEciqWrapper.eq(PrmCategoryEciq::getAreaCode, customsDeclarationDetail.getDistrictCode());
                PrmCategoryEciq prmCategoryEciq = prmCategoryEciqService.getOne(prmCategoryEciqWrapper);
                if (prmCategoryEciq != null) {
                    detailForDistrictCodeList.add(prmCategoryEciq);
                }
            }
            customsDeclarationDetail.setDistrictCodes(detailForDistrictCodeList);
        });
        return list;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateCustomsDeclarationDetail(Integer customsDeclarationId, List<CustomsDeclarationDetail> newlist) {
        List<CustomsDeclarationDetail> existList = listByCustomsDeclarationId(customsDeclarationId);
        ArrayList<CustomsDeclarationDetail> updateList = new ArrayList<>();
        newlist.stream().forEach(customsDeclarationDetail -> {
            customsDeclarationDetail.setCustomsDeclarationId(customsDeclarationId);
            customsDeclarationDetail.setOrgId(SecurityUtils.getUser().getOrgId());
            updateList.add(customsDeclarationDetail);
            if (customsDeclarationDetail.getCustomsDeclarationDetailId() != null) {
                existList.removeIf(existDetail -> existDetail.getCustomsDeclarationDetailId().equals(customsDeclarationDetail.getCustomsDeclarationDetailId()));
            }
        });
        if (!updateList.isEmpty()) {
            saveOrUpdateBatch(updateList);
        }
        if (existList.size() > 0) {
            removeByIds(existList.stream().map(CustomsDeclarationDetail::getCustomsDeclarationDetailId).collect(Collectors.toList()));
        }
    }
}
