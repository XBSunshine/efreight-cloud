package com.efreight.afbase.service;

import com.efreight.afbase.entity.CssIncomeWriteoffStatementDetail;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * CSS 应收：核销单 明细（清单） 服务类
 * </p>
 *
 * @author xiaobo
 * @since 2020-07-07
 */
public interface CssIncomeWriteoffStatementDetailService extends IService<CssIncomeWriteoffStatementDetail> {

    List<CssIncomeWriteoffStatementDetail> queryListByIncomeWriteoffId(Integer incomeWriteoffId);
}
