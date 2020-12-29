package com.efreight.afbase.service;

import java.util.List;
import java.util.Map;

import com.baomidou.mybatisplus.extension.service.IService;
import com.efreight.afbase.entity.procedure.CssWorkload;
import com.efreight.afbase.entity.procedure.CssWorkloadDetail;

public interface CssWorkloadService  extends IService<CssWorkload>{

	List<CssWorkload> getCssWorkloadList(CssWorkload cssWorkload);
	
	List<Map> getCssWorkloadDetail(CssWorkload cssWorkload);

	List<CssWorkloadDetail> getCssWorkloadDetailForExcel(CssWorkload cssWorkload);
}
