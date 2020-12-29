package com.efreight.afbase.dao;


import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.efreight.afbase.entity.AfOrder;
import com.efreight.afbase.entity.AfOrderShipperConsignee;
import com.efreight.afbase.entity.OrgInterface;


public interface FhlOperationMapper extends BaseMapper<AfOrder> {


    @Select({"<script>",
		"SELECT a.org_code orgCode,",
		"CASE WHEN b.social_credit_code IS NULL THEN '' ELSE b.social_credit_code END socialCreditCode",
		"FROM hrs_org a",
		"LEFT JOIN prm_coop b ON a.coop_id=b.coop_id",
		"where a.org_id= #{org_id}",
	"</script>"})
	Map<String,String> getOrgMap(@Param("org_id") Integer org_id);
    
    
    @Select("SELECT org_api_config_id orgApiConfigId,api_type apiType,api_remark apiRemark,enable enable,appid appid,auth_token authToken,platform platform,function function,url_auth urlAuth,url_post urlPost" +
            " FROM hrs_org_api_config "
            + " WHERE org_id=#{org_id} and api_type=#{apiType} and enable=1 order by create_time desc limit 0,1")
	OrgInterface getShippingBillConfig(@Param("org_id") Integer org_id, @Param("apiType") String apiType);
    
    @Delete("delete from af_order where business_scope='AI' and org_id=#{org_id} and awb_number=#{awb_number} and hawb_number=#{hawb_number}")
    void deleteFhl(@Param("org_id") Integer org_id, @Param("awb_number") String awb_number, @Param("hawb_number") String hawb_number);
    
    @Select("SELECT * " +
            " FROM af_order "
            + " where business_scope='AI' and org_id=#{org_id} and awb_number=#{awb_number} and hawb_number=#{hawb_number}"
            + " order by create_time desc limit 0,1")
    AfOrder getAiOrder(@Param("org_id") Integer org_id, @Param("awb_number") String awb_number, @Param("hawb_number") String hawb_number);
    @Select("SELECT * " +
    		" FROM af_order_shipper_consignee "
    		+ " where org_id=#{org_id} and order_id=#{order_id} and sc_type=#{sc_type}"
    		+ " order by create_time desc limit 0,1")
    AfOrderShipperConsignee getShipperConsignee(@Param("org_id") Integer org_id, @Param("order_id") Integer order_id, @Param("sc_type") Integer sc_type);
    
}
