package com.efreight.afbase.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.math.BigDecimal;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 
 * </p>
 *
 * @author qipm
 * @since 2020-10-26
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("af_order_storage_mns")
public class AfOrderStorageMns implements Serializable {

    private static final long serialVersionUID = 1L;
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    /**
     * 主单号
     */
    private String mawbcode;
    private String forwarder;
    private String filecontent;
    private String msgsource;
    private String receiver;
    private String sender;
    private String msgid;
    private String smi;
    private String fsutype;
    private String awb;
    private String dep;
    private String arr;
    private String awbpcs;
    private String awbgwt;
    private Integer pcs;
    private BigDecimal gwt;
    private String uld;
    private String flightno;
    private String flightdate;
    private String occurplace;
    private String occurtime;
    private String nkgagentcode;
    private String createdate;
    private LocalDateTime createtime;



}
