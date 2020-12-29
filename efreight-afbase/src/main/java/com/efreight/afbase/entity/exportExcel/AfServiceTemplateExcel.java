package com.efreight.afbase.entity.exportExcel;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;


@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class AfServiceTemplateExcel {
    /**
     * 业务范畴
     */
    private String businessScope;
    /**
     * 模板类型
     */
    private String templateType;

    /**
     * 目的港/始发港
     */
    private String portCode;

    /**
     * 模板名称
     */
    private String templateName;

    /**
     * 备注
     */
    private String templateRemark;

    /**
     * 操作人
     */
    private String editorName;

    /**
     * 操作日期
     */
    private LocalDateTime editTime;

}
