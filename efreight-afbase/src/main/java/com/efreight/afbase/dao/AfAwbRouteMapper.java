package com.efreight.afbase.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.efreight.afbase.entity.AfAwbRoute;

public interface AfAwbRouteMapper extends BaseMapper<AfAwbRoute>{
	@Select({"<script>",
		"select * from af_awb_route",
		"where is_track=0 and awb_number in",
		"<foreach collection='arrayAwbNumber' item='awbNumber' open='(' separator=',' close=')'>",
		"#{awbNumber}",
		"</foreach>",
        "</script>"
			})
	List<AfAwbRoute> queryAfAwbRouteList(@Param("arrayAwbNumber") String[] arrayAwbNumber);
	
	@Select("select * from af_awb_route \n"
			+ " where is_track=0\n")
	List<AfAwbRoute> queryAfAwbRouteListAll();
}
