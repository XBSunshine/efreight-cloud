package com.efreight.prm.service.impl;

import com.alibaba.druid.util.StringUtils;
import com.efreight.common.core.jms.MailSendService;
import com.efreight.common.security.util.SecurityUtils;
import com.efreight.prm.dao.CoopShareMapper;
import com.efreight.prm.entity.CoopShare;
import com.efreight.prm.entity.CoopShareEmail;
import com.efreight.prm.entity.PrmCoopShareFields;
import com.efreight.prm.entity.ShareFields;
import com.efreight.prm.service.CoopShareService;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RefreshScope
public class CoopShareServiceImpl implements CoopShareService{
	@Autowired
	private CoopShareMapper coopShareMapper;
	@Autowired
	private MailSendService mailSendService;
	
	
	@Override
	public Map<String, Object> getPage(Integer current, Integer size, CoopShare coopShare) {
	   Page<CoopShare> page = PageHelper.startPage(current, size);
	   coopShare.setOrgId(SecurityUtils.getUser().getOrgId());
	   if(!StringUtils.isEmpty(coopShare.getCoopType())) {
		   coopShare.setCoopTypes(coopShare.getCoopType().split(","));
	   }
       List<CoopShare> list = coopShareMapper.selectList(coopShare);
       long total = page.getTotal();
       HashMap<String, Object> resultMap = new HashMap<>();
       resultMap.put("records", list);
       resultMap.put("total", total);
       return resultMap;
	}


	@Override
	public void modifyShare(Integer coopId, String shareType) {
		CoopShare coopShare = new CoopShare();
		coopShare.setIsShare(shareType);
		coopShare.setCoopId(coopId);
		coopShareMapper.modifyShare(coopShare);
	}


	@Override
	public void coopShareWithEmail(CoopShareEmail coopShareEmail) throws Exception{
		coopShareEmail.checkRequired();
        String content = this.buildCoopShareContent(coopShareEmail);
        try {
            mailSendService.sendHtmlMailNew(true, coopShareEmail.getToUsers().split(";"), coopShareEmail.getCcUsers().split(";"), null, coopShareEmail.getSubject(), content, null);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        } 
		
	}
	
	private String  buildCoopShareContent(CoopShareEmail coopShareEmail) {
        String content = coopShareEmail.getContent();
        StringBuilder builder = new StringBuilder();
        builder.append(content.replaceAll("\n", "<br />"));
        builder.append("<br />");
        builder.append("<br />");
        builder.append("网址：");
        builder.append("<a href=\"");
        builder.append(coopShareEmail.getWebsite());
        builder.append("\">");
        builder.append(coopShareEmail.getWebsite());
        builder.append("</a>");
        return builder.toString();
	}


	@Override
	public void coopShareBind(CoopShareEmail coopShareEmail) throws Exception {
		//先变更当前orgId  所选的客商资料的信息
		coopShareMapper.updateOperCoop(coopShareEmail);
		//在变更申请分享的orgId 所选的客商资料信息
		coopShareMapper.updateShareCoop(coopShareEmail);
	}


	@Override
	public CoopShare getCoopShareInfo(Integer orgId, Integer coopId) {
		CoopShare coopShare = new CoopShare();
		coopShare.setOrgId(orgId);
		coopShare.setCoopId(coopId);
		return coopShareMapper.getCoopShareInfo(coopShare);
	}


	@Override
	public List<ShareFields> sharefieldsInfo(String businessScope,Integer coopId) {
		HashMap map = new HashMap();
		map.put("businessScope", businessScope);
		//查询当前业务范畴下的 数据
		List<ShareFields> list = coopShareMapper.getSharefields(map);
		
		map.put("coopId", coopId);
		map.put("orgId", SecurityUtils.getUser().getOrgId());
		//查询当前业务范畴下的 数据
		List<PrmCoopShareFields> listP  = coopShareMapper.getPrmCoopShareFields(map);
		if(list!=null&&list.size()>0) {
			//赋值数据
			list.stream().forEach(sf->{
				if(listP!=null&&listP.size()>0) {
					 Optional<PrmCoopShareFields> firstP= listP.stream().filter(pcsf->sf.getName().equals(pcsf.getFieldsName())).findFirst();
					 if (firstP.isPresent()) {
						 sf.setIsShare(firstP.get().getIsShare());
						 sf.setIsSubscribe(firstP.get().getIsSubscribe());
					 }
				}
			});
			//分组封装树结构
		List<ShareFields> listTree = list.stream().filter(sf->StringUtils.isEmpty(sf.getPCode())).collect(Collectors.toList());
		listTree.stream().forEach(sf->{
			List<ShareFields> listTreeNew = list.stream().filter(sfTree->sf.getCode().equals(sfTree.getPCode())).collect(Collectors.toList());
			sf.setChildren(listTreeNew);
		});
		return listTree;
	  }
		return list;
	}

	@Transactional(rollbackFor = Exception.class)
	@Override
	public void saveShareFields(PrmCoopShareFields prmCoopShareFields) {
		//先处理删除 当前签约公司下的 指定客商资料ID 的对应业务范畴的  存储数据
		HashMap map = new HashMap();
		map.put("orgId", SecurityUtils.getUser().getOrgId());
		map.put("coopId", prmCoopShareFields.getCoopId());
		map.put("businessScope", prmCoopShareFields.getBusinessScope());
		coopShareMapper.deleteShareFields(map);
		//新增
		if(prmCoopShareFields.getSubList()!=null&&prmCoopShareFields.getSubList().size()>0) {
			prmCoopShareFields.getSubList().stream().forEach(p->{
				HashMap saveMap = new HashMap();
				saveMap.put("name", p.getName());
				saveMap.put("isShare", StringUtils.isEmpty(p.getIsShare())?0:p.getIsShare());
				saveMap.put("isSubscribe", StringUtils.isEmpty(p.getIsSubscribe())?0:p.getIsSubscribe());
				saveMap.put("coopId", prmCoopShareFields.getCoopId());
				saveMap.put("orgId", SecurityUtils.getUser().getOrgId());
				saveMap.put("businessScope", prmCoopShareFields.getBusinessScope());
				saveMap.put("creatorId", SecurityUtils.getUser().getId());
				saveMap.put("creatorName",SecurityUtils.getUser().buildOptName());
				coopShareMapper.saveShareFields(saveMap);
			});
		}
		
	}

	@Override
	public CoopShare checkBindNew(Integer orgId, Integer coopOrgId) {
		CoopShare coopShare = new CoopShare();
		coopShare.setOrgId(orgId);
		coopShare.setCoopOrgId(coopOrgId);
		return coopShareMapper.getCoopShareInfo(coopShare);
	}

}
