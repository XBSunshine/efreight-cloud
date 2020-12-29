package com.efreight.afbase.dao;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.efreight.afbase.entity.*;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * <p>
 * AF 延伸服务 应收 Mapper 接口
 * </p>
 *
 * @author qipm
 * @since 2019-10-12
 */
public interface AfIncomeMapper extends BaseMapper<AfIncome> {

	@Delete("delete from af_cost \n"
			+ " where income_id=#{income_id} and org_id=#{org_id} \n")
	void deleteCostByIncomeId(@Param("org_id") Integer org_id,@Param("income_id") Integer income_id);
	@Delete("delete from sc_cost \n"
			+ " where income_id=#{income_id} and org_id=#{org_id} \n")
	void deleteCostByIncomeIdSE(@Param("org_id") Integer org_id,@Param("income_id") Integer income_id);
	@Delete("delete from tc_cost \n"
			+ " where income_id=#{income_id} and org_id=#{org_id} \n")
	void deleteCostByIncomeIdTC(@Param("org_id") Integer org_id,@Param("income_id") Integer income_id);
	@Delete("delete from af_cost \n"
			+ " where income_id=#{income_id} and org_id=#{org_id} and payment_id IS NULL\n")
	void deleteCostByIncomeId2(@Param("org_id") Integer org_id,@Param("income_id") Integer income_id);
	@Delete("delete from sc_cost \n"
			+ " where income_id=#{income_id} and org_id=#{org_id} and payment_id IS NULL\n")
	void deleteCostBySEIncomeId(@Param("org_id") Integer org_id,@Param("income_id") Integer income_id);
	@Select("select * from af_cost \n"
			+ " where income_id=#{income_id} and org_id=#{org_id} \n")
	List<AfCost> queryByIncomeId(@Param("org_id") Integer org_id,@Param("income_id") Integer income_id);
	@Select("select * from sc_cost \n"
			+ " where income_id=#{income_id} and org_id=#{org_id} \n")
	List<AfCost> queryByIncomeIdSE(@Param("org_id") Integer org_id,@Param("income_id") Integer income_id);
	@Select("select * from tc_cost \n"
			+ " where income_id=#{income_id} and org_id=#{org_id} \n")
	List<AfCost> queryByIncomeIdTC(@Param("org_id") Integer org_id,@Param("income_id") Integer income_id);
	@Select({"<script>",
			"select * from ",
			"<when test='businessScope==\"AI\" or businessScope==\"AE\"'>",
			"af_income ",
	        "</when>",
	        "<when test='businessScope==\"SI\" or businessScope==\"SE\"'>",
	        "sc_income ",
	        "</when>",
			 " where order_uuid=#{order_uuid} and org_id=#{org_id}  and debit_note_id IS NOT NULL\n",
			"</script>"})
	List<AfIncome> queryAfIncomeList(@Param("org_id") Integer org_id,@Param("order_uuid") String order_uuid,@Param("businessScope") String businessScope);

	@Select("<script>select a.* from (select * from af_income ${ew.customSqlSegment}) a left join af_order b on a.order_id=b.order_id order by <when test='businessScope==\"AE\"'>b.expect_departure</when><when test='businessScope==\"AI\"'>b.expect_arrival</when> desc</script>")
    IPage<AfIncome> getPageForAF(Page page,@Param(Constants.WRAPPER) LambdaQueryWrapper<AfIncome> wrapperForAF,@Param("businessScope") String businessScope);
	@Select("<script>select a.* from (select * from sc_income ${ew.customSqlSegment}) a left join sc_order b on a.order_id=b.order_id order by <when test='businessScope==\"SE\"'>b.expect_departure</when><when test='businessScope==\"SI\"'>b.expect_arrival</when> desc</script>")
	IPage<ScIncome> getPageForSC(Page page,@Param(Constants.WRAPPER) LambdaQueryWrapper<ScIncome> wrapperForSC,@Param("businessScope") String businessScope);
	
	@Select("<script>select a.* from (select * from tc_income ${ew.customSqlSegment}) a left join tc_order b on a.order_id=b.order_id order by <when test='businessScope==\"TE\"'>b.expect_departure</when><when test='businessScope==\"TI\"'>b.expect_arrival</when> desc</script>")
	IPage<TcIncome> getPageForTC(Page page,@Param(Constants.WRAPPER) LambdaQueryWrapper<TcIncome> wrapperForTC,@Param("businessScope") String businessScope);
	
	@Select("<script>select a.* from (select * from lc_income ${ew.customSqlSegment}) a left join lc_order b on a.order_id=b.order_id order by b.driving_time desc</script>")
	IPage<LcIncome> getPageForLC(Page page,@Param(Constants.WRAPPER) LambdaQueryWrapper<LcIncome> wrapperForTC,@Param("businessScope") String businessScope);

	@Select("<script>select a.* from (select * from io_income ${ew.customSqlSegment}) a left join io_order b on a.order_id=b.order_id order by b.business_date desc</script>")
	IPage<IoIncome> getPageForIO(Page page, @Param(Constants.WRAPPER) LambdaQueryWrapper<IoIncome> wrapper);
}
