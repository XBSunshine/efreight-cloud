package com.efreight.prm.dao;


import com.efreight.prm.entity.*;

import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;
import java.util.Map;

public interface CoopBillMapper {
    CoopBill selectById(Integer billId);

    void insert(CoopBill coopBill);

    void deleteStatementById(Integer statement_id);

    void deleteBilltById(Integer statement_id);

    List<CoopBill> selectList(CoopBill coopBill);
    List<CoopBill> isHavedList(CoopBill coopBill);

    void confirm(@Param("billId") Integer billId, @Param("now") Date now);

    void verify(@Param("statement_id") Integer statement_id, @Param("invoiceWriteoffDate") Date invoiceWriteoffDate,@Param("acturalCharge") Double acturalCharge,@Param("editorId") Integer editorId,@Param("editorName") String editorName,@Param("invoiceWriteoffUserName") String invoiceWriteoffUserName,@Param("now") Date now);

    void fill(@Param("billId") Integer billId, @Param("acturalCharge") Double acturalCharge, @Param("now") Date now,@Param("fill_user") String fill_user);
    void doFill(CoopBill billBean);
    void doFill1(CoopBill billBean);

    void invoice(@Param("statement_id") Integer statement_id, @Param("invoiceNo") String invoiceNo, @Param("acturalCharge") Double acturalCharge,@Param("now") Date now,@Param("editorId") Integer editorId,
                 @Param("editorName") String editorName,@Param("invoiceUserName") String invoiceUserName,@Param("invoiceTitle") String invoiceTitle,@Param("invoiceType") String invoiceType,
                 @Param("invoiceRemark") String invoiceRemark,@Param("expressCompany") String expressCompany,@Param("expressNumber") String expressNumber
                ,@Param("invoiceDate") Date invoiceDate);

    String getCurrentBillNumber(CoopUnConfirmBillDetail coopUnConfirmBillDetail);

    List<CoopBillEmail> selectBillEmailList(Integer orgId);

    List<CoopBillGroup> selectByGroup(CoopBillGroup coopBillGroup);

    List<CoopBillGroupMerge> selectByMerge(CoopBillGroupMerge coopBillGroupMerge);

    List<CoopBillGroupDetail> selectByMonthAndCoopName(CoopBillGroup coopBill);

    List<CoopBillStatement> getMadeBillList(CoopBillStatement coopBillStatement);

    String getCurrentBillStatementNumber(CoopBillStatement coopBillStatement);

    void insertStatement(CoopBillStatement coopBillStatement);

    void updateCoopBillByBillId(CoopBill coopBill);

    List<CoopBill> checkBillByStatementId(Integer statement_id);

    void updateStatementMailDate(CoopBillStatement coopBillStatement);

    void generateBill(CoopBill coopBill);

    List<List<CoopBillEmail>> getPdfFields(Map<String, Object> param);

    List<BillServiceGroup> searchBillServiceGroup(BillServiceGroup billServiceGroup);

    List<BillServiceDetail> getServiceDetailByServiceCode(BillServiceGroup billServiceGroup);

    void addService(BillServiceGroup billServiceGroup);

    String getMaxServiceCode();

    String getMaxServiceCodeDetail(String serviceCodeGroup);

    void editService(BillServiceGroup billServiceGroup);

    List<CoopUnConfirmBillGroup> searchUnConfirmBillGroup(CoopUnConfirmBillDetail coopUnConfirmBillDetail);

    List<CoopUnConfirmBillDetail> findCoopUnConfirmBillDetail(Map<String, Object> paramMap);

    void updateStatementByStatementId(CoopUnConfirmBillGroup coopUnConfirmBillGroup);

    void updateStatementByStatementId1(CoopUnConfirmBillGroup coopUnConfirmBillGroup);

    void updateStatementByStatementId2(CoopUnConfirmBillGroup coopUnConfirmBillGroup);

    void updateStatementByStatementId3(CoopUnConfirmBillGroup coopUnConfirmBillGroup);

    void updateAmountReceivable(CoopUnConfirmBillGroup coopUnConfirmBillGroup);

    void setStatement(CoopBill coopBill);

    void customerConfirmBill(CoopUnConfirmBillGroup coopUnConfirmBillGroup);

    void updateSettlementBySettlementId(CoopUnConfirmBillGroup coopUnConfirmBillGroup);

    List<CoopUnConfirmBillGroup> searchUnConfirmBillGroup_detail(CoopUnConfirmBillDetail coopUnConfirmBillDetail);

    List<CoopUnConfirmBillDetail> findCoopUnConfirmBillDetail_detail(Map<String, Object> paramMap);

    List<CoopBillSettle> searchCoopBillSettleList(Map<String, Object> paramMap);

    CoopUnConfirmBillGroup getTotalPlanChargeByStatementId(Integer statementId);

    Integer getCountByStatementId(Integer statementId);

    void updateStatementStatusByStatementId(Integer statementId);

    Integer getServiceCountByServiceName(BillServiceGroup billServiceGroup);

    Integer getServiceProjectCountByServiceName(BillServiceGroup billServiceGroup);

    List<CoopServiceBean> queryServiceIsValid();

    List<CoopServiceBean> queryServiceTwoIsValid();

    List<CoopBillSettleExcel> queryListForExcel(Map<String, Object> paramMap);

    void repairBill(CoopBill coopBill);

    Double getOriginalChargeByBillId(Integer billId);

    List<String> checkIfModify(CoopUnConfirmBillGroup coopUnConfirmBillGroup);

    CoopUnConfirmBillDetail getRemarkByBillId(Map<String, Object> paramMap);

    String findBillConfirmContacts(CoopAgreementSettlement cas);

    List<CoopBillMadeExcel> queryMadeBillListForExcel(CoopBillStatement coopBillStatement);

    String getConfirmSalerEmailByStatementId(CoopUnConfirmBillGroup coopUnConfirmBillGroup);

    void insertBill(CoopUnConfirmBillDetail coopUnConfirmBillDetail);

    String getEmailByContactId(CoopBillStatement coopBillStatement);

    List<Integer> getAutoSendList(String billMonth);

    List<CoopUnConfirmBillDetail> getAllBillByStatementId(Integer statementId);

    void updateBillByStatementId(Integer statementId);

    void updateBillByBillId(CoopUnConfirmBillDetail det);

    void updateOtherBillByBillId(CoopUnConfirmBillDetail det);
}
