package com.efreight.afbase.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class DebitNotePrint implements Serializable {

    private static final long serialVersionUID = 1L;
    //签约公司名称
    private String orgName;

    //收款客户名称
    private String customerName;

    //签约公司地址
    private String orgAddress;

    //签约公司电话
    private String orgTelephone;

    //账单编号
    private String debitNoteNum;

    //账单日期
    private LocalDate debitNoteDate;

    //主单号
    private String awbNumber;

    //工作号
    private String jobNumber;

    //始发港
    private String departure;

    //目的港
    private String destination;

    //航班号
    private String flightNo;

    //航班日期
    private LocalDate flightDate;

    //件数
    private Integer pieces;

    //计费重量
    private BigDecimal chargeWeight;

    //体积
    private BigDecimal volume;

    //账单金额
    private String amount;

    //账单本币金额（大写）
    private String functionalAmountBig;

    //账单本币金额
    private String functionalAmount;

    //制单人
    private String creatorName;

    //制单日期
    private LocalDateTime createTime;

    //银行信息
    private String bankInfo;
    //账单备注
    private String remark;

    //费用明细
    private List<AfIncome> incomeList;

}
