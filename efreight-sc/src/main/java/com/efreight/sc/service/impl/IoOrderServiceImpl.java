package com.efreight.sc.service.impl;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.efreight.common.core.utils.ExcelExportUtils;
import com.efreight.common.core.utils.FormatUtils;
import com.efreight.common.security.util.SecurityUtils;
import com.efreight.sc.dao.IoIncomeMapper;
import com.efreight.sc.entity.*;
import com.efreight.sc.dao.IoOrderMapper;
import com.efreight.sc.entity.view.AfVPrmCoop;
import com.efreight.sc.service.*;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.efreight.sc.utils.FieldValUtils;
import com.efreight.sc.utils.LoginUtils;
import lombok.AllArgsConstructor;
import org.apache.commons.lang.StringUtils;
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
 * IO 订单管理 其他业务订单 服务实现类
 * </p>
 *
 * @author xiaobo
 * @since 2020-09-17
 */
@Service
@AllArgsConstructor
public class IoOrderServiceImpl extends ServiceImpl<IoOrderMapper, IoOrder> implements IoOrderService {

    private final AfVPrmCoopService afVPrmCoopService;

    private final IoLogService ioLogService;

    private final IoIncomeMapper ioIncomeService;

    private final IoCostService ioCostService;

    private final IoOrderShipperConsigneeService ioOrderShipperConsigneeService;

    @Override
    public IPage getPage(Page page, IoOrder ioOrder) {
        ioOrder.setCurrentUserId(SecurityUtils.getUser().getId());
        //获取查询条件
        LambdaQueryWrapper<IoOrder> wrapper = getWrapper(ioOrder);
        if (wrapper == null) {
            ArrayList<IoOrder> ioOrders = new ArrayList<>();
            page.setRecords(ioOrders);
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
    public IoOrder total(IoOrder ioOrder) {
        ioOrder.setCurrentUserId(SecurityUtils.getUser().getId());
        LambdaQueryWrapper<IoOrder> wrapper = getWrapper(ioOrder);
        IoOrder total = new IoOrder();
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

        });
        if (StrUtil.isBlank(total.getOrderStatus())) {
            return null;
        }
        total.setPlanPiecesStr(total.getPlanPieces() == null ? "" : FormatUtils.formatWithQWFNoBit(BigDecimal.valueOf(total.getPlanPieces())));
        total.setPlanWeightStr(total.getPlanWeight() == null ? "" : FormatUtils.formatWithQWFNoBit(total.getPlanWeight()));
        total.setPlanChargeWeightStr(total.getPlanChargeWeight() == null ? "" : FormatUtils.formatWithQWFNoBit(total.getPlanChargeWeight()));
        total.setPlanVolumeStr(total.getPlanVolume() == null ? "" : FormatUtils.formatWithQWFNoBit(total.getPlanVolume()));
        return total;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void insert(IoOrder ioOrder) {
        //校验

        //保存
        if (ioOrder.getPlanPieces() == null) {
            ioOrder.setPlanPieces(0);
        }
        if (ioOrder.getPlanVolume() == null) {
            ioOrder.setPlanVolume(BigDecimal.ZERO);
        }
        if (ioOrder.getPlanWeight() == null) {
            ioOrder.setPlanWeight(BigDecimal.ZERO);
        }
        if (ioOrder.getPlanChargeWeight() == null) {
            ioOrder.setPlanChargeWeight(BigDecimal.ZERO);
        }
        ioOrder.setBusinessScope("IO");
        ioOrder.setOrderStatus("订单创建");
        ioOrder.setIncomeStatus("未录收入");
        ioOrder.setCostStatus("未录成本");
        ioOrder.setIncomeRecorded(false);
        ioOrder.setCostRecorded(false);
        ioOrder.setOrderUuid(createUuid());
        ioOrder.setOrderCode(createOrderCode("IO"));
        if (StrUtil.isBlank(ioOrder.getCustomerNumber())) {
            ioOrder.setCustomerNumber(ioOrder.getOrderCode());
        }
        ioOrder.setRowUuid(UUID.randomUUID().toString());
        ioOrder.setOrgId(SecurityUtils.getUser().getOrgId());
        ioOrder.setCreateTime(LocalDateTime.now());
        ioOrder.setCreatorId(SecurityUtils.getUser().getId());
        ioOrder.setCreatorName(SecurityUtils.getUser().getUserCname() + " " + SecurityUtils.getUser().getUserEmail());
        ioOrder.setEditorId(SecurityUtils.getUser().getId());
        ioOrder.setEditTime(LocalDateTime.now());
        ioOrder.setEditorName(SecurityUtils.getUser().getUserCname() + " " + SecurityUtils.getUser().getUserEmail());
        save(ioOrder);

        //新建订单收发货人
        IoOrderShipperConsignee shipper = ioOrder.getShipper();
        IoOrderShipperConsignee consignee = ioOrder.getConsignee();
        if (StrUtil.isNotBlank(shipper.getScPrintRemark())) {
            shipper.setCreateTime(LocalDateTime.now());
            shipper.setCreatorId(SecurityUtils.getUser().getId());
            shipper.setCreatorName(SecurityUtils.getUser().getUserCname() + " " + SecurityUtils.getUser().getUserEmail());
            shipper.setOrgId(SecurityUtils.getUser().getOrgId());
            shipper.setEditorId(SecurityUtils.getUser().getId());
            shipper.setEditorName(SecurityUtils.getUser().getUserCname() + " " + SecurityUtils.getUser().getUserEmail());
            shipper.setEditTime(LocalDateTime.now());
            shipper.setOrderId(ioOrder.getOrderId());
            ioOrderShipperConsigneeService.save(shipper);
        }

        if (StrUtil.isNotBlank(consignee.getScPrintRemark())) {
            consignee.setCreateTime(LocalDateTime.now());
            consignee.setCreatorId(SecurityUtils.getUser().getId());
            consignee.setCreatorName(SecurityUtils.getUser().getUserCname() + " " + SecurityUtils.getUser().getUserEmail());
            consignee.setOrgId(SecurityUtils.getUser().getOrgId());
            consignee.setEditorId(SecurityUtils.getUser().getId());
            consignee.setEditorName(SecurityUtils.getUser().getUserCname() + " " + SecurityUtils.getUser().getUserEmail());
            consignee.setEditTime(LocalDateTime.now());
            consignee.setOrderId(ioOrder.getOrderId());
            ioOrderShipperConsigneeService.save(consignee);
        }
        //保存日志
        IoLog ioLog = new IoLog();
        ioLog.setOrderId(ioOrder.getOrderId());
        ioLog.setOrderNumber(ioOrder.getOrderCode());
        ioLog.setOrderUuid(ioOrder.getOrderUuid());
        ioLog.setBusinessScope("IO");
        ioLog.setLogType("IO 订单");
        ioLog.setNodeName("订单创建");
        ioLog.setPageName("IO 订单");
        ioLog.setPageFunction("订单创建");
        ioLogService.insert(ioLog);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void modify(IoOrder ioOrder) {
        //校验
        IoOrder order = getById(ioOrder.getOrderId());
        if (order == null) {
            throw new RuntimeException("该订单不存在");
        }

        if (StrUtil.isNotBlank(order.getRowUuid()) && !order.getRowUuid().equals(ioOrder.getRowUuid())) {
            throw new RuntimeException("当前页面不是最新数据，请刷新再试");
        }
        if ("强制关闭".equals(order.getOrderStatus())) {
            throw new RuntimeException("订单已强制关闭,无法执行此操作");
        }
        if ("财务锁账".equals(order.getOrderStatus())) {
            throw new RuntimeException("订单已财务锁账,无法执行此操作");
        }
        //修改
        if (StrUtil.isBlank(ioOrder.getCustomerNumber())) {
            ioOrder.setCustomerNumber(ioOrder.getOrderCode());
        }
        ioOrder.setRowUuid(UUID.randomUUID().toString());
        ioOrder.setEditorId(SecurityUtils.getUser().getId());
        ioOrder.setEditTime(LocalDateTime.now());
        ioOrder.setEditorName(SecurityUtils.getUser().getUserCname() + " " + SecurityUtils.getUser().getUserEmail());
        updateById(ioOrder);

        //修改收发货人
        IoOrderShipperConsignee shipper = ioOrder.getShipper();
        IoOrderShipperConsignee consignee = ioOrder.getConsignee();
        if (shipper!=null&&StrUtil.isNotBlank(shipper.getScPrintRemark())) {
            if (shipper.getOrderScId() == null) {
                shipper.setCreateTime(LocalDateTime.now());
                shipper.setCreatorId(SecurityUtils.getUser().getId());
                shipper.setCreatorName(SecurityUtils.getUser().getUserCname() + " " + SecurityUtils.getUser().getUserEmail());
                shipper.setOrgId(SecurityUtils.getUser().getOrgId());
                shipper.setEditorId(SecurityUtils.getUser().getId());
                shipper.setEditorName(SecurityUtils.getUser().getUserCname() + " " + SecurityUtils.getUser().getUserEmail());
                shipper.setEditTime(LocalDateTime.now());
                shipper.setOrderId(order.getOrderId());
                ioOrderShipperConsigneeService.save(shipper);
            } else {
                shipper.setEditorId(SecurityUtils.getUser().getId());
                shipper.setEditorName(SecurityUtils.getUser().getUserCname() + " " + SecurityUtils.getUser().getUserEmail());
                shipper.setEditTime(LocalDateTime.now());
                ioOrderShipperConsigneeService.updateById(shipper);
            }
        } else {
            if (shipper!=null&&shipper.getOrderScId() != null) {
                ioOrderShipperConsigneeService.removeById(shipper.getOrderScId());
            }
        }

        if (consignee!=null&&StrUtil.isNotBlank(consignee.getScPrintRemark())) {
            if (consignee.getOrderScId() == null) {
                consignee.setCreateTime(LocalDateTime.now());
                consignee.setCreatorId(SecurityUtils.getUser().getId());
                consignee.setCreatorName(SecurityUtils.getUser().getUserCname() + " " + SecurityUtils.getUser().getUserEmail());
                consignee.setOrgId(SecurityUtils.getUser().getOrgId());
                consignee.setEditorId(SecurityUtils.getUser().getId());
                consignee.setEditorName(SecurityUtils.getUser().getUserCname() + " " + SecurityUtils.getUser().getUserEmail());
                consignee.setEditTime(LocalDateTime.now());
                consignee.setOrderId(order.getOrderId());
                ioOrderShipperConsigneeService.save(consignee);
            } else {
                consignee.setEditorId(SecurityUtils.getUser().getId());
                consignee.setEditorName(SecurityUtils.getUser().getUserCname() + " " + SecurityUtils.getUser().getUserEmail());
                consignee.setEditTime(LocalDateTime.now());
                ioOrderShipperConsigneeService.updateById(consignee);
            }
        } else {
            if (consignee!=null&&consignee.getOrderScId() != null) {
                ioOrderShipperConsigneeService.removeById(consignee.getOrderScId());
            }
        }
        //保存日志
        IoLog ioLog = new IoLog();
        ioLog.setOrderId(ioOrder.getOrderId());
        ioLog.setOrderNumber(ioOrder.getOrderCode());
        ioLog.setOrderUuid(ioOrder.getOrderUuid());
        ioLog.setBusinessScope("IO");
        ioLog.setLogType("IO 订单");
        ioLog.setNodeName("订单修改");
        ioLog.setPageName("IO 订单");
        ioLog.setPageFunction("订单修改");
        ioLogService.insert(ioLog);
    }

    @Override
    public void delete(Integer orderId) {
        //校验
        IoOrder ioOrder = getById(orderId);
        if (ioOrder == null) {
            throw new RuntimeException("该订单不存在");
        }
        //删除
        delete(orderId);
    }

    @Override
    public IoOrder view(Integer orderId) {
        IoOrder ioOrder = getById(orderId);
        LambdaQueryWrapper<AfVPrmCoop> afVPrmCoopWrapper = Wrappers.<AfVPrmCoop>lambdaQuery();
        afVPrmCoopWrapper.eq(AfVPrmCoop::getCoopId, ioOrder.getCoopId());
        AfVPrmCoop afVPrmCoop = afVPrmCoopService.getOne(afVPrmCoopWrapper);
        if (afVPrmCoop != null) {
            ioOrder.setCoopName(afVPrmCoop.getCoopName());
        }
        LambdaQueryWrapper<IoOrderShipperConsignee> shipperWrapper = Wrappers.<IoOrderShipperConsignee>lambdaQuery();
        shipperWrapper.eq(IoOrderShipperConsignee::getOrderId, orderId).eq(IoOrderShipperConsignee::getOrgId, SecurityUtils.getUser().getOrgId()).eq(IoOrderShipperConsignee::getScType, 0);
        IoOrderShipperConsignee shipper = ioOrderShipperConsigneeService.getOne(shipperWrapper);
        if (shipper == null) {
            shipper = new IoOrderShipperConsignee();
            shipper.setScPrintRemark("");
        }
        ioOrder.setShipper(shipper);

        LambdaQueryWrapper<IoOrderShipperConsignee> consigneeWrapper = Wrappers.<IoOrderShipperConsignee>lambdaQuery();
        consigneeWrapper.eq(IoOrderShipperConsignee::getOrderId, orderId).eq(IoOrderShipperConsignee::getOrgId, SecurityUtils.getUser().getOrgId()).eq(IoOrderShipperConsignee::getScType, 1);
        IoOrderShipperConsignee consignee = ioOrderShipperConsigneeService.getOne(consigneeWrapper);
        if (consignee == null) {
            consignee = new IoOrderShipperConsignee();
            consignee.setScPrintRemark("");
        }
        ioOrder.setConsignee(consignee);

        return ioOrder;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void incomeComplete(Integer orderId) {
        IoOrder ioOrder = getById(orderId);
        if (ioOrder == null) {
            throw new RuntimeException("该订单不存在");
        }
        if (ioOrder.getIncomeRecorded()) {
            throw new RuntimeException("该订单已经收入完成");
        }
        if ("强制关闭".equals(ioOrder.getOrderStatus())) {
            throw new RuntimeException("订单已强制关闭,无法执行此操作");
        }
        if ("财务锁账".equals(ioOrder.getOrderStatus())) {
            throw new RuntimeException("订单已财务锁账,无法执行此操作");
        }
        ioOrder.setIncomeRecorded(true);
        //修改
        ioOrder.setRowUuid(UUID.randomUUID().toString());
        ioOrder.setEditorId(SecurityUtils.getUser().getId());
        ioOrder.setEditTime(LocalDateTime.now());
        ioOrder.setEditorName(SecurityUtils.getUser().getUserCname() + " " + SecurityUtils.getUser().getUserEmail());
        updateById(ioOrder);

        //保存日志
        IoLog ioLog = new IoLog();
        ioLog.setLogType("IO 订单");
        ioLog.setNodeName("费用录入");
        ioLog.setPageName("费用录入");
        ioLog.setPageFunction("收入完成");
        ioLog.setBusinessScope(ioOrder.getBusinessScope());
        ioLog.setOrderNumber(ioOrder.getOrderCode());
        ioLog.setOrderId(ioOrder.getOrderId());
        ioLog.setOrderUuid(ioOrder.getOrderUuid());
        ioLogService.insert(ioLog);

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void costComplete(Integer orderId) {
        IoOrder ioOrder = getById(orderId);
        if (ioOrder == null) {
            throw new RuntimeException("该订单不存在");
        }
        if (ioOrder.getCostRecorded()) {
            throw new RuntimeException("该订单已经成本完成");
        }
        if ("强制关闭".equals(ioOrder.getOrderStatus())) {
            throw new RuntimeException("订单已强制关闭,无法执行此操作");
        }
        if ("财务锁账".equals(ioOrder.getOrderStatus())) {
            throw new RuntimeException("订单已财务锁账,无法执行此操作");
        }
        ioOrder.setCostRecorded(true);
        //修改
        ioOrder.setRowUuid(UUID.randomUUID().toString());
        ioOrder.setEditorId(SecurityUtils.getUser().getId());
        ioOrder.setEditTime(LocalDateTime.now());
        ioOrder.setEditorName(SecurityUtils.getUser().getUserCname() + " " + SecurityUtils.getUser().getUserEmail());
        updateById(ioOrder);

        IoLog ioLog = new IoLog();
        ioLog.setLogType("IO 订单");
        ioLog.setNodeName("费用录入");
        ioLog.setPageName("费用录入");
        ioLog.setPageFunction("成本完成");
        ioLog.setBusinessScope(ioOrder.getBusinessScope());
        ioLog.setOrderNumber(ioOrder.getOrderCode());
        ioLog.setOrderId(ioOrder.getOrderId());
        ioLog.setOrderUuid(ioOrder.getOrderUuid());
        ioLogService.insert(ioLog);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void forceStop(String reason, String orderId) {
        //校验
        IoOrder ioOrder = getById(Integer.valueOf(orderId));
        if (ioOrder == null) {
            throw new RuntimeException("订单不存在");
        }
        if ("强制关闭".equals(ioOrder.getOrderStatus())) {
            throw new RuntimeException("订单已强制关闭,无需重复关闭");
        }
        if ("财务锁账".equals(ioOrder.getOrderStatus())) {
            throw new RuntimeException("订单已财务锁账,无法强制关闭");
        }
        if ("已制账单".equals(ioOrder.getIncomeStatus()) || "部分核销".equals(ioOrder.getIncomeStatus()) || "核销完毕".equals(ioOrder.getIncomeStatus())) {
            throw new RuntimeException("订单已生成收入账单,无法强制关闭");
        }
//        if ("已制账单".equals(ioOrder.getCostStatus()) || "部分核销".equals(ioOrder.getCostStatus()) || "核销完毕".equals(ioOrder.getCostStatus())) {
//            throw new RuntimeException("订单已生成成本账单,无法强制关闭");
//        }
        List<Map<String, Object>> list = baseMapper.getPaymentDetailByOrderId(ioOrder.getOrderId(), ioOrder.getBusinessScope(), SecurityUtils.getUser().getOrgId());
        if (list.size() > 0) {
            throw new RuntimeException("订单已生成成本账单,无法强制关闭");
        }
        //强制关闭
        ioOrder.setOrderStatus("强制关闭");
        ioOrder.setRowUuid(UUID.randomUUID().toString());
        ioOrder.setEditorId(SecurityUtils.getUser().getId());
        ioOrder.setEditTime(LocalDateTime.now());
        ioOrder.setEditorName(SecurityUtils.getUser().getUserCname() + " " + SecurityUtils.getUser().getUserEmail());
        updateById(ioOrder);

        //关闭后相关删除操作
        LambdaQueryWrapper<IoIncome> ioIncomeLambdaQueryWrapper = Wrappers.<IoIncome>lambdaQuery();
        ioIncomeLambdaQueryWrapper.eq(IoIncome::getOrderId, ioOrder.getOrderId()).eq(IoIncome::getOrgId, SecurityUtils.getUser().getOrgId());
        ioIncomeService.delete(ioIncomeLambdaQueryWrapper);

        LambdaQueryWrapper<IoCost> ioCostLambdaQueryWrapper = Wrappers.<IoCost>lambdaQuery();
        ioCostLambdaQueryWrapper.eq(IoCost::getOrderId, ioOrder.getOrderId()).eq(IoCost::getOrgId, SecurityUtils.getUser().getOrgId());
        ioCostService.remove(ioCostLambdaQueryWrapper);

        //保存日志
        IoLog ioLog = new IoLog();

        ioLog.setPageName(ioOrder.getBusinessScope() + "订单");
        ioLog.setPageFunction("强制关闭");
        ioLog.setLogRemark(reason);
        ioLog.setBusinessScope(ioOrder.getBusinessScope());
        ioLog.setOrderNumber(ioOrder.getOrderCode());
        ioLog.setOrderId(ioOrder.getOrderId());
        ioLog.setOrderUuid(ioOrder.getOrderUuid());

        ioLogService.insert(ioLog);
    }

    @Override
    public void exportExcel(IoOrder ioOrder) {
        LambdaQueryWrapper<IoOrder> wrapper = getWrapper(ioOrder);
        List<IoOrder> list = list(wrapper);
        fixRecords(list);

        List<LinkedHashMap> listExcel = new ArrayList<LinkedHashMap>();
        if (StrUtil.isNotBlank(ioOrder.getColumnStrs())) {
            //转json为数组
            JSONArray jsonArr = JSONArray.parseArray(ioOrder.getColumnStrs());
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
                IoOrder total = total(ioOrder);
                total.setOrderStatus("");
                FieldValUtils.setFieldValueByFieldName(colunmStrs[0], "合计：", total);
                list.add(total);
                for (IoOrder excel : list) {
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
                        } else if ("businessDate".equals(colunmStrs[j])) {
                            if (excel.getBusinessDate() != null) {
                                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                                map.put("businessDate", formatter.format(excel.getBusinessDate()));
                            } else {
                                map.put("businessDate", "");
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
                        } else if ("planPieces".equals(colunmStrs[j])) {
                            map.put(colunmStrs[j], excel.getPlanPiecesStr());
                        } else if ("planWeight".equals(colunmStrs[j])) {
                            map.put(colunmStrs[j], excel.getPlanWeightStr());
                        } else if ("planChargeWeight".equals(colunmStrs[j])) {
                            map.put(colunmStrs[j], excel.getPlanChargeWeightStr());
                        } else if ("planVolume".equals(colunmStrs[j])) {
                            map.put(colunmStrs[j], excel.getPlanVolumeStr());
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


    /**
     * 对查询条件封装，方便调用
     *
     * @param ioOrder
     * @return
     */
    private LambdaQueryWrapper<IoOrder> getWrapper(IoOrder ioOrder) {
        LambdaQueryWrapper<IoOrder> wrapper = Wrappers.<IoOrder>lambdaQuery();

        //查询客户
        if (StrUtil.isNotBlank(ioOrder.getCoopName())) {
            LambdaQueryWrapper<AfVPrmCoop> afVPrmCoopWrapper = Wrappers.<AfVPrmCoop>lambdaQuery();
            afVPrmCoopWrapper.eq(AfVPrmCoop::getOrgId, SecurityUtils.getUser().getOrgId()).like(AfVPrmCoop::getCoopName, ioOrder.getCoopName());
            List<Integer> coopIds = afVPrmCoopService.list(afVPrmCoopWrapper).stream().map(afVPrmCoop -> afVPrmCoop.getCoopId()).collect(Collectors.toList());
            if (coopIds.isEmpty()) {
                return null;
            }
            wrapper.in(IoOrder::getCoopId, coopIds);
        }
        if (StrUtil.isNotBlank(ioOrder.getCustomerNumber())) {
            wrapper.like(IoOrder::getCustomerNumber, ioOrder.getCustomerNumber());
        }
        if (StrUtil.isNotBlank(ioOrder.getBusinessMethod())) {
            wrapper.eq(IoOrder::getBusinessMethod, ioOrder.getBusinessMethod());
        }
        if (ioOrder.getCreateTimeStart() != null) {
            wrapper.ge(IoOrder::getCreateTime, ioOrder.getCreateTimeStart());
        }
        if (ioOrder.getCreateTimeEnd() != null) {
            wrapper.le(IoOrder::getCreateTime, ioOrder.getCreateTimeEnd());
        }

        if (ioOrder.getBusinessDateStart() != null) {
            wrapper.ge(IoOrder::getBusinessDate, ioOrder.getBusinessDateStart());
        }
        if (ioOrder.getBusinessDateEnd() != null) {
            wrapper.le(IoOrder::getBusinessDate, ioOrder.getBusinessDateEnd());
        }


        if (StrUtil.isNotBlank(ioOrder.getOrderCode())) {
            wrapper.like(IoOrder::getOrderCode, ioOrder.getOrderCode());
        }

        if (StrUtil.isNotBlank(ioOrder.getOrderCodeAssociated())) {
            wrapper.like(IoOrder::getOrderCodeAssociated, ioOrder.getOrderCodeAssociated());
        }
        if (StrUtil.isNotBlank(ioOrder.getDepartureStation())) {
            wrapper.like(IoOrder::getDepartureStation, ioOrder.getDepartureStation());
        }
        if (StrUtil.isNotBlank(ioOrder.getArrivalStation())) {
            wrapper.like(IoOrder::getArrivalStation, ioOrder.getArrivalStation());
        }
        if (StrUtil.isNotBlank(ioOrder.getServicerName())) {
            wrapper.like(IoOrder::getServicerName, ioOrder.getServicerName());
        }
        if (StrUtil.isNotBlank(ioOrder.getSalesName())) {
            wrapper.like(IoOrder::getSalesName, ioOrder.getSalesName());
        }
        if (StrUtil.isNotBlank(ioOrder.getCreatorName())) {
            wrapper.like(IoOrder::getCreatorName, ioOrder.getCreatorName());
        }
        if ("未锁账".equals(ioOrder.getOrderStatus())) {
            wrapper.ne(IoOrder::getOrderStatus, "财务锁账");
        }
        if ("已锁账".equals(ioOrder.getOrderStatus())) {
            wrapper.eq(IoOrder::getOrderStatus, "财务锁账");
        }
        if (ioOrder.getIncomeRecorded() != null && ioOrder.getIncomeRecorded()) {
            wrapper.eq(IoOrder::getIncomeRecorded, ioOrder.getIncomeRecorded());
        }
        if (ioOrder.getIncomeRecorded() != null && !ioOrder.getIncomeRecorded()) {
            wrapper.and(i -> i.eq(IoOrder::getIncomeRecorded, ioOrder.getIncomeRecorded()).or(j -> j.isNull(IoOrder::getIncomeRecorded)));
        }
        if (ioOrder.getCostRecorded() != null && ioOrder.getCostRecorded()) {
            wrapper.eq(IoOrder::getCostRecorded, ioOrder.getCostRecorded());
        }
        if (ioOrder.getCostRecorded() != null && !ioOrder.getCostRecorded()) {
            wrapper.and(i -> i.eq(IoOrder::getCostRecorded, ioOrder.getCostRecorded()).or(j -> j.isNull(IoOrder::getCostRecorded)));
        }
        if (ioOrder.getOrderPermission() == 1) {
            wrapper.and(i -> i.eq(IoOrder::getCreatorId, ioOrder.getCurrentUserId()).or(j -> j.eq(IoOrder::getSalesId, ioOrder.getCurrentUserId())).or(k -> k.eq(IoOrder::getServicerId, ioOrder.getCurrentUserId())));
        }
        if (ioOrder.getOrderPermission() == 2) {
            List<Integer> WorkgroupIds = baseMapper.getWorkgroupIds(ioOrder.getCurrentUserId());
            wrapper.and(i -> i.eq(IoOrder::getCreatorId, ioOrder.getCurrentUserId()).or(j -> j.eq(IoOrder::getSalesId, ioOrder.getCurrentUserId())).or(k -> k.eq(IoOrder::getServicerId, ioOrder.getCurrentUserId())).or(m -> m.in(IoOrder::getWorkgroupId, WorkgroupIds)));
        }
        wrapper.eq(IoOrder::getOrgId, SecurityUtils.getUser().getOrgId()).ne(IoOrder::getOrderStatus, "强制关闭").orderByDesc(IoOrder::getOrderId);
        return wrapper;
    }

    /**
     * 对结果集封装
     *
     * @param list
     */
    private void fixRecords(List<IoOrder> list) {
        list.stream().forEach(ioOrder -> {
            LambdaQueryWrapper<AfVPrmCoop> afVPrmCoopLambdaQueryWrapper = Wrappers.<AfVPrmCoop>lambdaQuery();
            afVPrmCoopLambdaQueryWrapper.eq(AfVPrmCoop::getOrgId, SecurityUtils.getUser().getOrgId()).eq(AfVPrmCoop::getCoopId, ioOrder.getCoopId());
            AfVPrmCoop coop = afVPrmCoopService.getOne(afVPrmCoopLambdaQueryWrapper);
            if (coop != null) {
                ioOrder.setCoopName(coop.getCoopName());
            }

            //设置收入完成和成本完成（排序使用）
            if (ioOrder.getIncomeRecorded() == true) {
                ioOrder.setIncomeRecordedForSort(2);
            } else if (StringUtils.isNotBlank(ioOrder.getIncomeStatus()) && !"未录收入".equals(ioOrder.getIncomeStatus())) {
                ioOrder.setIncomeRecordedForSort(1);
            } else {
                ioOrder.setIncomeRecordedForSort(0);
            }
            if (ioOrder.getCostRecorded() == true) {
                ioOrder.setCostRecordedForSort(2);
            } else if (StringUtils.isNotBlank(ioOrder.getCostStatus()) && !"未录成本".equals(ioOrder.getCostStatus())) {
                ioOrder.setCostRecordedForSort(1);
            } else {
                ioOrder.setCostRecordedForSort(0);
            }

            //格式化
            ioOrder.setPlanPiecesStr(ioOrder.getPlanPieces() == null ? "" : FormatUtils.formatWithQWFNoBit(BigDecimal.valueOf(ioOrder.getPlanPieces())));
            ioOrder.setPlanWeightStr(ioOrder.getPlanWeight() == null ? "" : FormatUtils.formatWithQWFNoBit(ioOrder.getPlanWeight()));
            ioOrder.setPlanChargeWeightStr(ioOrder.getPlanChargeWeight() == null ? "" : FormatUtils.formatWithQWFNoBit(ioOrder.getPlanChargeWeight()));
            ioOrder.setPlanVolumeStr(ioOrder.getPlanVolume() == null ? "" : FormatUtils.formatWithQWFNoBit(ioOrder.getPlanVolume()));
        });
    }

    private String createOrderCode(String businessScope) {
        String numberPrefix = businessScope + "-" + DateTimeFormatter.ofPattern("yyMMdd").format(LocalDate.now());
        LambdaQueryWrapper<IoOrder> wrapper = Wrappers.<IoOrder>lambdaQuery();
        wrapper.eq(IoOrder::getOrgId, SecurityUtils.getUser().getOrgId()).like(IoOrder::getOrderCode, "%" + numberPrefix + "%").orderByDesc(IoOrder::getOrderCode).last(" limit 1");

        IoOrder order = getOne(wrapper);

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
