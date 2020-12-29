package com.efreight.afbase.service;

import com.efreight.afbase.entity.CssIncomeWriteoffDetail;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * CSS 应收：核销 明细 服务类
 * </p>
 *
 * @author xiaobo
 * @since 2020-02-03
 */
public interface CssIncomeWriteoffDetailService extends IService<CssIncomeWriteoffDetail> {

    CssIncomeWriteoffDetail queryDebitNoteWriteoffDetailList(Integer incomeWriteoffId);
}
