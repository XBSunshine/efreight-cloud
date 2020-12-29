package com.efreight.afbase.service.impl;

import cn.hutool.core.util.StrUtil;

import com.alibaba.druid.util.StringUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.efreight.afbase.entity.OrderInquiry;
import com.efreight.afbase.dao.OrderInquiryMapper;
import com.efreight.afbase.entity.OrderInquiryQuotation;
import com.efreight.afbase.entity.exportExcel.OrderInquiryQuotationExcel;
import com.efreight.afbase.service.OrderInquiryQuotationService;
import com.efreight.afbase.service.OrderInquiryService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.efreight.afbase.utils.PDFUtils;
import com.efreight.common.core.feign.RemoteServiceToHRS;
import com.efreight.common.core.jms.MailSendService;
import com.efreight.common.core.utils.JxlsUtils;
import com.efreight.common.security.util.SecurityUtils;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * AF 询价单 服务实现类
 * </p>
 *
 * @author xiaobo
 * @since 2020-05-26
 */
@Service
@AllArgsConstructor
public class OrderInquiryServiceImpl extends ServiceImpl<OrderInquiryMapper, OrderInquiry> implements OrderInquiryService {

    private final OrderInquiryQuotationService orderInquiryQuotationService;

    private final MailSendService mailSendService;

    private final RemoteServiceToHRS remoteServiceToHRS;

    @Override
    public IPage getPage(Page page, OrderInquiry orderInquiry) {
        LambdaQueryWrapper<OrderInquiry> wrapper = Wrappers.<OrderInquiry>lambdaQuery();
        //条件
        if (StrUtil.isNotBlank(orderInquiry.getDepartureStation())) {
            wrapper.like(OrderInquiry::getDepartureStation, "%" + orderInquiry.getDepartureStation() + "%");
        }
        if (StrUtil.isNotBlank(orderInquiry.getArrivalStation())) {
            wrapper.like(OrderInquiry::getArrivalStation, "%" + orderInquiry.getArrivalStation() + "%");
        }
        if (orderInquiry.getEditTimeBegin() != null) {
            wrapper.ge(OrderInquiry::getEditTime, orderInquiry.getEditTimeBegin());
        }
        if (orderInquiry.getEditTimeEnd() != null) {
            wrapper.le(OrderInquiry::getEditTime, orderInquiry.getEditTimeEnd());
        }
        if (StrUtil.isNotBlank(orderInquiry.getOrderInquiryStatus())) {
            LambdaQueryWrapper<OrderInquiryQuotation> orderInquiryQuotationWrapper = Wrappers.<OrderInquiryQuotation>lambdaQuery();
            orderInquiryQuotationWrapper.eq(OrderInquiryQuotation::getOrgId, SecurityUtils.getUser().getOrgId()).eq(OrderInquiryQuotation::getIsValid, true).groupBy(OrderInquiryQuotation::getOrderInquiryId).select(OrderInquiryQuotation::getOrderInquiryId);
            List<Integer> orderInquiryIds = orderInquiryQuotationService.list(orderInquiryQuotationWrapper).stream().map(orderInquiryQuotation -> orderInquiryQuotation.getOrderInquiryId()).collect(Collectors.toList());
            if (orderInquiry.getOrderInquiryStatus().contains("已收方案")) {
                if (orderInquiryIds.size() == 0) {
                    wrapper.in(OrderInquiry::getOrderInquiryStatus, orderInquiry.getOrderInquiryStatus().split(","));
                } else {
                    wrapper.and(i -> i.in(OrderInquiry::getOrderInquiryId, orderInquiryIds).eq(OrderInquiry::getOrderInquiryStatus, "已创建").or(j -> j.in(OrderInquiry::getOrderInquiryStatus, orderInquiry.getOrderInquiryStatus().split(","))));
                }
            } else {
                if (orderInquiry.getOrderInquiryStatus().contains("已创建")) {
                    if (orderInquiryIds.size() == 0) {
                        wrapper.in(OrderInquiry::getOrderInquiryStatus, orderInquiry.getOrderInquiryStatus().split(","));
                    } else {
                        wrapper.and(i -> i.notIn(OrderInquiry::getOrderInquiryId, orderInquiryIds).eq(OrderInquiry::getOrderInquiryStatus, "已创建").or(j -> j.ne(OrderInquiry::getOrderInquiryStatus, "已创建").in(OrderInquiry::getOrderInquiryStatus, orderInquiry.getOrderInquiryStatus().split(","))));
                    }
                } else {
                    wrapper.in(OrderInquiry::getOrderInquiryStatus, orderInquiry.getOrderInquiryStatus().split(","));
                }

            }
        }
        if (StrUtil.isNotBlank(orderInquiry.getEditorName())) {
            wrapper.like(OrderInquiry::getEditorName, "%" + orderInquiry.getEditorName() + "%");
        }
        wrapper.eq(OrderInquiry::getOrgId, SecurityUtils.getUser().getOrgId()).orderByDesc(OrderInquiry::getOrderInquiryCode);
        IPage<OrderInquiry> result = page(page, wrapper);
        result.getRecords().stream().forEach(inquiry -> {
            String flightClaim = (StrUtil.isBlank(inquiry.getDirectFlight()) ? "" : inquiry.getDirectFlight() + " / ") + (StrUtil.isBlank(inquiry.getCarrierCode()) ? "" : inquiry.getCarrierCode() + " / ") + (inquiry.getExpectDeparture() == null ? "" : DateTimeFormatter.ofPattern("yyyy-MM-dd").format(inquiry.getExpectDeparture()) + " / ") + (StrUtil.isBlank(inquiry.getFlightRemark()) ? "" : inquiry.getFlightRemark());
            if (flightClaim.endsWith(" / ")) {
                flightClaim = flightClaim.substring(0, flightClaim.lastIndexOf(" / "));
            }
            inquiry.setFlightClaim(flightClaim);
            String specialGoods = inquiry.getOverWeight() + " / " + inquiry.getOverSize();
            inquiry.setSpecialGoods(specialGoods);
            if (StrUtil.isNotBlank(inquiry.getInquiryAgentIds())) {
                List<Map<String, String>> agentList = baseMapper.selectInquiryAgentByIds(inquiry.getInquiryAgentIds());
                HashMap<String, String> temp = new HashMap<>();
                agentList.stream().forEach(agent -> {
                    if (temp.get("inquiryAgentName") == null) {
                        temp.put("inquiryAgentName", agent.get("inquiryAgentName"));
                    } else {
                        temp.put("inquiryAgentName", temp.get("inquiryAgentName") + ", " + agent.get("inquiryAgentName"));
                    }
                });
                inquiry.setInquiryAgentNames(temp.get("inquiryAgentName"));
            }
            //报价方案
            Map<String, String> inquiryPlanMap = baseMapper.selectInquiryPlan(inquiry.getOrderInquiryId(), SecurityUtils.getUser().getOrgId());
            if (inquiryPlanMap != null) {
                if (!"0 / 0".equals(inquiryPlanMap.get("inquiryPlan")) && "已创建".equals(inquiry.getOrderInquiryStatus())) {
                    inquiry.setOrderInquiryStatus("已收方案");
                }
                inquiry.setInquiryPlan(inquiryPlanMap.get("inquiryPlan"));
            }

        });

        return result;
    }

    @Override
    public OrderInquiry view(Integer orderInquiryId) {
        OrderInquiry orderInquiry = getById(orderInquiryId);
        //添加辅助数据
        if (StrUtil.isNotBlank(orderInquiry.getInquiryAgentIds())) {
            List<String> inquiryAgentNames = baseMapper.selectInquiryAgentByIds(orderInquiry.getInquiryAgentIds()).stream().map(agentMap -> agentMap.get("inquiryAgentName")).collect(Collectors.toList());
            if (inquiryAgentNames.size() != 0) {
                HashMap<String, String> inquiryAgentNameMap = new HashMap<>();
                inquiryAgentNames.stream().forEach(inquiryAgentName -> {
                    if (StrUtil.isNotBlank(inquiryAgentName)) {
                        if (StrUtil.isBlank(inquiryAgentNameMap.get("inquiryAgentName"))) {
                            inquiryAgentNameMap.put("inquiryAgentName", inquiryAgentName);
                        } else {
                            inquiryAgentNameMap.put("inquiryAgentName", inquiryAgentNameMap.get("inquiryAgentName") + "," + inquiryAgentName);
                        }
                    }

                });
                orderInquiry.setInquiryAgentNames(inquiryAgentNameMap.get("inquiryAgentName"));
            }
        }

        //插入报价单列表
        LambdaQueryWrapper<OrderInquiryQuotation> quotationWrapper = Wrappers.<OrderInquiryQuotation>lambdaQuery();
        //quotationWrapper.eq(OrderInquiryQuotation::getOrderInquiryId, orderInquiryId).eq(OrderInquiryQuotation::getOrgId, SecurityUtils.getUser().getOrgId()).eq(OrderInquiryQuotation::getIsValid, true).orderByAsc(OrderInquiryQuotation::getQuotationCompanyName, OrderInquiryQuotation::getQuotationContacts, OrderInquiryQuotation::getQuotationEndDate);
        quotationWrapper.eq(OrderInquiryQuotation::getOrderInquiryId, orderInquiryId).eq(OrderInquiryQuotation::getOrgId, SecurityUtils.getUser().getOrgId()).orderByDesc(OrderInquiryQuotation::getIsValid).orderByAsc(OrderInquiryQuotation::getQuotationCompanyName, OrderInquiryQuotation::getQuotationContacts, OrderInquiryQuotation::getQuotationEndDate);
        orderInquiry.setOrderInquiryQuotations(orderInquiryQuotationService.list(quotationWrapper));
        return orderInquiry;
    }

    @Override
    public OrderInquiry view(String orderInquiryUuid) {
        LambdaQueryWrapper<OrderInquiry> wrapper = Wrappers.<OrderInquiry>lambdaQuery();
        wrapper.isNull(OrderInquiry::getOrderId).ne(OrderInquiry::getOrderInquiryStatus, "已关闭").eq(OrderInquiry::getOrderInquiryOrderUuid, orderInquiryUuid);
        OrderInquiry orderInquiry = getOne(wrapper);
        //添加辅助数据
        if (orderInquiry != null && StrUtil.isNotBlank(orderInquiry.getInquiryAgentIds())) {
            List<String> inquiryAgentNames = baseMapper.selectInquiryAgentByIds(orderInquiry.getInquiryAgentIds()).stream().map(agentMap -> agentMap.get("inquiryAgentName")).collect(Collectors.toList());
            if (inquiryAgentNames.size() != 0) {
                HashMap<String, String> inquiryAgentNameMap = new HashMap<>();
                inquiryAgentNames.stream().forEach(inquiryAgentName -> {
                    if (StrUtil.isNotBlank(inquiryAgentName)) {
                        if (StrUtil.isBlank(inquiryAgentNameMap.get("inquiryAgentName"))) {
                            inquiryAgentNameMap.put("inquiryAgentName", inquiryAgentName);
                        } else {
                            inquiryAgentNameMap.put("inquiryAgentName", inquiryAgentNameMap.get("inquiryAgentName") + "," + inquiryAgentName);
                        }
                    }

                });
                orderInquiry.setInquiryAgentNames(inquiryAgentNameMap.get("inquiryAgentName"));
            }
        }
        return orderInquiry;
    }

    @Override
    public synchronized Integer insert(OrderInquiry orderInquiry) {
        //校验


        //设置必要数据
        orderInquiry.setBusinessScope("AE");
        orderInquiry.setOrderInquiryStatus("已创建");
        orderInquiry.setOrderInquiryCode(createOrderInquiryCode());
        orderInquiry.setOrderInquiryOrderUuid(createUuid());
        orderInquiry.setRowUuid(UUID.randomUUID().toString());
        orderInquiry.setOrgId(SecurityUtils.getUser().getOrgId());
        orderInquiry.setCreatorId(SecurityUtils.getUser().getId());
        orderInquiry.setEditorId(SecurityUtils.getUser().getId());
        orderInquiry.setCreatorName(SecurityUtils.getUser().getUserCname() + " " + SecurityUtils.getUser().getUserEmail());
        orderInquiry.setEditorName(SecurityUtils.getUser().getUserCname() + " " + SecurityUtils.getUser().getUserEmail());
        orderInquiry.setCreatTime(LocalDateTime.now());
        orderInquiry.setEditTime(orderInquiry.getCreatTime());
        //保存
        save(orderInquiry);
        return orderInquiry.getOrderInquiryId();
    }

    @Override
    public void modify(OrderInquiry orderInquiry) {
        //校验
        OrderInquiry inquiry = getById(orderInquiry.getOrderInquiryId());
        if (StrUtil.isNotBlank(inquiry.getRowUuid()) && !inquiry.getRowUuid().equals(orderInquiry.getRowUuid())) {
            throw new RuntimeException("当前页面不是最新数据，请刷新再操作");
        }
        if (inquiry == null) {
            throw new RuntimeException("询价单不存在,修改失败");
        }
        if (!"已创建".equals(inquiry.getOrderInquiryStatus())) {
            throw new RuntimeException("询价单的状态不是已创建,无法修改");
        }
        LambdaQueryWrapper<OrderInquiryQuotation> orderInquiryQuotationWrapper = Wrappers.<OrderInquiryQuotation>lambdaQuery();
        orderInquiryQuotationWrapper.eq(OrderInquiryQuotation::getIsValid, true).eq(OrderInquiryQuotation::getOrderInquiryId, orderInquiry.getOrderInquiryId()).eq(OrderInquiryQuotation::getOrgId, SecurityUtils.getUser().getOrgId());
        List<OrderInquiryQuotation> list = orderInquiryQuotationService.list(orderInquiryQuotationWrapper);
        if (list.size() != 0) {
            throw new RuntimeException("询价单已收方案，无法修改");
        }
        //修改必要数据
        orderInquiry.setRowUuid(UUID.randomUUID().toString());
        orderInquiry.setEditTime(LocalDateTime.now());
        orderInquiry.setEditorName(SecurityUtils.getUser().getUserCname() + " " + SecurityUtils.getUser().getUserEmail());
        orderInquiry.setEditorId(SecurityUtils.getUser().getId());
        //修改
        updateById(orderInquiry);

    }

    @Override
    public void stop(Integer orderInquiryId) {
        //校验
        OrderInquiry orderInquiry = getById(orderInquiryId);
        if (orderInquiry == null) {
            throw new RuntimeException("询价单不存在");
        }
        if ("已关闭".equals(orderInquiry.getOrderInquiryStatus())) {
            throw new RuntimeException("询价单已关闭");
        }
        if (orderInquiry.getOrderId() != null) {
            throw new RuntimeException("询价单已转订单，无法关闭");
        }
        orderInquiry.setRowUuid(UUID.randomUUID().toString());
        orderInquiry.setOrderInquiryStatus("已关闭");
        orderInquiry.setEditTime(LocalDateTime.now());
        orderInquiry.setEditorName(SecurityUtils.getUser().getUserCname() + " " + SecurityUtils.getUser().getUserEmail());
        orderInquiry.setEditorId(SecurityUtils.getUser().getId());

        //修改
        updateById(orderInquiry);
    }

    @Override
    public List<Map<String, String>> getInquryAgentContactList(String inquryAgentIds) {
        return baseMapper.getInquryAgentContactList(inquryAgentIds, SecurityUtils.getUser().getOrgId());
    }

    @Override
    @SneakyThrows
    public void sendQrcode(Map<String, Object> param) {
        String content = buildInquryQrcodeContent(param);
        Path tmpPath = Files.createTempFile("ef_qr_", ".png");
        String imageBase64 = (String) param.get("imageURL");
        byte[] bytes = Base64.getDecoder().decode(imageBase64.substring(imageBase64.indexOf(",") + 1).getBytes(StandardCharsets.UTF_8));
        Path path = Files.write(tmpPath, bytes);
        HashMap<String, File> imgMap = new HashMap<>();
        imgMap.put("qr_image", path.toFile());
        String[] bccUsers = null;
        if (StrUtil.isNotBlank(param.get("bccUsers").toString())) {
            bccUsers = param.get("bccUsers").toString().split(";");
        }
        mailSendService.sendHtmlMailNew(true, param.get("toUsers").toString().split(";"), null, bccUsers, param.get("subject").toString(), content, imgMap);
    }

    @Override
    public List<OrderInquiry> exportExcel(OrderInquiry orderInquiry) {
        LambdaQueryWrapper<OrderInquiry> wrapper = Wrappers.<OrderInquiry>lambdaQuery();
        //条件
        if (StrUtil.isNotBlank(orderInquiry.getDepartureStation())) {
            wrapper.like(OrderInquiry::getDepartureStation, "%" + orderInquiry.getDepartureStation() + "%");
        }
        if (StrUtil.isNotBlank(orderInquiry.getArrivalStation())) {
            wrapper.like(OrderInquiry::getArrivalStation, "%" + orderInquiry.getArrivalStation() + "%");
        }
        if (orderInquiry.getEditTimeBegin() != null) {
            wrapper.ge(OrderInquiry::getEditTime, orderInquiry.getEditTimeBegin());
        }
        if (orderInquiry.getEditTimeEnd() != null) {
            wrapper.le(OrderInquiry::getEditTime, orderInquiry.getEditTimeEnd());
        }
        if (StrUtil.isNotBlank(orderInquiry.getOrderInquiryStatus())) {
            LambdaQueryWrapper<OrderInquiryQuotation> orderInquiryQuotationWrapper = Wrappers.<OrderInquiryQuotation>lambdaQuery();
            orderInquiryQuotationWrapper.eq(OrderInquiryQuotation::getOrgId, SecurityUtils.getUser().getOrgId()).eq(OrderInquiryQuotation::getIsValid, true).groupBy(OrderInquiryQuotation::getOrderInquiryId).select(OrderInquiryQuotation::getOrderInquiryId);
            List<Integer> orderInquiryIds = orderInquiryQuotationService.list(orderInquiryQuotationWrapper).stream().map(orderInquiryQuotation -> orderInquiryQuotation.getOrderInquiryId()).collect(Collectors.toList());
            if (orderInquiry.getOrderInquiryStatus().contains("已收方案")) {
                if (orderInquiryIds.size() == 0) {
                    wrapper.in(OrderInquiry::getOrderInquiryStatus, orderInquiry.getOrderInquiryStatus().split(","));
                } else {
                    wrapper.and(i -> i.in(OrderInquiry::getOrderInquiryId, orderInquiryIds).eq(OrderInquiry::getOrderInquiryStatus, "已创建").or(j -> j.in(OrderInquiry::getOrderInquiryStatus, orderInquiry.getOrderInquiryStatus().split(","))));
                }
            } else {
                if (orderInquiry.getOrderInquiryStatus().contains("已创建")) {
                    if (orderInquiryIds.size() == 0) {
                        wrapper.in(OrderInquiry::getOrderInquiryStatus, orderInquiry.getOrderInquiryStatus().split(","));
                    } else {
                        wrapper.and(i -> i.notIn(OrderInquiry::getOrderInquiryId, orderInquiryIds).eq(OrderInquiry::getOrderInquiryStatus, "已创建").or(j -> j.ne(OrderInquiry::getOrderInquiryStatus, "已创建").in(OrderInquiry::getOrderInquiryStatus, orderInquiry.getOrderInquiryStatus().split(","))));
                    }
                } else {
                    wrapper.in(OrderInquiry::getOrderInquiryStatus, orderInquiry.getOrderInquiryStatus().split(","));
                }

            }
        }
        if (StrUtil.isNotBlank(orderInquiry.getEditorName())) {
            wrapper.like(OrderInquiry::getEditorName, "%" + orderInquiry.getEditorName() + "%");
        }
        wrapper.eq(OrderInquiry::getOrgId, SecurityUtils.getUser().getOrgId()).orderByDesc(OrderInquiry::getOrderInquiryCode);
        List<OrderInquiry> list = list(wrapper);
        list.stream().forEach(inquiry -> {
            String flightClaim = (StrUtil.isBlank(inquiry.getDirectFlight()) ? "" : inquiry.getDirectFlight() + " / ") + (StrUtil.isBlank(inquiry.getCarrierCode()) ? "" : inquiry.getCarrierCode() + " / ") + (inquiry.getExpectDeparture() == null ? "" : DateTimeFormatter.ofPattern("yyyy-MM-dd").format(inquiry.getExpectDeparture()) + " / ") + (StrUtil.isBlank(inquiry.getFlightRemark()) ? "" : inquiry.getFlightRemark());
            if (flightClaim.endsWith(" / ")) {
                flightClaim = flightClaim.substring(0, flightClaim.lastIndexOf(" / "));
            }
            inquiry.setFlightClaim(flightClaim);
            String specialGoods = inquiry.getOverWeight() + " / " + inquiry.getOverSize();
            inquiry.setSpecialGoods(specialGoods);
            if (StrUtil.isNotBlank(inquiry.getInquiryAgentIds())) {
                List<Map<String, String>> agentList = baseMapper.selectInquiryAgentByIds(inquiry.getInquiryAgentIds());
                HashMap<String, String> temp = new HashMap<>();
                agentList.stream().forEach(agent -> {
                    if (temp.get("inquiryAgentName") == null) {
                        temp.put("inquiryAgentName", agent.get("inquiryAgentName"));
                    } else {
                        temp.put("inquiryAgentName", temp.get("inquiryAgentName") + ", " + agent.get("inquiryAgentName"));
                    }
                });
                inquiry.setInquiryAgentNames(temp.get("inquiryAgentName"));
            }
            //报价方案
            Map<String, String> inquiryPlanMap = baseMapper.selectInquiryPlan(inquiry.getOrderInquiryId(), SecurityUtils.getUser().getOrgId());
            if (inquiryPlanMap != null) {
                if (!"0 / 0".equals(inquiryPlanMap.get("inquiryPlan")) && "已创建".equals(inquiry.getOrderInquiryStatus())) {
                    inquiry.setOrderInquiryStatus("已收方案");
                }
                inquiry.setInquiryPlan(inquiryPlanMap.get("inquiryPlan"));
            }

        });

        return list;
    }

    private String createOrderInquiryCode() {
        String numberPrefix = "IQ-" + DateTimeFormatter.ofPattern("yyMMdd").format(LocalDate.now());
        LambdaQueryWrapper<OrderInquiry> wrapper = Wrappers.<OrderInquiry>lambdaQuery();
        wrapper.eq(OrderInquiry::getOrgId, SecurityUtils.getUser().getOrgId()).like(OrderInquiry::getOrderInquiryCode, "%" + numberPrefix + "%").orderByDesc(OrderInquiry::getOrderInquiryCode).last(" limit 1");

        OrderInquiry orderInquiry = getOne(wrapper);

        String numberSuffix = "";
        if (orderInquiry == null) {
            numberSuffix = "0001";
        } else if (orderInquiry.getOrderInquiryCode().substring(orderInquiry.getOrderInquiryCode().length() - 4).equals("9999")) {
            throw new RuntimeException("今天询价单已满无法创建,明天再整吧亲");
        } else {
            String n = Integer.valueOf(orderInquiry.getOrderInquiryCode().substring(orderInquiry.getOrderInquiryCode().length() - 4)) + 1 + "";
            numberSuffix = "0000".substring(0, 4 - n.length()) + n;
        }
        return numberPrefix + numberSuffix;
    }

    private String createUuid() {
        return baseMapper.getUuid();
    }

    private String buildInquryQrcodeContent(Map<String, Object> param) {
        String content = (String) param.get("content");
        StringBuilder builder = new StringBuilder();
        builder.append(content.replaceAll("\n", "<br />"));
        builder.append("<br />");
        builder.append("<br />");

//        builder.append("网址：");
//        builder.append(param.get("website"));
        builder.append("<font style='background: yellow;'>请点击下面的二维码链接进行报价，感谢合作，顺祝商祺</font>");
        builder.append("<br />");
        builder.append("<br />");

        builder.append("二维码链接:");
        builder.append("<div id='");
        builder.append(new Date().getTime());
        builder.append("'>");
        builder.append("<a href=\"");
        builder.append(param.get("website"));
        builder.append("\">");
        builder.append("<img src='cid:qr_image' />");
        builder.append("</a>");
        builder.append("</div>");

        return builder.toString();
    }

    @Override
    public Map getInquryAgentDepList(String dep) {
        List<Map> mapList = null;
        boolean flag = false;
        mapList = baseMapper.getInquryAgentDepList(SecurityUtils.getUser().getOrgId(), dep);
        if (mapList != null && mapList.size() > 0) {
            flag = true;
        } else {
            if (!StringUtils.isEmpty(dep)) {
                dep = null;
                mapList = baseMapper.getInquryAgentDepList(SecurityUtils.getUser().getOrgId(), dep);
                if (mapList != null && mapList.size() > 0) {
                    flag = true;
                }
            }
        }
        if (flag) {
            Map map = new HashMap();
            StringBuffer sbId = new StringBuffer();
            StringBuffer sbName = new StringBuffer();
            for (Map m : mapList) {
                if ("YFTCAN".equals(m.get("coop_code").toString()) || "YFTPEK".equals(m.get("coop_code").toString()) ||
                        "YFTSHA".equals(m.get("coop_code").toString()) || "YFTXMN".equals(m.get("coop_code").toString())) {
                    sbId.append(m.get("inquiry_id")).append(",");
                    sbName.append(m.get("inquiry_agent_name_short")).append(",");
                }
            }
            if (sbId != null && !StringUtils.isEmpty(sbId.toString())) {
                map.put("inquiryAgentIds", sbId.toString().substring(0, sbId.toString().length() - 1));
                map.put("inquiryAgentNames", sbName.toString().substring(0, sbName.toString().length() - 1));
                return map;
            }
        }
        return null;
    }

    @Override
    @SneakyThrows
    public void exportInquiryQuotationExcel(Integer orderInquiryId) {
        if (orderInquiryId == null) {
            throw new RuntimeException("询报价单不存在，导出失败");
        }
        List<List<OrderInquiryQuotationExcel>> lists = baseMapper.queryInquiryQuotationExcel(SecurityUtils.getUser().getOrgId(), orderInquiryId);
        if (lists != null && lists.size() > 0) {
            OrderInquiryQuotationExcel orderInquiryQuotationExcel = lists.get(0).get(0);

            //添加辅助数据
            if (StrUtil.isNotBlank(orderInquiryQuotationExcel.getInquiryAgentIds())) {
                List<String> inquiryAgentNames = baseMapper.selectInquiryAgentByIds(orderInquiryQuotationExcel.getInquiryAgentIds()).stream().map(agentMap -> agentMap.get("inquiryAgentName")).collect(Collectors.toList());
                if (inquiryAgentNames.size() != 0) {
                    HashMap<String, String> inquiryAgentNameMap = new HashMap<>();
                    inquiryAgentNames.stream().forEach(inquiryAgentName -> {
                        if (StrUtil.isNotBlank(inquiryAgentName)) {
                            if (StrUtil.isBlank(inquiryAgentNameMap.get("inquiryAgentName"))) {
                                inquiryAgentNameMap.put("inquiryAgentName", inquiryAgentName);
                            } else {
                                inquiryAgentNameMap.put("inquiryAgentName", inquiryAgentNameMap.get("inquiryAgentName") + "," + inquiryAgentName);
                            }
                        }

                    });
                    orderInquiryQuotationExcel.setInquiryAgentNames(inquiryAgentNameMap.get("inquiryAgentName"));
                }
            }
            //设置目的港
            if ("/".equals(orderInquiryQuotationExcel.getArrivalStation())) {
                orderInquiryQuotationExcel.setArrivalStation("");
            } else if (orderInquiryQuotationExcel.getArrivalStation().startsWith("/") || orderInquiryQuotationExcel.getArrivalStation().endsWith("/")) {
                orderInquiryQuotationExcel.setArrivalStation(orderInquiryQuotationExcel.getArrivalStation().replace("/", ""));
            }
            //设置件数
            if ("/".equals(orderInquiryQuotationExcel.getPlanPieces())) {
                orderInquiryQuotationExcel.setPlanPieces("");
            } else if (orderInquiryQuotationExcel.getPlanPieces().startsWith("/") || orderInquiryQuotationExcel.getPlanPieces().endsWith("/")) {
                orderInquiryQuotationExcel.setPlanPieces(orderInquiryQuotationExcel.getPlanPieces().replace("/", ""));
            }
            //设置毛重
            if ("/".equals(orderInquiryQuotationExcel.getPlanWeight())) {
                orderInquiryQuotationExcel.setPlanWeight("");
            } else if (orderInquiryQuotationExcel.getPlanWeight().startsWith("/") || orderInquiryQuotationExcel.getPlanWeight().endsWith("/")) {
                orderInquiryQuotationExcel.setPlanWeight(orderInquiryQuotationExcel.getPlanWeight().replace("/", ""));
            }
            //设置体积
            if ("/".equals(orderInquiryQuotationExcel.getPlanVolume())) {
                orderInquiryQuotationExcel.setPlanVolume("");
            } else if (orderInquiryQuotationExcel.getPlanVolume().startsWith("/") || orderInquiryQuotationExcel.getPlanVolume().endsWith("/")) {
                orderInquiryQuotationExcel.setPlanVolume(orderInquiryQuotationExcel.getPlanVolume().replace("/", ""));
            }
            //设置计重/密度
            if ("/".equals(orderInquiryQuotationExcel.getPlanChargeWeight())) {
                orderInquiryQuotationExcel.setPlanChargeWeight("");
            } else if (orderInquiryQuotationExcel.getPlanChargeWeight().startsWith("/") || orderInquiryQuotationExcel.getPlanChargeWeight().endsWith("/")) {
                orderInquiryQuotationExcel.setPlanChargeWeight(orderInquiryQuotationExcel.getPlanChargeWeight().replace("/", ""));
            }
            //设置货物类型
            if ("/".equals(orderInquiryQuotationExcel.getBatteryType())) {
                orderInquiryQuotationExcel.setBatteryType("");
            } else if (orderInquiryQuotationExcel.getBatteryType().startsWith("/") || orderInquiryQuotationExcel.getBatteryType().endsWith("/")) {
                orderInquiryQuotationExcel.setBatteryType(orderInquiryQuotationExcel.getBatteryType().replace("/", ""));
            }
            //设置价格预期
            if ("/".equals(orderInquiryQuotationExcel.getPlanPrice())) {
                orderInquiryQuotationExcel.setPlanPrice("");
            } else if (orderInquiryQuotationExcel.getPlanPrice().startsWith("/") || orderInquiryQuotationExcel.getPlanPrice().endsWith("/")) {
                orderInquiryQuotationExcel.setPlanPrice(orderInquiryQuotationExcel.getPlanPrice().replace("/", ""));
            }

            HashMap<String, Object> context = new HashMap<>();
            context.put("inquiry", lists.get(0).get(0));
            context.put("inquiryList", lists.get(1));
            JxlsUtils.exportExcelWithLocalModel(PDFUtils.filePath + "/PDFtemplate/INQUIRY.xlsx", context);
        } else {
            throw new RuntimeException("询报价单无数据，导出失败");
        }
    }

    @Override
    public void createFourYCWhenInquiry() {
        baseMapper.createFourYCWhenInquiry(SecurityUtils.getUser().getOrgId(),SecurityUtils.getUser().getId());
    }

    @Override
    public Boolean getFourYCWhenInquiry() {
        List list = baseMapper.getFourYCWhenInquiry(SecurityUtils.getUser().getOrgId());
        if (list.size() == 0) {
            return false;
        }
        return true;
    }
}
