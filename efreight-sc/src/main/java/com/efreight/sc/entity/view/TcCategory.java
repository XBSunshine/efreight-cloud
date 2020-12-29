package com.efreight.sc.entity.view;

import java.io.Serializable;

import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("af_V_tc_category")
public class TcCategory implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	  /**
     * 参数分类名称
     */
    private String categoryName;
    /**
     * 参数名称
     */
    private String paramText;

}
