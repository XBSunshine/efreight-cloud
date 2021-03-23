package com.efreight.afbase.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.time.LocalDateTime;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * CSS 成本对账：附件表
 * </p>
 *
 * @author xiaobo
 * @since 2021-03-10
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("css_cost_files")
public class CssCostFiles implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 附件ID
     */
    @TableId(value = "file_id", type = IdType.AUTO)
    private Integer fileId;

    /**
     * 签约公司ID
     */
    private Integer orgId;

    /**
     * 业务范畴
     */
    private String businessScope;

    /**
     * 对账单ID
     */
    private Integer paymentId;

    /**
     * 发票明细ID
     */
    private Integer invoiceDetailId;

    /**
     * 发票明细核销ID
     */
    private Integer invoiceDetailWriteoffId;

    /**
     * 附件类型：图片、文件
     */
    private String fileType;

    /**
     * 附件文件名
     */
    private String fileName;

    /**
     * 附件地址
     */
    private String fileUrl;

    /**
     * 备注
     */
    private String fileRemark;

    private Integer creatorId;

    private String creatorName;

    private LocalDateTime createTime;


}
