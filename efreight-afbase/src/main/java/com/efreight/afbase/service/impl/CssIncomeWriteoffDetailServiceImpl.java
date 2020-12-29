package com.efreight.afbase.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.efreight.afbase.entity.*;
import com.efreight.afbase.dao.CssIncomeWriteoffDetailMapper;
import com.efreight.afbase.service.*;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.efreight.common.security.util.SecurityUtils;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * CSS 应收：核销 明细 服务实现类
 * </p>
 *
 * @author xiaobo
 * @since 2020-02-03
 */
@Service
@AllArgsConstructor
public class CssIncomeWriteoffDetailServiceImpl extends ServiceImpl<CssIncomeWriteoffDetailMapper, CssIncomeWriteoffDetail> implements CssIncomeWriteoffDetailService {

    private final CssDebitNoteService cssDebitNoteService;

    private final CssDebitNoteCurrencyService cssDebitNoteCurrencyService;

    private final ScOrderService scOrderService;

    private final AfOrderService afOrderService;
    
    private final TcOrderService tcOrderService;
    
    private final LcOrderService lcOrderService;

    @Override
    public CssIncomeWriteoffDetail queryDebitNoteWriteoffDetailList(Integer incomeWriteoffId) {
        LambdaQueryWrapper<CssIncomeWriteoffDetail> wrapper = Wrappers.<CssIncomeWriteoffDetail>lambdaQuery();
        wrapper.eq(CssIncomeWriteoffDetail::getIncomeWriteoffId, incomeWriteoffId);
        CssIncomeWriteoffDetail cssIncomeWriteoffDetail = getOne(wrapper);
        //设置关于账单信息
        CssDebitNote cssDebitNote = cssDebitNoteService.getById(cssIncomeWriteoffDetail.getDebitNoteId());
        //设置账单号
        cssIncomeWriteoffDetail.setDebitNoteNum(cssDebitNote.getDebitNoteNum());
        //设置汇率
        LambdaQueryWrapper<CssDebitNoteCurrency> cssDebitNoteCurrencyWrapper = Wrappers.<CssDebitNoteCurrency>lambdaQuery();
        cssDebitNoteCurrencyWrapper.eq(CssDebitNoteCurrency::getCurrency, cssIncomeWriteoffDetail.getCurrency()).eq(CssDebitNoteCurrency::getDebitNoteId, cssIncomeWriteoffDetail.getDebitNoteId()).eq(CssDebitNoteCurrency::getOrgId, SecurityUtils.getUser().getOrgId());
        CssDebitNoteCurrency cssDebitNoteCurrency = cssDebitNoteCurrencyService.getOne(cssDebitNoteCurrencyWrapper);
        cssIncomeWriteoffDetail.setExchangeRate(cssDebitNoteCurrency.getExchangeRate());
        //设置应收金额
        cssIncomeWriteoffDetail.setAmount(cssDebitNoteCurrency.getAmount());
        cssIncomeWriteoffDetail.setFunctionalAmount(cssDebitNoteCurrency.getFunctionalAmount());
        //设置运单号
        if (cssDebitNote.getBusinessScope().equals("AE")) {
            AfOrder afOrder = afOrderService.getById(cssDebitNote.getOrderId());
            cssIncomeWriteoffDetail.setAwbNumber(afOrder.getAwbNumber());
        } else if (cssDebitNote.getBusinessScope().startsWith("AI")) {
            AfOrder afOrder = afOrderService.getById(cssDebitNote.getOrderId());
            if (StrUtil.isNotBlank(afOrder.getAwbNumber()) && StrUtil.isNotBlank(afOrder.getHawbNumber())) {
                cssIncomeWriteoffDetail.setAwbNumber(afOrder.getAwbNumber() + "_" + afOrder.getHawbNumber());
            } else if (StrUtil.isNotBlank(afOrder.getAwbNumber())) {
                cssIncomeWriteoffDetail.setAwbNumber(afOrder.getAwbNumber());
            } else if (StrUtil.isNotBlank(afOrder.getHawbNumber())) {
                cssIncomeWriteoffDetail.setAwbNumber(afOrder.getHawbNumber());
            }
        } else if (cssDebitNote.getBusinessScope().startsWith("S")) {
            ScOrder scOrder = scOrderService.getById(cssDebitNote.getOrderId());
            if (StrUtil.isNotBlank(scOrder.getMblNumber()) && StrUtil.isNotBlank(scOrder.getHblNumber())) {
                cssIncomeWriteoffDetail.setAwbNumber(scOrder.getMblNumber() + "_" + scOrder.getHblNumber());
            } else if (StrUtil.isNotBlank(scOrder.getMblNumber())) {
                cssIncomeWriteoffDetail.setAwbNumber(scOrder.getMblNumber());
            } else if (StrUtil.isNotBlank(scOrder.getHblNumber())) {
                cssIncomeWriteoffDetail.setAwbNumber(scOrder.getHblNumber());
            }
        }else if(cssDebitNote.getBusinessScope().startsWith("T")) {
        	TcOrder tcOrder = tcOrderService.getById(cssDebitNote.getOrderId());
        	if(StrUtil.isNotBlank(tcOrder.getRwbNumber())) {
        		cssIncomeWriteoffDetail.setAwbNumber(tcOrder.getRwbNumber());
        	}
        }else if(cssDebitNote.getBusinessScope().startsWith("L")) {
        	LcOrder lcOrder = lcOrderService.getById(cssDebitNote.getOrderId());
        	if(StrUtil.isNotBlank(lcOrder.getCustomerNumber())) {
        		cssIncomeWriteoffDetail.setAwbNumber(lcOrder.getCustomerNumber());
        	}
        }

        return cssIncomeWriteoffDetail;
    }
}
