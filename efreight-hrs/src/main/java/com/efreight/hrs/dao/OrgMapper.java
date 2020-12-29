package com.efreight.hrs.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.efreight.hrs.entity.AFVPRMCategory;
import com.efreight.hrs.entity.Org;
import com.efreight.hrs.entity.OrgInterface;
import com.efreight.hrs.entity.OrgTemplateConfig;
import com.efreight.hrs.pojo.org.OrgQuery;
import com.efreight.hrs.pojo.org.OrgVO;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author zhanghw
 * @since 2019-06-03
 */
public interface OrgMapper extends BaseMapper<Org> {

	@Select({"<script>",
		   " SELECT o.*,c.`black_valid`,c.`coop_status` FROM hrs_org o ",
		   " LEFT JOIN prm_coop c ON o.`coop_id`=c.`coop_id` ",
		   " WHERE 1=1 ",
		    "<when test='orgStatus!=null and orgStatus==0'>",
		    " AND (o.org_status = #{orgStatus} OR  curdate()<![CDATA[>]]>o.stop_date)",
		    "</when>",
		    "<when test='orgStatus!=null and orgStatus==1'>",
		    " AND o.org_status = #{orgStatus} AND  curdate()<![CDATA[<=]]>o.stop_date ",
		    "</when>",
		    "<when test='orgCode!=null and orgCode!=\"\"'>",
		    " AND o.org_code like  \"%\"#{orgCode}\"%\"",
		    "</when>",
		    "<when test='adminTel!=null and adminTel!=\"\"'>",
		    " AND o.admin_tel like  \"%\"#{adminTel}\"%\"",
		    "</when>",
		    "<when test='adminEmail!=null and adminEmail!=\"\"'>",
		    " AND o.admin_email like  \"%\"#{adminEmail}\"%\"",
		    "</when>",
		    "<when test='adminEmail!=null and adminEmail!=\"\"'>",
		    " AND o.demand_person_id = #{demandPersonId}",
		    "</when>",
		    "<when test='orgType!=null and orgType!=\"\"'>",
		    " AND o.org_type = #{orgType}",
		    "</when>",
		    "<when test='orgName!=null and orgName!=\"\"'>",            
		    " AND ( o.org_name like  \"%\"#{orgName}\"%\"",
		    " or  o.short_name like  \"%\"#{orgName}\"%\")",
		    "</when>",
		    "<when test='orgEname!=null and orgEname!=\"\"'>",            
		    " AND (o.org_ename like  \"%\"#{orgEname}\"%\"",
		    " or  o.short_ename like  \"%\"#{orgEname}\"%\")",
		    " </when> ",
		    "order by o.org_id desc",
		  "</script>"})
	IPage<Org> getDeptList(Page page,@Param("orgCode") String orgCode,@Param("orgName") String orgName,@Param("orgEname") String orgEname,@Param("orgStatus") Boolean orgStatus,@Param("orgType") Integer orgType,@Param("adminEmail") String adminEmail,@Param("demandPersonId") Integer demandPersonId,@Param("adminTel") String adminTel);
	
	@Select("SELECT o.*,c.`black_valid`,c.`coop_status` FROM hrs_org o "
			+ " LEFT JOIN prm_coop c ON o.`coop_id`=c.`coop_id` WHERE o.`org_code`=#{orgCode}  ")
	Org getOneByCode(@Param("orgCode") String orgCode);
	@Update("update hrs_user \n"
			+ " set user_email=#{user_email} \n"
			+ " ,phone_number=#{phone_number} \n"
			+ "	 WHERE	 user_id=#{admin_id}\n")
	void updateUser(@Param("org_id") Integer org_id,@Param("admin_id") Integer admin_id,@Param("user_email") String user_email,@Param("phone_number") String phone_number);

	@Select("CALL hrs_P_org_create(#{orgId},#{passWord},#{passWordVerification})")
    String insertCurrencyRateWithCallProcedure(@Param("orgId") Integer orgId,@Param("passWord") String passWord,@Param("passWordVerification") String passWordVerification);

    @Options(useGeneratedKeys = true, keyProperty = "orgId", keyColumn = "org_id")
    @Insert("insert into hrs_org \n"
            + " ( org_type,org_name,org_ename,org_status,org_user_count,creator_id,create_time) \n"
            + "	 values (#{bean.orgType},#{bean.orgName},#{bean.orgName},#{bean.orgStatus},#{bean.orgUserCount}"
            + " ,#{bean.creatorId},#{bean.createTime})\n")
    void insertOrg(@Param("bean") Org bean);

	@Select("SELECT org_api_config_id orgApiConfigId,api_type apiType,api_remark apiRemark,enable enable,appid appid,auth_token authToken,platform platform,function function,url_auth urlAuth,url_post urlPost" +
			" FROM hrs_org_api_config "
			+ " WHERE org_id=#{orgId}  ")
	List<OrgInterface> queryInterfaceList(@Param("orgId") Integer orgId);

	@Insert("insert into hrs_org_api_config \n"
			+ " ( org_id,api_type,appid,auth_token,platform,function,enable,url_auth,url_post,api_remark,creator_id,creator_name,create_time) \n"
			+ "	 values (#{bean.orgId},#{bean.apiType},#{bean.appid},#{bean.authToken},#{bean.platform}"
			+ " ,#{bean.function},#{bean.enable},#{bean.urlAuth},#{bean.urlPost},#{bean.apiRemark},#{bean.creatorId},#{bean.creatorName},#{bean.createTime})\n")
	void saveInterface(@Param("bean") OrgInterface bean);

	@Update("update hrs_org_api_config \n"
			+ " set api_type=#{bean.apiType} \n"
			+ " ,appid=#{bean.appid} \n"
			+ " ,auth_token=#{bean.authToken} \n"
			+ " ,platform=#{bean.platform} \n"
			+ " ,function=#{bean.function} \n"
			+ " ,enable=#{bean.enable} \n"
			+ " ,url_auth=#{bean.urlAuth} \n"
			+ " ,url_post=#{bean.urlPost} \n"
			+ " ,api_remark=#{bean.apiRemark} \n"
			+ " ,editor_id=#{bean.editorId} \n"
			+ " ,editor_name=#{bean.editorName} \n"
			+ " ,edit_time=#{bean.editTime} \n"
			+ "	 WHERE	 org_api_config_id=#{bean.orgApiConfigId}\n")
	void editInterface(@Param("bean") OrgInterface bean);

	@Select("select count(user_email) as countEmail from hrs_user where isadmin = 1 and LOWER(user_email) = #{adminEmail} and user_id != #{adminId}")
	Integer countByEmail(@Param("adminEmail") String adminEmail,@Param("adminId") Integer adminId);

	@Select("select count(user_email) as countEmail from hrs_user where isadmin = 1 and LOWER(user_email) = #{adminEmail}")
	Integer countByEmail1(@Param("adminEmail") String adminEmail);

	@Select("select count(user_email) as countEmail from hrs_user where isadmin = 0 and LOWER(user_email) = #{adminEmail}")
	Integer countByEmail2(@Param("adminEmail") String adminEmail);

    @Select("SELECT org_api_config_id orgApiConfigId,api_type apiType,api_remark apiRemark,enable enable,appid appid,auth_token authToken,platform platform,function function,url_auth urlAuth,url_post urlPost" +
            " FROM hrs_org_api_config "
            + " WHERE org_id=#{orgId} and api_type=#{apiType} and enable=1 order by create_time desc limit 0,1")
	OrgInterface getInterface(@Param("orgId") Integer orgId, @Param("apiType") String apiType);


    @Select("SELECT param_text as paramText,EDICode1 FROM af_V_prm_category WHERE category_name=#{categoryName} ")
    List<AFVPRMCategory> listCategory(@Param("categoryName") String categoryName);

	/**
	 * 查询含有某个权限企业信息
	 * @param page 分页信息
	 * @param orgQuery 查询条件
	 * @return
	 */
	@Select("<script>" +
			"SELECT \n" +
			"O.org_id,  \n" +
			"O.org_code, \n" +
			"O.org_name, \n" +
			"O.org_type AS account_type, \n" +
			"O.org_edition_id AS version_type, \n" +
			"O.create_time, " +
			"O.stop_date AS stop_time " +
			"FROM hrs_org_permission P LEFT JOIN hrs_org O \n" +
			"ON P.org_id = O.org_id \n" +
			"WHERE " +
			" O.org_status = 1" +
			" AND P.permission_id = #{query.permissionId} " +
			"<if test='query.accountType == null'>" +
				" AND O.org_type > 0" +
			"</if>" +
			"<if test='query.versionType != null'>" +
				" AND O.org_edition_id = #{query.versionType}" +
			"</if>" +
			"<if test='query.accountType != null'>" +
				" AND O.org_type =  #{query.accountType}" +
			"</if>" +
			"<if test='@org.apache.commons.lang.StringUtils@isNotBlank(query.name)'>" +
			" AND ( O.org_name LIKE CONCAT('%', #{query.name},'%') OR O.org_code LIKE CONCAT('%', #{query.name},'%') )" +
			"</if>" +
			"<if test='query.cTimeStart != null'>" +
				" AND O.create_time <![CDATA[>=]]> #{query.cTimeStart}" +
			"</if>" +
			"<if test='query.cTimeEnd != null'>" +
				" AND O.create_time <![CDATA[<=]]> #{query.cTimeEnd}" +
			"</if>" +
			"</script>")
    IPage<OrgVO> getEqPermissionOrgVoPage(Page<OrgVO> page, @Param("query")OrgQuery orgQuery);

	/**
	 * 查询不含有某个权限的企业
	 * @param page
	 * @param orgQuery
	 * @return
	 */
	@Select("<script>" +
			"SELECT \n" +
			"O.org_id,  \n" +
			"O.org_code, \n" +
			"O.org_name, \n" +
			"O.org_type AS account_type, \n" +
			"O.org_edition_id AS version_type, \n" +
			"O.create_time, " +
			"O.stop_date AS stop_time " +
			"FROM hrs_org O " +
			"WHERE " +
			" O.org_status = 1 " +
			"AND O.org_id NOT IN (" +
				"SELECT P.org_id from hrs_org_permission P WHERE  P.permission_id = #{query.permissionId}" +
			")" +
			"<if test='query.accountType == null'>" +
			" AND O.org_type > 0" +
			"</if>" +
			"<if test='query.versionType != null'>" +
			" AND O.org_edition_id = #{query.versionType}" +
			"</if>" +
			"<if test='query.accountType != null'>" +
			" AND O.org_type =  #{query.accountType}" +
			"</if>" +
			"<if test='@org.apache.commons.lang.StringUtils@isNotBlank(query.name)'>" +
			" AND ( O.org_name LIKE CONCAT('%', #{query.name},'%') OR O.org_code LIKE CONCAT('%', #{query.name},'%') )" +
			"</if>" +
			"<if test='query.cTimeStart != null'>" +
			" AND O.create_time <![CDATA[>=]]> #{query.cTimeStart}" +
			"</if>" +
			"<if test='query.cTimeEnd != null'>" +
			" AND O.create_time <![CDATA[<=]]> #{query.cTimeEnd}" +
			"</if>" +
			"</script>")
	IPage<OrgVO> getNePermissionOrgVoPage(Page<OrgVO> page, @Param("query")OrgQuery orgQuery);

	/**
	 * 修改意向客户字段
	 * @param orgId 企业ID
	 * @param status 状态 true or false
	 * @return
	 */
	@Update("update hrs_org set is_Intention_user=#{status} where org_id = #{orgId}")
	int updateIntendedUser(@Param("orgId") Integer orgId, @Param("status") Integer status);

	@Update("update hrs_dept \n"
			+ " set dept_name=#{bean.orgName} \n"
			+ " ,short_name=#{bean.orgName} \n"
			+ " ,full_name=#{bean.orgName} \n"
			+ "	 WHERE	 org_id=#{bean.orgId} and dept_code = '111'\n")
	void updateDeptName(@Param("bean") Org bean);

	@Update("UPDATE hrs_dept A\n" +
			"INNER JOIN (\n" +
			"\tSELECT\n" +
			"\t\torg_id,\n" +
			"\t\tdept_code,\n" +
			"\t\tconcat(\n" +
			"\t\t\t#{bean.orgName},\n" +
			"\t\t\tSUBSTRING(\n" +
			"\t\t\t\tfull_name,\n" +
			"\t\t\tinstr( full_name, '/' ))) AS newFullName \n" +
			"\tFROM\n" +
			"\t\thrs_dept \n" +
			"\tWHERE\n" +
			"\t\torg_id = #{bean.orgId} \n" +
			"\t\tAND dept_code != '111' \n" +
			"\t) B ON A.org_id = B.org_id \n" +
			"\tAND A.dept_code = B.dept_code \n" +
			"\tSET A.full_name = B.newFullName")
	void updateFullName(@Param("bean") Org bean);

	@Select("SELECT order_finance_lock_view FROM hrs_org WHERE org_id=#{org.orgId} ")
	Boolean getOrderFinanceLockView(@Param("org") Org org);
	@Select("SELECT finance_lock_view FROM hrs_org_order_config WHERE org_id=#{orgId} and business_scope=#{businessScope}")
	Map getOrderFinanceLockViewNew(@Param("orgId") Integer orgId,@Param("businessScope") String businessScope);
	@Select("SELECT g.* FROM hrs_org h left join hrs_org g on h.org_edition_id=g.org_id WHERE h.org_id=#{orgId} ")
	Org getOrgVersion(@Param("orgId") Integer orgId);

	@Insert("insert into hrs_org_template_config \n"
			+ " ( org_id,statement_template_ae_excel_cn,statement_template_ae_excel_en,statement_template_ai_excel_cn,statement_template_ai_excel_en,statement_template_se_excel_cn,statement_template_se_excel_en" +
			",statement_template_si_excel_cn,statement_template_si_excel_en,statement_template_te_excel_cn,statement_template_te_excel_en,statement_template_ti_excel_cn,statement_template_ti_excel_en" +
			",statement_template_lc_excel_cn,statement_template_lc_excel_en,statement_template_io_excel_cn,statement_template_io_excel_en) \n"
			+ "	 values (#{bean.orgId},#{bean.statementTemplateAeExcelCn},#{bean.statementTemplateAeExcelEn},#{bean.statementTemplateAiExcelCn},#{bean.statementTemplateAiExcelEn}"
			+ " ,#{bean.statementTemplateSeExcelCn},#{bean.statementTemplateSeExcelEn},#{bean.statementTemplateSiExcelCn},#{bean.statementTemplateSiExcelEn},#{bean.statementTemplateTeExcelCn}" +
			",#{bean.statementTemplateTeExcelEn},#{bean.statementTemplateTiExcelCn},#{bean.statementTemplateTiExcelEn},#{bean.statementTemplateLcExcelCn},#{bean.statementTemplateLcExcelEn}" +
			",#{bean.statementTemplateIoExcelCn},#{bean.statementTemplateIoExcelEn})\n")
	void insertStTemplate(@Param("bean") Org org);

	@Select("SELECT * FROM hrs_org_template_config WHERE org_id=#{orgId} limit 1 ")
	OrgTemplateConfig getStatementTemplateConfig(@Param("orgId") Integer orgId);

	@Update("update hrs_org_template_config \n"
			+ " set statement_template_ae_excel_cn=#{bean.statementTemplateAeExcelCn} \n"
			+ " ,statement_template_ae_excel_en=#{bean.statementTemplateAeExcelEn} \n"
			+ " ,statement_template_ai_excel_cn=#{bean.statementTemplateAiExcelCn} \n"
			+ " ,statement_template_ai_excel_en=#{bean.statementTemplateAiExcelEn} \n"
			+ " ,statement_template_se_excel_cn=#{bean.statementTemplateSeExcelCn} \n"
			+ " ,statement_template_se_excel_en=#{bean.statementTemplateSeExcelEn} \n"
			+ " ,statement_template_si_excel_cn=#{bean.statementTemplateSiExcelCn} \n"
			+ " ,statement_template_si_excel_en=#{bean.statementTemplateSiExcelEn} \n"
			+ " ,statement_template_te_excel_cn=#{bean.statementTemplateTeExcelCn} \n"
			+ " ,statement_template_te_excel_en=#{bean.statementTemplateTeExcelEn} \n"
			+ " ,statement_template_ti_excel_cn=#{bean.statementTemplateTiExcelCn} \n"
			+ " ,statement_template_ti_excel_en=#{bean.statementTemplateTiExcelEn} \n"
			+ " ,statement_template_lc_excel_cn=#{bean.statementTemplateLcExcelCn} \n"
			+ " ,statement_template_lc_excel_en=#{bean.statementTemplateLcExcelEn} \n"
			+ " ,statement_template_io_excel_cn=#{bean.statementTemplateIoExcelCn} \n"
			+ " ,statement_template_io_excel_en=#{bean.statementTemplateIoExcelEn} \n"
			+ "	 WHERE	 org_id=#{bean.orgId}\n")
	void updateOrgTemplateConfig(@Param("bean") Org org);
	
	@Select({"<script>",
			" SELECT org_id,org_name FROM hrs_org WHERE org_type=1 ",
			" <when test='org.checkJt == null or org.checkJt==\"\"'>",
			" and (group_id =#{org.orgId} or (org_id = #{org.orgId} and group_id is not null))",
			" </when>",
			" <when test='org.checkJt != null and org.checkJt!=\"\"'>",
			" and group_id =#{org.orgId} order by org_name asc",
			" </when>",
			"</script>"})
	List<Org> getOrgChild(@Param("org") Org org);
	
	@Select({"<script>",
		"select group_id from hrs_org where org_id=#{org_id} ",
	"</script>"})
	Map getGroupIdByOrgId(@Param("org_id") Integer org_id);

	@Update("update prm_coop \n"
			+ " set coop_org_coop_id=#{coopIdS} \n"
			+ "	 WHERE coop_id=#{coopIdP}\n")
	void updateCoopById(@Param("coopIdP") Integer coopIdP,@Param("coopIdS") Integer coopIdS);
}
