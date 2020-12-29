package com.efreight.afbase.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.efreight.afbase.entity.*;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * AF 延伸服务 成本 服务类
 * </p>
 *
 * @author qipm
 * @since 2019-10-12
 */
public interface AfCostService extends IService<AfCost> {

	Boolean doSave(AfCost bean);
	Boolean doUpdate(AfCost bean);
	Boolean doDelete(AfCost bean);

    List<AfCost> getCostList(AfCost afCost);

    IPage getPageForAF(Page page, AfCost afCost);

    IPage getPageForSC(Page page, ScCost scCost);
    IPage getPageForTC(Page page, TcCost tcCost);
    IPage getPageForLC(Page page, LcCost lcCost);

    void exportExcelForAF(AfCost afCost);

    void exportExcelForSC(ScCost scCost);
    
    void exportExcelForTC(TcCost tcCost);
    void exportExcelForLC(LcCost lcCost);

    void exportExcelForIO(IoCost ioCost);

    IPage getPageForIO(Page page, IoCost ioCost);
}
