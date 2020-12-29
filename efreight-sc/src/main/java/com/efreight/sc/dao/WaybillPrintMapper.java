package com.efreight.sc.dao;

import com.efreight.sc.entity.WaybillPrint;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.efreight.sc.entity.WaybillPrintDetails;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * CS 订单管理 海运制单 Mapper 接口
 * </p>
 *
 * @author xiaobo
 * @since 2020-08-17
 */
public interface WaybillPrintMapper extends BaseMapper<WaybillPrint> {

    @Select("CALL sc_P_waybill_print(#{orgId},null,#{orderIdOrMblNumber},#{flag},'init')")
    WaybillPrint initData(@Param("orderIdOrMblNumber") String orderIdOrMblNumber, @Param("orgId") Integer orgId, @Param("flag") String flag);

    @Select("CALL sc_P_waybill_print(#{orgId},#{waybillPrintId},null,'main','print')")
    WaybillPrint printMain(@Param("waybillPrintId") Integer waybillPrintId, @Param("orgId") Integer orgId);

    @Select("CALL sc_P_waybill_print(#{orgId},#{waybillPrintId},null,'detail','print')")
    List<WaybillPrintDetails> printDetail(@Param("waybillPrintId") Integer waybillPrintId, @Param("orgId") Integer orgId);
}
