package com.efreight.afbase.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.efreight.afbase.dao.SendMapper;
import com.efreight.afbase.entity.*;
import com.efreight.afbase.dao.AfOrderIdentifyMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.efreight.afbase.service.AfOrderIdentifyDetailService;
import com.efreight.afbase.service.AfOrderIdentifyService;
import com.efreight.afbase.service.AfOrderService;
import com.efreight.afbase.utils.RequestUtil;
import com.efreight.afbase.utils.XmlApiUtils;
import com.efreight.common.core.exception.CheckedException;
import com.efreight.common.security.util.SecurityUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author mayt
 * @since 2020-10-12
 */
@Service
@AllArgsConstructor
@Slf4j
public class AfOrderIdentifyServiceImpl extends ServiceImpl<AfOrderIdentifyMapper, AfOrderIdentify> implements AfOrderIdentifyService {

    private final AfOrderIdentifyDetailService detailService;
    private final SendMapper sendMapper;
    private final AfOrderService orderService;

    public List<AfOrderIdentify> getAfOrderIdentifyList(Integer orderId) {
        QueryWrapper<AfOrderIdentify> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("order_id",orderId);
        List<AfOrderIdentify> afOrderIdentifyList = baseMapper.selectList(queryWrapper);
        for (AfOrderIdentify afOrderIdentify:afOrderIdentifyList) {
            afOrderIdentify.setAfOrderIdentifyDetailList(detailService.getAfOrderIdentifiesDetailList(afOrderIdentify.getOrderIdentifyId()));
        }
        return afOrderIdentifyList;
    }

    @Override
    public AfOrderIdentify getAfOrderIdentify(Integer orderIdentifyId) {
        AfOrderIdentify afOrderIdentify = baseMapper.selectById(orderIdentifyId);
        if(afOrderIdentify==null){
            return null;
        }
        afOrderIdentify.setAfOrderIdentifyDetailList(detailService.getAfOrderIdentifiesDetailList(afOrderIdentify.getOrderIdentifyId()));
        return afOrderIdentify;
    }

    @Override
    public boolean saveAfOrderIdentify(AfOrderIdentify afOrderIdentify) {
        if(afOrderIdentify.getOrderIdentifyId()==null
            ||afOrderIdentify.getOrderIdentifyId()==0){
            afOrderIdentify.setCreateId(SecurityUtils.getUser().getId());
            afOrderIdentify.setCreatorName(SecurityUtils.getUser().getUsername());
            afOrderIdentify.setCreateDate(LocalDateTime.now());
            afOrderIdentify.setOrgId(SecurityUtils.getUser().getOrgId());
        }

        boolean save_result = saveOrUpdate(afOrderIdentify);

        if(afOrderIdentify.getOrderIdentifyId() > 0) {
            QueryWrapper<AfOrderIdentifyDetail> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("masterid", afOrderIdentify.getOrderIdentifyId());
            detailService.remove(queryWrapper);
        }

        for (AfOrderIdentifyDetail detail: afOrderIdentify.getAfOrderIdentifyDetailList()) {
            detail.setMasterid(afOrderIdentify.getOrderIdentifyId());
        }
        save_result= save_result && detailService.saveBatch(afOrderIdentify.getAfOrderIdentifyDetailList());
        return save_result;
    }

    private void updateOrderIdentify(Integer orderId){
        List<AfOrderIdentify> afOrderIdentifyList = getAfOrderAudiedIdentifyList(orderId);
        StringBuffer reportIssueOrganBuffer = new StringBuffer();
        StringBuffer reportIssueNoBuffer = new StringBuffer();
        for (AfOrderIdentify afOrderIdentify:afOrderIdentifyList) {
            for (AfOrderIdentifyDetail detail:afOrderIdentify.getAfOrderIdentifyDetailList()) {
                reportIssueOrganBuffer.append(detail.getReportIssueOrgan()).append(";");
                reportIssueNoBuffer.append(detail.getReportIssueNo()).append(";");
            }
        }
        if(reportIssueOrganBuffer.length()>0){
            reportIssueOrganBuffer.deleteCharAt(reportIssueOrganBuffer.length()-1);
        }
        if(reportIssueNoBuffer.length()>0){
            reportIssueNoBuffer.deleteCharAt(reportIssueNoBuffer.length()-1);
        }
        UpdateWrapper<AfOrder> updateWrapper = new UpdateWrapper<>();
        updateWrapper.set("appraisal_note",reportIssueNoBuffer.toString());
        updateWrapper.set("appraisal_company",reportIssueOrganBuffer.toString());
        updateWrapper.eq("order_id",orderId);
        orderService.update(updateWrapper);
    }

    @Override
    public boolean deleteAfOrderIdentify(Integer orderIdentifyId) throws Exception{
        AfOrderIdentify afOrderIdentifies= baseMapper.selectById(orderIdentifyId);
        if(afOrderIdentifies==null){
            log.info("该运单号的该鉴定信息不存在");
            throw new CheckedException("该运单号的该鉴定信息不存在");
        }
        if("send".equalsIgnoreCase(afOrderIdentifies.getStatus())){
            log.info("主单号:"+afOrderIdentifies.getAwbNumber() +"的该鉴定信息已申报目前无法删除");
            throw new CheckedException("对不起，该鉴定信息已申报目前无法删除，如您一定要删除，请先提交删除申请之后再进行删除操作！");
        }

        HashMap<String,Object> colomnMap = new HashMap<String,Object>();
        colomnMap.put("masterid",orderIdentifyId);
        return  detailService.removeByMap(colomnMap)&&removeById(orderIdentifyId);
    }

    @Override
    public boolean declare(AfOrderIdentify afOrderIdentify) throws Exception {
        AfOrderIdentify temp = baseMapper.selectById(afOrderIdentify.getOrderIdentifyId());
        if(temp !=null){
            if("delete".equalsIgnoreCase(temp.getStatus())){
                log.info("主单号:"+afOrderIdentify.getAwbNumber() +"的该鉴定信息已被删除，无法申报");
                throw new CheckedException("对不起，该鉴定信息已被删除，请重新创建鉴定证书！");
            }
            if("send".equalsIgnoreCase(temp.getStatus())){
                log.info("主单号:"+afOrderIdentify.getAwbNumber() +"的该鉴定信息已申报，无法再重复申报");
                throw new CheckedException("对不起，该鉴定信息已被申报，无需再重复申报！");
            }
            afOrderIdentify.setCreateId(temp.getCreateId());
            afOrderIdentify.setCreatorName(temp.getCreatorName());
            afOrderIdentify.setCreateDate(temp.getCreateDate());
        }
        return declareHandler(afOrderIdentify);
    }

    @Override
    public boolean declare(Integer orderIdentifyId) throws Exception {
        AfOrderIdentify afOrderIdentify= baseMapper.selectById(orderIdentifyId);
        if(afOrderIdentify==null){
            return false;
        }
        if("delete".equalsIgnoreCase(afOrderIdentify.getStatus())){
            log.info("主单号:"+afOrderIdentify.getAwbNumber() +"的该鉴定信息已被删除，无法申报");
            throw new CheckedException("对不起，该鉴定信息已被删除，请重新创建鉴定证书！");
        }
        afOrderIdentify.setAfOrderIdentifyDetailList(detailService.getAfOrderIdentifiesDetailList(orderIdentifyId));
        return declareHandler(afOrderIdentify);
    }
    private boolean declareHandler(AfOrderIdentify afOrderIdentify) throws Exception{
        OrgInterface config = sendMapper.getShippingBillConfig(SecurityUtils.getUser().getOrgId(), "AE_IDF_POST_MAWB");
        if(config==null){
            log.info("主单号:"+afOrderIdentify.getAwbNumber() +" 没有配置其鉴定信息传输身份，请联系管理员进行配置");
            throw new CheckedException("对不起，没有配置其鉴定信息传输身份，请联系管理员进行配置！");
        }
        String xml = structureXml(config.getAppid(),afOrderIdentify);
        String responseXml = RequestUtil.PosteFreightHttpEngine(config.getUrlPost(),xml);
        Document resultDoc = XmlApiUtils.parseXML(responseXml,true);
        if(!"1".equals(XmlApiUtils.getNodeText(resultDoc,"//ResultCode"))){
            throw new CheckedException("申报鉴定信息时出现异常！" + XmlApiUtils.getNodeText(resultDoc,"//ResultContent"));
        }
        String originalSyscode = XmlApiUtils.getNodeText(resultDoc,"//ServiceEntitySyscode");
        if(StrUtil.isEmpty(originalSyscode)){
            throw new CheckedException("申报鉴定信息失败！" );
        }

        afOrderIdentify.setOriginalSyscode(Long.parseLong(originalSyscode));
        afOrderIdentify.setStatus("send");
        afOrderIdentify.setDeclareId(SecurityUtils.getUser().getId());
        afOrderIdentify.setDeclareName(SecurityUtils.getUser().getUsername());
        afOrderIdentify.setDeclareDate(LocalDateTime.now());

        return saveAfOrderIdentify(afOrderIdentify);
    }

    private String structureXml(String appid,AfOrderIdentify afOrderIdentify){
        /**
         * 构造xml请求
         */
//        appid = "fJARMcET7E";
        String xml ="<Service>\n" +
                "    <ServiceURL>Identify_Adapter</ServiceURL>\n" +
                "    <ServiceAction>ApiSend</ServiceAction>\n" +
                "    <ServiceData>\n" +
                "        <Identify>\n" +
                "            <Forwarder>"+  appid +"</Forwarder>\n" +
                "            <AgentHandlerName>"+ afOrderIdentify.getAgentHandlerName() +"</AgentHandlerName>\n" +
                "            <Mawb>"+ afOrderIdentify.getAwbNumber() +"</Mawb>\n" +
                "            <Carrier>"+ afOrderIdentify.getCarrierId() +"</Carrier>\n" +
                "            <CargoTerminal>"+ afOrderIdentify.getCargoTerminal() +"</CargoTerminal>\n";

        StringBuffer reportuffer = new StringBuffer();
        reportuffer.append("<Reports>");
        for (AfOrderIdentifyDetail detail: afOrderIdentify.getAfOrderIdentifyDetailList()) {
            reportuffer.append("<Report>");
            reportuffer.append("<IssueNO>").append(detail.getReportIssueNo()).append("</IssueNO>");
            reportuffer.append("<IssueOrgan>").append(detail.getReportIssueOrgan()).append("</IssueOrgan>");
            reportuffer.append("<IssueDate>").append(detail.getReportIssueDate()).append("</IssueDate>");
            reportuffer.append("<Applicant>").append(detail.getReportApplicant()).append("</Applicant>");
            reportuffer.append("<GoodsCN>").append(detail.getReportGoodsCnname()).append("</GoodsCN>");
            reportuffer.append("<GoodsEN>").append(detail.getReportGoodsEnname()).append("</GoodsEN>");
            reportuffer.append("<URL>").append(detail.getReportImgUrl()).append("</URL>");
            reportuffer.append("</Report>");
        }
        reportuffer.append("</Reports>");

//        StringBuffer imgUrlBuffer = new StringBuffer();
//        String urls = afOrderIdentify.getReportImgUrls();
//        String[] imgUrls = urls.split(";");
//        imgUrlBuffer.append("<ImgUrls>");
//        for (String imgUrl:imgUrls) {
//            imgUrlBuffer.append("<ImgUrl>").append(imgUrl).append("</ImgUrl>");
//        }
//        imgUrlBuffer.append("</ImgUrls>");
        xml +=  reportuffer.toString() +"</Identify></ServiceData></Service>";
        return xml;
    }

    @Override
    public boolean deleteDeclare(Integer orderIdentifyId) throws Exception{
        AfOrderIdentify afOrderIdentify= baseMapper.selectById(orderIdentifyId);
        if(afOrderIdentify==null){
            return false;
        }
        if(!"send".equalsIgnoreCase(afOrderIdentify.getStatus())){
            log.info("主单号:"+afOrderIdentify.getAwbNumber() +"的该鉴定信息未申报，无法删除");
            throw new CheckedException("对不起，该鉴定信息未申报，无法删除！");
        }
        OrgInterface config = sendMapper.getShippingBillConfig(SecurityUtils.getUser().getOrgId(), "AE_IDF_POST_MAWB");
        if(config==null){
            log.info("主单号:"+afOrderIdentify.getAwbNumber() +" 没有配置其鉴定信息传输身份，请联系管理员进行配置");
            throw new CheckedException("对不起，没有配置其鉴定信息传输身份，请联系管理员进行配置！");
        }
        String appid = config.getAppid();
        String xml = "<Service>\n" +
                "    <ServiceURL>Identify_Adapter</ServiceURL>\n" +
                "    <ServiceAction>ApiDel</ServiceAction>\n" +
                "    <ServiceData>\n" +
                "        <Identify>\n" +
                "            <Forwarder>"+ appid +"</Forwarder>\n" +
                "            <AgentHandlerName>"+ afOrderIdentify.getAgentHandlerName() +"</AgentHandlerName>\n" +
                "            <Mawb>"+ afOrderIdentify.getAwbNumber() +"</Mawb>\n" +
                "            <Carrier>"+ afOrderIdentify.getCarrierId() +"</Carrier>\n" +
                "            <CargoTerminal>"+ afOrderIdentify.getCargoTerminal() +"</CargoTerminal>\n" +
                "        </Identify>\n" +
                "    </ServiceData>\n" +
                "</Service>";
        String responseXml = RequestUtil.PosteFreightHttpEngine(config.getUrlPost(),xml);
        Document resultDoc = XmlApiUtils.parseXML(responseXml,true);
        if(!"1".equals(XmlApiUtils.getNodeText(resultDoc,"//ResultCode"))){
            throw new CheckedException("申报删除鉴定信息时出现异常！" + XmlApiUtils.getNodeText(resultDoc,"//ResultContent"));
        }

        UpdateWrapper<AfOrderIdentify> wrapper = new UpdateWrapper<AfOrderIdentify>();
        wrapper.set("status","delete");
        wrapper.set("audit_status","delete");
        wrapper.eq("awb_number",afOrderIdentify.getAwbNumber());
        if(update(wrapper)){
            updateOrderIdentify(afOrderIdentify.getOrderIdentifyId());
            return true;
        }
        return false;
    }

    @Override
    public boolean audit(Integer originalSyscode,String auditName) {
        AfOrderIdentify queryOrderIdentify = new AfOrderIdentify();
        queryOrderIdentify.setOriginalSyscode(Long.parseLong(String.valueOf(originalSyscode)));
        QueryWrapper<AfOrderIdentify> queryWrapper = new QueryWrapper<>(queryOrderIdentify);
        AfOrderIdentify afOrderIdentifies = baseMapper.selectOne(queryWrapper);

        afOrderIdentifies.setStatus("send");
        afOrderIdentifies.setAuditStatus("send");
        afOrderIdentifies.setAuditDate(LocalDateTime.now());
        afOrderIdentifies.setAuditName(auditName);

        if (saveOrUpdate(afOrderIdentifies)) {
            updateOrderIdentify(afOrderIdentifies.getOrderId());
            return true;
        }
        return false;
    }

    /**
     * 获取审核通过鉴定证书信息，需要带有明细
     * @param orderId
     * @return
     */
    public List<AfOrderIdentify> getAfOrderAudiedIdentifyList(Integer orderId) {
        QueryWrapper<AfOrderIdentify> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("order_id",orderId);
        queryWrapper.eq("audit_status","send");
        List<AfOrderIdentify>  afOrderIdentifyList = baseMapper.selectList(queryWrapper);
        for (AfOrderIdentify afOrderIdentify: afOrderIdentifyList) {
            afOrderIdentify.setAfOrderIdentifyDetailList(detailService.getAfOrderIdentifiesDetailList(afOrderIdentify.getOrderIdentifyId()));
        }
        return afOrderIdentifyList;
    }

}
