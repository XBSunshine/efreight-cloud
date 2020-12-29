package com.efreight.afbase.service.impl;

import com.efreight.afbase.entity.CargoGoodsnames;
import com.efreight.afbase.dao.CargoGoodsnamesMapper;
import com.efreight.afbase.service.CargoGoodsnamesService;
import com.efreight.afbase.utils.PDFUtils;
import com.efreight.common.security.util.SecurityUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import java.time.LocalDateTime;
import java.util.Date;
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
public class CargoGoodsnamesServiceImpl extends ServiceImpl<CargoGoodsnamesMapper, CargoGoodsnames> implements CargoGoodsnamesService {

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
		return true;
	}

	@Override
	public boolean doUpdate(CargoGoodsnames bean) {
		bean.setEditTime(LocalDateTime.now());
        bean.setEditorId(SecurityUtils.getUser().getId());
        bean.setEditorName(SecurityUtils.getUser().getUserCname() + " " + SecurityUtils.getUser().getUserEmail());
        baseMapper.updateById(bean);
		return true;
	}

	@Override
	public boolean doDelete(CargoGoodsnames bean) {
		baseMapper.deleteById(bean.getId());
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
