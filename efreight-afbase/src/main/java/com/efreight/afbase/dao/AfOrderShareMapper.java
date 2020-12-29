package com.efreight.afbase.dao;

import java.util.HashMap;
import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.efreight.afbase.entity.AfOrderShare;

public interface AfOrderShareMapper extends BaseMapper<AfOrderShare>{
//	and p.coop_org_id is not null 
	@Select({"<script>",
        "SELECT t.org_name AS orgName,p.coop_code AS coopCode,p.is_share AS isShare,p.coop_org_id AS coopOrgId,p.coop_name AS coopName,p.coop_id AS coopId,p.coop_org_id AS shareOrgId,p.coop_org_coop_id shareCoopId  FROM prm_coop p",
        " left join hrs_org t on p.coop_org_id=t.org_id ",
        " where p.org_id=#{bean.orgId} and p.is_share=1 ",
        "<when test='bean.coopName!=null and bean.coopName!=\"\"'>",
        " AND (p.coop_name like  \"%\"#{bean.coopName}\"%\" or p.coop_code like  \"%\"#{bean.coopName}\"%\")",
        "</when>",
        "</script>"})
	 IPage<HashMap> getCoopList(Page page, @Param("bean") AfOrderShare bean);
	
	 @Select({"<script>",
        "SELECT fields_name  FROM prm_coop_share_fields ",
        " where org_id=#{orgId} and is_share=1 and coop_id=#{coopId}",
        "</script>"})
	 List<String> queryPrmCoopShareFields(@Param("orgId") Integer orgId,@Param("coopId") Integer coopId);
	 @Select({"<script>",
	        "SELECT fields_name  FROM prm_coop_share_fields ",
	        " where org_id=#{orgId} and is_subscribe=1 and coop_id=#{coopId}",
	        "</script>"})
	List<String> queryPrmCoopShareFieldsTwo(@Param("orgId") Integer orgId,@Param("coopId") Integer coopId);
	 

}
