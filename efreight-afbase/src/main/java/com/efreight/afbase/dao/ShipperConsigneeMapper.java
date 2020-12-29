package com.efreight.afbase.dao;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.efreight.afbase.entity.Letters;
import com.efreight.afbase.entity.ShipperConsignee;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.efreight.afbase.entity.exportExcel.ShipperConsigneeExcel;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * <p>
 * AF 基础信息 收发货人 Mapper 接口
 * </p>
 *
 * @author xiaobo
 * @since 2019-10-09
 */
public interface ShipperConsigneeMapper extends BaseMapper<ShipperConsignee> {

    @Select("<script>" +
            "SELECT " +
            "SC.*, " +
            "(SELECT  upper(MAX(A.nation_name_cn)) AS nation_name_cn FROM af_airport A GROUP BY A.nation_code HAVING A.nation_code = SC.nation_code) as nation_name_cn, " +
            "(SELECT  upper(MAX(A.nation_name_en)) AS nation_name_en FROM af_airport A GROUP BY A.nation_code HAVING A.nation_code = SC.nation_code) as nation_name_en, " +
            "(SELECT  upper(MAX(A.city_name_cn)) AS city_name_cn  FROM af_airport A GROUP BY A.city_code HAVING A.city_code = SC.city_code and SC.city_code != '') as city_name_cn, " +
            "(SELECT  upper(MAX(A.city_name_en)) AS city_name_en  FROM af_airport A GROUP BY A.city_code HAVING A.city_code = SC.city_code and SC.city_code != '') as city_name_en " +
            " FROM  af_shipper_consignee as SC WHERE SC.org_id=#{condition.orgId}" +
            "<if test='condition.scType != null'>" +
                "AND SC.sc_type = #{condition.scType}" +
            "</if>" +
            "<if test='condition.scName !=null and condition.scName !=\"\" '>" +
                "AND SC.sc_name like  \"%\"#{condition.scName}\"%\" " +
            "</if>" +
            "<if test='condition.scMnemonic !=null and condition.scMnemonic != \"\" '>" +
                "AND SC.sc_mnemonic like \"%\"#{condition.scMnemonic}\"%\" " +
            "</if>" +
            "<if test='condition.scCode !=null and condition.scCode != \"\" '>" +
                "AND SC.sc_code like \"%\"#{condition.scCode}\"%\" " +
            "</if>" +
            "<if test='condition.nationCode != null and condition.nationCode !=\"\" '>" +
                "AND upper(SC.nation_code) = #{condition.nationCode}" +
            "</if>" +
            "<if test='condition.cityName != null and condition.cityName !=\"\" '>" +
                "AND (( SELECT MAX( upper(A.city_name_cn) ) AS city_name_cn FROM af_airport A GROUP BY A.city_code HAVING A.city_code = SC.city_code ) like \"%\"#{condition.cityName}\"%\"  or ( SELECT MAX( upper(A.city_name_en) ) AS city_name_en FROM af_airport A GROUP BY A.city_code HAVING A.city_code = SC.city_code ) like \"%\"#{condition.cityName}\"%\" or upper(SC.city_code) = #{condition.cityName} ) " +
            "</if>" +
            "<if test='condition.isValid != null'>" +
                "AND SC.is_valid = #{condition.isValid}" +
            "</if>" +
            "ORDER BY SC.create_time DESC,SC.sc_mnemonic ASC, SC.sc_name ASC"+
            "</script>")
    IPage<ShipperConsignee> selectToPage(IPage page, @Param("condition") ShipperConsignee shipperConsignee);

    @Select({"<script>",
            " select count(ap_id) AS airCount from af_airport where city_code=#{cityCode} and nation_code=#{nationCode} ",
            "</script>"})
    Integer getCountByCityAndNation(@Param("cityCode") String cityCode, @Param("nationCode") String nationCode);

    @Select("<script>" +
            "SELECT\n" +
            "\tCASE SC.sc_type \n" +
            "\t\t\t WHEN 0 THEN '发货人'\n" +
            "\t\t\t WHEN 1 THEN '收货人' \n" +
            "\t\t\t ELSE '' END AS sc_type,\n" +
            "\tSC.sc_code,\t\t\n" +
            "  SC.sc_name,\t\t\n" +
            "\tCASE SC.is_valid \n" +
            "\t\t\t WHEN 0 THEN ''\n" +
            "\t\t\t WHEN 1 THEN '√' \n" +
            "\t\t\t ELSE '' END AS is_valid, " +
            "(SELECT  upper(MAX(A.nation_name_en)) AS nation_name_en FROM af_airport A GROUP BY A.nation_code HAVING A.nation_code = SC.nation_code) as nation_name_en, " +
            "(SELECT  upper(MAX(A.city_name_en)) AS city_name_en  FROM af_airport A GROUP BY A.city_code HAVING A.city_code = SC.city_code and SC.city_code != '') as city_name_en " +
            " FROM  af_shipper_consignee as SC WHERE SC.org_id=#{condition.orgId}" +
            "<if test='condition.scType != null'>" +
            "AND SC.sc_type = #{condition.scType}" +
            "</if>" +
            "<if test='condition.scName !=null and condition.scName !=\"\" '>" +
            "AND SC.sc_name like  \"%\"#{condition.scName}\"%\" " +
            "</if>" +
            "<if test='condition.scMnemonic !=null and condition.scMnemonic != \"\" '>" +
            "AND SC.sc_mnemonic like \"%\"#{condition.scMnemonic}\"%\" " +
            "</if>" +
            "<if test='condition.scCode !=null and condition.scCode != \"\" '>" +
            "AND SC.sc_code like \"%\"#{condition.scCode}\"%\" " +
            "</if>" +
            "<if test='condition.nationCode != null and condition.nationCode !=\"\" '>" +
            "AND upper(SC.nation_code) = #{condition.nationCode}" +
            "</if>" +
            "<if test='condition.cityName != null and condition.cityName !=\"\" '>" +
            "AND (( SELECT MAX( upper(A.city_name_cn) ) AS city_name_cn FROM af_airport A GROUP BY A.city_code HAVING A.city_code = SC.city_code ) like \"%\"#{condition.cityName}\"%\"  or ( SELECT MAX( upper(A.city_name_en) ) AS city_name_en FROM af_airport A GROUP BY A.city_code HAVING A.city_code = SC.city_code ) like \"%\"#{condition.cityName}\"%\" or upper(SC.city_code) = #{condition.cityName} ) " +
            "</if>" +
            "<if test='condition.isValid != null'>" +
            "AND SC.is_valid = #{condition.isValid}" +
            "</if>" +
            "ORDER BY SC.create_time DESC,SC.sc_mnemonic ASC, SC.sc_name ASC"+
            "</script>")
    List<ShipperConsigneeExcel> queryListForExcel(@Param("condition") ShipperConsignee shipperConsignee);
}
