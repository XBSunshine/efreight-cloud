package com.efreight.afbase.dao;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import com.efreight.afbase.entity.AfCost;
import com.efreight.afbase.entity.AfIncome;
import com.efreight.afbase.entity.AfIncomeCost;
import com.efreight.afbase.entity.AfIncomeCostTree;
import com.efreight.afbase.entity.AfOrder;
import com.efreight.afbase.entity.ScOrder;
import com.efreight.afbase.entity.TcOrder;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * <p>
 * AF 延伸服务 应收 Mapper 接口
 * </p>
 *
 * @author qipm
 * @since 2019-10-12
 */
public interface AfIncomeCostMapper extends BaseMapper<AfIncomeCost> {

	@Select({"<script>",
		" SELECT  ",
		" CONCAT('1',B.income_id,'0') treeId,",
		" 1 AS sstype,",
		" 0 AS cost_id,",
		" B.*,",

		" IFNULL(B.income_functional_amount,0)-IFNULL(C.cost_functional_amount,0) AS profitAmount,",
		" IFNULL(B.income_amount_not_tax,0)-IFNULL(C.cost_amount_not_tax,0) AS noTaxProfitAmount",
		" FROM af_income B",
		" LEFT JOIN (",
		"  SELECT income_id,SUM(cost_functional_amount) AS cost_functional_amount,",
		"  SUM(cost_amount_not_tax) AS cost_amount_not_tax  FROM af_cost",
		"  GROUP BY income_id",
		" ) C ON C.income_id=B.income_id \n",
		"	where B.org_id = #{org_id} and B.order_id = #{order_id} \n",
		" order by B.order_id \n",
	"</script>"})
	List<AfIncomeCostTree> getIncomeCostLst(@Param("org_id") Integer org_id,@Param("order_id") Integer order_id);
	@Select({"<script>",
		" SELECT  ",
		" CONCAT('1',B.income_id,'0') treeId,",
		" 1 AS sstype,",
		" 0 AS cost_id,",
		" B.*,",
		
		" IFNULL(B.income_functional_amount,0)-IFNULL(C.cost_functional_amount,0) AS profitAmount,",
		" IFNULL(B.income_amount_not_tax,0)-IFNULL(C.cost_amount_not_tax,0) AS noTaxProfitAmount",
		" FROM sc_income B",
		" LEFT JOIN (",
		"  SELECT income_id,SUM(cost_functional_amount) AS cost_functional_amount,",
		"  SUM(cost_amount_not_tax) AS cost_amount_not_tax  FROM sc_cost",
		"  GROUP BY income_id",
		" ) C ON C.income_id=B.income_id \n",
		"	where B.org_id = #{org_id} and B.order_id = #{order_id} \n",
		" order by B.order_id \n",
	"</script>"})
	List<AfIncomeCostTree> getIncomeCostLstBySE(@Param("org_id") Integer org_id,@Param("order_id") Integer order_id);
	@Select({"<script>",
		" SELECT ",
		" CONCAT('2',C.income_id,C.cost_id) treeId,",
		" 2 AS sstype,",
		" C.*",
		" FROM af_cost C\n",
		"	where C.org_id = #{org_id} and C.income_id = #{income_id} \n",
		" order by C.cost_id \n",
	"</script>"})
	List<AfIncomeCost> getChildrenLst(@Param("org_id") Integer org_id,@Param("income_id") Integer income_id);
	@Select({"<script>",
		" SELECT ",
		" CONCAT('2',C.income_id,C.cost_id) treeId,",
		" 2 AS sstype,",
		" C.*",
		" ,cost_unit_price incomeUnitPrice ",
		" ,cost_quantity incomeQuantity ",
		" ,cost_currency incomeCurrency ",
		" ,cost_amount incomeAmount ",
		" ,cost_functional_amount incomeFunctionalAmount ",
		" ,cost_amount_tax_rate incomeAmountTaxRate ",
		" ,cost_amount_not_tax incomeAmountNotTax ",
		" ,cost_amount_tax incomeAmountTax ",
		" FROM af_cost C\n",
		"	where C.org_id = #{org_id} and C.income_id = #{income_id} \n",
		" order by C.cost_id \n",
	"</script>"})
	List<AfIncomeCost> getChildrenLst2(@Param("org_id") Integer org_id,@Param("income_id") Integer income_id);
	@Select({"<script>",
		" SELECT ",
		" CONCAT('2',C.income_id,C.cost_id) treeId,",
		" 2 AS sstype,",
		" C.*",
		" ,cost_unit_price incomeUnitPrice ",
		" ,cost_quantity incomeQuantity ",
		" ,cost_currency incomeCurrency ",
		" ,cost_amount incomeAmount ",
		" ,cost_functional_amount incomeFunctionalAmount ",
		" ,cost_amount_tax_rate incomeAmountTaxRate ",
		" ,cost_amount_not_tax incomeAmountNotTax ",
		" ,cost_amount_tax incomeAmountTax ",
		" FROM sc_cost C\n",
		"	where C.org_id = #{org_id} and C.income_id = #{income_id} \n",
		" order by C.cost_id \n",
	"</script>"})
	List<AfIncomeCost> getChildrenLstBySE2(@Param("org_id") Integer org_id,@Param("income_id") Integer income_id);
	
	
	
	@Select({"<script>",
		" SELECT  ",
		"a.*,b.debit_note_num",
		" FROM af_income a",
		" left join css_debit_note b on a.debit_note_id=b.debit_note_id",
		"	where a.org_id = #{org_id} and a.order_id = #{order_id} ",
		" order by a.income_id",
	"</script>"})
	List<AfIncome> getIncomeList(@Param("org_id") Integer org_id,@Param("order_id") Integer order_id);
	@Select({"<script>",
		" SELECT  ",
		"A.*,B.payment_num",
		" FROM af_cost A",
		" LEFT JOIN (",
		" 	 SELECT ",
		" 	  A.org_id,A.cost_id,GROUP_CONCAT(B.payment_num) AS payment_num",
		" 	 FROM css_payment_detail A ",
		" 	 INNER JOIN css_payment B ON A.payment_id=B.payment_id and (B.business_scope='AE' or B.business_scope='AI')",
		" 	 GROUP BY A.org_id,A.cost_id",
		" ) AS B ON A.cost_id=B.cost_id AND A.org_id=B.org_id",
		"	where A.org_id = #{org_id} and A.order_id = #{order_id} ",
		" order by A.cost_id \n",
	"</script>"})
	List<AfCost> getCostList(@Param("org_id") Integer org_id,@Param("order_id") Integer order_id);
	@Select({"<script>",
		" SELECT  ",
		"a.*,b.debit_note_num",
		" FROM sc_income a",
		" left join css_debit_note b on a.debit_note_id=b.debit_note_id",
		"	where a.org_id = #{org_id} and a.order_id = #{order_id} ",
		" order by a.income_id",
	"</script>"})
	List<AfIncome> getIncomeListSE(@Param("org_id") Integer org_id,@Param("order_id") Integer order_id);
	@Select({"<script>",
		" SELECT  ",
		"A.*,B.payment_num",
		" FROM sc_cost A",
		" LEFT JOIN (",
		" 	 SELECT ",
		" 	  A.org_id,A.cost_id,GROUP_CONCAT(B.payment_num) AS payment_num",
		" 	 FROM css_payment_detail A ",
		" 	 INNER JOIN css_payment B ON A.payment_id=B.payment_id and (B.business_scope='SE' or B.business_scope='SI')",
		" 	 GROUP BY A.org_id,A.cost_id",
		" ) AS B ON A.cost_id=B.cost_id AND A.org_id=B.org_id",
		"	where A.org_id = #{org_id} and A.order_id = #{order_id} ",
		" order by A.cost_id \n",
	"</script>"})
	List<AfCost> getCostListSE(@Param("org_id") Integer org_id,@Param("order_id") Integer order_id);
	
	@Select({"<script>",
		" SELECT  ",
		"a.*,b.debit_note_num",
		" FROM tc_income a",
		" left join css_debit_note b on a.debit_note_id=b.debit_note_id",
		"	where a.org_id = #{org_id} and a.order_id = #{order_id} ",
		" order by a.income_id",
	"</script>"})
	List<AfIncome> getIncomeListTE(@Param("org_id") Integer org_id,@Param("order_id") Integer order_id);
	@Select({"<script>",
		" SELECT  ",
		"A.*,B.payment_num",
		" FROM tc_cost A",
		" LEFT JOIN (",
		" 	 SELECT ",
		" 	  A.org_id,A.cost_id,GROUP_CONCAT(B.payment_num) AS payment_num",
		" 	 FROM css_payment_detail A ",
		" 	 INNER JOIN css_payment B ON A.payment_id=B.payment_id and (B.business_scope='TE' or B.business_scope='TI')",
		" 	 GROUP BY A.org_id,A.cost_id",
		" ) AS B ON A.cost_id=B.cost_id AND A.org_id=B.org_id",
		"	where A.org_id = #{org_id} and A.order_id = #{order_id} ",
		" order by A.cost_id \n",
	"</script>"})
	List<AfCost> getCostListTE(@Param("org_id") Integer org_id,@Param("order_id") Integer order_id);
	
	
	@Select({"<script>",
		" SELECT  ",
		"a.income_charge_standard debitNoteNum,",
		"a.service_id serviceId,",
		"CONCAT(a.service_type,' - ',a.service_name_cn) serviceName  ,",
		"IFNULL(CONCAT(a.income_currency,' ',FORMAT(a.income_unit_price, 2),' / ',a.income_charge_standard),'') serviceRemark  ,",
		"a.income_unit_price incomeUnitPrice,",
		"a.income_currency incomeCurrency,",
		
		"a.income_amount_min serviceAmountMin,",
		"a.income_amount_max serviceAmountMax,",
		"a.income_amount_digits serviceAmountDigits,",
		"a.income_amount_carry serviceAmountCarry,",
		
		"0 incomeAmountTaxRate,",
		"b.currency_rate incomeExchangeRate",
		"FROM af_service a",
		"LEFT JOIN af_V_currency_rate b ON a.income_currency=b.currency_code",
		"WHERE a.org_id=#{org_id} and a.is_valid=1 and b.org_id=#{org_id} AND a.business_scope=#{business_scope} AND a.default_income=1",
	"</script>"})
	List<AfIncome> getIncomeTemplate(@Param("org_id") Integer org_id,@Param("business_scope") String business_scope);
	@Select({"<script>",
		"select ",
		"a.service_charge_standard debitNoteNum,",
		"a.service_id serviceId,",
		"CONCAT(b.service_type,' - ',b.service_name_cn) serviceName  ,",
		"a.customer_id customerId,",
		"d.coop_name customerName,",
		"IFNULL(CONCAT(a.service_currency,' ',FORMAT(a.service_unit_price, 2),' / ',a.service_charge_standard),'') serviceRemark  ,",
		"a.service_unit_price incomeUnitPrice,",
		"a.service_currency incomeCurrency,",
		
		"a.service_amount_min serviceAmountMin,",
		"a.service_amount_max serviceAmountMax,",
		"a.service_amount_digits serviceAmountDigits,",
		"a.service_amount_carry serviceAmountCarry,",
		
		"0 incomeAmountTaxRate,",
		"c.currency_rate incomeExchangeRate",
		"from af_service_template a",
		"left join af_service b on a.service_id=b.service_id",
		"LEFT JOIN af_V_currency_rate c ON a.service_currency=c.currency_code",
		" left join prm_coop d on a.customer_id=d.coop_id",
		"	where a.org_id = #{org_id} and b.org_id = #{org_id} and c.org_id = #{org_id} and a.business_scope = #{business_scope} and a.template_type = #{template_type} and a.template_code = #{template_code}",
	"</script>"})
	List<AfIncome> getIncomeTemplate2(@Param("org_id") Integer org_id, @Param("business_scope") String business_scope,@Param("template_type") Integer template_type,@Param("template_code") String template_code);
	@Select({"<script>",
		" SELECT  ",
		"a.income_charge_standard debitNoteNum,",
		"a.service_id serviceId,",
		"CONCAT(a.service_type,' - ',a.service_name_cn) serviceName  ,",
		"'' serviceRemark  ,",
		"a.income_unit_price incomeUnitPrice,",
		"a.income_currency incomeCurrency,",
		"a.income_amount_min serviceAmountMin,",
		"a.income_amount_max serviceAmountMax,",
		"a.income_amount_digits serviceAmountDigits,",
		"a.income_amount_carry serviceAmountCarry,",
		"0 incomeAmountTaxRate,",
		"b.currency_rate incomeExchangeRate",
		"FROM af_service a",
		"LEFT JOIN af_V_currency_rate b ON a.income_currency=b.currency_code",
		"WHERE a.org_id=#{org_id} and a.is_valid=1 and b.org_id=#{org_id} AND a.business_scope=#{business_scope} AND a.service_code='GX01'",
	"</script>"})
	AfIncome getIncome(@Param("org_id") Integer org_id,@Param("business_scope") String business_scope);
	@Select({"<script>",
		" SELECT  ",
		"a.cost_charge_standard debitNoteNum,",
		"a.service_id serviceId,",
		"CONCAT(a.service_type,' - ',a.service_name_cn) serviceName  ,",
		"IFNULL(CONCAT(a.cost_currency,' ',FORMAT(a.cost_unit_price, 2),' / ',a.cost_charge_standard),'') serviceRemark  ,",
		"a.cost_unit_price costUnitPrice,",
		"a.cost_currency costCurrency,",
		"a.cost_amount_min serviceAmountMin,",
		"a.cost_amount_max serviceAmountMax,",
		"a.cost_amount_digits serviceAmountDigits,",
		"a.cost_amount_carry serviceAmountCarry,",
		"0 costAmountTaxRate,",
		"b.currency_rate costExchangeRate",
		"FROM af_service a",
		"LEFT JOIN af_V_currency_rate b ON a.cost_currency=b.currency_code",
		"WHERE a.org_id=#{org_id} and a.is_valid=1  and b.org_id=#{org_id} AND a.business_scope=#{business_scope} AND a.default_cost=1",
	"</script>"})
	List<AfCost> getCostTemplate(@Param("org_id") Integer org_id,@Param("business_scope") String business_scope);
	@Select({"<script>",
		"select ",
		"a.service_charge_standard debitNoteNum,",
		"a.service_id serviceId,",
		"CONCAT(b.service_type,' - ',b.service_name_cn) serviceName  ,",
		"a.customer_id customerId,",
		"d.coop_name customerName,",
		"IFNULL(CONCAT(a.service_currency,' ',FORMAT(a.service_unit_price, 2),' / ',a.service_charge_standard),'') serviceRemark  ,",
		"a.service_unit_price costUnitPrice,",
		"a.service_currency costCurrency,",
		
		"a.service_amount_min serviceAmountMin,",
		"a.service_amount_max serviceAmountMax,",
		"a.service_amount_digits serviceAmountDigits,",
		"a.service_amount_carry serviceAmountCarry,",
		
		"0 costAmountTaxRate,",
		"c.currency_rate costExchangeRate",
		"from af_service_template a",
		"left join af_service b on a.service_id=b.service_id",
		"LEFT JOIN af_V_currency_rate c ON a.service_currency=c.currency_code",
		" left join prm_coop d on a.customer_id=d.coop_id",
		"	where a.org_id = #{org_id} and b.org_id = #{org_id} and c.org_id = #{org_id}  and a.business_scope = #{business_scope} and a.template_type = #{template_type} and a.template_code = #{template_code}",
	"</script>"})
	List<AfCost> getCostTemplate2(@Param("org_id") Integer org_id, @Param("business_scope") String business_scope,@Param("template_type") Integer template_type,@Param("template_code") String template_code);
	@Select({"<script>",
		" SELECT  ",
		"a.cost_charge_standard debitNoteNum,",
		"a.service_id serviceId,",
		"CONCAT(a.service_type,' - ',a.service_name_cn) serviceName  ,",
		"'' serviceRemark  ,",
		"a.cost_unit_price costUnitPrice,",
		"a.cost_currency costCurrency,",
		"a.cost_amount_min serviceAmountMin,",
		"a.cost_amount_max serviceAmountMax,",
		"a.cost_amount_digits serviceAmountDigits,",
		"a.cost_amount_carry serviceAmountCarry,",
		"0 costAmountTaxRate,",
		"b.currency_rate costExchangeRate",
		"FROM af_service a",
		"LEFT JOIN af_V_currency_rate b ON a.cost_currency=b.currency_code",
		"WHERE a.org_id=#{org_id} and a.is_valid=1  and b.org_id=#{org_id} AND a.business_scope=#{business_scope} AND a.service_code='GX01'",
	"</script>"})
	AfCost getCost(@Param("org_id") Integer org_id,@Param("business_scope") String business_scope);
	@Select({"<script>",
		" SELECT  ",
		"a.*,b.awb_from_id,b.awb_from_name,c.currency_rate incomeExchangeRate,d.currency_rate costExchangeRate",
		"FROM af_order a",
		"left join af_awb_number b on a.awb_id=b.awb_id",
		"LEFT JOIN af_V_currency_rate c ON a.currecny_code=c.currency_code",
		"LEFT JOIN af_V_currency_rate d ON a.msr_currecny_code=d.currency_code",
		"WHERE a.org_id=#{org_id} AND a.order_id=#{order_id} and c.org_id=#{org_id} and d.org_id=#{org_id}",
	"</script>"})
	AfOrder getOrder(@Param("org_id") Integer org_id,@Param("order_id") Integer order_id);
	@Select({"<script>",
		" SELECT  ",
		"a.*",
		"FROM sc_order a",
		"WHERE a.org_id=#{org_id} AND a.order_id=#{order_id} ",
	"</script>"})
	ScOrder getOrder2(@Param("org_id") Integer org_id,@Param("order_id") Integer order_id);
	@Select({"<script>",
		" SELECT  ",
		"a.*",
		"FROM tc_order a",
		"WHERE a.org_id=#{org_id} AND a.order_id=#{order_id} ",
	"</script>"})
	TcOrder getOrderTC(@Param("org_id") Integer org_id,@Param("order_id") Integer order_id);
	
	@Delete({"<script>",
		" delete  ",
		"FROM af_income ",
		"WHERE org_id=#{org_id} AND income_id=#{income_id} and debit_note_id IS NULL and financial_date IS NULL",
	"</script>"})
	void deleteIncome(@Param("org_id") Integer org_id,@Param("income_id") Integer income_id);
	@Delete({"<script>",
		" delete  ",
		"FROM af_cost ",
		"WHERE org_id=#{org_id} AND cost_id=#{cost_id} and payment_id IS NULL and financial_date IS NULL",
	"</script>"})
	void deleteCost(@Param("org_id") Integer org_id,@Param("cost_id") Integer cost_id);
	@Delete({"<script>",
		" delete  ",
		"FROM sc_income ",
		"WHERE org_id=#{org_id} AND income_id=#{income_id} and debit_note_id IS NULL and financial_date IS NULL",
	"</script>"})
	void deleteIncomeSE(@Param("org_id") Integer org_id,@Param("income_id") Integer income_id);
	@Delete({"<script>",
		" delete  ",
		"FROM sc_cost ",
		"WHERE org_id=#{org_id} AND cost_id=#{cost_id} and payment_id IS NULL and financial_date IS NULL",
	"</script>"})
	void deleteCostSE(@Param("org_id") Integer org_id,@Param("cost_id") Integer cost_id);
	@Delete({"<script>",
		" delete  ",
		"FROM tc_income ",
		"WHERE org_id=#{org_id} AND income_id=#{income_id} and debit_note_id IS NULL and financial_date IS NULL",
	"</script>"})
	void deleteIncomeTC(@Param("org_id") Integer org_id,@Param("income_id") Integer income_id);
	@Delete({"<script>",
		" delete  ",
		"FROM tc_cost ",
		"WHERE org_id=#{org_id} AND cost_id=#{cost_id} and payment_id IS NULL and financial_date IS NULL",
	"</script>"})
	void deleteCostTC(@Param("org_id") Integer org_id,@Param("cost_id") Integer cost_id);
	@Select({"<script>",
		" SELECT order_id,sum(IFNULL(pieces,0)) AS income_quantity FROM  ",
		"<when test='table_name==\"sc\"'>",
		" sc_order_container_details ",
		"</when>",
		"<when test='table_name==\"tc\"'>",
		" tc_order_container_details ",
		"</when>",
		"WHERE org_id=#{org_id} AND order_id=#{order_id}  AND CONCAT(IFNULL(container_size,0),IFNULL(container_code,''))=#{type}  group by order_id",
	"</script>"})
	List<AfIncome> getServiceStandard(@Param("org_id") Integer org_id,@Param("order_id") Integer order_id,@Param("table_name") String table_name,@Param("type") String type);
	
}
