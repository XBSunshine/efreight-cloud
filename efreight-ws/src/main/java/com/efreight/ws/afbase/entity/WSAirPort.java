package com.efreight.ws.afbase.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("af_airport")
public class WSAirPort implements Serializable {

    @TableId(value = "ap_id", type = IdType.AUTO)
    private Integer apId;
    private String apCode;
    private String apNameCn;
    private String apNameEn;
    private Boolean apStatus;

}
