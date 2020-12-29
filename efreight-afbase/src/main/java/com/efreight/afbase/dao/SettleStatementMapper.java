package com.efreight.afbase.dao;

import com.efreight.afbase.entity.procedure.SettleStatement;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.ResultMap;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.mapping.StatementType;

import java.util.List;

public interface SettleStatementMapper {

    @Options(statementType = StatementType.CALLABLE)
    @ResultMap({"settlestatement1", "settlestatement2","settlestatement3"})
    @Select({"<script>",
            "CALL css_P_statement_list_print(#{orgId},#{businessScope},#{statementNo},#{lang})\n",
            "</script>"})
    List<List<SettleStatement>> querySettleStatement(@Param("orgId") Integer orgId, @Param("businessScope") String businessScope, @Param("statementNo") String statementNo, @Param("lang") String lang);

    @Select({"<script>",
		" SELECT  ",
		" currency AS incomeCurrency,SUM(amount) AS functionalAmount,debit_note_id AS debitNoteId  ",
		" from css_debit_note_currency",
		" where",
		" debit_note_id in (SELECT b.debit_note_id from css_debit_note b where b.org_id = #{orgId} AND b.business_scope = #{businessScope} AND b.statement_id = #{statementNo})",
		" and  org_id = #{orgId}",
		" GROUP BY currency,debit_note_id",
	"</script>"})
    List<SettleStatement> queryCurrencyList(@Param("orgId") Integer orgId, @Param("businessScope") String businessScope,@Param("statementNo") Integer statementNo);
}
