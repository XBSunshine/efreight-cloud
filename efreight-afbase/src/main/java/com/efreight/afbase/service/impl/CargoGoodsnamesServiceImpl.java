package com.efreight.afbase.service.impl;

import com.efreight.afbase.entity.CargoGoodsnames;
import com.efreight.afbase.entity.LogBean;
import com.efreight.afbase.dao.AfOrderMapper;
import com.efreight.afbase.dao.AfRountingSignMapper;
import com.efreight.afbase.dao.CargoGoodsnamesMapper;
import com.efreight.afbase.service.AfOrderService;
import com.efreight.afbase.service.CargoGoodsnamesService;
import com.efreight.afbase.service.InboundFilesService;
import com.efreight.afbase.service.LogService;
import com.efreight.afbase.utils.PDFUtils;
import com.efreight.common.security.util.SecurityUtils;

import lombok.AllArgsConstructor;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author qipm
 * @since 2020-12-17
 */
@Service
@AllArgsConstructor
public class CargoGoodsnamesServiceImpl extends ServiceImpl<CargoGoodsnamesMapper, CargoGoodsnames> implements CargoGoodsnamesService {
	private final LogService logService;
	@Override
	public List<CargoGoodsnames> querylist(CargoGoodsnames bean) {
		List<CargoGoodsnames> list=baseMapper.querylist(bean);
		return list;
	}

	@Override
	public boolean doSave(CargoGoodsnames bean) {
		bean.setOrgId(SecurityUtils.getUser().getOrgId());
		bean.setCreateTime(LocalDateTime.now());
		bean.setCreatorId(SecurityUtils.getUser().getId());
		bean.setCreatorName(SecurityUtils.getUser().getUserCname() + " " + SecurityUtils.getUser().getUserEmail());
		baseMapper.insert(bean);
		//添加日志
		LogBean logBean = new LogBean();
		logBean.setPageName(bean.getPageName());
		logBean.setPageFunction("保存货物清单");
		logBean.setBusinessScope("AE");
		
		logBean.setOrderNumber(bean.getOrderCode());
		logBean.setLogRemark("中文品名："+bean.getGoodsCnnames());
		logBean.setOrderId(bean.getOrderId());
		logBean.setOrderUuid(bean.getOrderUuid());
		logService.saveLog(logBean);
		return true;
	}

	@Override
	public boolean doUpdate(CargoGoodsnames bean) {
		bean.setEditTime(LocalDateTime.now());
        bean.setEditorId(SecurityUtils.getUser().getId());
        bean.setEditorName(SecurityUtils.getUser().getUserCname() + " " + SecurityUtils.getUser().getUserEmail());
        baseMapper.updateById(bean);
        
        //添加日志
  		LogBean logBean = new LogBean();
  		logBean.setPageName(bean.getPageName());
  		logBean.setPageFunction("修改货物清单");
  		logBean.setBusinessScope("AE");
  		
  		logBean.setOrderNumber(bean.getOrderCode());
  		logBean.setLogRemark("中文品名："+bean.getGoodsCnnames());
  		logBean.setOrderId(bean.getOrderId());
  		logBean.setOrderUuid(bean.getOrderUuid());
  		logService.saveLog(logBean);
		return true;
	}

	@Override
	public boolean doDelete(CargoGoodsnames bean) {
		baseMapper.deleteById(bean.getId());
		
		//添加日志
  		LogBean logBean = new LogBean();
  		logBean.setPageName(bean.getPageName());
  		logBean.setPageFunction("删除货物清单");
  		logBean.setBusinessScope("AE");
  		
  		logBean.setOrderNumber(bean.getOrderCode());
  		logBean.setLogRemark("中文品名："+bean.getGoodsCnnames());
  		logBean.setOrderId(bean.getOrderId());
  		logBean.setOrderUuid(bean.getOrderUuid());
  		logService.saveLog(logBean);
		return true;
	}
	@Override
    public String downloadTemplate(){
        String lastFilePath = PDFUtils.filePath + "/PDFtemplate/GoodsNameImport.xlsx";
        return lastFilePath.replace(PDFUtils.filePath, "");
    }

	@Override
	public void doImport(List<CargoGoodsnames> data) {
		for (int i = 0; i < data.size(); i++) {
			data.get(i).setOrgId(SecurityUtils.getUser().getOrgId());
			data.get(i).setCreateTime(LocalDateTime.now());
			data.get(i).setCreatorId(SecurityUtils.getUser().getId());
			data.get(i).setCreatorName(SecurityUtils.getUser().getUserCname() + " " + SecurityUtils.getUser().getUserEmail());
		}
		
	}
}
