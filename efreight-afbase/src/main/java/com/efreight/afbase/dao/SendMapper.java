package com.efreight.afbase.dao;


import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.efreight.afbase.entity.AfOrder;
import com.efreight.afbase.entity.CargoGoodsnames;
import com.efreight.afbase.entity.OrgInterface;


public interface SendMapper extends BaseMapper<AfOrder> {

	@Select("SELECT org_api_config_id orgApiConfigId,api_type apiType,api_remark apiRemark,enable enable,appid appid,auth_token authToken,platform platform,function function,url_auth urlAuth,url_post urlPost" +
            " FROM hrs_org_api_config "
            + " WHERE org_id=#{orgId} and api_type=#{apiType} and enable=1 order by create_time desc limit 0,1")
	OrgInterface getShippingBillConfig(@Param("orgId") Integer orgId, @Param("apiType") String apiType);
	@Select({"<script>",
        "select a.*,b.coop_name from af_order a",
        "left join prm_coop b on a.coop_id=b.coop_id",
        "	where a.org_id = #{org_id} and a.order_uuid = #{order_uuid}",
        "</script>"})
	AfOrder getOrderByUUID(@Param("org_id") Integer org_id, @Param("order_uuid") String order_uuid);
	@Select({"<script>",
		"select a.*,b.coop_name from af_order a",
		"left join prm_coop b on a.coop_id=b.coop_id",
		"	where a.org_id = #{org_id} and a.order_code = #{order_code}",
	"</script>"})
	AfOrder getOrderNumber(@Param("org_id") Integer org_id, @Param("order_code") String order_code);
	
	
	@Select("CALL af_P_API_OP_HAWB_MFT(#{orderUUID}, #{letterIds}, #{userId}, #{apiType})")
    String getNewHAWBXML(@Param("orderUUID") String orderUUID, @Param("letterIds") String letterIds, @Param("userId") Integer userId, @Param("apiType") String apiType);

    @Select("CALL af_P_API_OP_MFT(#{orderUUID}, #{userId}, #{apiType})")
    String getNewMAWBXML(@Param("orderUUID") String orderUUID, @Param("userId") Integer userId, @Param("apiType") String apiType);
    @Select({"<script>",
		"select * from af_cargo_goodsnames a",
		"	where a.order_id = #{order_id}",
	"</script>"})
    List<CargoGoodsnames> querylist(@Param("order_id") Integer order_id);
}
