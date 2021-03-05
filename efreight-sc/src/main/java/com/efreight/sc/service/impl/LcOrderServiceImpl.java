package com.efreight.sc.service.impl;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.efreight.common.core.feign.RemoteServiceToAF;
import com.efreight.common.core.utils.ExcelExportUtils;
import com.efreight.common.core.utils.FormatUtils;
import com.efreight.common.remoteVo.Airport;
import com.efreight.common.remoteVo.OrderForVL;
import com.efreight.common.security.util.SecurityUtils;
import com.efreight.sc.dao.LcIncomeMapper;
import com.efreight.sc.dao.VlOrderMapper;
import com.efreight.sc.entity.*;
import com.efreight.sc.dao.LcOrderMapper;
import com.efreight.sc.entity.view.AfVPrmCoop;
import com.efreight.sc.service.*;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.efreight.sc.utils.FieldValUtils;
import com.efreight.sc.utils.LoginUtils;
import lombok.AllArgsConstructor;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * LC 订单管理 LC陆运订单 服务实现类
 * </p>
 *
 * @author xiaobo
 * @since 2020-07-21
 */
@Service
@AllArgsConstructor
public class LcOrderServiceImpl extends ServiceImpl<LcOrderMapper, LcOrder> implements LcOrderService {

    private final AfVPrmCoopService afVPrmCoopService;

    private final LcLogService lcLogService;

    private final LcIncomeMapper lcIncomeService;

    private final LcCostService lcCostService;

    private final RemoteServiceToAF remoteServiceToAF;

    private final VlOrderDetailOrderService vlOrderDetailOrderService;

    private final VlOrderMapper vlOrderMapper;

    private final LcTruckService lcTruckService;

    @Override
    public IPage getPage(Page page, LcOrder lcOrder) {
        lcOrder.setCurrentUserId(SecurityUtils.getUser().getId());
        //获取查询条件
        LambdaQueryWrapper<LcOrder> wrapper = getWrapper(lcOrder);
        if (wrapper == null) {
            ArrayList<LcOrder> lcOrders = new ArrayList<>();
            page.setRecords(lcOrders);
            page.setTotal(0);
            return page;
        }

        //查询结果
        IPage result = page(page, wrapper);

        //修理结果集
        fixRecords(result.getRecords());

        return result;
    }


    @Override
    public LcOrder total(LcOrder lcOrder) {
        lcOrder.setCurrentUserId(SecurityUtils.getUser().getId());
        LambdaQueryWrapper<LcOrder> wrapper = getWrapper(lcOrder);
        LcOrder total = new LcOrder();
        list(wrapper).stream().forEach(order -> {
            total.setOrderStatus("合计:");
            //统计预报件数
            if (total.getPlanPieces() == null) {
                total.setPlanPieces(order.getPlanPieces() == null ? 0 : order.getPlanPieces());
            } else {
                total.setPlanPieces(total.getPlanPieces() + (order.getPlanPieces() == null ? 0 : order.getPlanPieces()));
            }
            //统计预报毛重
            if (total.getPlanWeight() == null) {
                total.setPlanWeight(order.getPlanWeight() == null ? BigDecimal.ZERO : order.getPlanWeight());
            } else {
                total.setPlanWeight(total.getPlanWeight().add(order.getPlanWeight() == null ? BigDecimal.ZERO : order.getPlanWeight()));
            }
            //统计预报体积
            if (total.getPlanVolume() == null) {
                total.setPlanVolume(order.getPlanVolume() == null ? BigDecimal.ZERO : order.getPlanVolume());
            } else {
                total.setPlanVolume(total.getPlanVolume().add(order.getPlanVolume() == null ? BigDecimal.ZERO : order.getPlanVolume()));
            }
            //统计预报计重
            if (total.getPlanChargeWeight() == null) {
                total.setPlanChargeWeight(order.getPlanChargeWeight() == null ? BigDecimal.ZERO : order.getPlanChargeWeight());
            } else {
                total.setPlanChargeWeight(total.getPlanChargeWeight().add(order.getPlanChargeWeight() == null ? BigDecimal.ZERO : order.getPlanChargeWeight()));
            }

            //统计实际件数
            if (total.getConfirmPieces() == null) {
                total.setConfirmPieces(order.getConfirmPieces() == null ? 0 : order.getConfirmPieces());
            } else {
                total.setConfirmPieces(total.getConfirmPieces() + (order.getConfirmPieces() == null ? 0 : order.getConfirmPieces()));
            }
            //统计实际毛重
            if (total.getConfirmWeight() == null) {
                total.setConfirmWeight(order.getConfirmWeight() == null ? BigDecimal.ZERO : order.getConfirmWeight());
            } else {
                total.setConfirmWeight(total.getConfirmWeight().add(order.getConfirmWeight() == null ? BigDecimal.ZERO : order.getConfirmWeight()));
            }
            //统计实际体积
            if (total.getConfirmVolume() == null) {
                total.setConfirmVolume(order.getConfirmVolume() == null ? BigDecimal.ZERO : order.getConfirmVolume());
            } else {
                total.setConfirmVolume(total.getConfirmVolume().add(order.getConfirmVolume() == null ? BigDecimal.ZERO : order.getConfirmVolume()));
            }
            //统计实际计重
            if (total.getConfirmChargeWeight() == null) {
                total.setConfirmChargeWeight(order.getConfirmChargeWeight() == null ? BigDecimal.ZERO : order.getConfirmChargeWeight());
            } else {
                total.setConfirmChargeWeight(total.getConfirmChargeWeight().add(order.getConfirmChargeWeight() == null ? BigDecimal.ZERO : order.getConfirmChargeWeight()));
            }
        });
        if (StrUtil.isBlank(total.getOrderStatus())) {
            return null;
        }
        total.setPlanWeightStr(FormatUtils.formatWithQWF(total.getPlanWeight(), 3));
        total.setPlanChargeWeightStr(FormatUtils.formatWithQWF(total.getPlanChargeWeight(), 3));
        total.setPlanVolumeStr(FormatUtils.formatWithQWF(total.getPlanVolume(), 3));
        total.setConfirmWeightStr(FormatUtils.formatWithQWF(total.getConfirmWeight(), 3));
        total.setConfirmChargeWeightStr(FormatUtils.formatWithQWF(total.getConfirmChargeWeight(), 3));
        total.setConfirmVolumeStr(FormatUtils.formatWithQWF(total.getConfirmVolume(), 3));
        return total;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void insert(LcOrder lcOrder) {
        //校验

        //保存
        if (lcOrder.getPlanPieces() == null) {
            lcOrder.setPlanPieces(0);
        }
        if (lcOrder.getPlanVolume() == null) {
            lcOrder.setPlanVolume(BigDecimal.ZERO);
        }
        if (lcOrder.getPlanWeight() == null) {
            lcOrder.setPlanWeight(BigDecimal.ZERO);
        }
        if (lcOrder.getPlanChargeWeight() == null) {
            lcOrder.setPlanChargeWeight(BigDecimal.ZERO);
        }
        lcOrder.setBusinessScope("LC");
        lcOrder.setOrderStatus("订单创建");
        lcOrder.setIncomeStatus("未录收入");
        lcOrder.setCostStatus("未录成本");
        lcOrder.setIncomeRecorded(false);
        lcOrder.setCostRecorded(false);
        lcOrder.setOrderUuid(createUuid());
        lcOrder.setOrderCode(createOrderCode("LC"));
        lcOrder.setRowUuid(UUID.randomUUID().toString());
        lcOrder.setOrgId(SecurityUtils.getUser().getOrgId());
        lcOrder.setCreateTime(LocalDateTime.now());
        lcOrder.setCreatorId(SecurityUtils.getUser().getId());
        lcOrder.setCreatorName(SecurityUtils.getUser().getUserCname() + " " + SecurityUtils.getUser().getUserEmail());
        lcOrder.setEditorId(SecurityUtils.getUser().getId());
        lcOrder.setEditTime(LocalDateTime.now());
        lcOrder.setEditorName(SecurityUtils.getUser().getUserCname() + " " + SecurityUtils.getUser().getUserEmail());
        save(lcOrder);

        //保存日志
        LcLog lcLog = new LcLog();
        lcLog.setOrderId(lcOrder.getOrderId());
        lcLog.setOrderNumber(lcOrder.getOrderCode());
        lcLog.setOrderUuid(lcOrder.getOrderUuid());
        lcLog.setBusinessScope("LC");
        lcLog.setLogType("LC 订单");
        lcLog.setNodeName("订单创建");
        lcLog.setPageName("LC 订单");
        lcLog.setPageFunction("订单创建");
        lcLogService.insert(lcLog);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void modify(LcOrder lcOrder) {
        //校验
        LcOrder order = getById(lcOrder.getOrderId());
        if (order == null) {
            throw new RuntimeException("该订单不存在");
        }

        if (StrUtil.isNotBlank(order.getRowUuid()) && !order.getRowUuid().equals(lcOrder.getRowUuid())) {
            throw new RuntimeException("当前页面不是最新数据，请刷新再试");
        }
        if ("强制关闭".equals(order.getOrderStatus())) {
            throw new RuntimeException("订单已强制关闭,无法执行此操作");
        }
        if ("财务锁账".equals(order.getOrderStatus())) {
            throw new RuntimeException("订单已财务锁账,无法执行此操作");
        }
        //修改
        lcOrder.setRowUuid(UUID.randomUUID().toString());
        lcOrder.setEditorId(SecurityUtils.getUser().getId());
        lcOrder.setEditTime(LocalDateTime.now());
        lcOrder.setEditorName(SecurityUtils.getUser().getUserCname() + " " + SecurityUtils.getUser().getUserEmail());
        if (StrUtil.isNotBlank(lcOrder.getOrderCodeVl())) {
            lcOrder.setDriverNumber(order.getDriverNumber());
            lcOrder.setDriverName(order.getDriverName());
            lcOrder.setDriverTel(order.getDriverTel());
            lcOrder.setOrderCodeVl(order.getOrderCodeVl());
        }
        updateById(lcOrder);

        //保存日志
        LcLog lcLog = new LcLog();
        lcLog.setOrderId(lcOrder.getOrderId());
        lcLog.setOrderNumber(lcOrder.getOrderCode());
        lcLog.setOrderUuid(lcOrder.getOrderUuid());
        lcLog.setBusinessScope("LC");
        lcLog.setLogType("LC 订单");
        lcLog.setNodeName("订单修改");
        lcLog.setPageName("LC 订单");
        lcLog.setPageFunction("订单修改");
        lcLogService.insert(lcLog);
    }

    @Override
    public void delete(Integer orderId) {
        //校验
        LcOrder lcOrder = getById(orderId);
        if (lcOrder == null) {
            throw new RuntimeException("该订单不存在");
        }
        //删除
        delete(orderId);
    }

    @Override
    public LcOrder view(Integer orderId) {
        LcOrder lcOrder = getById(orderId);
        LambdaQueryWrapper<AfVPrmCoop> afVPrmCoopWrapper = Wrappers.<AfVPrmCoop>lambdaQuery();
        afVPrmCoopWrapper.eq(AfVPrmCoop::getCoopId, lcOrder.getCoopId());
        AfVPrmCoop afVPrmCoop = afVPrmCoopService.getOne(afVPrmCoopWrapper);
        if (afVPrmCoop != null) {
            lcOrder.setCoopName(afVPrmCoop.getCoopName());
        }

        //封装派车单信息
        LambdaQueryWrapper<VlOrderDetailOrder> vlOrderDetailOrderLambdaQueryWrapper = Wrappers.<VlOrderDetailOrder>lambdaQuery();
        vlOrderDetailOrderLambdaQueryWrapper.eq(VlOrderDetailOrder::getOrderId, orderId).eq(VlOrderDetailOrder::getBusinessScope, "LC").eq(VlOrderDetailOrder::getOrgId, SecurityUtils.getUser().getOrgId());
        List<VlOrderDetailOrder> orderDetailOrderList = vlOrderDetailOrderService.list(vlOrderDetailOrderLambdaQueryWrapper);
        if (orderDetailOrderList.size() > 0) {
            StringBuffer orderCodeVlBuffer = new StringBuffer();
            StringBuffer driverNumberBuffer = new StringBuffer();
            StringBuffer driverNameBuffer = new StringBuffer();
            StringBuffer driverTelBuffer = new StringBuffer();
            orderDetailOrderList.stream().forEach(vlOrderDetailOrder -> {
                VlOrder vlOrder = vlOrderMapper.selectById(vlOrderDetailOrder.getVlOrderId());
                //车牌号
                if (vlOrder.getTruckId() != null) {
                    LcTruck lcTruck = lcTruckService.getById(vlOrder.getTruckId());
                    if (lcTruck != null) {
                        if (driverNumberBuffer.length() == 0) {
                            driverNumberBuffer.append(lcTruck.getTruckNumber());
                        } else {
                            driverNumberBuffer.append(",").append(lcTruck.getTruckNumber());
                        }
                    }
                }
                //派单号
                if (orderCodeVlBuffer.length() == 0) {
                    orderCodeVlBuffer.append(vlOrder.getOrderCode());
                } else {
                    orderCodeVlBuffer.append(",").append(vlOrder.getOrderCode());
                }

                //司机姓名
                if (StrUtil.isNotBlank(vlOrder.getDriverName())) {
                    if (driverNameBuffer.length() == 0) {
                        driverNameBuffer.append(vlOrder.getDriverName());
                    } else {
                        driverNameBuffer.append(",").append(vlOrder.getDriverName());
                    }
                }

                //司机电话
                if (StrUtil.isNotBlank(vlOrder.getDriverTel())) {
                    if (driverTelBuffer.length() == 0) {
                        driverTelBuffer.append(vlOrder.getDriverTel());
                    } else {
                        driverTelBuffer.append(",").append(vlOrder.getDriverTel());
                    }
                }

            });

            lcOrder.setOrderCodeVl(orderCodeVlBuffer.toString());
            lcOrder.setDriverNumber(driverNumberBuffer.toString());
            lcOrder.setDriverName(driverNameBuffer.toString());
            lcOrder.setDriverTel(driverTelBuffer.toString());
        }
        return lcOrder;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void incomeComplete(Integer orderId) {
        LcOrder lcOrder = getById(orderId);
        if (lcOrder == null) {
            throw new RuntimeException("该订单不存在");
        }
        if (lcOrder.getIncomeRecorded()) {
            throw new RuntimeException("该订单已经收入完成");
        }
        if ("强制关闭".equals(lcOrder.getOrderStatus())) {
            throw new RuntimeException("订单已强制关闭,无法执行此操作");
        }
        if ("财务锁账".equals(lcOrder.getOrderStatus())) {
            throw new RuntimeException("订单已财务锁账,无法执行此操作");
        }
        lcOrder.setIncomeRecorded(true);
        //修改
        lcOrder.setRowUuid(UUID.randomUUID().toString());
        lcOrder.setEditorId(SecurityUtils.getUser().getId());
        lcOrder.setEditTime(LocalDateTime.now());
        lcOrder.setEditorName(SecurityUtils.getUser().getUserCname() + " " + SecurityUtils.getUser().getUserEmail());
        updateById(lcOrder);

        //保存日志
        LcLog lcLog = new LcLog();
        lcLog.setLogType("LC 订单");
        lcLog.setNodeName("费用录入");
        lcLog.setPageName("费用录入");
        lcLog.setPageFunction("收入完成");
        lcLog.setBusinessScope(lcOrder.getBusinessScope());
        lcLog.setOrderNumber(lcOrder.getOrderCode());
        lcLog.setOrderId(lcOrder.getOrderId());
        lcLog.setOrderUuid(lcOrder.getOrderUuid());
        lcLogService.insert(lcLog);

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void costComplete(Integer orderId) {
        LcOrder lcOrder = getById(orderId);
        if (lcOrder == null) {
            throw new RuntimeException("该订单不存在");
        }
        if (lcOrder.getCostRecorded()) {
            throw new RuntimeException("该订单已经成本完成");
        }
        if ("强制关闭".equals(lcOrder.getOrderStatus())) {
            throw new RuntimeException("订单已强制关闭,无法执行此操作");
        }
        if ("财务锁账".equals(lcOrder.getOrderStatus())) {
            throw new RuntimeException("订单已财务锁账,无法执行此操作");
        }
        lcOrder.setCostRecorded(true);
        //修改
        lcOrder.setRowUuid(UUID.randomUUID().toString());
        lcOrder.setEditorId(SecurityUtils.getUser().getId());
        lcOrder.setEditTime(LocalDateTime.now());
        lcOrder.setEditorName(SecurityUtils.getUser().getUserCname() + " " + SecurityUtils.getUser().getUserEmail());
        updateById(lcOrder);

        LcLog lcLog = new LcLog();
        lcLog.setLogType("LC 订单");
        lcLog.setNodeName("费用录入");
        lcLog.setPageName("费用录入");
        lcLog.setPageFunction("成本完成");
        lcLog.setBusinessScope(lcOrder.getBusinessScope());
        lcLog.setOrderNumber(lcOrder.getOrderCode());
        lcLog.setOrderId(lcOrder.getOrderId());
        lcLog.setOrderUuid(lcOrder.getOrderUuid());
        lcLogService.insert(lcLog);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void forceStop(String reason, String orderId) {
        //校验
        LcOrder lcOrder = getById(Integer.valueOf(orderId));
        if (lcOrder == null) {
            throw new RuntimeException("订单不存在");
        }
        if ("强制关闭".equals(lcOrder.getOrderStatus())) {
            throw new RuntimeException("订单已强制关闭,无需重复关闭");
        }
        if ("财务锁账".equals(lcOrder.getOrderStatus())) {
            throw new RuntimeException("订单已财务锁账,无法强制关闭");
        }
        if ("已制账单".equals(lcOrder.getIncomeStatus()) || "部分核销".equals(lcOrder.getIncomeStatus()) || "核销完毕".equals(lcOrder.getIncomeStatus())) {
            throw new RuntimeException("订单已生成收入账单,无法强制关闭");
        }
//        if ("已制账单".equals(lcOrder.getCostStatus()) || "部分核销".equals(lcOrder.getCostStatus()) || "核销完毕".equals(lcOrder.getCostStatus())) {
//            throw new RuntimeException("订单已生成成本账单,无法强制关闭");
//        }
        List<Map<String, Object>> list = baseMapper.getPaymentDetailByOrderId(lcOrder.getOrderId(), lcOrder.getBusinessScope(), SecurityUtils.getUser().getOrgId());
        if (list.size() > 0) {
            throw new RuntimeException("订单已生成成本账单,无法强制关闭");
        }
        //强制关闭
        lcOrder.setOrderStatus("强制关闭");
        lcOrder.setRowUuid(UUID.randomUUID().toString());
        lcOrder.setEditorId(SecurityUtils.getUser().getId());
        lcOrder.setEditTime(LocalDateTime.now());
        lcOrder.setEditorName(SecurityUtils.getUser().getUserCname() + " " + SecurityUtils.getUser().getUserEmail());
        updateById(lcOrder);

        //关闭后相关删除操作
        LambdaQueryWrapper<LcIncome> lcIncomeLambdaQueryWrapper = Wrappers.<LcIncome>lambdaQuery();
        lcIncomeLambdaQueryWrapper.eq(LcIncome::getOrderId, lcOrder.getOrderId()).eq(LcIncome::getOrgId, SecurityUtils.getUser().getOrgId());
        lcIncomeService.delete(lcIncomeLambdaQueryWrapper);

        LambdaQueryWrapper<LcCost> lcCostLambdaQueryWrapper = Wrappers.<LcCost>lambdaQuery();
        lcCostLambdaQueryWrapper.eq(LcCost::getOrderId, lcOrder.getOrderId()).eq(LcCost::getOrgId, SecurityUtils.getUser().getOrgId());
        lcCostService.remove(lcCostLambdaQueryWrapper);

        //保存日志
        LcLog lcLog = new LcLog();

        lcLog.setPageName(lcOrder.getBusinessScope() + "订单");
        lcLog.setPageFunction("强制关闭");
        lcLog.setLogRemark(reason);
        lcLog.setBusinessScope(lcOrder.getBusinessScope());
        lcLog.setOrderNumber(lcOrder.getOrderCode());
        lcLog.setOrderId(lcOrder.getOrderId());
        lcLog.setOrderUuid(lcOrder.getOrderUuid());

        lcLogService.insert(lcLog);
    }

    @Override
    public void exportExcel(LcOrder lcOrder) {
        lcOrder.setCurrentUserId(SecurityUtils.getUser().getId());
        LambdaQueryWrapper<LcOrder> wrapper = getWrapper(lcOrder);
        List<LcOrder> list = list(wrapper);
        fixRecords(list);

        List<LinkedHashMap> listExcel = new ArrayList<LinkedHashMap>();
        if (StrUtil.isNotBlank(lcOrder.getColumnStrs())) {
            //转json为数组
            JSONArray jsonArr = JSONArray.parseArray(lcOrder.getColumnStrs());
            String[] headers = new String[jsonArr.size()];
            String[] colunmStrs = new String[jsonArr.size()];

            //生成表头跟字段
            if (jsonArr != null && jsonArr.size() > 0) {
                for (int i = 0; i < jsonArr.size(); i++) {
                    JSONObject job = jsonArr.getJSONObject(i);
                    headers[i] = job.getString("label");
                    colunmStrs[i] = job.getString("prop");
                }
            }

            if (list != null && list.size() > 0) {
                //设置总数显示列
                LcOrder total = total(lcOrder);
                total.setOrderStatus("");
                FieldValUtils.setFieldValueByFieldName(colunmStrs[0], "合计：", total);
                list.add(total);
                for (LcOrder excel : list) {
                    LinkedHashMap map = new LinkedHashMap();
                    for (int j = 0; j < colunmStrs.length; j++) {
                        if ("incomeRecorded".equals(colunmStrs[j])) {
                            //收 完成状态
                            if ((excel.getIncomeRecorded() != null && excel.getIncomeRecorded()) || (StrUtil.isNotBlank(excel.getIncomeStatus()) && !"未录收入".equals(excel.getIncomeStatus()))) {
                                map.put("incomeRecorded", "√");
                            } else {
                                map.put("incomeRecorded", "");
                            }
                        } else if ("costRecorded".equals(colunmStrs[j])) {
                            if ((excel.getCostRecorded() != null && excel.getCostRecorded()) || (StrUtil.isNotBlank(excel.getCostStatus()) && !"未录成本".equals(excel.getCostStatus()))) {
                                map.put("costRecorded", "√");
                            } else {
                                map.put("costRecorded", "");
                            }
                        } else if ("drivingTime".equals(colunmStrs[j])) {
                            if (excel.getDrivingTime() != null) {
                                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
                                map.put("drivingTime", formatter.format(excel.getDrivingTime()));
                            } else {
                                map.put("drivingTime", "");
                            }
                        } else if ("servicerName".equals(colunmStrs[j])) {
                            if (!StringUtils.isEmpty(excel.getServicerName())) {
                                map.put("servicerName", excel.getServicerName().split(" ")[0]);
                            } else {
                                map.put("servicerName", "");
                            }
                        } else if ("salesName".equals(colunmStrs[j])) {
                            if (!StringUtils.isEmpty(excel.getSalesName())) {
                                map.put("salesName", excel.getSalesName().split(" ")[0]);
                            } else {
                                map.put("salesName", "");
                            }
                        } else if ("creatorName".equals(colunmStrs[j])) {
                            if (!StringUtils.isEmpty(excel.getCreatorName())) {
                                map.put("creatorName", excel.getCreatorName().split(" ")[0]);
                            } else {
                                map.put("creatorName", "");
                            }
                        } else if ("planWeight".equals(colunmStrs[j])) {
                            map.put(colunmStrs[j], excel.getPlanWeightStr());
                        } else if ("confirmWeight".equals(colunmStrs[j])) {
                            map.put(colunmStrs[j], excel.getConfirmWeightStr());
                        } else if ("planChargeWeight".equals(colunmStrs[j])) {
                            map.put(colunmStrs[j], excel.getPlanChargeWeightStr());
                        } else if ("confirmChargeWeight".equals(colunmStrs[j])) {
                            map.put(colunmStrs[j], excel.getConfirmChargeWeightStr());
                        } else if ("planVolume".equals(colunmStrs[j])) {
                            map.put(colunmStrs[j], excel.getPlanVolumeStr());
                        } else if ("confirmVolume".equals(colunmStrs[j])) {
                            map.put(colunmStrs[j], excel.getConfirmVolumeStr());
                        } else {
                            map.put(colunmStrs[j], FieldValUtils.getFieldValueByFieldName(colunmStrs[j], excel));
                        }
                    }
                    listExcel.add(map);
                }
            }
            ExcelExportUtils u = new ExcelExportUtils();
            u.exportExcelLinkListMap(LoginUtils.getResponse(), "导出EXCEL", headers, listExcel, "Export");
        }
    }

    @Override
    public List<OrderForVL> getLCOrderListForVL(OrderForVL orderForVL) {
        LambdaQueryWrapper<LcOrder> wrapper = Wrappers.<LcOrder>lambdaQuery();
        if (StrUtil.isNotBlank(orderForVL.getOrderCode())) {
            wrapper.like(LcOrder::getOrderCode, orderForVL.getOrderCode());
        }

        if (StrUtil.isNotBlank(orderForVL.getCustomerNumber())) {
            wrapper.like(LcOrder::getCustomerNumber, orderForVL.getCustomerNumber());
        }

        if (StrUtil.isNotBlank(orderForVL.getAwbNumber())) {
            wrapper.like(LcOrder::getCustomerNumber, orderForVL.getAwbNumber());
        }

        if (orderForVL.getBusinessScope().equals("LC") && orderForVL.getFlightDateStart() != null) {
            wrapper.ge(LcOrder::getDrivingTime, orderForVL.getFlightDateStart());
        }
        if (orderForVL.getBusinessScope().equals("LC") && orderForVL.getFlightDateEnd() != null) {
            wrapper.le(LcOrder::getDrivingTime, orderForVL.getFlightDateEnd());
        }
        if (StrUtil.isNotBlank(orderForVL.getNoOrderIds())) {
            wrapper.notIn(LcOrder::getOrderId, orderForVL.getNoOrderIds().split(","));
        }
        wrapper.eq(LcOrder::getOrgId, SecurityUtils.getUser().getOrgId()).eq(LcOrder::getBusinessScope, orderForVL.getBusinessScope()).ne(LcOrder::getCostRecorded, true).notIn(LcOrder::getOrderStatus, "强制关闭", "财务锁账").orderByAsc(LcOrder::getDrivingTime);
        return list(wrapper).stream().map(scOrder -> {
            OrderForVL order = new OrderForVL();
            BeanUtils.copyProperties(scOrder, order);
            order.setFlightDate(scOrder.getDrivingTime() != null ? scOrder.getDrivingTime().toLocalDate() : null);
            return order;
        }).collect(Collectors.toList());
    }

    /**
     * 对查询条件封装，方便调用
     *
     * @param lcOrder
     * @return
     */
    private LambdaQueryWrapper<LcOrder> getWrapper(LcOrder lcOrder) {
        LambdaQueryWrapper<LcOrder> wrapper = Wrappers.<LcOrder>lambdaQuery();

        //查询客户
        if (StrUtil.isNotBlank(lcOrder.getCoopName())) {
            LambdaQueryWrapper<AfVPrmCoop> afVPrmCoopWrapper = Wrappers.<AfVPrmCoop>lambdaQuery();
            afVPrmCoopWrapper.eq(AfVPrmCoop::getOrgId, SecurityUtils.getUser().getOrgId()).like(AfVPrmCoop::getCoopName, lcOrder.getCoopName());
            List<Integer> coopIds = afVPrmCoopService.list(afVPrmCoopWrapper).stream().map(afVPrmCoop -> afVPrmCoop.getCoopId()).collect(Collectors.toList());
            if (coopIds.isEmpty()) {
                return null;
            }
            wrapper.in(LcOrder::getCoopId, coopIds);
        }
        if (StrUtil.isNotBlank(lcOrder.getCustomerNumber())) {
            wrapper.like(LcOrder::getCustomerNumber, lcOrder.getCustomerNumber());
        }

        if (StrUtil.isNotBlank(lcOrder.getShippingMethod())) {
            wrapper.eq(LcOrder::getShippingMethod, lcOrder.getShippingMethod());
        }

        if (lcOrder.getCreateTimeStart() != null) {
            wrapper.ge(LcOrder::getCreateTime, lcOrder.getCreateTimeStart());
        }
        if (lcOrder.getCreateTimeEnd() != null) {
            wrapper.le(LcOrder::getCreateTime, lcOrder.getCreateTimeEnd());
        }

        if (lcOrder.getDrivingTimeStart() != null) {
            wrapper.ge(LcOrder::getDrivingTime, lcOrder.getDrivingTimeStart());
        }
        if (lcOrder.getDrivingTimeEnd() != null) {
            wrapper.le(LcOrder::getDrivingTime, lcOrder.getDrivingTimeEnd());
        }


        if (StrUtil.isNotBlank(lcOrder.getOrderCode())) {
            wrapper.like(LcOrder::getOrderCode, lcOrder.getOrderCode());
        }

        if (StrUtil.isNotBlank(lcOrder.getOrderCodeAssociated())) {
            wrapper.like(LcOrder::getOrderCodeAssociated, lcOrder.getOrderCodeAssociated());
        }
        if (StrUtil.isNotBlank(lcOrder.getDepartureStation())) {
            wrapper.like(LcOrder::getDepartureStation, lcOrder.getDepartureStation());
        }
        if (StrUtil.isNotBlank(lcOrder.getArrivalStation())) {
            wrapper.like(LcOrder::getArrivalStation, lcOrder.getArrivalStation());
        }
        if (StrUtil.isNotBlank(lcOrder.getOrderCodeVl())) {
            wrapper.like(LcOrder::getOrderCodeVl, lcOrder.getOrderCodeVl());
        }
        if (StrUtil.isNotBlank(lcOrder.getDriverName())) {
            wrapper.like(LcOrder::getDriverName, lcOrder.getDriverName());
        }
        if (StrUtil.isNotBlank(lcOrder.getDriverNumber())) {
            wrapper.like(LcOrder::getDriverNumber, lcOrder.getDriverNumber());
        }
        if (StrUtil.isNotBlank(lcOrder.getDriverTel())) {
            wrapper.like(LcOrder::getDriverTel, lcOrder.getDriverTel());
        }
        if (StrUtil.isNotBlank(lcOrder.getServicerName())) {
            wrapper.like(LcOrder::getServicerName, lcOrder.getServicerName());
        }
        if (StrUtil.isNotBlank(lcOrder.getSalesName())) {
            wrapper.like(LcOrder::getSalesName, lcOrder.getSalesName());
        }
        if (StrUtil.isNotBlank(lcOrder.getCreatorName())) {
            wrapper.like(LcOrder::getCreatorName, lcOrder.getCreatorName());
        }
        if ("未锁账".equals(lcOrder.getOrderStatus())) {
            wrapper.ne(LcOrder::getOrderStatus, "财务锁账");
        }
        if ("已锁账".equals(lcOrder.getOrderStatus())) {
            wrapper.eq(LcOrder::getOrderStatus, "财务锁账");
        }
        if (lcOrder.getIncomeRecorded() != null && lcOrder.getIncomeRecorded()) {
            wrapper.eq(LcOrder::getIncomeRecorded, lcOrder.getIncomeRecorded());
        }
        if (lcOrder.getIncomeRecorded() != null && !lcOrder.getIncomeRecorded()) {
            wrapper.and(i -> i.eq(LcOrder::getIncomeRecorded, lcOrder.getIncomeRecorded()).or(j -> j.isNull(LcOrder::getIncomeRecorded)));
        }
        if (lcOrder.getCostRecorded() != null && lcOrder.getCostRecorded()) {
            wrapper.eq(LcOrder::getCostRecorded, lcOrder.getCostRecorded());
        }
        if (lcOrder.getCostRecorded() != null && !lcOrder.getCostRecorded()) {
            wrapper.and(i -> i.eq(LcOrder::getCostRecorded, lcOrder.getCostRecorded()).or(j -> j.isNull(LcOrder::getCostRecorded)));
        }
        if (lcOrder.getOrderPermission() != null && lcOrder.getOrderPermission() == 1) {
            wrapper.and(i -> i.eq(LcOrder::getCreatorId, lcOrder.getCurrentUserId()).or(j -> j.eq(LcOrder::getSalesId, lcOrder.getCurrentUserId())).or(k -> k.eq(LcOrder::getServicerId, lcOrder.getCurrentUserId())));
        }
        if (lcOrder.getOrderPermission() != null && lcOrder.getOrderPermission() == 2) {
            List<Integer> WorkgroupIds = baseMapper.getWorkgroupIds(lcOrder.getCurrentUserId());
            wrapper.and(i -> i.eq(LcOrder::getCreatorId, lcOrder.getCurrentUserId()).or(j -> j.eq(LcOrder::getSalesId, lcOrder.getCurrentUserId())).or(k -> k.eq(LcOrder::getServicerId, lcOrder.getCurrentUserId())).or(m -> m.in(LcOrder::getWorkgroupId, WorkgroupIds)));
        }
        wrapper.eq(LcOrder::getOrgId, SecurityUtils.getUser().getOrgId()).ne(LcOrder::getOrderStatus, "强制关闭").orderByDesc(LcOrder::getOrderId);
        return wrapper;
    }

    /**
     * 对结果集封装
     *
     * @param list
     */
    private void fixRecords(List<LcOrder> list) {
        list.stream().forEach(lcOrder -> {
            LambdaQueryWrapper<AfVPrmCoop> afVPrmCoopLambdaQueryWrapper = Wrappers.<AfVPrmCoop>lambdaQuery();
            afVPrmCoopLambdaQueryWrapper.eq(AfVPrmCoop::getOrgId, SecurityUtils.getUser().getOrgId()).eq(AfVPrmCoop::getCoopId, lcOrder.getCoopId());
            AfVPrmCoop coop = afVPrmCoopService.getOne(afVPrmCoopLambdaQueryWrapper);
            if (coop != null) {
                lcOrder.setCoopName(coop.getCoopName());
            }

            if (StrUtil.isNotBlank(lcOrder.getDepartureStation())) {
                String cityNameCn = baseMapper.getCityNameCn(lcOrder.getDepartureStation());
                if(StrUtil.isNotBlank(cityNameCn)){
                    lcOrder.setDepartureStation(cityNameCn);
                }
                /*Airport airport = remoteServiceToAF.viewAirportCity(lcOrder.getDepartureStation()).getData();
                if (airport != null) {
                    lcOrder.setDepartureStation(airport.getCityNameCn());
                }*/
            }

            if (StrUtil.isNotBlank(lcOrder.getArrivalStation())) {
                String cityNameCn = baseMapper.getCityNameCn(lcOrder.getArrivalStation());
                if(StrUtil.isNotBlank(cityNameCn)){
                    lcOrder.setArrivalStation(cityNameCn);
                }
                /*Airport airport = remoteServiceToAF.viewAirportCity(lcOrder.getArrivalStation()).getData();
                if (airport != null) {
                    lcOrder.setArrivalStation(airport.getCityNameCn());
                }*/
            }
            //设置收入完成和成本完成（排序使用）
            if (lcOrder.getIncomeRecorded() == true) {
                lcOrder.setIncomeRecordedForSort(2);
            } else if (StringUtils.isNotBlank(lcOrder.getIncomeStatus()) && !"未录收入".equals(lcOrder.getIncomeStatus())) {
                lcOrder.setIncomeRecordedForSort(1);
            } else {
                lcOrder.setIncomeRecordedForSort(0);
            }
            if (lcOrder.getCostRecorded() == true) {
                lcOrder.setCostRecordedForSort(2);
            } else if (StringUtils.isNotBlank(lcOrder.getCostStatus()) && !"未录成本".equals(lcOrder.getCostStatus())) {
                lcOrder.setCostRecordedForSort(1);
            } else {
                lcOrder.setCostRecordedForSort(0);
            }

            //格式化
            lcOrder.setPlanWeightStr(lcOrder.getPlanWeight() == null ? "" : FormatUtils.formatWithQWFNoBit(lcOrder.getPlanWeight()));
            lcOrder.setPlanChargeWeightStr(lcOrder.getPlanChargeWeight() == null ? "" : FormatUtils.formatWithQWFNoBit(lcOrder.getPlanChargeWeight()));
            lcOrder.setPlanVolumeStr(lcOrder.getPlanVolume() == null ? "" : FormatUtils.formatWithQWFNoBit(lcOrder.getPlanVolume()));
            lcOrder.setConfirmWeightStr(lcOrder.getConfirmWeight() == null ? "" : FormatUtils.formatWithQWFNoBit(lcOrder.getConfirmWeight()));
            lcOrder.setConfirmChargeWeightStr(lcOrder.getConfirmChargeWeight() == null ? "" : FormatUtils.formatWithQWFNoBit(lcOrder.getConfirmChargeWeight()));
            lcOrder.setConfirmVolumeStr(lcOrder.getConfirmVolume() == null ? "" : FormatUtils.formatWithQWFNoBit(lcOrder.getConfirmVolume()));
        });
    }

    private String createOrderCode(String businessScope) {
        String numberPrefix = businessScope + "-" + DateTimeFormatter.ofPattern("yyMMdd").format(LocalDate.now());
        LambdaQueryWrapper<LcOrder> wrapper = Wrappers.<LcOrder>lambdaQuery();
        wrapper.eq(LcOrder::getOrgId, SecurityUtils.getUser().getOrgId()).like(LcOrder::getOrderCode, "%" + numberPrefix + "%").orderByDesc(LcOrder::getOrderCode).last(" limit 1");

        LcOrder order = getOne(wrapper);

        String numberSuffix = "";
        if (order == null) {
            numberSuffix = "0001";
        } else if (order.getOrderCode().substring(order.getOrderCode().length() - 4).equals("9999")) {
            throw new RuntimeException("今天订单已满无法创建,明天再整吧亲");
        } else {
            String n = Integer.valueOf(order.getOrderCode().substring(order.getOrderCode().length() - 4)) + 1 + "";
            numberSuffix = "0000".substring(0, 4 - n.length()) + n;
        }
        return numberPrefix + numberSuffix;
    }

    private String createUuid() {
        return baseMapper.getUuid();
    }
}
