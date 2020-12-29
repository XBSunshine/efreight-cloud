package com.efreight.afbase.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * AF 基础信息 仓库货栈 托书模板 附加模板（安全说明模板)
 * @author lc
 * @date 2020/8/26 16:35
 */

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("af_warehouse_letter_attach_file")
public class WarehouseLetterAttachFile implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * 数据ID
     */
    private Integer attachFileId;
    /**
     * 挺快模板ID
     */
    private Integer warehouseLetterId;
    /**
     *航司三字码
     */
    private String carrierPrefix;
    /**
     *模板文件名
     */
    private String templateNameExcel;
    /**
     * 模板文件地址
     */
    private String templateFileExcel;
    /**
     * PDF模板文件名
     */
    private String templateNamePdf;
    /**
     * PDF模板文件地址
     */
    private String templateFilePdf;
    /**
     * 数据创建ID
     */
    private Integer creatorId;
    /**
     * 创建人
     */
    private String creatorName;
    /**
     * 数据创建时间
     */
    private LocalDateTime createTime;
}
