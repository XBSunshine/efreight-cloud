package com.efreight.afbase.dao;

import com.efreight.afbase.entity.CssCostInvoiceDetailWriteoff;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.efreight.afbase.entity.CssPaymentDetail;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * <p>
 * CSS 应收：发票明细 核销表 Mapper 接口
 * </p>
 *
 * @author xiaobo
 * @since 2021-01-04
 */
public interface CssCostInvoiceDetailWriteoffMapper extends BaseMapper<CssCostInvoiceDetailWriteoff> {


    @Select("<script>" +
            "SELECT A.payment_id_detail_id,A.org_id,A.payment_id,A.order_id,A.cost_id,A.currency,A.amount_payment,null AS amount_payment_writeoff" +
            "<when test='businessScope==\"AE\" or businessScope==\"SE\" or businessScope==\"TE\"'>" +
            ",B.expect_departure AS flightDate" +
            "</when>" +
            "<when test='businessScope==\"AI\" or businessScope==\"SI\"'>" +
            ",B.expect_arrival AS flightDate" +
            "</when>" +
            "<when test='businessScope==\"LC\"'>" +
            ",B.driving_time AS flightDate" +
            "</when>" +
            "<when test='businessScope==\"IO\"'>" +
            ",B.business_date AS flightDate" +
            "</when>" +
            " FROM css_payment_detail A" +
            "<when test='businessScope!=null and (businessScope==\"AE\" or businessScope==\"AI\")'>" +
            " LEFT JOIN af_order B ON A.order_id=B.order_id" +
            "</when>" +
            "<when test='businessScope!=null and (businessScope==\"SE\" or businessScope==\"SI\")'>" +
            " LEFT JOIN sc_order B ON A.order_id=B.order_id" +
            "</when>" +
            "<when test='businessScope!=null and (businessScope==\"TE\" or businessScope==\"TI\")'>" +
            " LEFT JOIN tc_order B ON A.order_id=B.order_id" +
            "</when>" +
            "<when test='businessScope!=null and businessScope==\"LC\"'>" +
            " LEFT JOIN lc_order B ON A.order_id=B.order_id" +
            "</when>" +
            "<when test='businessScope!=null and businessScope==\"IO\"'>" +
            " LEFT JOIN io_order B ON A.order_id=B.order_id" +
            "</when>" +
            " WHERE A.org_id=#{orgId} and A.payment_id=#{paymentId}" +
            "<when test='businessScope==\"AE\" or businessScope==\"SE\" or businessScope==\"TE\"'>" +
            " ORDER BY B.expect_departure,A.amount_payment" +
            "</when>" +
            "<when test='businessScope==\"AI\" or businessScope==\"SI\"'>" +
            " ORDER BY B.expect_arrival,A.amount_payment" +
            "</when>" +
            "<when test='businessScope==\"LC\"'>" +
            " ORDER BY B.driving_time,A.amount_payment" +
            "</when>" +
            "<when test='businessScope==\"IO\"'>" +
            " ORDER BY B.business_date,A.amount_payment" +
            "</when>" +
            "</script>")
    List<CssPaymentDetail> getPaymentDetailListOrderByFlightDateAndAmountDESC(@Param("businessScope") String businessScope, @Param("paymentId") Integer paymentId, @Param("orgId") Integer orgId);
}
