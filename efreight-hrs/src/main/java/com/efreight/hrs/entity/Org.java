package com.efreight.hrs.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 * 
 * </p>
 *
 * @author zhanghw
 * @since 2019-06-03
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("hrs_org")
public class Org implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "org_id", type = IdType.AUTO)
    private Integer orgId;

    private String orgCode;

    private String orgCodeThree;

    private String orgName;

    private String shortName;

    private String orgEname;

    private String shortEname;

    private String socialCreditCode;

    private String orgLogo;

    private String shortLogo;
    private String chBillTemplate;
    private String chListTemplate;
    private String enBillTemplate;
    private String enListTemplate;
    
    private String chBillTemplateAi;
    private String enBillTemplateAi;
    private String chBillTemplateSe;
    private String enBillTemplateSe;
    private String chBillTemplateSi;
    private String enBillTemplateSi;
    private String chBillTemplateTe;
    private String enBillTemplateTe;
    private String chBillTemplateTi;
    private String enBillTemplateTi;
    private String chBillTemplateLc;
    private String enBillTemplateLc;
    private String chBillTemplateIo;
    private String enBillTemplateIo;
    

    private Integer adminId;
    
    private Integer roleId;
    
    private String adminName;

    private String adminEmail;

    private String adminTel;

    private String rcEmail;

    private Integer creatorId;

    private LocalDateTime createTime;

    private Integer editorId;

    private LocalDateTime editTime;

    private LocalDateTime stopDate;

    private Integer stopId;

    private Boolean orgStatus;

    private Integer coopId;

    private String orgSeal;

    private Integer orgType;

    private Integer orgEditionId;

    private Integer orgUserCount;

    private String oneStopCode;

    private transient Integer blackValid;
    private transient Integer coopStatus;
    private String adminInternationalCountryCode;
    private Integer demandPersonId;
    private String orgRemark;
    private String orgBankInfoCn;
    private String orgBankInfoEn;
    private String orgAddressInfoCn;
    private String orgAddressInfoEn;
    private String financialVoucherOutType;
    private String financialVoucherOutCurrency;
    
    private String chBillTemplateExcel;
    private String enBillTemplateExcel;
    private String chBillTemplateAiExcel;
    private String enBillTemplateAiExcel;
    private String chBillTemplateSeExcel;
    private String enBillTemplateSeExcel;
    private String chBillTemplateSiExcel;
    private String enBillTemplateSiExcel;
    private String chBillTemplateTeExcel;
    private String enBillTemplateTeExcel;
    private String chBillTemplateTiExcel;
    private String enBillTemplateTiExcel;
    private String chBillTemplateLcExcel;
    private String enBillTemplateLcExcel;
    private String chBillTemplateIoExcel;
    private String enBillTemplateIoExcel;

    private transient String updateDeptNameFlag;
    
    private boolean orderFinanceLockView;

    /**
     * 企业配置信息
     */
    @TableField(exist = false)
    private List<OrgOrderConfig> orderConfig;

    @TableField(exist = false)
    private String statementTemplateAeExcelCn;
    @TableField(exist = false)
    private String statementTemplateAeExcelEn;
    @TableField(exist = false)
    private String statementTemplateAiExcelCn;
    @TableField(exist = false)
    private String statementTemplateAiExcelEn;
    @TableField(exist = false)
    private String statementTemplateSeExcelCn;
    @TableField(exist = false)
    private String statementTemplateSeExcelEn;
    @TableField(exist = false)
    private String statementTemplateSiExcelCn;
    @TableField(exist = false)
    private String statementTemplateSiExcelEn;
    @TableField(exist = false)
    private String statementTemplateTeExcelCn;
    @TableField(exist = false)
    private String statementTemplateTeExcelEn;
    @TableField(exist = false)
    private String statementTemplateTiExcelCn;
    @TableField(exist = false)
    private String statementTemplateTiExcelEn;
    @TableField(exist = false)
    private String statementTemplateLcExcelCn;
    @TableField(exist = false)
    private String statementTemplateLcExcelEn;
    @TableField(exist = false)
    private String statementTemplateIoExcelCn;
    @TableField(exist = false)
    private String statementTemplateIoExcelEn;
    /**
     * 附加服务配置信息
     */
    @TableField(exist = false)
    private List<OrgServiceMealConfig> serviceMealConfigList;

    /**
     * 总用户数
     */
    @TableField(exist = false)
    private int totalUser;
    /**
     * 用于获取公司的子公司判断
     */
    @TableField(exist = false)
    private String checkJt;

    @TableField(strategy = FieldStrategy.IGNORED)
    private Integer groupId;
    
    private String orgFromRemark;

    @TableField(value = "org_from_remark_2")
    private String orgFromRemark2;

    /**
     * 银行信息
     */
    @TableField(exist = false)
    private List<OrgBankConfig> orgBankConfigList;

    private Integer activeIndex;

    @TableField(exist = false)
    private Integer rankNumber;
}
