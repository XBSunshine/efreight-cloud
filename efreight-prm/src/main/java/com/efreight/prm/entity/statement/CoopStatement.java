package com.efreight.prm.entity.statement;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * @author lc
 * @date 2021/3/15 9:05
 */
@Data
@TableName("prm_coop_statement")
public class CoopStatement implements Serializable {

      private static final long serialVersionUID = 1L;

      private Integer statementId;
      private Integer orgId;
      private Integer coopId;
      private Integer settlementId;
      private String statementName;
      private String statementStatus;
      private String statementNumber;
      private String statementDate;
      private Date statementMailDate;
      private Integer statementMailSenderId;
      private String statementMailSenderName;
      private BigDecimal amountReceivable;
      private BigDecimal amountReceived;
      private BigDecimal amountReceivedDiscount;
      private BigDecimal minCharge;
      private BigDecimal maxCharge;
      private BigDecimal invoiceAmount;
      private String invoiceNumber;
      private Date invoiceDate;
      private String invoiceUserName;
      private BigDecimal invoiceWriteoffAmount;
      private Date invoiceWriteoffDate;
      private String invoiceWriteoffUserName;
      private Integer creatorId;
      private String creatorName;
      private Date createTime;
      private Integer editorId;
      private String editorName;
      private Date editTime;
      private Boolean modifySaler;
      private String confirmHeadOfficeName;
      private Date confirmHeadOfficeTime;
      private String confirmSalerName;
      private Date confirmSalerTime;
      private String confirmCustomerName;
      private Date confirmCustomerTime;
      private String invoiceTitle;
      private String invoiceType;
      private String invoiceRemark;
      private String invoiceMailTo;
      private String billManualMailTo;
      private String expressCompany;
      private String expressNumber;
      private Date mailSendTime;
      private String billTemplate;
      private String rowUuid;
}
