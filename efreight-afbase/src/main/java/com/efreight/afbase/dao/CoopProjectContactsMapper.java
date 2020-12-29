package com.efreight.afbase.dao;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import com.efreight.afbase.entity.CoopProjectContacts;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;


public interface CoopProjectContactsMapper extends BaseMapper<CoopProjectContacts> {

	@Select({"<script>",
		"SELECT * FROM af_coop_project_contacts ",
		"where project_id=#{project_id}  and org_id=#{org_id}",
		"</script>"})
List<Map<String, Object>> selectAll(@Param("project_id") Integer project_id,@Param("org_id") Integer org_id);
}
