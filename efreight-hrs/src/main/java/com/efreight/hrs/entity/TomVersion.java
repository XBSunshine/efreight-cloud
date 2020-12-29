package com.efreight.hrs.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.springframework.format.annotation.DateTimeFormat;

/**
 * <p>
 *
 * </p>
 *
 * @author xiaobo
 * @since 2019-07-22
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("tom_version")
public class TomVersion implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "version_id", type = IdType.AUTO)
    private Integer versionId;

    private String versionCode;


    private LocalDateTime versionDate;

    private String versionText;
    @TableField(exist = false)
    private LocalDateTime versionDateStart;
    @TableField(exist = false)
    private LocalDateTime versionDateEnd;
}
