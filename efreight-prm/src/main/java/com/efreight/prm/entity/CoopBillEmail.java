package com.efreight.prm.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class CoopBillEmail implements Serializable {
    private static final long serialVersionUID = 1L;
    private Integer billId;
    private String coopName;//客商资料名称
    private String  settlementModName;//账单模板名称
    private Double unitPrice;//单价
    private Integer fillNumber;//数量
    private Double  acturalCharge;//总额
    private String  formatCharge;//总额大写
    private String  contactsName;//客户对账联系人
    private String  email;//客户对账联系人邮箱
    private String  confirmName;//账单确认责任人
    private String  confirmEmail;//账单确认责任人邮箱
    private String  billMouth;//账单月份
    private String  settlementPeriodBegin;//账单期限_开始
    private String  settlementPeriodEnd;//账单期限_截止
    private String  phoneNumber;//账单责任人 手机号
    private String fillUrl;//明细附件地址
    private String mailAttachment;

    private String  txt_01;//客商资料名称
    private String  txt_02;//账单期间
    private String  txt_03;//户名
    private String  txt_04;//账号
    private String  txt_05;//开户行
    private String  txt_06;//应付金额
    private String  txt_07;//人民币大写
    private String  txt_08;//账单月度
    private String  txt_09;//账单编号
    private String  txt_10;//打印日期
    private String  mail_to;//邮件接收人
    private String  mail_cc;//邮件抄送人
    private String  mailTitle;//邮件标题
    private String  printTemplate;//账单模板
    private String  mailBody;//邮件正文

    private String  r_01;//序号
    private String  r_02;//名称
    private String  r_03;//企业6字码
    private String  r_04;//口岸
    private String  r_05;//数量
    private String  r_06;//单价
    private String  r_07;//账单期间
    private String  r_08;//备注
    private String  r_09;//金额
    private String  mailAttachmentUrl;
    private String  mailAttachmentName;

}
