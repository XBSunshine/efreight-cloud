package com.efreight.afbase.dao;

import com.efreight.afbase.entity.view.VAfCategory;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * <p>
 * VIEW Mapper 接口
 * </p>
 *
 * @author xiaobo
 * @since 2020-02-28
 */
public interface VAfCategoryMapper extends BaseMapper<VAfCategory> {

	@Select({"<script>",
		"SELECT * FROM af_V_sc_category " ,
		"WHERE category_name =#{category_name}",
	"</script>"})
	List<VAfCategory> getscList(@Param("category_name") String category_name);

}
