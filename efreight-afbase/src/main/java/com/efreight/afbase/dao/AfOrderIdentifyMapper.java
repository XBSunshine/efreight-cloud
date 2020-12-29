package com.efreight.afbase.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.efreight.afbase.entity.AfOrderIdentify;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author mayt
 * @since 2020-10-12
 */
public interface AfOrderIdentifyMapper extends BaseMapper<AfOrderIdentify> {
    @Update({"<script>",
            "update af_order_identifies\n",
            " set status = 'send',declare_id=#{declare_id},declare_name=#{declare_name},declare_date=now() ",
            " where order_identify_id = #{orderIdentifyId} ",
            "</script>"})
    boolean declare(@Param("order_identify_id") Integer orderIdentifyId, @Param("order_identify_id") Integer declareId, @Param("declare_name") String declareName);
}

