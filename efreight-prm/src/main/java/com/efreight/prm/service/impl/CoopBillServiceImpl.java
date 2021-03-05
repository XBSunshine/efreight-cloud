package com.efreight.prm.service.impl;

import cn.hutool.core.util.StrUtil;

import com.efreight.common.core.jms.MailSendService;
import com.efreight.common.security.util.SecurityUtils;
import com.efreight.prm.dao.CoopBillMapper;
import com.efreight.prm.entity.*;
import com.efreight.prm.service.CoopAgreementSettlementService;
import com.efreight.prm.service.CoopBillService;
import com.efreight.prm.service.LogService;
import com.efreight.prm.util.PDFUtils;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.BadPdfFormatException;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;


import java.io.*;
import java.math.BigDecimal;
import java.sql.*;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;

@Slf4j
@Service
@RefreshScope
public class CoopBillServiceImpl implements CoopBillService {
    @Autowired
    private CoopAgreementSettlementService coopAgreementSettlementService;
    @Autowired
    private CoopBillMapper coopBillMapper;
    @Autowired
    private LogService logService;
    @Autowired
    private MailSendService mailSendService;

    private String parentPath = "/datadisk/html";

    @Override
    public CoopBill getView(Integer billId) {
        return coopBillMapper.selectById(billId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void save(Date createTimeStart, Date createTimeEnd) throws ClassNotFoundException, SQLException {

        SimpleDateFormat dateFormat1 = new SimpleDateFormat("yyyy-MM");
        if (createTimeStart != null) {
            String billMonth = dateFormat1.format(createTimeStart);
            CoopBill coopBill = new CoopBill();
            coopBill.setBillName(billMonth);
            coopBillMapper.generateBill(coopBill);
        }

        try {
            LogBean bean = new LogBean();
            bean.setOrg_id(SecurityUtils.getUser().getOrgId());
            bean.setDept_id(SecurityUtils.getUser().getDeptId());
            bean.setCreator_id(SecurityUtils.getUser().getId());
            bean.setCreate_time(new Date());
            bean.setCreator_name("");
            bean.setOp_type("新建");
            bean.setOp_level("高");
            bean.setOp_name("翌飞账单");
            bean.setOp_info("新建账单");
            logService.doSave(bean);
        } catch (Exception e) {
            log.info("翌飞账单创建成功,日志更新失败!");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void repairBill(Date createTimeStart) throws ClassNotFoundException, SQLException {

        SimpleDateFormat dateFormat1 = new SimpleDateFormat("yyyy-MM");
        if (createTimeStart != null) {
            String billMonth = dateFormat1.format(createTimeStart);
            CoopBill coopBill = new CoopBill();
            coopBill.setBillName(billMonth);
            coopBillMapper.repairBill(coopBill);
        }

        try {
            LogBean bean = new LogBean();
            bean.setOrg_id(SecurityUtils.getUser().getOrgId());
            bean.setDept_id(SecurityUtils.getUser().getDeptId());
            bean.setCreator_id(SecurityUtils.getUser().getId());
            bean.setCreate_time(new Date());
            bean.setCreator_name("");
            bean.setOp_type("新建");
            bean.setOp_level("高");
            bean.setOp_name("翌飞账单");
            bean.setOp_info("新建账单");
            logService.doSave(bean);
        } catch (Exception e) {
            log.info("翌飞账单创建成功,日志更新失败!");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void makingBill(CoopBillStatement coopBillStatement) throws ParseException {
        //向prm_coop_statement表插入一条数据
        coopBillStatement.setCreateTime(new Date());
        coopBillStatement.setCreatorId(SecurityUtils.getUser().getId());
        coopBillStatement.setOrgId(SecurityUtils.getUser().getOrgId());
        coopBillStatement.setCreatorName(SecurityUtils.getUser().getUserCname() + " " + SecurityUtils.getUser().getUserEmail());
        coopBillStatement.setEditorId(SecurityUtils.getUser().getId());
        coopBillStatement.setEditorName(SecurityUtils.getUser().getUserCname() + " " + SecurityUtils.getUser().getUserEmail());
        coopBillStatement.setEditTime(new Date());
        //获取选择月份下属于本签约公司的账单号的最大值
        String currentBillNumber = coopBillMapper.getCurrentBillStatementNumber(coopBillStatement);
        String billNumber = "";
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyMM");
        SimpleDateFormat dateFormat1 = new SimpleDateFormat("yyyy-MM");
        if (currentBillNumber == null || "".equals(currentBillNumber)) {//如果最大值为空，则说明还不存在，则编号从000001开始
            billNumber = "ST" + dateFormat.format(dateFormat1.parse(coopBillStatement.getBillName())) + "000001";
        } else {//如果存在则给其序列号+1
            String subBillNumber = currentBillNumber.substring(currentBillNumber.length() - 6);
            if (subBillNumber != null && !subBillNumber.isEmpty()) {
                int newSubBillNumber = Integer.parseInt(subBillNumber) + 1;
                billNumber = String.format(currentBillNumber.substring(0, 6) + "%06d", newSubBillNumber);
            }
        }
        coopBillStatement.setStatementNumber(billNumber);
        coopBillMapper.insertStatement(coopBillStatement);
        //获取自动生成的账单ID
        Integer statementId = coopBillStatement.getStatement_id();
        //根据传过来的billid更新prm_coop_bill表，设置statementID字段，将账单状态设为已确认
        CoopBill coopBill = new CoopBill();
        coopBill.setStatementId(statementId);
        String billIds = coopBillStatement.getBillIds();
        if (billIds != null && !"".equals(billIds)) {
            String[] billIds1 = billIds.split(",");
            if (billIds1 != null && billIds1.length > 0) {
                for (int i = 0; i < billIds1.length; i++) {
                    coopBill.setBillId(Integer.parseInt(billIds1[i]));
                    coopBillMapper.updateCoopBillByBillId(coopBill);
                }
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Integer statement_id) {
        coopBillMapper.deleteStatementById(statement_id);
        coopBillMapper.deleteBilltById(statement_id);
        try {
            LogBean bean = new LogBean();
            bean.setOrg_id(SecurityUtils.getUser().getOrgId());
            bean.setDept_id(SecurityUtils.getUser().getDeptId());
            bean.setCreator_id(SecurityUtils.getUser().getId());
            bean.setCreate_time(new Date());
            bean.setCreator_name("");
            bean.setOp_type("账单删除");
            bean.setOp_level("高");
            bean.setOp_name("翌飞账单");
            bean.setOp_info("账单号" + statement_id + "删除成功");
            logService.doSave(bean);
        } catch (Exception e) {
            log.info("翌飞账单删除成功,日志更新失败!");
            throw new RuntimeException("翌飞账单删除成功,日志更新失败!");
        }
    }

    @Override
    public void fill(Integer billId, Double acturalCharge) {
        CoopBill coopBill = coopBillMapper.selectById(billId);
        if (coopBill == null) {
            throw new RuntimeException("该账单不存在");
        }
        if (!("未操作".equals(coopBill.getBillStatus()) || "已确认".equals(coopBill.getBillStatus()) || "已核销".equals(coopBill.getBillStatus()) || "已开票".equals(coopBill.getBillStatus()))) {
            throw new RuntimeException("当前账单状态不可以数据填充");
        }
        String fill_user = SecurityUtils.getUser().getUserCname() + " " + SecurityUtils.getUser().getUserEmail();
        coopBillMapper.fill(billId, acturalCharge, new Date(), fill_user);
        try {
            LogBean bean = new LogBean();
            bean.setOrg_id(SecurityUtils.getUser().getOrgId());
            bean.setDept_id(SecurityUtils.getUser().getDeptId());
            bean.setCreator_id(SecurityUtils.getUser().getId());
            bean.setCreate_time(new Date());
            bean.setCreator_name("");
            bean.setOp_type("账单数据填充");
            bean.setOp_level("高");
            bean.setOp_name("翌飞账单");
            bean.setOp_info("账单号" + billId + "已数据填充");
            logService.doSave(bean);
        } catch (Exception e) {
            throw new RuntimeException("翌飞账单数据填充成功,日志更新失败!");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void doFill(CoopBill billBean) {
        CoopBill coopBill = coopBillMapper.selectById(billBean.getBillId());
        if (coopBill == null) {
            throw new RuntimeException("该账单不存在");
        }
        billBean.setFillUser(SecurityUtils.getUser().getUserCname() + " " + SecurityUtils.getUser().getUserEmail());
        billBean.setBillFillDate(new Date());
        if (billBean.getIsDetail() == "detail" || "detail".equals(billBean.getIsDetail())) {//说明是从账单明细进来的
            billBean.setActuralCharge(billBean.getPlanCharge());
            billBean.setDiscount(10.00);
            coopBillMapper.doFill(billBean);
            //根据statementId在prm_coop_bill表中查询填充后总的应收金额
            CoopUnConfirmBillGroup coopUnConfirmBillGroup = coopBillMapper.getTotalPlanChargeByStatementId(billBean.getStatementId());

            if (coopUnConfirmBillGroup != null) {
                Double acturalCharge = coopUnConfirmBillGroup.getTotalActuralCharge();
                if (coopUnConfirmBillGroup.getMinCharge() != null && !"".equals(coopUnConfirmBillGroup.getMinCharge()) && coopUnConfirmBillGroup.getMinCharge() != 0 && acturalCharge < coopUnConfirmBillGroup.getMinCharge()) {
                    acturalCharge = coopUnConfirmBillGroup.getMinCharge();
                }
                if (coopUnConfirmBillGroup.getMaxCharge() != null && !"".equals(coopUnConfirmBillGroup.getMaxCharge()) && coopUnConfirmBillGroup.getMaxCharge() != 0 && acturalCharge > coopUnConfirmBillGroup.getMaxCharge()) {
                    acturalCharge = coopUnConfirmBillGroup.getMaxCharge();
                }
                //更新prm_coop_statement表根据statementId
                coopUnConfirmBillGroup.setPlanCharge(acturalCharge);
                coopBillMapper.updateAmountReceivable(coopUnConfirmBillGroup);
            }
            //根据statementId去prm_coop_bill表查询相应的statementId的记录是否还有状态为数据未填充的记录，如果没有，则根据statementId更新prm_coop_statement表的状态位数据已填充
            Integer count = coopBillMapper.getCountByStatementId(billBean.getStatementId());
            if (count == 0) {
                coopBillMapper.updateStatementStatusByStatementId(billBean.getStatementId());
            }
        } else {
            //根据billId查询账单明细的原始金额（记录填充账单的金额）
            Double originalCharge = coopBillMapper.getOriginalChargeByBillId(billBean.getBillId());
            if (billBean.getActuralCharge() !=null && !billBean.getActuralCharge().equals(originalCharge)) {//更改过
                billBean.setModifySaler(1);
                //更新prm_coop_statement，设置modify_saler为1
                coopBillMapper.setStatement(billBean);
            }
            coopBillMapper.doFill1(billBean);
        }
        try {
            LogBean bean = new LogBean();
            bean.setOrg_id(SecurityUtils.getUser().getOrgId());
            bean.setDept_id(SecurityUtils.getUser().getDeptId());
            bean.setCreator_id(SecurityUtils.getUser().getId());
            bean.setCreate_time(new Date());
            bean.setCreator_name("");
            bean.setOp_type("账单数据填充");
            bean.setOp_level("高");
            bean.setOp_name("翌飞账单");
            bean.setOp_info("账单号" + billBean.getBillId() + "已数据填充");
            logService.doSave(bean);
        } catch (Exception e) {
            throw new RuntimeException("翌飞账单数据填充成功,日志更新失败!");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void sendBill(Integer statement_id) throws ClassNotFoundException, SQLException, IOException {

        Map<String, Object> param = new HashMap<>();
        param.put("statement_id", statement_id);
        // param.put("billStyle", billStyle);
        List<List<CoopBillEmail>> list = coopBillMapper.getPdfFields(param);
        /*System.out.println("第一个结果集"+list.get(0));
        System.out.println("第二个结果集"+list.get(1));
        System.out.println("第二个结果集ges"+list.get(1).size());*/
        if (list != null && list.size() > 0) {
            if (list.get(0) != null && list.get(0).size() > 0) {
                CoopBillEmail coopBillEmail = new CoopBillEmail();
                List<CoopBillEmail> listDetail = new ArrayList<CoopBillEmail>();
                if (list != null && list.size() > 0) {
                    //拼接PDF表头
                    List<CoopBillEmail> list1 = list.get(0);
                    coopBillEmail.setTxt_01(list1.get(0).getTxt_01());
                    coopBillEmail.setTxt_02(list1.get(0).getTxt_02());
                    coopBillEmail.setTxt_03(list1.get(0).getTxt_03());
                    coopBillEmail.setTxt_04(list1.get(0).getTxt_04());
                    coopBillEmail.setTxt_05(list1.get(0).getTxt_05());
                    coopBillEmail.setTxt_06(list1.get(0).getTxt_06());
                    coopBillEmail.setTxt_07(list1.get(0).getTxt_07());
                    coopBillEmail.setTxt_08(list1.get(0).getTxt_08());
                    coopBillEmail.setTxt_09(list1.get(0).getTxt_09());
                    coopBillEmail.setTxt_10(list1.get(0).getTxt_10());
                    coopBillEmail.setMail_cc(list1.get(0).getMail_cc());
                    coopBillEmail.setMail_to(list1.get(0).getMail_to());
                    coopBillEmail.setMailAttachment(list1.get(0).getMailAttachment());
                    coopBillEmail.setMailTitle(list1.get(0).getMailTitle());
                    coopBillEmail.setPrintTemplate(list1.get(0).getPrintTemplate());
                    coopBillEmail.setMailBody(list1.get(0).getMailBody());

                    //拼接账单明细
                    listDetail = list.get(1);
                }
                if (coopBillEmail != null) {
                    List<File> files = new ArrayList<File>();
                    //生成PDF并放入附件
                    String pdfPath = "";
                    try {
                        pdfPath = PDFUtils.fillTemplate(coopBillEmail, listDetail, parentPath);//生成PDF
                        File file = new File(pdfPath);
                        files.add(file);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    //是否有明细附件，如果有放入附件
                    String filePathMimeName = "";
                    ArrayList<Map<String, String>> fileList = new ArrayList<>();
                    Map<String, String> map = new HashMap<>();
                    map.put("path", PDFUtils.filePath + pdfPath);//账单路径放入map
                    map.put("name", pdfPath.substring(pdfPath.lastIndexOf("/") + 1, pdfPath.length()));//账单附件名称放入map
                    map.put("flag", "local");
                    fileList.add(map);
                    //从明细中拿附件路径和名称
                    if (listDetail != null && listDetail.size() > 0) {
                        for (int i = 0; i < listDetail.size(); i++) {
                            if (!StrUtil.isBlank(listDetail.get(i).getMailAttachmentUrl())) {
                                String mailAttachmentUrl = listDetail.get(i).getMailAttachmentUrl();
                                filePathMimeName = listDetail.get(i).getMailAttachmentName() + mailAttachmentUrl.substring(mailAttachmentUrl.lastIndexOf("."), mailAttachmentUrl.length());
                                Map<String, String> map1 = new HashMap<>();
                                map1.put("path", mailAttachmentUrl);//账单路径放入map
                                map1.put("name", filePathMimeName);//账单附件名称放入map
                                map1.put("flag", "upload");
                                fileList.add(map1);
                            }
                        }
                    }
                    //生成邮件
                    //生成收件人数组
                    String recipientEmails = coopBillEmail.getMail_to();
                    String[] recipientEmail = new String[0];
                    if (recipientEmails != null && !"".equals(recipientEmails)) {
                        recipientEmail = recipientEmails.split(",");
                    }
                    String title = coopBillEmail.getMailTitle();
                    String mailBody = coopBillEmail.getMailBody();
                    StringBuilder builder = new StringBuilder();
                    builder.append(mailBody);
                    //生成抄送人数组
                    String ccEmails = coopBillEmail.getMail_cc();
                    String[] ccEmail = null;
                    if (ccEmails != null && !"".equals(ccEmails)) {
                        ccEmail = ccEmails.split(",");
                    }
                    mailSendService.sendAttachmentsMailNew(false, recipientEmail, ccEmail, null, title, builder.toString(), fileList, null);
                }
                //更新 prm_coop_statement
                CoopBillStatement cst = new CoopBillStatement();
                cst.setStatement_id(statement_id);
                cst.setStatementMailDate(new Date());
                cst.setStatementMailSenderName(SecurityUtils.getUser().getUserCname() + " " + SecurityUtils.getUser().getUserEmail());
                cst.setStatementMailSenderId(SecurityUtils.getUser().getId());
                cst.setMailSendTime(new Date());
                coopBillMapper.updateStatementMailDate(cst);
            }
        }

        try {
            LogBean bean = new LogBean();
            bean.setOrg_id(SecurityUtils.getUser().getOrgId());
            bean.setDept_id(SecurityUtils.getUser().getDeptId());
            bean.setCreator_id(SecurityUtils.getUser().getId());
            bean.setCreate_time(new Date());
            bean.setCreator_name("");
            bean.setOp_type("账单确认");
            bean.setOp_level("高");
            bean.setOp_name("翌飞账单");
            bean.setOp_info("账单号" + statement_id + "已确认");
            logService.doSave(bean);
        } catch (Exception e) {
            throw new RuntimeException("翌飞账单确认成功,日志更新失败!");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void autoSendBill(String billMonth) throws ClassNotFoundException, SQLException, IOException {
        //查询需要自动发送邮件的账单
        List<Integer> autoSendList = coopBillMapper.getAutoSendList(billMonth);
        if(autoSendList != null && autoSendList.size() > 0){
            for (int j = 0; j < autoSendList.size(); j++) {

                Integer statement_id = autoSendList.get(j);
                Map<String, Object> param = new HashMap<>();
                param.put("statement_id", statement_id);
                List<List<CoopBillEmail>> list = coopBillMapper.getPdfFields(param);
                if (list != null && list.size() > 0) {
                    if (list.get(0) != null && list.get(0).size() > 0) {
                        CoopBillEmail coopBillEmail = new CoopBillEmail();
                        List<CoopBillEmail> listDetail = new ArrayList<CoopBillEmail>();
                        if (list != null && list.size() > 0) {
                            //拼接PDF表头
                            List<CoopBillEmail> list1 = list.get(0);
                            coopBillEmail.setTxt_01(list1.get(0).getTxt_01());
                            coopBillEmail.setTxt_02(list1.get(0).getTxt_02());
                            coopBillEmail.setTxt_03(list1.get(0).getTxt_03());
                            coopBillEmail.setTxt_04(list1.get(0).getTxt_04());
                            coopBillEmail.setTxt_05(list1.get(0).getTxt_05());
                            coopBillEmail.setTxt_06(list1.get(0).getTxt_06());
                            coopBillEmail.setTxt_07(list1.get(0).getTxt_07());
                            coopBillEmail.setTxt_08(list1.get(0).getTxt_08());
                            coopBillEmail.setTxt_09(list1.get(0).getTxt_09());
                            coopBillEmail.setTxt_10(list1.get(0).getTxt_10());
                            coopBillEmail.setMail_cc(list1.get(0).getMail_cc());
                            coopBillEmail.setMail_to(list1.get(0).getMail_to());
                            coopBillEmail.setMailAttachment(list1.get(0).getMailAttachment());
                            coopBillEmail.setMailTitle(list1.get(0).getMailTitle());
                            coopBillEmail.setPrintTemplate(list1.get(0).getPrintTemplate());
                            coopBillEmail.setMailBody(list1.get(0).getMailBody());

                            //拼接账单明细
                            listDetail = list.get(1);
                        }
                        if (coopBillEmail != null) {
                            List<File> files = new ArrayList<File>();
                            //生成PDF并放入附件
                            String pdfPath = "";
                            try {
                                pdfPath = PDFUtils.fillTemplate(coopBillEmail, listDetail, parentPath);//生成PDF
                                File file = new File(pdfPath);
                                files.add(file);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            //是否有明细附件，如果有放入附件
                            String filePathMimeName = "";
                            ArrayList<Map<String, String>> fileList = new ArrayList<>();
                            Map<String, String> map = new HashMap<>();
                            map.put("path", PDFUtils.filePath + pdfPath);//账单路径放入map
                            map.put("name", pdfPath.substring(pdfPath.lastIndexOf("/") + 1, pdfPath.length()));//账单附件名称放入map
                            map.put("flag", "local");
                            fileList.add(map);
                            //从明细中拿附件路径和名称
                            if (listDetail != null && listDetail.size() > 0) {
                                for (int i = 0; i < listDetail.size(); i++) {
                                    if (!StrUtil.isBlank(listDetail.get(i).getMailAttachmentUrl())) {
                                        String mailAttachmentUrl = listDetail.get(i).getMailAttachmentUrl();
                                        filePathMimeName = listDetail.get(i).getMailAttachmentName() + mailAttachmentUrl.substring(mailAttachmentUrl.lastIndexOf("."), mailAttachmentUrl.length());
                                        Map<String, String> map1 = new HashMap<>();
                                        map1.put("path", mailAttachmentUrl);//账单路径放入map
                                        map1.put("name", filePathMimeName);//账单附件名称放入map
                                        map1.put("flag", "upload");
                                        fileList.add(map1);
                                    }
                                }
                            }
                            //生成邮件
                            //生成收件人数组
                            String recipientEmails = coopBillEmail.getMail_to();
                            String[] recipientEmail = new String[0];
                            if (recipientEmails != null && !"".equals(recipientEmails)) {
                                recipientEmail = recipientEmails.split(",");
                            }
                            String title = coopBillEmail.getMailTitle();
                            String mailBody = coopBillEmail.getMailBody();
                            StringBuilder builder = new StringBuilder();
                            builder.append(mailBody);
                            //生成抄送人数组
                            String ccEmails = coopBillEmail.getMail_cc();
                            String[] ccEmail = null;
                            if (ccEmails != null && !"".equals(ccEmails)) {
                                ccEmail = ccEmails.split(",");
                            }
                            mailSendService.sendAttachmentsMailNew(false, recipientEmail, ccEmail, null, title, builder.toString(), fileList, null);
                        }
                        //更新 prm_coop_statement
                        CoopBillStatement cst = new CoopBillStatement();
                        cst.setStatement_id(statement_id);
                        cst.setStatementMailDate(new Date());
                        cst.setStatementMailSenderName(SecurityUtils.getUser().getUserCname() + " " + SecurityUtils.getUser().getUserEmail());
                        cst.setStatementMailSenderId(SecurityUtils.getUser().getId());
                        cst.setConfirmSalerTime(new Date());
                        cst.setConfirmSalerName("系统");
                        cst.setMailSendTime(new Date());
                        cst.setStatementStatus("账单已发送");
                        coopBillMapper.updateStatementMailDate(cst);
                    }
                }

                try {
                    LogBean bean = new LogBean();
                    bean.setOrg_id(SecurityUtils.getUser().getOrgId());
                    bean.setDept_id(SecurityUtils.getUser().getDeptId());
                    bean.setCreator_id(SecurityUtils.getUser().getId());
                    bean.setCreate_time(new Date());
                    bean.setCreator_name("");
                    bean.setOp_type("自动发送账单");
                    bean.setOp_level("高");
                    bean.setOp_name("翌飞账单");
                    bean.setOp_info("账单号" + statement_id + "已发送");
                    logService.doSave(bean);
                } catch (Exception e) {
                    throw new RuntimeException("翌飞账单确认成功,日志更新失败!");
                }
            }
        }
    }

    @Override
    public Boolean printBill(Integer statement_id) {
        Map<String, Object> param = new HashMap<>();
        param.put("statement_id", statement_id);
        List<List<CoopBillEmail>> list = coopBillMapper.getPdfFields(param);
        CoopBillEmail coopBillEmail = new CoopBillEmail();
        List<CoopBillEmail> listDetail = new ArrayList<CoopBillEmail>();
        if (list != null && list.size() > 0) {
            //拼接PDF表头
            List<CoopBillEmail> list1 = list.get(0);
            coopBillEmail.setTxt_01(list1.get(0).getTxt_01());
            coopBillEmail.setTxt_02(list1.get(0).getTxt_02());
            coopBillEmail.setTxt_03(list1.get(0).getTxt_03());
            coopBillEmail.setTxt_04(list1.get(0).getTxt_04());
            coopBillEmail.setTxt_05(list1.get(0).getTxt_05());
            coopBillEmail.setTxt_06(list1.get(0).getTxt_06());
            coopBillEmail.setTxt_07(list1.get(0).getTxt_07());
            coopBillEmail.setTxt_08(list1.get(0).getTxt_08());
            coopBillEmail.setTxt_09(list1.get(0).getTxt_09());
            coopBillEmail.setTxt_10(list1.get(0).getTxt_10());
            coopBillEmail.setMail_cc(list1.get(0).getMail_cc());
            coopBillEmail.setMail_to(list1.get(0).getMail_to());
            coopBillEmail.setMailAttachment(list1.get(0).getMailAttachment());
            coopBillEmail.setMailTitle(list1.get(0).getMailTitle());
            coopBillEmail.setPrintTemplate(list1.get(0).getPrintTemplate());

            //拼接账单明细
            listDetail = list.get(1);
        }
        try {
            PDFUtils.printBill(coopBillEmail, listDetail, parentPath);//生成PDF
        } catch (BadPdfFormatException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (DocumentException e) {
            e.printStackTrace();
        }
        return true;
    }

    @Override
    public String printBill1(Integer statementId) throws IOException, DocumentException {
        Map<String, Object> param = new HashMap<>();
        param.put("statement_id", statementId);
        List<List<CoopBillEmail>> list = coopBillMapper.getPdfFields(param);
        CoopBillEmail coopBillEmail = new CoopBillEmail();
        List<CoopBillEmail> listDetail = new ArrayList<CoopBillEmail>();
        if (list != null && list.size() > 0) {
            //拼接PDF表头
            List<CoopBillEmail> list1 = list.get(0);
            coopBillEmail.setTxt_01(list1.get(0).getTxt_01());
            coopBillEmail.setTxt_02(list1.get(0).getTxt_02());
            coopBillEmail.setTxt_03(list1.get(0).getTxt_03());
            coopBillEmail.setTxt_04(list1.get(0).getTxt_04());
            coopBillEmail.setTxt_05(list1.get(0).getTxt_05());
            coopBillEmail.setTxt_06(list1.get(0).getTxt_06());
            coopBillEmail.setTxt_07(list1.get(0).getTxt_07());
            coopBillEmail.setTxt_08(list1.get(0).getTxt_08());
            coopBillEmail.setTxt_09(list1.get(0).getTxt_09());
            coopBillEmail.setTxt_10(list1.get(0).getTxt_10());
            coopBillEmail.setMail_cc(list1.get(0).getMail_cc());
            coopBillEmail.setMail_to(list1.get(0).getMail_to());
            coopBillEmail.setMailAttachment(list1.get(0).getMailAttachment());
            coopBillEmail.setMailTitle(list1.get(0).getMailTitle());
            coopBillEmail.setPrintTemplate(list1.get(0).getPrintTemplate());

            //拼接账单明细
            listDetail = list.get(1);
        }

        ArrayList<String> newFilePaths = new ArrayList<>();
        String path = print(coopBillEmail, listDetail, false);
        newFilePaths.add(path);

        String lastFilePath = PDFUtils.filePath + "/PDFtemplate/temp/printBillTemp/DEBITNOTE_" + new Date().getTime() + ".pdf";
        PDFUtils.loadAllPDFForFile(newFilePaths, lastFilePath, null);
        return lastFilePath.replace(PDFUtils.filePath, "");
    }

    @Override
    @SneakyThrows
    public String print(CoopBillEmail coopBillEmail, List<CoopBillEmail> list, boolean flag) {

        if (flag) {
            return fillTemplate1(coopBillEmail, list, PDFUtils.filePath);
        } else {
            return fillTemplate1(coopBillEmail, list, "");
        }
    }

    public static String fillTemplate1(CoopBillEmail coopBillEmail, List<CoopBillEmail> list, String parentPath) throws IOException, DocumentException {

        //新建临时保存路径的路径数组
        List<String> PDFPathList = new ArrayList<>();


        SimpleDateFormat df1 = new SimpleDateFormat("yyyy-MM-ddHH-mm-ss");
        String pdfName = df1.format(new Date());
        //String templateName = order.getLetterPdf();
        // 模板路径
        String templatePath = PDFUtils.filePath + "/PDFtemplate/" + coopBillEmail.getPrintTemplate();
        String savePath = PDFUtils.filePath + "/PDFtemplate/temp/printBillTemp";


        //得到文件保存的名称
        String saveFilename = PDFUtils.makeFileName(pdfName + ".pdf");
        //得到文件的保存目录
        String newPDFPath = PDFUtils.makePath(saveFilename, savePath) + "/" + saveFilename;
        PDFPathList.add(newPDFPath);

        // Map<String, String> valueData  = new HashMap<>();
        //月份全部大写
        //String Etd = "";

        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        Map<String, String> valueData = new HashMap<>();
        valueData.put("txt_01", coopBillEmail.getTxt_01());//客商资料名称
        valueData.put("txt_02", coopBillEmail.getTxt_02());
        valueData.put("txt_03", coopBillEmail.getTxt_03());
        valueData.put("txt_04", coopBillEmail.getTxt_04());
        valueData.put("txt_05", coopBillEmail.getTxt_05());
        valueData.put("txt_06", coopBillEmail.getTxt_06());
        valueData.put("txt_07", coopBillEmail.getTxt_07());
        valueData.put("txt_08", coopBillEmail.getTxt_08());
        valueData.put("txt_09", coopBillEmail.getTxt_09());
        valueData.put("txt_10", coopBillEmail.getTxt_10());

        //明细
        for (int i = 0; i < list.size(); i++) {
            CoopBillEmail innerDetail = list.get(i);
            int index = (i + 1);
            if (index < 10) {
                valueData.put("r_0" + index + "01", innerDetail.getR_01());
                valueData.put("r_0" + index + "02", innerDetail.getR_02());
                valueData.put("r_0" + index + "03", innerDetail.getR_03());
                valueData.put("r_0" + index + "04", innerDetail.getR_04());
                valueData.put("r_0" + index + "05", innerDetail.getR_05());
                valueData.put("r_0" + index + "06", innerDetail.getR_06());
                valueData.put("r_0" + index + "07", innerDetail.getR_07());
                valueData.put("r_0" + index + "08", innerDetail.getR_08());
                valueData.put("r_0" + index + "09", innerDetail.getR_09());
            } else {
                valueData.put("r_" + index + "01", innerDetail.getR_01());
                valueData.put("r_" + index + "02", innerDetail.getR_02());
                valueData.put("r_" + index + "03", innerDetail.getR_03());
                valueData.put("r_" + index + "04", innerDetail.getR_04());
                valueData.put("r_" + index + "05", innerDetail.getR_05());
                valueData.put("r_" + index + "06", innerDetail.getR_06());
                valueData.put("r_" + index + "07", innerDetail.getR_07());
                valueData.put("r_" + index + "08", innerDetail.getR_08());
                valueData.put("r_" + index + "09", innerDetail.getR_09());
            }
        }

        //填充每个PDF
        PDFUtils.loadPDF2(templatePath, newPDFPath, valueData, false);
        return newPDFPath.replace(parentPath, "");
    }

    @Override
    public void invoice(String invoiceNo, Integer statement_id, Double acturalCharge, String invoiceTitle, String invoiceType, String invoiceRemark, String expressCompany, String expressNumber, Date invoiceDate) {
        /*CoopBill coopBill = coopBillMapper.selectById(statement_id);
        if (coopBill == null) {
            throw new RuntimeException("该账单不存在");
        }*/
//        if (!"已确认".equals(coopBill.getBillStatus())) {
//            throw new RuntimeException("只有已确认的账单才可以开发票");
//        }
        /*if ("未操作".equals(coopBill.getBillStatus())|| "已核销".equals(coopBill.getBillStatus())|| "已填充".equals(coopBill.getBillStatus())) {
    		throw new RuntimeException("当前账单状态不可以数据填充");
    	}*/
        coopBillMapper.invoice(statement_id, invoiceNo, acturalCharge, new Date(), SecurityUtils.getUser().getId(), SecurityUtils.getUser().getUserCname(), SecurityUtils.getUser().getUserCname() + " " + SecurityUtils.getUser().getUserEmail(),
                invoiceTitle, invoiceType, invoiceRemark, expressCompany, expressNumber, invoiceDate);

        try {
            LogBean bean = new LogBean();
            bean.setOrg_id(SecurityUtils.getUser().getOrgId());
            bean.setDept_id(SecurityUtils.getUser().getDeptId());
            bean.setCreator_id(SecurityUtils.getUser().getId());
            bean.setCreate_time(new Date());
            bean.setCreator_name("");
            bean.setOp_type("账单开发票");
            bean.setOp_level("高");
            bean.setOp_name("翌飞账单");
            bean.setOp_info("账单号" + statement_id + "已开发票");
            logService.doSave(bean);
        } catch (Exception e) {
            throw new RuntimeException("翌飞账单开发票成功,日志更新失败!");
        }
    }

    @Override
    public void sendInvoiceEmail(SendInvoiceEmail sendInvoiceEmail) throws Exception {
        sendInvoiceEmail.checkRequired();
        mailSendService.sendHtmlMailNew(true,sendInvoiceEmail.toUserEmails(), sendInvoiceEmail.ccUserEmails(), null,sendInvoiceEmail.getSubject(), sendInvoiceEmail.getContent().replaceAll("\n", "<br />"), null);
        System.out.println(sendInvoiceEmail);
    }

    @Override
    public void verify(Integer statement_id, Double acturalCharge, String invoiceWriteoffDate) throws ParseException {
       /* CoopBill coopBill = coopBillMapper.selectById(statement_id);
        if (coopBill == null) {
            throw new RuntimeException("该账单不存在");
        }
        if (!"已开票".equals(coopBill.getBillStatus())) {
            throw new RuntimeException("只有已开票的账单才可以核销");
        }*/
        SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        coopBillMapper.verify(statement_id, sDateFormat.parse(invoiceWriteoffDate), acturalCharge, SecurityUtils.getUser().getId(), SecurityUtils.getUser().getUserCname(), SecurityUtils.getUser().getUserCname() + " " + SecurityUtils.getUser().getUserEmail(), new Date());
        try {
            LogBean bean = new LogBean();
            bean.setOrg_id(SecurityUtils.getUser().getOrgId());
            bean.setDept_id(SecurityUtils.getUser().getDeptId());
            bean.setCreator_id(SecurityUtils.getUser().getId());
            bean.setCreate_time(new Date());
            bean.setCreator_name("");
            bean.setOp_type("账单核销");
            bean.setOp_level("高");
            bean.setOp_name("翌飞账单");
            bean.setOp_info("账单号" + statement_id + "已核销");
            logService.doSave(bean);
        } catch (Exception e) {
            throw new RuntimeException("翌飞账单核销成功,日志更新失败!");
        }
    }


    @Override
    public Map<String, Object> getPage(Integer current, Integer size, CoopBill coopBill) {
        Page<CoopBill> page = PageHelper.startPage(current, size);
        coopBill.setOrgId(SecurityUtils.getUser().getOrgId());
        List<CoopBill> list = coopBillMapper.selectList(coopBill);
        long total = page.getTotal();
        HashMap<String, Object> resultMap = new HashMap<>();
        resultMap.put("records", list);
        resultMap.put("total", total);
        return resultMap;
    }

    @Override
    public Map<String, Object> getMadeBillList(Integer current, Integer size, CoopBillStatement coopBillStatement) {
        Page<CoopBillStatement> page = PageHelper.startPage(current, size);
        coopBillStatement.setOrgId(SecurityUtils.getUser().getOrgId());
        if (!"".equals(coopBillStatement.getCoopName())) {
            coopBillStatement.setCoopName(coopBillStatement.getCoopName().toUpperCase());
        }
        if (!"".equals(coopBillStatement.getConfirmCustomerTime_begin())) {
            coopBillStatement.setConfirmCustomerTime_begin(coopBillStatement.getConfirmCustomerTime_begin() + " 00:00:00");
        }
        if (!"".equals(coopBillStatement.getConfirmCustomerTime_end())) {
            coopBillStatement.setConfirmCustomerTime_end(coopBillStatement.getConfirmCustomerTime_end() + " 23:59:59");
        }
        List<CoopBillStatement> list = coopBillMapper.getMadeBillList(coopBillStatement);
        /*if(list != null && list.size()>0){
            for(int i=0;i<list.size();i++){
                CoopAgreementSettlement cas= new CoopAgreementSettlement();
                cas.setOrgId(SecurityUtils.getUser().getOrgId());
                cas.setSettlementId(list.get(i).getSettlementId());
                //查询对账联系人
                String billConfirmContacts = coopBillMapper.findBillConfirmContacts(cas);
                list.get(i).setInvoiceReceiveEmail(billConfirmContacts);
            }
        }*/
        long total = page.getTotal();
        HashMap<String, Object> resultMap = new HashMap<>();
        resultMap.put("records", list);
        resultMap.put("total", total);
        return resultMap;
    }

    @Override
    public List<CoopBillStatement> getTotalMadeBill(CoopBillStatement coopBillStatement) {
        coopBillStatement.setOrgId(SecurityUtils.getUser().getOrgId());
        if(!"".equals(coopBillStatement.getCoopName())){
            coopBillStatement.setCoopName(coopBillStatement.getCoopName().toUpperCase());
        }
        if(!"".equals(coopBillStatement.getConfirmCustomerTime_begin())){
            coopBillStatement.setConfirmCustomerTime_begin(coopBillStatement.getConfirmCustomerTime_begin() + " 00:00:00");
        }
        if(!"".equals(coopBillStatement.getConfirmCustomerTime_end())){
            coopBillStatement.setConfirmCustomerTime_end(coopBillStatement.getConfirmCustomerTime_end() + " 23:59:59");
        }
        List<CoopBillStatement> list = coopBillMapper.getMadeBillList(coopBillStatement);

        List<CoopBillStatement> tatolList = new ArrayList<CoopBillStatement>();
        CoopBillStatement order = new CoopBillStatement();
        BigDecimal invoiceAmountTotal = new BigDecimal(0);
        if(list!=null && list.size()>0){
            for (int i = 0; i < list.size(); i++) {
                //计算账单金额合计
                CoopBillStatement cbs = list.get(i);
                if (cbs.getInvoiceAmount() != null) {
                    invoiceAmountTotal = invoiceAmountTotal.add(BigDecimal.valueOf(cbs.getInvoiceAmount()));
                }
            }
        }
        order.setInvoiceAmountTotal(invoiceAmountTotal);
        tatolList.add(order);
        return tatolList;
    }

    /*@Override
    public List<CoopBillGroup> getUnmakeBillList(CoopBillGroup coopBillGroup) {
        ArrayList<CoopBillGroup> resultTree = new ArrayList<>();
        //根据账单月份、客商资料 进行分组
        coopBillGroup.setOrgId(SecurityUtils.getUser().getOrgId());
        List<CoopBillGroup> coopBills=coopBillMapper.selectByGroup(coopBillGroup);
        if(coopBills!=null && coopBills.size()>0){
            for(int i=0;i<coopBills.size();i++){
                CoopBillGroup cb=coopBills.get(i);
                if(cb!=null){
                    //根据账单月份、客商资料查询账单
                    cb.setOrgId(SecurityUtils.getUser().getOrgId());
                    cb.setBillName1(coopBillGroup.getBillName());
                    cb.setCoopName1(coopBillGroup.getCoopName());
                    cb.setBillConfirmName(coopBillGroup.getBillConfirmName());
                    cb.setBillNumber(coopBillGroup.getBillNumber());
                    cb.setQuantityConfirmName(coopBillGroup.getQuantityConfirmName());
                    cb.setSettlementModName(coopBillGroup.getSettlementModName());
                    List<CoopBillGroupDetail> coopBillGroupDetails =coopBillMapper.selectByMonthAndCoopName(cb);
                    cb.setCoopBillGroupDetails(coopBillGroupDetails);
                    resultTree.add(cb);
                }
            }
        }
        System.out.println(resultTree);
        return resultTree;
    }*/

    @Override
    public List<CoopBillGroup> getUnmakeBillList(CoopBillGroupMerge coopBillGroupMerge) {
        ArrayList<CoopBillGroupMerge> resultTree = new ArrayList<>();
        //先一起查询分组和明细
        coopBillGroupMerge.setOrgId(SecurityUtils.getUser().getOrgId());
        List<CoopBillGroupMerge> coopBills = coopBillMapper.selectByMerge(coopBillGroupMerge);
        List<CoopBillGroup> cbgs = new ArrayList<>();
        if (coopBills != null && coopBills.size() > 0) {
            for (int i = 0; i < coopBills.size(); i++) {
                CoopBillGroupMerge cbgm = coopBills.get(i);
                if (cbgm != null && cbgm.getBillId() == 0) {//是分组的组头
                    CoopBillGroup cbg = new CoopBillGroup();
                    cbg.setCoopId(cbgm.getCoopId());
                    cbg.setCoopName(cbgm.getCoopName());
                    cbg.setBillName(cbgm.getBillName());
                    cbg.setCoopNameAndBillName(cbgm.getCoopNameAndBillName());
                    cbg.setBillId(UUID.randomUUID().toString());
                    // cbg.setHasChildren(true);
                    cbgs.add(cbg);
                }
            }
            if (cbgs != null && cbgs.size() > 0) {
                for (int j = 0; j < cbgs.size(); j++) {
                    String coopNameAndBillName = cbgs.get(j).getCoopNameAndBillName();
                    List<CoopBillGroupDetail> cbgds = new ArrayList<>();//存储子集
                    for (int i = 0; i < coopBills.size(); i++) {
                        CoopBillGroupMerge cbgm = coopBills.get(i);
                        if (cbgm != null && cbgm.getBillId() != 0 && cbgm.getCoopNameAndBillName().equals(coopNameAndBillName)) {//是子集
                            CoopBillGroupDetail cbgd = new CoopBillGroupDetail();
                            cbgd.setActuralCharge(cbgm.getActuralCharge());
                            cbgd.setDiscount(cbgm.getDiscount());
                            cbgd.setPlanCharge(cbgm.getPlanCharge());
                            cbgd.setBillConfirmName(cbgm.getBillConfirmName());
                            cbgd.setBillId(cbgm.getBillId());
                            cbgd.setBillName(cbgm.getBillName());
                            cbgd.setBillNumber(cbgm.getBillNumber());
                            cbgd.setBillStatus(cbgm.getBillStatus());
                            cbgd.setCoopId(cbgm.getCoopId());
                            cbgd.setCoopName(cbgm.getSettlementModName());
                            cbgd.setCoopNameAndBillName(cbgm.getCoopNameAndBillName());
                            cbgd.setFillNumber(cbgm.getFillNumber());
                            cbgd.setQuantityConfirmName(cbgm.getQuantityConfirmName());
                            cbgd.setSettlementId(cbgm.getSettlementId());
                            cbgd.setSettlementModName(cbgm.getSettlementModName());
                            cbgd.setPaymentMethod(cbgm.getPaymentMethod());
                            cbgd.setSettlementPeriod(cbgm.getSettlementPeriod());
                            cbgd.setSettlementType(cbgm.getSettlementType());
                            cbgd.setCoopName1(cbgm.getCoopName());
                            cbgd.setFillName(cbgm.getFillName());
                            cbgd.setFillUrl(cbgm.getFillUrl());
                            // cbgd.setHasChildren(true);
                            cbgds.add(cbgd);
                        }
                    }
                    cbgs.get(j).setCoopBillGroupDetails(cbgds);
                }
            }
        }
        System.out.println(cbgs);
        return cbgs;
    }

    @Override
    public List<CoopBill> checkBillByStatementId(Integer statement_id) {
        List<CoopBill> coopBills = coopBillMapper.checkBillByStatementId(statement_id);
        return coopBills;
    }

    @Override
    public List<BillServiceGroup> searchBillService(BillServiceGroup billServiceGroup) {
        //先查询分组
        List<BillServiceGroup> coopBillGroups = coopBillMapper.searchBillServiceGroup(billServiceGroup);
        if (coopBillGroups != null && coopBillGroups.size() > 0) {
            for (int i = 0; i < coopBillGroups.size(); i++) {
                BillServiceGroup billServiceGroup2 = new BillServiceGroup();
                billServiceGroup2.setServiceCode(coopBillGroups.get(i).getServiceCode());
                if (billServiceGroup.getIsValid1() != null ) {
                    billServiceGroup2.setIsValid1(billServiceGroup.getIsValid1());
                }
                if (billServiceGroup.getIsValid1() == 0) {//查询条件是无效
                    if (coopBillGroups.get(i).getIsValid() == 1) {//组有效
                        billServiceGroup2.setIsDetailAll(1);
                    } else {//组无效
                        billServiceGroup2.setIsDetailAll(2);
                    }
                }
                List<BillServiceDetail> fds = coopBillMapper.getServiceDetailByServiceCode(billServiceGroup2);
                coopBillGroups.get(i).setBillServiceDetails(fds);
            }
        }
        return coopBillGroups;
    }

    @Override
    public List<BillServiceGroup> searchBillServiceSelect() {
        //先查询分组
        BillServiceGroup billServiceGroup1 = new BillServiceGroup();
        billServiceGroup1.setIsValid1(1);
        List<BillServiceGroup> coopBillGroups = coopBillMapper.searchBillServiceGroup(billServiceGroup1);
        if (coopBillGroups != null && coopBillGroups.size() > 0) {
            for (int i = 0; i < coopBillGroups.size(); i++) {
                BillServiceGroup billServiceGroup = coopBillGroups.get(i);
                billServiceGroup.setIsValid1(1);
                List<BillServiceDetail> fds = coopBillMapper.getServiceDetailByServiceCode(billServiceGroup);
                coopBillGroups.get(i).setBillServiceDetails(fds);
            }
        }
        return coopBillGroups;
    }

    @Override
    public void addService(BillServiceGroup billServiceGroup) {
        billServiceGroup.setCreatorId(SecurityUtils.getUser().getId());
        billServiceGroup.setCreateTime(new Date());
        billServiceGroup.setCreatorName(SecurityUtils.getUser().getUserCname() + " " + SecurityUtils.getUser().getUserEmail());
        if (billServiceGroup.getServiceCodeGroup() != null && !"".equals(billServiceGroup.getServiceCodeGroup()) && !"undefined".equals(billServiceGroup.getServiceCodeGroup())) {//新增组成员
            String serviceCodeDetail = coopBillMapper.getMaxServiceCodeDetail(billServiceGroup.getServiceCodeGroup());
            if (serviceCodeDetail != null && !"".equals(serviceCodeDetail)) {
                String serviceCode1 = "";
                if (Integer.parseInt(serviceCodeDetail.substring(3, 6)) + 1 < 10) {
                    serviceCode1 = billServiceGroup.getServiceCodeGroup() + "00" + (Integer.parseInt(serviceCodeDetail.substring(3, 6)) + 1);
                } else if (Integer.parseInt(serviceCodeDetail.substring(3, 6)) + 1 >= 10 && Integer.parseInt(serviceCodeDetail.substring(3, 6)) + 1 < 100) {
                    serviceCode1 = billServiceGroup.getServiceCodeGroup() + "0" + (Integer.parseInt(serviceCodeDetail.substring(3, 6)) + 1);
                } else {
                    serviceCode1 = billServiceGroup.getServiceCodeGroup() + (Integer.parseInt(serviceCodeDetail.substring(3, 6)) + 1) + "";
                }
                billServiceGroup.setServiceCode(serviceCode1);
            } else {
                billServiceGroup.setServiceCode(billServiceGroup.getServiceCodeGroup() + "001");
            }
        } else {//增加组
            String serviceCode = coopBillMapper.getMaxServiceCode();
            if (serviceCode != null && !"".equals(serviceCode)) {
                String serviceCode1 = "";
                if (Integer.parseInt(serviceCode) + 1 < 10) {
                    serviceCode1 = "00" + (Integer.parseInt(serviceCode) + 1);
                } else if (Integer.parseInt(serviceCode) + 1 >= 10 && Integer.parseInt(serviceCode) + 1 < 100) {
                    serviceCode1 = "0" + (Integer.parseInt(serviceCode) + 1);
                } else {
                    serviceCode1 = (Integer.parseInt(serviceCode) + 1) + "";
                }
                billServiceGroup.setServiceCode(serviceCode1);
            } else {
                billServiceGroup.setServiceCode("001");
            }
        }
        coopBillMapper.addService(billServiceGroup);
    }

    @Override
    public Integer isHaveServiceClass(BillServiceGroup billServiceGroup) {
        Integer serviceCount = coopBillMapper.getServiceCountByServiceName(billServiceGroup);
        return serviceCount;
    }

    @Override
    public Integer isHaveServiceProject(BillServiceGroup billServiceGroup) {
        if (billServiceGroup.getServiceCode() != null && !"".equals(billServiceGroup.getServiceCode())) {
            billServiceGroup.setServiceCodeGroup(billServiceGroup.getServiceCode().substring(0, 3));
        }
        Integer serviceCount = coopBillMapper.getServiceProjectCountByServiceName(billServiceGroup);
        return serviceCount;
    }

    @Override
    public void editService(BillServiceGroup billServiceGroup) {
        billServiceGroup.setEditorId(SecurityUtils.getUser().getId());
        billServiceGroup.setEditTime(new Date());
        billServiceGroup.setEditorName(SecurityUtils.getUser().getUserCname() + " " + SecurityUtils.getUser().getUserEmail());

        coopBillMapper.editService(billServiceGroup);
    }

    /**
     * 有条件查询列表数据
     *
     * @param coopUnConfirmBillDetail
     * @param currentPage
     * @param pageSize
     * @return
     */
    @Override
    public Map<String, Object> searchUnConfirmBill(CoopUnConfirmBillDetail coopUnConfirmBillDetail, Integer currentPage, Integer pageSize) {
        Page<CoopUnConfirmBillGroup> page = PageHelper.startPage(currentPage, pageSize);
        coopUnConfirmBillDetail.setOrgId(SecurityUtils.getUser().getOrgId());
        String billStatus = "";
        if (coopUnConfirmBillDetail.getBillStatus1() != null && coopUnConfirmBillDetail.getBillStatus1().size() > 0) {
            for (int i = 0; i < coopUnConfirmBillDetail.getBillStatus1().size(); i++) {
                billStatus = billStatus + "'" + coopUnConfirmBillDetail.getBillStatus1().get(i) + "',";
            }
        }
        if (!"".equals(billStatus)) {
            billStatus = billStatus.substring(0, billStatus.length() - 1);
        }
        coopUnConfirmBillDetail.setBillStatus(billStatus);
        List<CoopUnConfirmBillGroup> paramList = coopBillMapper.searchUnConfirmBillGroup(coopUnConfirmBillDetail);
        //根据statement_id查询子级
        for (int i = 0; i < paramList.size(); i++) {
            paramList.get(i).setId(UUID.randomUUID().toString());
            HashMap<String, Object> serachMap = new HashMap<>();
            serachMap.put("orgId", SecurityUtils.getUser().getOrgId());
            serachMap.put("statementId", paramList.get(i).getStatementId());
            CoopUnConfirmBillGroup cas = paramList.get(i);
            List<CoopUnConfirmBillDetail> coopUnConfirmBillDetailList = coopBillMapper.findCoopUnConfirmBillDetail(serachMap);
            if (coopUnConfirmBillDetailList != null && coopUnConfirmBillDetailList.size() > 0) {
                for (int j = 0; j < coopUnConfirmBillDetailList.size(); j++) {
                    coopUnConfirmBillDetailList.get(j).setId(UUID.randomUUID().toString());
                    coopUnConfirmBillDetailList.get(j).setStatementStatus(paramList.get(i).getBillStatus());//账单明细也设置下账单的状态，为了备注的显示

                    if ("待总部确认".equals(paramList.get(i).getBillStatus())) {
                        String remark = "";
                        if (coopUnConfirmBillDetailList.get(j).getFillNumber() != null && !coopUnConfirmBillDetailList.get(j).getFillNumber().equals(coopUnConfirmBillDetailList.get(j).getFillNumberOriginal())) {
                            DecimalFormat df = new DecimalFormat(",###,##0");
                            String fillNumberOriginal = "";
                            if (!"".equals(coopUnConfirmBillDetailList.get(j).getFillNumberOriginal()) && coopUnConfirmBillDetailList.get(j).getFillNumberOriginal() != null) {
                                fillNumberOriginal = df.format(coopUnConfirmBillDetailList.get(j).getFillNumberOriginal());
                            }
                            String fillNumber = "";
                            if (!"".equals(coopUnConfirmBillDetailList.get(j).getFillNumber())) {
                                fillNumber = df.format(coopUnConfirmBillDetailList.get(j).getFillNumber());
                            }
                            remark += "数量：" + fillNumberOriginal + "调整为" + fillNumber + ",";
                        }
                        if (coopUnConfirmBillDetailList.get(j).getActuralCharge() != null && !coopUnConfirmBillDetailList.get(j).getActuralCharge().equals(coopUnConfirmBillDetailList.get(j).getOriginalCharge())) {
                            DecimalFormat df = new DecimalFormat(",###,##0.00");
                            String originalCharge = "";
                            if (!"".equals(coopUnConfirmBillDetailList.get(j).getOriginalCharge()) && coopUnConfirmBillDetailList.get(j).getOriginalCharge() != null) {
                                originalCharge = df.format(coopUnConfirmBillDetailList.get(j).getOriginalCharge());
                            }
                            String acturalCharge = "";
                            if (!"".equals(coopUnConfirmBillDetailList.get(j).getActuralCharge())) {
                                acturalCharge = df.format(coopUnConfirmBillDetailList.get(j).getActuralCharge());
                            }
                            remark += "金额：" + originalCharge + "调整为" + acturalCharge;
                        }

                        coopUnConfirmBillDetailList.get(j).setRemark(remark);
                    }
                }
            }
            paramList.get(i).setCoopUnConfirmBillDetail(coopUnConfirmBillDetailList);
        }
        Integer totalNum = Integer.parseInt(page.getTotal() + "");
        HashMap<String, Object> resultMap = new HashMap<>();
        resultMap.put("totalNum", totalNum);
        resultMap.put("paramList", paramList);
        return resultMap;
    }

    /**
     * 有条件查询列表数据
     *
     * @param coopUnConfirmBillDetail
     * @param currentPage
     * @param pageSize
     * @return
     */
    @Override
    public Map<String, Object> searchUnConfirmBill_detail(CoopUnConfirmBillDetail coopUnConfirmBillDetail, Integer currentPage, Integer pageSize) {
        Page<CoopUnConfirmBillGroup> page = PageHelper.startPage(currentPage, pageSize);
        coopUnConfirmBillDetail.setOrgId(SecurityUtils.getUser().getOrgId());
        List<CoopUnConfirmBillGroup> paramList = coopBillMapper.searchUnConfirmBillGroup_detail(coopUnConfirmBillDetail);
        //根据statement_id查询子级
        if (paramList != null && paramList.size() > 0) {
            for (int i = 0; i < paramList.size(); i++) {
                paramList.get(i).setId(UUID.randomUUID().toString());
                HashMap<String, Object> serachMap = new HashMap<>();
                serachMap.put("orgId", SecurityUtils.getUser().getOrgId());
                serachMap.put("statementId", paramList.get(i).getStatementId());

                if (coopUnConfirmBillDetail.getBillName() != null && !"".equals(coopUnConfirmBillDetail.getBillName())) {
                    serachMap.put("billName", coopUnConfirmBillDetail.getBillName());
                }
                if (coopUnConfirmBillDetail.getServiceName() != null && !"".equals(coopUnConfirmBillDetail.getServiceName())) {
                    serachMap.put("serviceName", coopUnConfirmBillDetail.getServiceName());
                }
                if (coopUnConfirmBillDetail.getBillConfirmName() != null && !"".equals(coopUnConfirmBillDetail.getBillConfirmName())) {
                    serachMap.put("billConfirmName", coopUnConfirmBillDetail.getBillConfirmName());
                }
                if (coopUnConfirmBillDetail.getQuantityConfirmName() != null && !"".equals(coopUnConfirmBillDetail.getQuantityConfirmName())) {
                    serachMap.put("quantityConfirmName", coopUnConfirmBillDetail.getQuantityConfirmName());
                }
                if (coopUnConfirmBillDetail.getBillStatus() != null && !"".equals(coopUnConfirmBillDetail.getBillStatus())) {
                    serachMap.put("billStatus", coopUnConfirmBillDetail.getBillStatus());
                }
                if (coopUnConfirmBillDetail.getItCode() != null && !"".equals(coopUnConfirmBillDetail.getItCode())) {
                    serachMap.put("itCode", coopUnConfirmBillDetail.getItCode());
                }
                CoopUnConfirmBillGroup cas = paramList.get(i);
                List<CoopUnConfirmBillDetail> coopUnConfirmBillDetailList = coopBillMapper.findCoopUnConfirmBillDetail_detail(serachMap);
                if (coopUnConfirmBillDetailList != null && coopUnConfirmBillDetailList.size() > 0) {
                    for (int j = 0; j < coopUnConfirmBillDetailList.size(); j++) {
                        coopUnConfirmBillDetailList.get(j).setId(UUID.randomUUID().toString());
                        coopUnConfirmBillDetailList.get(j).setIsSendMailAuto(paramList.get(i).getIsSendMailAuto());
                    }
                    paramList.get(i).setCoopUnConfirmBillDetail(coopUnConfirmBillDetailList);
                } else {
                    paramList.remove(i);
                    i = i - 1;
                }

            }
        }
        Integer totalNum = Integer.parseInt(page.getTotal() + "");
        HashMap<String, Object> resultMap = new HashMap<>();
        resultMap.put("totalNum", totalNum);
        resultMap.put("paramList", paramList);
        return resultMap;
    }

    /**
     * 有条件查询结算报表
     *
     * @param
     * @param currentPage
     * @param pageSize
     * @return
     */
    @Override
    public Map<String, Object> searchCoopBillSettleList(CoopBillSettle coopBillSettle, Integer currentPage, Integer pageSize) {
        Page<CoopBillSettle> page = PageHelper.startPage(currentPage, pageSize);
        HashMap<String, Object> paramMap = new HashMap<>();
        paramMap.put("orgId", SecurityUtils.getUser().getOrgId());
        paramMap.put("coopName", coopBillSettle.getCoopName());
        paramMap.put("billTemplate", coopBillSettle.getBillTemplate());
        paramMap.put("departureStation", coopBillSettle.getDepartureStation());
        paramMap.put("billConfirmName", coopBillSettle.getBillConfirmName());
        paramMap.put("saleConfirmName", coopBillSettle.getSaleConfirmName());
        paramMap.put("statementStatus", coopBillSettle.getStatementStatus());
        paramMap.put("statementDate_begin", coopBillSettle.getStatementDate_begin());
        paramMap.put("statementDate_end", coopBillSettle.getStatementDate_end());
        paramMap.put("validBeginDate", coopBillSettle.getValidBeginDate());
        paramMap.put("validEndDate", coopBillSettle.getValidEndDate());
        paramMap.put("invoiceWriteoffDateBegin", coopBillSettle.getInvoiceWriteoffDateBegin());
        paramMap.put("invoiceWriteoffDateEnd", coopBillSettle.getInvoiceWriteoffDateEnd());
        paramMap.put("paymentMethod", coopBillSettle.getPaymentMethod());
        paramMap.put("isNewBusiness", coopBillSettle.getIsNewBusiness());
        if (coopBillSettle.getServiceNameOne() != null && !"".equals(coopBillSettle.getServiceNameOne()))
            paramMap.put("serviceNameOne", coopBillSettle.getServiceNameOne().split(","));
        if (coopBillSettle.getServiceNameTwo() != null && !"".equals(coopBillSettle.getServiceNameTwo()))
            paramMap.put("serviceNameTwo", coopBillSettle.getServiceNameTwo().split(","));

        List<CoopBillSettle> paramList = coopBillMapper.searchCoopBillSettleList(paramMap);
        Integer totalNum = Integer.parseInt(page.getTotal() + "");
        HashMap<String, Object> resultMap = new HashMap<>();
        resultMap.put("totalNum", totalNum);
        resultMap.put("paramList", paramList);
        return resultMap;
    }

    @Override
    public List<CoopBillSettle> getTotalSettle(CoopBillSettle coopBillSettle) {

        HashMap<String, Object> paramMap = new HashMap<>();
        paramMap.put("orgId", SecurityUtils.getUser().getOrgId());
        paramMap.put("coopName", coopBillSettle.getCoopName());
        paramMap.put("billTemplate", coopBillSettle.getBillTemplate());
        paramMap.put("departureStation", coopBillSettle.getDepartureStation());
        paramMap.put("billConfirmName", coopBillSettle.getBillConfirmName());
        paramMap.put("saleConfirmName", coopBillSettle.getSaleConfirmName());
        paramMap.put("statementStatus", coopBillSettle.getStatementStatus());
        paramMap.put("statementDate_begin", coopBillSettle.getStatementDate_begin());
        paramMap.put("statementDate_end", coopBillSettle.getStatementDate_end());
        paramMap.put("validBeginDate", coopBillSettle.getValidBeginDate());
        paramMap.put("validEndDate", coopBillSettle.getValidEndDate());
        paramMap.put("invoiceWriteoffDateBegin", coopBillSettle.getInvoiceWriteoffDateBegin());
        paramMap.put("invoiceWriteoffDateEnd", coopBillSettle.getInvoiceWriteoffDateEnd());
        paramMap.put("paymentMethod", coopBillSettle.getPaymentMethod());
        paramMap.put("isNewBusiness", coopBillSettle.getIsNewBusiness());
        if (coopBillSettle.getServiceNameOne() != null && !"".equals(coopBillSettle.getServiceNameOne()))
            paramMap.put("serviceNameOne", coopBillSettle.getServiceNameOne().split(","));
        if (coopBillSettle.getServiceNameTwo() != null && !"".equals(coopBillSettle.getServiceNameTwo()))
            paramMap.put("serviceNameTwo", coopBillSettle.getServiceNameTwo().split(","));

        List<CoopBillSettle> list = coopBillMapper.searchCoopBillSettleList(paramMap);
        List<CoopBillSettle> tatolList = new ArrayList<CoopBillSettle>();
        CoopBillSettle order = new CoopBillSettle();
        BigDecimal acturalChargeTotal = new BigDecimal(0);
        BigDecimal amountReceivedTotal = new BigDecimal(0);
        HashMap<Integer, BigDecimal> resultMap = new HashMap<>();
        if (list != null && list.size() > 0) {
            for (int i = 0; i < list.size(); i++) {
                //计算实收金额合计
                CoopBillSettle afOrder = list.get(i);
                if (afOrder.getActuralCharge() != null) {
                    acturalChargeTotal = acturalChargeTotal.add(afOrder.getActuralCharge());
                }
                //账单实收金额合计去重
                // resultMap.put(afOrder.getStatementId(),afOrder.getAmountReceived());
            }
            //计算账单实收金额合计
            /*for(BigDecimal val : resultMap.values()) {
                if(val!=null){
                    amountReceivedTotal = amountReceivedTotal.add(val);
                }
            }*/
        }

        order.setActuralChargeTotal(acturalChargeTotal);
        order.setAmountReceivedTotal(amountReceivedTotal);
        tatolList.add(order);
        return tatolList;
    }

    @Override
    public List<CoopBillSettleExcel> queryListForExcel(CoopBillSettle bean) {
        HashMap<String, Object> paramMap = new HashMap<>();
        paramMap.put("orgId", SecurityUtils.getUser().getOrgId());
        paramMap.put("coopName", bean.getCoopName());
        paramMap.put("billTemplate", bean.getBillTemplate());
        paramMap.put("departureStation", bean.getDepartureStation());
        paramMap.put("billConfirmName", bean.getBillConfirmName());
        paramMap.put("saleConfirmName", bean.getSaleConfirmName());
        paramMap.put("statementStatus", bean.getStatementStatus());
        paramMap.put("statementDate_begin", bean.getStatementDate_begin());
        paramMap.put("statementDate_end", bean.getStatementDate_end());
        paramMap.put("validBeginDate", bean.getValidBeginDate());
        paramMap.put("validEndDate", bean.getValidEndDate());
        paramMap.put("invoiceWriteoffDateBegin", bean.getInvoiceWriteoffDateBegin());
        paramMap.put("invoiceWriteoffDateEnd", bean.getInvoiceWriteoffDateEnd());
        paramMap.put("paymentMethod", bean.getPaymentMethod());
        paramMap.put("isNewBusiness", bean.getIsNewBusiness());
        if (bean.getServiceNameOne() != null && !"".equals(bean.getServiceNameOne()))
            paramMap.put("serviceNameOne", bean.getServiceNameOne().split(","));
        if (bean.getServiceNameTwo() != null && !"".equals(bean.getServiceNameTwo()))
            paramMap.put("serviceNameTwo", bean.getServiceNameTwo().split(","));

        List<CoopBillSettleExcel> list = coopBillMapper.queryListForExcel(paramMap);

        BigDecimal acturalChargeTotal = new BigDecimal(0);
        if (list != null && list.size() > 0) {
            for (int i = 0; i < list.size(); i++) {
                //计算实收金额合计
                CoopBillSettleExcel afOrder = list.get(i);
                if (afOrder.getActuralCharge1() != null) {
                    String acturalCharge1 = afOrder.getActuralCharge1().replaceAll(",", "");
                    BigDecimal bd = new BigDecimal(acturalCharge1);
                    acturalChargeTotal = acturalChargeTotal.add(bd);
                }
            }
        }
        CoopBillSettleExcel cse = new CoopBillSettleExcel();
        DecimalFormat df = new DecimalFormat(",###,##0.00");
        df.format(acturalChargeTotal);
        cse.setActuralCharge1(df.format(acturalChargeTotal));
        cse.setBillNumber("合计");
        list.add(cse);

        return list;
    }

    @Override
    public List<CoopServiceBean> queryServiceIsValid() {
        return coopBillMapper.queryServiceIsValid();
    }

    @Override
    public List<CoopServiceBean> queryServiceTwoIsValid() {
        return coopBillMapper.queryServiceTwoIsValid();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void refuseConfirmBill(CoopUnConfirmBillGroup coopUnConfirmBillGroup) {
        //修改状态为“数据已填充
        coopUnConfirmBillGroup.setEditorId(SecurityUtils.getUser().getId());
        coopUnConfirmBillGroup.setEditTime(new Date());
        coopUnConfirmBillGroup.setEditorName(SecurityUtils.getUser().getUserCname() + " " + SecurityUtils.getUser().getUserEmail());
        coopUnConfirmBillGroup.setStatementStatus("数据已填充");
        coopBillMapper.updateStatementByStatementId3(coopUnConfirmBillGroup);
        //查询销售确认人邮箱
        String confirmSalerEmail = coopBillMapper.getConfirmSalerEmailByStatementId(coopUnConfirmBillGroup);
        String subject = coopUnConfirmBillGroup.getBillName() + " " + coopUnConfirmBillGroup.getCoopName() + "(" + coopUnConfirmBillGroup.getServiceName() + ")：总部拒绝确认";
        StringBuilder builder = new StringBuilder();
        builder.append("账单月份：" + coopUnConfirmBillGroup.getBillName());
        builder.append("<br />");
        builder.append("账单名称：" + coopUnConfirmBillGroup.getCoopName() + "(" + coopUnConfirmBillGroup.getServiceName() + ")");
        builder.append("<br />");
        builder.append("备注：总部拒绝确认");
        mailSendService.sendHtmlMailNew(true,new String[]{confirmSalerEmail}, null,null, subject, builder.toString(), null);

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void confirmBill(CoopUnConfirmBillGroup coopUnConfirmBillGroup) {
        //更新 账单明细 的 结算金额2021-03-02添加
        //1.如果账单金额 = SUM(明细金额)
        if(coopUnConfirmBillGroup.getActuralCharge().equals(coopUnConfirmBillGroup.getActuralChargeAll())){
            coopBillMapper.updateBillByStatementId(coopUnConfirmBillGroup.getStatementId());
        }else{
            //根据账单ID查询相应的账单明细
            List<CoopUnConfirmBillDetail> detailList = coopBillMapper.getAllBillByStatementId(coopUnConfirmBillGroup.getStatementId());
            Double diff = subDouble(coopUnConfirmBillGroup.getActuralCharge(),coopUnConfirmBillGroup.getActuralChargeAll());
            if(detailList != null && detailList.size() == 1){
                CoopUnConfirmBillDetail det = detailList.get(0);
                det.setSettlementCharge(addDouble1(diff,det.getActuralCharge()));
                coopBillMapper.updateBillByBillId(det);
            }else if(detailList != null && detailList.size() > 1){
                CoopUnConfirmBillDetail det = detailList.get(0);
                det.setSettlementCharge(addDouble1(diff,det.getActuralCharge()));
                coopBillMapper.updateBillByBillId(det);
                //更新其他明细
                coopBillMapper.updateOtherBillByBillId(det);
            }
        }

        coopUnConfirmBillGroup.setEditorId(SecurityUtils.getUser().getId());
        coopUnConfirmBillGroup.setEditTime(new Date());
        coopUnConfirmBillGroup.setEditorName(SecurityUtils.getUser().getUserCname() + " " + SecurityUtils.getUser().getUserEmail());
        if (coopUnConfirmBillGroup.getBillStatus() == "待总部确认" || "待总部确认".equals(coopUnConfirmBillGroup.getBillStatus())) {//说明是总部确认账单操作

            //0金额账单不发送邮件，只更新状态
            if (coopUnConfirmBillGroup.getPlanCharge() == 0) {
                coopUnConfirmBillGroup.setStatementMailDate(new Date());
                coopUnConfirmBillGroup.setStatementMailSenderName(SecurityUtils.getUser().getUserCname() + " " + SecurityUtils.getUser().getUserEmail());
                coopUnConfirmBillGroup.setStatementMailSenderId(SecurityUtils.getUser().getId());
                coopUnConfirmBillGroup.setConfirmHeadOfficeTime(new Date());
                coopUnConfirmBillGroup.setConfirmHeadOfficeName(SecurityUtils.getUser().getUserCname() + " " + SecurityUtils.getUser().getUserEmail());
                coopUnConfirmBillGroup.setStatementStatus("账单已核销");
                coopBillMapper.updateStatementByStatementId1(coopUnConfirmBillGroup);
            } else {
                //更新账单状态
                coopUnConfirmBillGroup.setStatementMailDate(new Date());
                coopUnConfirmBillGroup.setStatementMailSenderName(SecurityUtils.getUser().getUserCname() + " " + SecurityUtils.getUser().getUserEmail());
                coopUnConfirmBillGroup.setStatementMailSenderId(SecurityUtils.getUser().getId());
                coopUnConfirmBillGroup.setConfirmHeadOfficeTime(new Date());
                coopUnConfirmBillGroup.setConfirmHeadOfficeName(SecurityUtils.getUser().getUserCname() + " " + SecurityUtils.getUser().getUserEmail());
                coopUnConfirmBillGroup.setStatementStatus("账单已发送");
                coopUnConfirmBillGroup.setMailSendTime(new Date());
                coopBillMapper.updateStatementByStatementId1(coopUnConfirmBillGroup);

                Map<String, Object> param = new HashMap<>();
                param.put("statement_id", coopUnConfirmBillGroup.getStatementId());
                List<List<CoopBillEmail>> list = coopBillMapper.getPdfFields(param);
                if (list != null && list.size() > 0) {
                    if (list.get(0) != null && list.get(0).size() > 0) {
                        //发送邮件
                        CoopBillEmail coopBillEmail = new CoopBillEmail();
                        List<CoopBillEmail> listDetail = new ArrayList<CoopBillEmail>();
                        if (list != null && list.size() > 0) {
                            //拼接PDF表头
                            List<CoopBillEmail> list1 = list.get(0);
                            coopBillEmail.setTxt_01(list1.get(0).getTxt_01());
                            coopBillEmail.setTxt_02(list1.get(0).getTxt_02());
                            coopBillEmail.setTxt_03(list1.get(0).getTxt_03());
                            coopBillEmail.setTxt_04(list1.get(0).getTxt_04());
                            coopBillEmail.setTxt_05(list1.get(0).getTxt_05());
                            coopBillEmail.setTxt_06(list1.get(0).getTxt_06());
                            coopBillEmail.setTxt_07(list1.get(0).getTxt_07());
                            coopBillEmail.setTxt_08(list1.get(0).getTxt_08());
                            coopBillEmail.setTxt_09(list1.get(0).getTxt_09());
                            coopBillEmail.setTxt_10(list1.get(0).getTxt_10());
                            coopBillEmail.setMail_cc(list1.get(0).getMail_cc());
                            coopBillEmail.setMail_to(list1.get(0).getMail_to());
                            coopBillEmail.setMailAttachment(list1.get(0).getMailAttachment());
                            coopBillEmail.setMailTitle(list1.get(0).getMailTitle());
                            coopBillEmail.setPrintTemplate(list1.get(0).getPrintTemplate());
                            coopBillEmail.setMailBody(list1.get(0).getMailBody());

                            //拼接账单明细
                            listDetail = list.get(1);
                        }
                        if (coopBillEmail != null) {
                            List<File> files = new ArrayList<File>();
                            //生成PDF并放入附件
                            String pdfPath = "";
                            try {
                                pdfPath = PDFUtils.fillTemplate(coopBillEmail, listDetail, parentPath);//生成PDF
                                File file = new File(pdfPath);
                                files.add(file);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            //是否有明细附件，如果有放入附件
                            String filePathMimeName = "";
                            ArrayList<Map<String, String>> fileList = new ArrayList<>();
                            Map<String, String> map = new HashMap<>();
                            map.put("path", PDFUtils.filePath + pdfPath);//账单路径放入map
                            map.put("name", pdfPath.substring(pdfPath.lastIndexOf("/") +1, pdfPath.length()));//账单附件名称放入map
                            map.put("flag", "local");
                            fileList.add(map);
                            //从明细中拿附件路径和名称
                            if (listDetail != null && listDetail.size() > 0) {
                                for (int i = 0; i < listDetail.size(); i++) {
                                    if (!StrUtil.isBlank(listDetail.get(i).getMailAttachmentUrl())) {
                                        String mailAttachmentUrl = listDetail.get(i).getMailAttachmentUrl();
                                        filePathMimeName = listDetail.get(i).getMailAttachmentName() + mailAttachmentUrl.substring(mailAttachmentUrl.lastIndexOf("."), mailAttachmentUrl.length());
                                        Map<String, String> map1 = new HashMap<>();
                                        map1.put("path", mailAttachmentUrl);//账单路径放入map
                                        map1.put("name", filePathMimeName);//账单附件名称放入map
                                        map1.put("flag", "upload");
                                        fileList.add(map1);
                                    }
                                }
                            }
                            //生成邮件
                            //生成收件人数组
                            String recipientEmails = coopBillEmail.getMail_to();
                            String[] recipientEmail = new String[0];
                            if (recipientEmails != null && !"".equals(recipientEmails)) {
                                recipientEmail = recipientEmails.split(",");
                            }
                            String title = coopBillEmail.getMailTitle();
                            String mailBody = coopBillEmail.getMailBody();
                            StringBuilder builder = new StringBuilder();
                            builder.append(mailBody);
                            //生成抄送人数组
                            String ccEmails = coopBillEmail.getMail_cc();
                            String[] ccEmail = null;
                            if (ccEmails != null && !"".equals(ccEmails)) {
                                ccEmail = ccEmails.split(",");
                            }
                            try {
                                mailSendService.sendAttachmentsMailNew(false, recipientEmail, ccEmail, null, title, builder.toString(), fileList,null);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
        } else {//说明是销售确认账单操作
            if ("1".equals(coopUnConfirmBillGroup.getIsModify())) {//更改了
                coopUnConfirmBillGroup.setConfirmSalerTime(new Date());
                coopUnConfirmBillGroup.setConfirmSalerName(SecurityUtils.getUser().getUserCname() + " " + SecurityUtils.getUser().getUserEmail());
                coopUnConfirmBillGroup.setStatementStatus("待总部确认");
                coopBillMapper.updateStatementByStatementId(coopUnConfirmBillGroup);
            } else {//没更改，直接发送账单
                //0金额账单不发送邮件，只更新状态
                if (coopUnConfirmBillGroup.getPlanCharge() == 0) {
                    //设置账单状态
                    coopUnConfirmBillGroup.setStatementMailDate(new Date());
                    coopUnConfirmBillGroup.setStatementMailSenderName(SecurityUtils.getUser().getUserCname() + " " + SecurityUtils.getUser().getUserEmail());
                    coopUnConfirmBillGroup.setStatementMailSenderId(SecurityUtils.getUser().getId());
                    coopUnConfirmBillGroup.setConfirmSalerTime(new Date());
                    coopUnConfirmBillGroup.setConfirmSalerName(SecurityUtils.getUser().getUserCname() + " " + SecurityUtils.getUser().getUserEmail());
                    coopUnConfirmBillGroup.setStatementStatus("账单已核销");
                    coopBillMapper.updateStatementByStatementId2(coopUnConfirmBillGroup);
                } else {
                    //设置账单状态
                    coopUnConfirmBillGroup.setStatementMailDate(new Date());
                    coopUnConfirmBillGroup.setStatementMailSenderName(SecurityUtils.getUser().getUserCname() + " " + SecurityUtils.getUser().getUserEmail());
                    coopUnConfirmBillGroup.setStatementMailSenderId(SecurityUtils.getUser().getId());
                    coopUnConfirmBillGroup.setConfirmSalerTime(new Date());
                    coopUnConfirmBillGroup.setConfirmSalerName(SecurityUtils.getUser().getUserCname() + " " + SecurityUtils.getUser().getUserEmail());
                    coopUnConfirmBillGroup.setStatementStatus("账单已发送");
                    coopUnConfirmBillGroup.setMailSendTime(new Date());
                    coopBillMapper.updateStatementByStatementId2(coopUnConfirmBillGroup);

                    Map<String, Object> param = new HashMap<>();
                    param.put("statement_id", coopUnConfirmBillGroup.getStatementId());
                    List<List<CoopBillEmail>> list = coopBillMapper.getPdfFields(param);
                    if (list != null && list.size() > 0) {
                        if (list.get(0) != null && list.get(0).size() > 0) {
                            //发送邮件
                            CoopBillEmail coopBillEmail = new CoopBillEmail();
                            List<CoopBillEmail> listDetail = new ArrayList<CoopBillEmail>();
                            if (list != null && list.size() > 0) {
                                //拼接PDF表头
                                List<CoopBillEmail> list1 = list.get(0);
                                coopBillEmail.setTxt_01(list1.get(0).getTxt_01());
                                coopBillEmail.setTxt_02(list1.get(0).getTxt_02());
                                coopBillEmail.setTxt_03(list1.get(0).getTxt_03());
                                coopBillEmail.setTxt_04(list1.get(0).getTxt_04());
                                coopBillEmail.setTxt_05(list1.get(0).getTxt_05());
                                coopBillEmail.setTxt_06(list1.get(0).getTxt_06());
                                coopBillEmail.setTxt_07(list1.get(0).getTxt_07());
                                coopBillEmail.setTxt_08(list1.get(0).getTxt_08());
                                coopBillEmail.setTxt_09(list1.get(0).getTxt_09());
                                coopBillEmail.setTxt_10(list1.get(0).getTxt_10());
                                coopBillEmail.setMail_cc(list1.get(0).getMail_cc());
                                coopBillEmail.setMail_to(list1.get(0).getMail_to());
                                coopBillEmail.setMailAttachment(list1.get(0).getMailAttachment());
                                coopBillEmail.setMailTitle(list1.get(0).getMailTitle());
                                coopBillEmail.setPrintTemplate(list1.get(0).getPrintTemplate());
                                coopBillEmail.setMailBody(list1.get(0).getMailBody());

                                //拼接账单明细
                                listDetail = list.get(1);
                            }
                            if (coopBillEmail != null) {
                                List<File> files = new ArrayList<File>();
                                //生成PDF并放入附件
                                String pdfPath = "";
                                try {
                                    pdfPath = PDFUtils.fillTemplate(coopBillEmail, listDetail, parentPath);//生成PDF
                                    File file = new File(pdfPath);
                                    files.add(file);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }

                                String filePathMimeName = "";
                                ArrayList<Map<String, String>> fileList = new ArrayList<>();
                                Map<String, String> map = new HashMap<>();
                                map.put("path", PDFUtils.filePath + pdfPath);//账单路径放入map
                                map.put("name", pdfPath.substring(pdfPath.lastIndexOf("/") +1, pdfPath.length()));//账单附件名称放入map
                                map.put("flag", "local");
                                fileList.add(map);
                                //是否有明细附件，如果有放入附件
                                if (listDetail != null && listDetail.size() > 0) {
                                    for (int i = 0; i < listDetail.size(); i++) {
                                        if (!StrUtil.isBlank(listDetail.get(i).getMailAttachmentUrl())) {
                                            String mailAttachmentUrl = listDetail.get(i).getMailAttachmentUrl();
                                            filePathMimeName = listDetail.get(i).getMailAttachmentName() + mailAttachmentUrl.substring(mailAttachmentUrl.lastIndexOf("."), mailAttachmentUrl.length());
                                            Map<String, String> map1 = new HashMap<>();
                                            map1.put("path", mailAttachmentUrl);//账单路径放入map
                                            map1.put("name", filePathMimeName);//账单附件名称放入map
                                            map1.put("flag", "upload");
                                            fileList.add(map1);
                                        }
                                    }
                                }
                                //生成邮件
                                //生成收件人数组
                                String recipientEmails = coopBillEmail.getMail_to();
                                String[] recipientEmail = new String[0];
                                if (recipientEmails != null && !"".equals(recipientEmails)) {
                                    recipientEmail = recipientEmails.split(",");
                                }
                                String title = coopBillEmail.getMailTitle();
                                String mailBody = coopBillEmail.getMailBody();
                                StringBuilder builder = new StringBuilder();
                                builder.append(mailBody);
                                //生成抄送人数组
                                String ccEmails = coopBillEmail.getMail_cc();
                                String[] ccEmail = null;
                                if (ccEmails != null && !"".equals(ccEmails)) {
                                    ccEmail = ccEmails.split(",");
                                }
                                try {
                                    mailSendService.sendAttachmentsMailNew(false, recipientEmail, ccEmail, null, title, builder.toString(), fileList,null);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public void updateAmountReceivable(CoopUnConfirmBillGroup coopUnConfirmBillGroup) {
        coopBillMapper.updateAmountReceivable(coopUnConfirmBillGroup);
    }

    @Override
    //@Transactional(rollbackFor = Exception.class)
    public void customerConfirmBill(Integer statementId,Integer settlementId) {
        CoopUnConfirmBillGroup coopUnConfirmBillGroup = new CoopUnConfirmBillGroup();
        coopUnConfirmBillGroup.setStatementId(statementId);
        coopUnConfirmBillGroup.setConfirmCustomerTime(new Date());
        coopUnConfirmBillGroup.setConfirmCustomerName(SecurityUtils.getUser().getUserCname() + " " + SecurityUtils.getUser().getUserEmail());
        coopUnConfirmBillGroup.setStatementStatus("客户已确认");
        coopUnConfirmBillGroup.setSettlementId(settlementId);
        coopBillMapper.customerConfirmBill(coopUnConfirmBillGroup);
        //如果 对应的模板 的 首次收费月份 =NULL，则 将 当前时间（精确到秒） 更新到 对应模板的 start_charge_time 字段；（暂时不要了）
        //coopBillMapper.updateSettlementBySettlementId(coopUnConfirmBillGroup);
    }

    @Override
    public String checkIfModify(CoopUnConfirmBillGroup coopUnConfirmBillGroup) {
        List<String> list = coopBillMapper.checkIfModify(coopUnConfirmBillGroup);
        String result = "";
        if (list != null && list.size() > 0) {
            for (int i = 0; i < list.size(); i++) {
                if ("modify".equals(list.get(i))) {
                    result = "modify";
                }
            }
        }
        return result;
    }

    @Override
    public String getRemarkByBillId(Integer billId) {
        HashMap<String, Object> serachMap = new HashMap<>();
        serachMap.put("orgId", SecurityUtils.getUser().getOrgId());
        serachMap.put("billId", billId);
        CoopUnConfirmBillDetail coopUnConfirmBillDetailList = coopBillMapper.getRemarkByBillId(serachMap);
        String remark = "";
        if (coopUnConfirmBillDetailList.getFillNumber() != null && !coopUnConfirmBillDetailList.getFillNumber().equals(coopUnConfirmBillDetailList.getFillNumberOriginal())) {
            DecimalFormat df = new DecimalFormat(",###,##0");
            String fillNumberOriginal = "";
            if (coopUnConfirmBillDetailList.getFillNumberOriginal() != null && !"".equals(coopUnConfirmBillDetailList.getFillNumberOriginal())) {
                fillNumberOriginal = df.format(coopUnConfirmBillDetailList.getFillNumberOriginal());
            }
            String fillNumber = "";
            if (coopUnConfirmBillDetailList.getFillNumber() != null && !"".equals(coopUnConfirmBillDetailList.getFillNumber())) {
                fillNumber = df.format(coopUnConfirmBillDetailList.getFillNumber());
            }
            remark += "数量：" + fillNumberOriginal + "调整为" + fillNumber + ",";


            // remark += "数量："+ coopUnConfirmBillDetailList.getFillNumberOriginal() + "调整为" + coopUnConfirmBillDetailList.getFillNumber() + ",";
        }
        if (coopUnConfirmBillDetailList.getActuralCharge() != null && !coopUnConfirmBillDetailList.getActuralCharge().equals(coopUnConfirmBillDetailList.getOriginalCharge())) {
            DecimalFormat df = new DecimalFormat(",###,##0.00");
            String originalCharge = "";
            if (coopUnConfirmBillDetailList.getOriginalCharge() != null && !"".equals(coopUnConfirmBillDetailList.getOriginalCharge())) {
                originalCharge = df.format(coopUnConfirmBillDetailList.getOriginalCharge());
            }
            String acturalCharge = "";
            if (coopUnConfirmBillDetailList.getActuralCharge() != null && !"".equals(coopUnConfirmBillDetailList.getActuralCharge())) {
                acturalCharge = df.format(coopUnConfirmBillDetailList.getActuralCharge());
            }
            remark += "金额：" + originalCharge + "调整为" + acturalCharge;


            // remark += "金额："+ coopUnConfirmBillDetailList.getOriginalCharge() + "调整为" + coopUnConfirmBillDetailList.getActuralCharge();
        }
        if (remark == "") {
            if (coopUnConfirmBillDetailList.getRemarkSaler() != null && !"".equals(coopUnConfirmBillDetailList.getRemarkSaler())) {
                remark = ";" + coopUnConfirmBillDetailList.getRemarkSaler();
            }
        } else {
            if (coopUnConfirmBillDetailList.getRemarkSaler() != null && !"".equals(coopUnConfirmBillDetailList.getRemarkSaler())) {
                remark += ";" + coopUnConfirmBillDetailList.getRemarkSaler();
            }
        }

        return remark;
    }

    @Override
    public List<CoopBillMadeExcel> querymadeBillListForExcel(CoopBillStatement coopBillStatement) {
        coopBillStatement.setOrgId(SecurityUtils.getUser().getOrgId());
        if (coopBillStatement.getCoopName() != null && !"".equals(coopBillStatement.getCoopName())) {
            coopBillStatement.setCoopName(coopBillStatement.getCoopName().toUpperCase());
        }
        if(!"".equals(coopBillStatement.getConfirmCustomerTime_begin())){
            coopBillStatement.setConfirmCustomerTime_begin(coopBillStatement.getConfirmCustomerTime_begin() + " 00:00:00");
        }
        if(!"".equals(coopBillStatement.getConfirmCustomerTime_end())){
            coopBillStatement.setConfirmCustomerTime_end(coopBillStatement.getConfirmCustomerTime_end() + " 23:59:59");
        }
        List<CoopBillMadeExcel> list = coopBillMapper.queryMadeBillListForExcel(coopBillStatement);

        BigDecimal invoiceAmountTotal = new BigDecimal(0);
        if(list!=null && list.size()>0){
            for (int i = 0; i < list.size(); i++) {
                //计算账单金额合计
                CoopBillMadeExcel cbme = list.get(i);
                if (cbme.getInvoiceAmount() != null) {
                    String invoiceAmount= cbme.getInvoiceAmount().replaceAll(",","");
                    BigDecimal bd=new BigDecimal(invoiceAmount);
                    invoiceAmountTotal = invoiceAmountTotal.add(bd);
                }
            }
        }
        CoopBillMadeExcel cse = new CoopBillMadeExcel();
        DecimalFormat df = new DecimalFormat(",###,##0.00");
        df.format(invoiceAmountTotal);
        cse.setInvoiceAmount(df.format(invoiceAmountTotal));
        cse.setStatementName("合计");
        list.add(cse);
        return list;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer saveManualBill(CoopManualBill coopManualBill) throws ParseException {
        CoopBillStatement coopBillStatement = new CoopBillStatement();
        if (coopManualBill != null) {
            //向prm_coop_statement表插入一条数据
            coopBillStatement.setOrgId(SecurityUtils.getUser().getOrgId());
            coopBillStatement.setCoopId(coopManualBill.getCoopId());
            coopBillStatement.setStatementName(coopManualBill.getStatementName());
            coopBillStatement.setStatementStatus("客户已确认");
            coopBillStatement.setBillName(coopManualBill.getStatementDate());
            coopBillStatement.setActuralCharge(coopManualBill.getTotalCharge().doubleValue());
            coopBillStatement.setInvoiceAmount(coopManualBill.getTotalCharge().doubleValue());
            coopBillStatement.setCreatorId(SecurityUtils.getUser().getId());
            coopBillStatement.setCreateTime(new Date());
            coopBillStatement.setCreatorName(SecurityUtils.getUser().getUserCname() + " " + SecurityUtils.getUser().getUserEmail());
            coopBillStatement.setConfirmCustomerName(SecurityUtils.getUser().getUserCname() + " " + SecurityUtils.getUser().getUserEmail());
            coopBillStatement.setConfirmCustomerTime(new Date());
            coopBillStatement.setInvoiceTitle(coopManualBill.getInvoiceTitle());
            coopBillStatement.setInvoiceType(coopManualBill.getInvoiceType());
            coopBillStatement.setInvoiceMailTo(coopManualBill.getInvoiceMailTo());
            coopBillStatement.setInvoiceRemark(coopManualBill.getInvoiceRemark());
            coopBillStatement.setBillManualMailTo(coopManualBill.getBillManualMailTo());
            coopBillStatement.setBillTemplate(coopManualBill.getBillTemplate());
            //获取选择月份下属于本签约公司的账单号的最大值
            String currentBillNumber = coopBillMapper.getCurrentBillStatementNumber(coopBillStatement);
            String billNumber = "";
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyMM");
            SimpleDateFormat dateFormat1 = new SimpleDateFormat("yyyy-MM");
            if (currentBillNumber == null || "".equals(currentBillNumber)) {//如果最大值为空，则说明还不存在，则编号从000001开始
                billNumber = "ST" + dateFormat.format(dateFormat1.parse(coopBillStatement.getBillName())) + "000001";
            } else {//如果存在则给其序列号+1
                String subBillNumber = currentBillNumber.substring(currentBillNumber.length() - 6);
                if (subBillNumber != null && !subBillNumber.isEmpty()) {
                    int newSubBillNumber = Integer.parseInt(subBillNumber) + 1;
                    billNumber = String.format(currentBillNumber.substring(0, 6) + "%06d", newSubBillNumber);
                }
            }
            coopBillStatement.setStatementNumber(billNumber);
            coopBillMapper.insertStatement(coopBillStatement);

            //向prm_coop_bill表插入一条数据
            CoopUnConfirmBillDetail coopUnConfirmBillDetail = new CoopUnConfirmBillDetail();
            coopUnConfirmBillDetail.setStatementId(coopBillStatement.getStatement_id());
            coopUnConfirmBillDetail.setSettlementType("99");
            coopUnConfirmBillDetail.setServiceId(coopManualBill.getServiceId());
            coopUnConfirmBillDetail.setServiceName(coopManualBill.getServiceName());
            coopUnConfirmBillDetail.setUnitPrice(coopManualBill.getUnitPrice());
            coopUnConfirmBillDetail.setOriginalCharge(coopManualBill.getTotalCharge());
            coopUnConfirmBillDetail.setPlanCharge(coopManualBill.getTotalCharge());
            coopUnConfirmBillDetail.setActuralCharge(coopManualBill.getTotalCharge());
            coopUnConfirmBillDetail.setBillFillDate(new Date());
            coopUnConfirmBillDetail.setBillStatus("数据已填充");
            coopUnConfirmBillDetail.setCreatorId(SecurityUtils.getUser().getId());
            coopUnConfirmBillDetail.setCreateTime(new Date());
            coopUnConfirmBillDetail.setCreatorName(SecurityUtils.getUser().getUserCname() + " " + SecurityUtils.getUser().getUserEmail());
            coopUnConfirmBillDetail.setOrgId(SecurityUtils.getUser().getOrgId());
            coopUnConfirmBillDetail.setBillName(coopManualBill.getStatementDate());
            coopUnConfirmBillDetail.setCoopId(coopManualBill.getCoopId());
            coopUnConfirmBillDetail.setCoopName(coopManualBill.getCoopName());
            coopUnConfirmBillDetail.setFillUrl(coopManualBill.getFillUrl());
            coopUnConfirmBillDetail.setFillName(coopManualBill.getFillName());
            coopUnConfirmBillDetail.setFillUser(SecurityUtils.getUser().getUserCname() + " " + SecurityUtils.getUser().getUserEmail());
            coopUnConfirmBillDetail.setRemark(coopManualBill.getRemark());
            //coopUnConfirmBillDetail.setDepartureStation(coopManualBill.getDepartureStation());
            coopUnConfirmBillDetail.setFillNumber(coopManualBill.getBaseQuantity());
            coopUnConfirmBillDetail.setFillNumberOriginal(coopManualBill.getBaseQuantity());
            coopUnConfirmBillDetail.setBillTemplate(coopManualBill.getBillTemplate());
            if (!"".equals(coopManualBill.getFillUrl())) {
                coopUnConfirmBillDetail.setNeedEmail(1);
            } else {
                coopUnConfirmBillDetail.setNeedEmail(0);
            }

            //获取选择月份下属于本签约公司的账单号的最大值
            String currentBillNumber1 = coopBillMapper.getCurrentBillNumber(coopUnConfirmBillDetail);
            String billNumber1 = "";
            if (currentBillNumber1 == null || "".equals(currentBillNumber1)) {//如果最大值为空，则说明还不存在，则编号从000001开始
                billNumber1 = dateFormat.format(dateFormat1.parse(coopUnConfirmBillDetail.getBillName())) + "000001";
            } else {//如果存在则给其序列号+1
                String subBillNumber1 = currentBillNumber1.substring(currentBillNumber1.length() - 6);
                if (subBillNumber1 != null && !subBillNumber1.isEmpty()) {
                    int newSubBillNumber1 = Integer.parseInt(subBillNumber1) + 1;
                    billNumber1 = String.format(currentBillNumber1.substring(0, 4) + "%06d", newSubBillNumber1);
                }
            }
            coopUnConfirmBillDetail.setBillNumber(billNumber1);
            coopBillMapper.insertBill(coopUnConfirmBillDetail);
        }
        return coopBillStatement.getStatement_id();
    }

    @Override
    public void sendManualBill(Integer statement_id, String toUsers) {

        Map<String, Object> param = new HashMap<>();
        param.put("statement_id", statement_id);
        List<List<CoopBillEmail>> list = coopBillMapper.getPdfFields(param);
        if (list != null && list.size() > 0) {
            if (list.get(0) != null && list.get(0).size() > 0) {
                CoopBillEmail coopBillEmail = new CoopBillEmail();
                List<CoopBillEmail> listDetail = new ArrayList<CoopBillEmail>();
                if (list != null && list.size() > 0) {
                    //拼接PDF表头
                    List<CoopBillEmail> list1 = list.get(0);
                    coopBillEmail.setTxt_01(list1.get(0).getTxt_01());
                    coopBillEmail.setTxt_02(list1.get(0).getTxt_02());
                    coopBillEmail.setTxt_03(list1.get(0).getTxt_03());
                    coopBillEmail.setTxt_04(list1.get(0).getTxt_04());
                    coopBillEmail.setTxt_05(list1.get(0).getTxt_05());
                    coopBillEmail.setTxt_06(list1.get(0).getTxt_06());
                    coopBillEmail.setTxt_07(list1.get(0).getTxt_07());
                    coopBillEmail.setTxt_08(list1.get(0).getTxt_08());
                    coopBillEmail.setTxt_09(list1.get(0).getTxt_09());
                    coopBillEmail.setTxt_10(list1.get(0).getTxt_10());
                    coopBillEmail.setMail_cc(list1.get(0).getMail_cc());
                    coopBillEmail.setMail_to(list1.get(0).getMail_to());
                    coopBillEmail.setMailAttachment(list1.get(0).getMailAttachment());
                    coopBillEmail.setMailTitle(list1.get(0).getMailTitle());
                    coopBillEmail.setPrintTemplate(list1.get(0).getPrintTemplate());
                    coopBillEmail.setMailBody(list1.get(0).getMailBody());

                    //拼接账单明细
                    listDetail = list.get(1);
                }
                if (coopBillEmail != null) {
                    List<File> files = new ArrayList<File>();
                    //生成PDF并放入附件
                    String pdfPath = "";
                    try {
                        pdfPath = PDFUtils.fillTemplate(coopBillEmail, listDetail, parentPath);//生成PDF
                        File file = new File(pdfPath);
                        files.add(file);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    //是否有明细附件，如果有放入附件
                    String filePathMimeName = "";
                    ArrayList<Map<String, String>> fileList = new ArrayList<>();
                    Map<String, String> map = new HashMap<>();
                    map.put("path", PDFUtils.filePath + pdfPath);//账单路径放入map
                    map.put("name", pdfPath.substring(pdfPath.lastIndexOf("/") +1, pdfPath.length()));//账单附件名称放入map
                    map.put("flag", "local");
                    fileList.add(map);
                    //从明细中拿附件路径和名称
                    if (listDetail != null && listDetail.size() > 0) {
                        for (int i = 0; i < listDetail.size(); i++) {
                            if (!StrUtil.isBlank(listDetail.get(i).getMailAttachmentUrl())) {
                                String mailAttachmentUrl = listDetail.get(i).getMailAttachmentUrl();
                                filePathMimeName = listDetail.get(i).getMailAttachmentName() + mailAttachmentUrl.substring(mailAttachmentUrl.lastIndexOf("."), mailAttachmentUrl.length());
                                Map<String, String> map1 = new HashMap<>();
                                map1.put("path", mailAttachmentUrl);//账单路径放入map
                                map1.put("name", filePathMimeName);//账单附件名称放入map
                                map1.put("flag", "upload");
                                fileList.add(map1);
                            }
                        }
                    }
                    //生成邮件
                    //生成收件人数组
                    /*String recipientEmails=coopBillEmail.getMail_to();
                    String[] recipientEmail = new String[0];
                    if(recipientEmails!=null && !"".equals(recipientEmails)){
                        recipientEmail=recipientEmails.split(",");
                    }*/
                    String[] recipientEmail = this.parse(toUsers);

                    String title = coopBillEmail.getMailTitle();
                    String mailBody = coopBillEmail.getMailBody();
                    StringBuilder builder = new StringBuilder();
                    builder.append(mailBody);
                    //生成抄送人数组
                    String ccEmails = coopBillEmail.getMail_cc();
                    String[] ccEmail = null;
                    if (ccEmails != null && !"".equals(ccEmails)) {
                        ccEmail = ccEmails.split(",");
                    }
                    try {
                        mailSendService.sendAttachmentsMailNew(false, recipientEmail, ccEmail, null, title, builder.toString(), fileList,null);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                //更新 prm_coop_statement
                CoopBillStatement cst = new CoopBillStatement();
                cst.setStatement_id(statement_id);
                cst.setStatementMailDate(new Date());
                cst.setStatementMailSenderName(SecurityUtils.getUser().getUserCname() + " " + SecurityUtils.getUser().getUserEmail());
                cst.setStatementMailSenderId(SecurityUtils.getUser().getId());
                cst.setMailSendTime(new Date());
                coopBillMapper.updateStatementMailDate(cst);
            }
        }
    }

    private String[] parse(String str) {
        if (StringUtils.isBlank(str)) {
            return new String[]{};
        }
        StringBuffer sb = new StringBuffer();
        sb.append("'");
        String str1 = str.replaceAll(";","','");
        sb.append(str1);
        sb.append("'");
        String contactIds = sb.toString();
        CoopBillStatement bean = new CoopBillStatement();
        bean.setBillManualMailTo(contactIds);
        String emails = coopBillMapper.getEmailByContactId(bean);
        if(emails != null && !"".equals(emails)){
            return emails.trim().split(",");
        }else{
            return new String[]{};
        }
    }

    private String[] parse1(String str, String delimiter) {
        if (StringUtils.isBlank(str)) {
            return new String[]{};
        }
        Assert.hasLength(delimiter, "未指定分割符!");
        return str.trim().split(delimiter);
    }

    public static double addDouble(double m1, double m2) {
        BigDecimal p1 = new BigDecimal(Double.toString(m1));
        BigDecimal p2 = new BigDecimal(Double.toString(m2));
        return p1.add(p2).doubleValue();
    }

    public static BigDecimal addDouble1(double m1, BigDecimal m2) {
        BigDecimal p1 = new BigDecimal(Double.toString(m1));
        //BigDecimal p2 = new BigDecimal(Double.toString(m2));
        return p1.add(m2);
    }

    public static double subDouble(double m1, double m2) {
        BigDecimal p1 = new BigDecimal(Double.toString(m1));
        BigDecimal p2 = new BigDecimal(Double.toString(m2));
        return p1.subtract(p2).doubleValue();
    }
}
