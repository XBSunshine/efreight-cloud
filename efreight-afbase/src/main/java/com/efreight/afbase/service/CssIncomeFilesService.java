package com.efreight.afbase.service;

import java.util.List;
import java.util.Map;

import com.baomidou.mybatisplus.extension.service.IService;
import com.efreight.afbase.entity.CssIncomeFiles;

public interface CssIncomeFilesService extends IService<CssIncomeFiles> {
	
	List<CssIncomeFiles> fileList(String businessType,Integer ids);
	
	boolean saveOrModify(CssIncomeFiles bean);
	boolean deleteFile(CssIncomeFiles bean);

}
