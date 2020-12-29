package com.efreight.afbase.dao;

import com.efreight.afbase.entity.CssFinancialExpenseReportFiles;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * <p>
 * CSS 财务费用报销 附件Mapper 接口
 * </p>
 *
 * @author caiwd
 * @since 2020-10-28
 */
public interface CssFinancialExpenseReportFilesMapper extends BaseMapper<CssFinancialExpenseReportFiles> {

	
	@Select({"<script>",
		 "    SELECT * ",
		 " from css_financial_expense_report_files  ",
		 " where  ",
		 " org_id =#{bean.orgId} and expense_report_id = #{bean.expenseReportId}",
         "</script>"})
	List<CssFinancialExpenseReportFiles> getList(@Param("bean") CssFinancialExpenseReportFiles bean);
	
}
