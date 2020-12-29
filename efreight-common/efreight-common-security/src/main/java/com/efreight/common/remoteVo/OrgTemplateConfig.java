package com.efreight.common.remoteVo;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * <p>
 * HRS 签约公司表：模板配置表
 * </p>
 *
 * @author xiaobo
 * @since 2020-08-14
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class OrgTemplateConfig implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * ID
     */
    private Integer configId;

    /**
     * 签约公司ID
     */
    private Integer orgId;

    /**
     * 账单清单：AE 中文
     */
    private String statementTemplateAeExcelCn;

    /**
     * 账单清单：AE 英文
     */
    private String statementTemplateAeExcelEn;

    /**
     * 账单清单：AI 中文
     */
    private String statementTemplateAiExcelCn;

    /**
     * 账单清单：AI 英文
     */
    private String statementTemplateAiExcelEn;

    /**
     * 账单清单：SE 中文
     */
    private String statementTemplateSeExcelCn;

    /**
     * 账单清单：SE 英文
     */
    private String statementTemplateSeExcelEn;

    /**
     * 账单清单：SI 中文
     */
    private String statementTemplateSiExcelCn;

    /**
     * 账单清单：SI 英文
     */
    private String statementTemplateSiExcelEn;

    /**
     * 账单清单：TE 中文
     */
    private String statementTemplateTeExcelCn;

    /**
     * 账单清单：TE 英文
     */
    private String statementTemplateTeExcelEn;

    /**
     * 账单清单：TI 中文
     */
    private String statementTemplateTiExcelCn;

    /**
     * 账单清单：TI 英文
     */
    private String statementTemplateTiExcelEn;

    /**
     * 账单清单：LC 中文
     */
    private String statementTemplateLcExcelCn;

    /**
     * 账单清单：LC 英文
     */
    private String statementTemplateLcExcelEn;
    
    /**
     * 账单清单：IO 中文
     */
    private String statementTemplateIoExcelCn;

    /**
     * 账单清单：IO 英文
     */
    private String statementTemplateIoExcelEn;


}
