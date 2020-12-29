package com.efreight.afbase.dao;

import com.efreight.afbase.entity.OrderFiles;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Update;

/**
 * <p>
 * AF 订单管理 出口订单附件 Mapper 接口
 * </p>
 *
 * @author xiaobo
 * @since 2020-02-12
 */
public interface OrderFilesMapper extends BaseMapper<OrderFiles> {
	 @Select({"<script>",
         "SELECT * FROM sc_order_files a\n",
         "	where org_id = #{org_id} and order_id = #{order_id}",
         "</script>"})
	List<OrderFiles> getSCList(@Param("org_id") Integer org_id,@Param("order_id") Integer order_id);
	 @Select({"<script>",
         "SELECT * FROM tc_order_files a\n",
         "	where org_id = #{org_id} and order_id = #{order_id}",
         "</script>"})
	List<OrderFiles> getTCList(@Param("org_id") Integer org_id,@Param("order_id") Integer order_id);

	@Update({"<script>",
			"update af_order_files\n",
			"	set is_display = #{isDisplay} where order_file_id = #{orderFilesId}",
			"</script>"})
	void upDateShowFile(@Param("orderFilesId") Integer orderFilesId,@Param("isDisplay") Integer isDisplay);

	@Update({"<script>",
			"update sc_order_files\n",
			"	set is_display = #{isDisplay} where order_file_id = #{orderFilesId}",
			"</script>"})
	void upDateShowFileSc(@Param("orderFilesId") Integer orderFilesId,@Param("isDisplay") Integer isDisplay);
	@Update({"<script>",
		"update tc_order_files\n",
		"	set is_display = #{isDisplay} where order_file_id = #{orderFilesId}",
		"</script>"})
    void upDateShowFileTC(@Param("orderFilesId") Integer orderFilesId,@Param("isDisplay") Integer isDisplay);
}
