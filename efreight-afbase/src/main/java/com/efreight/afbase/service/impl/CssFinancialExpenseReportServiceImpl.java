package com.efreight.afbase.service.impl;

import com.efreight.afbase.entity.CssFinancialExpenseReport;
import com.efreight.afbase.entity.CssFinancialExpenseReportFiles;
import com.efreight.afbase.dao.CssFinancialExpenseReportFilesMapper;
import com.efreight.afbase.dao.CssFinancialExpenseReportMapper;
import com.efreight.afbase.service.CssFinancialExpenseReportService;
import com.efreight.afbase.utils.AmountToCNUtils;
import com.efreight.afbase.utils.FilePathUtils;
import com.efreight.common.core.utils.JxlsUtils;
import com.efreight.common.security.util.SecurityUtils;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * <p>
 * CSS 财务费用报销 服务实现类
 * </p>
 *
 * @author caiwd
 * @since 2020-10-14
 */
@Service
@AllArgsConstructor
@Slf4j
public class CssFinancialExpenseReportServiceImpl extends ServiceImpl<CssFinancialExpenseReportMapper, CssFinancialExpenseReport> implements CssFinancialExpenseReportService {

	
	private final CssFinancialExpenseReportFilesMapper fileMapper;
	@Override
	public IPage<CssFinancialExpenseReport> getListPage(Page page, CssFinancialExpenseReport bean) {
		bean.setOrgId(SecurityUtils.getUser().getOrgId());
		bean.setUserId(SecurityUtils.getUser().getId());
		IPage<CssFinancialExpenseReport> afPage = baseMapper.getListPage(page, bean);
		return afPage;
	}

	@Override
	public Map getDeptInfo() {
		Integer deptId = SecurityUtils.getUser().getDeptId();
		Map map = baseMapper.getDeptInfo(deptId);
		map.put("user_name", SecurityUtils.getUser().getUserCname());
		return map;
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public CssFinancialExpenseReport doSave(CssFinancialExpenseReport bean) {
		//申请人  部门  审批人
		Integer deptId = SecurityUtils.getUser().getDeptId();
		Map map = baseMapper.getDeptInfo(deptId);
		if(map.get("manager_id")!=null) {
			bean.setApprovalDeptManagerId(Integer.valueOf(map.get("manager_id").toString()));//审批人
			if(map.get("dept_user_name")!=null) {
				bean.setApprovalDeptManagerName(map.get("dept_user_name").toString());//审批人名称
			}
			bean.setDeptManagerId(Integer.valueOf(map.get("manager_id").toString()));//申请人负责人ID
		}
		bean.setCreatorId(SecurityUtils.getUser().getId());//申请人
		bean.setDeptId(deptId);//申请人所在部门
		bean.setCreateTime(LocalDateTime.now());
		bean.setCreatorName(SecurityUtils.getUser().getUserCname());
		bean.setOrgId(SecurityUtils.getUser().getOrgId());
		//查询当前签约公司 报销单 最大号
		String code = this.getNum();
		bean.setCode(code);
		List<CssFinancialExpenseReport> list = baseMapper.getInfo(bean);
		String strTwo = ""; 
		if(list!=null&&list.size()>0) {
			strTwo = list.get(0).getExpenseReportNum();
			strTwo = strTwo.substring(strTwo.length()-4,strTwo.length());
			Integer strInt = Integer.valueOf(strTwo);
			if(strInt>=9999) {
				//抛出异常 不可创建
				throw new RuntimeException("当前签约公司报销单号已超出上限");
			}else {
				strInt = strInt+1;
				if(strInt<10) {
					strTwo = "000"+strInt;
				}else if(10<=strInt&&strInt<100) {
					strTwo = "00"+strInt;
				}else if(100<=strInt&&strInt<1000) {
					strTwo = "0"+strInt;
				}else {
					strTwo = ""+strInt;
				}
			}
		}else {
			strTwo = "0001";
		}
		//生成报销单号
		String str = code+strTwo;
		bean.setExpenseReportNum(str);
		baseMapper.insert(bean);
		//检查上传文件插入
		if(!StringUtils.isEmpty(bean.getFileStrs())) {
			JSONArray jsonArr = JSONArray.parseArray(bean.getFileStrs());
			for (int i = 0; i < jsonArr.size(); i++) {
                JSONObject job = jsonArr.getJSONObject(i);
                CssFinancialExpenseReportFiles fileMap = new CssFinancialExpenseReportFiles();
                fileMap.setExpenseReportId(bean.getExpenseReportId());
                fileMap.setOrgId(SecurityUtils.getUser().getOrgId());
                fileMap.setFileName(job.getString("fileName"));
                fileMap.setFileRemark(job.getString("fileRemark"));
                fileMap.setFileType(job.getString("fileType"));
                fileMap.setFileUrl(job.getString("fileUrl"));
                fileMapper.insert(fileMap);
			}
		}
		return bean;
	}
	
    private String getNum() {
        Date dt = new Date();
        String year = String.format("%ty", dt);
        String mon = String.format("%tm", dt);
        String day = String.format("%td", dt);
        return "ER-" + year + mon + day;
    }

	@Override
	public CssFinancialExpenseReport view(Integer expenseReportId) {
		CssFinancialExpenseReport cfer = baseMapper.selectById(expenseReportId);
		if(cfer.getDeptId()!=null) {
			Map map = baseMapper.getDeptInfo(cfer.getDeptId());
			if(map!=null&&map.containsKey("dept_name")) {
				cfer.setDeptName(map.get("dept_name").toString());
			}
		}
		//查询 上传附件
		CssFinancialExpenseReportFiles fileMap = new CssFinancialExpenseReportFiles();
		fileMap.setExpenseReportId(cfer.getExpenseReportId());
		fileMap.setOrgId(SecurityUtils.getUser().getOrgId());
		List<CssFinancialExpenseReportFiles> listFiles = fileMapper.getList(fileMap);
		cfer.setListFiles(listFiles);
		return cfer;
	}

	@Override
	public CssFinancialExpenseReport modify(CssFinancialExpenseReport bean) {
		CssFinancialExpenseReport cfer = baseMapper.selectById(bean.getExpenseReportId());
		if(cfer==null) {
			throw new RuntimeException("当前报销单不存在");
		}else {
			cfer.setExpenseReportDate(bean.getExpenseReportDate());
			cfer.setExpenseReportNum(bean.getExpenseReportNum());
			cfer.setAttachedDocument(bean.getAttachedDocument());
			cfer.setExpenseAmount(bean.getExpenseAmount());
			cfer.setExpensesUse(bean.getExpensesUse());
			cfer.setExpenseReportRemark(bean.getExpenseReportRemark());
			cfer.setPaymentMethod(bean.getPaymentMethod());
			cfer.setExpenseReportMode(bean.getExpenseReportMode());
			if(StringUtils.isNotEmpty(bean.getExpenseReportStatus())) {
				cfer.setExpenseReportStatus(bean.getExpenseReportStatus());
			}
			cfer.setEditorId(SecurityUtils.getUser().getId());
			cfer.setEditorName(SecurityUtils.getUser().getUserCname());
			cfer.setEditTime(LocalDateTime.now());
			baseMapper.updateById(cfer);
			
			//检查上传文件插入
			//先删除 后插入
			CssFinancialExpenseReportFiles queryParam = new CssFinancialExpenseReportFiles();
			queryParam.setOrgId(SecurityUtils.getUser().getOrgId());
			queryParam.setExpenseReportId(cfer.getExpenseReportId());
			List<CssFinancialExpenseReportFiles> list = fileMapper.getList(queryParam);
			if(list!=null&&list.size()>0) {
				list.stream().forEach(o->{
					fileMapper.deleteById(o.getExpenseReportFileId());
				});
			}
			if(!StringUtils.isEmpty(bean.getFileStrs())) {
				JSONArray jsonArr = JSONArray.parseArray(bean.getFileStrs());
				for (int i = 0; i < jsonArr.size(); i++) {
	                JSONObject job = jsonArr.getJSONObject(i);
	                CssFinancialExpenseReportFiles fileMap = new CssFinancialExpenseReportFiles();
	                fileMap.setExpenseReportId(bean.getExpenseReportId());
	                fileMap.setOrgId(SecurityUtils.getUser().getOrgId());
	                fileMap.setFileName(job.getString("fileName"));
	                fileMap.setFileRemark(job.getString("fileRemark"));
	                fileMap.setFileType(job.getString("fileType"));
	                fileMap.setFileUrl(job.getString("fileUrl"));
	                fileMapper.insert(fileMap);
				}
			}
		}
		return cfer;
	}

	@Override
	public CssFinancialExpenseReport modifyStatus(CssFinancialExpenseReport bean) {
		CssFinancialExpenseReport cfer = baseMapper.selectById(bean.getExpenseReportId());
		if(cfer==null) {
			throw new RuntimeException("当前报销单不存在");
		}else {
			cfer.setExpenseReportStatus(bean.getExpenseReportStatus());
			if("已审批".equals(bean.getExpenseReportStatus())) {
				cfer.setApprovalDeptManagerTime(LocalDateTime.now());
			}
			cfer.setEditorId(SecurityUtils.getUser().getId());
			cfer.setEditorName(SecurityUtils.getUser().getUserCname());
			cfer.setEditTime(LocalDateTime.now());
			baseMapper.updateById(cfer);
		}
		return cfer;
	}

	@Override
	public Map getSubject() {
		//费用科目
		List<Map> listOne = baseMapper.getSubject(SecurityUtils.getUser().getOrgId(),"费用");
		//银行科目
		List<Map> listTwo = baseMapper.getSubjectBank(SecurityUtils.getUser().getOrgId(),"付款");
		Map map = new HashMap();
		map.put("oneList", listOne);
		map.put("twoList", listTwo);
		return map;
	}

	@Override
	public CssFinancialExpenseReport audit(CssFinancialExpenseReport bean) {
		bean.setExpenseFinancialAccountCode(bean.getExpenseFinancialAccountCode());
		bean.setExpenseFinancialAccountName(bean.getExpenseFinancialAccountName());
		bean.setBankFinancialAccountCode(bean.getBankFinancialAccountCode());
		bean.setBankFinancialAccountName(bean.getBankFinancialAccountName());
		bean.setExpenseReportStatus("已审核");
		bean.setEditorId(SecurityUtils.getUser().getId());
		bean.setEditorName(SecurityUtils.getUser().getUserCname());
		bean.setEditTime(LocalDateTime.now());
		bean.setApprovalFinancialUserId(SecurityUtils.getUser().getId());
		bean.setApprovalFinancialUserName(SecurityUtils.getUser().getUserCname());
		bean.setApprovalFinancialTime(LocalDateTime.now());
	    baseMapper.updateAudit(bean);
		return bean;
	}

	@Override
	public CssFinancialExpenseReport payment(CssFinancialExpenseReport bean) {
		bean.setExpenseReportStatus("已付款");
		bean.setEditorId(SecurityUtils.getUser().getId());
		bean.setEditorName(SecurityUtils.getUser().getUserCname());
		bean.setEditTime(LocalDateTime.now());
		bean.setPayerId(SecurityUtils.getUser().getId());
		bean.setPayerName(SecurityUtils.getUser().getUserCname());
		bean.setPayerTime(LocalDateTime.now());
	    baseMapper.updatePayment(bean);
		return bean;
	}

	@Override
	public void delete(CssFinancialExpenseReport bean) {
		baseMapper.deleteIds(bean);
	}

	@Override
	public void print(CssFinancialExpenseReport bean) {
		List<CssFinancialExpenseReport> list = baseMapper.getPrint(bean);
		if(list!=null&&list.size()>0) {
			Map resultMap = new HashMap();
			for(CssFinancialExpenseReport mp:list) {
				mp.setOrgName(bean.getOrgName());
				//时间 年
				mp.setYear(mp.getExpenseReportDate().getYear());
				//月
				mp.setMonth(mp.getExpenseReportDate().getMonthValue());
				//日
				mp.setDay(mp.getExpenseReportDate().getDayOfMonth());
				//申请时间
				mp.setCreateTimeM(mp.getCreateTime().format(DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm")));
				//付款方式
				if("现金".equals(mp.getPaymentMethod())) {
					mp.setPaymentMethodOne("√");
				}else if("支票".equals(mp.getPaymentMethod())) {
					mp.setPaymentMethodTwo("√");
				}else {
					mp.setPaymentMethodThree("√");
				}
				//报销性质
				if("对公".equals(mp.getExpenseReportMode())) {
					mp.setExpenseReportModeOne("√");
				}else {
					mp.setExpenseReportModeTwo("√");
				}
                //大写金额
				mp.setExpenseAmountMax(AmountToCNUtils.number2CNMontrayUnit(mp.getExpenseAmount()));
				//审批时间
				if(mp.getApprovalDeptManagerTime()!=null) {
					mp.setApprovalDeptManagerTimeStr(mp.getApprovalDeptManagerTime().format(DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm")));
				}
				//复核时间
				if(mp.getApprovalFinancialTime()!=null) {
					mp.setApprovalFinancialTimeStr(mp.getApprovalFinancialTime().format(DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm")));
				}
				//费用科目
				if(StringUtils.isNotEmpty(mp.getExpenseFinancialAccountCode())) {
					mp.setExpenseFinancialAccount(mp.getExpenseFinancialAccountCode()+" "+mp.getExpenseFinancialAccountName());
				}
				//付款科目
				if(StringUtils.isNotEmpty(mp.getBankFinancialAccountCode())) {
					mp.setBankFinancialAccount(mp.getBankFinancialAccountCode()+" "+mp.getBankFinancialAccountName());
				}
				//会计
				
				//会计时间
				
				//出纳
				
				//出纳时间
				
				//当前时间
				
				mp.setTimeStr(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss")));
				
			}
			resultMap.put("mapList", list);
			JxlsUtils.exportExcelWithLocalModel(FilePathUtils.filePath + "/PDFtemplate/CSS_FINANCIAL_EXPENSE_REPORT.xlsx", resultMap);
		}
		
	}

	@Override
	public List<Map> getOrguser() {
		return baseMapper.getOrguser(SecurityUtils.getUser().getOrgId());
	}

}
