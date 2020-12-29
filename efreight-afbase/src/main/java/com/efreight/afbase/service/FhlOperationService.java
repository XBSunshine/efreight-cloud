package com.efreight.afbase.service;

import java.util.Map;

import com.baomidou.mybatisplus.extension.service.IService;
import com.efreight.afbase.entity.AfOrder;
import com.efreight.afbase.entity.BranchLine;
import com.efreight.afbase.entity.FhlOperation;
import com.efreight.afbase.entity.OperationLook;


public interface FhlOperationService extends IService<AfOrder>{

	Map<String,Object> queryList(Integer currentPage, Integer pageSize,FhlOperation bean);
	Boolean doSave(FhlOperation bean);
	Boolean doDelete(FhlOperation bean);
	Boolean doDeclare(FhlOperation bean);
	Map<String,Object> queryLineList(Integer currentPage, Integer pageSize,BranchLine bean);
	Boolean doMerge(BranchLine bean);
	Boolean doReset(BranchLine bean);
	Boolean doSplit(BranchLine bean);
	Map<String,Object> queryLookList(Integer currentPage, Integer pageSize,OperationLook bean);
	Map<String,Object> queryLogList(OperationLook bean);
	Map<String,Object> queryStatus(OperationLook bean);

}
