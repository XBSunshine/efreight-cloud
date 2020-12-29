package com.efreight.afbase.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import java.time.LocalDate;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;
import java.util.List;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * AF AE订单管理 鉴定证书
 * </p>
 *
 * @author mayt
 * @since 2020-10-12
 */

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("af_order_identifies")
public class AfOrderIdentify implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 鉴定证书的ID编号
     */
    @TableId(value = "order_identify_id",type = IdType.AUTO)
    private Integer orderIdentifyId;

    /**
     * 签约公司ID
     */
    private Integer orgId;

    /**
     * 订单id
     */
    private Integer orderId;

    /**
     * 操作人中文姓名
     */
    private String agentHandlerName;

    /**
     * 主单号
     */
    private String awbNumber;

    /**
     * 承运人2字码
     */
    private String carrierId;

    /**
     * 货站代码
     */
    private String cargoTerminal;

    /**
     * 报告文件url,jpg/jpeg/bmp/png/gif/pdf/excel/word
     */
    private String reportImgUrls;

    /**
     * 创建人id
     */
    private Integer createId;

    /**
     * 创建人名称
     */
    private String creatorName;

    /**
     * 创建时间
     */
    @ApiModelProperty(value="创建时间")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createDate;

    /**
     * 申报人Id
     */
    private Integer declareId;

    /**
     * 申报人名称
     */
    private String declareName;

    /**
     * 申报时间 
     */
    @ApiModelProperty(value="申报时间")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime declareDate;

    /**
     * 状态，默认save
     */
    private String status;

    /**
     * 审核人id
     */
    private Integer auditId;

    /**
     * 审核人名称
     */
    private String auditName;

    /**
     * 审核时间
     */
    @ApiModelProperty(value="审核时间")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime auditDate;

    /**
     * 审核状态，默认save
     */
    private String auditStatus;

    /**
     * 原有syscode
     */
    private Long originalSyscode;


    private transient List<AfOrderIdentifyDetail> afOrderIdentifyDetailList;
}
