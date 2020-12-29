package com.efreight.afbase.dao;

import java.math.BigDecimal;
import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import com.efreight.afbase.entity.CssDebitNoteCurrency;
import com.efreight.afbase.entity.StatementCurrency;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * <p>
 * CSS 应收：清单 币种汇总表 Mapper 接口
 * </p>
 *
 * @author xiaobo
 * @since 2019-12-26
 */
public interface StatementCurrencyMapper extends BaseMapper<StatementCurrency> {

	@Select({"<script>",
		"SELECT * FROM css_debit_note_currency",
		"where org_id=#{org_id} and debit_note_id in (${debitNoteIds})",
		"</script>"})
	List<CssDebitNoteCurrency> queryBill(@Param("org_id") Integer org_id,@Param("debitNoteIds") String debitNoteIds);
	@Select({"<script>",
		"SELECT * FROM css_statement_currency",
		"where org_id=#{org_id} and statement_id = ${statement_id}",
	"</script>"})
	List<StatementCurrency> queryBillCurrency(@Param("org_id") Integer org_id,@Param("statement_id") Integer statement_id);
	
	@Select({"<script>",
		"SELECT currency_rate FROM af_V_currency_rate",
		"where org_id=#{org_id} AND currency_code=#{currency_code}",
		"</script>"})
	BigDecimal getCurrencyRate(@Param("org_id") Integer org_id,@Param("currency_code") String currency_code);
}
