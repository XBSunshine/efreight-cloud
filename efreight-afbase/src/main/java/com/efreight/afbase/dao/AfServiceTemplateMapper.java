package com.efreight.afbase.dao;

import com.efreight.afbase.entity.AfServiceTemplate;
import com.efreight.afbase.entity.CssDebitNote;

import java.time.LocalDateTime;
import java.util.List;

import com.efreight.afbase.entity.exportExcel.AfServiceTemplateExcel;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

/**
 * <p>
 * AF 基础信息 服务类别：收付模板设定 Mapper 接口
 * </p>
 *
 * @author qipm
 * @since 2020-06-01
 */
public interface AfServiceTemplateMapper extends BaseMapper<AfServiceTemplate> {
	@Select({"<script>",
        "select * from af_service_template\n",
        "	where org_id = #{org_id} and business_scope = #{business_scope} and template_type = #{template_type}",
        " ORDER BY template_code DESC ",
        "</script>"})
	List<AfServiceTemplate> selectCode(@Param("org_id") Integer org_id, @Param("business_scope") String business_scope,@Param("template_type") Integer template_type);
	@Select({"<script>",
		"SELECT ",
		"	template_code,",
		"	MAX(business_scope) business_scope, ",
		"	MAX(template_type) template_type,",
		"	MAX(port_code) port_code,",
		"	MAX(template_name) template_name,",
		"	MAX(template_remark) template_remark,",
		"	MAX(editor_name) editor_name,",
		"	MAX(edit_time) edit_time",
		"FROM af_service_template",
		"WHERE 1=1 and org_id=#{bean.orgId} ",
		"<when test='bean.businessScope!=null and bean.businessScope!=\"\"'>",
        " AND business_scope = #{bean.businessScope}",
        "</when>",
        "<when test='bean.templateType!=null'>",
        " AND template_type = #{bean.templateType}",
        "</when>",
        "<when test='bean.templateName!=null and bean.templateName!=\"\"'>",
        " AND template_name like  \"%\"#{bean.templateName}\"%\"",
        "</when>",
        "<when test='bean.portCode!=null and bean.portCode!=\"\"'>",
        " AND port_code like  \"%\"#{bean.portCode}\"%\"",
        "</when>",
        "<when test='bean.createTimeBegin!=null and bean.createTimeBegin!=\"\"'>",
        " AND edit_time  <![CDATA[ >= ]]> #{bean.createTimeBegin}",
        "</when>",
        "<when test='bean.createTimeEnd!=null and bean.createTimeEnd!=\"\"'>",
        " AND edit_time <![CDATA[ <= ]]> #{bean.createTimeEnd}",
        "</when>",
		"GROUP BY org_id,business_scope,template_type,template_code",
		"ORDER BY template_code DESC",
	 "</script>"})
	IPage<AfServiceTemplate> getListPage(Page page, @Param("bean") AfServiceTemplate bean);
	
	@Delete({"<script>",
        "delete from af_service_template\n",
        "	where org_id = #{org_id} and template_id = #{template_id} ",
        "</script>"})
	void doDeleteById(@Param("org_id") Integer org_id, @Param("template_id") Integer template_id);
	@Delete({"<script>",
		"delete from af_service_template\n",
		"	where org_id = #{org_id} and business_scope = #{business_scope} and template_type = #{template_type} and template_code = #{template_code}",
	"</script>"})
	void doDelete(@Param("org_id") Integer org_id, @Param("business_scope") String business_scope,@Param("template_type") Integer template_type,@Param("template_code") String template_code);
	@Insert("insert into hrs_log (op_level,op_type,op_name,op_info,creator_id,create_time,org_id,dept_id) "
            + " VALUES ('高','服务模板删除',#{op_name},#{op_info},#{creator_id},#{create_time},#{org_id},#{dept_id})")
    void insertHrsLog(@Param("op_name") String op_name, @Param("op_info") String op_info,
                      @Param("creator_id") Integer creator_id, @Param("create_time") LocalDateTime create_time,
                      @Param("org_id") Integer org_id, @Param("dept_id") Integer dept_id);
	
	@Select({"<script>",
		"select * from af_service_template\n",
		"	where org_id = #{org_id} and business_scope = #{business_scope} and template_type = #{template_type} and template_code = #{template_code}",
	"</script>"})
	List<AfServiceTemplate> getView(@Param("org_id") Integer org_id, @Param("business_scope") String business_scope,@Param("template_type") Integer template_type,@Param("template_code") String template_code);
	@Select({"<script>",
		" select ",
		"	template_code,",
		"	MAX(business_scope) business_scope, ",
		"	MAX(template_type) template_type,",
		"	MAX(port_code) port_code,",
		"	MAX(template_name) template_name ",
		" from af_service_template",
		"	where org_id = #{org_id} and business_scope = #{business_scope} and template_type = #{template_type} and (port_code = #{port_code} or port_code ='' or port_code IS NULL)",
		" GROUP BY org_id,business_scope,template_type,template_code",
		" ORDER BY port_code DESC,template_name",
	"</script>"})
	List<AfServiceTemplate> getServicetemplate(@Param("org_id") Integer org_id, @Param("business_scope") String business_scope,@Param("template_type") Integer template_type,@Param("port_code") String port_code);

	@Select({"<script>",
			"SELECT\n" +
					"\tMAX( business_scope ) business_scope,\n" +
					"\tCASE MAX( template_type ) \n" +
					"\t\t\t WHEN 1 THEN '应收' \n" +
					"\t\t\t ELSE '应付' END AS template_type,\n" +
					"\tMAX( port_code ) port_code,\n" +
					"\tMAX( template_name ) template_name,\n" +
					"\tMAX( template_remark ) template_remark,\n" +
					"\tSUBSTRING_INDEX(MAX( editor_name ), ' ',  1) editor_name,\n" +
					"\tMAX( edit_time ) edit_time ",
			"FROM af_service_template",
			"WHERE 1=1 and org_id=#{bean.orgId} ",
			"<when test='bean.businessScope!=null and bean.businessScope!=\"\"'>",
			" AND business_scope = #{bean.businessScope}",
			"</when>",
			"<when test='bean.templateType!=null'>",
			" AND template_type = #{bean.templateType}",
			"</when>",
			"<when test='bean.templateName!=null and bean.templateName!=\"\"'>",
			" AND template_name like  \"%\"#{bean.templateName}\"%\"",
			"</when>",
			"<when test='bean.portCode!=null and bean.portCode!=\"\"'>",
			" AND port_code like  \"%\"#{bean.portCode}\"%\"",
			"</when>",
			"<when test='bean.createTimeBegin!=null and bean.createTimeBegin!=\"\"'>",
			" AND edit_time  <![CDATA[ >= ]]> #{bean.createTimeBegin}",
			"</when>",
			"<when test='bean.createTimeEnd!=null and bean.createTimeEnd!=\"\"'>",
			" AND edit_time <![CDATA[ <= ]]> #{bean.createTimeEnd}",
			"</when>",
			"GROUP BY org_id,business_scope,template_type,template_code",
			"ORDER BY template_code DESC",
			"</script>"})
	List<AfServiceTemplateExcel> queryListForExcel(@Param("bean") AfServiceTemplate bean);
}
