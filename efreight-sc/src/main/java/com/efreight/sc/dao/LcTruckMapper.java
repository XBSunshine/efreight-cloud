package com.efreight.sc.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.efreight.sc.entity.LcTruck;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * <p>
 * LC  车辆管理 Mapper 接口
 * </p>
 *
 * @author xiaobo
 * @since 2020-07-31
 */
public interface LcTruckMapper extends BaseMapper<LcTruck> {

    @Update("update lc_truck set truck_number = #{bean.truckNumber}, length=#{bean.length}, " +
            "ton=#{bean.ton}, weight_limit=#{bean.weightLimit}," +
            "volume_limit = #{bean.volumeLimit}, driver_name=#{bean.driverName}," +
            "driver_tel = #{bean.driverTel}, editor_id = #{bean.editorId}," +
            "editor_name = #{bean.editorName}, edit_time = #{bean.editTime} where truck_id = #{bean.truckId}")
    int updateByIdCanSetNull(@Param("bean")LcTruck lcTruck);
}
