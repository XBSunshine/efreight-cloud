package com.efreight.afbase.dao;

import com.efreight.afbase.entity.CssFinancialAccount;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.efreight.afbase.entity.FinancialAccount;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * <p>
 * CSS 财务科目 Mapper 接口
 * </p>
 *
 * @author xiaobo
 * @since 2021-01-13
 */
public interface CssFinancialAccountMapper extends BaseMapper<CssFinancialAccount> {

    @Select({"<script>",
            "SELECT\n" +
                    "\tA.financial_account_name AS financialAccountName,\n" +
                    "\tMAX( A.financial_account_id ) AS financialAccountId,\n" +
                    "CASE\n" +
                    "\t\tMAX( A.manage_mode ) \n" +
                    "\t\tWHEN '子科目' THEN\n" +
                    "\t\tIFNULL( A.financial_account_code, '' ) \n" +
                    "\t\tWHEN '辅助账' THEN\n" +
                    "\tCASE\n" +
                    "\t\t\t\n" +
                    "\t\t\tWHEN MAX( A.subsidiary_account ) = '往来单位' THEN\n" +
                    "\t\t\tIFNULL( A.financial_account_code, '' ) ELSE A.financial_account_code \n" +
                    "\t\tEND ELSE A.financial_account_code \n" +
                    "\tEND AS financialAccountCode,\n" +
                    "CASE \t\n" +
                    "    WHEN MAX( A.financial_account_type ) IN ('现金','银行存款') THEN\n" +
                    "\t\t\t'' ELSE \n" +
                    "\t\tCASE\n" +
                    "\t\tMAX( A.manage_mode ) \n" +
                    "\t\tWHEN '子科目' THEN\n" +
                    "\t\t'子科目' \n" +
                    "\t\tWHEN '辅助账' THEN\n" +
                    "\tCASE\n" +
                    "\t\t\t\n" +
                    "\t\t\tWHEN MAX( A.subsidiary_account ) = '往来单位' THEN\n" +
                    "\t\t\t'往来单位' ELSE '' \n" +
                    "\t\tEND ELSE '' \n" +
                    "\tEND\t\n" +
                    "\t\tEND\n" +
                    " AS financialAccountType \n" +
                    "FROM\n" +
                    "\tcss_financial_account A\n" +
                    "\tLEFT JOIN ( SELECT parent_id, COUNT(*) AS FCOUNT FROM css_financial_account WHERE org_id = #{orgId} AND business_scope = #{businessScope} \n" +
                    "\tAND is_valid = 1 \n" +
                    "\tAND financial_account_class_02 = 1 GROUP BY parent_id ) AS C ON A.financial_account_id = C.parent_id \n" +
                    "WHERE\n" +
                    "\tA.org_id = #{orgId}\n" +
                    "\tAND A.business_scope = #{businessScope} \n" +
                    "\tAND A.is_valid = 1 \n" +
                    "\tAND A.financial_account_class_02 = 1\n" +
                    "GROUP BY\n" +
                    "\tA.financial_account_name,\n" +
                    "\tA.financial_account_code \n" +
                    "HAVING \tMAX( C.FCOUNT ) IS NULL\n" +
                    "ORDER BY\n" +
                    "\tA.financial_account_code ASC",
            "</script>"})
    List<FinancialAccount> getList(@Param("businessScope") String businessScope, @Param("orgId") Integer orgId);
}
