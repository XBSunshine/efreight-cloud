package com.efreight.afbase.entity.view;

import java.io.Serializable;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class SendProductEmail implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String companyName;
	private String userName;
	private String productName;
	private String fileName;
	private String productDescribe;

}
