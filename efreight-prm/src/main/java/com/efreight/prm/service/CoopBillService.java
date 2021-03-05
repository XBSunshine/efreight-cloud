package com.efreight.prm.service;

import com.efreight.prm.entity.*;
import com.itextpdf.text.DocumentException;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.Map;

public interface CoopBillService {

    CoopBill getView(Integer billId);

    void save(Date createTimeStart, Date createTimeEnd) throws ClassNotFoundException, SQLException;

    void delete(Integer billId);

    void sendBill(Integer statementId) throws ClassNotFoundException, SQLException, IOException;

    void autoSendBill(String billMonth) throws ClassNotFoundException, SQLException, IOException;

    void verify(Integer statement_id,Double acturalCharge,String invoiceWriteoffDate) throws ParseException;

    void fill(Integer billId, Double acturalCharge);
    void doFill(CoopBill bean);

    void invoice(String invoiceNo,Integer statement_id,Double acturalCharge,String invoiceTitle,String invoiceType,String invoiceRemark,String expressCompany,String expressNumber,Date invoiceDate);

    Map<String, Object> getPage(Integer current, Integer size, CoopBill coopBill);

   // List<CoopBillGroup> getUnmakeBillList(CoopBillGroup coopBillGroup);
   List<CoopBillGroup> getUnmakeBillList(CoopBillGroupMerge coopBillGroupMerge);

    Map<String, Object> getMadeBillList(Integer current, Integer size, CoopBillStatement coopBillStatement);

    void makingBill(CoopBillStatement coopBillStatement) throws ParseException;

    List<CoopBill> checkBillByStatementId(Integer statement_id);

    Boolean printBill(Integer statement_id);

    String printBill1(Integer statement_id) throws IOException, DocumentException;

    List<BillServiceGroup> searchBillService(BillServiceGroup billServiceGroup);

    List<BillServiceGroup> searchBillServiceSelect();

    void addService(BillServiceGroup billServiceGroup);

    void editService(BillServiceGroup billServiceGroup);

    Map<String,Object> searchUnConfirmBill(CoopUnConfirmBillDetail coopUnConfirmBillDetail, Integer currentPage, Integer pageSize);

    void confirmBill(CoopUnConfirmBillGroup coopUnConfirmBillGroup);

    void refuseConfirmBill(CoopUnConfirmBillGroup coopUnConfirmBillGroup);

    void updateAmountReceivable(CoopUnConfirmBillGroup coopUnConfirmBillGroup);

    void customerConfirmBill(Integer statementId,Integer settlementId);

    Map<String,Object> searchUnConfirmBill_detail(CoopUnConfirmBillDetail coopUnConfirmBillDetail, Integer currentPage, Integer pageSize);

    Map<String,Object> searchCoopBillSettleList(CoopBillSettle coopBillSettle,Integer currentPage, Integer pageSize);

    Integer isHaveServiceClass(BillServiceGroup billServiceGroup);

    Integer isHaveServiceProject(BillServiceGroup billServiceGroup);

    List<CoopServiceBean> queryServiceIsValid();

    List<CoopServiceBean> queryServiceTwoIsValid();

    List<CoopBillSettle> getTotalSettle(CoopBillSettle coopBillSettle);

    List<CoopBillSettleExcel> queryListForExcel(CoopBillSettle coopBillSettle);

    void repairBill(Date createTimeStart) throws ClassNotFoundException, SQLException;

    String checkIfModify(CoopUnConfirmBillGroup coopUnConfirmBillGroup);

    String getRemarkByBillId(Integer billId);

    String print(CoopBillEmail coopBillEmail, List<CoopBillEmail> list, boolean flag);

    void sendInvoiceEmail(SendInvoiceEmail sendInvoiceEmail) throws Exception;

    List<CoopBillMadeExcel> querymadeBillListForExcel(CoopBillStatement coopBillStatement);

    Integer saveManualBill(CoopManualBill coopManualBill) throws ParseException;

    void sendManualBill(Integer statementId,String toUsers);

    List<CoopBillStatement> getTotalMadeBill(CoopBillStatement coopBillStatement);
}
