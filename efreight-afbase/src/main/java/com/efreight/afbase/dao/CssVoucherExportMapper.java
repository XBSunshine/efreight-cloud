package com.efreight.afbase.dao;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.efreight.afbase.entity.CssVoucherExport;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.ResultMap;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.mapping.StatementType;

import java.util.List;
import java.util.Map;

public interface CssVoucherExportMapper {


    @Select("<script>" +
            "SELECT c.order_id AS orderId,c.business_scope AS businessScope, c.order_code AS orderCode,CASE WHEN c.business_scope='AI' and (c.awb_number is null or c.awb_number = '') and c.hawb_number is not null and c.hawb_number != '' then c.hawb_number WHEN c.business_scope='AI' and c.awb_number is not null and c.awb_number != '' and c.hawb_number is not null and c.hawb_number != '' then  CONCAT(c.awb_number,'_',c.hawb_number) ELSE c.awb_number END AS awbNumber,c.customer_number AS customerNumber,d.coop_code,d.coop_name,\n" +
            "b.income_functional_amount,b.lockDate,b.voucher_date,b.voucher_number,b.voucher_creator_name,b.customerId,b.customerName\n" +
            "FROM af_order c\n" +
            "LEFT JOIN (SELECT order_id AS orderId,customer_id AS customerId,MAX(customer_name) as customerName,financial_date AS lockDate,SUM(income_functional_amount) AS income_functional_amount,voucher_date,voucher_number,MAX(voucher_creator_name) AS voucher_creator_name FROM af_income WHERE org_id=#{cssVoucherExport.orgId} GROUP BY order_id,financial_date,voucher_date,voucher_number,customer_id) b ON c.order_id = b.orderId\n" +
            "LEFT JOIN prm_coop d ON d.coop_id = c.coop_id\n" +
            "WHERE c.org_id=#{cssVoucherExport.orgId} and c.order_status !='强制关闭' and b.lockDate is not null" +
            "<when test='cssVoucherExport.businessScope!=null and cssVoucherExport.businessScope!=\"\"'>" +
            " and c.business_scope=#{cssVoucherExport.businessScope}" +
            "</when>" +
            "<when test='cssVoucherExport.coopName!=null and cssVoucherExport.coopName!=\"\"'>" +
            " and (d.coop_code like \"%\"#{cssVoucherExport.coopName}\"%\" or d.coop_name like \"%\"#{cssVoucherExport.coopName}\"%\")" +
            "</when>" +
            "<when test='cssVoucherExport.customerName!=null and cssVoucherExport.customerName!=\"\"'>" +
            " and (b.customerName like \"%\"#{cssVoucherExport.customerName}\"%\")" +
            "</when>" +
            "<when test='cssVoucherExport.voucherCreatorName!=null and cssVoucherExport.voucherCreatorName!=\"\"'>" +
            " and b.voucher_creator_name like \"%\"#{cssVoucherExport.voucherCreatorName}\"%\"" +
            "</when>" +
            "<when test='cssVoucherExport.awbNumber!=null and cssVoucherExport.awbNumber!=\"\" and cssVoucherExport.businessScope==\"AI\"'>" +
            " and (c.awb_number like \"%\"#{cssVoucherExport.awbNumber}\"%\" or c.hawb_number like \"%\"#{cssVoucherExport.awbNumber}\"%\" or c.order_code like \"%\"#{cssVoucherExport.awbNumber}\"%\")" +
            "</when>" +
            "<when test='cssVoucherExport.awbNumber!=null and cssVoucherExport.awbNumber!=\"\" and cssVoucherExport.businessScope==\"AE\"'>" +
            " and (c.awb_number like \"%\"#{cssVoucherExport.awbNumber}\"%\" or c.order_code like \"%\"#{cssVoucherExport.awbNumber}\"%\")" +
            "</when>" +
            "<when test='cssVoucherExport.voucherStatus!=null and cssVoucherExport.voucherStatus==true'>" +
            " and b.voucher_date is not null" +
            "</when>" +
            "<when test='cssVoucherExport.voucherStatus!=null and cssVoucherExport.voucherStatus==false'>" +
            " and b.voucher_date is null" +
            "</when>" +
            "<when test='cssVoucherExport.lockDateStart!=null'>" +
            " and b.lockDate <![CDATA[>=]]> #{cssVoucherExport.lockDateStart}" +
            "</when>" +
            "<when test='cssVoucherExport.lockDateEnd!=null'>" +
            " and b.lockDate <![CDATA[<=]]> #{cssVoucherExport.lockDateEnd}" +
            "</when>" +
            "<when test='cssVoucherExport.voucherDateStart!=null'>" +
            " and b.voucher_date <![CDATA[>=]]> #{cssVoucherExport.voucherDateStart}" +
            "</when>" +
            "<when test='cssVoucherExport.voucherDateEnd!=null'>" +
            " and b.voucher_date <![CDATA[<=]]> #{cssVoucherExport.voucherDateEnd}" +
            "</when>" +
            "order by c.order_code desc" +
            "</script>")
    IPage<CssVoucherExport> pageIncomeForAF(Page page, @Param("cssVoucherExport") CssVoucherExport cssVoucherExport);

    @Select("<script>" +
            "SELECT c.order_id AS orderId,c.business_scope AS businessScope,c.order_code AS orderCode,CASE WHEN c.business_scope='SI' and (c.mbl_number is null or c.mbl_number = '') and c.hbl_number is not null and c.hbl_number != '' then c.hbl_number WHEN c.business_scope='SI' and c.mbl_number is not null and c.mbl_number != '' and c.hbl_number is not null and c.hbl_number != '' then  CONCAT(c.mbl_number,'_',c.hbl_number) ELSE c.mbl_number END AS awbNumber,c.customer_number AS customerNumber,d.coop_code,d.coop_name,\n" +
            "b.income_functional_amount,b.lockDate,b.voucher_date,b.voucher_number,b.voucher_creator_name,b.customerId,b.customerName\n" +
            "FROM sc_order c\n" +
            "LEFT JOIN (SELECT order_id AS orderId,customer_id AS customerId,MAX(customer_name) as customerName,financial_date AS lockDate,SUM(income_functional_amount) AS income_functional_amount,voucher_date,voucher_number,MAX(voucher_creator_name) AS voucher_creator_name FROM sc_income WHERE org_id=#{cssVoucherExport.orgId} GROUP BY order_id,financial_date,voucher_date,voucher_number,customer_id) b ON c.order_id = b.orderId\n" +
            "LEFT JOIN prm_coop d ON d.coop_id = c.coop_id\n" +
            "WHERE c.org_id=#{cssVoucherExport.orgId} and c.order_status !='强制关闭' and b.lockDate is not null" +
            "<when test='cssVoucherExport.businessScope!=null and cssVoucherExport.businessScope!=\"\"'>" +
            " and c.business_scope=#{cssVoucherExport.businessScope}" +
            "</when>" +
            "<when test='cssVoucherExport.coopName!=null and cssVoucherExport.coopName!=\"\"'>" +
            " and (d.coop_code like \"%\"#{cssVoucherExport.coopName}\"%\" or d.coop_name like \"%\"#{cssVoucherExport.coopName}\"%\")" +
            "</when>" +
            "<when test='cssVoucherExport.customerName!=null and cssVoucherExport.customerName!=\"\"'>" +
            " and (b.customerName like \"%\"#{cssVoucherExport.customerName}\"%\")" +
            "</when>" +
            "<when test='cssVoucherExport.voucherCreatorName!=null and cssVoucherExport.voucherCreatorName!=\"\"'>" +
            " and b.voucher_creator_name like \"%\"#{cssVoucherExport.voucherCreatorName}\"%\"" +
            "</when>" +
            "<when test='cssVoucherExport.awbNumber!=null and cssVoucherExport.awbNumber!=\"\" and cssVoucherExport.businessScope==\"SI\"'>" +
            " and (c.mbl_number like \"%\"#{cssVoucherExport.awbNumber}\"%\" or c.hbl_number like \"%\"#{cssVoucherExport.awbNumber}\"%\" or c.order_code like \"%\"#{cssVoucherExport.awbNumber}\"%\")" +
            "</when>" +
            "<when test='cssVoucherExport.awbNumber!=null and cssVoucherExport.awbNumber!=\"\" and cssVoucherExport.businessScope==\"SE\"'>" +
            " and (c.mbl_number like \"%\"#{cssVoucherExport.awbNumber}\"%\" or c.order_code like \"%\"#{cssVoucherExport.awbNumber}\"%\")" +
            "</when>" +
            "<when test='cssVoucherExport.voucherStatus!=null and cssVoucherExport.voucherStatus==true'>" +
            " and b.voucher_date is not null" +
            "</when>" +
            "<when test='cssVoucherExport.voucherStatus!=null and cssVoucherExport.voucherStatus==false'>" +
            " and b.voucher_date is null" +
            "</when>" +
            "<when test='cssVoucherExport.lockDateStart!=null'>" +
            " and b.lockDate <![CDATA[>=]]> #{cssVoucherExport.lockDateStart}" +
            "</when>" +
            "<when test='cssVoucherExport.lockDateEnd!=null'>" +
            " and b.lockDate <![CDATA[<=]]> #{cssVoucherExport.lockDateEnd}" +
            "</when>" +
            "<when test='cssVoucherExport.voucherDateStart!=null'>" +
            " and b.voucher_date <![CDATA[>=]]> #{cssVoucherExport.voucherDateStart}" +
            "</when>" +
            "<when test='cssVoucherExport.voucherDateEnd!=null'>" +
            " and b.voucher_date <![CDATA[<=]]> #{cssVoucherExport.voucherDateEnd}" +
            "</when>" +
            "order by c.order_code desc" +
            "</script>")
    IPage<CssVoucherExport> pageIncomeForSC(Page page, @Param("cssVoucherExport") CssVoucherExport cssVoucherExport);

    @Select("<script>" +
            "SELECT c.order_id AS orderId,c.business_scope AS businessScope,c.order_code AS orderCode,c.rwb_number AS awbNumber,c.customer_number AS customerNumber,d.coop_code,d.coop_name,\n" +
            "b.income_functional_amount,b.lockDate,b.voucher_date,b.voucher_number,b.voucher_creator_name,b.customerId,b.customerName\n" +
            "FROM  tc_order c\n" +
            "LEFT JOIN (SELECT order_id AS orderId,customer_id AS customerId,MAX(customer_name) as customerName,financial_date AS lockDate,SUM(income_functional_amount) AS income_functional_amount,voucher_date,voucher_number,MAX(voucher_creator_name) AS voucher_creator_name FROM tc_income WHERE org_id=#{cssVoucherExport.orgId} GROUP BY order_id,financial_date,voucher_date,voucher_number,customer_id) b ON c.order_id = b.orderId\n" +
            "LEFT JOIN prm_coop d ON d.coop_id = c.coop_id\n" +
            "WHERE c.org_id=#{cssVoucherExport.orgId} and c.order_status !='强制关闭' and b.lockDate is not null" +
            "<when test='cssVoucherExport.businessScope!=null and cssVoucherExport.businessScope!=\"\"'>" +
            " and c.business_scope=#{cssVoucherExport.businessScope}" +
            "</when>" +
            "<when test='cssVoucherExport.coopName!=null and cssVoucherExport.coopName!=\"\"'>" +
            " and (d.coop_code like \"%\"#{cssVoucherExport.coopName}\"%\" or d.coop_name like \"%\"#{cssVoucherExport.coopName}\"%\")" +
            "</when>" +
            "<when test='cssVoucherExport.customerName!=null and cssVoucherExport.customerName!=\"\"'>" +
            " and (b.customerName like \"%\"#{cssVoucherExport.customerName}\"%\")" +
            "</when>" +
            "<when test='cssVoucherExport.voucherCreatorName!=null and cssVoucherExport.voucherCreatorName!=\"\"'>" +
            " and b.voucher_creator_name like \"%\"#{cssVoucherExport.voucherCreatorName}\"%\"" +
            "</when>" +
            "<when test='cssVoucherExport.awbNumber!=null and cssVoucherExport.awbNumber!=\"\"'>" +
            " and (c.customer_number like \"%\"#{cssVoucherExport.awbNumber}\"%\" or c.order_code like \"%\"#{cssVoucherExport.awbNumber}\"%\")" +
            "</when>" +
            "<when test='cssVoucherExport.voucherStatus!=null and cssVoucherExport.voucherStatus==true'>" +
            " and b.voucher_date is not null" +
            "</when>" +
            "<when test='cssVoucherExport.voucherStatus!=null and cssVoucherExport.voucherStatus==false'>" +
            " and b.voucher_date is null" +
            "</when>" +
            "<when test='cssVoucherExport.lockDateStart!=null'>" +
            " and b.lockDate <![CDATA[>=]]> #{cssVoucherExport.lockDateStart}" +
            "</when>" +
            "<when test='cssVoucherExport.lockDateEnd!=null'>" +
            " and b.lockDate <![CDATA[<=]]> #{cssVoucherExport.lockDateEnd}" +
            "</when>" +
            "<when test='cssVoucherExport.voucherDateStart!=null'>" +
            " and b.voucher_date <![CDATA[>=]]> #{cssVoucherExport.voucherDateStart}" +
            "</when>" +
            "<when test='cssVoucherExport.voucherDateEnd!=null'>" +
            " and b.voucher_date <![CDATA[<=]]> #{cssVoucherExport.voucherDateEnd}" +
            "</when>" +
            "order by c.order_code desc" +
            "</script>")
    IPage<CssVoucherExport> pageIncomeForTC(Page page, @Param("cssVoucherExport") CssVoucherExport cssVoucherExport);

    @Select("<script>" +
            "SELECT c.order_id AS orderId,c.business_scope AS businessScope,c.order_code AS orderCode,c.customer_number AS customerNumber,d.coop_code,d.coop_name,\n" +
            "b.income_functional_amount,b.lockDate,b.voucher_date,b.voucher_number,b.voucher_creator_name,b.customerId,b.customerName\n" +
            "FROM lc_order c\n" +
            "LEFT JOIN (SELECT order_id AS orderId,customer_id AS customerId,MAX(customer_name) as customerName,financial_date AS lockDate,SUM(income_functional_amount) AS income_functional_amount,voucher_date,voucher_number,MAX(voucher_creator_name) AS voucher_creator_name FROM lc_income WHERE org_id=#{cssVoucherExport.orgId} GROUP BY order_id,financial_date,voucher_date,voucher_number,customer_id) b ON c.order_id = b.orderId\n" +
            "LEFT JOIN prm_coop d ON d.coop_id = c.coop_id\n" +
            "WHERE c.org_id=#{cssVoucherExport.orgId} and c.order_status !='强制关闭' and b.lockDate is not null" +
            "<when test='cssVoucherExport.businessScope!=null and cssVoucherExport.businessScope!=\"\"'>" +
            " and c.business_scope=#{cssVoucherExport.businessScope}" +
            "</when>" +
            "<when test='cssVoucherExport.coopName!=null and cssVoucherExport.coopName!=\"\"'>" +
            " and (d.coop_code like \"%\"#{cssVoucherExport.coopName}\"%\" or d.coop_name like \"%\"#{cssVoucherExport.coopName}\"%\")" +
            "</when>" +
            "<when test='cssVoucherExport.customerName!=null and cssVoucherExport.customerName!=\"\"'>" +
            " and (b.customerName like \"%\"#{cssVoucherExport.customerName}\"%\")" +
            "</when>" +
            "<when test='cssVoucherExport.voucherCreatorName!=null and cssVoucherExport.voucherCreatorName!=\"\"'>" +
            " and b.voucher_creator_name like \"%\"#{cssVoucherExport.voucherCreatorName}\"%\"" +
            "</when>" +
            "<when test='cssVoucherExport.awbNumber!=null and cssVoucherExport.awbNumber!=\"\"'>" +
            " and (c.customer_number like \"%\"#{cssVoucherExport.awbNumber}\"%\" or c.order_code like \"%\"#{cssVoucherExport.awbNumber}\"%\")" +
            "</when>" +
            "<when test='cssVoucherExport.voucherStatus!=null and cssVoucherExport.voucherStatus==true'>" +
            " and b.voucher_date is not null" +
            "</when>" +
            "<when test='cssVoucherExport.voucherStatus!=null and cssVoucherExport.voucherStatus==false'>" +
            " and b.voucher_date is null" +
            "</when>" +
            "<when test='cssVoucherExport.lockDateStart!=null'>" +
            " and b.lockDate <![CDATA[>=]]> #{cssVoucherExport.lockDateStart}" +
            "</when>" +
            "<when test='cssVoucherExport.lockDateEnd!=null'>" +
            " and b.lockDate <![CDATA[<=]]> #{cssVoucherExport.lockDateEnd}" +
            "</when>" +
            "<when test='cssVoucherExport.voucherDateStart!=null'>" +
            " and b.voucher_date <![CDATA[>=]]> #{cssVoucherExport.voucherDateStart}" +
            "</when>" +
            "<when test='cssVoucherExport.voucherDateEnd!=null'>" +
            " and b.voucher_date <![CDATA[<=]]> #{cssVoucherExport.voucherDateEnd}" +
            "</when>" +
            "order by c.order_code desc" +
            "</script>")
    IPage<CssVoucherExport> pageIncomeForLC(Page page, @Param("cssVoucherExport") CssVoucherExport cssVoucherExport);

    @Select("<script>" +
            "SELECT c.order_id AS orderId,c.business_scope AS businessScope,c.order_code AS orderCode,c.customer_number AS customerNumber,d.coop_code,d.coop_name,\n" +
            "b.income_functional_amount,b.lockDate,b.voucher_date,b.voucher_number,b.voucher_creator_name,b.customerId,b.customerName\n" +
            "FROM  io_order c\n" +
            "LEFT JOIN (SELECT order_id AS orderId,customer_id AS customerId,MAX(customer_name) as customerName,financial_date AS lockDate,SUM(income_functional_amount) AS income_functional_amount,voucher_date,voucher_number,MAX(voucher_creator_name) AS voucher_creator_name FROM io_income WHERE org_id=#{cssVoucherExport.orgId} GROUP BY order_id,financial_date,voucher_date,voucher_number,customer_id) b ON c.order_id = b.orderId\n" +
            "LEFT JOIN prm_coop d ON d.coop_id = c.coop_id\n" +
            "WHERE c.org_id=#{cssVoucherExport.orgId} and c.order_status !='强制关闭' and b.lockDate is not null" +
            "<when test='cssVoucherExport.businessScope!=null and cssVoucherExport.businessScope!=\"\"'>" +
            " and c.business_scope=#{cssVoucherExport.businessScope}" +
            "</when>" +
            "<when test='cssVoucherExport.coopName!=null and cssVoucherExport.coopName!=\"\"'>" +
            " and (d.coop_code like \"%\"#{cssVoucherExport.coopName}\"%\" or d.coop_name like \"%\"#{cssVoucherExport.coopName}\"%\")" +
            "</when>" +
            "<when test='cssVoucherExport.customerName!=null and cssVoucherExport.customerName!=\"\"'>" +
            " and (b.customerName like \"%\"#{cssVoucherExport.customerName}\"%\")" +
            "</when>" +
            "<when test='cssVoucherExport.voucherCreatorName!=null and cssVoucherExport.voucherCreatorName!=\"\"'>" +
            " and b.voucher_creator_name like \"%\"#{cssVoucherExport.voucherCreatorName}\"%\"" +
            "</when>" +
            "<when test='cssVoucherExport.awbNumber!=null and cssVoucherExport.awbNumber!=\"\"'>" +
            " and (c.customer_number like \"%\"#{cssVoucherExport.awbNumber}\"%\" or c.order_code like \"%\"#{cssVoucherExport.awbNumber}\"%\")" +
            "</when>" +
            "<when test='cssVoucherExport.voucherStatus!=null and cssVoucherExport.voucherStatus==true'>" +
            " and b.voucher_date is not null" +
            "</when>" +
            "<when test='cssVoucherExport.voucherStatus!=null and cssVoucherExport.voucherStatus==false'>" +
            " and b.voucher_date is null" +
            "</when>" +
            "<when test='cssVoucherExport.lockDateStart!=null'>" +
            " and b.lockDate <![CDATA[>=]]> #{cssVoucherExport.lockDateStart}" +
            "</when>" +
            "<when test='cssVoucherExport.lockDateEnd!=null'>" +
            " and b.lockDate <![CDATA[<=]]> #{cssVoucherExport.lockDateEnd}" +
            "</when>" +
            "<when test='cssVoucherExport.voucherDateStart!=null'>" +
            " and b.voucher_date <![CDATA[>=]]> #{cssVoucherExport.voucherDateStart}" +
            "</when>" +
            "<when test='cssVoucherExport.voucherDateEnd!=null'>" +
            " and b.voucher_date <![CDATA[<=]]> #{cssVoucherExport.voucherDateEnd}" +
            "</when>" +
            "order by c.order_code desc" +
            "</script>")
    IPage<CssVoucherExport> pageIncomeForIO(Page page, @Param("cssVoucherExport") CssVoucherExport cssVoucherExport);

    @Select("<script>" +
            "SELECT c.order_id AS orderId,c.business_scope AS businessScope, c.order_code AS orderCode,CASE WHEN c.business_scope='AI' and (c.awb_number is null or c.awb_number = '') and c.hawb_number is not null and c.hawb_number != '' then c.hawb_number WHEN c.business_scope='AI' and c.awb_number is not null and c.awb_number != '' and c.hawb_number is not null and c.hawb_number != '' then  CONCAT(c.awb_number,'_',c.hawb_number) ELSE c.awb_number END AS awbNumber,c.customer_number AS customerNumber,d.coop_code,d.coop_name,\n" +
            "b.cost_functional_amount,b.lockDate,b.voucher_date,b.voucher_number,b.voucher_creator_name,b.customerId,b.customerName\n" +
            "FROM af_order c\n" +
            "LEFT JOIN (SELECT order_id AS orderId,customer_id AS customerId,MAX(customer_name) as customerName,financial_date AS lockDate,SUM(cost_functional_amount) AS cost_functional_amount,voucher_date,voucher_number,MAX(voucher_creator_name) AS voucher_creator_name FROM af_cost WHERE org_id=#{cssVoucherExport.orgId} GROUP BY order_id,financial_date,voucher_date,voucher_number,customer_id) b ON c.order_id = b.orderId\n" +
            "LEFT JOIN prm_coop d ON d.coop_id = c.coop_id\n" +
            "WHERE c.org_id=#{cssVoucherExport.orgId} and c.order_status !='强制关闭' and b.lockDate is not null" +
            "<when test='cssVoucherExport.businessScope!=null and cssVoucherExport.businessScope!=\"\"'>" +
            " and c.business_scope=#{cssVoucherExport.businessScope}" +
            "</when>" +
            "<when test='cssVoucherExport.coopName!=null and cssVoucherExport.coopName!=\"\"'>" +
            " and (d.coop_code like \"%\"#{cssVoucherExport.coopName}\"%\" or d.coop_name like \"%\"#{cssVoucherExport.coopName}\"%\")" +
            "</when>" +
            "<when test='cssVoucherExport.customerName!=null and cssVoucherExport.customerName!=\"\"'>" +
            " and (b.customerName like \"%\"#{cssVoucherExport.customerName}\"%\")" +
            "</when>" +
            "<when test='cssVoucherExport.voucherCreatorName!=null and cssVoucherExport.voucherCreatorName!=\"\"'>" +
            " and b.voucher_creator_name like \"%\"#{cssVoucherExport.voucherCreatorName}\"%\"" +
            "</when>" +
            "<when test='cssVoucherExport.awbNumber!=null and cssVoucherExport.awbNumber!=\"\" and cssVoucherExport.businessScope==\"AI\"'>" +
            " and (c.awb_number like \"%\"#{cssVoucherExport.awbNumber}\"%\" or c.hawb_number like \"%\"#{cssVoucherExport.awbNumber}\"%\" or c.order_code like \"%\"#{cssVoucherExport.awbNumber}\"%\")" +
            "</when>" +
            "<when test='cssVoucherExport.awbNumber!=null and cssVoucherExport.awbNumber!=\"\" and cssVoucherExport.businessScope==\"AE\"'>" +
            " and (c.awb_number like \"%\"#{cssVoucherExport.awbNumber}\"%\" or c.order_code like \"%\"#{cssVoucherExport.awbNumber}\"%\")" +
            "</when>" +
            "<when test='cssVoucherExport.voucherStatus!=null and cssVoucherExport.voucherStatus==true'>" +
            " and b.voucher_date is not null" +
            "</when>" +
            "<when test='cssVoucherExport.voucherStatus!=null and cssVoucherExport.voucherStatus==false'>" +
            " and b.voucher_date is null" +
            "</when>" +
            "<when test='cssVoucherExport.lockDateStart!=null'>" +
            " and b.lockDate <![CDATA[>=]]> #{cssVoucherExport.lockDateStart}" +
            "</when>" +
            "<when test='cssVoucherExport.lockDateEnd!=null'>" +
            " and b.lockDate <![CDATA[<=]]> #{cssVoucherExport.lockDateEnd}" +
            "</when>" +
            "<when test='cssVoucherExport.voucherDateStart!=null'>" +
            " and b.voucher_date <![CDATA[>=]]> #{cssVoucherExport.voucherDateStart}" +
            "</when>" +
            "<when test='cssVoucherExport.voucherDateEnd!=null'>" +
            " and b.voucher_date <![CDATA[<=]]> #{cssVoucherExport.voucherDateEnd}" +
            "</when>" +
            "order by c.order_code desc" +
            "</script>")
    IPage<CssVoucherExport> pageCostForAF(Page page, @Param("cssVoucherExport") CssVoucherExport cssVoucherExport);

    @Select("<script>" +
            "SELECT c.order_id AS orderId,c.business_scope AS businessScope,c.order_code AS orderCode,CASE WHEN c.business_scope='SI' and (c.mbl_number is null or c.mbl_number = '') and c.hbl_number is not null and c.hbl_number != '' then c.hbl_number WHEN c.business_scope='SI' and c.mbl_number is not null and c.mbl_number != '' and c.hbl_number is not null and c.hbl_number != '' then  CONCAT(c.mbl_number,'_',c.hbl_number) ELSE c.mbl_number END AS awbNumber,c.customer_number AS customerNumber,d.coop_code,d.coop_name,\n" +
            "b.cost_functional_amount,b.lockDate,b.voucher_date,b.voucher_number,b.voucher_creator_name,b.customerId,b.customerName\n" +
            "FROM sc_order c\n" +
            "LEFT JOIN (SELECT order_id AS orderId,customer_id AS customerId,MAX(customer_name) as customerName,financial_date AS lockDate,SUM(cost_functional_amount) AS cost_functional_amount,voucher_date,voucher_number,MAX(voucher_creator_name) AS voucher_creator_name FROM sc_cost WHERE org_id=#{cssVoucherExport.orgId} GROUP BY order_id,financial_date,voucher_date,voucher_number,customer_id) b ON c.order_id = b.orderId\n" +
            "LEFT JOIN prm_coop d ON d.coop_id = c.coop_id\n" +
            "WHERE c.org_id=#{cssVoucherExport.orgId} and c.order_status !='强制关闭' and b.lockDate is not null" +
            "<when test='cssVoucherExport.businessScope!=null and cssVoucherExport.businessScope!=\"\"'>" +
            " and c.business_scope=#{cssVoucherExport.businessScope}" +
            "</when>" +
            "<when test='cssVoucherExport.coopName!=null and cssVoucherExport.coopName!=\"\"'>" +
            " and (d.coop_code like \"%\"#{cssVoucherExport.coopName}\"%\" or d.coop_name like \"%\"#{cssVoucherExport.coopName}\"%\")" +
            "</when>" +
            "<when test='cssVoucherExport.customerName!=null and cssVoucherExport.customerName!=\"\"'>" +
            " and (b.customerName like \"%\"#{cssVoucherExport.customerName}\"%\")" +
            "</when>" +
            "<when test='cssVoucherExport.voucherCreatorName!=null and cssVoucherExport.voucherCreatorName!=\"\"'>" +
            " and b.voucher_creator_name like \"%\"#{cssVoucherExport.voucherCreatorName}\"%\"" +
            "</when>" +
            "<when test='cssVoucherExport.awbNumber!=null and cssVoucherExport.awbNumber!=\"\" and cssVoucherExport.businessScope==\"SI\"'>" +
            " and (c.mbl_number like \"%\"#{cssVoucherExport.awbNumber}\"%\" or c.hbl_number like \"%\"#{cssVoucherExport.awbNumber}\"%\" or c.order_code like \"%\"#{cssVoucherExport.awbNumber}\"%\")" +
            "</when>" +
            "<when test='cssVoucherExport.awbNumber!=null and cssVoucherExport.awbNumber!=\"\" and cssVoucherExport.businessScope==\"SE\"'>" +
            " and (c.mbl_number like \"%\"#{cssVoucherExport.awbNumber}\"%\" or c.order_code like \"%\"#{cssVoucherExport.awbNumber}\"%\")" +
            "</when>" +
            "<when test='cssVoucherExport.voucherStatus!=null and cssVoucherExport.voucherStatus==true'>" +
            " and b.voucher_date is not null" +
            "</when>" +
            "<when test='cssVoucherExport.voucherStatus!=null and cssVoucherExport.voucherStatus==false'>" +
            " and b.voucher_date is null" +
            "</when>" +
            "<when test='cssVoucherExport.lockDateStart!=null'>" +
            " and b.lockDate <![CDATA[>=]]> #{cssVoucherExport.lockDateStart}" +
            "</when>" +
            "<when test='cssVoucherExport.lockDateEnd!=null'>" +
            " and b.lockDate <![CDATA[<=]]> #{cssVoucherExport.lockDateEnd}" +
            "</when>" +
            "<when test='cssVoucherExport.voucherDateStart!=null'>" +
            " and b.voucher_date <![CDATA[>=]]> #{cssVoucherExport.voucherDateStart}" +
            "</when>" +
            "<when test='cssVoucherExport.voucherDateEnd!=null'>" +
            " and b.voucher_date <![CDATA[<=]]> #{cssVoucherExport.voucherDateEnd}" +
            "</when>" +
            "order by c.order_code desc" +
            "</script>")
    IPage<CssVoucherExport> pageCostForSC(Page page, @Param("cssVoucherExport") CssVoucherExport cssVoucherExport);

    @Select("<script>" +
            "SELECT c.order_id AS orderId,c.business_scope AS businessScope,c.order_code AS orderCode,c.rwb_number AS awbNumber,c.customer_number AS customerNumber,d.coop_code,d.coop_name,\n" +
            "b.cost_functional_amount,b.lockDate,b.voucher_date,b.voucher_number,b.voucher_creator_name,b.customerId,b.customerName\n" +
            "FROM  tc_order c\n" +
            "LEFT JOIN (SELECT order_id AS orderId,customer_id AS customerId,MAX(customer_name) as customerName,financial_date AS lockDate,SUM(cost_functional_amount) AS cost_functional_amount,voucher_date,voucher_number,MAX(voucher_creator_name) AS voucher_creator_name FROM tc_cost WHERE org_id=#{cssVoucherExport.orgId} GROUP BY order_id,financial_date,voucher_date,voucher_number,customer_id) b ON c.order_id = b.orderId\n" +
            "LEFT JOIN prm_coop d ON d.coop_id = c.coop_id\n" +
            "WHERE c.org_id=#{cssVoucherExport.orgId} and c.order_status !='强制关闭' and b.lockDate is not null" +
            "<when test='cssVoucherExport.businessScope!=null and cssVoucherExport.businessScope!=\"\"'>" +
            " and c.business_scope=#{cssVoucherExport.businessScope}" +
            "</when>" +
            "<when test='cssVoucherExport.coopName!=null and cssVoucherExport.coopName!=\"\"'>" +
            " and (d.coop_code like \"%\"#{cssVoucherExport.coopName}\"%\" or d.coop_name like \"%\"#{cssVoucherExport.coopName}\"%\")" +
            "</when>" +
            "<when test='cssVoucherExport.customerName!=null and cssVoucherExport.customerName!=\"\"'>" +
            " and (b.customerName like \"%\"#{cssVoucherExport.customerName}\"%\")" +
            "</when>" +
            "<when test='cssVoucherExport.voucherCreatorName!=null and cssVoucherExport.voucherCreatorName!=\"\"'>" +
            " and b.voucher_creator_name like \"%\"#{cssVoucherExport.voucherCreatorName}\"%\"" +
            "</when>" +
            "<when test='cssVoucherExport.awbNumber!=null and cssVoucherExport.awbNumber!=\"\"'>" +
            " and (c.customer_number like \"%\"#{cssVoucherExport.awbNumber}\"%\" or c.order_code like \"%\"#{cssVoucherExport.awbNumber}\"%\")" +
            "</when>" +
            "<when test='cssVoucherExport.voucherStatus!=null and cssVoucherExport.voucherStatus==true'>" +
            " and b.voucher_date is not null" +
            "</when>" +
            "<when test='cssVoucherExport.voucherStatus!=null and cssVoucherExport.voucherStatus==false'>" +
            " and b.voucher_date is null" +
            "</when>" +
            "<when test='cssVoucherExport.lockDateStart!=null'>" +
            " and b.lockDate <![CDATA[>=]]> #{cssVoucherExport.lockDateStart}" +
            "</when>" +
            "<when test='cssVoucherExport.lockDateEnd!=null'>" +
            " and b.lockDate <![CDATA[<=]]> #{cssVoucherExport.lockDateEnd}" +
            "</when>" +
            "<when test='cssVoucherExport.voucherDateStart!=null'>" +
            " and b.voucher_date <![CDATA[>=]]> #{cssVoucherExport.voucherDateStart}" +
            "</when>" +
            "<when test='cssVoucherExport.voucherDateEnd!=null'>" +
            " and b.voucher_date <![CDATA[<=]]> #{cssVoucherExport.voucherDateEnd}" +
            "</when>" +
            "order by c.order_code desc" +
            "</script>")
    IPage<CssVoucherExport> pageCostForTC(Page page, @Param("cssVoucherExport") CssVoucherExport cssVoucherExport);

    @Select("<script>" +
            "SELECT c.order_id AS orderId,c.business_scope AS businessScope,c.order_code AS orderCode,c.customer_number AS customerNumber,d.coop_code,d.coop_name,\n" +
            "b.cost_functional_amount,b.lockDate,b.voucher_date,b.voucher_number,b.voucher_creator_name,b.customerId,b.customerName\n" +
            "FROM lc_order c\n" +
            "LEFT JOIN (SELECT order_id AS orderId,customer_id AS customerId,MAX(customer_name) as customerName,financial_date AS lockDate,SUM(cost_functional_amount) AS cost_functional_amount,voucher_date,voucher_number,MAX(voucher_creator_name) AS voucher_creator_name FROM lc_cost WHERE org_id=#{cssVoucherExport.orgId} GROUP BY order_id,financial_date,voucher_date,voucher_number,customer_id) b ON c.order_id = b.orderId\n" +
            "LEFT JOIN prm_coop d ON d.coop_id = c.coop_id\n" +
            "WHERE c.org_id=#{cssVoucherExport.orgId} and c.order_status !='强制关闭' and b.lockDate is not null" +
            "<when test='cssVoucherExport.businessScope!=null and cssVoucherExport.businessScope!=\"\"'>" +
            " and c.business_scope=#{cssVoucherExport.businessScope}" +
            "</when>" +
            "<when test='cssVoucherExport.coopName!=null and cssVoucherExport.coopName!=\"\"'>" +
            " and (d.coop_code like \"%\"#{cssVoucherExport.coopName}\"%\" or d.coop_name like \"%\"#{cssVoucherExport.coopName}\"%\")" +
            "</when>" +
            "<when test='cssVoucherExport.customerName!=null and cssVoucherExport.customerName!=\"\"'>" +
            " and (b.customerName like \"%\"#{cssVoucherExport.customerName}\"%\")" +
            "</when>" +
            "<when test='cssVoucherExport.voucherCreatorName!=null and cssVoucherExport.voucherCreatorName!=\"\"'>" +
            " and b.voucher_creator_name like \"%\"#{cssVoucherExport.voucherCreatorName}\"%\"" +
            "</when>" +
            "<when test='cssVoucherExport.awbNumber!=null and cssVoucherExport.awbNumber!=\"\"'>" +
            " and (c.customer_number like \"%\"#{cssVoucherExport.awbNumber}\"%\" or c.order_code like \"%\"#{cssVoucherExport.awbNumber}\"%\")" +
            "</when>" +
            "<when test='cssVoucherExport.voucherStatus!=null and cssVoucherExport.voucherStatus==true'>" +
            " and b.voucher_date is not null" +
            "</when>" +
            "<when test='cssVoucherExport.voucherStatus!=null and cssVoucherExport.voucherStatus==false'>" +
            " and b.voucher_date is null" +
            "</when>" +
            "<when test='cssVoucherExport.lockDateStart!=null'>" +
            " and b.lockDate <![CDATA[>=]]> #{cssVoucherExport.lockDateStart}" +
            "</when>" +
            "<when test='cssVoucherExport.lockDateEnd!=null'>" +
            " and b.lockDate <![CDATA[<=]]> #{cssVoucherExport.lockDateEnd}" +
            "</when>" +
            "<when test='cssVoucherExport.voucherDateStart!=null'>" +
            " and b.voucher_date <![CDATA[>=]]> #{cssVoucherExport.voucherDateStart}" +
            "</when>" +
            "<when test='cssVoucherExport.voucherDateEnd!=null'>" +
            " and b.voucher_date <![CDATA[<=]]> #{cssVoucherExport.voucherDateEnd}" +
            "</when>" +
            "order by c.order_code desc" +
            "</script>")
    IPage<CssVoucherExport> pageCostForLC(Page page, @Param("cssVoucherExport") CssVoucherExport cssVoucherExport);

    @Select("<script>" +
            "SELECT c.order_id AS orderId,c.business_scope AS businessScope,c.order_code AS orderCode,c.customer_number AS customerNumber,d.coop_code,d.coop_name,\n" +
            "b.cost_functional_amount,b.lockDate,b.voucher_date,b.voucher_number,b.voucher_creator_name,b.customerId,b.customerName\n" +
            "FROM  io_order c\n" +
            "LEFT JOIN (SELECT order_id AS orderId,customer_id AS customerId,MAX(customer_name) as customerName,financial_date AS lockDate,SUM(cost_functional_amount) AS cost_functional_amount,voucher_date,voucher_number,MAX(voucher_creator_name) AS voucher_creator_name FROM io_cost WHERE org_id=#{cssVoucherExport.orgId} GROUP BY order_id,financial_date,voucher_date,voucher_number,customer_id) b ON c.order_id = b.orderId\n" +
            "LEFT JOIN prm_coop d ON d.coop_id = c.coop_id\n" +
            "WHERE c.org_id=#{cssVoucherExport.orgId} and c.order_status !='强制关闭' and b.lockDate is not null" +
            "<when test='cssVoucherExport.businessScope!=null and cssVoucherExport.businessScope!=\"\"'>" +
            " and c.business_scope=#{cssVoucherExport.businessScope}" +
            "</when>" +
            "<when test='cssVoucherExport.coopName!=null and cssVoucherExport.coopName!=\"\"'>" +
            " and (d.coop_code like \"%\"#{cssVoucherExport.coopName}\"%\" or d.coop_name like \"%\"#{cssVoucherExport.coopName}\"%\")" +
            "</when>" +
            "<when test='cssVoucherExport.customerName!=null and cssVoucherExport.customerName!=\"\"'>" +
            " and (b.customerName like \"%\"#{cssVoucherExport.customerName}\"%\")" +
            "</when>" +
            "<when test='cssVoucherExport.voucherCreatorName!=null and cssVoucherExport.voucherCreatorName!=\"\"'>" +
            " and b.voucher_creator_name like \"%\"#{cssVoucherExport.voucherCreatorName}\"%\"" +
            "</when>" +
            "<when test='cssVoucherExport.awbNumber!=null and cssVoucherExport.awbNumber!=\"\"'>" +
            " and (c.customer_number like \"%\"#{cssVoucherExport.awbNumber}\"%\" or c.order_code like \"%\"#{cssVoucherExport.awbNumber}\"%\")" +
            "</when>" +
            "<when test='cssVoucherExport.voucherStatus!=null and cssVoucherExport.voucherStatus==true'>" +
            " and b.voucher_date is not null" +
            "</when>" +
            "<when test='cssVoucherExport.voucherStatus!=null and cssVoucherExport.voucherStatus==false'>" +
            " and b.voucher_date is null" +
            "</when>" +
            "<when test='cssVoucherExport.lockDateStart!=null'>" +
            " and b.lockDate <![CDATA[>=]]> #{cssVoucherExport.lockDateStart}" +
            "</when>" +
            "<when test='cssVoucherExport.lockDateEnd!=null'>" +
            " and b.lockDate <![CDATA[<=]]> #{cssVoucherExport.lockDateEnd}" +
            "</when>" +
            "<when test='cssVoucherExport.voucherDateStart!=null'>" +
            " and b.voucher_date <![CDATA[>=]]> #{cssVoucherExport.voucherDateStart}" +
            "</when>" +
            "<when test='cssVoucherExport.voucherDateEnd!=null'>" +
            " and b.voucher_date <![CDATA[<=]]> #{cssVoucherExport.voucherDateEnd}" +
            "</when>" +
            "order by c.order_code desc" +
            "</script>")
    IPage<CssVoucherExport> pageCostForIO(Page page, @Param("cssVoucherExport") CssVoucherExport cssVoucherExport);

    @Select("<script>" +
            "SELECT c.order_id AS orderId,c.business_scope AS businessScope, c.order_code AS orderCode,CASE WHEN c.business_scope='AI' and (c.awb_number is null or c.awb_number = '') and c.hawb_number is not null and c.hawb_number != '' then c.hawb_number WHEN c.business_scope='AI' and c.awb_number is not null and c.awb_number != '' and c.hawb_number is not null and c.hawb_number != '' then  CONCAT(c.awb_number,'_',c.hawb_number) ELSE c.awb_number END AS awbNumber,c.customer_number AS customerNumber,d.coop_code,d.coop_name,\n" +
            "b.income_functional_amount,b.lockDate,b.voucher_date,b.voucher_number,b.voucher_creator_name,b.customerId,b.customerName\n" +
            "FROM af_order c\n" +
            "LEFT JOIN (SELECT order_id AS orderId,customer_id AS customerId,MAX(customer_name) as customerName,financial_date AS lockDate,SUM(income_functional_amount) AS income_functional_amount,voucher_date,voucher_number,MAX(voucher_creator_name) AS voucher_creator_name FROM af_income WHERE org_id=#{cssVoucherExport.orgId} GROUP BY order_id,financial_date,voucher_date,voucher_number,customer_id) b ON c.order_id = b.orderId\n" +
            "LEFT JOIN prm_coop d ON d.coop_id = c.coop_id\n" +
            "WHERE c.org_id=#{cssVoucherExport.orgId} and c.order_status !='强制关闭' and b.lockDate is not null" +
            "<when test='cssVoucherExport.businessScope!=null and cssVoucherExport.businessScope!=\"\"'>" +
            " and c.business_scope=#{cssVoucherExport.businessScope}" +
            "</when>" +
            "<when test='cssVoucherExport.coopName!=null and cssVoucherExport.coopName!=\"\"'>" +
            " and (d.coop_code like \"%\"#{cssVoucherExport.coopName}\"%\" or d.coop_name like \"%\"#{cssVoucherExport.coopName}\"%\")" +
            "</when>" +
            "<when test='cssVoucherExport.customerName!=null and cssVoucherExport.customerName!=\"\"'>" +
            " and (b.customerName like \"%\"#{cssVoucherExport.customerName}\"%\")" +
            "</when>" +
            "<when test='cssVoucherExport.voucherCreatorName!=null and cssVoucherExport.voucherCreatorName!=\"\"'>" +
            " and b.voucher_creator_name like \"%\"#{cssVoucherExport.voucherCreatorName}\"%\"" +
            "</when>" +
            "<when test='cssVoucherExport.awbNumber!=null and cssVoucherExport.awbNumber!=\"\" and cssVoucherExport.businessScope==\"AI\"'>" +
            " and (c.awb_number like \"%\"#{cssVoucherExport.awbNumber}\"%\" or c.hawb_number like \"%\"#{cssVoucherExport.awbNumber}\"%\" or c.order_code like \"%\"#{cssVoucherExport.awbNumber}\"%\")" +
            "</when>" +
            "<when test='cssVoucherExport.awbNumber!=null and cssVoucherExport.awbNumber!=\"\" and cssVoucherExport.businessScope==\"AE\"'>" +
            " and (c.awb_number like \"%\"#{cssVoucherExport.awbNumber}\"%\" or c.order_code like \"%\"#{cssVoucherExport.awbNumber}\"%\")" +
            "</when>" +
            "<when test='cssVoucherExport.voucherStatus!=null and cssVoucherExport.voucherStatus==true'>" +
            " and b.voucher_date is not null" +
            "</when>" +
            "<when test='cssVoucherExport.voucherStatus!=null and cssVoucherExport.voucherStatus==false'>" +
            " and b.voucher_date is null" +
            "</when>" +
            "<when test='cssVoucherExport.lockDateStart!=null'>" +
            " and b.lockDate <![CDATA[>=]]> #{cssVoucherExport.lockDateStart}" +
            "</when>" +
            "<when test='cssVoucherExport.lockDateEnd!=null'>" +
            " and b.lockDate <![CDATA[<=]]> #{cssVoucherExport.lockDateEnd}" +
            "</when>" +
            "<when test='cssVoucherExport.voucherDateStart!=null'>" +
            " and b.voucher_date <![CDATA[>=]]> #{cssVoucherExport.voucherDateStart}" +
            "</when>" +
            "<when test='cssVoucherExport.voucherDateEnd!=null'>" +
            " and b.voucher_date <![CDATA[<=]]> #{cssVoucherExport.voucherDateEnd}" +
            "</when>" +
            "order by c.order_code desc" +
            "</script>")
    List<CssVoucherExport> listIncomeForAF(@Param("cssVoucherExport") CssVoucherExport cssVoucherExport);

    @Select("<script>" +
            "SELECT c.order_id AS orderId,c.business_scope AS businessScope,c.order_code AS orderCode,CASE WHEN c.business_scope='SI' and (c.mbl_number is null or c.mbl_number = '') and c.hbl_number is not null and c.hbl_number != '' then c.hbl_number WHEN c.business_scope='SI' and c.mbl_number is not null and c.mbl_number != '' and c.hbl_number is not null and c.hbl_number != '' then  CONCAT(c.mbl_number,'_',c.hbl_number) ELSE c.mbl_number END AS awbNumber,c.customer_number AS customerNumber,d.coop_code,d.coop_name,\n" +
            "b.income_functional_amount,b.lockDate,b.voucher_date,b.voucher_number,b.voucher_creator_name,b.customerId,b.customerName\n" +
            "FROM sc_order c\n" +
            "LEFT JOIN (SELECT order_id AS orderId,customer_id AS customerId,MAX(customer_name) as customerName,financial_date AS lockDate,SUM(income_functional_amount) AS income_functional_amount,voucher_date,voucher_number,MAX(voucher_creator_name) AS voucher_creator_name FROM sc_income WHERE org_id=#{cssVoucherExport.orgId} GROUP BY order_id,financial_date,voucher_date,voucher_number,customer_id) b ON c.order_id = b.orderId\n" +
            "LEFT JOIN prm_coop d ON d.coop_id = c.coop_id\n" +
            "WHERE c.org_id=#{cssVoucherExport.orgId} and c.order_status !='强制关闭' and b.lockDate is not null" +
            "<when test='cssVoucherExport.businessScope!=null and cssVoucherExport.businessScope!=\"\"'>" +
            " and c.business_scope=#{cssVoucherExport.businessScope}" +
            "</when>" +
            "<when test='cssVoucherExport.coopName!=null and cssVoucherExport.coopName!=\"\"'>" +
            " and (d.coop_code like \"%\"#{cssVoucherExport.coopName}\"%\" or d.coop_name like \"%\"#{cssVoucherExport.coopName}\"%\")" +
            "</when>" +
            "<when test='cssVoucherExport.customerName!=null and cssVoucherExport.customerName!=\"\"'>" +
            " and (b.customerName like \"%\"#{cssVoucherExport.customerName}\"%\")" +
            "</when>" +
            "<when test='cssVoucherExport.voucherCreatorName!=null and cssVoucherExport.voucherCreatorName!=\"\"'>" +
            " and b.voucher_creator_name like \"%\"#{cssVoucherExport.voucherCreatorName}\"%\"" +
            "</when>" +
            "<when test='cssVoucherExport.awbNumber!=null and cssVoucherExport.awbNumber!=\"\" and cssVoucherExport.businessScope==\"SI\"'>" +
            " and (c.mbl_number like \"%\"#{cssVoucherExport.awbNumber}\"%\" or c.hbl_number like \"%\"#{cssVoucherExport.awbNumber}\"%\" or c.order_code like \"%\"#{cssVoucherExport.awbNumber}\"%\")" +
            "</when>" +
            "<when test='cssVoucherExport.awbNumber!=null and cssVoucherExport.awbNumber!=\"\" and cssVoucherExport.businessScope==\"SE\"'>" +
            " and (c.mbl_number like \"%\"#{cssVoucherExport.awbNumber}\"%\" or c.order_code like \"%\"#{cssVoucherExport.awbNumber}\"%\")" +
            "</when>" +
            "<when test='cssVoucherExport.voucherStatus!=null and cssVoucherExport.voucherStatus==true'>" +
            " and b.voucher_date is not null" +
            "</when>" +
            "<when test='cssVoucherExport.voucherStatus!=null and cssVoucherExport.voucherStatus==false'>" +
            " and b.voucher_date is null" +
            "</when>" +
            "<when test='cssVoucherExport.lockDateStart!=null'>" +
            " and b.lockDate <![CDATA[>=]]> #{cssVoucherExport.lockDateStart}" +
            "</when>" +
            "<when test='cssVoucherExport.lockDateEnd!=null'>" +
            " and b.lockDate <![CDATA[<=]]> #{cssVoucherExport.lockDateEnd}" +
            "</when>" +
            "<when test='cssVoucherExport.voucherDateStart!=null'>" +
            " and b.voucher_date <![CDATA[>=]]> #{cssVoucherExport.voucherDateStart}" +
            "</when>" +
            "<when test='cssVoucherExport.voucherDateEnd!=null'>" +
            " and b.voucher_date <![CDATA[<=]]> #{cssVoucherExport.voucherDateEnd}" +
            "</when>" +
            "order by c.order_code desc" +
            "</script>")
    List<CssVoucherExport> listIncomeForSC(@Param("cssVoucherExport") CssVoucherExport cssVoucherExport);

    @Select("<script>" +
            "SELECT c.order_id AS orderId,c.business_scope AS businessScope,c.order_code AS orderCode,c.rwb_number AS awbNumber,c.customer_number AS customerNumber,d.coop_code,d.coop_name,\n" +
            "b.income_functional_amount,b.lockDate,b.voucher_date,b.voucher_number,b.voucher_creator_name,b.customerId,b.customerName\n" +
            "FROM  tc_order c\n" +
            "LEFT JOIN (SELECT order_id AS orderId,customer_id AS customerId,MAX(customer_name) as customerName,financial_date AS lockDate,SUM(income_functional_amount) AS income_functional_amount,voucher_date,voucher_number,MAX(voucher_creator_name) AS voucher_creator_name FROM tc_income WHERE org_id=#{cssVoucherExport.orgId} GROUP BY order_id,financial_date,voucher_date,voucher_number,customer_id) b ON c.order_id = b.orderId\n" +
            "LEFT JOIN prm_coop d ON d.coop_id = c.coop_id\n" +
            "WHERE c.org_id=#{cssVoucherExport.orgId} and c.order_status !='强制关闭' and b.lockDate is not null" +
            "<when test='cssVoucherExport.businessScope!=null and cssVoucherExport.businessScope!=\"\"'>" +
            " and c.business_scope=#{cssVoucherExport.businessScope}" +
            "</when>" +
            "<when test='cssVoucherExport.coopName!=null and cssVoucherExport.coopName!=\"\"'>" +
            " and (d.coop_code like \"%\"#{cssVoucherExport.coopName}\"%\" or d.coop_name like \"%\"#{cssVoucherExport.coopName}\"%\")" +
            "</when>" +
            "<when test='cssVoucherExport.customerName!=null and cssVoucherExport.customerName!=\"\"'>" +
            " and (b.customerName like \"%\"#{cssVoucherExport.customerName}\"%\")" +
            "</when>" +
            "<when test='cssVoucherExport.voucherCreatorName!=null and cssVoucherExport.voucherCreatorName!=\"\"'>" +
            " and b.voucher_creator_name like \"%\"#{cssVoucherExport.voucherCreatorName}\"%\"" +
            "</when>" +
            "<when test='cssVoucherExport.awbNumber!=null and cssVoucherExport.awbNumber!=\"\"'>" +
            " and (c.customer_number like \"%\"#{cssVoucherExport.awbNumber}\"%\" or c.order_code like \"%\"#{cssVoucherExport.awbNumber}\"%\")" +
            "</when>" +
            "<when test='cssVoucherExport.voucherStatus!=null and cssVoucherExport.voucherStatus==true'>" +
            " and b.voucher_date is not null" +
            "</when>" +
            "<when test='cssVoucherExport.voucherStatus!=null and cssVoucherExport.voucherStatus==false'>" +
            " and b.voucher_date is null" +
            "</when>" +
            "<when test='cssVoucherExport.lockDateStart!=null'>" +
            " and b.lockDate <![CDATA[>=]]> #{cssVoucherExport.lockDateStart}" +
            "</when>" +
            "<when test='cssVoucherExport.lockDateEnd!=null'>" +
            " and b.lockDate <![CDATA[<=]]> #{cssVoucherExport.lockDateEnd}" +
            "</when>" +
            "<when test='cssVoucherExport.voucherDateStart!=null'>" +
            " and b.voucher_date <![CDATA[>=]]> #{cssVoucherExport.voucherDateStart}" +
            "</when>" +
            "<when test='cssVoucherExport.voucherDateEnd!=null'>" +
            " and b.voucher_date <![CDATA[<=]]> #{cssVoucherExport.voucherDateEnd}" +
            "</when>" +
            "order by c.order_code desc" +
            "</script>")
    List<CssVoucherExport> listIncomeForTC(@Param("cssVoucherExport") CssVoucherExport cssVoucherExport);

    @Select("<script>" +
            "SELECT c.order_id AS orderId,c.business_scope AS businessScope,c.order_code AS orderCode,c.customer_number AS customerNumber,d.coop_code,d.coop_name,\n" +
            "b.income_functional_amount,b.lockDate,b.voucher_date,b.voucher_number,b.voucher_creator_name,b.customerId,b.customerName\n" +
            "FROM lc_order c\n" +
            "LEFT JOIN (SELECT order_id AS orderId,customer_id AS customerId,MAX(customer_name) as customerName,financial_date AS lockDate,SUM(income_functional_amount) AS income_functional_amount,voucher_date,voucher_number,MAX(voucher_creator_name) AS voucher_creator_name FROM lc_income WHERE org_id=#{cssVoucherExport.orgId} GROUP BY order_id,financial_date,voucher_date,voucher_number,customer_id) b ON c.order_id = b.orderId\n" +
            "LEFT JOIN prm_coop d ON d.coop_id = c.coop_id\n" +
            "WHERE c.org_id=#{cssVoucherExport.orgId} and c.order_status !='强制关闭' and b.lockDate is not null" +
            "<when test='cssVoucherExport.businessScope!=null and cssVoucherExport.businessScope!=\"\"'>" +
            " and c.business_scope=#{cssVoucherExport.businessScope}" +
            "</when>" +
            "<when test='cssVoucherExport.coopName!=null and cssVoucherExport.coopName!=\"\"'>" +
            " and (d.coop_code like \"%\"#{cssVoucherExport.coopName}\"%\" or d.coop_name like \"%\"#{cssVoucherExport.coopName}\"%\")" +
            "</when>" +
            "<when test='cssVoucherExport.customerName!=null and cssVoucherExport.customerName!=\"\"'>" +
            " and (b.customerName like \"%\"#{cssVoucherExport.customerName}\"%\")" +
            "</when>" +
            "<when test='cssVoucherExport.voucherCreatorName!=null and cssVoucherExport.voucherCreatorName!=\"\"'>" +
            " and b.voucher_creator_name like \"%\"#{cssVoucherExport.voucherCreatorName}\"%\"" +
            "</when>" +
            "<when test='cssVoucherExport.awbNumber!=null and cssVoucherExport.awbNumber!=\"\"'>" +
            " and (c.customer_number like \"%\"#{cssVoucherExport.awbNumber}\"%\" or c.order_code like \"%\"#{cssVoucherExport.awbNumber}\"%\")" +
            "</when>" +
            "<when test='cssVoucherExport.voucherStatus!=null and cssVoucherExport.voucherStatus==true'>" +
            " and b.voucher_date is not null" +
            "</when>" +
            "<when test='cssVoucherExport.voucherStatus!=null and cssVoucherExport.voucherStatus==false'>" +
            " and b.voucher_date is null" +
            "</when>" +
            "<when test='cssVoucherExport.lockDateStart!=null'>" +
            " and b.lockDate <![CDATA[>=]]> #{cssVoucherExport.lockDateStart}" +
            "</when>" +
            "<when test='cssVoucherExport.lockDateEnd!=null'>" +
            " and b.lockDate <![CDATA[<=]]> #{cssVoucherExport.lockDateEnd}" +
            "</when>" +
            "<when test='cssVoucherExport.voucherDateStart!=null'>" +
            " and b.voucher_date <![CDATA[>=]]> #{cssVoucherExport.voucherDateStart}" +
            "</when>" +
            "<when test='cssVoucherExport.voucherDateEnd!=null'>" +
            " and b.voucher_date <![CDATA[<=]]> #{cssVoucherExport.voucherDateEnd}" +
            "</when>" +
            "order by c.order_code desc" +
            "</script>")
    List<CssVoucherExport> listIncomeForLC(@Param("cssVoucherExport") CssVoucherExport cssVoucherExport);

    @Select("<script>" +
            "SELECT c.order_id AS orderId,c.business_scope AS businessScope,c.order_code AS orderCode,c.customer_number AS customerNumber,d.coop_code,d.coop_name,\n" +
            "b.income_functional_amount,b.lockDate,b.voucher_date,b.voucher_number,b.voucher_creator_name,b.customerId,b.customerName\n" +
            "FROM  io_order c\n" +
            "LEFT JOIN (SELECT order_id AS orderId,customer_id AS customerId,MAX(customer_name) as customerName,financial_date AS lockDate,SUM(income_functional_amount) AS income_functional_amount,voucher_date,voucher_number,MAX(voucher_creator_name) AS voucher_creator_name FROM io_income WHERE org_id=#{cssVoucherExport.orgId} GROUP BY order_id,financial_date,voucher_date,voucher_number,customer_id) b ON c.order_id = b.orderId\n" +
            "LEFT JOIN prm_coop d ON d.coop_id = c.coop_id\n" +
            "WHERE c.org_id=#{cssVoucherExport.orgId} and c.order_status !='强制关闭' and b.lockDate is not null" +
            "<when test='cssVoucherExport.businessScope!=null and cssVoucherExport.businessScope!=\"\"'>" +
            " and c.business_scope=#{cssVoucherExport.businessScope}" +
            "</when>" +
            "<when test='cssVoucherExport.coopName!=null and cssVoucherExport.coopName!=\"\"'>" +
            " and (d.coop_code like \"%\"#{cssVoucherExport.coopName}\"%\" or d.coop_name like \"%\"#{cssVoucherExport.coopName}\"%\")" +
            "</when>" +
            "<when test='cssVoucherExport.customerName!=null and cssVoucherExport.customerName!=\"\"'>" +
            " and (b.customerName like \"%\"#{cssVoucherExport.customerName}\"%\")" +
            "</when>" +
            "<when test='cssVoucherExport.voucherCreatorName!=null and cssVoucherExport.voucherCreatorName!=\"\"'>" +
            " and b.voucher_creator_name like \"%\"#{cssVoucherExport.voucherCreatorName}\"%\"" +
            "</when>" +
            "<when test='cssVoucherExport.awbNumber!=null and cssVoucherExport.awbNumber!=\"\"'>" +
            " and (c.customer_number like \"%\"#{cssVoucherExport.awbNumber}\"%\" or c.order_code like \"%\"#{cssVoucherExport.awbNumber}\"%\")" +
            "</when>" +
            "<when test='cssVoucherExport.voucherStatus!=null and cssVoucherExport.voucherStatus==true'>" +
            " and b.voucher_date is not null" +
            "</when>" +
            "<when test='cssVoucherExport.voucherStatus!=null and cssVoucherExport.voucherStatus==false'>" +
            " and b.voucher_date is null" +
            "</when>" +
            "<when test='cssVoucherExport.lockDateStart!=null'>" +
            " and b.lockDate <![CDATA[>=]]> #{cssVoucherExport.lockDateStart}" +
            "</when>" +
            "<when test='cssVoucherExport.lockDateEnd!=null'>" +
            " and b.lockDate <![CDATA[<=]]> #{cssVoucherExport.lockDateEnd}" +
            "</when>" +
            "<when test='cssVoucherExport.voucherDateStart!=null'>" +
            " and b.voucher_date <![CDATA[>=]]> #{cssVoucherExport.voucherDateStart}" +
            "</when>" +
            "<when test='cssVoucherExport.voucherDateEnd!=null'>" +
            " and b.voucher_date <![CDATA[<=]]> #{cssVoucherExport.voucherDateEnd}" +
            "</when>" +
            "order by c.order_code desc" +
            "</script>")
    List<CssVoucherExport> listIncomeForIO(@Param("cssVoucherExport") CssVoucherExport cssVoucherExport);

    @Select("<script>" +
            "SELECT c.order_id AS orderId,c.business_scope AS businessScope, c.order_code AS orderCode,CASE WHEN c.business_scope='AI' and (c.awb_number is null or c.awb_number = '') and c.hawb_number is not null and c.hawb_number != '' then c.hawb_number WHEN c.business_scope='AI' and c.awb_number is not null and c.awb_number != '' and c.hawb_number is not null and c.hawb_number != '' then  CONCAT(c.awb_number,'_',c.hawb_number) ELSE c.awb_number END AS awbNumber,c.customer_number AS customerNumber,d.coop_code,d.coop_name,\n" +
            "b.cost_functional_amount,b.lockDate,b.voucher_date,b.voucher_number,b.voucher_creator_name,b.customerId,b.customerName\n" +
            "FROM af_order c\n" +
            "LEFT JOIN (SELECT order_id AS orderId,customer_id AS customerId,MAX(customer_name) as customerName,financial_date AS lockDate,SUM(cost_functional_amount) AS cost_functional_amount,voucher_date,voucher_number,MAX(voucher_creator_name) AS voucher_creator_name FROM af_cost WHERE org_id=#{cssVoucherExport.orgId} GROUP BY order_id,financial_date,voucher_date,voucher_number,customer_id) b ON c.order_id = b.orderId\n" +
            "LEFT JOIN prm_coop d ON d.coop_id = c.coop_id\n" +
            "WHERE c.org_id=#{cssVoucherExport.orgId} and c.order_status !='强制关闭' and b.lockDate is not null" +
            "<when test='cssVoucherExport.businessScope!=null and cssVoucherExport.businessScope!=\"\"'>" +
            " and c.business_scope=#{cssVoucherExport.businessScope}" +
            "</when>" +
            "<when test='cssVoucherExport.coopName!=null and cssVoucherExport.coopName!=\"\"'>" +
            " and (d.coop_code like \"%\"#{cssVoucherExport.coopName}\"%\" or d.coop_name like \"%\"#{cssVoucherExport.coopName}\"%\")" +
            "</when>" +
            "<when test='cssVoucherExport.customerName!=null and cssVoucherExport.customerName!=\"\"'>" +
            " and (b.customerName like \"%\"#{cssVoucherExport.customerName}\"%\")" +
            "</when>" +
            "<when test='cssVoucherExport.voucherCreatorName!=null and cssVoucherExport.voucherCreatorName!=\"\"'>" +
            " and b.voucher_creator_name like \"%\"#{cssVoucherExport.voucherCreatorName}\"%\"" +
            "</when>" +
            "<when test='cssVoucherExport.awbNumber!=null and cssVoucherExport.awbNumber!=\"\" and cssVoucherExport.businessScope==\"AI\"'>" +
            " and (c.awb_number like \"%\"#{cssVoucherExport.awbNumber}\"%\" or c.hawb_number like \"%\"#{cssVoucherExport.awbNumber}\"%\" or c.order_code like \"%\"#{cssVoucherExport.awbNumber}\"%\")" +
            "</when>" +
            "<when test='cssVoucherExport.awbNumber!=null and cssVoucherExport.awbNumber!=\"\" and cssVoucherExport.businessScope==\"AE\"'>" +
            " and (c.awb_number like \"%\"#{cssVoucherExport.awbNumber}\"%\" or c.order_code like \"%\"#{cssVoucherExport.awbNumber}\"%\")" +
            "</when>" +
            "<when test='cssVoucherExport.voucherStatus!=null and cssVoucherExport.voucherStatus==true'>" +
            " and b.voucher_date is not null" +
            "</when>" +
            "<when test='cssVoucherExport.voucherStatus!=null and cssVoucherExport.voucherStatus==false'>" +
            " and b.voucher_date is null" +
            "</when>" +
            "<when test='cssVoucherExport.lockDateStart!=null'>" +
            " and b.lockDate <![CDATA[>=]]> #{cssVoucherExport.lockDateStart}" +
            "</when>" +
            "<when test='cssVoucherExport.lockDateEnd!=null'>" +
            " and b.lockDate <![CDATA[<=]]> #{cssVoucherExport.lockDateEnd}" +
            "</when>" +
            "<when test='cssVoucherExport.voucherDateStart!=null'>" +
            " and b.voucher_date <![CDATA[>=]]> #{cssVoucherExport.voucherDateStart}" +
            "</when>" +
            "<when test='cssVoucherExport.voucherDateEnd!=null'>" +
            " and b.voucher_date <![CDATA[<=]]> #{cssVoucherExport.voucherDateEnd}" +
            "</when>" +
            "order by c.order_code desc" +
            "</script>")
    List<CssVoucherExport> listCostForAF(@Param("cssVoucherExport") CssVoucherExport cssVoucherExport);

    @Select("<script>" +
            "SELECT c.order_id AS orderId,c.business_scope AS businessScope,c.order_code AS orderCode,CASE WHEN c.business_scope='SI' and (c.mbl_number is null or c.mbl_number = '') and c.hbl_number is not null and c.hbl_number != '' then c.hbl_number WHEN c.business_scope='SI' and c.mbl_number is not null and c.mbl_number != '' and c.hbl_number is not null and c.hbl_number != '' then  CONCAT(c.mbl_number,'_',c.hbl_number) ELSE c.mbl_number END AS awbNumber,c.customer_number AS customerNumber,d.coop_code,d.coop_name,\n" +
            "b.cost_functional_amount,b.lockDate,b.voucher_date,b.voucher_number,b.voucher_creator_name,b.customerId,b.customerName\n" +
            "FROM sc_order c\n" +
            "LEFT JOIN (SELECT order_id AS orderId,customer_id AS customerId,MAX(customer_name) as customerName,financial_date AS lockDate,SUM(cost_functional_amount) AS cost_functional_amount,voucher_date,voucher_number,MAX(voucher_creator_name) AS voucher_creator_name FROM sc_cost WHERE org_id=#{cssVoucherExport.orgId} GROUP BY order_id,financial_date,voucher_date,voucher_number,customer_id) b ON c.order_id = b.orderId\n" +
            "LEFT JOIN prm_coop d ON d.coop_id = c.coop_id\n" +
            "WHERE c.org_id=#{cssVoucherExport.orgId} and c.order_status !='强制关闭' and b.lockDate is not null" +
            "<when test='cssVoucherExport.businessScope!=null and cssVoucherExport.businessScope!=\"\"'>" +
            " and c.business_scope=#{cssVoucherExport.businessScope}" +
            "</when>" +
            "<when test='cssVoucherExport.coopName!=null and cssVoucherExport.coopName!=\"\"'>" +
            " and (d.coop_code like \"%\"#{cssVoucherExport.coopName}\"%\" or d.coop_name like \"%\"#{cssVoucherExport.coopName}\"%\")" +
            "</when>" +
            "<when test='cssVoucherExport.customerName!=null and cssVoucherExport.customerName!=\"\"'>" +
            " and (b.customerName like \"%\"#{cssVoucherExport.customerName}\"%\")" +
            "</when>" +
            "<when test='cssVoucherExport.voucherCreatorName!=null and cssVoucherExport.voucherCreatorName!=\"\"'>" +
            " and b.voucher_creator_name like \"%\"#{cssVoucherExport.voucherCreatorName}\"%\"" +
            "</when>" +
            "<when test='cssVoucherExport.awbNumber!=null and cssVoucherExport.awbNumber!=\"\" and cssVoucherExport.businessScope==\"SI\"'>" +
            " and (c.mbl_number like \"%\"#{cssVoucherExport.awbNumber}\"%\" or c.hbl_number like \"%\"#{cssVoucherExport.awbNumber}\"%\" or c.order_code like \"%\"#{cssVoucherExport.awbNumber}\"%\")" +
            "</when>" +
            "<when test='cssVoucherExport.awbNumber!=null and cssVoucherExport.awbNumber!=\"\" and cssVoucherExport.businessScope==\"SE\"'>" +
            " and (c.mbl_number like \"%\"#{cssVoucherExport.awbNumber}\"%\" or c.order_code like \"%\"#{cssVoucherExport.awbNumber}\"%\")" +
            "</when>" +
            "<when test='cssVoucherExport.voucherStatus!=null and cssVoucherExport.voucherStatus==true'>" +
            " and b.voucher_date is not null" +
            "</when>" +
            "<when test='cssVoucherExport.voucherStatus!=null and cssVoucherExport.voucherStatus==false'>" +
            " and b.voucher_date is null" +
            "</when>" +
            "<when test='cssVoucherExport.lockDateStart!=null'>" +
            " and b.lockDate <![CDATA[>=]]> #{cssVoucherExport.lockDateStart}" +
            "</when>" +
            "<when test='cssVoucherExport.lockDateEnd!=null'>" +
            " and b.lockDate <![CDATA[<=]]> #{cssVoucherExport.lockDateEnd}" +
            "</when>" +
            "<when test='cssVoucherExport.voucherDateStart!=null'>" +
            " and b.voucher_date <![CDATA[>=]]> #{cssVoucherExport.voucherDateStart}" +
            "</when>" +
            "<when test='cssVoucherExport.voucherDateEnd!=null'>" +
            " and b.voucher_date <![CDATA[<=]]> #{cssVoucherExport.voucherDateEnd}" +
            "</when>" +
            "order by c.order_code desc" +
            "</script>")
    List<CssVoucherExport> listCostForSC(@Param("cssVoucherExport") CssVoucherExport cssVoucherExport);

    @Select("<script>" +
            "SELECT c.order_id AS orderId,c.business_scope AS businessScope,c.order_code AS orderCode,c.rwb_number AS awbNumber,c.customer_number AS customerNumber,d.coop_code,d.coop_name,\n" +
            "b.cost_functional_amount,b.lockDate,b.voucher_date,b.voucher_number,b.voucher_creator_name,b.customerId,b.customerName\n" +
            "FROM  tc_order c\n" +
            "LEFT JOIN (SELECT order_id AS orderId,customer_id AS customerId,MAX(customer_name) as customerName,financial_date AS lockDate,SUM(cost_functional_amount) AS cost_functional_amount,voucher_date,voucher_number,MAX(voucher_creator_name) AS voucher_creator_name FROM tc_cost WHERE org_id=#{cssVoucherExport.orgId} GROUP BY order_id,financial_date,voucher_date,voucher_number,customer_id) b ON c.order_id = b.orderId\n" +
            "LEFT JOIN prm_coop d ON d.coop_id = c.coop_id\n" +
            "WHERE c.org_id=#{cssVoucherExport.orgId} and c.order_status !='强制关闭' and b.lockDate is not null" +
            "<when test='cssVoucherExport.businessScope!=null and cssVoucherExport.businessScope!=\"\"'>" +
            " and c.business_scope=#{cssVoucherExport.businessScope}" +
            "</when>" +
            "<when test='cssVoucherExport.coopName!=null and cssVoucherExport.coopName!=\"\"'>" +
            " and (d.coop_code like \"%\"#{cssVoucherExport.coopName}\"%\" or d.coop_name like \"%\"#{cssVoucherExport.coopName}\"%\")" +
            "</when>" +
            "<when test='cssVoucherExport.customerName!=null and cssVoucherExport.customerName!=\"\"'>" +
            " and (b.customerName like \"%\"#{cssVoucherExport.customerName}\"%\")" +
            "</when>" +
            "<when test='cssVoucherExport.voucherCreatorName!=null and cssVoucherExport.voucherCreatorName!=\"\"'>" +
            " and b.voucher_creator_name like \"%\"#{cssVoucherExport.voucherCreatorName}\"%\"" +
            "</when>" +
            "<when test='cssVoucherExport.awbNumber!=null and cssVoucherExport.awbNumber!=\"\"'>" +
            " and (c.customer_number like \"%\"#{cssVoucherExport.awbNumber}\"%\" or c.order_code like \"%\"#{cssVoucherExport.awbNumber}\"%\")" +
            "</when>" +
            "<when test='cssVoucherExport.voucherStatus!=null and cssVoucherExport.voucherStatus==true'>" +
            " and b.voucher_date is not null" +
            "</when>" +
            "<when test='cssVoucherExport.voucherStatus!=null and cssVoucherExport.voucherStatus==false'>" +
            " and b.voucher_date is null" +
            "</when>" +
            "<when test='cssVoucherExport.lockDateStart!=null'>" +
            " and b.lockDate <![CDATA[>=]]> #{cssVoucherExport.lockDateStart}" +
            "</when>" +
            "<when test='cssVoucherExport.lockDateEnd!=null'>" +
            " and b.lockDate <![CDATA[<=]]> #{cssVoucherExport.lockDateEnd}" +
            "</when>" +
            "<when test='cssVoucherExport.voucherDateStart!=null'>" +
            " and b.voucher_date <![CDATA[>=]]> #{cssVoucherExport.voucherDateStart}" +
            "</when>" +
            "<when test='cssVoucherExport.voucherDateEnd!=null'>" +
            " and b.voucher_date <![CDATA[<=]]> #{cssVoucherExport.voucherDateEnd}" +
            "</when>" +
            "order by c.order_code desc" +
            "</script>")
    List<CssVoucherExport> listCostForTC(@Param("cssVoucherExport") CssVoucherExport cssVoucherExport);

    @Select("<script>" +
            "SELECT c.order_id AS orderId,c.business_scope AS businessScope,c.order_code AS orderCode,c.customer_number AS customerNumber,d.coop_code,d.coop_name,\n" +
            "b.cost_functional_amount,b.lockDate,b.voucher_date,b.voucher_number,b.voucher_creator_name,b.customerId,b.customerName\n" +
            "FROM lc_order c\n" +
            "LEFT JOIN (SELECT order_id AS orderId,customer_id AS customerId,MAX(customer_name) as customerName,financial_date AS lockDate,SUM(cost_functional_amount) AS cost_functional_amount,voucher_date,voucher_number,MAX(voucher_creator_name) AS voucher_creator_name FROM lc_cost WHERE org_id=#{cssVoucherExport.orgId} GROUP BY order_id,financial_date,voucher_date,voucher_number,customer_id) b ON c.order_id = b.orderId\n" +
            "LEFT JOIN prm_coop d ON d.coop_id = c.coop_id\n" +
            "WHERE c.org_id=#{cssVoucherExport.orgId} and c.order_status !='强制关闭' and b.lockDate is not null" +
            "<when test='cssVoucherExport.businessScope!=null and cssVoucherExport.businessScope!=\"\"'>" +
            " and c.business_scope=#{cssVoucherExport.businessScope}" +
            "</when>" +
            "<when test='cssVoucherExport.coopName!=null and cssVoucherExport.coopName!=\"\"'>" +
            " and (d.coop_code like \"%\"#{cssVoucherExport.coopName}\"%\" or d.coop_name like \"%\"#{cssVoucherExport.coopName}\"%\")" +
            "</when>" +
            "<when test='cssVoucherExport.customerName!=null and cssVoucherExport.customerName!=\"\"'>" +
            " and (b.customerName like \"%\"#{cssVoucherExport.customerName}\"%\")" +
            "</when>" +
            "<when test='cssVoucherExport.voucherCreatorName!=null and cssVoucherExport.voucherCreatorName!=\"\"'>" +
            " and b.voucher_creator_name like \"%\"#{cssVoucherExport.voucherCreatorName}\"%\"" +
            "</when>" +
            "<when test='cssVoucherExport.awbNumber!=null and cssVoucherExport.awbNumber!=\"\"'>" +
            " and (c.customer_number like \"%\"#{cssVoucherExport.awbNumber}\"%\" or c.order_code like \"%\"#{cssVoucherExport.awbNumber}\"%\")" +
            "</when>" +
            "<when test='cssVoucherExport.voucherStatus!=null and cssVoucherExport.voucherStatus==true'>" +
            " and b.voucher_date is not null" +
            "</when>" +
            "<when test='cssVoucherExport.voucherStatus!=null and cssVoucherExport.voucherStatus==false'>" +
            " and b.voucher_date is null" +
            "</when>" +
            "<when test='cssVoucherExport.lockDateStart!=null'>" +
            " and b.lockDate <![CDATA[>=]]> #{cssVoucherExport.lockDateStart}" +
            "</when>" +
            "<when test='cssVoucherExport.lockDateEnd!=null'>" +
            " and b.lockDate <![CDATA[<=]]> #{cssVoucherExport.lockDateEnd}" +
            "</when>" +
            "<when test='cssVoucherExport.voucherDateStart!=null'>" +
            " and b.voucher_date <![CDATA[>=]]> #{cssVoucherExport.voucherDateStart}" +
            "</when>" +
            "<when test='cssVoucherExport.voucherDateEnd!=null'>" +
            " and b.voucher_date <![CDATA[<=]]> #{cssVoucherExport.voucherDateEnd}" +
            "</when>" +
            "order by c.order_code desc" +
            "</script>")
    List<CssVoucherExport> listCostForLC(@Param("cssVoucherExport") CssVoucherExport cssVoucherExport);

    @Select("<script>" +
            "SELECT c.order_id AS orderId,c.business_scope AS businessScope,c.order_code AS orderCode,c.customer_number AS customerNumber,d.coop_code,d.coop_name,\n" +
            "b.cost_functional_amount,b.lockDate,b.voucher_date,b.voucher_number,b.voucher_creator_name,b.customerId,b.customerName\n" +
            "FROM  io_order c\n" +
            "LEFT JOIN (SELECT order_id AS orderId,customer_id AS customerId,MAX(customer_name) as customerName,financial_date AS lockDate,SUM(cost_functional_amount) AS cost_functional_amount,voucher_date,voucher_number,MAX(voucher_creator_name) AS voucher_creator_name FROM io_cost WHERE org_id=#{cssVoucherExport.orgId} GROUP BY order_id,financial_date,voucher_date,voucher_number,customer_id) b ON c.order_id = b.orderId\n" +
            "LEFT JOIN prm_coop d ON d.coop_id = c.coop_id\n" +
            "WHERE c.org_id=#{cssVoucherExport.orgId} and c.order_status !='强制关闭' and b.lockDate is not null" +
            "<when test='cssVoucherExport.businessScope!=null and cssVoucherExport.businessScope!=\"\"'>" +
            " and c.business_scope=#{cssVoucherExport.businessScope}" +
            "</when>" +
            "<when test='cssVoucherExport.coopName!=null and cssVoucherExport.coopName!=\"\"'>" +
            " and (d.coop_code like \"%\"#{cssVoucherExport.coopName}\"%\" or d.coop_name like \"%\"#{cssVoucherExport.coopName}\"%\")" +
            "</when>" +
            "<when test='cssVoucherExport.customerName!=null and cssVoucherExport.customerName!=\"\"'>" +
            " and (b.customerName like \"%\"#{cssVoucherExport.customerName}\"%\")" +
            "</when>" +
            "<when test='cssVoucherExport.voucherCreatorName!=null and cssVoucherExport.voucherCreatorName!=\"\"'>" +
            " and b.voucher_creator_name like \"%\"#{cssVoucherExport.voucherCreatorName}\"%\"" +
            "</when>" +
            "<when test='cssVoucherExport.awbNumber!=null and cssVoucherExport.awbNumber!=\"\"'>" +
            " and (c.customer_number like \"%\"#{cssVoucherExport.awbNumber}\"%\" or c.order_code like \"%\"#{cssVoucherExport.awbNumber}\"%\")" +
            "</when>" +
            "<when test='cssVoucherExport.voucherStatus!=null and cssVoucherExport.voucherStatus==true'>" +
            " and b.voucher_date is not null" +
            "</when>" +
            "<when test='cssVoucherExport.voucherStatus!=null and cssVoucherExport.voucherStatus==false'>" +
            " and b.voucher_date is null" +
            "</when>" +
            "<when test='cssVoucherExport.lockDateStart!=null'>" +
            " and b.lockDate <![CDATA[>=]]> #{cssVoucherExport.lockDateStart}" +
            "</when>" +
            "<when test='cssVoucherExport.lockDateEnd!=null'>" +
            " and b.lockDate <![CDATA[<=]]> #{cssVoucherExport.lockDateEnd}" +
            "</when>" +
            "<when test='cssVoucherExport.voucherDateStart!=null'>" +
            " and b.voucher_date <![CDATA[>=]]> #{cssVoucherExport.voucherDateStart}" +
            "</when>" +
            "<when test='cssVoucherExport.voucherDateEnd!=null'>" +
            " and b.voucher_date <![CDATA[<=]]> #{cssVoucherExport.voucherDateEnd}" +
            "</when>" +
            "order by c.order_code desc" +
            "</script>")
    List<CssVoucherExport> listCostForIO(@Param("cssVoucherExport") CssVoucherExport cssVoucherExport);


    @Select({"<script>",
            "select a.income_writeoff_id AS writeoffId,a.financial_account_code,a.financial_account_name,a.voucher_date,a.voucher_number,a.voucher_creator_name,a.writeoff_num AS writeoffNumber,a.writeoff_date AS writeoffDate,a.customer_name AS coopName,a.amount_writeoff AS writeoffAmount,a.currency,a.invoice_num AS invoiceNumber,a.invoice_title,a.invoice_date,a.invoice_remark from (",
            " SELECT a.income_writeoff_id,a.financial_account_code,a.financial_account_name,a.voucher_date,a.voucher_number,a.voucher_creator_name,a.writeoff_num,a.writeoff_date,a.customer_name,a.amount_writeoff,a.currency,b.invoice_num,b.invoice_title,b.invoice_date,b.invoice_remark",
            " FROM css_income_writeoff a",
            " LEFT JOIN  css_debit_note b ON a.debit_note_id=b.debit_note_id",
            "<when test='cssVoucherExport.businessScope==\"AI\" or cssVoucherExport.businessScope==\"AE\"'>",
            " LEFT JOIN  af_order c ON b.order_id=c.order_id",
            "</when>",
            "<when test='cssVoucherExport.businessScope==\"SI\" or cssVoucherExport.businessScope==\"SE\"'>",
            " LEFT JOIN  sc_order c ON b.order_id=c.order_id",
            "</when>",
            "<when test='cssVoucherExport.businessScope==\"TI\" or cssVoucherExport.businessScope==\"TE\"'>",
            " LEFT JOIN  tc_order c ON b.order_id=c.order_id",
            "</when>",
            "<when test='cssVoucherExport.businessScope==\"LC\"'>",
            " LEFT JOIN  lc_order c ON b.order_id=c.order_id",
            "</when>",
            "<when test='cssVoucherExport.businessScope==\"IO\"'>",
            " LEFT JOIN  io_order c ON b.order_id=c.order_id",
            "</when>",
            " WHERE 1=1 AND a.statement_id IS NULL  and a.org_id=#{cssVoucherExport.orgId} ",
            "<when test='cssVoucherExport.businessScope!=null and cssVoucherExport.businessScope!=\"\"'>",
            " AND a.business_scope = #{cssVoucherExport.businessScope}",
            "</when>",
            "<when test='cssVoucherExport.coopName!=null and cssVoucherExport.coopName!=\"\"'>",
            " AND a.customer_name like  \"%\"#{cssVoucherExport.coopName}\"%\"",
            "</when>",
            "<when test='cssVoucherExport.writeoffNumber!=null and cssVoucherExport.writeoffNumber!=\"\"'>",
            " AND a.writeoff_num like  \"%\"#{cssVoucherExport.writeoffNumber}\"%\"",
            "</when>",
            "<when test='cssVoucherExport.debitnoteNumber!=null and cssVoucherExport.debitnoteNumber!=\"\"'>",
            " AND b.debit_note_num like  \"%\"#{cssVoucherExport.debitnoteNumber}\"%\" ",
            "</when>",
            "<when test='cssVoucherExport.invoiceNumber!=null and cssVoucherExport.invoiceNumber!=\"\"'>",
            " AND b.invoice_num like  \"%\"#{cssVoucherExport.invoiceNumber}\"%\" ",
            "</when>",
            "<when test='cssVoucherExport.invoiceDateStart!=null'>",
            " AND b.invoice_date  <![CDATA[ >= ]]> #{cssVoucherExport.invoiceDateStart}",
            "</when>",
            "<when test='cssVoucherExport.invoiceDateEnd!=null'>",
            " AND b.invoice_date <![CDATA[ <= ]]> #{cssVoucherExport.invoiceDateEnd}",
            "</when>",
            "<when test='cssVoucherExport.awbNumber!=null and cssVoucherExport.awbNumber!=\"\" and (cssVoucherExport.businessScope==\"AE\" or cssVoucherExport.businessScope==\"AI\")'>",
            " AND (c.order_code like  \"%\"#{cssVoucherExport.awbNumber}\"%\" or c.awb_number like  \"%\"#{cssVoucherExport.awbNumber}\"%\" or c.hawb_number like  \"%\"#{cssVoucherExport.awbNumber}\"%\" ) ",
            "</when>",
            "<when test='cssVoucherExport.awbNumber!=null and cssVoucherExport.awbNumber!=\"\" and (cssVoucherExport.businessScope==\"SE\" or cssVoucherExport.businessScope==\"SI\")'>",
            " AND (c.order_code like  \"%\"#{cssVoucherExport.awbNumber}\"%\" or c.mbl_number like  \"%\"#{cssVoucherExport.awbNumber}\"%\" or c.hbl_number like  \"%\"#{cssVoucherExport.awbNumber}\"%\" ) ",
            "</when>",
            "<when test='cssVoucherExport.awbNumber!=null and cssVoucherExport.awbNumber!=\"\" and (cssVoucherExport.businessScope==\"TE\" or cssVoucherExport.businessScope==\"TI\")'>",
            " AND (c.order_code like  \"%\"#{cssVoucherExport.awbNumber}\"%\" or c.customer_number like \"%\"#{cssVoucherExport.awbNumber}\"%\")",
            "</when>",
            "<when test='cssVoucherExport.awbNumber!=null and cssVoucherExport.awbNumber!=\"\" and cssVoucherExport.businessScope==\"LC\"'>",
            " AND (c.order_code like  \"%\"#{cssVoucherExport.awbNumber}\"%\" or c.customer_number like \"%\"#{cssVoucherExport.awbNumber}\"%\")",
            "</when>",
            "<when test='cssVoucherExport.awbNumber!=null and cssVoucherExport.awbNumber!=\"\" and cssVoucherExport.businessScope==\"IO\"'>",
            " AND (c.order_code like  \"%\"#{cssVoucherExport.awbNumber}\"%\" or c.customer_number like \"%\"#{cssVoucherExport.awbNumber}\"%\")",
            "</when>",
            "<when test='cssVoucherExport.writeoffCreatorName!=null and cssVoucherExport.writeoffCreatorName!=\"\"'>",
            " AND a.creator_name like  \"%\"#{cssVoucherExport.writeoffCreatorName}\"%\" ",
            "</when>",
            "<when test='cssVoucherExport.writeoffDateStart!=null'>",
            " AND a.writeoff_date  <![CDATA[ >= ]]> #{cssVoucherExport.writeoffDateStart}",
            "</when>",
            "<when test='cssVoucherExport.writeoffDateEnd!=null'>",
            " AND a.writeoff_date <![CDATA[ <= ]]> #{cssVoucherExport.writeoffDateEnd}",
            "</when>",
            "<when test='cssVoucherExport.voucherDateStart!=null'>",
            " AND a.voucher_date  <![CDATA[ >= ]]> #{cssVoucherExport.voucherDateStart}",
            "</when>",
            "<when test='cssVoucherExport.voucherDateEnd!=null'>",
            " AND a.voucher_date <![CDATA[ <= ]]> #{cssVoucherExport.voucherDateEnd}",
            "</when>",
            "<when test='cssVoucherExport.voucherCreatorName!=null and cssVoucherExport.voucherCreatorName!=\"\"'>",
            " and a.voucher_creator_name like \"%\"#{cssVoucherExport.voucherCreatorName}\"%\"",
            "</when>",
            "<when test='cssVoucherExport.voucherStatus!=null and cssVoucherExport.voucherStatus==true'>",
            " and a.voucher_date is not null",
            "</when>",
            "<when test='cssVoucherExport.voucherStatus!=null and cssVoucherExport.voucherStatus==false'>",
            " and a.voucher_date is null",
            "</when>",
            " UNION ALL ",
            " SELECT a.income_writeoff_id,a.financial_account_code,a.financial_account_name,a.voucher_date,a.voucher_number,a.voucher_creator_name,a.writeoff_num,a.writeoff_date,a.customer_name,a.amount_writeoff,a.currency,b.invoice_num,b.invoice_title,b.invoice_date,b.invoice_remark",
            " FROM css_income_writeoff a",
            " LEFT JOIN css_statement b ON a.statement_id=b.statement_id",
            " INNER JOIN ",
            " ( SELECT ca.statement_id FROM css_debit_note ca",
            "<when test='cssVoucherExport.businessScope==\"AI\" or cssVoucherExport.businessScope==\"AE\"'>",
            "  INNER JOIN  af_order cb ON ca.order_id=cb.order_id",
            "</when>",
            "<when test='cssVoucherExport.businessScope==\"SI\" or cssVoucherExport.businessScope==\"SE\"'>",
            "  INNER JOIN  sc_order cb ON ca.order_id=cb.order_id",
            "</when>",

            "<when test='cssVoucherExport.businessScope==\"TI\" or cssVoucherExport.businessScope==\"TE\"'>",
            "  INNER JOIN  tc_order cb ON ca.order_id=cb.order_id",
            "</when>",
            "<when test='cssVoucherExport.businessScope==\"LC\"'>",
            "  INNER JOIN  lc_order cb ON ca.order_id=cb.order_id",
            "</when>",
            "<when test='cssVoucherExport.businessScope==\"IO\"'>",
            "  INNER JOIN  io_order cb ON ca.order_id=cb.order_id",
            "</when>",

            "  WHERE 1=1 AND ca.org_id=#{cssVoucherExport.orgId}",
            "<when test='cssVoucherExport.awbNumber!=null and cssVoucherExport.awbNumber!=\"\" and (cssVoucherExport.businessScope==\"AE\" or cssVoucherExport.businessScope==\"AI\")'>",
            " AND (cb.order_code like  \"%\"#{cssVoucherExport.awbNumber}\"%\" or cb.awb_number like  \"%\"#{cssVoucherExport.awbNumber}\"%\" or cb.hawb_number like  \"%\"#{cssVoucherExport.awbNumber}\"%\" ) ",
            "</when>",
            "<when test='cssVoucherExport.awbNumber!=null and cssVoucherExport.awbNumber!=\"\" and (cssVoucherExport.businessScope==\"SE\" or cssVoucherExport.businessScope==\"SI\")'>",
            " AND (cb.order_code like  \"%\"#{cssVoucherExport.awbNumber}\"%\" or cb.mbl_number like  \"%\"#{cssVoucherExport.awbNumber}\"%\" or cb.hbl_number like  \"%\"#{cssVoucherExport.awbNumber}\"%\" ) ",
            "</when>",

            "<when test='cssVoucherExport.awbNumber!=null and cssVoucherExport.awbNumber!=\"\" and (cssVoucherExport.businessScope==\"TE\" or cssVoucherExport.businessScope==\"TI\")'>",
            " AND (cb.order_code like  \"%\"#{cssVoucherExport.awbNumber}\"%\" or cb.customer_number like  \"%\"#{cssVoucherExport.awbNumber}\"%\") ",
            "</when>",

            "<when test='cssVoucherExport.awbNumber!=null and cssVoucherExport.awbNumber!=\"\" and cssVoucherExport.businessScope==\"TE\"'>",
            " AND (cb.order_code like  \"%\"#{cssVoucherExport.awbNumber}\"%\" or cb.customer_number like  \"%\"#{cssVoucherExport.awbNumber}\"%\") ",
            "</when>",
            "<when test='cssVoucherExport.awbNumber!=null and cssVoucherExport.awbNumber!=\"\" and cssVoucherExport.businessScope==\"LC\"'>",
            " AND (cb.order_code like  \"%\"#{cssVoucherExport.awbNumber}\"%\" or cb.customer_number like \"%\"#{cssVoucherExport.awbNumber}\"%\")",
            "</when>",
            "<when test='cssVoucherExport.awbNumber!=null and cssVoucherExport.awbNumber!=\"\" and cssVoucherExport.businessScope==\"IO\"'>",
            " AND (cb.order_code like  \"%\"#{cssVoucherExport.awbNumber}\"%\" or cb.customer_number like \"%\"#{cssVoucherExport.awbNumber}\"%\")",
            "</when>",
            "  GROUP BY ca.statement_id",
            " ) c ON b.statement_id=c.statement_id",
            "<when test='cssVoucherExport.businessScope!=null and cssVoucherExport.businessScope!=\"\"'>",
            " AND a.business_scope = #{cssVoucherExport.businessScope}",
            "</when>",
            "<when test='cssVoucherExport.coopName!=null and cssVoucherExport.coopName!=\"\"'>",
            " AND a.customer_name like  \"%\"#{cssVoucherExport.coopName}\"%\"",
            "</when>",
            "<when test='cssVoucherExport.writeoffNumber!=null and cssVoucherExport.writeoffNumber!=\"\"'>",
            " AND a.writeoff_num like  \"%\"#{cssVoucherExport.writeoffNumber}\"%\"",
            "</when>",
            "<when test='cssVoucherExport.debitnoteNumber!=null and cssVoucherExport.debitnoteNumber!=\"\"'>",
            " AND b.statement_num like  \"%\"#{cssVoucherExport.debitnoteNumber}\"%\" ",
            "</when>",
            "<when test='cssVoucherExport.invoiceNumber!=null and cssVoucherExport.invoiceNumber!=\"\"'>",
            " AND b.invoice_num like  \"%\"#{cssVoucherExport.invoiceNumber}\"%\" ",
            "</when>",
            "<when test='cssVoucherExport.invoiceDateStart!=null'>",
            " AND b.invoice_date  <![CDATA[ >= ]]> #{cssVoucherExport.invoiceDateStart}",
            "</when>",
            "<when test='cssVoucherExport.invoiceDateEnd!=null'>",
            " AND b.invoice_date <![CDATA[ <= ]]> #{cssVoucherExport.invoiceDateEnd}",
            "</when>",
            "<when test='cssVoucherExport.writeoffCreatorName!=null and cssVoucherExport.writeoffCreatorName!=\"\"'>",
            " AND a.creator_name like  \"%\"#{cssVoucherExport.writeoffCreatorName}\"%\" ",
            "</when>",
            "<when test='cssVoucherExport.writeoffDateStart!=null'>",
            " AND a.writeoff_date  <![CDATA[ >= ]]> #{cssVoucherExport.writeoffDateStart}",
            "</when>",
            "<when test='cssVoucherExport.writeoffDateEnd!=null'>",
            " AND a.writeoff_date <![CDATA[ <= ]]> #{cssVoucherExport.writeoffDateEnd}",
            "</when>",
            "<when test='cssVoucherExport.voucherDateStart!=null'>",
            " AND a.voucher_date  <![CDATA[ >= ]]> #{cssVoucherExport.voucherDateStart}",
            "</when>",
            "<when test='cssVoucherExport.voucherDateEnd!=null'>",
            " AND a.voucher_date <![CDATA[ <= ]]> #{cssVoucherExport.voucherDateEnd}",
            "</when>",
            "<when test='cssVoucherExport.voucherCreatorName!=null and cssVoucherExport.voucherCreatorName!=\"\"'>",
            " and a.voucher_creator_name like \"%\"#{cssVoucherExport.voucherCreatorName}\"%\"",
            "</when>",
            "<when test='cssVoucherExport.voucherStatus!=null and cssVoucherExport.voucherStatus==true'>",
            " and a.voucher_date is not null",
            "</when>",
            "<when test='cssVoucherExport.voucherStatus!=null and cssVoucherExport.voucherStatus==false'>",
            " and a.voucher_date is null",
            "</when>",
            ") a",
            " order by a.income_writeoff_id DESC ",
            "</script>"})
    IPage<CssVoucherExport> pageIncomeWriteoff(Page page, @Param("cssVoucherExport") CssVoucherExport cssVoucherExport);

    @Select({"<script>",
            "select a.income_writeoff_id AS writeoffId,a.financial_account_code,a.financial_account_name,a.voucher_date,a.voucher_number,a.voucher_creator_name,a.writeoff_num AS writeoffNumber,a.writeoff_date AS writeoffDate,a.customer_name AS coopName,a.amount_writeoff AS writeoffAmount,a.currency,a.invoice_num AS invoiceNumber,a.invoice_title,a.invoice_date,a.invoice_remark from (",
            " SELECT a.income_writeoff_id,a.financial_account_code,a.financial_account_name,a.voucher_date,a.voucher_number,a.voucher_creator_name,a.writeoff_num,a.writeoff_date,a.customer_name,a.amount_writeoff,a.currency,b.invoice_num,b.invoice_title,b.invoice_date,b.invoice_remark",
            " FROM css_income_writeoff a",
            " LEFT JOIN  css_debit_note b ON a.debit_note_id=b.debit_note_id",
            "<when test='cssVoucherExport.businessScope==\"AI\" or cssVoucherExport.businessScope==\"AE\"'>",
            " LEFT JOIN  af_order c ON b.order_id=c.order_id",
            "</when>",
            "<when test='cssVoucherExport.businessScope==\"SI\" or cssVoucherExport.businessScope==\"SE\"'>",
            " LEFT JOIN  sc_order c ON b.order_id=c.order_id",
            "</when>",
            "<when test='cssVoucherExport.businessScope==\"TI\" or cssVoucherExport.businessScope==\"TE\"'>",
            " LEFT JOIN  tc_order c ON b.order_id=c.order_id",
            "</when>",
            "<when test='cssVoucherExport.businessScope==\"LC\"'>",
            " LEFT JOIN  lc_order c ON b.order_id=c.order_id",
            "</when>",
            "<when test='cssVoucherExport.businessScope==\"IO\"'>",
            " LEFT JOIN  io_order c ON b.order_id=c.order_id",
            "</when>",
            " WHERE 1=1 AND a.statement_id IS NULL  and a.org_id=#{cssVoucherExport.orgId} ",
            "<when test='cssVoucherExport.businessScope!=null and cssVoucherExport.businessScope!=\"\"'>",
            " AND a.business_scope = #{cssVoucherExport.businessScope}",
            "</when>",
            "<when test='cssVoucherExport.coopName!=null and cssVoucherExport.coopName!=\"\"'>",
            " AND a.customer_name like  \"%\"#{cssVoucherExport.coopName}\"%\"",
            "</when>",
            "<when test='cssVoucherExport.writeoffNumber!=null and cssVoucherExport.writeoffNumber!=\"\"'>",
            " AND a.writeoff_num like  \"%\"#{cssVoucherExport.writeoffNumber}\"%\"",
            "</when>",
            "<when test='cssVoucherExport.debitnoteNumber!=null and cssVoucherExport.debitnoteNumber!=\"\"'>",
            " AND b.debit_note_num like  \"%\"#{cssVoucherExport.debitnoteNumber}\"%\" ",
            "</when>",
            "<when test='cssVoucherExport.invoiceNumber!=null and cssVoucherExport.invoiceNumber!=\"\"'>",
            " AND b.invoice_num like  \"%\"#{cssVoucherExport.invoiceNumber}\"%\" ",
            "</when>",
            "<when test='cssVoucherExport.invoiceDateStart!=null'>",
            " AND b.invoice_date  <![CDATA[ >= ]]> #{cssVoucherExport.invoiceDateStart}",
            "</when>",
            "<when test='cssVoucherExport.invoiceDateEnd!=null'>",
            " AND b.invoice_date <![CDATA[ <= ]]> #{cssVoucherExport.invoiceDateEnd}",
            "</when>",
            "<when test='cssVoucherExport.awbNumber!=null and cssVoucherExport.awbNumber!=\"\" and (cssVoucherExport.businessScope==\"AE\" or cssVoucherExport.businessScope==\"AI\")'>",
            " AND (c.order_code like  \"%\"#{cssVoucherExport.awbNumber}\"%\" or c.awb_number like  \"%\"#{cssVoucherExport.awbNumber}\"%\" or c.hawb_number like  \"%\"#{cssVoucherExport.awbNumber}\"%\" ) ",
            "</when>",
            "<when test='cssVoucherExport.awbNumber!=null and cssVoucherExport.awbNumber!=\"\" and (cssVoucherExport.businessScope==\"SE\" or cssVoucherExport.businessScope==\"SI\")'>",
            " AND (c.order_code like  \"%\"#{cssVoucherExport.awbNumber}\"%\" or c.mbl_number like  \"%\"#{cssVoucherExport.awbNumber}\"%\" or c.hbl_number like  \"%\"#{cssVoucherExport.awbNumber}\"%\" ) ",
            "</when>",
            "<when test='cssVoucherExport.awbNumber!=null and cssVoucherExport.awbNumber!=\"\" and (cssVoucherExport.businessScope==\"TE\" or cssVoucherExport.businessScope==\"TI\")'>",
            " AND (c.order_code like  \"%\"#{cssVoucherExport.awbNumber}\"%\" or c.customer_number like \"%\"#{cssVoucherExport.awbNumber}\"%\")",
            "</when>",
            "<when test='cssVoucherExport.awbNumber!=null and cssVoucherExport.awbNumber!=\"\" and cssVoucherExport.businessScope==\"LC\"'>",
            " AND (c.order_code like  \"%\"#{cssVoucherExport.awbNumber}\"%\" or c.customer_number like \"%\"#{cssVoucherExport.awbNumber}\"%\")",
            "</when>",
            "<when test='cssVoucherExport.awbNumber!=null and cssVoucherExport.awbNumber!=\"\" and cssVoucherExport.businessScope==\"IO\"'>",
            " AND (c.order_code like  \"%\"#{cssVoucherExport.awbNumber}\"%\" or c.customer_number like \"%\"#{cssVoucherExport.awbNumber}\"%\")",
            "</when>",
            "<when test='cssVoucherExport.writeoffCreatorName!=null and cssVoucherExport.writeoffCreatorName!=\"\"'>",
            " AND a.creator_name like  \"%\"#{cssVoucherExport.writeoffCreatorName}\"%\" ",
            "</when>",
            "<when test='cssVoucherExport.writeoffDateStart!=null'>",
            " AND a.writeoff_date  <![CDATA[ >= ]]> #{cssVoucherExport.writeoffDateStart}",
            "</when>",
            "<when test='cssVoucherExport.writeoffDateEnd!=null'>",
            " AND a.writeoff_date <![CDATA[ <= ]]> #{cssVoucherExport.writeoffDateEnd}",
            "</when>",
            "<when test='cssVoucherExport.voucherDateStart!=null'>",
            " AND a.voucher_date  <![CDATA[ >= ]]> #{cssVoucherExport.voucherDateStart}",
            "</when>",
            "<when test='cssVoucherExport.voucherDateEnd!=null'>",
            " AND a.voucher_date <![CDATA[ <= ]]> #{cssVoucherExport.voucherDateEnd}",
            "</when>",
            "<when test='cssVoucherExport.voucherCreatorName!=null and cssVoucherExport.voucherCreatorName!=\"\"'>",
            " and a.voucher_creator_name like \"%\"#{cssVoucherExport.voucherCreatorName}\"%\"",
            "</when>",
            "<when test='cssVoucherExport.voucherStatus!=null and cssVoucherExport.voucherStatus==true'>",
            " and a.voucher_date is not null",
            "</when>",
            "<when test='cssVoucherExport.voucherStatus!=null and cssVoucherExport.voucherStatus==false'>",
            " and a.voucher_date is null",
            "</when>",
            " UNION ALL ",
            " SELECT a.income_writeoff_id,a.financial_account_code,a.financial_account_name,a.voucher_date,a.voucher_number,a.voucher_creator_name,a.writeoff_num,a.writeoff_date,a.customer_name,a.amount_writeoff,a.currency,b.invoice_num,b.invoice_title,b.invoice_date,b.invoice_remark",
            " FROM css_income_writeoff a",
            " LEFT JOIN css_statement b ON a.statement_id=b.statement_id",
            " INNER JOIN ",
            " ( SELECT ca.statement_id FROM css_debit_note ca",
            "<when test='cssVoucherExport.businessScope==\"AI\" or cssVoucherExport.businessScope==\"AE\"'>",
            "  INNER JOIN  af_order cb ON ca.order_id=cb.order_id",
            "</when>",
            "<when test='cssVoucherExport.businessScope==\"SI\" or cssVoucherExport.businessScope==\"SE\"'>",
            "  INNER JOIN  sc_order cb ON ca.order_id=cb.order_id",
            "</when>",

            "<when test='cssVoucherExport.businessScope==\"TI\" or cssVoucherExport.businessScope==\"TE\"'>",
            "  INNER JOIN  tc_order cb ON ca.order_id=cb.order_id",
            "</when>",
            "<when test='cssVoucherExport.businessScope==\"LC\"'>",
            "  INNER JOIN  lc_order cb ON ca.order_id=cb.order_id",
            "</when>",
            "<when test='cssVoucherExport.businessScope==\"IO\"'>",
            "  INNER JOIN  io_order cb ON ca.order_id=cb.order_id",
            "</when>",

            "  WHERE 1=1 AND ca.org_id=#{cssVoucherExport.orgId}",
            "<when test='cssVoucherExport.awbNumber!=null and cssVoucherExport.awbNumber!=\"\" and (cssVoucherExport.businessScope==\"AE\" or cssVoucherExport.businessScope==\"AI\")'>",
            " AND (cb.order_code like  \"%\"#{cssVoucherExport.awbNumber}\"%\" or cb.awb_number like  \"%\"#{cssVoucherExport.awbNumber}\"%\" or cb.hawb_number like  \"%\"#{cssVoucherExport.awbNumber}\"%\" ) ",
            "</when>",
            "<when test='cssVoucherExport.awbNumber!=null and cssVoucherExport.awbNumber!=\"\" and (cssVoucherExport.businessScope==\"SE\" or cssVoucherExport.businessScope==\"SI\")'>",
            " AND (cb.order_code like  \"%\"#{cssVoucherExport.awbNumber}\"%\" or cb.mbl_number like  \"%\"#{cssVoucherExport.awbNumber}\"%\" or cb.hbl_number like  \"%\"#{cssVoucherExport.awbNumber}\"%\" ) ",
            "</when>",

            "<when test='cssVoucherExport.awbNumber!=null and cssVoucherExport.awbNumber!=\"\" and (cssVoucherExport.businessScope==\"TE\" or cssVoucherExport.businessScope==\"TI\")'>",
            " AND (cb.order_code like  \"%\"#{cssVoucherExport.awbNumber}\"%\" or cb.customer_number like  \"%\"#{cssVoucherExport.awbNumber}\"%\") ",
            "</when>",

            "<when test='cssVoucherExport.awbNumber!=null and cssVoucherExport.awbNumber!=\"\" and cssVoucherExport.businessScope==\"TE\"'>",
            " AND (cb.order_code like  \"%\"#{cssVoucherExport.awbNumber}\"%\" or cb.customer_number like  \"%\"#{cssVoucherExport.awbNumber}\"%\") ",
            "</when>",
            "<when test='cssVoucherExport.awbNumber!=null and cssVoucherExport.awbNumber!=\"\" and cssVoucherExport.businessScope==\"LC\"'>",
            " AND (cb.order_code like  \"%\"#{cssVoucherExport.awbNumber}\"%\" or cb.customer_number like \"%\"#{cssVoucherExport.awbNumber}\"%\")",
            "</when>",
            "<when test='cssVoucherExport.awbNumber!=null and cssVoucherExport.awbNumber!=\"\" and cssVoucherExport.businessScope==\"IO\"'>",
            " AND (cb.order_code like  \"%\"#{cssVoucherExport.awbNumber}\"%\" or cb.customer_number like \"%\"#{cssVoucherExport.awbNumber}\"%\")",
            "</when>",
            "  GROUP BY ca.statement_id",
            " ) c ON b.statement_id=c.statement_id",
            "<when test='cssVoucherExport.businessScope!=null and cssVoucherExport.businessScope!=\"\"'>",
            " AND a.business_scope = #{cssVoucherExport.businessScope}",
            "</when>",
            "<when test='cssVoucherExport.coopName!=null and cssVoucherExport.coopName!=\"\"'>",
            " AND a.customer_name like  \"%\"#{cssVoucherExport.coopName}\"%\"",
            "</when>",
            "<when test='cssVoucherExport.writeoffNumber!=null and cssVoucherExport.writeoffNumber!=\"\"'>",
            " AND a.writeoff_num like  \"%\"#{cssVoucherExport.writeoffNumber}\"%\"",
            "</when>",
            "<when test='cssVoucherExport.debitnoteNumber!=null and cssVoucherExport.debitnoteNumber!=\"\"'>",
            " AND b.statement_num like  \"%\"#{cssVoucherExport.debitnoteNumber}\"%\" ",
            "</when>",
            "<when test='cssVoucherExport.invoiceNumber!=null and cssVoucherExport.invoiceNumber!=\"\"'>",
            " AND b.invoice_num like  \"%\"#{cssVoucherExport.invoiceNumber}\"%\" ",
            "</when>",
            "<when test='cssVoucherExport.invoiceDateStart!=null'>",
            " AND b.invoice_date  <![CDATA[ >= ]]> #{cssVoucherExport.invoiceDateStart}",
            "</when>",
            "<when test='cssVoucherExport.invoiceDateEnd!=null'>",
            " AND b.invoice_date <![CDATA[ <= ]]> #{cssVoucherExport.invoiceDateEnd}",
            "</when>",
            "<when test='cssVoucherExport.writeoffCreatorName!=null and cssVoucherExport.writeoffCreatorName!=\"\"'>",
            " AND a.creator_name like  \"%\"#{cssVoucherExport.writeoffCreatorName}\"%\" ",
            "</when>",
            "<when test='cssVoucherExport.writeoffDateStart!=null'>",
            " AND a.writeoff_date  <![CDATA[ >= ]]> #{cssVoucherExport.writeoffDateStart}",
            "</when>",
            "<when test='cssVoucherExport.writeoffDateEnd!=null'>",
            " AND a.writeoff_date <![CDATA[ <= ]]> #{cssVoucherExport.writeoffDateEnd}",
            "</when>",
            "<when test='cssVoucherExport.voucherDateStart!=null'>",
            " AND a.voucher_date  <![CDATA[ >= ]]> #{cssVoucherExport.voucherDateStart}",
            "</when>",
            "<when test='cssVoucherExport.voucherDateEnd!=null'>",
            " AND a.voucher_date <![CDATA[ <= ]]> #{cssVoucherExport.voucherDateEnd}",
            "</when>",
            "<when test='cssVoucherExport.voucherCreatorName!=null and cssVoucherExport.voucherCreatorName!=\"\"'>",
            " and a.voucher_creator_name like \"%\"#{cssVoucherExport.voucherCreatorName}\"%\"",
            "</when>",
            "<when test='cssVoucherExport.voucherStatus!=null and cssVoucherExport.voucherStatus==true'>",
            " and a.voucher_date is not null",
            "</when>",
            "<when test='cssVoucherExport.voucherStatus!=null and cssVoucherExport.voucherStatus==false'>",
            " and a.voucher_date is null",
            "</when>",
            ") a",
            " order by a.income_writeoff_id DESC ",
            "</script>"})
    List<CssVoucherExport> listIncomeWriteoff(@Param("cssVoucherExport") CssVoucherExport cssVoucherExport);

    @Options(statementType = StatementType.CALLABLE)
    @ResultMap({"voucherExport1", "voucherExport2"})
    @Select({"<script>",
            "CALL css_P_financial_account_print(#{cssVoucherExport.orgId},#{cssVoucherExport.businessScope},'income',1,#{cssVoucherExport.sql},#{cssVoucherExport.voucherDate},#{cssVoucherExport.voucherNumber},#{cssVoucherExport.voucherIsDetail},#{cssVoucherExport.voucherCreatorId},#{cssVoucherExport.voucherCreatorName})\n",
            "</script>"})
    List<Map<String, Object>> voucherGenerateForIncome(@Param("cssVoucherExport") CssVoucherExport cssVoucherExport);

    @Options(statementType = StatementType.CALLABLE)
    @ResultMap({"voucherExport1", "voucherExport2"})
    @Select({"<script>",
            "CALL css_P_financial_account_print(#{cssVoucherExport.orgId},#{cssVoucherExport.businessScope},'cost',1,#{cssVoucherExport.sql},#{cssVoucherExport.voucherDate},#{cssVoucherExport.voucherNumber},#{cssVoucherExport.voucherIsDetail},#{cssVoucherExport.voucherCreatorId},#{cssVoucherExport.voucherCreatorName})\n",
            "</script>"})
    List<Map<String, Object>> voucherGenerateForCost(@Param("cssVoucherExport") CssVoucherExport cssVoucherExport);

    @Select({"<script>",
            "CALL css_P_financial_account_print(#{cssVoucherExport.orgId},#{cssVoucherExport.businessScope},'income',2,#{cssVoucherExport.sql},null,null,null,#{cssVoucherExport.voucherCreatorId},#{cssVoucherExport.voucherCreatorName})\n",
            "</script>"})
    Map<String, String> voucherCallbackForIncome(@Param("cssVoucherExport") CssVoucherExport cssVoucherExport);

    @Select({"<script>",
            "CALL css_P_financial_account_print(#{cssVoucherExport.orgId},#{cssVoucherExport.businessScope},'cost',2,#{cssVoucherExport.sql},null,null,null,#{cssVoucherExport.voucherCreatorId},#{cssVoucherExport.voucherCreatorName})\n",
            "</script>"})
    Map<String, String> voucherCallbackForCost(@Param("cssVoucherExport") CssVoucherExport cssVoucherExport);

    @Options(statementType = StatementType.CALLABLE)

    @ResultMap({"voucherExport1", "voucherExport2"})
    @Select({"<script>",
            "CALL css_P_financial_account_writeoff_print(#{cssVoucherExport.orgId},#{cssVoucherExport.businessScope},#{cssVoucherExport.writeoffType},1,#{cssVoucherExport.sql},#{cssVoucherExport.voucherDate},#{cssVoucherExport.voucherNumber},#{cssVoucherExport.voucherIsDetail},#{cssVoucherExport.voucherCreatorId},#{cssVoucherExport.voucherCreatorName})\n",
            "</script>"})
    List<Map<String, Object>> voucherGenerateForWriteoff(@Param("cssVoucherExport") CssVoucherExport cssVoucherExport);

    @Select({"<script>",
            "CALL css_P_financial_account_writeoff_print(#{cssVoucherExport.orgId},#{cssVoucherExport.businessScope},#{cssVoucherExport.writeoffType},2,#{cssVoucherExport.sql},null,null,null,#{cssVoucherExport.voucherCreatorId},#{cssVoucherExport.voucherCreatorName})\n",
            "</script>"})
    Map<String, String> voucherCallbackForWriteoff(@Param("cssVoucherExport") CssVoucherExport cssVoucherExport);

    @Select("<script>select expense_report_id,expense_financial_account_code,expense_financial_account_name,bank_financial_account_code,bank_financial_account_name,voucher_date,voucher_number,voucher_creator_name,expense_report_num,expense_report_status,expense_report_date,expenses_use,expense_amount,expense_report_remark,approval_financial_user_name,creator_name as expenseCreatorName from css_financial_expense_report" +
            " WHERE org_id=#{cssVoucherExport.orgId} and expense_report_status in ('已审核','已付款')" +
            "<when test='cssVoucherExport.paymentMethod!=null and cssVoucherExport.paymentMethod!=\"\"'>" +
            " and payment_method =#{cssVoucherExport.paymentMethod}" +
            "</when>" +
            "<when test='cssVoucherExport.expenseReportMode!=null and cssVoucherExport.expenseReportMode!=\"\"'>" +
            " and expense_report_mode =#{cssVoucherExport.expenseReportMode}" +
            "</when>" +
            "<when test='cssVoucherExport.expenseCreatorName!=null and cssVoucherExport.expenseCreatorName!=\"\"'>" +
            " and creator_name like \"%\"#{cssVoucherExport.expenseCreatorName}\"%\"" +
            "</when>" +
            "<when test='cssVoucherExport.voucherCreatorName!=null and cssVoucherExport.voucherCreatorName!=\"\"'>" +
            " and voucher_creator_name like \"%\"#{cssVoucherExport.voucherCreatorName}\"%\"" +
            "</when>" +
            "<when test='cssVoucherExport.expensesUse!=null and cssVoucherExport.expensesUse!=\"\"'>" +
            " and expenses_use like \"%\"#{cssVoucherExport.expensesUse}\"%\"" +
            "</when>" +
            "<when test='cssVoucherExport.expenseReportNum!=null and cssVoucherExport.expenseReportNum!=\"\"'>" +
            " and expense_report_num like \"%\"#{cssVoucherExport.expenseReportNum}\"%\"" +
            "</when>" +
            "<when test='cssVoucherExport.expenseReportDateStart!=null'>" +
            " and expense_report_date <![CDATA[>=]]> #{cssVoucherExport.expenseReportDateStart}" +
            "</when>" +
            "<when test='cssVoucherExport.expenseReportDateEnd!=null'>" +
            " and expense_report_date <![CDATA[<=]]> #{cssVoucherExport.expenseReportDateEnd}" +
            "</when>" +
            "<when test='cssVoucherExport.voucherDateStart!=null'>" +
            " and voucher_date <![CDATA[>=]]> #{cssVoucherExport.voucherDateStart}" +
            "</when>" +
            "<when test='cssVoucherExport.voucherDateEnd!=null'>" +
            " and voucher_date <![CDATA[<=]]> #{cssVoucherExport.voucherDateEnd}" +
            "</when>" +
            "<when test='cssVoucherExport.voucherStatus!=null and cssVoucherExport.voucherStatus==true'>" +
            " and voucher_date is not null" +
            "</when>" +
            "<when test='cssVoucherExport.voucherStatus!=null and cssVoucherExport.voucherStatus==false'>" +
            " and voucher_date is null" +
            "</when>" +
            "order by expense_report_id desc</script>")
    IPage<CssVoucherExport> pageExpenseReport(Page page, @Param("cssVoucherExport") CssVoucherExport cssVoucherExport);

    @Select("<script>select expense_report_id,expense_financial_account_code,expense_financial_account_name,bank_financial_account_code,bank_financial_account_name,voucher_date,voucher_number,voucher_creator_name,expense_report_num,expense_report_status,expense_report_date,expenses_use,expense_amount,expense_report_remark,approval_financial_user_name,creator_name as expenseCreatorName from css_financial_expense_report" +
            " WHERE org_id=#{cssVoucherExport.orgId} and expense_report_status in ('已审核','已付款')" +
            "<when test='cssVoucherExport.paymentMethod!=null and cssVoucherExport.paymentMethod!=\"\"'>" +
            " and payment_method =#{cssVoucherExport.paymentMethod}" +
            "</when>" +
            "<when test='cssVoucherExport.expenseReportMode!=null and cssVoucherExport.expenseReportMode!=\"\"'>" +
            " and expense_report_mode =#{cssVoucherExport.expenseReportMode}" +
            "</when>" +
            "<when test='cssVoucherExport.expenseCreatorName!=null and cssVoucherExport.expenseCreatorName!=\"\"'>" +
            " and creator_name like \"%\"#{cssVoucherExport.expenseCreatorName}\"%\"" +
            "</when>" +
            "<when test='cssVoucherExport.voucherCreatorName!=null and cssVoucherExport.voucherCreatorName!=\"\"'>" +
            " and voucher_creator_name like \"%\"#{cssVoucherExport.voucherCreatorName}\"%\"" +
            "</when>" +
            "<when test='cssVoucherExport.expensesUse!=null and cssVoucherExport.expensesUse!=\"\"'>" +
            " and expenses_use like \"%\"#{cssVoucherExport.expensesUse}\"%\"" +
            "</when>" +
            "<when test='cssVoucherExport.expenseReportNum!=null and cssVoucherExport.expenseReportNum!=\"\"'>" +
            " and expense_report_num like \"%\"#{cssVoucherExport.expenseReportNum}\"%\"" +
            "</when>" +
            "<when test='cssVoucherExport.expenseReportDateStart!=null'>" +
            " and expense_report_date <![CDATA[>=]]> #{cssVoucherExport.expenseReportDateStart}" +
            "</when>" +
            "<when test='cssVoucherExport.expenseReportDateEnd!=null'>" +
            " and expense_report_date <![CDATA[<=]]> #{cssVoucherExport.expenseReportDateEnd}" +
            "</when>" +
            "<when test='cssVoucherExport.voucherDateStart!=null'>" +
            " and voucher_date <![CDATA[>=]]> #{cssVoucherExport.voucherDateStart}" +
            "</when>" +
            "<when test='cssVoucherExport.voucherDateEnd!=null'>" +
            " and voucher_date <![CDATA[<=]]> #{cssVoucherExport.voucherDateEnd}" +
            "</when>" +
            "<when test='cssVoucherExport.voucherStatus!=null and cssVoucherExport.voucherStatus==true'>" +
            " and voucher_date is not null" +
            "</when>" +
            "<when test='cssVoucherExport.voucherStatus!=null and cssVoucherExport.voucherStatus==false'>" +
            " and voucher_date is null" +
            "</when>" +
            "order by expense_report_id desc</script>")
    List<CssVoucherExport> listExpenseReport(@Param("cssVoucherExport") CssVoucherExport cssVoucherExport);

    @Select({"<script>",
            "CALL css_P_financial_expense_report_print(#{cssVoucherExport.orgId},2,#{cssVoucherExport.sql},null,null,null,#{cssVoucherExport.voucherCreatorId},#{cssVoucherExport.voucherCreatorName})\n",
            "</script>"})
    Map<String, String> voucherCallbackForExpenseReport(@Param("cssVoucherExport") CssVoucherExport cssVoucherExport);

    @Options(statementType = StatementType.CALLABLE)

    @ResultMap({"voucherExport1", "voucherExport2"})
    @Select({"<script>",
            "CALL css_P_financial_expense_report_print(#{cssVoucherExport.orgId},1,#{cssVoucherExport.sql},#{cssVoucherExport.voucherDate},#{cssVoucherExport.voucherNumber},#{cssVoucherExport.voucherIsDetail},#{cssVoucherExport.voucherCreatorId},#{cssVoucherExport.voucherCreatorName})\n",
            "</script>"})
    List voucherGenerateForExpenseReport(@Param("cssVoucherExport") CssVoucherExport cssVoucherExport);

    @Select("SELECT MAX(voucher_number) AS voucher_number FROM css_financial_voucher_number_log\n" +
            "WHERE org_id=#{orgId}\n" +
            "AND return_voucher_creator_id IS NULL\n" +
            "AND DATE_FORMAT(voucher_date,'%Y-%m')=#{voucherDate}")
    Integer getMaxVoucherNumber(@Param("voucherDate") String voucherDate, @Param("orgId") Integer orgId);
}
