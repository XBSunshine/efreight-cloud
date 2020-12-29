package com.efreight.afbase.dao;

import com.efreight.afbase.entity.CssReportNoWriteoff;
import com.efreight.afbase.entity.CssReportNoWriteoffDetail;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface CssReportNoWriteoffMapper {

    @Select("CALL css_P_report_customer_balance(#{cssReportNoWriteoff.coopCode},#{cssReportNoWriteoff.coopName},#{cssReportNoWriteoff.orgId},#{cssReportNoWriteoff.otherOrg})")
    List<CssReportNoWriteoff> getList(@Param("cssReportNoWriteoff") CssReportNoWriteoff cssReportNoWriteoff);

    @Select("CALL css_P_report_customer_balance_detail(#{coopId},#{orgId},#{type},#{otherOrg})")
    List<CssReportNoWriteoffDetail> view(@Param("coopId") Integer coopId, @Param("orgId") Integer orgId,@Param("type") Integer type,@Param("otherOrg") Integer otherOrg);
}
