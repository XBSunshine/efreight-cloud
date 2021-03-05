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
	@Select({"<script>",
		"SELECT * FROM css_debit_note_currency",
		"where org_id=#{org_id} and debit_note_id=#{debit_note_id} and currency=#{currency}",
		"</script>"})
	CssDebitNoteCurrency queryCssDebitNoteCurrency(@Param("org_id") Integer org_id,@Param("debit_note_id") Integer debit_note_id,@Param("currency") String currency);
	
	@Select({"<script>",
		"SELECT A.* FROM css_debit_note_currency A",
		" inner join css_debit_note B on A.debit_note_id = B.debit_note_id ",
		" <when test='businessScope==\"AE\" or businessScope==\"AI\"'>",
        " left join af_order C on B.order_id=C.order_id",
        "  </when>",
        "  <when test='businessScope==\"SE\" or businessScope==\"SI\"'>",
        " left join sc_order C on B.order_id=C.order_id",
        "  </when>",
        "  <when test='businessScope==\"TE\" or businessScope==\"TI\"'>",
        " left join tc_order C on B.order_id=C.order_id",
        "  </when>",
        "  <when test='businessScope==\"LC\"'>",
        " left join lc_order C on B.order_id=C.order_id",
        "  </when>",
        "  <when test='businessScope==\"IO\"'>",
        " left join io_order C on B.order_id=C.order_id",
        "  </when>",
		" where A.org_id=#{org_id} and A.debit_note_id=#{debit_note_id} and A.currency=#{currency}",
		"  <when test='businessScope==\"AE\" or businessScope==\"SE\" or businessScope==\"TE\"'>",
        "  order by C.expect_departure , A.amount asc ",
        "  </when>",
        "  <when test='businessScope==\"AI\" or businessScope==\"SI\" or businessScope==\"TI\"'>",
        "  order by C.expect_arrival , A.amount asc ",
        "  </when>",
        "  <when test='businessScope==\"LC\"'>",
        "  order by C.driving_time , A.amount asc ",
        "  </when>",
        "  <when test='businessScope==\"IO\"'>",
        "  order by C.business_date , A.amount asc ",
        "  </when>",
		"</script>"})
	List<CssDebitNoteCurrency> queryCssDebitNoteCurrencyOrder(@Param("org_id") Integer org_id,@Param("debit_note_id") Integer debit_note_id,@Param("currency") String currency,@Param("businessScope") String businessScope);
}
