package com.efreight.afbase.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.efreight.afbase.entity.Carrier;
import com.efreight.afbase.entity.view.CarrierSearch;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author wangxx
 * @since 2019-07-17
 */
public interface CarrierMapper extends BaseMapper<Carrier> {

    /**
     * 搜索租屋
     * @param searchKey 关键字
     * @return
     */
    @Select("SELECT DISTINCT carrier_code,carrier_prefix,carrier_name_cn FROM\n" +
            "(\n" +
            " SELECT * FROM \n" +
            " (\n" +
            "  SELECT \n" +
            "   carrier_code,carrier_prefix,carrier_name_cn\n" +
            "  FROM af_carrier\n" +
            "  WHERE carrier_code LIKE concat(#{searchKey}, '%')\n" +
            "  ORDER BY carrier_code\n" +
            "  LIMIT 20\n" +
            " ) AS A\n" +
            " UNION ALL\n" +
            " SELECT * FROM \n" +
            " (\n" +
            "  SELECT \n" +
            "   carrier_code,carrier_prefix,carrier_name_cn\n" +
            "  FROM af_carrier\n" +
            "  WHERE carrier_prefix LIKE concat(#{searchKey},'%')\n" +
            "  ORDER BY carrier_prefix\n" +
            "  LIMIT 20\n" +
            " ) AS B\n" +
            " UNION ALL\n" +
            " SELECT * FROM \n" +
            " (\n" +
            "  SELECT \n" +
            "   carrier_code,carrier_prefix,carrier_name_cn\n" +
            "  FROM af_carrier\n" +
            "  WHERE carrier_name_cn LIKE concat('%', #{searchKey}, '%')\n" +
            "  ORDER BY carrier_name_cn\n" +
            "  LIMIT 20\n" +
            " ) AS C\n" +
            ") AS T")
    List<CarrierSearch> search(String searchKey);
}
