package com.efreight.afbase.dao;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.efreight.afbase.entity.FinancialAccount;
import com.efreight.afbase.entity.FinancialAccountLevel;
import com.efreight.afbase.entity.WriteOffFinancialAccount;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface FinancialAccountMapper extends BaseMapper<FinancialAccount> {
	@Select({"<script>",
		" SELECT\n" +
				"  B.financial_account_id AS financialAccountIdB,\n" +
				"\tB.financial_account_code AS financialAccountCodeB,\n" +
				"\tC.financial_account_id AS financialAccountIdC,\n" +
				"\tC.financial_account_code AS financialAccountCodeC,\n" +
				"\tD.financial_account_id AS financialAccountIdD,\n" +
				"\tD.financial_account_code AS financialAccountCodeD,\n" +
				"\tE.financial_account_id AS financialAccountIdE,\n" +
				"\tE.financial_account_code AS financialAccountCodeE \n" +
				"FROM\n" +
				"\tcss_financial_account A\n" +
				"\tLEFT JOIN css_financial_account B ON B.parent_id = A.financial_account_id\n" +
				"\tLEFT JOIN css_financial_account C ON C.parent_id = B.financial_account_id\n" +
				"\tLEFT JOIN css_financial_account D ON D.parent_id = C.financial_account_id\n" +
				"\tLEFT JOIN css_financial_account E ON E.parent_id = D.financial_account_id\n" +
				"\twhere A.financial_account_id= #{financialAccountId} \n",
	"</script>"})
	List<FinancialAccountLevel> getFinancialAccountLevel(@Param("financialAccountId") Integer financialAccountId);

	@Select({"<script>",
			" SELECT\n" +
					"\tB.financial_account_id AS financialAccountIdB,\n" +
					"\tB.financial_account_code AS financialAccountCodeB,\n" +
					"\tC.financial_account_id AS financialAccountIdC,\n" +
					"\tC.financial_account_code AS financialAccountCodeC,\n" +
					"\tD.financial_account_id AS financialAccountIdD,\n" +
					"\tD.financial_account_code AS financialAccountCodeD,\n" +
					"\tE.financial_account_id AS financialAccountIdE,\n" +
					"\tE.financial_account_code AS financialAccountCodeE \n" +
					"FROM\n" +
					"\tcss_financial_account A\n" +
					"\tLEFT JOIN css_financial_account B ON A.parent_id = B.financial_account_id\n" +
					"\tLEFT JOIN css_financial_account C ON B.parent_id = C.financial_account_id\n" +
					"\tLEFT JOIN css_financial_account D ON C.parent_id = D.financial_account_id\n" +
					"\tLEFT JOIN css_financial_account E ON D.parent_id = E.financial_account_id \n" +
					"WHERE\n" +
					"\tA.financial_account_id = #{financialAccountId} \n",
			"</script>"})
	FinancialAccountLevel getFinancialAccountAllParent(@Param("financialAccountId") Integer financialAccountId);

	@Select("SELECT financial_account_name AS accountName," +
			"IF(MAX(subsidiary_account)='往来单位', '往来单位', IF(MAX(manage_mode)='子科目', '子科目', null)) AS accountType, financial_account_code AS accountCode " +
			"FROM css_financial_account\n" +
			"WHERE org_id= #{orgId}\n" +
			"AND financial_account_class_02=1\n" +
			"AND business_scope = 'EF'\n" +
			"GROUP BY financial_account_name,financial_account_code\n" +
			"ORDER BY financial_account_code ASC\n")
	List<WriteOffFinancialAccount> listWriteOffAccount(@Param("orgId") Integer orgId);
}
