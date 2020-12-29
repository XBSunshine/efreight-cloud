package com.efreight.afbase.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.efreight.afbase.entity.*;
import com.efreight.afbase.dao.CssIncomeWriteoffStatementDetailMapper;
import com.efreight.afbase.service.*;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.efreight.common.security.util.SecurityUtils;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * CSS 应收：核销单 明细（清单） 服务实现类
 * </p>
 *
 * @author xiaobo
 * @since 2020-07-07
 */
@Service
@AllArgsConstructor
public class CssIncomeWriteoffStatementDetailServiceImpl extends ServiceImpl<CssIncomeWriteoffStatementDetailMapper, CssIncomeWriteoffStatementDetail> implements CssIncomeWriteoffStatementDetailService {

    private final CssDebitNoteService cssDebitNoteService;

    private final CssDebitNoteCurrencyService cssDebitNoteCurrencyService;

    private final ScOrderService scOrderService;

    private final AfOrderService afOrderService;
    
    private final TcOrderService tcOrderService;
    
    private final LcOrderService lcOrderService;

    @Override
    public List<CssIncomeWriteoffStatementDetail> queryListByIncomeWriteoffId(Integer incomeWriteoffId) {
        LambdaQueryWrapper<CssIncomeWriteoffStatementDetail> wrapper = Wrappers.<CssIncomeWriteoffStatementDetail>lambdaQuery();
        wrapper.eq(CssIncomeWriteoffStatementDetail::getIncomeWriteoffId, incomeWriteoffId);
        List<CssIncomeWriteoffStatementDetail> list = list(wrapper);
        list.stream().forEach(cssIncomeWriteoffStatementDetail -> {
            //设置关于账单信息
            CssDebitNote cssDebitNote = cssDebitNoteService.getById(cssIncomeWriteoffStatementDetail.getDebitNoteId());
            //设置账单号
            cssIncomeWriteoffStatementDetail.setDebitNoteNum(cssDebitNote.getDebitNoteNum());
            //设置汇率
            LambdaQueryWrapper<CssDebitNoteCurrency> cssDebitNoteCurrencyWrapper = Wrappers.<CssDebitNoteCurrency>lambdaQuery();
            cssDebitNoteCurrencyWrapper.eq(CssDebitNoteCurrency::getCurrency, cssIncomeWriteoffStatementDetail.getCurrency()).eq(CssDebitNoteCurrency::getDebitNoteId, cssIncomeWriteoffStatementDetail.getDebitNoteId()).eq(CssDebitNoteCurrency::getOrgId, SecurityUtils.getUser().getOrgId());
            CssDebitNoteCurrency cssDebitNoteCurrency = cssDebitNoteCurrencyService.getOne(cssDebitNoteCurrencyWrapper);
            cssIncomeWriteoffStatementDetail.setExchangeRate(cssDebitNoteCurrency.getExchangeRate());
            //设置应收金额
            cssIncomeWriteoffStatementDetail.setAmount(cssDebitNoteCurrency.getAmount());
            cssIncomeWriteoffStatementDetail.setFunctionalAmount(cssDebitNoteCurrency.getFunctionalAmount());
            //设置运单号
            if (cssDebitNote.getBusinessScope().equals("AE")) {
                AfOrder afOrder = afOrderService.getById(cssDebitNote.getOrderId());
                cssIncomeWriteoffStatementDetail.setAwbNumber(afOrder.getAwbNumber());
            } else if (cssDebitNote.getBusinessScope().startsWith("AI")) {
                AfOrder afOrder = afOrderService.getById(cssDebitNote.getOrderId());
                if (StrUtil.isNotBlank(afOrder.getAwbNumber()) && StrUtil.isNotBlank(afOrder.getHawbNumber())) {
                    cssIncomeWriteoffStatementDetail.setAwbNumber(afOrder.getAwbNumber() + "_" + afOrder.getHawbNumber());
                } else if (StrUtil.isNotBlank(afOrder.getAwbNumber())) {
                    cssIncomeWriteoffStatementDetail.setAwbNumber(afOrder.getAwbNumber());
                } else if (StrUtil.isNotBlank(afOrder.getHawbNumber())) {
                    cssIncomeWriteoffStatementDetail.setAwbNumber(afOrder.getHawbNumber());
                }
            } else if (cssDebitNote.getBusinessScope().startsWith("S")) {
                ScOrder scOrder = scOrderService.getById(cssDebitNote.getOrderId());
                if (StrUtil.isNotBlank(scOrder.getMblNumber()) && StrUtil.isNotBlank(scOrder.getHblNumber())) {
                    cssIncomeWriteoffStatementDetail.setAwbNumber(scOrder.getMblNumber() + "_" + scOrder.getHblNumber());
                } else if (StrUtil.isNotBlank(scOrder.getMblNumber())) {
                    cssIncomeWriteoffStatementDetail.setAwbNumber(scOrder.getMblNumber());
                } else if (StrUtil.isNotBlank(scOrder.getHblNumber())) {
                    cssIncomeWriteoffStatementDetail.setAwbNumber(scOrder.getHblNumber());
                }
            }else if (cssDebitNote.getBusinessScope().startsWith("T")) {
            	TcOrder tcOrder = tcOrderService.getById(cssDebitNote.getOrderId());
            	if(StrUtil.isNotBlank(tcOrder.getRwbNumber())) {
            		cssIncomeWriteoffStatementDetail.setAwbNumber(tcOrder.getRwbNumber());
            	}
            }else if(cssDebitNote.getBusinessScope().startsWith("L")) {
            	LcOrder lcOrder = lcOrderService.getById(cssDebitNote.getOrderId());
            	if(StrUtil.isNotBlank(lcOrder.getCustomerNumber())) {
            		cssIncomeWriteoffStatementDetail.setAwbNumber(lcOrder.getCustomerNumber());
            	}
            }

        });
        return list;
    }
}
