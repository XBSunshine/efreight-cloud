package com.efreight.hrs.service.impl;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.efreight.common.core.utils.ExportExcel;
import com.efreight.hrs.dao.OrgCouldUserMapper;
import com.efreight.hrs.entity.OrgCouldUser;
import com.efreight.hrs.entity.OrgCouldUserExcel;
import com.efreight.hrs.service.OrgCouldUserService;

@Service
public class OrgCouldUserServiceImpl extends ServiceImpl<OrgCouldUserMapper, OrgCouldUser> implements OrgCouldUserService{

	@Override
	public IPage<OrgCouldUser> queryCouldUser(Page page,OrgCouldUser user) {
		if("".equals(user.getIsStatus())) {
			user.setIsStatus(null);
		}
		if("".equals(user.getDemandPersonId())) {
			user.setDemandPersonId(null);
		}
		if("".equals(user.getOrgEditionId())) {
			user.setOrgEditionId(null);
		}
		if("".equals(user.getOrgType())) {
			user.setOrgType(null);
		}
		
		return baseMapper.getOrgCouldUserList(page, user);
	}

	@SuppressWarnings("unlikely-arg-type")
	@Override
	public void exportExcelList(OrgCouldUser user) {
		if("".equals(user.getIsStatus())) {
			user.setIsStatus(null);
		}
		if("".equals(user.getDemandPersonId())) {
			user.setDemandPersonId(null);
		}
		if("".equals(user.getOrgEditionId())) {
			user.setOrgEditionId(null);
		}
		if("".equals(user.getOrgType())) {
			user.setOrgType(null);
		}
		List<OrgCouldUser> excelUser = baseMapper.getOrgCouldUserExcel(user);
		List<OrgCouldUserExcel> list = new ArrayList<OrgCouldUserExcel>();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
		DateTimeFormatter formatterTwo = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		if(excelUser!=null&&excelUser.size()>0) {
			list = excelUser.stream().map(o->{
				OrgCouldUserExcel excel = new OrgCouldUserExcel();
				excel.setOrgType(o.getOrgType()==1?"个人":"公司");
				excel.setOrgCode(o.getOrgCode());
				excel.setOrgName(o.getOrgName());
//				excel.setOneStopCode(o.getOneStopCode());
				Boolean flagUser = true;
				if(o.getIntendedUser() == null){
					o.setIntendedUser(-1);
				}
				if(o.getOrgEditionName().indexOf("内部")>0 || "标准版".equals(o.getOrgEditionName())  || "专业版".equals(o.getOrgEditionName())){
					flagUser = false;
				}

				Integer subscriptionNum = Optional.ofNullable(o.getSubscriptionNum()).orElse(-1);
				if((o.getOrgOrderCount() > 0 || o.getOrgCoopCount() > 2 || o.getIntendedUser() == 1 || subscriptionNum > 0) && o.getIntendedUser() != 0 && flagUser){
					excel.setIntendedUser("是");
				}

				excel.setDemandPersonName(o.getDemandPersonName());
				excel.setOrgEditionName(o.getOrgEditionName());
				excel.setUserCount(o.getUserCount());
				if(o.getCreateTime()!=null) {
					excel.setCreateTime(o.getCreateTime().format(formatter));
				}
				LocalDateTime nowTime= LocalDateTime.now();
				if(o.getStopDate()!=null) {
					excel.setStopDate(o.getStopDate().format(formatter));
					if(!o.isOrgStatus()||(nowTime.isAfter(o.getStopDate()))) {
						excel.setStatusFlag("是");
					}
				}
                if(o.getCreateTimeAf()!=null&&o.getCreateTimeSc()!=null) {
                	if(o.getCreateTimeAf().isAfter(o.getCreateTimeSc())) {
                		excel.setOrderTime(o.getCreateTimeAf().format(formatterTwo));
                	}else {
                		excel.setOrderTime(o.getCreateTimeSc().format(formatterTwo));
                	}
                }else if(o.getCreateTimeAf()!=null&&o.getCreateTimeSc()==null) {
                	excel.setOrderTime(o.getCreateTimeAf().format(formatterTwo));
                }else if(o.getCreateTimeAf()==null&&o.getCreateTimeSc()!=null){
                	excel.setOrderTime(o.getCreateTimeSc().format(formatterTwo));
                }else {
                	excel.setOrderTime("");
                }
				if(o.getSubscriptionTime()!=null) {
					excel.setSubscriptionTime(o.getSubscriptionTime().format(formatterTwo));
				}
				excel.setSubscriptionNum(o.getSubscriptionNum());
				excel.setOrgUserCount(o.getOrgUserCount());
				excel.setOrgCoopCount(o.getOrgCoopCount());
				excel.setOrgOrderCount(o.getOrgOrderCount());
				excel.setAdminName(o.getAdminName());
				excel.setAdminEmail(o.getAdminEmail());
				excel.setAdminTel(o.getAdminTel());
				excel.setOrgRemark(o.getOrgRemark());
			  return excel;
			}).collect(Collectors.toList());
			
			ExportExcel<OrgCouldUserExcel> ex = new ExportExcel<OrgCouldUserExcel>();
			String[] headers = {"账户类型", "企业编码", "签约公司名称", "意向用户", "需求人", "版本类型", "用户限制", "创建日期", "失效日期","是否失效","用户数量", "客商数量", "订单数量","订单时间" , "订阅数量","订阅日期","注册来源","管理员账户", "管理员邮箱", "管理员电话", "备注信息"};
			RequestAttributes ra = RequestContextHolder.getRequestAttributes();
			ex.exportExcel(((ServletRequestAttributes)ra).getResponse(), "导出EXCEL", headers, list, "Export");
		}
		
	}

}
