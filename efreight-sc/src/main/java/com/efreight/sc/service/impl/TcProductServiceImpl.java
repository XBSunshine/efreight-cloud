package com.efreight.sc.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.alibaba.druid.util.StringUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.efreight.common.core.feign.RemoteCoopService;
import com.efreight.common.security.util.SecurityUtils;
import com.efreight.common.security.vo.CoopVo;
import com.efreight.sc.dao.OrderFilesMapper;
import com.efreight.sc.dao.TcProductMapper;
import com.efreight.sc.entity.TcProduct;
import com.efreight.sc.service.AfVPrmCoopService;
import com.efreight.sc.service.CostService;
import com.efreight.sc.service.IncomeService;
import com.efreight.sc.service.LogService;
import com.efreight.sc.service.OrderContainerDetailsService;
import com.efreight.sc.service.OrderShipInfoService;
import com.efreight.sc.service.OrderShipperConsigneeService;
import com.efreight.sc.service.PortMaintenanceService;
import com.efreight.sc.service.ShipCompanyService;
import com.efreight.sc.service.TcProductService;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class TcProductServiceImpl extends ServiceImpl<TcProductMapper, TcProduct> implements TcProductService{
	
	private final RemoteCoopService remoteCoopService;
    
	@Override
	public IPage gePageList(Page page, TcProduct bean) {
		bean.setOrgId(SecurityUtils.getUser().getOrgId());
		LambdaQueryWrapper<TcProduct> wrapper = Wrappers.<TcProduct>lambdaQuery();
        wrapper.eq(TcProduct::getOrgId, bean.getOrgId());
        wrapper.eq(TcProduct::isProductStatus, true);
        wrapper.eq(TcProduct::getBusinessScope,bean.getBusinessScope());
        if(!StringUtils.isEmpty(bean.getProductType())) {
        	wrapper.eq(TcProduct::getProductType, bean.getProductType());
        }
        if(!StringUtils.isEmpty(bean.getProductName())) {
        	wrapper.like(TcProduct::getProductName, bean.getProductName());
        }
        if(!StringUtils.isEmpty(bean.getDepartureStation())) {
        	wrapper.like(TcProduct::getDepartureStation, bean.getDepartureStation());
        }
        if(!StringUtils.isEmpty(bean.getExitPort())) {
        	wrapper.like(TcProduct::getExitPort, bean.getExitPort());
        }
        if(!StringUtils.isEmpty(bean.getTransitStation())) {
        	wrapper.like(TcProduct::getTransitStation, bean.getTransitStation());
        }
        if(!StringUtils.isEmpty(bean.getArrivalStation())) {
        	wrapper.like(TcProduct::getArrivalStation, bean.getArrivalStation());
        }
        wrapper.orderByDesc(TcProduct::getProductId);

		if(!StringUtils.isEmpty(bean.getBookingAgentName())) {
			List<Integer> coopIds = remoteCoopService.listByCoopName(bean.getBookingAgentName()).getData().stream().map(coopVo -> coopVo.getCoop_id()).collect(Collectors.toList());
			if (coopIds != null && coopIds.size() > 0) {
				wrapper.in(TcProduct::getBookingAgentId, coopIds);
			}else{
				wrapper.eq(TcProduct::getProductId, null);
			}
		}
       IPage<TcProduct> resultPage = baseMapper.selectPage(page, wrapper);
       if(resultPage!=null&&resultPage.getRecords()!=null&&resultPage.getRecords().size()>0) {
    	   resultPage.getRecords().stream().forEach(o->{
    		    if(o.getBookingAgentId()!=null) {
    			   CoopVo coop = remoteCoopService.viewCoop(o.getBookingAgentId().toString()).getData();
       	           if (coop != null) {
       	              o.setBookingAgentName(coop.getCoop_name());
       	           }
    		    }
    		    if(o.getFreightUnitprice()!=null) {
    		    	o.setFreightPrice(o.getFreightUnitprice());
    		    	o.setFreightType("单价");
    		    }
    		    if(o.getFreightAmount()!=null) {
    		    	o.setFreightPrice(o.getFreightAmount());
    		    	o.setFreightType("总价");
    		    }
    		    if(o.getMsrUnitprice()!=null) {
    		    	o.setMsrPrice(o.getMsrUnitprice());
    		    	o.setMsrType("单价");
    		    }
    		    if(o.getMsrAmount()!=null){
    		    	o.setMsrPrice(o.getMsrAmount());
    		    	o.setMsrType("总价");
    		    }
    	   });
       }
		return resultPage;
	}

	@Override
	public void saveProduct(TcProduct bean) {
		bean.setOrgId(SecurityUtils.getUser().getOrgId());
		LambdaQueryWrapper<TcProduct> wrapper = Wrappers.<TcProduct>lambdaQuery();
        wrapper.eq(TcProduct::getOrgId, bean.getOrgId());
        wrapper.eq(TcProduct::getBusinessScope,bean.getBusinessScope());
        wrapper.eq(TcProduct::getProductName, bean.getProductName());
        wrapper.eq(TcProduct::isProductStatus, true);
        List<TcProduct> listCheck = list(wrapper);
        //查询校验
        if(listCheck!=null&&listCheck.size()>0) {
        	throw new RuntimeException("该产品名称已经存在");
        }
        if(bean.getFreightPrice()!=null) {
           if("单价".equals(bean.getFreightType())) {
        	   bean.setFreightUnitprice(bean.getFreightPrice());
           }else {
        	   bean.setFreightAmount(bean.getFreightPrice());
           }
        }
        if(bean.getMsrPrice()!=null) {
        	if("单价".equals(bean.getMsrType())) {
         	   bean.setMsrUnitprice(bean.getMsrPrice());
            }else {
         	   bean.setMsrAmount(bean.getMsrPrice());
            }
        }
        bean.setProductStatus(true);
        bean.setCreatorId(SecurityUtils.getUser().getId());
        bean.setCreatorName(SecurityUtils.getUser().buildOptName());
        bean.setCreatTime(LocalDateTime.now());
        baseMapper.insert(bean);
	}

	@Override
	public void modifyProduct(TcProduct bean) {
		bean.setOrgId(SecurityUtils.getUser().getOrgId());
		LambdaQueryWrapper<TcProduct> wrapper = Wrappers.<TcProduct>lambdaQuery();
		wrapper.ne(TcProduct::getProductId, bean.getProductId());
        wrapper.eq(TcProduct::getOrgId, bean.getOrgId());
        wrapper.eq(TcProduct::getBusinessScope,bean.getBusinessScope());
        wrapper.eq(TcProduct::getProductName, bean.getProductName());
        wrapper.eq(TcProduct::isProductStatus, true);
        List<TcProduct> listCheck = list(wrapper);
        if(listCheck!=null&&listCheck.size()>0) {
        	throw new RuntimeException("该产品名称已经存在");
        }
        if(bean.getFreightPrice()!=null) {
            if("单价".equals(bean.getFreightType())) {
         	   bean.setFreightUnitprice(bean.getFreightPrice());
         	   bean.setFreightAmount(null);
            }else {
         	   bean.setFreightAmount(bean.getFreightPrice());
         	   bean.setFreightUnitprice(null);
            }
         }
         if(bean.getMsrPrice()!=null) {
         	if("单价".equals(bean.getMsrType())) {
          	   bean.setMsrUnitprice(bean.getMsrPrice());
          	   bean.setMsrAmount(null);
             }else {
          	   bean.setMsrAmount(bean.getMsrPrice());
          	   bean.setMsrUnitprice(null);
             }
         }
        baseMapper.updateById(bean);
	}

	@Override
	public void deleteById(Integer productId) {
		TcProduct bean = baseMapper.selectById(productId);
		if(bean==null) {
			throw new RuntimeException("该产品不存在");
		}
		bean.setProductStatus(false);
		bean.setProductId(productId);
		baseMapper.updateById(bean);
	}

	@Override
	public TcProduct view(Integer productId) {
		TcProduct bean = baseMapper.selectById(productId);
		if(bean==null) {
			throw new RuntimeException("该产品不存在");
		}
		if(bean.getBookingAgentId()!=null) {
			CoopVo coop = remoteCoopService.viewCoop(bean.getBookingAgentId().toString()).getData();
	        if (coop != null) {
	        	bean.setBookingAgentName(coop.getCoop_name());
	        }
		}
        if(bean.getFreightUnitprice()!=null) {
        	bean.setFreightPrice(bean.getFreightUnitprice());
        	bean.setFreightType("单价");
	    }
	    if(bean.getFreightAmount()!=null) {
	    	bean.setFreightPrice(bean.getFreightAmount());
	    	bean.setFreightType("总价");
	    }
	    if(bean.getMsrUnitprice()!=null) {
	    	bean.setMsrPrice(bean.getMsrUnitprice());
	    	bean.setMsrType("单价");
	    }
	    if(bean.getMsrAmount()!=null){
	    	bean.setMsrPrice(bean.getMsrAmount());
	    	bean.setMsrType("总价");
	    }
		return bean;
	}

}
