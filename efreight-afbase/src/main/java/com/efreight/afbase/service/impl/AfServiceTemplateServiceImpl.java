package com.efreight.afbase.service.impl;

import com.efreight.afbase.entity.AfServiceTemplate;
import com.efreight.afbase.dao.AfServiceTemplateMapper;
import com.efreight.afbase.entity.exportExcel.AfServiceTemplateExcel;
import com.efreight.afbase.service.AfServiceTemplateService;
import com.efreight.common.security.util.SecurityUtils;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * <p>
 * AF 基础信息 服务类别：收付模板设定 服务实现类
 * </p>
 *
 * @author qipm
 * @since 2020-06-01
 */
@Service
public class AfServiceTemplateServiceImpl extends ServiceImpl<AfServiceTemplateMapper, AfServiceTemplate> implements AfServiceTemplateService {

	@Override
    public IPage<AfServiceTemplate> getListPage(Page page, AfServiceTemplate bean) {

        bean.setOrgId(SecurityUtils.getUser().getOrgId());
        return baseMapper.getListPage(page, bean);
    }
	@Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean doSave(AfServiceTemplate bean) {
		List<AfServiceTemplate> addList=bean.getAddTemplate();
        String businessScope = bean.getBusinessScope();
        Integer templateType = bean.getTemplateType();
        List<AfServiceTemplate> codeList = baseMapper.selectCode(SecurityUtils.getUser().getOrgId(), businessScope,templateType);
        if (codeList.size() == 0) {
            bean.setTemplateCode("TC001");
        } else {
            if (("TC999").equals(codeList.get(0).getTemplateCode())) {
                throw new RuntimeException("最多可以创建999个" + businessScope + "模板");
            } else {
                String str = codeList.get(0).getTemplateCode();
                str = str.substring(str.length() - 3);
                bean.setTemplateCode("TC" + String.format("%03d", Integer.parseInt(str) + 1));
            }

        }
        bean.setCreateTime(LocalDateTime.now());
        bean.setEditTime(LocalDateTime.now());
        bean.setCreatorId(SecurityUtils.getUser().getId());
        bean.setCreatorName(SecurityUtils.getUser().getUserCname() + " " + SecurityUtils.getUser().getUserEmail());
        bean.setEditorId(SecurityUtils.getUser().getId());	
        bean.setEditorName(SecurityUtils.getUser().getUserCname() + " " + SecurityUtils.getUser().getUserEmail());
        bean.setOrgId(SecurityUtils.getUser().getOrgId());
        for (int i = 0; i < addList.size(); i++) {
        	AfServiceTemplate bean2=addList.get(i);
        	
        	bean.setCustomerId(bean2.getCustomerId());
        	bean.setServiceId(bean2.getServiceId());
        	bean.setServiceChargeStandard(bean2.getServiceChargeStandard());
        	bean.setServiceCurrency(bean2.getServiceCurrency());
        	bean.setServiceUnitPrice(bean2.getServiceUnitPrice());
        	bean.setServiceAmountMin(bean2.getServiceAmountMin());
			bean.setServiceAmountMax(bean2.getServiceAmountMax());
			bean.setServiceAmountDigits(bean2.getServiceAmountDigits());
			bean.setServiceAmountCarry(bean2.getServiceAmountCarry());
			
        	baseMapper.insert(bean);
		}
		return true;
    }
	@Override
	public AfServiceTemplate getView(AfServiceTemplate bean) {
		String businessScope = bean.getBusinessScope();
		Integer templateType = bean.getTemplateType();
		String templateCode = bean.getTemplateCode();
		List<AfServiceTemplate> addList=baseMapper.getView(SecurityUtils.getUser().getOrgId(), businessScope,templateType,templateCode);
		
		bean.setAddTemplate(addList);
		
		return bean;
	}
	@Override
	public List<AfServiceTemplate> getServicetemplate(AfServiceTemplate bean) {
		String businessScope = bean.getBusinessScope();
		Integer templateType = bean.getTemplateType();
		String portCode = bean.getPortCode();
		List<AfServiceTemplate> list=baseMapper.getServicetemplate(SecurityUtils.getUser().getOrgId(), businessScope,templateType,portCode);
		
		return list;
	}
	@Override
	@Transactional(rollbackFor = Exception.class)
	public Boolean doEdit(AfServiceTemplate bean) {
		List<AfServiceTemplate> addList=bean.getAddTemplate();
//		String businessScope = bean.getBusinessScope();
//		Integer templateType = bean.getTemplateType();
//		String templateCode = bean.getTemplateCode();
		for (int i = 0; i < bean.getDeleteTemplate().size(); i++) {
			baseMapper.doDeleteById(SecurityUtils.getUser().getOrgId(),bean.getDeleteTemplate().get(i).getTemplateId());
		}
		
		
		for (int i = 0; i < addList.size(); i++) {
			AfServiceTemplate bean2=addList.get(i);
			
			
			if (bean2.getTemplateId()!=null) {
				bean2.setBusinessScope(bean.getBusinessScope());
				bean2.setTemplateType(bean.getTemplateType());
				bean2.setPortCode(bean.getPortCode());
				bean2.setTemplateName(bean.getTemplateName());
				bean2.setTemplateCode(bean.getTemplateCode());
				bean2.setTemplateRemark(bean.getTemplateRemark());
				bean2.setEditTime(LocalDateTime.now());
				bean2.setEditorId(SecurityUtils.getUser().getId());
				bean2.setEditorName(SecurityUtils.getUser().getUserCname() + " " + SecurityUtils.getUser().getUserEmail());
				bean2.setOrgId(SecurityUtils.getUser().getOrgId());
				baseMapper.updateById(bean2);
			} else {
				bean.setCustomerId(bean2.getCustomerId());
				bean.setServiceId(bean2.getServiceId());
				bean.setServiceChargeStandard(bean2.getServiceChargeStandard());
				bean.setServiceCurrency(bean2.getServiceCurrency());
				bean.setServiceUnitPrice(bean2.getServiceUnitPrice());
				bean.setServiceAmountMin(bean2.getServiceAmountMin());
				bean.setServiceAmountMax(bean2.getServiceAmountMax());
				bean.setServiceAmountDigits(bean2.getServiceAmountDigits());
				bean.setServiceAmountCarry(bean2.getServiceAmountCarry());
				
				bean.setCreateTime(LocalDateTime.now());
				bean.setEditTime(LocalDateTime.now());
				bean.setCreatorId(SecurityUtils.getUser().getId());
				bean.setCreatorName(SecurityUtils.getUser().getUserCname() + " " + SecurityUtils.getUser().getUserEmail());
				bean.setEditorId(SecurityUtils.getUser().getId());
				bean.setEditorName(SecurityUtils.getUser().getUserCname() + " " + SecurityUtils.getUser().getUserEmail());
				bean.setOrgId(SecurityUtils.getUser().getOrgId());
				baseMapper.insert(bean);
			}
			
		}
		return true;
	}
	@Override
	@Transactional(rollbackFor = Exception.class)
	public Boolean doDelete(AfServiceTemplate bean) {
		String businessScope = bean.getBusinessScope();
		String templateCode = bean.getTemplateCode();
		Integer templateType = bean.getTemplateType();
		baseMapper.doDelete(SecurityUtils.getUser().getOrgId(), businessScope,templateType,templateCode);
		//HRS日志
        baseMapper.insertHrsLog(businessScope + "模板", "模板号:" + bean.getTemplateCode(),
                SecurityUtils.getUser().getId(), LocalDateTime.now(), SecurityUtils.getUser().getOrgId(), SecurityUtils.getUser().getDeptId());
		return true;
	}

	@Override
	public List<AfServiceTemplateExcel> queryListForExcel(AfServiceTemplate bean) {
		bean.setOrgId(SecurityUtils.getUser().getOrgId());
		return baseMapper.queryListForExcel(bean);
	}
}
