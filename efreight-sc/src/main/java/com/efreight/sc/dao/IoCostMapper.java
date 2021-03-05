package com.efreight.sc.dao;

import com.efreight.sc.entity.IoCost;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * IO 费用录入 成本 Mapper 接口
 * </p>
 *
 * @author xiaobo
 * @since 2020-09-17
 */
public interface IoCostMapper extends BaseMapper<IoCost> {

    @Select("select * from css_payment_detail a inner join css_payment b on a.payment_id=b.payment_id and b.business_scope='IO' where a.org_id=#{orgId} and a.cost_id=#{costId}")
    List<Map<String, Object>> getPaymentDetailByCostId(@Param("costId") Integer costId, @Param("orgId") Integer orgId);

    @Select("SELECT\n" +
            "  MIN(CASE WHEN IFNULL(cost_amount_writeoff,0)=cost_amount THEN '1' ELSE '0' END) AS completeWriteoffFlag\n" +
            " ,MAX(CASE WHEN IFNULL(cost_amount_writeoff,0) <> 0 THEN '1' ELSE '0' END) AS writeoffFlag\n" +
            " ,MAX(CASE WHEN IFNULL(cost_amount_payment,0)<>0 THEN '1' ELSE '0' END) AS paymentFlag\n" +
            " ,'1' AS costFlag\n" +
            "FROM io_cost where order_id=#{orderId}\n" +
            "GROUP BY order_id")
    Map<String, String> getOrderCostStatusForIO(Integer orderId);

    @Select("SELECT CASE WHEN MIN(CASE WHEN IFNULL(cost_amount_payment,0)=cost_amount THEN 1 ELSE 0 END) = 1 AND MIN(IFNULL(invoice_status,0)) = 1 THEN '1' ELSE '0' END AS completeInvoice,CASE WHEN MAX(IFNULL(invoice_status,-2)) = -1 THEN '1' ELSE '0' END AS noInvoice,CASE WHEN MAX(IFNULL(invoice_status,-1)) > -1 THEN '1' ELSE '0' END AS InvoiceNoComplete " +
            " FROM io_cost a Left JOIN (select b.cost_id,b.payment_id from css_payment_detail b where b.order_id=#{orderId} and b.org_id=#{orgId} and exists(select c.payment_id from css_payment c where c.business_scope='IO' and c.payment_id=b.payment_id)) d on a.cost_id=d.cost_id LEFT JOIN css_cost_invoice e ON d.payment_id = e.payment_id " +
            " where a.order_id=#{orderId}" +
            " GROUP BY a.order_id")
    Map<String, String> getOrderCostStatusForIOAboutInvoiceStatus(@Param("orderId") Integer orderId, @Param("orgId") Integer orgId);
}
