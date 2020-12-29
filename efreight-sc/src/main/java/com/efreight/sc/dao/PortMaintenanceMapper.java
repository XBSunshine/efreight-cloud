package com.efreight.sc.dao;

import com.efreight.sc.entity.PortMaintenance;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * <p>
 * CS 海运港口表 Mapper 接口
 * </p>
 *
 * @author xiaobo
 * @since 2020-03-03
 */
public interface PortMaintenanceMapper extends BaseMapper<PortMaintenance> {

    /**
     * 使用关键字进行搜索
     * @param key
     * @return
     */
    @Select("SELECT DISTINCT port_code,port_name_en,port_name_cn\n" +
            " FROM (\n" +
            " SELECT *\n" +
            " FROM (\n" +
            " SELECT port_code,port_name_en,CONCAT(port_name_cn,'(',country_name_cn,')') AS port_name_cn\n" +
            " FROM sc_port_maintenance\n" +
            " WHERE is_valid = 1 and port_name_en LIKE concat('%', #{key},'%') ORDER BY port_name_en\n" +
            " LIMIT 10 ) AS A\n" +
            " UNION ALL\n" +
            " SELECT *\n" +
            " FROM (\n" +
            " SELECT port_code,port_name_en,CONCAT(port_name_cn,'(',country_name_cn,')') AS port_name_cn\n" +
            " FROM sc_port_maintenance\n" +
            " WHERE is_valid = 1 and port_code LIKE concat('%', #{key},'%') ORDER BY port_code\n" +
            " LIMIT 10 ) AS B\n" +
            " UNION ALL\n" +
            " SELECT *\n" +
            " FROM (\n" +
            " SELECT port_code,port_name_en,CONCAT(port_name_cn,'(',country_name_cn,')') AS port_name_cn\n" +
            " FROM sc_port_maintenance\n" +
            " WHERE is_valid = 1 and port_name_cn LIKE concat('%', #{key},'%') ORDER BY port_name_cn\n" +
            " LIMIT 10 ) AS C ) AS T")
    List<PortMaintenance> search(String key);

}
