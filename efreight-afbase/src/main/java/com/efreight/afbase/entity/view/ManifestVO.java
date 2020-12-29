package com.efreight.afbase.entity.view;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.apache.commons.lang.StringUtils;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author lc
 * @date 2020/11/9 14:35
 */
@Data
public class ManifestVO implements Serializable {

    private static final long serialVersionUID = 1L;
    /**
     * 订单号
     */
    private String orderNo;

    /**
     * 主单号
     */
    private String awbNumber;

    /**
     * 分单号
     */
    private String hawbNumber;

    /**
     * 报官单号
     */
    private String declarationNumber;

    /**
     * 是否为主单信息
     */
    private boolean masterFlag;

    /**
     * 件数
     */
    private String quantity;

    /**
     * 毛重
     */
    private String grossWeight;

    /**
     * 件重信息
     */
    private String pieceWeightInfo;

    /**
     * 原始时间
     */
    @JsonFormat(pattern = "MM-dd HH:mm",timezone = "GMT+8")
    private LocalDateTime originalTime;

    /**
     * 理货时间
     */
    @JsonFormat(pattern = "MM-dd HH:mm",timezone = "GMT+8")
    private LocalDateTime tallyTime;

    /**
     * 放行时间
     */
    @JsonFormat(pattern = "MM-dd HH:mm",timezone = "GMT+8")
    private LocalDateTime passedTime;

    /**
     * 分拨申请时间
     */
    @JsonFormat(pattern = "MM-dd HH:mm",timezone = "GMT+8")
    private LocalDateTime applyTime;

    /**
     * 运抵时间
     */
    @JsonFormat(pattern = "MM-dd HH:mm",timezone = "GMT+8")
    private LocalDateTime arriveTime;

    /**
     * 预配时间
     */
    @JsonFormat(pattern = "MM-dd HH:mm",timezone = "GMT+8")
    private LocalDateTime provisionTime;


    public String getPieceWeightInfo(){
        StringBuffer buffer = new StringBuffer();
        if(StringUtils.isBlank(this.quantity) && StringUtils.isBlank(this.grossWeight)){
            return buffer.toString();
        }
        buffer.append(StringUtils.isBlank(this.quantity) ? '-' : this.quantity + " Pcs");
        buffer.append(" / ");
        buffer.append(StringUtils.isBlank(this.grossWeight) ? '-' : this.grossWeight + " Kg");
        return buffer.toString();
    }

}
