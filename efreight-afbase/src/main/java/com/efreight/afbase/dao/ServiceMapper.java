package com.efreight.afbase.dao;


import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.efreight.afbase.entity.Service;

public interface ServiceMapper extends BaseMapper<Service> {
	@Select({"<script>",
		" SELECT  ",
		"  a.*,CONCAT(a.service_mnemonic,' - ',a.service_type,' - ',a.service_name_cn,CASE WHEN a.service_name_en IS NOT NULL AND a.service_name_en !='' THEN CONCAT('  <![CDATA[<]]>',IFNULL(a.service_name_en,''),'<![CDATA[>]]>') ELSE '' END) value ,",
		" b.currency_rate incomeExchangeRate,",
		" c.currency_rate costExchangeRate",
		" FROM af_service a",
		"LEFT JOIN af_V_currency_rate b ON a.income_currency=b.currency_code",
		"LEFT JOIN af_V_currency_rate c ON a.cost_currency=c.currency_code",
		"LEFT JOIN af_V_prm_category d ON a.service_type=d.param_text ",
		"	where a.org_id = #{org_id} and b.org_id=#{org_id} and c.org_id=#{org_id} AND d.category_name='服务类别' and a.is_valid=1 and a.business_scope = #{business_scope} \n",
		" order by a.service_mnemonic ASC,a.service_name_cn ASC \n",
	"</script>"})
	List<Service> queryList(@Param("org_id") Integer org_id,@Param("business_scope") String business_scope);
	@Select({"<script>",
		" SELECT  ",
		"  a.*,CONCAT(a.service_type,' - ',a.service_name_cn,' ',a.service_code) value ,",
		" b.currency_rate incomeExchangeRate,",
		" c.currency_rate costExchangeRate",
		" FROM af_service a",
		"LEFT JOIN af_V_currency_rate b ON a.income_currency=b.currency_code",
		"LEFT JOIN af_V_currency_rate c ON a.cost_currency=c.currency_code",
		"	where a.org_id = #{org_id} and b.org_id=#{org_id} and c.org_id=#{org_id} and a.is_valid=1 and a.business_scope = #{business_scope} \n",
		" order by a.is_frequent DESC,a.service_type ASC \n",
	"</script>"})
	List<Service> queryListOld(@Param("org_id") Integer org_id,@Param("business_scope") String business_scope);

	@Select({"<script>",
		"select group_id from hrs_org where org_id=#{org_id} ",
	"</script>"})
	Map queryGroupIdByOrgId(@Param("org_id") Integer org_id);
	
}
