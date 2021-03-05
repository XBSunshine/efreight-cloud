package com.efreight.afbase.dao;


import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.efreight.afbase.entity.AfOrder;
import com.efreight.afbase.entity.CargoGoodsnames;
import com.efreight.afbase.entity.OrgInterface;
import com.efreight.afbase.entity.VlEntryOrder;
import com.efreight.afbase.entity.VlEntryOrderDetail;


public interface SendMapper extends BaseMapper<AfOrder> {

	@Select("SELECT org_api_config_id orgApiConfigId,api_type apiType,api_remark apiRemark,enable enable,appid appid,auth_token authToken,platform platform,function function,url_auth urlAuth,url_post urlPost" +
            " FROM hrs_org_api_config "
            + " WHERE org_id=#{orgId} and api_type=#{apiType} and enable=1 order by create_time desc limit 0,1")
	OrgInterface getShippingBillConfig(@Param("orgId") Integer orgId, @Param("apiType") String apiType);

	@Select("SELECT mft2201_save mft2201Save FROM hrs_org_order_config where org_id=#{orgId} and business_scope=#{businessScope}")
	String selectMft2201SaveByOrgId(@Param("orgId") Integer orgId,@Param("businessScope") String businessScope);

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
    @Select({"<script>",
    	"select * from vl_entry_order ",
    	"	where entry_order_id = #{entryOrderId}",
    "</script>"})
    VlEntryOrder getVlEntryOrder(@Param("entryOrderId") Integer entryOrderId);
    @Select({"<script>",
    	"select * from vl_entry_order_detail ",
    	"	where entry_order_id = #{entryOrderId}",
    "</script>"})
    List<VlEntryOrderDetail> getVlEntryOrderDetails(@Param("entryOrderId") Integer entryOrderId);
    @Select({"<script>",
    	"SELECT param_text FROM af_category_pro  ",
    	"	WHERE category_name=#{categoryName} AND EDICode1=#{EDICode1}",
    "</script>"})
    String getHandlingCompanyName(@Param("EDICode1") String EDICode1,@Param("categoryName") String categoryName);
    @Update({"<script>",
    	"update vl_entry_order set",
    	"mft8802024_message_id =#{mft8802024_message_id},",
    	"entry_declare_id =#{entry_declare_id},",
    	"entry_declare_name =#{entry_declare_name} ",
    	"	WHERE entry_order_id=#{entry_order_id} ",
    "</script>"})
    void updateVEEntry(@Param("entry_order_id") Integer entry_order_id,@Param("mft8802024_message_id") String mft8802024_message_id,@Param("entry_declare_id") Integer entry_declare_id,@Param("entry_declare_name") String entry_declare_name);
    @Update({"<script>",
    	"update vl_entry_order set",
    	"appoint_declare_datetime =now(),",
    	"appoint_declare_status ='成功',",
    	"appoint_declare_id =#{appoint_declare_id},",
    	"appoint_declare_name =#{appoint_declare_name} ",
    	"	WHERE entry_order_id=#{entry_order_id} ",
    "</script>"})
    void updateVEAppoint(@Param("entry_order_id") Integer entry_order_id,@Param("appoint_declare_id") Integer appoint_declare_id,@Param("appoint_declare_name") String appoint_declare_name);
    
}
