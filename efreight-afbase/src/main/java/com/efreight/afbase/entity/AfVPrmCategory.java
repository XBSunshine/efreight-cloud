package com.efreight.afbase.entity;



import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class AfVPrmCategory implements Serializable{
	
	private static final long serialVersionUID = 1L;

	private String paramText;

	private String categoryName;
	private String paramRanking;
	private String remarks;
	@TableField(value = "EDICode1")
	private String ediCode1;

}
