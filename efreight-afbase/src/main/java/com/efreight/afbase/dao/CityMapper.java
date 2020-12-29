package com.efreight.afbase.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.efreight.afbase.entity.City;
import com.efreight.afbase.entity.view.AirportCitySearch;
import org.apache.ibatis.annotations.Param;
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
public interface CityMapper extends BaseMapper<City> {

    @Select({"<script>",
            "SELECT\n" +
                    "\tci.*,\n" +
                    "\tna.nation_name_cn AS nationNameCn,\n" +
                    "\tna.nation_name_en AS nationNameEn \n" +
                    "FROM\n" +
                    "\taf_city ci\n" +
                    "\tLEFT JOIN af_nation na ON ci.nation_code = na.nation_code_two",
            "WHERE 1=1",
            "<when test='bean.cityCode!=null and bean.cityCode!=\"\"'>",
            " AND ci.city_code like  \"%\"#{bean.cityCode}\"%\"",
            "</when>",
            "<when test='bean.nationCode!=null and bean.nationCode!=\"\"'>",
            " AND ci.nation_code like  \"%\"#{bean.nationCode}\"%\"",
            "</when>",
            "<when test='bean.cityNameCn!=null and bean.cityNameCn!=\"\"'>",
            " AND (ci.city_name_cn like  \"%\"#{bean.cityNameCn}\"%\" or ci.city_name_en like  \"%\"#{bean.cityNameCn}\"%\")",
            "</when>",
            "<when test='bean.nationNameCn!=null and bean.nationNameCn!=\"\"'>",
            " AND (na.nation_name_cn like  \"%\"#{bean.nationNameCn}\"%\" or na.nation_name_en like  \"%\"#{bean.nationNameCn}\"%\")",
            "</when>",
            " ORDER BY na.nation_code_two,ci.city_code",
            "</script>"})
    IPage<City> getListPage(Page page, @Param("bean") City bean);



    /**
     * 搜索城市信息
     * @param key 城市关键字
     * @return
     */
    @Select("SELECT DISTINCT city_code, city_name_en, city_name_cn FROM (\n" +
            "\tSELECT * FROM (\n" +
            "\t\tSELECT city_code,\n" +
            "\t\tMAX(city_name_en) AS city_name_en,\n" +
            "\t\tMAX(city_name_cn) AS city_name_cn\n" +
            "\t\tFROM af_city\n" +
            "\t\tWHERE nation_code='CN' AND city_code LIKE concat(#{searchKey},'%')\n" +
            "\t\tGROUP BY city_code\n" +
            "\t\tORDER BY city_code ASC\n" +
            "\t\tLIMIT 10\n" +
            "\t) AS A\n" +
            "\tUNION ALL\n" +
            "\tSELECT * FROM (\n" +
            "\t\tSELECT city_code,\n" +
            "\t\tMAX(city_name_en) AS city_name_en,\n" +
            "\t\tMAX(city_name_cn) AS city_name_cn\n" +
            "\t\tFROM af_city\n" +
            "\t\tWHERE nation_code='CN' AND city_name_en LIKE concat('%', #{searchKey}, '%')\n" +
            "\t\tGROUP BY city_code\n" +
            "\t\tORDER BY city_code ASC\n" +
            "\t\tLIMIT 10\n" +
            "\t) AS B\n" +
            "\tUNION ALL\n" +
            "\tSELECT * FROM (\n" +
            "\t\tSELECT city_code,\n" +
            "\t\tMAX(city_name_en) AS city_name_en,\n" +
            "\t\tMAX(city_name_cn) AS city_name_cn\n" +
            "\t\tFROM af_city\n" +
            "\t\tWHERE nation_code='CN' AND city_name_cn LIKE concat('%', #{searchKey}, '%')\n" +
            "\t\tGROUP BY city_code\n" +
            "\t\tORDER BY city_code ASC\n" +
            "\t\tLIMIT 10\n" +
            "\t) AS C\n" +
            ") AS T")
    List<AirportCitySearch> searchCities(String key);
}
