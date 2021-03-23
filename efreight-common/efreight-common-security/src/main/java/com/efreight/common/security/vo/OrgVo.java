package com.efreight.common.security.vo;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 *
 * </p>
 *
 * @author xiaobo
 * @since 2019-11-08
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class OrgVo implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer orgId;

    private String orgCode;

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

    private transient Integer blackValid;
    private transient Integer coopStatus;

    private String financialVoucherOutType;
    private String financialVoucherOutCurrency;

}
