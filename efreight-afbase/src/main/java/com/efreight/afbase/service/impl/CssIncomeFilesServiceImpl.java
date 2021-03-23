package com.efreight.afbase.service.impl;

import java.time.LocalDateTime;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.efreight.afbase.dao.CssIncomeFilesMapper;
import com.efreight.afbase.dao.CssIncomeInvoiceDetailMapper;
import com.efreight.afbase.dao.CssIncomeInvoiceDetailWriteoffMapper;
import com.efreight.afbase.entity.CssIncomeFiles;
import com.efreight.afbase.entity.CssIncomeInvoiceDetail;
import com.efreight.afbase.entity.CssIncomeInvoiceDetailWriteoff;
import com.efreight.afbase.service.CssIncomeFilesService;
import com.efreight.common.security.util.SecurityUtils;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class CssIncomeFilesServiceImpl extends ServiceImpl<CssIncomeFilesMapper,CssIncomeFiles> implements CssIncomeFilesService{
	
	private final CssIncomeInvoiceDetailMapper detailMapper;

    private final CssIncomeInvoiceDetailWriteoffMapper detailWriteoffMapper;
	
    @Override
	public List<CssIncomeFiles> fileList(String businessType, Integer ids) {
	    LambdaQueryWrapper<CssIncomeFiles> cssIncomeFilesWrapper = new LambdaQueryWrapper<CssIncomeFiles>();
		if("invoice".equals(businessType)) {
			cssIncomeFilesWrapper.eq(CssIncomeFiles::getInvoiceDetailId, ids);
			cssIncomeFilesWrapper.isNull(CssIncomeFiles::getInvoiceDetailWriteoffId);
		}else {
			cssIncomeFilesWrapper.eq(CssIncomeFiles::getInvoiceDetailWriteoffId, ids);
		}
		List<CssIncomeFiles> list = baseMapper.selectList(cssIncomeFilesWrapper);
			
	    return list;
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public boolean saveOrModify(CssIncomeFiles bean) {
		LambdaQueryWrapper<CssIncomeFiles> cssIncomeFilesWrapper = new LambdaQueryWrapper<CssIncomeFiles>();
		if(bean.getInvoiceDetailId()!=null) {
			cssIncomeFilesWrapper.eq(CssIncomeFiles::getInvoiceDetailId, bean.getInvoiceDetailId());
		}else {
			cssIncomeFilesWrapper.eq(CssIncomeFiles::getInvoiceDetailWriteoffId, bean.getInvoiceDetailWriteoffId());
		}
		List<CssIncomeFiles> list = baseMapper.selectList(cssIncomeFilesWrapper);
		
		if("invoice".equals(bean.getBusinessType())) {
			//发票附件
			//发票信息
			CssIncomeInvoiceDetail detail = detailMapper.selectById(bean.getInvoiceDetailId());
			if(detail==null) {
				throw new RuntimeException("发票信息异常");
			}
			bean.setBusinessScope(detail.getBusinessScope());
			bean.setDebitNoteId(detail.getDebitNoteId());
			bean.setStatementId(detail.getStatementId());
		}
		if("writeoff".equals(bean.getBusinessType())) {
			//核销单附件
			//核销单信息
			CssIncomeInvoiceDetailWriteoff detailWriteoff = detailWriteoffMapper.selectById(bean.getInvoiceDetailWriteoffId());
		    if(detailWriteoff==null) {
		    	throw new RuntimeException("核销单信息异常");
		    }
		    bean.setBusinessScope(detailWriteoff.getBusinessScope());
			bean.setDebitNoteId(detailWriteoff.getDebitNoteId());
			bean.setStatementId(detailWriteoff.getStatementId());
		}
		//先清理历史附件
//		if(list!=null&&list.size()>0) {
//			list.stream().forEach(o->{
//				baseMapper.deleteById(o);
//			});
//		}
		
		//上传文件插入
		if(!StringUtils.isEmpty(bean.getFileStrs())) {
			JSONArray jsonArr = JSONArray.parseArray(bean.getFileStrs());
			for (int i = 0; i < jsonArr.size(); i++) {
                JSONObject job = jsonArr.getJSONObject(i);
                CssIncomeFiles fileMap = new CssIncomeFiles();
                fileMap.setOrgId(SecurityUtils.getUser().getOrgId());
                fileMap.setFileName(job.getString("fileName"));
                fileMap.setFileRemark(job.getString("fileRemark"));
                fileMap.setFileType(job.getString("fileType"));
                fileMap.setFileUrl(job.getString("fileUrl"));
                fileMap.setDebitNoteId(bean.getDebitNoteId());
                fileMap.setStatementId(bean.getStatementId());
                fileMap.setBusinessScope(bean.getBusinessScope());
                fileMap.setInvoiceDetailId(bean.getInvoiceDetailId());
                fileMap.setInvoiceDetailWriteoffId(bean.getInvoiceDetailWriteoffId());
                fileMap.setCreateTime(LocalDateTime.now());
                fileMap.setCreatorId(SecurityUtils.getUser().getId());
                fileMap.setCreatorName(SecurityUtils.getUser().buildOptName());
                baseMapper.insert(fileMap);
			}
		}
		return true;
	}

	@Override
	public boolean deleteFile(CssIncomeFiles bean) {
		CssIncomeFiles cif = baseMapper.selectById(bean.getFileId());
		if(cif==null) {
			throw new RuntimeException("该附件不存在");
		}
		baseMapper.deleteById(cif);
		return true;
	}


    

}
