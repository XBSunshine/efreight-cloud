package com.efreight.afbase.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.efreight.afbase.entity.Airport;
import com.efreight.afbase.entity.view.AirportCitySearch;
import com.efreight.afbase.entity.view.AirportCountrySearch;
import com.efreight.afbase.entity.view.AirportSearch;
import org.apache.ibatis.annotations.Delete;
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
public interface AirportMapper extends BaseMapper<Airport> {
	
	@Delete("TRUNCATE TABLE af_airport")
	void truncateTable();

	@Select({"<script>",
        "    SELECT * from af_airport",
        "    where city_code = #{cityCode}",
        "</script>"})
	List<Airport> getAirport(@Param("cityCode") String cityCode);
	@Select({"<script>",
		"    SELECT * from af_airport",
		"    where ap_code = #{cityCode} AND nation_code='CN'",
	"</script>"})
	List<Airport> checkFlag(@Param("cityCode") String cityCode);

	/**
	 * 使用关键字进行搜索
	 * @param key
	 * @return
	 */
	@Select("SELECT DISTINCT ap_code,ap_name_en,ap_name_cn FROM\n" +
			"(\n" +
			" SELECT * FROM \n" +
			" (\n" +
			"  SELECT ap_code,ap_name_en,CONCAT(ap_name_cn,'(',nation_name_cn,')') AS ap_name_cn \n" +
			"  FROM af_airport\n" +
			"  WHERE ap_status = 1 and ap_code LIKE concat(#{key}, '%')\n" +
			"  ORDER BY ap_code\n" +
			"  LIMIT 10\n" +
			" ) AS A\n" +
			" UNION ALL\n" +
			" SELECT * FROM \n" +
			" (\n" +
			"  SELECT ap_code,ap_name_en,CONCAT(ap_name_cn,'(',nation_name_cn,')') AS ap_name_cn \n" +
			"  FROM af_airport\n" +
			"  WHERE ap_status = 1 and ap_name_en LIKE concat('%', #{key},'%')\n" +
			"  ORDER BY ap_name_en\n" +
			"  LIMIT 10\n" +
			" ) AS B\n" +
			" UNION ALL\n" +
			" SELECT * FROM \n" +
			" (\n" +
			"  SELECT ap_code,ap_name_en,CONCAT(ap_name_cn,'(',nation_name_cn,')') AS ap_name_cn \n" +
			"  FROM af_airport\n" +
			"  WHERE  ap_status = 1 and ap_name_cn LIKE concat('%', #{key},'%')\n" +
			"  ORDER BY ap_name_cn\n" +
			"  LIMIT 10\n" +
			" ) AS C\n" +
			") AS T")
    List<AirportSearch> search(String key);

	/**
	 * 搜索国家数据
	 * @param searchKey 关键字
	 * @return
	 */
	@Select("SELECT DISTINCT nation_code,nation_name_en,nation_name_cn FROM\n" +
			"(\n" +
			" SELECT * FROM \n" +
			" (\n" +
			"  SELECT nation_code,MAX(nation_name_en) AS nation_name_en,MAX(nation_name_cn) AS nation_name_cn\n" +
			"  FROM af_airport\n" +
			"  WHERE ap_status=1 AND nation_code LIKE concat(#{searchKey},'%') \n" +
			" GROUP BY nation_code\n" +
			"  ORDER BY nation_code\n" +
			"  LIMIT 10\n" +
			" ) AS A\n" +
			" UNION ALL\n" +
			" SELECT * FROM \n" +
			" (\n" +
			"  SELECT nation_code,MAX(nation_name_en) AS nation_name_en,MAX(nation_name_cn) AS nation_name_cn\n" +
			"  FROM af_airport\n" +
			"  WHERE ap_status=1 AND nation_name_en LIKE concat('%', #{searchKey}, '%')\n" +
			" GROUP BY nation_code\n" +
			"  ORDER BY nation_name_en\n" +
			"  LIMIT 10\n" +
			" ) AS B\n" +
			" UNION ALL\n" +
			" SELECT * FROM \n" +
			" (\n" +
			"  SELECT nation_code,MAX(nation_name_en) AS nation_name_en,MAX(nation_name_cn) AS nation_name_cn\n" +
			"  FROM af_airport\n" +
			"  WHERE ap_status=1 AND nation_name_cn LIKE concat('%', #{searchKey}, '%')\n" +
			" GROUP BY nation_code\n" +
			"  ORDER BY nation_name_cn\n" +
			"  LIMIT 10\n" +
			" ) AS C\n" +
			") AS T")
	List<AirportCountrySearch> searchCountry(String searchKey);

	/**
	 * 搜索城市信息
	 * @param searchKey 城市代码
	 * @return
	 */
	@Select("SELECT DISTINCT city_code, city_name_en, city_name_cn, CONCAT(city_name_cn,'(',nation_name_cn,')') as nation_name_cn FROM (\n" +
			"\tSELECT * FROM (\n" +
			"\t\tSELECT city_code,\n" +
			"\t\tMAX(city_name_en) AS city_name_en,\n" +
			"\t\tMAX(city_name_cn) AS city_name_cn,\n" +
			"\t\tMAX(nation_name_cn) AS nation_name_cn \n" +
			"\t\tFROM af_airport\n" +
			"\t\tWHERE ap_status=1 AND city_code LIKE concat(#{searchKey},'%')\n" +
			"\t\tGROUP BY city_code\n" +
			"\t\tORDER BY city_code ASC\n" +
			"\t\tLIMIT 10\n" +
			"\t) AS A\n" +
			"\tUNION ALL\n" +
			"\tSELECT * FROM (\n" +
			"\t\tSELECT city_code,\n" +
			"\t\tMAX(city_name_en) AS city_name_en,\n" +
			"\t\tMAX(city_name_cn) AS city_name_cn,\n" +
			"\t\tMAX(nation_name_cn) AS nation_name_cn \n" +
			"\t\tFROM af_airport\n" +
			"\t\tWHERE ap_status=1 AND city_name_en LIKE concat('%', #{searchKey}, '%')\n" +
			"\t\tGROUP BY city_code\n" +
			"\t\tORDER BY city_code ASC\n" +
			"\t\tLIMIT 10\n" +
			"\t) AS B\n" +
			"\tUNION ALL\n" +
			"\tSELECT * FROM (\n" +
			"\t\tSELECT city_code,\n" +
			"\t\tMAX(city_name_en) AS city_name_en,\n" +
			"\t\tMAX(city_name_cn) AS city_name_cn,\n" +
			"\t\tMAX(nation_name_cn) AS nation_name_cn \n" +
			"\t\tFROM af_airport\n" +
			"\t\tWHERE ap_status=1 AND city_name_cn LIKE concat('%', #{searchKey}, '%')\n" +
			"\t\tGROUP BY city_code\n" +
			"\t\tORDER BY city_code ASC\n" +
			"\t\tLIMIT 10\n" +
			"\t) AS C\n" +
			") AS T")
	List<AirportCitySearch> searchCities(String searchKey);

	@Select("select nation_code_three,max(nation_name_cn) nation_name_cn from af_airport where ap_status=1 and nation_code_three is not null group by nation_code_three order by nation_code_three")
	List<Airport> queryNationWithNationCodeThreeIsNotNull();
}
