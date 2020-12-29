package com.efreight.afbase.dao;

import com.efreight.afbase.entity.CssFinancialExpenseReport;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

/**
 * <p>
 * CSS 财务费用报销 Mapper 接口
 * </p>
 *
 * @author caiwd
 * @since 2020-10-14
 */
public interface CssFinancialExpenseReportMapper extends BaseMapper<CssFinancialExpenseReport> {

	
	@Select({"<script>",
		 "    SELECT a.* ,",
		 "b.full_name AS dept_name ",
		 " from css_financial_expense_report a ",
		 " left join  hrs_dept b on a.dept_id = b.dept_id ",
		 " where  ",
		 " a.org_id =#{bean.orgId}   ",
		 "<when test='bean.expenseReportDateStart!=null and bean.expenseReportDateStart!=\"\"'>",
         " AND a.expense_report_date <![CDATA[ >= ]]> #{bean.expenseReportDateStart}",
         "</when>",
         "<when test='bean.expenseReportDateEnd!=null and bean.expenseReportDateEnd!=\"\"'>",
         " AND a.expense_report_date <![CDATA[ <= ]]> #{bean.expenseReportDateEnd}",
         "</when>",
         "<when test='bean.auditType==\"2\"'>",
         " AND (a.creator_id = #{bean.creatorId} or a.approval_dept_manager_id = #{bean.approvalDeptManagerId})",
         "</when>",
         "<when test='bean.creatorId!=null and bean.auditType==\"1\"'>",
         " AND a.creator_id = #{bean.creatorId}",
         "</when>",
         "<when test='bean.approvalDeptManagerId!=null and bean.auditType==\"1\"'>",
         " AND a.approval_dept_manager_id = #{bean.approvalDeptManagerId}",
         "</when>",
         "<when test='bean.expenseReportStatus!=null and bean.expenseReportStatus!=\"\"'>",
         " AND a.expense_report_status in (${bean.expenseReportStatus})",
         "</when>",
         "<when test='bean.expenseReportNum!=null and bean.expenseReportNum!=\"\"'>",
         " AND a.expense_report_num like  \"%\"#{bean.expenseReportNum}\"%\"",
         "</when>",
         " order by create_time desc",
         "</script>"})
	IPage<CssFinancialExpenseReport> getListPage(Page page, @Param("bean") CssFinancialExpenseReport bean);
	@Select({"<script>",
		"select a.full_name as dept_name,a.manager_id,b.user_name as dept_user_name",
		" from hrs_dept a  left join hrs_user b on a.manager_id = b.user_id  ",
		" where a.dept_id = #{deptId}",
	"</script>"})
	Map getDeptInfo(@Param("deptId") Integer deptId);
	
	@Select({"<script>",
		"select * from css_financial_expense_report ",
		" where org_id = #{bean.orgId}",
		"<when test='bean.code!=null and bean.code!=\"\"'>",
		 " and expense_report_num like  \"%\"#{bean.code}\"%\"",
        "</when>",
		" order by create_time desc",
	"</script>"})
	List<CssFinancialExpenseReport> getInfo(@Param("bean") CssFinancialExpenseReport bean);
	@Select({"<script>",
		"SELECT financial_account_name as paramName,financial_account_code as paramCode,min(financial_account_id) as id,min(financial_account_type) as type,CONCAT(financial_account_name,' ',financial_account_code) as paramText FROM css_financial_account",
		" WHERE org_id=#{orgId}",
		"<when test='type!=null and type!=\"\" and type==\"费用\"'>",
	    " AND financial_account_class_03=1",
	    "</when>",
	    "<when test='type!=null and type!=\"\" and type==\"付款\"'>",
	    " AND financial_account_class_02=1",
	    "</when>",
		" GROUP BY financial_account_name,financial_account_code",
		" ORDER BY financial_account_code ASC",
	"</script>"})
	List<Map> getSubject(@Param("orgId") Integer orgId,@Param("type") String type);
	
	@Select({"<script>",
		"select *  from ( ",
		"SELECT A.financial_account_name as paramName,MAX(B.FCOUNT) AS FCOUNT,A.financial_account_code as paramCode,min(A.financial_account_id) as id,min(A.financial_account_type) as type,CONCAT(A.financial_account_name,' ',A.financial_account_code) as paramText FROM css_financial_account A",
		" LEFT JOIN ( SELECT parent_id,COUNT(*) AS FCOUNT FROM css_financial_account  WHERE org_id=#{orgId} AND financial_account_class_02=1 AND is_valid=1",
		" GROUP BY parent_id ) AS B ON A.financial_account_id=B.parent_id ",
		" WHERE org_id=#{orgId}",
//		"<when test='type!=null and type!=\"\" and type==\"费用\"'>",
//	    " AND financial_account_class_03=1",
//	    "</when>",
//	    "<when test='type!=null and type!=\"\" and type==\"付款\"'>",
	    " AND A.financial_account_class_02=1 AND is_valid=1",
//	    "</when>",
		" GROUP BY A.financial_account_name,A.financial_account_code",
		" ORDER BY A.financial_account_code ASC",
		") d where d.FCOUNT is NULL",
	"</script>"})
	List<Map> getSubjectBank(@Param("orgId") Integer orgId,@Param("type") String type);
	
	 @Update("update css_financial_expense_report set\n"
	            + " expense_financial_account_code=#{bean.expenseFinancialAccountCode}\n"
	            + ",expense_financial_account_name=#{bean.expenseFinancialAccountName}\n"
	            + ",expense_financial_account_id=#{bean.expenseFinancialAccountId}\n"
	            + ",expense_financial_account_type=#{bean.expenseFinancialAccountType}\n"
	            + ",bank_financial_account_code=#{bean.bankFinancialAccountCode}\n"
	            + ",bank_financial_account_name=#{bean.bankFinancialAccountName}\n"
	            + ",bank_financial_account_id=#{bean.bankFinancialAccountId}\n"
	            + ",bank_financial_account_type=#{bean.bankFinancialAccountType}\n"
	            + ",expense_report_status=#{bean.expenseReportStatus}\n"
	            + ",editor_id=#{bean.editorId}\n"
	            + ",editor_name=#{bean.editorName}\n"
	            + ",edit_time=#{bean.editTime}\n"
	            + ",financial_date=#{bean.financialDate}\n"
	            + ",approval_financial_user_id=#{bean.approvalFinancialUserId}\n"
	            + ",approval_financial_user_name=#{bean.approvalFinancialUserName}\n"
	            + ",approval_financial_time=#{bean.approvalFinancialTime}\n"
	            + " where  expense_report_id in (${bean.expenseReportIdStr})\n")
     void updateAudit(@Param("bean") CssFinancialExpenseReport bean);
	 
	 @Update("update css_financial_expense_report set\n"
	            + " payer_id=#{bean.payerId}\n"
	            + " ,payer_name=#{bean.payerName}\n"
	            + " ,payer_time=#{bean.payerTime}\n"
	            + " ,editor_id=#{bean.editorId}\n"
	            + " ,editor_name=#{bean.editorName}\n"
	            + " ,edit_time=#{bean.editTime}\n"
	            + " ,expense_report_status=#{bean.expenseReportStatus}\n"
	            + " where  expense_report_id in (${bean.expenseReportIdStr})\n")
    void updatePayment(@Param("bean") CssFinancialExpenseReport bean);
	
	@Update("delete from  css_financial_expense_report \n"
	            + " where  expense_report_id in (${bean.expenseReportId})\n")
    void deleteIds(@Param("bean") CssFinancialExpenseReport bean);
	
	@Select({"<script>",
		 "    SELECT a.* ,",
		 "b.full_name AS dept_name",
		 " from css_financial_expense_report a ",
		 " left join  hrs_dept b on a.dept_id = b.dept_id ",
		 " where 1=1 ",
        "<when test='bean.expenseReportIdStr!=null and bean.expenseReportIdStr!=\"\"'>",
        " AND a.expense_report_id in (${bean.expenseReportIdStr})",
        "</when>",
        "</script>"})
	List<CssFinancialExpenseReport> getPrint(@Param("bean") CssFinancialExpenseReport bean);
	@Select({"<script>",
		 "    SELECT user_id as code,user_name as name",
		 " from hrs_user  ",
		 " where org_id=${orgId} and leave_date is null and blacklist_date is null ",
       "</script>"})
	List<Map> getOrguser(@Param("orgId") Integer orgId);
}
