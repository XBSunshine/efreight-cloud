package com.efreight.afbase.dao;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.efreight.afbase.entity.OrderLockOrUnlock;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
import java.util.List;

public interface OrderLockOrUnlockMapper {
    @Select("<script>" +
            "SELECT c.order_id AS orderId,c.order_uuid AS orderUuid,c.business_scope AS businessScope, c.order_code AS orderCode,CASE WHEN c.business_scope='AI' and (c.awb_number is null or c.awb_number = '') and c.hawb_number is not null and c.hawb_number != '' then c.hawb_number WHEN c.business_scope='AI' and c.awb_number is not null and c.awb_number != '' and c.hawb_number is not null and c.hawb_number != '' then  CONCAT(c.awb_number,'_',c.hawb_number) ELSE c.awb_number END AS awbNumber,c.customer_number AS customerNumber,c.income_recorded AS incomeFinishStatus,c.cost_recorded AS costFinishStatus,\n" +
            "IF(c.order_status='财务锁账',1,0) AS orderLockStatus,c.order_status AS orderStatus,c.income_status AS incomeStatus,c.cost_status AS costStatus,b.incomeAmount,b.costAmount,b.profitAmount,b.lockDate,c.expect_flight AS flightNo,IF(c.business_scope='AE',c.expect_departure,c.expect_arrival) AS flightDate,c.business_product AS businessMethod,\n" +
            "c.departure_station AS departureStation,c.arrival_station AS arrivalStation,d.coop_code AS coopCode,d.coop_name AS coopName,e.awb_from_name AS awbFromName,c.sales_name AS salesName,\n" +
            "c.servicer_name AS servicerName,c.goods_type AS goodsType,IFNULL(c.confirm_pieces,c.plan_pieces) AS planPieces,IFNULL(c.confirm_weight,c.plan_weight) AS planWeight,IFNULL(c.confirm_volume,c.plan_volume) AS planVolume,IFNULL(c.confirm_charge_weight,c.plan_charge_weight) AS planChargeWeight,f.sign_state AS signState\n" +
            "FROM af_order c\n" +
            "LEFT JOIN (SELECT MAX(a.orderId) AS orderId,MAX(a.lockDate) AS lockDate,SUM(a.incomeAmount) AS incomeAmount,SUM(a.costAmount) AS costAmount,SUM(a.incomeAmount)- SUM(a.costAmount) AS profitAmount FROM\n" +
            "(SELECT income_functional_amount AS incomeAmount,0 AS costAmount,order_id AS orderId,financial_date as lockDate FROM af_income WHERE org_id=#{orderLockOrUnlock.orgId}\n" +
            "UNION ALL\n" +
            "SELECT 0 AS incomeAmount,cost_functional_amount AS costAmount,order_id AS orderId,financial_date as lockDate FROM af_cost WHERE org_id=#{orderLockOrUnlock.orgId}) a GROUP BY a.orderId,a.lockDate) b ON c.order_id = b.orderId\n" +
            "LEFT JOIN prm_coop d ON d.coop_id = c.coop_id\n" +
            "LEFT JOIN af_awb_number e ON e.awb_id = c.awb_id AND c.awb_id is not null\n" +
            "LEFT JOIN af_rounting_sign f ON f.order_id = c.order_id\n" +
            "WHERE c.org_id=#{orderLockOrUnlock.orgId} and c.order_status !='强制关闭'" +
            "<when test='orderLockOrUnlock.businessScope!=null and orderLockOrUnlock.businessScope!=\"\"'>" +
            " and c.business_scope=#{orderLockOrUnlock.businessScope}" +
            "</when>" +
            "<when test='orderLockOrUnlock.orderStatus!=null and orderLockOrUnlock.orderStatus!=\"\" and orderLockOrUnlock.orderStatus==\"是\"'>" +
            " and c.order_status='财务锁账'" +
            "</when>" +
            "<when test='orderLockOrUnlock.orderStatus!=null and orderLockOrUnlock.orderStatus!=\"\" and orderLockOrUnlock.orderStatus==\"否\"'>" +
            " and c.order_status!='财务锁账'" +
            "</when>" +
            "<when test='orderLockOrUnlock.incomeFinishStatus!=null and orderLockOrUnlock.incomeFinishStatus==true'>" +
            " and c.income_recorded=#{orderLockOrUnlock.incomeFinishStatus}" +
            "</when>" +
            "<when test='orderLockOrUnlock.incomeFinishStatus!=null and orderLockOrUnlock.incomeFinishStatus==false'>" +
            " and (c.income_recorded=#{orderLockOrUnlock.incomeFinishStatus} or c.income_recorded is null)" +
            "</when>" +
            "<when test='orderLockOrUnlock.costFinishStatus!=null and orderLockOrUnlock.costFinishStatus==true'>" +
            " and c.cost_recorded=#{orderLockOrUnlock.costFinishStatus}" +
            "</when>" +
            "<when test='orderLockOrUnlock.costFinishStatus!=null and orderLockOrUnlock.costFinishStatus==false'>" +
            " and (c.cost_recorded=#{orderLockOrUnlock.costFinishStatus} or c.cost_recorded is null)" +
            "</when>" +
            "<when test='orderLockOrUnlock.orderCode!=null and orderLockOrUnlock.orderCode!=\"\"'>" +
            " and upper(c.order_code) like \"%\"#{orderLockOrUnlock.orderCode}\"%\"" +
            "</when>" +
            "<when test='orderLockOrUnlock.coopName!=null and orderLockOrUnlock.coopName!=\"\"'>" +
            " and (upper(d.coop_code) like \"%\"#{orderLockOrUnlock.coopName}\"%\" or upper(d.coop_name) like \"%\"#{orderLockOrUnlock.coopName}\"%\")" +
            "</when>" +
            "<when test='orderLockOrUnlock.businessScope==\"AE\" and orderLockOrUnlock.awbNumber!=null and orderLockOrUnlock.awbNumber!=\"\"'>" +
            " and c.awb_number like \"%\"#{orderLockOrUnlock.awbNumber}\"%\"" +
            "</when>" +
            "<when test='orderLockOrUnlock.businessScope==\"AI\" and orderLockOrUnlock.awbNumber!=null and orderLockOrUnlock.awbNumber!=\"\"'>" +
            " and CONCAT(c.awb_number,'_',c.hawb_number) like \"%\"#{orderLockOrUnlock.awbNumber}\"%\"" +
            "</when>" +
            "<when test='orderLockOrUnlock.businessScope==\"AE\" and orderLockOrUnlock.flightDateStart!=null'>" +
            " and c.expect_departure <![CDATA[>=]]> #{orderLockOrUnlock.flightDateStart}" +
            "</when>" +
            "<when test='orderLockOrUnlock.businessScope==\"AE\" and orderLockOrUnlock.flightDateEnd!=null'>" +
            " and c.expect_departure <![CDATA[<=]]> #{orderLockOrUnlock.flightDateEnd}" +
            "</when>" +
            "<when test='orderLockOrUnlock.businessScope==\"AI\" and orderLockOrUnlock.flightDateStart!=null'>" +
            " and c.expect_arrival <![CDATA[>=]]> #{orderLockOrUnlock.flightDateStart}" +
            "</when>" +
            "<when test='orderLockOrUnlock.businessScope==\"AI\" and orderLockOrUnlock.flightDateEnd!=null'>" +
            " and c.expect_arrival <![CDATA[<=]]> #{orderLockOrUnlock.flightDateEnd}" +
            "</when>" +
            "<when test='orderLockOrUnlock.flightNo!=null and orderLockOrUnlock.flightNo!=\"\"'>" +
            " and upper(c.expect_flight) like \"%\"#{orderLockOrUnlock.flightNo}\"%\" " +
            "</when>" +
            "order by c.order_code desc" +
            "</script>")
    IPage<OrderLockOrUnlock> pageForAF(Page page, @Param("orderLockOrUnlock") OrderLockOrUnlock orderLockOrUnlock);

    @Select("<script>" +
            "SELECT c.order_id AS orderId,c.order_uuid AS orderUuid,c.business_scope AS businessScope,c.order_code AS orderCode,CASE WHEN c.business_scope='SI' and (c.mbl_number is null or c.mbl_number = '') and c.hbl_number is not null and c.hbl_number != '' then c.hbl_number WHEN c.business_scope='SI' and c.mbl_number is not null and c.mbl_number != '' and c.hbl_number is not null and c.hbl_number != '' then  CONCAT(c.mbl_number,'_',c.hbl_number) ELSE c.mbl_number END AS awbNumber,c.customer_number AS customerNumber,c.income_recorded AS incomeFinishStatus,c.cost_recorded AS costFinishStatus,\n" +
            "IF(c.order_status='财务锁账',1,0) AS orderLockStatus,c.order_status AS orderStatus,c.income_status AS incomeStatus,c.cost_status AS costStatus,b.incomeAmount,b.costAmount,b.profitAmount,b.lockDate,c.ship_voyage_number AS flightNo,IF(c.business_scope='SE',c.expect_departure,c.expect_arrival) AS flightDate,c.container_method AS businessMethod,\n" +
            "f.port_name_en AS departureStation,g.port_name_en AS arrivalStation,d.coop_code AS coopCode,d.coop_name AS coopName,e.coop_name AS awbFromName,c.sales_name AS salesName,\n" +
            "c.servicer_name AS servicerName,c.goods_type AS goodsType,c.plan_pieces AS planPieces,c.plan_weight AS planWeight,c.plan_volume AS planVolume,c.plan_charge_weight AS planChargeWeight\n" +
            "FROM  sc_order c\n" +
            "LEFT JOIN (SELECT MAX(a.orderId) AS orderId,MAX(a.lockDate) AS lockDate,SUM(a.incomeAmount) AS incomeAmount,SUM(a.costAmount) AS costAmount,SUM(a.incomeAmount)- SUM(a.costAmount) AS profitAmount FROM\n" +
            "(SELECT income_functional_amount AS incomeAmount,0 AS costAmount,order_id AS orderId,financial_date as lockDate FROM sc_income WHERE org_id=#{orderLockOrUnlock.orgId}\n" +
            "UNION ALL\n" +
            "SELECT 0 AS incomeAmount,cost_functional_amount AS costAmount,order_id AS orderId,financial_date as lockDate FROM sc_cost WHERE org_id=#{orderLockOrUnlock.orgId}) a GROUP BY a.orderId,a.lockDate) b on c.order_id = b.orderId\n" +
            "LEFT JOIN prm_coop d ON d.coop_id = c.coop_id\n" +
            "LEFT JOIN af_V_prm_coop e ON e.coop_id = c.booking_agent_id AND c.booking_agent_id is not null\n" +
            "LEFT JOIN sc_port_maintenance f ON f.port_code=c.departure_station\n" +
            "LEFT JOIN sc_port_maintenance g ON g.port_code=c.arrival_station\n" +
            "WHERE c.org_id=#{orderLockOrUnlock.orgId} and c.order_status !='强制关闭'" +
            "<when test='orderLockOrUnlock.businessScope!=null and orderLockOrUnlock.businessScope!=\"\"'>" +
            " and c.business_scope=#{orderLockOrUnlock.businessScope}" +
            "</when>" +
            "<when test='orderLockOrUnlock.orderStatus!=null and orderLockOrUnlock.orderStatus!=\"\" and orderLockOrUnlock.orderStatus==\"是\"'>" +
            " and c.order_status='财务锁账'" +
            "</when>" +
            "<when test='orderLockOrUnlock.orderStatus!=null and orderLockOrUnlock.orderStatus!=\"\" and orderLockOrUnlock.orderStatus==\"否\"'>" +
            " and c.order_status!='财务锁账'" +
            "</when>" +
            "<when test='orderLockOrUnlock.incomeFinishStatus!=null and orderLockOrUnlock.incomeFinishStatus==true'>" +
            " and c.income_recorded=#{orderLockOrUnlock.incomeFinishStatus}" +
            "</when>" +
            "<when test='orderLockOrUnlock.incomeFinishStatus!=null and orderLockOrUnlock.incomeFinishStatus==false'>" +
            " and (c.income_recorded=#{orderLockOrUnlock.incomeFinishStatus} or c.income_recorded is null)" +
            "</when>" +
            "<when test='orderLockOrUnlock.costFinishStatus!=null and orderLockOrUnlock.costFinishStatus==true'>" +
            " and c.cost_recorded=#{orderLockOrUnlock.costFinishStatus}" +
            "</when>" +
            "<when test='orderLockOrUnlock.costFinishStatus!=null and orderLockOrUnlock.costFinishStatus==false'>" +
            " and (c.cost_recorded=#{orderLockOrUnlock.costFinishStatus} or c.cost_recorded is null)" +
            "</when>" +
            "<when test='orderLockOrUnlock.coopName!=null and orderLockOrUnlock.coopName!=\"\"'>" +
            " and (upper(d.coop_code) like \"%\"#{orderLockOrUnlock.coopName}\"%\" or upper(d.coop_name) like \"%\"#{orderLockOrUnlock.coopName}\"%\")" +
            "</when>" +
            "<when test='orderLockOrUnlock.orderCode!=null and orderLockOrUnlock.orderCode!=\"\"'>" +
            " and upper(c.order_code) like \"%\"#{orderLockOrUnlock.orderCode}\"%\"" +
            "</when>" +
            "<when test='orderLockOrUnlock.businessScope==\"SE\" and orderLockOrUnlock.awbNumber!=null and orderLockOrUnlock.awbNumber!=\"\"'>" +
            " and c.mbl_number like \"%\"#{orderLockOrUnlock.awbNumber}\"%\"" +
            "</when>" +
            "<when test='orderLockOrUnlock.businessScope==\"SI\" and orderLockOrUnlock.awbNumber!=null and orderLockOrUnlock.awbNumber!=\"\"'>" +
            " and CONCAT(c.mbl_number,'_',c.hbl_number) like \"%\"#{orderLockOrUnlock.awbNumber}\"%\"" +
            "</when>" +
            "<when test='orderLockOrUnlock.businessScope==\"SE\" and orderLockOrUnlock.flightDateStart!=null'>" +
            " and c.expect_departure <![CDATA[>=]]> #{orderLockOrUnlock.flightDateStart}" +
            "</when>" +
            "<when test='orderLockOrUnlock.businessScope==\"SE\" and orderLockOrUnlock.flightDateEnd!=null'>" +
            " and c.expect_departure <![CDATA[<=]]> #{orderLockOrUnlock.flightDateEnd}" +
            "</when>" +
            "<when test='orderLockOrUnlock.businessScope==\"SI\" and orderLockOrUnlock.flightDateStart!=null'>" +
            " and c.expect_arrival <![CDATA[>=]]> #{orderLockOrUnlock.flightDateStart}" +
            "</when>" +
            "<when test='orderLockOrUnlock.businessScope==\"SI\" and orderLockOrUnlock.flightDateEnd!=null'>" +
            " and c.expect_arrival <![CDATA[<=]]> #{orderLockOrUnlock.flightDateEnd}" +
            "</when>" +
            "<when test='orderLockOrUnlock.flightNo!=null and orderLockOrUnlock.flightNo!=\"\"'>" +
            " and upper(c.ship_voyage_number) like \"%\"#{orderLockOrUnlock.flightNo}\"%\" " +
            "</when>" +
            "order by c.order_code desc" +
            "</script>")
    IPage<OrderLockOrUnlock> pageForSC(Page page, @Param("orderLockOrUnlock") OrderLockOrUnlock orderLockOrUnlock);

    @Select("<script>" +
            "SELECT c.order_id AS orderId,c.order_uuid AS orderUuid,c.business_scope AS businessScope,c.order_code AS orderCode,c.rwb_number AS awbNumber,c.customer_number AS customerNumber,c.income_recorded AS incomeFinishStatus,c.cost_recorded AS costFinishStatus,\n" +
            "IF(c.order_status='财务锁账',1,0) AS orderLockStatus,c.order_status AS orderStatus,c.income_status AS incomeStatus,c.cost_status AS costStatus,b.incomeAmount,b.costAmount,b.profitAmount,b.lockDate,'' AS flightNo,IF(c.business_scope='TE',c.expect_departure,c.expect_arrival) AS flightDate,c.container_method AS businessMethod,\n" +
            "c.departure_station AS departureStation,c.arrival_station AS arrivalStation,d.coop_code AS coopCode,d.coop_name AS coopName,e.coop_name AS awbFromName,c.sales_name AS salesName,\n" +
            "c.servicer_name AS servicerName,c.goods_type AS goodsType,c.plan_pieces AS planPieces,c.plan_weight AS planWeight,c.plan_volume AS planVolume,c.plan_charge_weight AS planChargeWeight\n" +
            "FROM  tc_order c\n" +
            "LEFT JOIN (SELECT MAX(a.orderId) AS orderId,MAX(a.lockDate) AS lockDate,SUM(a.incomeAmount) AS incomeAmount,SUM(a.costAmount) AS costAmount,SUM(a.incomeAmount)- SUM(a.costAmount) AS profitAmount FROM\n" +
            "(SELECT income_functional_amount AS incomeAmount,0 AS costAmount,order_id AS orderId,financial_date as lockDate FROM tc_income WHERE org_id=#{orderLockOrUnlock.orgId}\n" +
            "UNION ALL\n" +
            "SELECT 0 AS incomeAmount,cost_functional_amount AS costAmount,order_id AS orderId,financial_date as lockDate FROM tc_cost WHERE org_id=#{orderLockOrUnlock.orgId}) a GROUP BY a.orderId,a.lockDate) b on c.order_id = b.orderId\n" +
            "LEFT JOIN prm_coop d ON d.coop_id = c.coop_id\n" +
            "LEFT JOIN af_V_prm_coop e ON e.coop_id = c.booking_agent_id AND c.booking_agent_id is not null\n" +


            "WHERE c.org_id=#{orderLockOrUnlock.orgId} and c.order_status !='强制关闭'" +
            "<when test='orderLockOrUnlock.businessScope!=null and orderLockOrUnlock.businessScope!=\"\"'>" +
            " and c.business_scope=#{orderLockOrUnlock.businessScope}" +
            "</when>" +
            "<when test='orderLockOrUnlock.orderStatus!=null and orderLockOrUnlock.orderStatus!=\"\" and orderLockOrUnlock.orderStatus==\"是\"'>" +
            " and c.order_status='财务锁账'" +
            "</when>" +
            "<when test='orderLockOrUnlock.orderStatus!=null and orderLockOrUnlock.orderStatus!=\"\" and orderLockOrUnlock.orderStatus==\"否\"'>" +
            " and c.order_status!='财务锁账'" +
            "</when>" +
            "<when test='orderLockOrUnlock.incomeFinishStatus!=null and orderLockOrUnlock.incomeFinishStatus==true'>" +
            " and c.income_recorded=#{orderLockOrUnlock.incomeFinishStatus}" +
            "</when>" +
            "<when test='orderLockOrUnlock.incomeFinishStatus!=null and orderLockOrUnlock.incomeFinishStatus==false'>" +
            " and (c.income_recorded=#{orderLockOrUnlock.incomeFinishStatus} or c.income_recorded is null)" +
            "</when>" +
            "<when test='orderLockOrUnlock.costFinishStatus!=null and orderLockOrUnlock.costFinishStatus==true'>" +
            " and c.cost_recorded=#{orderLockOrUnlock.costFinishStatus}" +
            "</when>" +
            "<when test='orderLockOrUnlock.costFinishStatus!=null and orderLockOrUnlock.costFinishStatus==false'>" +
            " and (c.cost_recorded=#{orderLockOrUnlock.costFinishStatus} or c.cost_recorded is null)" +
            "</when>" +
            "<when test='orderLockOrUnlock.coopName!=null and orderLockOrUnlock.coopName!=\"\"'>" +
            " and (upper(d.coop_code) like \"%\"#{orderLockOrUnlock.coopName}\"%\" or upper(d.coop_name) like \"%\"#{orderLockOrUnlock.coopName}\"%\")" +
            "</when>" +
            "<when test='orderLockOrUnlock.orderCode!=null and orderLockOrUnlock.orderCode!=\"\"'>" +
            " and upper(c.order_code) like \"%\"#{orderLockOrUnlock.orderCode}\"%\"" +
            "</when>" +
            "<when test='orderLockOrUnlock.awbNumber!=null and orderLockOrUnlock.awbNumber!=\"\"'>" +
            " and c.rwb_number like \"%\"#{orderLockOrUnlock.awbNumber}\"%\"" +
            "</when>" +

            "<when test='orderLockOrUnlock.businessScope==\"TE\" and orderLockOrUnlock.flightDateStart!=null'>" +
            " and c.expect_departure <![CDATA[>=]]> #{orderLockOrUnlock.flightDateStart}" +
            "</when>" +
            "<when test='orderLockOrUnlock.businessScope==\"TE\" and orderLockOrUnlock.flightDateEnd!=null'>" +
            " and c.expect_departure <![CDATA[<=]]> #{orderLockOrUnlock.flightDateEnd}" +
            "</when>" +
            "<when test='orderLockOrUnlock.businessScope==\"TI\" and orderLockOrUnlock.flightDateStart!=null'>" +
            " and c.expect_arrival <![CDATA[>=]]> #{orderLockOrUnlock.flightDateStart}" +
            "</when>" +
            "<when test='orderLockOrUnlock.businessScope==\"TI\" and orderLockOrUnlock.flightDateEnd!=null'>" +
            " and c.expect_arrival <![CDATA[<=]]> #{orderLockOrUnlock.flightDateEnd}" +
            "</when>" +
//            "<when test='orderLockOrUnlock.flightNo!=null and orderLockOrUnlock.flightNo!=\"\"'>" +
//            " and upper(c.ship_voyage_number) like \"%\"#{orderLockOrUnlock.flightNo}\"%\" " +
//            "</when>" +
            "order by c.order_code desc" +
            "</script>")
    IPage<OrderLockOrUnlock> pageForTC(Page page, @Param("orderLockOrUnlock") OrderLockOrUnlock orderLockOrUnlock);

    @Select("<script>" +
            "SELECT c.order_id AS orderId,c.order_uuid AS orderUuid,c.business_scope AS businessScope,c.order_code AS orderCode,c.customer_number AS awbNumber,c.customer_number AS customerNumber,c.income_recorded AS incomeFinishStatus,c.cost_recorded AS costFinishStatus,c.shipping_method AS businessMethod,\n" +
            "IF(c.order_status='财务锁账',1,0) AS orderLockStatus,c.order_status AS orderStatus,c.income_status AS incomeStatus,c.cost_status AS costStatus,b.incomeAmount,b.costAmount,b.profitAmount,b.lockDate,'' AS flightNo,c.driving_time AS flightDate,'' AS businessProduct,\n" +
            "c.departure_station AS departureStation,c.arrival_station AS arrivalStation,d.coop_code AS coopCode,d.coop_name AS coopName,'' AS awbFromName,c.sales_name AS salesName,\n" +
            "c.servicer_name AS servicerName,c.goods_type AS goodsType,IFNULL(c.confirm_pieces,c.plan_pieces) AS planPieces,IFNULL(c.confirm_weight,c.plan_weight) AS planWeight,IFNULL(c.confirm_volume,c.plan_volume) AS planVolume,IFNULL(c.confirm_charge_weight,c.plan_charge_weight) AS planChargeWeight\n" +
            "FROM  lc_order c\n" +
            "LEFT JOIN (SELECT MAX(a.orderId) AS orderId,MAX(a.lockDate) AS lockDate,SUM(a.incomeAmount) AS incomeAmount,SUM(a.costAmount) AS costAmount,SUM(a.incomeAmount)- SUM(a.costAmount) AS profitAmount FROM\n" +
            "(SELECT income_functional_amount AS incomeAmount,0 AS costAmount,order_id AS orderId,financial_date as lockDate FROM lc_income WHERE org_id=#{orderLockOrUnlock.orgId}\n" +
            "UNION ALL\n" +
            "SELECT 0 AS incomeAmount,cost_functional_amount AS costAmount,order_id AS orderId,financial_date as lockDate FROM lc_cost WHERE org_id=#{orderLockOrUnlock.orgId}) a GROUP BY a.orderId,a.lockDate) b on c.order_id = b.orderId\n" +
            "LEFT JOIN prm_coop d ON d.coop_id = c.coop_id\n" +

            "WHERE c.org_id=#{orderLockOrUnlock.orgId} and c.order_status !='强制关闭'" +
            "<when test='orderLockOrUnlock.businessScope!=null and orderLockOrUnlock.businessScope!=\"\"'>" +
            " and c.business_scope=#{orderLockOrUnlock.businessScope}" +
            "</when>" +
            "<when test='orderLockOrUnlock.orderStatus!=null and orderLockOrUnlock.orderStatus!=\"\" and orderLockOrUnlock.orderStatus==\"是\"'>" +
            " and c.order_status='财务锁账'" +
            "</when>" +
            "<when test='orderLockOrUnlock.orderStatus!=null and orderLockOrUnlock.orderStatus!=\"\" and orderLockOrUnlock.orderStatus==\"否\"'>" +
            " and c.order_status!='财务锁账'" +
            "</when>" +
            "<when test='orderLockOrUnlock.incomeFinishStatus!=null and orderLockOrUnlock.incomeFinishStatus==true'>" +
            " and c.income_recorded=#{orderLockOrUnlock.incomeFinishStatus}" +
            "</when>" +
            "<when test='orderLockOrUnlock.incomeFinishStatus!=null and orderLockOrUnlock.incomeFinishStatus==false'>" +
            " and (c.income_recorded=#{orderLockOrUnlock.incomeFinishStatus} or c.income_recorded is null)" +
            "</when>" +
            "<when test='orderLockOrUnlock.costFinishStatus!=null and orderLockOrUnlock.costFinishStatus==true'>" +
            " and c.cost_recorded=#{orderLockOrUnlock.costFinishStatus}" +
            "</when>" +
            "<when test='orderLockOrUnlock.costFinishStatus!=null and orderLockOrUnlock.costFinishStatus==false'>" +
            " and (c.cost_recorded=#{orderLockOrUnlock.costFinishStatus} or c.cost_recorded is null)" +
            "</when>" +
            "<when test='orderLockOrUnlock.coopName!=null and orderLockOrUnlock.coopName!=\"\"'>" +
            " and (upper(d.coop_code) like \"%\"#{orderLockOrUnlock.coopName}\"%\" or upper(d.coop_name) like \"%\"#{orderLockOrUnlock.coopName}\"%\")" +
            "</when>" +
            "<when test='orderLockOrUnlock.orderCode!=null and orderLockOrUnlock.orderCode!=\"\"'>" +
            " and upper(c.order_code) like \"%\"#{orderLockOrUnlock.orderCode}\"%\"" +
            "</when>" +
            "<when test='orderLockOrUnlock.awbNumber!=null and orderLockOrUnlock.awbNumber!=\"\"'>" +
            " and c.customer_number like \"%\"#{orderLockOrUnlock.awbNumber}\"%\"" +
            "</when>" +

            "<when test='orderLockOrUnlock.flightDateStart!=null'>" +
            " and c.driving_time <![CDATA[>=]]> #{orderLockOrUnlock.flightDateStart}" +
            "</when>" +
            "<when test='orderLockOrUnlock.flightDateEnd!=null'>" +
            " and c.driving_time <![CDATA[<=]]> #{orderLockOrUnlock.flightDateEnd}" +
            "</when>" +
            "order by c.order_code desc" +
            "</script>")
    IPage<OrderLockOrUnlock> pageForLC(Page page, @Param("orderLockOrUnlock") OrderLockOrUnlock orderLockOrUnlock);

    @Select("<script>" +
            "SELECT c.order_id AS orderId,c.order_uuid AS orderUuid,c.business_scope AS businessScope,c.order_code AS orderCode,c.customer_number AS awbNumber,c.customer_number AS customerNumber,c.income_recorded AS incomeFinishStatus,c.cost_recorded AS costFinishStatus,c.business_method,\n" +
            "IF(c.order_status='财务锁账',1,0) AS orderLockStatus,c.order_status AS orderStatus,c.income_status AS incomeStatus,c.cost_status AS costStatus,b.incomeAmount,b.costAmount,b.profitAmount,b.lockDate,'' AS flightNo,c.business_date AS flightDate,'' AS businessProduct,\n" +
            "c.departure_station AS departureStation,c.arrival_station AS arrivalStation,d.coop_code AS coopCode,d.coop_name AS coopName,'' AS awbFromName,c.sales_name AS salesName,\n" +
            "c.servicer_name AS servicerName,c.goods_type AS goodsType,c.plan_pieces AS planPieces,c.plan_weight AS planWeight,c.plan_volume AS planVolume,c.plan_charge_weight AS planChargeWeight\n" +
            "FROM  io_order c\n" +
            "LEFT JOIN (SELECT MAX(a.orderId) AS orderId,MAX(a.lockDate) AS lockDate,SUM(a.incomeAmount) AS incomeAmount,SUM(a.costAmount) AS costAmount,SUM(a.incomeAmount)- SUM(a.costAmount) AS profitAmount FROM\n" +
            "(SELECT income_functional_amount AS incomeAmount,0 AS costAmount,order_id AS orderId,financial_date as lockDate FROM io_income WHERE org_id=#{orderLockOrUnlock.orgId}\n" +
            "UNION ALL\n" +
            "SELECT 0 AS incomeAmount,cost_functional_amount AS costAmount,order_id AS orderId,financial_date as lockDate FROM io_cost WHERE org_id=#{orderLockOrUnlock.orgId}) a GROUP BY a.orderId,a.lockDate) b on c.order_id = b.orderId\n" +
            "LEFT JOIN prm_coop d ON d.coop_id = c.coop_id\n" +

            "WHERE c.org_id=#{orderLockOrUnlock.orgId} and c.order_status !='强制关闭'" +
            "<when test='orderLockOrUnlock.businessScope!=null and orderLockOrUnlock.businessScope!=\"\"'>" +
            " and c.business_scope=#{orderLockOrUnlock.businessScope}" +
            "</when>" +
            "<when test='orderLockOrUnlock.orderStatus!=null and orderLockOrUnlock.orderStatus!=\"\" and orderLockOrUnlock.orderStatus==\"是\"'>" +
            " and c.order_status='财务锁账'" +
            "</when>" +
            "<when test='orderLockOrUnlock.orderStatus!=null and orderLockOrUnlock.orderStatus!=\"\" and orderLockOrUnlock.orderStatus==\"否\"'>" +
            " and c.order_status!='财务锁账'" +
            "</when>" +
            "<when test='orderLockOrUnlock.incomeFinishStatus!=null and orderLockOrUnlock.incomeFinishStatus==true'>" +
            " and c.income_recorded=#{orderLockOrUnlock.incomeFinishStatus}" +
            "</when>" +
            "<when test='orderLockOrUnlock.incomeFinishStatus!=null and orderLockOrUnlock.incomeFinishStatus==false'>" +
            " and (c.income_recorded=#{orderLockOrUnlock.incomeFinishStatus} or c.income_recorded is null)" +
            "</when>" +
            "<when test='orderLockOrUnlock.costFinishStatus!=null and orderLockOrUnlock.costFinishStatus==true'>" +
            " and c.cost_recorded=#{orderLockOrUnlock.costFinishStatus}" +
            "</when>" +
            "<when test='orderLockOrUnlock.costFinishStatus!=null and orderLockOrUnlock.costFinishStatus==false'>" +
            " and (c.cost_recorded=#{orderLockOrUnlock.costFinishStatus} or c.cost_recorded is null)" +
            "</when>" +
            "<when test='orderLockOrUnlock.coopName!=null and orderLockOrUnlock.coopName!=\"\"'>" +
            " and (upper(d.coop_code) like \"%\"#{orderLockOrUnlock.coopName}\"%\" or upper(d.coop_name) like \"%\"#{orderLockOrUnlock.coopName}\"%\")" +
            "</when>" +
            "<when test='orderLockOrUnlock.orderCode!=null and orderLockOrUnlock.orderCode!=\"\"'>" +
            " and upper(c.order_code) like \"%\"#{orderLockOrUnlock.orderCode}\"%\"" +
            "</when>" +
            "<when test='orderLockOrUnlock.awbNumber!=null and orderLockOrUnlock.awbNumber!=\"\"'>" +
            " and c.customer_number like \"%\"#{orderLockOrUnlock.awbNumber}\"%\"" +
            "</when>" +

            "<when test='orderLockOrUnlock.flightDateStart!=null'>" +
            " and c.business_date <![CDATA[>=]]> #{orderLockOrUnlock.flightDateStart}" +
            "</when>" +
            "<when test='orderLockOrUnlock.flightDateEnd!=null'>" +
            " and c.business_date <![CDATA[<=]]> #{orderLockOrUnlock.flightDateEnd}" +
            "</when>" +
            "order by c.order_code desc" +
            "</script>")
    IPage<OrderLockOrUnlock> pageForIO(Page page, @Param("orderLockOrUnlock") OrderLockOrUnlock orderLockOrUnlock);

    @Update("update sc_order set order_status='财务锁账',income_recorded=1,cost_recorded=1,edit_time=#{now},editor_name=#{editorName},editor_id=#{editorId},row_uuid=#{rowUuid} where org_id=#{orgId} and order_id in (${orderIds})")
    void lockOrderForSC(@Param("orderIds") String orderIds, @Param("orgId") Integer orgId, @Param("now") LocalDateTime now, @Param("editorName") String editorName, @Param("editorId") Integer editorId, @Param("rowUuid") String rowUuid);

    @Update("update tc_order set order_status='财务锁账',income_recorded=1,cost_recorded=1,edit_time=#{now},editor_name=#{editorName},editor_id=#{editorId},row_uuid=#{rowUuid} where org_id=#{orgId} and order_id in (${orderIds})")
    void lockOrderForTC(@Param("orderIds") String orderIds, @Param("orgId") Integer orgId, @Param("now") LocalDateTime now, @Param("editorName") String editorName, @Param("editorId") Integer editorId, @Param("rowUuid") String rowUuid);

    @Update("update lc_order set order_status='财务锁账',income_recorded=1,cost_recorded=1,edit_time=#{now},editor_name=#{editorName},editor_id=#{editorId},row_uuid=#{rowUuid} where org_id=#{orgId} and order_id in (${orderIds})")
    void lockOrderForLC(@Param("orderIds") String orderIds, @Param("orgId") Integer orgId, @Param("now") LocalDateTime now, @Param("editorName") String editorName, @Param("editorId") Integer editorId, @Param("rowUuid") String rowUuid);

    @Update("update io_order set order_status='财务锁账',income_recorded=1,cost_recorded=1,edit_time=#{now},editor_name=#{editorName},editor_id=#{editorId},row_uuid=#{rowUuid} where org_id=#{orgId} and order_id in (${orderIds})")
    void lockOrderForIO(@Param("orderIds") String orderIds, @Param("orgId") Integer orgId, @Param("now") LocalDateTime now, @Param("editorName") String editorName, @Param("editorId") Integer editorId, @Param("rowUuid") String rowUuid);

    @Update("update sc_income set financial_date = #{lockDate} where org_id=#{orgId} and order_id in (${orderIds}) and financial_date is null")
    void lockIncomeForSC(@Param("orderIds") String orderIds, @Param("orgId") Integer orgId, @Param("lockDate") LocalDateTime lockDate);

    @Update("update tc_income set financial_date = #{lockDate} where org_id=#{orgId} and order_id in (${orderIds}) and financial_date is null")
    void lockIncomeForTC(@Param("orderIds") String orderIds, @Param("orgId") Integer orgId, @Param("lockDate") LocalDateTime lockDate);

    @Update("update lc_income set financial_date = #{lockDate} where org_id=#{orgId} and order_id in (${orderIds}) and financial_date is null")
    void lockIncomeForLC(@Param("orderIds") String orderIds, @Param("orgId") Integer orgId, @Param("lockDate") LocalDateTime lockDate);

    @Update("update io_income set financial_date = #{lockDate} where org_id=#{orgId} and order_id in (${orderIds}) and financial_date is null")
    void lockIncomeForIO(@Param("orderIds") String orderIds, @Param("orgId") Integer orgId, @Param("lockDate") LocalDateTime lockDate);


    @Update("update sc_cost set financial_date = #{lockDate} where org_id=#{orgId} and order_id in (${orderIds}) and financial_date is null")
    void lockCostForSC(@Param("orderIds") String orderIds, @Param("orgId") Integer orgId, @Param("lockDate") LocalDateTime lockDate);

    @Update("update tc_cost set financial_date = #{lockDate} where org_id=#{orgId} and order_id in (${orderIds}) and financial_date is null")
    void lockCostForTC(@Param("orderIds") String orderIds, @Param("orgId") Integer orgId, @Param("lockDate") LocalDateTime lockDate);

    @Update("update lc_cost set financial_date = #{lockDate} where org_id=#{orgId} and order_id in (${orderIds}) and financial_date is null")
    void lockCostForLC(@Param("orderIds") String orderIds, @Param("orgId") Integer orgId, @Param("lockDate") LocalDateTime lockDate);

    @Update("update io_cost set financial_date = #{lockDate} where org_id=#{orgId} and order_id in (${orderIds}) and financial_date is null")
    void lockCostForIO(@Param("orderIds") String orderIds, @Param("orgId") Integer orgId, @Param("lockDate") LocalDateTime lockDate);

    @Update("update af_order set order_status='财务锁账',income_recorded=1,cost_recorded=1,edit_time=#{now},editor_name=#{editorName},editor_id=#{editorId},row_uuid=#{rowUuid} where org_id=#{orgId} and order_id in (${orderIds})")
    void lockOrderForAF(@Param("orderIds") String orderIds, @Param("orgId") Integer orgId, @Param("now") LocalDateTime now, @Param("editorName") String editorName, @Param("editorId") Integer editorId, @Param("rowUuid") String rowUuid);

    @Update("update af_income set financial_date = #{lockDate} where org_id=#{orgId} and order_id in (${orderIds}) and financial_date is null")
    void lockIncomeForAF(@Param("orderIds") String orderIds, @Param("orgId") Integer orgId, @Param("lockDate") LocalDateTime lockDate);

    @Update("update af_cost set financial_date = #{lockDate} where org_id=#{orgId} and order_id in (${orderIds}) and financial_date is null")
    void lockCostForAF(@Param("orderIds") String orderIds, @Param("orgId") Integer orgId, @Param("lockDate") LocalDateTime lockDate);

    @Update("update sc_order set order_status=#{orderStatus},edit_time=#{now},editor_name=#{editorName},editor_id=#{editorId},row_uuid=#{rowUuid} where org_id=#{orgId} and order_id=#{orderId}")
    void unlockOrderForSC(@Param("orderId") Integer orderId, @Param("orgId") Integer orgId, @Param("now") LocalDateTime now, @Param("editorName") String editorName, @Param("editorId") Integer editorId, @Param("rowUuid") String rowUuid, @Param("orderStatus") String orderStatus);

    @Update("update tc_order set order_status=#{orderStatus},edit_time=#{now},editor_name=#{editorName},editor_id=#{editorId},row_uuid=#{rowUuid} where org_id=#{orgId} and order_id=#{orderId}")
    void unlockOrderForTC(@Param("orderId") Integer orderId, @Param("orgId") Integer orgId, @Param("now") LocalDateTime now, @Param("editorName") String editorName, @Param("editorId") Integer editorId, @Param("rowUuid") String rowUuid, @Param("orderStatus") String orderStatus);

    @Update("update lc_order set order_status=#{orderStatus},edit_time=#{now},editor_name=#{editorName},editor_id=#{editorId},row_uuid=#{rowUuid} where org_id=#{orgId} and order_id=#{orderId}")
    void unlockOrderForLC(@Param("orderId") Integer orderId, @Param("orgId") Integer orgId, @Param("now") LocalDateTime now, @Param("editorName") String editorName, @Param("editorId") Integer editorId, @Param("rowUuid") String rowUuid, @Param("orderStatus") String orderStatus);

    @Update("update io_order set order_status=#{orderStatus},edit_time=#{now},editor_name=#{editorName},editor_id=#{editorId},row_uuid=#{rowUuid} where org_id=#{orgId} and order_id=#{orderId}")
    void unlockOrderForIO(@Param("orderId") Integer orderId, @Param("orgId") Integer orgId, @Param("now") LocalDateTime now, @Param("editorName") String editorName, @Param("editorId") Integer editorId, @Param("rowUuid") String rowUuid, @Param("orderStatus") String orderStatus);

    @Update("update af_order set order_status=#{orderStatus},edit_time=#{now},editor_name=#{editorName},editor_id=#{editorId},row_uuid=#{rowUuid} where org_id=#{orgId} and order_id=#{orderId}")
    void unlockOrderForAF(@Param("orderId") Integer orderId, @Param("orgId") Integer orgId, @Param("now") LocalDateTime now, @Param("editorName") String editorName, @Param("editorId") Integer editorId, @Param("rowUuid") String rowUuid, @Param("orderStatus") String orderStatus);

    @Select("<script>" +
            "SELECT c.order_id AS orderId, c.order_code AS orderCode,CASE WHEN c.business_scope='AI' and (c.awb_number is null or c.awb_number = '') and c.hawb_number is not null and c.hawb_number != '' then c.hawb_number WHEN c.business_scope='AI' and c.awb_number is not null and c.awb_number != '' and c.hawb_number is not null and c.hawb_number != '' then  CONCAT(c.awb_number,'_',c.hawb_number) ELSE c.awb_number END AS awbNumber,c.customer_number AS customerNumber,c.income_recorded AS incomeFinishStatus,c.cost_recorded AS costFinishStatus,\n" +
            "IF(c.order_status='财务锁账',1,0) AS orderLockStatus,c.order_status AS orderStatus,c.income_status AS incomeStatus,c.cost_status AS costStatus,b.incomeAmount,b.costAmount,b.profitAmount,b.lockDate,c.expect_flight AS flightNo,IF(c.business_scope='AE',c.expect_departure,c.expect_arrival) AS flightDate,c.business_product AS business_method,\n" +
            "c.departure_station AS departureStation,c.arrival_station AS arrivalStation,d.coop_code AS coopCode,d.coop_name AS coopName,e.awb_from_name AS awbFromName,substring_index(c.sales_name,' ',1) AS salesName,\n" +
            "substring_index(c.servicer_name,' ',1) AS servicerName,c.goods_type AS goodsType,c.plan_pieces AS planPieces,c.plan_weight AS planWeight,c.plan_volume AS planVolume,c.plan_charge_weight AS planChargeWeight\n" +
            "FROM af_order c\n" +
            "LEFT JOIN (SELECT MAX(a.orderId) AS orderId,MAX(a.lockDate) AS lockDate,SUM(a.incomeAmount) AS incomeAmount,SUM(a.costAmount) AS costAmount,SUM(a.incomeAmount)- SUM(a.costAmount) AS profitAmount FROM\n" +
            "(SELECT income_functional_amount AS incomeAmount,0 AS costAmount,order_id AS orderId,financial_date as lockDate FROM af_income WHERE org_id=#{orderLockOrUnlock.orgId}\n" +
            "UNION ALL\n" +
            "SELECT 0 AS incomeAmount,cost_functional_amount AS costAmount,order_id AS orderId,financial_date as lockDate FROM af_cost WHERE org_id=#{orderLockOrUnlock.orgId}) a GROUP BY a.orderId,a.lockDate) b on c.order_id = b.orderId\n" +
            "LEFT JOIN prm_coop d ON d.coop_id = c.coop_id\n" +
            "LEFT JOIN af_awb_number e ON e.awb_id = c.awb_id AND c.awb_id is not null\n" +
            "WHERE c.org_id=#{orderLockOrUnlock.orgId} and c.order_status !='强制关闭'" +
            "<when test='orderLockOrUnlock.businessScope!=null and orderLockOrUnlock.businessScope!=\"\"'>" +
            " and c.business_scope=#{orderLockOrUnlock.businessScope}" +
            "</when>" +
            "<when test='orderLockOrUnlock.orderStatus!=null and orderLockOrUnlock.orderStatus!=\"\" and orderLockOrUnlock.orderStatus==\"是\"'>" +
            " and c.order_status='财务锁账'" +
            "</when>" +
            "<when test='orderLockOrUnlock.orderStatus!=null and orderLockOrUnlock.orderStatus!=\"\" and orderLockOrUnlock.orderStatus==\"否\"'>" +
            " and c.order_status!='财务锁账'" +
            "</when>" +
            "<when test='orderLockOrUnlock.incomeFinishStatus!=null and orderLockOrUnlock.incomeFinishStatus==true'>" +
            " and c.income_recorded=#{orderLockOrUnlock.incomeFinishStatus}" +
            "</when>" +
            "<when test='orderLockOrUnlock.incomeFinishStatus!=null and orderLockOrUnlock.incomeFinishStatus==false'>" +
            " and (c.income_recorded=#{orderLockOrUnlock.incomeFinishStatus} or c.income_recorded is null)" +
            "</when>" +
            "<when test='orderLockOrUnlock.costFinishStatus!=null and orderLockOrUnlock.costFinishStatus==true'>" +
            " and c.cost_recorded=#{orderLockOrUnlock.costFinishStatus}" +
            "</when>" +
            "<when test='orderLockOrUnlock.costFinishStatus!=null and orderLockOrUnlock.costFinishStatus==false'>" +
            " and (c.cost_recorded=#{orderLockOrUnlock.costFinishStatus} or c.cost_recorded is null)" +
            "</when>" +
            "<when test='orderLockOrUnlock.coopName!=null and orderLockOrUnlock.coopName!=\"\"'>" +
            " and (upper(d.coop_code) like \"%\"#{orderLockOrUnlock.coopName}\"%\" or upper(d.coop_name) like \"%\"#{orderLockOrUnlock.coopName}\"%\")" +
            "</when>" +
            "<when test='orderLockOrUnlock.orderCode!=null and orderLockOrUnlock.orderCode!=\"\"'>" +
            " and upper(c.order_code) like \"%\"#{orderLockOrUnlock.orderCode}\"%\"" +
            "</when>" +
            "<when test='orderLockOrUnlock.businessScope==\"AE\" and orderLockOrUnlock.awbNumber!=null and orderLockOrUnlock.awbNumber!=\"\"'>" +
            " and c.awb_number like \"%\"#{orderLockOrUnlock.awbNumber}\"%\"" +
            "</when>" +
            "<when test='orderLockOrUnlock.businessScope==\"AI\" and orderLockOrUnlock.awbNumber!=null and orderLockOrUnlock.awbNumber!=\"\"'>" +
            " and CONCAT(c.awb_number,'_',c.hawb_number) like \"%\"#{orderLockOrUnlock.awbNumber}\"%\"" +
            "</when>" +
            "<when test='orderLockOrUnlock.businessScope==\"AE\" and orderLockOrUnlock.flightDateStart!=null'>" +
            " and c.expect_departure <![CDATA[>=]]> #{orderLockOrUnlock.flightDateStart}" +
            "</when>" +
            "<when test='orderLockOrUnlock.businessScope==\"AE\" and orderLockOrUnlock.flightDateEnd!=null'>" +
            " and c.expect_departure <![CDATA[<=]]> #{orderLockOrUnlock.flightDateEnd}" +
            "</when>" +
            "<when test='orderLockOrUnlock.businessScope==\"AI\" and orderLockOrUnlock.flightDateStart!=null'>" +
            " and c.expect_arrival <![CDATA[>=]]> #{orderLockOrUnlock.flightDateStart}" +
            "</when>" +
            "<when test='orderLockOrUnlock.businessScope==\"AI\" and orderLockOrUnlock.flightDateEnd!=null'>" +
            " and c.expect_arrival <![CDATA[<=]]> #{orderLockOrUnlock.flightDateEnd}" +
            "</when>" +
            "<when test='orderLockOrUnlock.flightNo!=null and orderLockOrUnlock.flightNo!=\"\"'>" +
            " and upper(c.expect_flight) like \"%\"#{orderLockOrUnlock.flightNo}\"%\" " +
            "</when>" +
            "order by c.order_code desc" +
            "</script>")
    List<OrderLockOrUnlock> getListForAF(@Param("orderLockOrUnlock") OrderLockOrUnlock orderLockOrUnlock);

    @Select("<script>" +
            "SELECT c.order_id AS orderId,c.order_code AS orderCode,CASE WHEN c.business_scope='SI' and (c.mbl_number is null or c.mbl_number = '') and c.hbl_number is not null and c.hbl_number != '' then c.hbl_number WHEN c.business_scope='SI' and c.mbl_number is not null and c.mbl_number != '' and c.hbl_number is not null and c.hbl_number != '' then  CONCAT(c.mbl_number,'_',c.hbl_number) ELSE c.mbl_number END AS awbNumber,c.customer_number AS customerNumber,c.income_recorded AS incomeFinishStatus,c.cost_recorded AS costFinishStatus,\n" +
            "IF(c.order_status='财务锁账',1,0) AS orderLockStatus,c.order_status AS orderStatus,c.income_status AS incomeStatus,c.cost_status AS costStatus,b.incomeAmount,b.costAmount,b.profitAmount,b.lockDate,c.ship_voyage_number AS flightNo,IF(c.business_scope='SE',c.expect_departure,c.expect_arrival) AS flightDate,c.container_method AS business_method,\n" +
            "f.port_name_en AS departureStation,g.port_name_en AS arrivalStation,d.coop_code AS coopCode,d.coop_name AS coopName,e.coop_name AS awbFromName,substring_index(c.sales_name,' ',1) AS salesName,\n" +
            "substring_index(c.servicer_name,' ',1) AS servicerName,c.goods_type AS goodsType,c.plan_pieces AS planPieces,c.plan_weight AS planWeight,c.plan_volume AS planVolume,c.plan_charge_weight AS planChargeWeight\n" +
            "FROM sc_order c\n" +
            "LEFT JOIN (SELECT MAX(a.orderId) AS orderId,MAX(a.lockDate) AS lockDate,SUM(a.incomeAmount) AS incomeAmount,SUM(a.costAmount) AS costAmount,SUM(a.incomeAmount)- SUM(a.costAmount) AS profitAmount FROM\n" +
            "(SELECT income_functional_amount AS incomeAmount,0 AS costAmount,order_id AS orderId,financial_date as lockDate FROM sc_income WHERE org_id=#{orderLockOrUnlock.orgId}\n" +
            "UNION ALL\n" +
            "SELECT 0 AS incomeAmount,cost_functional_amount AS costAmount,order_id AS orderId,financial_date as lockDate FROM sc_cost WHERE org_id=#{orderLockOrUnlock.orgId}) a GROUP BY a.orderId,a.lockDate) b on c.order_id = b.orderId\n" +
            "LEFT JOIN prm_coop d ON d.coop_id = c.coop_id\n" +
            "LEFT JOIN af_V_prm_coop e ON e.coop_id = c.booking_agent_id AND c.booking_agent_id is not null\n" +
            "LEFT JOIN sc_port_maintenance f ON f.port_code=c.departure_station\n" +
            "LEFT JOIN sc_port_maintenance g ON g.port_code=c.arrival_station\n" +
            "WHERE c.org_id=#{orderLockOrUnlock.orgId} and c.order_status !='强制关闭'" +
            "<when test='orderLockOrUnlock.businessScope!=null and orderLockOrUnlock.businessScope!=\"\"'>" +
            " and c.business_scope=#{orderLockOrUnlock.businessScope}" +
            "</when>" +
            "<when test='orderLockOrUnlock.orderStatus!=null and orderLockOrUnlock.orderStatus!=\"\" and orderLockOrUnlock.orderStatus==\"是\"'>" +
            " and c.order_status='财务锁账'" +
            "</when>" +
            "<when test='orderLockOrUnlock.orderStatus!=null and orderLockOrUnlock.orderStatus!=\"\" and orderLockOrUnlock.orderStatus==\"否\"'>" +
            " and c.order_status!='财务锁账'" +
            "</when>" +
            "<when test='orderLockOrUnlock.incomeFinishStatus!=null and orderLockOrUnlock.incomeFinishStatus==true'>" +
            " and c.income_recorded=#{orderLockOrUnlock.incomeFinishStatus}" +
            "</when>" +
            "<when test='orderLockOrUnlock.incomeFinishStatus!=null and orderLockOrUnlock.incomeFinishStatus==false'>" +
            " and (c.income_recorded=#{orderLockOrUnlock.incomeFinishStatus} or c.income_recorded is null)" +
            "</when>" +
            "<when test='orderLockOrUnlock.costFinishStatus!=null and orderLockOrUnlock.costFinishStatus==true'>" +
            " and c.cost_recorded=#{orderLockOrUnlock.costFinishStatus}" +
            "</when>" +
            "<when test='orderLockOrUnlock.costFinishStatus!=null and orderLockOrUnlock.costFinishStatus==false'>" +
            " and (c.cost_recorded=#{orderLockOrUnlock.costFinishStatus} or c.cost_recorded is null)" +
            "</when>" +
            "<when test='orderLockOrUnlock.coopName!=null and orderLockOrUnlock.coopName!=\"\"'>" +
            " and (upper(d.coop_code) like \"%\"#{orderLockOrUnlock.coopName}\"%\" or upper(d.coop_name) like \"%\"#{orderLockOrUnlock.coopName}\"%\")" +
            "</when>" +
            "<when test='orderLockOrUnlock.orderCode!=null and orderLockOrUnlock.orderCode!=\"\"'>" +
            " and upper(c.order_code) like \"%\"#{orderLockOrUnlock.orderCode}\"%\"" +
            "</when>" +
            "<when test='orderLockOrUnlock.businessScope==\"SE\" and orderLockOrUnlock.awbNumber!=null and orderLockOrUnlock.awbNumber!=\"\"'>" +
            " and c.mbl_number like \"%\"#{orderLockOrUnlock.awbNumber}\"%\"" +
            "</when>" +
            "<when test='orderLockOrUnlock.businessScope==\"SI\" and orderLockOrUnlock.awbNumber!=null and orderLockOrUnlock.awbNumber!=\"\"'>" +
            " and CONCAT(c.mbl_number,'_',c.hbl_number) like \"%\"#{orderLockOrUnlock.awbNumber}\"%\"" +
            "</when>" +
            "<when test='orderLockOrUnlock.businessScope==\"SE\" and orderLockOrUnlock.flightDateStart!=null'>" +
            " and c.expect_departure <![CDATA[>=]]> #{orderLockOrUnlock.flightDateStart}" +
            "</when>" +
            "<when test='orderLockOrUnlock.businessScope==\"SE\" and orderLockOrUnlock.flightDateEnd!=null'>" +
            " and c.expect_departure <![CDATA[<=]]> #{orderLockOrUnlock.flightDateEnd}" +
            "</when>" +
            "<when test='orderLockOrUnlock.businessScope==\"SI\" and orderLockOrUnlock.flightDateStart!=null'>" +
            " and c.expect_arrival <![CDATA[>=]]> #{orderLockOrUnlock.flightDateStart}" +
            "</when>" +
            "<when test='orderLockOrUnlock.businessScope==\"SI\" and orderLockOrUnlock.flightDateEnd!=null'>" +
            " and c.expect_arrival <![CDATA[<=]]> #{orderLockOrUnlock.flightDateEnd}" +
            "</when>" +
            "<when test='orderLockOrUnlock.flightNo!=null and orderLockOrUnlock.flightNo!=\"\"'>" +
            " and upper(c.ship_voyage_number) like \"%\"#{orderLockOrUnlock.flightNo}\"%\" " +
            "</when>" +
            "order by c.order_code desc" +
            "</script>")
    List<OrderLockOrUnlock> getListForSC(@Param("orderLockOrUnlock") OrderLockOrUnlock orderLockOrUnlock);

    @Select("<script>" +
            "SELECT c.order_id AS orderId,c.order_uuid AS orderUuid,c.business_scope AS businessScope,c.order_code AS orderCode,c.rwb_number AS awbNumber,c.customer_number AS customerNumber,c.income_recorded AS incomeFinishStatus,c.cost_recorded AS costFinishStatus,\n" +
            "IF(c.order_status='财务锁账',1,0) AS orderLockStatus,c.order_status AS orderStatus,c.income_status AS incomeStatus,c.cost_status AS costStatus,b.incomeAmount,b.costAmount,b.profitAmount,b.lockDate,'' AS flightNo,IF(c.business_scope='TE',c.expect_departure,c.expect_arrival) AS flightDate,c.container_method AS business_method,\n" +
            "c.departure_station AS departureStation,c.arrival_station AS arrivalStation,d.coop_code AS coopCode,d.coop_name AS coopName,e.coop_name AS awbFromName,c.sales_name AS salesName,\n" +
            "c.servicer_name AS servicerName,c.goods_type AS goodsType,c.plan_pieces AS planPieces,c.plan_weight AS planWeight,c.plan_volume AS planVolume,c.plan_charge_weight AS planChargeWeight\n" +
            "FROM  tc_order c\n" +
            "LEFT JOIN (SELECT MAX(a.orderId) AS orderId,MAX(a.lockDate) AS lockDate,SUM(a.incomeAmount) AS incomeAmount,SUM(a.costAmount) AS costAmount,SUM(a.incomeAmount)- SUM(a.costAmount) AS profitAmount FROM\n" +
            "(SELECT income_functional_amount AS incomeAmount,0 AS costAmount,order_id AS orderId,financial_date as lockDate FROM tc_income WHERE org_id=#{orderLockOrUnlock.orgId}\n" +
            "UNION ALL\n" +
            "SELECT 0 AS incomeAmount,cost_functional_amount AS costAmount,order_id AS orderId,financial_date as lockDate FROM tc_cost WHERE org_id=#{orderLockOrUnlock.orgId}) a GROUP BY a.orderId,a.lockDate) b on c.order_id = b.orderId\n" +
            "LEFT JOIN prm_coop d ON d.coop_id = c.coop_id\n" +
            "LEFT JOIN af_V_prm_coop e ON e.coop_id = c.booking_agent_id AND c.booking_agent_id is not null\n" +

            "WHERE c.org_id=#{orderLockOrUnlock.orgId} and c.order_status !='强制关闭'" +
            "<when test='orderLockOrUnlock.businessScope!=null and orderLockOrUnlock.businessScope!=\"\"'>" +
            " and c.business_scope=#{orderLockOrUnlock.businessScope}" +
            "</when>" +
            "<when test='orderLockOrUnlock.orderStatus!=null and orderLockOrUnlock.orderStatus!=\"\" and orderLockOrUnlock.orderStatus==\"是\"'>" +
            " and c.order_status='财务锁账'" +
            "</when>" +
            "<when test='orderLockOrUnlock.orderStatus!=null and orderLockOrUnlock.orderStatus!=\"\" and orderLockOrUnlock.orderStatus==\"否\"'>" +
            " and c.order_status!='财务锁账'" +
            "</when>" +
            "<when test='orderLockOrUnlock.incomeFinishStatus!=null and orderLockOrUnlock.incomeFinishStatus==true'>" +
            " and c.income_recorded=#{orderLockOrUnlock.incomeFinishStatus}" +
            "</when>" +
            "<when test='orderLockOrUnlock.incomeFinishStatus!=null and orderLockOrUnlock.incomeFinishStatus==false'>" +
            " and (c.income_recorded=#{orderLockOrUnlock.incomeFinishStatus} or c.income_recorded is null)" +
            "</when>" +
            "<when test='orderLockOrUnlock.costFinishStatus!=null and orderLockOrUnlock.costFinishStatus==true'>" +
            " and c.cost_recorded=#{orderLockOrUnlock.costFinishStatus}" +
            "</when>" +
            "<when test='orderLockOrUnlock.costFinishStatus!=null and orderLockOrUnlock.costFinishStatus==false'>" +
            " and (c.cost_recorded=#{orderLockOrUnlock.costFinishStatus} or c.cost_recorded is null)" +
            "</when>" +
            "<when test='orderLockOrUnlock.coopName!=null and orderLockOrUnlock.coopName!=\"\"'>" +
            " and (upper(d.coop_code) like \"%\"#{orderLockOrUnlock.coopName}\"%\" or upper(d.coop_name) like \"%\"#{orderLockOrUnlock.coopName}\"%\")" +
            "</when>" +
            "<when test='orderLockOrUnlock.orderCode!=null and orderLockOrUnlock.orderCode!=\"\"'>" +
            " and upper(c.order_code) like \"%\"#{orderLockOrUnlock.orderCode}\"%\"" +
            "</when>" +
            "<when test='orderLockOrUnlock.awbNumber!=null and orderLockOrUnlock.awbNumber!=\"\"'>" +
            " and c.rwb_number like \"%\"#{orderLockOrUnlock.awbNumber}\"%\"" +
            "</when>" +

            "<when test='orderLockOrUnlock.businessScope==\"TE\" and orderLockOrUnlock.flightDateStart!=null'>" +
            " and c.expect_departure <![CDATA[>=]]> #{orderLockOrUnlock.flightDateStart}" +
            "</when>" +
            "<when test='orderLockOrUnlock.businessScope==\"TE\" and orderLockOrUnlock.flightDateEnd!=null'>" +
            " and c.expect_departure <![CDATA[<=]]> #{orderLockOrUnlock.flightDateEnd}" +
            "</when>" +
            "<when test='orderLockOrUnlock.businessScope==\"TI\" and orderLockOrUnlock.flightDateStart!=null'>" +
            " and c.expect_arrival <![CDATA[>=]]> #{orderLockOrUnlock.flightDateStart}" +
            "</when>" +
            "<when test='orderLockOrUnlock.businessScope==\"TI\" and orderLockOrUnlock.flightDateEnd!=null'>" +
            " and c.expect_arrival <![CDATA[<=]]> #{orderLockOrUnlock.flightDateEnd}" +
            "</when>" +
            "order by c.order_code desc" +
            "</script>")
    List<OrderLockOrUnlock> getListForTC(@Param("orderLockOrUnlock") OrderLockOrUnlock orderLockOrUnlock);

    @Select("<script>" +
            "SELECT c.order_id AS orderId,c.order_uuid AS orderUuid,c.business_scope AS businessScope,c.order_code AS orderCode,c.customer_number AS awbNumber,c.customer_number AS customerNumber,c.income_recorded AS incomeFinishStatus,c.cost_recorded AS costFinishStatus,c.shipping_method AS businessMethod,\n" +
            "IF(c.order_status='财务锁账',1,0) AS orderLockStatus,c.order_status AS orderStatus,c.income_status AS incomeStatus,c.cost_status AS costStatus,b.incomeAmount,b.costAmount,b.profitAmount,b.lockDate,'' AS flightNo,c.driving_time AS flightDate,'' AS businessProduct,\n" +
            "c.departure_station AS departureStation,c.arrival_station AS arrivalStation,d.coop_code AS coopCode,d.coop_name AS coopName,'' AS awbFromName,c.sales_name AS salesName,\n" +
            "c.servicer_name AS servicerName,c.goods_type AS goodsType,c.plan_pieces AS planPieces,c.plan_weight AS planWeight,c.plan_volume AS planVolume,c.plan_charge_weight AS planChargeWeight\n" +
            "FROM  lc_order c\n" +
            "LEFT JOIN (SELECT MAX(a.orderId) AS orderId,MAX(a.lockDate) AS lockDate,SUM(a.incomeAmount) AS incomeAmount,SUM(a.costAmount) AS costAmount,SUM(a.incomeAmount)- SUM(a.costAmount) AS profitAmount FROM\n" +
            "(SELECT income_functional_amount AS incomeAmount,0 AS costAmount,order_id AS orderId,financial_date as lockDate FROM lc_income WHERE org_id=#{orderLockOrUnlock.orgId}\n" +
            "UNION ALL\n" +
            "SELECT 0 AS incomeAmount,cost_functional_amount AS costAmount,order_id AS orderId,financial_date as lockDate FROM lc_cost WHERE org_id=#{orderLockOrUnlock.orgId}) a GROUP BY a.orderId,a.lockDate) b on c.order_id = b.orderId\n" +
            "LEFT JOIN prm_coop d ON d.coop_id = c.coop_id\n" +

            "WHERE c.org_id=#{orderLockOrUnlock.orgId} and c.order_status !='强制关闭'" +
            "<when test='orderLockOrUnlock.businessScope!=null and orderLockOrUnlock.businessScope!=\"\"'>" +
            " and c.business_scope=#{orderLockOrUnlock.businessScope}" +
            "</when>" +
            "<when test='orderLockOrUnlock.orderStatus!=null and orderLockOrUnlock.orderStatus!=\"\" and orderLockOrUnlock.orderStatus==\"是\"'>" +
            " and c.order_status='财务锁账'" +
            "</when>" +
            "<when test='orderLockOrUnlock.orderStatus!=null and orderLockOrUnlock.orderStatus!=\"\" and orderLockOrUnlock.orderStatus==\"否\"'>" +
            " and c.order_status!='财务锁账'" +
            "</when>" +
            "<when test='orderLockOrUnlock.incomeFinishStatus!=null and orderLockOrUnlock.incomeFinishStatus==true'>" +
            " and c.income_recorded=#{orderLockOrUnlock.incomeFinishStatus}" +
            "</when>" +
            "<when test='orderLockOrUnlock.incomeFinishStatus!=null and orderLockOrUnlock.incomeFinishStatus==false'>" +
            " and (c.income_recorded=#{orderLockOrUnlock.incomeFinishStatus} or c.income_recorded is null)" +
            "</when>" +
            "<when test='orderLockOrUnlock.costFinishStatus!=null and orderLockOrUnlock.costFinishStatus==true'>" +
            " and c.cost_recorded=#{orderLockOrUnlock.costFinishStatus}" +
            "</when>" +
            "<when test='orderLockOrUnlock.costFinishStatus!=null and orderLockOrUnlock.costFinishStatus==false'>" +
            " and (c.cost_recorded=#{orderLockOrUnlock.costFinishStatus} or c.cost_recorded is null)" +
            "</when>" +
            "<when test='orderLockOrUnlock.coopName!=null and orderLockOrUnlock.coopName!=\"\"'>" +
            " and (upper(d.coop_code) like \"%\"#{orderLockOrUnlock.coopName}\"%\" or upper(d.coop_name) like \"%\"#{orderLockOrUnlock.coopName}\"%\")" +
            "</when>" +
            "<when test='orderLockOrUnlock.orderCode!=null and orderLockOrUnlock.orderCode!=\"\"'>" +
            " and upper(c.order_code) like \"%\"#{orderLockOrUnlock.orderCode}\"%\"" +
            "</when>" +
            "<when test='orderLockOrUnlock.awbNumber!=null and orderLockOrUnlock.awbNumber!=\"\"'>" +
            " and c.customer_number like \"%\"#{orderLockOrUnlock.awbNumber}\"%\"" +
            "</when>" +

            "<when test='orderLockOrUnlock.flightDateStart!=null'>" +
            " and c.driving_time <![CDATA[>=]]> #{orderLockOrUnlock.flightDateStart}" +
            "</when>" +
            "<when test='orderLockOrUnlock.flightDateEnd!=null'>" +
            " and c.driving_time <![CDATA[<=]]> #{orderLockOrUnlock.flightDateEnd}" +
            "</when>" +
            "order by c.order_code desc" +
            "</script>")
    List<OrderLockOrUnlock> getListForLC(@Param("orderLockOrUnlock") OrderLockOrUnlock orderLockOrUnlock);

    @Select("<script>" +
            "SELECT c.order_id AS orderId,c.order_uuid AS orderUuid,c.business_scope AS businessScope,c.order_code AS orderCode,c.customer_number AS awbNumber,c.customer_number AS customerNumber,c.income_recorded AS incomeFinishStatus,c.cost_recorded AS costFinishStatus,c.business_method,\n" +
            "IF(c.order_status='财务锁账',1,0) AS orderLockStatus,c.order_status AS orderStatus,c.income_status AS incomeStatus,c.cost_status AS costStatus,b.incomeAmount,b.costAmount,b.profitAmount,b.lockDate,'' AS flightNo,c.business_date AS flightDate,'' AS businessProduct,\n" +
            "c.departure_station AS departureStation,c.arrival_station AS arrivalStation,d.coop_code AS coopCode,d.coop_name AS coopName,'' AS awbFromName,c.sales_name AS salesName,\n" +
            "c.servicer_name AS servicerName,c.goods_type AS goodsType,c.plan_pieces AS planPieces,c.plan_weight AS planWeight,c.plan_volume AS planVolume,c.plan_charge_weight AS planChargeWeight\n" +
            "FROM  io_order c\n" +
            "LEFT JOIN (SELECT MAX(a.orderId) AS orderId,MAX(a.lockDate) AS lockDate,SUM(a.incomeAmount) AS incomeAmount,SUM(a.costAmount) AS costAmount,SUM(a.incomeAmount)- SUM(a.costAmount) AS profitAmount FROM\n" +
            "(SELECT income_functional_amount AS incomeAmount,0 AS costAmount,order_id AS orderId,financial_date as lockDate FROM io_income WHERE org_id=#{orderLockOrUnlock.orgId}\n" +
            "UNION ALL\n" +
            "SELECT 0 AS incomeAmount,cost_functional_amount AS costAmount,order_id AS orderId,financial_date as lockDate FROM io_cost WHERE org_id=#{orderLockOrUnlock.orgId}) a GROUP BY a.orderId,a.lockDate) b on c.order_id = b.orderId\n" +
            "LEFT JOIN prm_coop d ON d.coop_id = c.coop_id\n" +

            "WHERE c.org_id=#{orderLockOrUnlock.orgId} and c.order_status !='强制关闭'" +
            "<when test='orderLockOrUnlock.businessScope!=null and orderLockOrUnlock.businessScope!=\"\"'>" +
            " and c.business_scope=#{orderLockOrUnlock.businessScope}" +
            "</when>" +
            "<when test='orderLockOrUnlock.orderStatus!=null and orderLockOrUnlock.orderStatus!=\"\" and orderLockOrUnlock.orderStatus==\"是\"'>" +
            " and c.order_status='财务锁账'" +
            "</when>" +
            "<when test='orderLockOrUnlock.orderStatus!=null and orderLockOrUnlock.orderStatus!=\"\" and orderLockOrUnlock.orderStatus==\"否\"'>" +
            " and c.order_status!='财务锁账'" +
            "</when>" +
            "<when test='orderLockOrUnlock.incomeFinishStatus!=null and orderLockOrUnlock.incomeFinishStatus==true'>" +
            " and c.income_recorded=#{orderLockOrUnlock.incomeFinishStatus}" +
            "</when>" +
            "<when test='orderLockOrUnlock.incomeFinishStatus!=null and orderLockOrUnlock.incomeFinishStatus==false'>" +
            " and (c.income_recorded=#{orderLockOrUnlock.incomeFinishStatus} or c.income_recorded is null)" +
            "</when>" +
            "<when test='orderLockOrUnlock.costFinishStatus!=null and orderLockOrUnlock.costFinishStatus==true'>" +
            " and c.cost_recorded=#{orderLockOrUnlock.costFinishStatus}" +
            "</when>" +
            "<when test='orderLockOrUnlock.costFinishStatus!=null and orderLockOrUnlock.costFinishStatus==false'>" +
            " and (c.cost_recorded=#{orderLockOrUnlock.costFinishStatus} or c.cost_recorded is null)" +
            "</when>" +
            "<when test='orderLockOrUnlock.coopName!=null and orderLockOrUnlock.coopName!=\"\"'>" +
            " and (upper(d.coop_code) like \"%\"#{orderLockOrUnlock.coopName}\"%\" or upper(d.coop_name) like \"%\"#{orderLockOrUnlock.coopName}\"%\")" +
            "</when>" +
            "<when test='orderLockOrUnlock.orderCode!=null and orderLockOrUnlock.orderCode!=\"\"'>" +
            " and upper(c.order_code) like \"%\"#{orderLockOrUnlock.orderCode}\"%\"" +
            "</when>" +
            "<when test='orderLockOrUnlock.awbNumber!=null and orderLockOrUnlock.awbNumber!=\"\"'>" +
            " and c.customer_number like \"%\"#{orderLockOrUnlock.awbNumber}\"%\"" +
            "</when>" +

            "<when test='orderLockOrUnlock.flightDateStart!=null'>" +
            " and c.business_date <![CDATA[>=]]> #{orderLockOrUnlock.flightDateStart}" +
            "</when>" +
            "<when test='orderLockOrUnlock.flightDateEnd!=null'>" +
            " and c.business_date <![CDATA[<=]]> #{orderLockOrUnlock.flightDateEnd}" +
            "</when>" +
            "order by c.order_code desc" +
            "</script>")
    List<OrderLockOrUnlock> getListForIO(@Param("orderLockOrUnlock") OrderLockOrUnlock orderLockOrUnlock);
}
