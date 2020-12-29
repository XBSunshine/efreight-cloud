package com.efreight.afbase.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import java.time.LocalDate;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 *  AF AE订单管理 鉴定证书明细信息
 * </p>
 *
 * @author mayt
 * @since 2020-10-13
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("af_order_identify_detail")
public class AfOrderIdentifyDetail implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 鉴定证书的ID编号
     */
    @TableId(value = "id",type = IdType.AUTO)
    private Integer Id;

    /**
     * 父节点ID，外键
     */
    private Integer masterid;

    /**
     * 报告编号
     */
    private String reportIssueNo;

    /**
     * 鉴定机构
     */
    private String reportIssueOrgan;

    /**
     * 签发日期
     */
    @ApiModelProperty(value="签发日期")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private LocalDate reportIssueDate;

    /**
     * 委托单位
     */
    private String reportApplicant;

    /**
     * 中文品名
     */
    private String reportGoodsCnname;

    /**
     * 英文品名
     */
    private String reportGoodsEnname;

    /**
     * 报告文件url,jpg/jpeg/bmp/png/gif/pdf/excel/word
     */
    private String reportImgUrl;
}
