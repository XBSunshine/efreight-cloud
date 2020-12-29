package com.efreight.afbase.dao;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import com.efreight.afbase.entity.CssDebitNoteCurrency;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * <p>
 * CSS 应收：账单 币种汇总表 Mapper 接口
 * </p>
 *
 * @author qipm
 * @since 2019-12-24
 */
public interface CssDebitNoteCurrencyMapper extends BaseMapper<CssDebitNoteCurrency> {

	
	@Select({"<script>",
		"SELECT * FROM css_debit_note_currency",
		"where org_id=#{org_id} and debit_note_id=#{debit_note_id}",
		"</script>"})
	List<CssDebitNoteCurrency> queryBill2(@Param("org_id") Integer org_id,@Param("debit_note_id") String debit_note_id);
	
	@Delete({"<script>",
		"delete FROM css_debit_note_currency",
		"where org_id=#{org_id} and debit_note_id=#{debit_note_id}",
		"</script>"})
	void deleteByDebitId(@Param("org_id") Integer org_id,@Param("debit_note_id") Integer debit_note_id);
	
	@Select({"<script>",
		"SELECT currency_rate FROM af_V_currency_rate",
		"where org_id=#{org_id} AND currency_code=#{currency_code}",
		"</script>"})
	BigDecimal getCurrencyRate(@Param("org_id") Integer org_id,@Param("currency_code") String currency_code);
}
