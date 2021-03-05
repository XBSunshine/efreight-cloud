package com.efreight.afbase.dao;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.efreight.afbase.entity.AwbNumber;
import com.efreight.afbase.entity.LogBean;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

public interface AwbNumberMapper extends BaseMapper<AwbNumber> {

    @Select({"<script>",
            "select param_text value,param_text label ,EDICode1 code from af_category\n",
            "		where is_valid=1 and category_name = #{category}\n",
            "		ORDER BY param_ranking\n",
            "</script>"})
    List<Map<String, Object>> selectCategory(@Param("category") String category);
    @Select({"<script>",
        "select param_text value,param_text label ,EDICode1 code from af_category\n",
        "		where is_valid=1 and category_name = #{category} AND EDICode1='SIGN'",
        "		ORDER BY param_ranking ",
        "</script>"})
    List<Map<String, Object>> selectCategorySign(@Param("category") String category);

    @Select({"<script>",
            "select param_text value,param_text label ,EDICode1 code from af_V_prm_category\n",
            "		where EDICode1=#{businessScope} and category_name = #{category}\n",
            "</script>"})
    List<Map<String, Object>> selectCategory2(@Param("category") String category, @Param("businessScope") String businessScope);
    @Select({"<script>",
    	"select param_text value,param_text label,EDICode1 code from af_category_pro\n",
    	"		where ap_code=#{departureStation} and category_name = #{category}\n",
    	"ORDER BY param_ranking",
    "</script>"})
    List<Map<String, Object>> selectCategoryPro(@Param("category") String category, @Param("departureStation") String departureStation);

    @Select({"<script>",
            "SELECT warehouse_id value,warehouse_code value2, warehouse_name_cn label from af_warehouse\n",
            "	where org_id=#{org_id} and  business_scope='AE'",
            "  and customs_supervision like \"%\"#{warehouse}\"%\"",
            " ORDER BY warehouse_code\n",
            "</script>"})
    List<Map<String, Object>> selectWarehouse(@Param("org_id") Integer org_id, @Param("warehouse") String warehouse);

    @Select({"<script>",
            "select * from af_carrier\n",
            "	where carrier_prefix = #{carrier_prefix}\n",
            "</script>"})
    List<Map<String, Object>> selectCarrier(@Param("carrier_prefix") String carrier_prefix);

    @Select({"<script>",
            "SELECT org_id orgIdV,coop_id coopIdV,coop_type coopTypeV,coop_code coopCodeV,coop_mnemonic coopMnemonicV,coop_name coopNameV,short_name shortNameV",
            " FROM af_V_prm_coop",
            " WHERE business_scope_AE='AE' and coop_type in (${coop_type})",
            " AND org_id =#{org_id}",
            "<when test='coop_mnemonic!=null and coop_mnemonic!=\"\"'>",
            " AND coop_code like  \"%\"#{coop_mnemonic}\"%\"",
            "</when>",
            "<when test='coop_name!=null and coop_name!=\"\"'>",
            " AND coop_name like  \"%\"#{coop_name}\"%\"",
            "</when>",
            "</script>"})
    IPage<AwbNumber> getSelectListPage(Page page, @Param("org_id") Integer org_id, @Param("coop_type") String coop_type, @Param("coop_mnemonic") String coop_mnemonic, @Param("coop_name") String coop_name);

    @Select({"<script>",
            "SELECT org_id orgIdV,coop_id coopIdV,coop_type coopTypeV,coop_code coopCodeV,coop_mnemonic coopMnemonicV,coop_name coopNameV,short_name shortNameV",
            " ,CONCAT(coop_name,' ',IFNULL(coop_mnemonic,'')) VALUE",
            " FROM af_V_prm_coop",
            " WHERE  coop_type in (${coop_type})",
            " AND org_id =#{org_id}",
            "<when test='coop_mnemonic!=null and coop_mnemonic!=\"\"'>",
            " AND coop_mnemonic like  \"%\"#{coop_mnemonic}\"%\"",
            "</when>",
            "<when test='coop_name!=null and coop_name!=\"\"'>",
            " AND coop_name like  \"%\"#{coop_name}\"%\"",
            "</when>",
            "<when test='business_scope!=null and business_scope==\"AE\"'>",
            " AND business_scope_AE='AE'",
            "</when>",
            "<when test='business_scope!=null and business_scope==\"AI\"'>",
            " AND business_scope_AI='AI'",
            "</when>",
            "<when test='business_scope!=null and business_scope==\"SE\"'>",
            " AND business_scope_SE='SE'",
            "</when>",
            "<when test='business_scope!=null and business_scope==\"SI\"'>",
            " AND business_scope_SI='SI'",
            "</when>",
            "<when test='business_scope!=null and business_scope==\"TE\"'>",
            " AND business_scope_TE='TE'",
            "</when>",
            "<when test='business_scope!=null and business_scope==\"TI\"'>",
            " AND business_scope_TI='TI'",
            "</when>",
            "<when test='business_scope!=null and business_scope==\"LC\"'>",
            " AND business_scope_LC='LC'",
            "</when>",
            "</script>"})
    List<AwbNumber> queryList(@Param("org_id") Integer org_id, @Param("coop_type") String coop_type, @Param("coop_mnemonic") String coop_mnemonic, @Param("coop_name") String coop_name, @Param("business_scope") String business_scope);

    @Insert("insert into af_awb_number \n"
            + " ( awb_uuid,org_id,awb_number,awb_status,departure_station,awb_from_type,awb_from_id,awb_from_name,creator_id,creator_name,creat_time) \n"
            + "	 values (uuid(),#{bean.orgId},#{bean.awbNumber},'未使用',#{bean.departureStation},#{bean.awbFromType},#{bean.awbFromId},#{bean.awbFromName}"
            + " ,#{bean.creatorId},#{bean.creatorName},#{bean.creatTime})\n")
    void insertAwbNumber(@Param("bean") AwbNumber bean);

    @Update("update af_awb_number set\n"
            + " reserved_coop_id=#{bean.reservedCoopId}, reserved_coop_name=#{bean.reservedCoopName},\n"
            + "	 reserved_user_id=#{bean.reservedUserId},reserved_user=#{bean.reservedUser}, reserved_time=#{bean.reservedTime}"
            + " where awb_status='未使用' and  org_id=#{bean.orgId} and awb_id in (${bean.awbIds})\n")
    void updateAwbNumber(@Param("bean") AwbNumber bean);

    @Update("update af_awb_number set\n"
            + " reserved_coop_id=#{bean.reservedCoopId}, reserved_coop_name=#{bean.reservedCoopName},\n"
            + "	 reserved_user_id=#{bean.reservedUserId},reserved_user=#{bean.reservedUser}, reserved_time=#{bean.reservedTime}"
            + " where awb_status='未使用' and org_id=#{bean.orgId} and awb_id in (${bean.awbIds})\n")
    void cancelBook(@Param("bean") AwbNumber bean);

    @Update("update af_awb_number set\n"
            + " locker_id=#{bean.lockerId}, locker_name=#{bean.lockerName},awb_status='已锁定',\n"
            + "	 lock_time=#{bean.lockTime}"
            + " where awb_status='未使用' and org_id=#{bean.orgId} and awb_id in (${bean.awbIds})\n")
    void doLock(@Param("bean") AwbNumber bean);

    @Update("update af_awb_number set\n"
            + " locker_id=#{bean.lockerId}, locker_name=#{bean.lockerName},awb_status='未使用',\n"
            + "	 lock_time=#{bean.lockTime}"
            + " where awb_status='未使用' and org_id=#{bean.orgId} and awb_id in (${bean.awbIds})\n")
    void doCancelLock(@Param("bean") AwbNumber bean);

    @Delete("delete from af_awb_number \n"
            + " where awb_status='未使用' and org_id=#{bean.orgId} and awb_id in (${bean.awbIds})\n")
    void doDelete(@Param("bean") AwbNumber bean);

    @Select({"<script>",
            "select param_text value,param_text label ,param_ranking code from af_V_prm_category\n",
            "		where  category_name = #{category}\n",
            "</script>"})
    List<Map<String, Object>> selectVCategory(@Param("category") String category);

    @Select({"<script>",
            "select * from (\n" +
                    "\n" +
                    "SELECT\n" +
                    "\tpage_name pageName,\n" +
                    "\tpage_function pageFunction,\n" +
                    "\tCONCAT(order_number,' ', log_remark) logRemark,\n" +
                    "IF(LOCATE('@', creator_name) > 0, REPLACE(creator_name, substring_index(creator_name, ' ', -1), ''), creator_name) AS creatorName," +
                    "\tcreat_time creatTime\n" +
                    "FROM\n" +
                    "\taf_log \n" +
                    "WHERE\n" +
                    "\torg_id = #{bean.orgId} \n" +
                    "\tAND business_scope = 'AE' \n" +
                    "\tAND log_remark LIKE \"%\"#{bean.awbNumber}\"%\" \n" +
                    "\tAND page_function != '修改订单' \n" +
                    "UNION ALL\n" +
                    "SELECT\n" +
                    "\t'主单号管理' pageName,\n" +
                    "\t'主单创建' pageFunction,\n" +
                    "\tCONCAT('主运单号：', awb_number) logRemark,\n" +
                    "IF(LOCATE('@', creator_name) > 0, REPLACE(creator_name, substring_index(creator_name, ' ', -1), ''), creator_name) AS creatorName," +
                    "\tcreat_time creatTime\n" +
                    "FROM\n" +
                    "\taf_awb_number \n" +
                    "WHERE\n" +
                    "\torg_id = #{bean.orgId} \n" +
                    "\tand awb_number = #{bean.awbNumber}) AS T\n" +
                    "order by T.creatTime desc",
            "</script>"})
    IPage<LogBean> awbLogPage(Page page, @Param("bean") LogBean bean);
}
