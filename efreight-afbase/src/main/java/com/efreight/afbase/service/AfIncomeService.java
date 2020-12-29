package com.efreight.afbase.service;

import java.util.List;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.efreight.afbase.entity.AfCost;
import com.efreight.afbase.entity.AfIncome;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * AF 延伸服务 应收 服务类
 * </p>
 *
 * @author qipm
 * @since 2019-10-12
 */
public interface AfIncomeService extends IService<AfIncome> {
	Boolean doSave(AfIncome bean);
	Boolean doUpdate(AfIncome bean);
	Boolean doDelete(AfIncome bean);
	List<AfCost> queryByIncomeId(Integer id);
	List<AfCost> queryByIncomeIdSE(Integer id);
	List<AfCost> queryByIncomeIdTC(Integer id);
    IPage getPage(Page page, AfIncome income);

    void exportExcel(AfIncome income);
}
