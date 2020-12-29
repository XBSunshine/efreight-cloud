package com.efreight.afbase.dao;

import com.efreight.afbase.entity.AfOrderStorageMns;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author qipm
 * @since 2020-10-26
 */
public interface AfOrderStorageMnsMapper extends BaseMapper<AfOrderStorageMns> {

	@Update("update af_order set "
            + " storage_pieces=#{storage_pieces},"
            + " storage_weight=#{storage_weight},"
            + " storage_time=#{storage_time} "
            + " where  awb_number = #{awb_number}\n")
    void updateOrder(@Param("storage_pieces") Integer storage_pieces, @Param("storage_weight") BigDecimal storage_weight,
    		@Param("storage_time") LocalDateTime storage_time, @Param("awb_number") String awb_number);
	@Select({"<script>",
	"select * from af_order_storage_mns",
	" WHERE 1=1",
	" and mawbcode=#{bean.mawbcode}",
	"</script>"})
	List<AfOrderStorageMns> queryList(@Param("bean") AfOrderStorageMns bean);
}
