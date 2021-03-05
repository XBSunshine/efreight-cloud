package com.efreight.afbase.service.impl;

import cn.hutool.core.util.StrUtil;

import com.alibaba.druid.util.StringUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.efreight.afbase.dao.AfOrderMapper;
import com.efreight.afbase.dao.RountingSignMapper;
import com.efreight.afbase.dao.ServiceMapper;
import com.efreight.afbase.entity.*;
import com.efreight.afbase.service.LogService;
import com.efreight.afbase.service.ServiceService;
import com.efreight.afbase.service.VPrmCategoryService;
import com.efreight.afbase.utils.FormatUtils;
import com.efreight.common.security.util.SecurityUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@org.springframework.stereotype.Service
@AllArgsConstructor
@Slf4j
public class ServiceServiceImpl extends ServiceImpl<ServiceMapper, Service> implements ServiceService {
    private final VPrmCategoryService vPrmCategoryService;
    private final LogService logService;
    private final RountingSignMapper rountingSignMapper;
    private final AfOrderMapper afOrderMapper;

    @Override
    public List<VPrmCategoryTree> getList(String businessScope) {
        ArrayList<VPrmCategoryTree> resultTree = new ArrayList<>();
        vPrmCategoryService.list(
                Wrappers.<VPrmCategory>lambdaQuery()
                        .eq(VPrmCategory::getCategoryName, "服务类别")
                        .orderByAsc(VPrmCategory::getParamRanking)).stream().forEach(vPrmCategory -> {
            LambdaQueryWrapper<Service> wrapper = Wrappers.<Service>lambdaQuery();
            if (StrUtil.isNotBlank(businessScope)) {
                wrapper.eq(Service::getBusinessScope, businessScope);
            }
            List<Service> services = baseMapper.selectList(
                    wrapper.eq(Service::getServiceType, vPrmCategory.getParamText())
                            .eq(Service::getOrgId, SecurityUtils.getUser().getOrgId())
                            .eq(Service::getIsValid, 1)
                            .orderByAsc(Service::getServiceCode)
            );
            services.stream().forEach(s->{
            	if(s.getIncomeUnitPrice()!=null) {
            		s.setIncomeUnitPriceStr(FormatUtils.formatWith2AndQFW(s.getIncomeUnitPrice()));
            	}
            	if(s.getCostUnitPrice()!=null) {
            		s.setCostUnitPriceStr(FormatUtils.formatWith2AndQFW(s.getCostUnitPrice()));
            	}
            });
            
            VPrmCategoryTree vPrmCategoryTree = new VPrmCategoryTree();
            vPrmCategoryTree.setServiceId("A" + vPrmCategory.getParamRanking());
            vPrmCategoryTree.setServiceNameCn(vPrmCategory.getParamText());
            vPrmCategoryTree.setServices(services);
            vPrmCategoryTree.setServiceRemark(vPrmCategory.getRemarks());
            resultTree.add(vPrmCategoryTree);
        });
        return resultTree;
    }
    @Override
    public List<VPrmCategoryTree> treeList(String businessScope) {
    	ArrayList<VPrmCategoryTree> resultTree = new ArrayList<>();
    	LambdaQueryWrapper<Service> wrapper1 = Wrappers.<Service>lambdaQuery();
    	List<Service> services1 = baseMapper.selectList(wrapper1.eq(Service::getIsFrequent, 1).eq(Service::getIsValid, 1).eq(Service::getOrgId, SecurityUtils.getUser().getOrgId()).orderByAsc(Service::getServiceCode));
    	if (services1.size()>0) {	
    		VPrmCategoryTree vPrmCategoryTree1 = new VPrmCategoryTree();
			vPrmCategoryTree1.setServiceId("Aa" + "001");
			vPrmCategoryTree1.setServiceNameCn("常用服务");
			vPrmCategoryTree1.setServices(services1);
			vPrmCategoryTree1.setServiceRemark("常用服务");
			resultTree.add(vPrmCategoryTree1);
		}
    	vPrmCategoryService.list(Wrappers.<VPrmCategory>lambdaQuery().eq(VPrmCategory::getCategoryName, "服务类别").orderByAsc(VPrmCategory::getParamRanking)).stream().forEach(vPrmCategory -> {
    		LambdaQueryWrapper<Service> wrapper = Wrappers.<Service>lambdaQuery();
    		if (StrUtil.isNotBlank(businessScope)) {
    			wrapper.eq(Service::getBusinessScope, businessScope);
    		}
    		List<Service> services = baseMapper.selectList(wrapper.eq(Service::getServiceType, vPrmCategory.getParamText()).eq(Service::getIsValid, 1).ne(Service::getIsFrequent, 1).eq(Service::getOrgId, SecurityUtils.getUser().getOrgId()).orderByAsc(Service::getServiceCode));
    		if (services.size()>0) {
    			VPrmCategoryTree vPrmCategoryTree = new VPrmCategoryTree();
        		vPrmCategoryTree.setServiceId("A" + vPrmCategory.getParamRanking());
        		vPrmCategoryTree.setServiceNameCn(vPrmCategory.getParamText());
        		vPrmCategoryTree.setServices(services);
        		vPrmCategoryTree.setServiceRemark(vPrmCategory.getRemarks());
        		resultTree.add(vPrmCategoryTree);
			}
    		
    	});
    	return resultTree;
    }
    @Override
    public List<Service> queryList(String businessScope) {
    	return baseMapper.queryList(SecurityUtils.getUser().getOrgId(),businessScope);
    }

    @Override
    public boolean save(Service service) {
        //生成服务代码
        String serviceCode = "";
        List<Service> services = baseMapper.selectList(Wrappers.<Service>lambdaQuery().eq(Service::getServiceType, service.getServiceType()).eq(Service::getOrgId, SecurityUtils.getUser().getOrgId()).eq(Service::getBusinessScope,service.getBusinessScope()).orderByDesc(Service::getServiceCode));
        if (services.size() == 0) {
            List<VPrmCategory> list = vPrmCategoryService.list(Wrappers.<VPrmCategory>lambdaQuery().eq(VPrmCategory::getCategoryName, "服务类别").eq(VPrmCategory::getParamText, service.getServiceType()));
            if (list.size() != 0) {
                serviceCode = list.get(0).getEdicode1() + "01";
            }
        } else {
            String code = services.get(0).getServiceCode();
            String codePre = code.substring(0, code.length() - 2);
            String codeNum = code.substring(code.length() - 2, code.length());
            if ("99".equals(codeNum)) {
                throw new RuntimeException("服务类别为" + service.getServiceType() + "的服务数量已达到 上限 99个，不允许增加。");
            }
            if (Integer.parseInt(codeNum) + 1 < 10) {
                serviceCode = codePre + "0" + (Integer.parseInt(codeNum) + 1);
            } else {
                serviceCode = codePre + (Integer.parseInt(codeNum) + 1);
            }
        }

        service.setCreateTime(LocalDateTime.now());
        service.setEditTime(LocalDateTime.now());
        service.setCreatorId(SecurityUtils.getUser().getId());
        service.setEditorId(SecurityUtils.getUser().getId());
        service.setCreatorName(SecurityUtils.getUser().getUserCname() + " " + SecurityUtils.getUser().getUserEmail());
        service.setEditorName(SecurityUtils.getUser().getUserCname() + " " + SecurityUtils.getUser().getUserEmail());
        service.setIsSys(false);
        service.setOrgId(SecurityUtils.getUser().getOrgId());
        service.setServiceCode(serviceCode);
        boolean save = super.save(service);
        return save;
    }

    @Override
    public void edit(Service service) {
        service.setEditorId(SecurityUtils.getUser().getId());
        service.setEditorName(SecurityUtils.getUser().getUserCname() + " " + SecurityUtils.getUser().getUserEmail());
        service.setEditTime(LocalDateTime.now());
        updateById(service);
    }

    @Override
    public List<VPrmCategoryTree> startPage() {
        List<VPrmCategory> list = vPrmCategoryService.list(Wrappers.<VPrmCategory>lambdaQuery().eq(VPrmCategory::getCategoryName, "服务类别").orderByAsc(VPrmCategory::getParamRanking));
        List<VPrmCategoryTree> result = list.stream().map(vPrmCategory -> {
            VPrmCategoryTree vPrmCategoryTree = new VPrmCategoryTree();
            vPrmCategoryTree.setServiceId("A" + vPrmCategory.getParamRanking());
            vPrmCategoryTree.setServiceNameCn(vPrmCategory.getParamText());
            vPrmCategoryTree.setServiceRemark(vPrmCategory.getRemarks());
            return vPrmCategoryTree;
        }).collect(Collectors.toList());
        return result;
    }

    @Override
    public List<VPrmCategory> businessScope() {
        LambdaQueryWrapper<VPrmCategory> lambdaQueryWrapper = Wrappers.lambdaQuery();
        lambdaQueryWrapper.eq(VPrmCategory::getCategoryName, "业务范畴");
        lambdaQueryWrapper.ne(VPrmCategory::getEdicode1, "");
        return vPrmCategoryService.list(lambdaQueryWrapper);
    }

    @Override
    public int delete(Integer serviceId) {
        if(null != serviceId){
            Service dbService = this.baseMapper.selectById(serviceId);
            if(null != dbService){
                dbService.setIsValid(-1);
                return this.baseMapper.updateById(dbService);
            }
        }
        return 0;

    }
	@Override
	public List<Service> queryListAE(String businessScope, Integer orderId) {
		List<Service> listResult = new ArrayList<Service>();
    	List<Service> list = baseMapper.queryList(SecurityUtils.getUser().getOrgId(),businessScope);
    	if("AE".equals(businessScope)) {
    		//判断当前签约公司是否设置
//    		boolean flag = true;
    		boolean flag = false;
    		Map map = rountingSignMapper.getOrgConfigForWhere(SecurityUtils.getUser().getOrgId(),"AE");
    		if(map!=null&&map.containsKey("rounting_sign")) {
    			AfOrder order = afOrderMapper.selectById(orderId);
    			if("true".equals(map.get("rounting_sign").toString())) {
        			if(order!=null&&!StringUtils.isEmpty(order.getBusinessProduct())&&(map.get("rounting_sign_business_product").toString().contains(order.getBusinessProduct()))) {
    				  flag = true;
	    			}
    			}
    		}
    		if(flag) {
    			for(Service o: list) {
    				if("干线".equals(o.getServiceType())&&"空运费".equals(o.getServiceNameCn())) {
    					//todo
    				}else {
    					listResult.add(o);
    				}
    			}
    		}else {
    			listResult.addAll(list);
    		}
    	}
    	return listResult;
	}
}
