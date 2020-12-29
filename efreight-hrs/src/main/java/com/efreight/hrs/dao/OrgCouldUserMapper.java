package com.efreight.hrs.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.efreight.hrs.entity.OrgCouldUser;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface OrgCouldUserMapper extends BaseMapper<OrgCouldUser>{
	
	@Select({"<script>",
		    "select",
			"t.org_id,",
			"t.org_from_remark,",
			"t.is_Intention_user as intended_user,",
	      "t.org_code,t.one_stop_code,t.org_name,u.user_name AS demand_person_name,t.admin_international_country_code,t.org_type,t.org_user_count AS user_count,",
	      "t.create_time, p2.org_name AS org_edition_name,IFNULL(hu.h_user_num,0) AS org_user_count ,",
	      " s.create_time_sc,a.create_time_af,t.org_status,",
	      "t.stop_date,IFNULL(p.pc_coop_num,0) as org_coop_count,(IFNULL(a.af_order_num,0)+IFNULL(s.sc_order_num,0)) AS org_order_count,",
	      "t.admin_id,t.admin_email,t.admin_name,t.admin_tel,t.rc_email,t.org_remark,subscription.subscription_num,subscription.subscription_time,t.group_id,suborg.suborg_count,p3.org_name AS parentOrg ",
	      " from hrs_org t ",  
	      //where pc.coop_name not in ('初始化客户','初始化供应商')
	      "left join (select count(*) AS pc_coop_num,pc.org_id  from prm_coop pc  GROUP BY pc.org_id) p on t.org_id=p.org_id",
	      //where af.order_status not in ('强制关闭')
	      "left join (select count(*) AS af_order_num,af.org_id,MAX(af.create_time) AS create_time_af from af_order af  group by af.org_id ) a on t.org_id = a.org_id ",
	      //where sc.order_status not in ('强制关闭')
	      "left join (select count(*) AS sc_order_num,sc.org_id,MAX(sc.create_time) AS create_time_sc  from sc_order sc  group by sc.org_id ) s on t.org_id = s.org_id ",
	      "left join (select count(*) AS h_user_num,h.org_id  from hrs_user h where h.isadmin=0 group by h.org_id ) hu  on t.org_id=hu.org_id ",
	      "left join hrs_user u on t.demand_person_id=u.user_id ",
	      "left join hrs_org p2 on t.org_edition_id=p2.org_id ",
		  "left join (SELECT count(*) AS subscription_num,org_id,MAX(create_time) AS subscription_time FROM af_awb_subscription GROUP BY org_id ) subscription on t.org_id = subscription.org_id ",
			"left join ( SELECT group_id,count(*) as suborg_count FROM `hrs_org` where group_id != org_id GROUP BY group_id) suborg on t.org_id = suborg.group_id ",
			"left join hrs_org p3 on t.group_id=p3.org_id ",
	      
		  " WHERE t.org_type !=0  ",
		     "<when test='bean.createTimeStart!=null'>",
		     " and t.create_time <![CDATA[>=]]>#{bean.createTimeStart}",
		     "</when>",
		     "<when test='bean.createTimeEnd!=null'>",
		     " and t.create_time <![CDATA[<=]]>#{bean.createTimeEnd}",
		     "</when>",
		     "<when test='bean.isStatus!=null and bean.isStatus!=\"\" and bean.isStatus==\"1\"'>",
		     "  AND t.org_status = 1 AND  curdate()<![CDATA[<=]]>t.stop_date ",
		     "</when>",
		     "<when test='bean.isStatus!=null and bean.isStatus!=\"\" and bean.isStatus==\"0\"'>",
		     " AND (t.org_status = 0 OR  curdate()<![CDATA[>]]>t.stop_date)",
		     "</when>",
		     "<when test='bean.intendedUser!=null and bean.intendedUser!=\"\" and bean.intendedUser==\"1\"'>",
		     "  AND (((IFNULL(a.af_order_num,0)+IFNULL(s.sc_order_num,0))<![CDATA[>]]>0  or IFNULL(p.pc_coop_num,0) <![CDATA[>]]> 2 or t.is_Intention_user = 1 or subscription.subscription_num > 0) and (t.is_Intention_user = 1 or t.is_Intention_user = -1 or t.is_Intention_user is null) and p2.org_name NOT IN ('标准版','专业版') and p2.org_name not like CONCAT('%', '内部', '%')) ",
		     "</when>",
		     "<when test='bean.orgEditionId!=null and bean.orgEditionId!=\"\"'>",
		     " and t.org_edition_id=#{bean.orgEditionId} ",
		     "</when>",
		     "<when test='bean.demandPersonId!=null and bean.demandPersonId!=\"\"'>",
		     " and t.demand_person_id=#{bean.demandPersonId} ",
		     "</when>",
		     "<when test='bean.orgName!=null and bean.orgName!=\"\"'>",
		     " and (t.org_name like  \"%\"#{bean.orgName}\"%\" or t.org_code like \"%\"#{bean.orgName}\"%\")",
		     "</when>",
		     "<when test='bean.orgType!=null and bean.orgType!=\"\"'>",
		     " and t.org_type = #{bean.orgType} ",
		     "</when>",
			"<when test='bean.adminEmail!=null and bean.adminEmail!=\"\"'>",
				" and t.admin_email like CONCAT('%', #{bean.adminEmail}, '%') ",
			"</when>",
			"<when test='bean.adminTel!=null and bean.adminTel!=\"\"'>",
				" and t.admin_tel like CONCAT('%', #{bean.adminTel}, '%') ",
			"</when>",
			"<when test='bean.isSubOrg!=null and bean.isSubOrg!=\"\" and bean.isSubOrg==\"是\"'>",
			"  AND (suborg.suborg_count > 0 or  (t.group_id != 0 or t.group_id != null or t.group_id != '') )",
			"</when>",
		     " order by t.create_time desc",
		  "</script>"})
	IPage<OrgCouldUser> getOrgCouldUserList(Page page,@Param("bean") OrgCouldUser bean);
	@Select({"<script>",
	    "select",

			"t.org_from_remark,",
	    "t.org_code,t.one_stop_code,t.org_name,t.is_Intention_user as intended_user,u.user_name AS demand_person_name,t.admin_international_country_code,t.org_type,t.org_user_count AS user_count,",
	      "t.create_time, p2.org_name AS org_edition_name,IFNULL(hu.h_user_num,0) AS org_user_count ,",
	      " s.create_time_sc,a.create_time_af,t.org_status,",
	      "t.stop_date,IFNULL(p.pc_coop_num,0) as org_coop_count,(IFNULL(a.af_order_num,0)+IFNULL(s.sc_order_num,0)) AS org_order_count,",
	      "t.admin_email,t.admin_name,t.admin_tel,t.rc_email,t.org_remark,subscription.subscription_num,subscription.subscription_time ",
	      " from hrs_org t ",  
	      //where pc.coop_name not in ('初始化客户','初始化供应商')
	      "left join (select count(*) AS pc_coop_num,pc.org_id  from prm_coop pc  GROUP BY pc.org_id) p on t.org_id=p.org_id",
	      //where af.order_status not in ('强制关闭')
	      "left join (select count(*) AS af_order_num,af.org_id,MAX(af.create_time) AS create_time_af from af_order af  group by af.org_id ) a on t.org_id = a.org_id ",
	      //where sc.order_status not in ('强制关闭')
	      "left join (select count(*) AS sc_order_num,sc.org_id,MAX(sc.create_time) AS create_time_sc  from sc_order sc  group by sc.org_id ) s on t.org_id = s.org_id ",
	      "left join (select count(*) AS h_user_num,h.org_id  from hrs_user h where h.isadmin=0 group by h.org_id ) hu  on t.org_id=hu.org_id ",
	      "left join hrs_user u on t.demand_person_id=u.user_id ",
	      "left join hrs_org p2 on t.org_edition_id=p2.org_id ",
		  "left join (SELECT count(*) AS subscription_num,org_id,MAX(create_time) AS subscription_time FROM af_awb_subscription GROUP BY org_id ) subscription on t.org_id = subscription.org_id ",
	      
		  " WHERE t.org_type !=0  ",
		     "<when test='bean.createTimeStart!=null'>",
		     " and t.create_time <![CDATA[>=]]>#{bean.createTimeStart}",
		     "</when>",
		     "<when test='bean.createTimeEnd!=null'>",
		     " and t.create_time <![CDATA[<=]]>#{bean.createTimeEnd}",
		     "</when>",
		     "<when test='bean.isStatus!=null and bean.isStatus!=\"\" and bean.isStatus==\"1\"'>",
		     "  AND t.org_status = 1 AND  curdate()<![CDATA[<=]]>t.stop_date ",
		     "</when>",
		     "<when test='bean.isStatus!=null and bean.isStatus!=\"\" and bean.isStatus==\"0\"'>",
		     " AND (t.org_status = 0 OR  curdate()<![CDATA[>]]>t.stop_date)",
		     "</when>",
		     "<when test='bean.intendedUser!=null and bean.intendedUser!=\"\" and bean.intendedUser==\"1\"'>",
		     "  AND (((IFNULL(a.af_order_num,0)+IFNULL(s.sc_order_num,0))<![CDATA[>]]>0  or IFNULL(p.pc_coop_num,0) <![CDATA[>]]> 2 or t.is_Intention_user = 1 or subscription.subscription_num > 0) and (t.is_Intention_user = 1 or t.is_Intention_user = -1 or t.is_Intention_user is null) and p2.org_name NOT IN ('标准版','专业版') and p2.org_name not like CONCAT('%', '内部', '%')) ",
		     "</when>",
		     "<when test='bean.orgEditionId!=null and bean.orgEditionId!=\"\"'>",
		     " and t.org_edition_id=#{bean.orgEditionId} ",
		     "</when>",
		     "<when test='bean.demandPersonId!=null and bean.demandPersonId!=\"\"'>",
		     " and t.demand_person_id=#{bean.demandPersonId} ",
		     "</when>",
		     "<when test='bean.orgName!=null and bean.orgName!=\"\"'>",
		     " and (t.org_name like  \"%\"#{bean.orgName}\"%\" or t.org_code like \"%\"#{bean.orgName}\"%\")",
		     "</when>",
		     "<when test='bean.orgType!=null and bean.orgType!=\"\"'>",
		     " and t.org_type = #{bean.orgType} ",
		     "</when>",
		     " order by t.create_time desc",
	  "</script>"})
 List<OrgCouldUser> getOrgCouldUserExcel(@Param("bean") OrgCouldUser bean);
	
	

}
