package com.efreight.afbase.controller;


import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.efreight.afbase.entity.AfOrderIdentify;
import com.efreight.afbase.entity.AfOrderIdentifyDetail;
import com.efreight.afbase.service.AfOrderIdentifyService;
import com.efreight.common.core.exception.CheckedException;
import com.efreight.common.security.util.MessageInfo;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author mayt
 * @since 2020-10-12
 */
@RestController
@AllArgsConstructor
@RequestMapping("/identify")
@Slf4j
public class AfOrderIdentifyController {
    private final AfOrderIdentifyService service;

    /**
     * 1、暂存
     * 2、申报，使用ID申报和对象申报
     * 3、查询
     * 4、审核回执接收
     * 5、删除申请
     * 6、删除操作、批量删除
     * 7、查询单条数据
     */

    /**
     * 查询
     * @param orderId
     * @return
     */
    @GetMapping("querylist")
    public MessageInfo getAfOrderIdentifyList(Integer orderId) {
        return MessageInfo.ok(service.getAfOrderIdentifyList(orderId));
    }

    /**
     * 查询
     * @param orderId
     * @return
     */
    @GetMapping("query")
    public MessageInfo getAfOrderIdentifies(Integer orderId) {
        return MessageInfo.ok(service.getAfOrderIdentify(orderId));
    }

    /**
     * 暂存
     * @param afOrderIdentify
     * @return
     */
    @PostMapping("save")
    public MessageInfo addAfOrderIdentify(@Valid @RequestBody AfOrderIdentify afOrderIdentify){
        try{
            MessageInfo messageInfo = checkAfOrderIdentify2(afOrderIdentify);
            if(messageInfo!=null){
                return  messageInfo;
            }
            afOrderIdentify.setStatus("save");
            return afterOparete(service.saveAfOrderIdentify(afOrderIdentify),"保存");
        }catch (CheckedException e){
            log.error("保存鉴定信息时出现异常："+ e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }catch (Exception e){
            log.error("保存鉴定信息时出现异常："+ e.getMessage());
            return MessageInfo.failed("保存时出现异常！");
        }
    }

    private MessageInfo checkAfOrderIdentify2(AfOrderIdentify afOrderIdentify){
        if(afOrderIdentify.getOrderId()==null
                &&afOrderIdentify.getOrderId()<=0){
            return MessageInfo.failed( "保存鉴定信息失败！没有对应的订单信息");
        }
        
        if(StrUtil.isEmpty(afOrderIdentify.getAgentHandlerName())){
            return MessageInfo.failed( "保存鉴定信息失败！请填写操作人中文姓名");
        }
//        if(StrUtil.isEmpty(afOrderIdentify.getReportImgUrls())){
//            return MessageInfo.failed( "保存鉴定信息失败！请填写上传文件");
//        }
        boolean hasUploadFile = false;
        List<AfOrderIdentifyDetail> details = afOrderIdentify.getAfOrderIdentifyDetailList();
        StringBuffer buffer = new StringBuffer();
        for (int i =0;details!=null && i<details.size();i++) {
            AfOrderIdentifyDetail detail = details.get(i);
            if(StrUtil.isEmpty(detail.getReportIssueNo())){
                buffer.append("第").append(i+1).append("行的报告编号不能为空，请填写！");
            }
            if(StrUtil.isEmpty(detail.getReportIssueOrgan())){
                buffer.append("第").append(i+1).append("行的鉴定机构不能为空，请填写！");
            }
            if(detail.getReportIssueDate()==null){
                buffer.append("第").append(i+1).append("行的签发日期不能为空，请填写！");
            }
            if(!StrUtil.isEmpty(detail.getReportImgUrl())){
                hasUploadFile =true;
            }
        }
        if(!hasUploadFile){
            return MessageInfo.failed( "保存鉴定信息失败！请填写上传文件");
        }

        if(buffer.length()>0){
            return MessageInfo.failed( "保存鉴定信息失败！" + buffer.toString());
        }
        return null;
    }
    private MessageInfo checkAfOrderIdentify(AfOrderIdentify afOrderIdentify){
    	if(afOrderIdentify.getOrderId()==null
    			&&afOrderIdentify.getOrderId()<=0){
    		return MessageInfo.failed( "保存鉴定信息失败！没有对应的订单信息");
    	}
    	if(StrUtil.isEmpty(afOrderIdentify.getAwbNumber())){
//    		return MessageInfo.failed( "保存鉴定信息失败！没有对应的运单信息");
    		return MessageInfo.failed( "对不起，您的订单缺少运单号，请先完善订单之后再发送！");
    	}
    	if(StrUtil.isEmpty(afOrderIdentify.getCarrierId())){
//    		return MessageInfo.failed( "保存鉴定信息失败！请填写承运人信息");
    		return MessageInfo.failed( "对不起，您未填写航司信息，请填写航司信息后再发送！");
    	}
    	if(StrUtil.isEmpty(afOrderIdentify.getAgentHandlerName())){
    		return MessageInfo.failed( "保存鉴定信息失败！请填写操作人中文姓名");
    	}
//        if(StrUtil.isEmpty(afOrderIdentify.getReportImgUrls())){
//            return MessageInfo.failed( "保存鉴定信息失败！请填写上传文件");
//        }
    	boolean hasUploadFile = false;
    	List<AfOrderIdentifyDetail> details = afOrderIdentify.getAfOrderIdentifyDetailList();
    	StringBuffer buffer = new StringBuffer();
    	for (int i =0;details!=null && i<details.size();i++) {
    		AfOrderIdentifyDetail detail = details.get(i);
    		if(StrUtil.isEmpty(detail.getReportIssueNo())){
    			buffer.append("第").append(i+1).append("行的报告编号不能为空，请填写！");
    		}
    		if(StrUtil.isEmpty(detail.getReportIssueOrgan())){
    			buffer.append("第").append(i+1).append("行的鉴定机构不能为空，请填写！");
    		}
    		if(detail.getReportIssueDate()==null){
    			buffer.append("第").append(i+1).append("行的签发日期不能为空，请填写！");
    		}
    		if(!StrUtil.isEmpty(detail.getReportImgUrl())){
    			hasUploadFile =true;
    		}
    	}
    	if(!hasUploadFile){
    		return MessageInfo.failed( "保存鉴定信息失败！请填写上传文件");
    	}
    	
    	if(buffer.length()>0){
    		return MessageInfo.failed( "保存鉴定信息失败！" + buffer.toString());
    	}
    	return null;
    }

    /**
     * 申报，通过ID申报
     * @param orderIdentifyId
     * @return
     */
    @PostMapping("declareById")
    public MessageInfo declareAfOrderIdentify(@Valid @RequestParam("orderIdentifyId") Integer orderIdentifyId){
        try{
            return afterOparete(service.declare(orderIdentifyId),"申报");
        }catch (CheckedException e){
            log.error("申报鉴定信息时出现异常："+ e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }catch (Exception e){
            log.error("申报鉴定信息时出现异常："+ e.getMessage());
            return MessageInfo.failed("申报鉴定信息时出现异常");
        }
    }

    public MessageInfo afterOparete(boolean op_result,String typeName){
        if(op_result){
            return MessageInfo.ok();
        }else{
            return MessageInfo.failed(typeName + "鉴定信息失败！");
        }
    }

    /**
     * 申报
     * @param afOrderIdentify
     * @return
     */
    @PostMapping("declare")
    public MessageInfo declareAfOrderIdentify(@Valid @RequestBody AfOrderIdentify afOrderIdentify){
        try{
            MessageInfo messageInfo = checkAfOrderIdentify(afOrderIdentify);
            if(messageInfo!=null){
                return  messageInfo;
            }

            return afterOparete(service.declare(afOrderIdentify),"申报");
        }catch (CheckedException e){
            log.error("申报鉴定信息时出现异常："+ e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }catch (Exception e){
            log.error("申报鉴定信息时出现异常："+ e.getMessage());
            return MessageInfo.failed("申报鉴定信息时出现异常");
        }
    }

    /**
     * 删除
     * @param orderIdentifyId
     * @return
     */
    @PostMapping("delete/{orderIdentifyId}/{pageName}")
    public MessageInfo deleteAfOrderIdentify(@PathVariable Integer orderIdentifyId,@PathVariable String pageName){
    	try{
    		return afterOparete(service.deleteAfOrderIdentify(orderIdentifyId,pageName),"删除");
    	}catch (CheckedException e){
    		log.error("删除鉴定信息时出现异常："+ e.getMessage());
    		return MessageInfo.failed(e.getMessage());
    	}catch (Exception e){
    		log.error("删除鉴定信息时出现异常："+ e.getMessage());
    		return MessageInfo.failed("删除鉴定信息时出现异常");
    	}
    }

    /**
     * 删除申报
     * @param orderIdentifyId
     * @return
     */
    @PostMapping("deldeclare/{orderIdentifyId}/{pageName}")
    public MessageInfo deleteDeclareAfOrderIdentify(@PathVariable Integer orderIdentifyId,@PathVariable String pageName){
        try{
            return afterOparete(service.deleteDeclare(orderIdentifyId,pageName),"删除申报");
        }catch (CheckedException e){
            log.error("删除申报鉴定信息时出现异常："+ e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }catch (Exception e){
            log.error("删除申报鉴定信息时出现异常："+ e.getMessage());
            return MessageInfo.failed("删除申报鉴定信息时出现异常");
        }
    }

    /**
     * 审核
     * @param originalSyscode
     * @return
     */
    @PostMapping("audit")
    public MessageInfo audit(@Valid @RequestParam("syscode") Integer originalSyscode,@Valid @RequestParam("auditname") String auditName){
        try{
            return afterOparete(service.audit(originalSyscode,auditName),"审核");
        }catch (CheckedException e){
            log.error("审核鉴定信息时出现异常："+ e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }catch (Exception e){
            log.error("审核鉴定信息时出现异常："+ e.getMessage());
            return MessageInfo.failed("审核鉴定信息时出现异常");
        }
    }
}

