package com.efreight.afbase.dao;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import com.efreight.afbase.entity.CoopProject;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;


public interface CoopProjectMapper extends BaseMapper<CoopProject> {

@Select({"<script>",
		"SELECT * FROM af_V_currency_rate",
		"where org_id=#{org_id} ORDER BY currency_code asc",
		"</script>"})
List<Map<String, Object>> selectCurrency(@Param("org_id") Integer org_id);
}
