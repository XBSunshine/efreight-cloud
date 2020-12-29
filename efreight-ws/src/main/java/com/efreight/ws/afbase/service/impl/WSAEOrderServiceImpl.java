package com.efreight.ws.afbase.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.efreight.ws.afbase.contant.AFConstant;
import com.efreight.ws.afbase.entity.*;
import com.efreight.ws.afbase.mapper.WSAwbNumberMapper;
import com.efreight.ws.afbase.mapper.WSInboundMapper;
import com.efreight.ws.afbase.mapper.WSOrderMapper;
import com.efreight.ws.afbase.mapper.WSOrderShipperConsigneeMapper;
import com.efreight.ws.afbase.pojo.order.create.CreateOrderRequest;
import com.efreight.ws.afbase.pojo.order.create.WSCreateOrderResponse;
import com.efreight.ws.afbase.pojo.order.detail.*;
import com.efreight.ws.afbase.pojo.order.edit.EditOrderRequest;
import com.efreight.ws.afbase.pojo.order.edit.WSEditOrderResponse;
import com.efreight.ws.afbase.pojo.order.edit.inbound.EditInboundOrderRequest;
import com.efreight.ws.afbase.pojo.order.edit.inbound.WSEditInboundOrderResponse;
import com.efreight.ws.afbase.pojo.order.inbound.InboundOrderRequest;
import com.efreight.ws.afbase.pojo.order.inbound.WSInboundOrderResponse;
import com.efreight.ws.afbase.pojo.order.list.ListOrderRequest;
import com.efreight.ws.afbase.pojo.order.list.Order;
import com.efreight.ws.afbase.pojo.order.list.WSListOrderResponse;
import com.efreight.ws.afbase.service.WSAEOrderService;
import com.efreight.ws.afbase.service.WSLogService;
import com.efreight.ws.common.contant.EFConstant;
import com.efreight.ws.common.pojo.WSException;
import com.efreight.ws.common.util.ValidUtil;
import com.efreight.ws.hrs.entity.WSUser;
import com.efreight.ws.prm.entity.WSCoop;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class WSAEOrderServiceImpl extends ServiceImpl<WSOrderMapper, WSOrder> implements WSAEOrderService {

    private static final Map<String, String> CREATE_ORDER_VALID_FIELD  = new HashMap<>();
    private static final Map<String, String> INBOUND_ORDER_VALID_FIELD  = new HashMap<>();

    static {
        CREATE_ORDER_VALID_FIELD.put("coopCode", "参数错误：客户代码");
        CREATE_ORDER_VALID_FIELD.put("storehouseCode", "参数错误：仓库代码");
        CREATE_ORDER_VALID_FIELD.put("departureStation", "参数错误：始发港");
        CREATE_ORDER_VALID_FIELD.put("arrivalStation", "参数错误：目的港");
        CREATE_ORDER_VALID_FIELD.put("expectDeparture", "参数错误：航班日期");
        CREATE_ORDER_VALID_FIELD.put("planPieces", "参数错误：预报件数");
        CREATE_ORDER_VALID_FIELD.put("planWeight", "参数错误：预报毛重");
        CREATE_ORDER_VALID_FIELD.put("planVolume", "参数错误：预报体积");
        CREATE_ORDER_VALID_FIELD.put("freightUnitPrice", "参数错误：空运费单价");
        CREATE_ORDER_VALID_FIELD.put("serviceName", "参数错误：客服负责人");
        CREATE_ORDER_VALID_FIELD.put("salesName", "参数错误：销售负责人");
        CREATE_ORDER_VALID_FIELD.put("awbNumber", "主运单号");

        INBOUND_ORDER_VALID_FIELD.put("orderCode", "参数错误：订单号");
        INBOUND_ORDER_VALID_FIELD.put("awbNumber", "参数错误：主单号");
        INBOUND_ORDER_VALID_FIELD.put("orderPieces", "参数错误：入库件数");
        INBOUND_ORDER_VALID_FIELD.put("orderGrossWeight", "参数错误：入库毛重");
        INBOUND_ORDER_VALID_FIELD.put("orderVolume", "参数错误：入库体积");
        INBOUND_ORDER_VALID_FIELD.put("orderChargeWeight", "参数错误：计费重量");
        INBOUND_ORDER_VALID_FIELD.put("receiptDate", "参数错误：到货日期");
        INBOUND_ORDER_VALID_FIELD.put("warehouseCode", "参数错误：货站代码");
    }

    @Resource
    private WSAwbNumberMapper wsAwbNumberMapper;
    @Resource
    private WSOrderShipperConsigneeMapper wsOrderShipperConsigneeMapper;
    @Resource
    private WSLogService wsLogService;
    @Resource
    private WSInboundMapper wsInboundMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public WSCreateOrderResponse createOrder(Integer orgId, CreateOrderRequest orderRequest) {
        ValidUtil.valid(orgId, "参数错误：未获取到机构ID");
        ValidUtil.valid(orderRequest, CREATE_ORDER_VALID_FIELD);

        LocalDate expectDeparture = getLocalDate(orderRequest.getExpectDeparture());
        //验证客户代码
        WSCoop wsCoop = this.baseMapper.getCoopByCode(orgId, orderRequest.getCoopCode());
        if(null == wsCoop){
            throw new WSException(201001, "订单创建：客户不存在或未启用");
        }
        //检货站代码
        WSWarehouse wsWarehouse = this.baseMapper.getWarehouseByCodeAndApCode(orgId, orderRequest.getStorehouseCode());
        if(null == wsWarehouse){
            throw new WSException(201002, "订单创建：未查询到有效货站信息");
        }
        //检查港口
        WSAirPort departureStation = this.baseMapper.getAriPortByApCode(orderRequest.getDepartureStation());
        if(null == departureStation){
            throw new WSException(201003, "订单创建：始发港不存在或未启用");
        }
        WSAirPort arrivalStation = this.baseMapper.getAriPortByApCode(orderRequest.getArrivalStation());
        if(null == arrivalStation){
            throw new WSException(201004, "订单创建：目的港不存在或未启用");
        }
        //客服
        WSUser serviceUser = this.baseMapper.getUserByName(orgId, orderRequest.getServiceName());
        if(null == serviceUser){
            throw new WSException(201005, "订单创建：客服不存在或未启用");
        }
        WSUser saleUser = this.baseMapper.getUserByName(orgId, orderRequest.getSalesName());
        if(null == serviceUser){
            throw new WSException(201006, "订单创建：销售不存在或未启用");
        }
        //主单号
        WSAwbNumber wsAwbNumber = wsAwbNumberMapper.getAwbNumberByAwbNumber(orgId, orderRequest.getAwbNumber());
        if(null == wsAwbNumber){
            throw new WSException(201007, "订单创建：主单号不存在或已使用");
        }
        //修改-主单号状态
        updateAwbNumberStatus(wsAwbNumber);
        //添加-订单数据
        WSOrder wsOrder = new WSOrder();
        wsOrder.setOrgId(orgId);
        wsOrder.setOrderUuid(UUID.randomUUID().toString());
        wsOrder.setOrderCode(getOrderCode(orgId, AFConstant.SERVICE_SCOPE.AE));
        wsOrder.setCustomerNumber(wsOrder.getOrderCode());
        wsOrder.setOrderStatus("订单创建");
        wsOrder.setDepartureStorehouseId(wsWarehouse.getWarehouseId());
        wsOrder.setDepartureStation(departureStation.getApCode());
        wsOrder.setArrivalStation(arrivalStation.getApCode());
        wsOrder.setExpectDeparture(expectDeparture);
        wsOrder.setPlanPieces(orderRequest.getPlanPieces());
        wsOrder.setPlanWeight(orderRequest.getPlanWeight());
        wsOrder.setPlanVolume(orderRequest.getPlanVolume());
        wsOrder.setFreightUnitprice(orderRequest.getFreightUnitPrice());
        wsOrder.setServicerId(serviceUser.getUserId());
        wsOrder.setServicerName(serviceUser.buildUserName());
        wsOrder.setSalesId(saleUser.getUserId());
        wsOrder.setSalesName(saleUser.buildUserName());
        wsOrder.setAwbId(wsAwbNumber.getAwbId());
        wsOrder.setAwbUuid(wsAwbNumber.getAwbUuid());
        wsOrder.setAwbNumber(wsAwbNumber.getAwbNumber());
        wsOrder.setOperationRemark(orderRequest.getOperationRemark());
        wsOrder.setFreightProfitRatioRemark(orderRequest.getFreightProfitRatioRemark());
        wsOrder.setOrderRemark(orderRequest.getOrderRemark());
        wsOrder.setPriceRemark(orderRequest.getPriceRemark());
        wsOrder.setCreateTime(new Date());
        wsOrder.setCreatorId(EFConstant.CREATOR_ID);
        wsOrder.setCreatorName(EFConstant.CREATOR);
        wsOrder.setCoopId(wsCoop.getCoopId());

        //设置默认数据
        setOrderDefaultValue(wsOrder);
        //计算预报计费重量  （预报体积 * 1000000 /6000) AS tmpWeight > 预报毛重 ? tmpWeight : 预报毛重
        calculateChargeWeight(wsOrder);

        this.baseMapper.insert(wsOrder);
        //添加-收发货人数据
        addShipperConsignee(wsOrder, orderRequest);
        //添加-日志数据
        wsLogService.addOrderCreateLog(wsOrder);

        WSCreateOrderResponse response = new WSCreateOrderResponse();
        response.setOrderCode(wsOrder.getOrderCode());
        response.setCode(200);
        response.setMessage("操作成功");
        return response;
    }

    private void calculateChargeWeight(WSOrder wsOrder) {
        Double planVolume = wsOrder.getPlanVolume();
        BigDecimal tmpWeight = BigDecimal.valueOf(planVolume * 1000000 /6000);
        if(tmpWeight.compareTo(wsOrder.getPlanWeight()) < 0){
            tmpWeight = wsOrder.getPlanWeight();
        }
        wsOrder.setPlanChargeWeight(tmpWeight.setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue());
    }

    private void setOrderDefaultValue(WSOrder wsOrder) {
        wsOrder.setIncomeStatus("未录收入");
        wsOrder.setCostStatus("未录成本");
        wsOrder.setHawbQuantity(0);
        wsOrder.setBusinessScope("AE");
        wsOrder.setBusinessProduct("代理制");
        wsOrder.setIsConsol(false);
        wsOrder.setGoodsType("普货");
        wsOrder.setBatteryType("不含电池");
        wsOrder.setPackageType("纸箱（CTNS）");
        wsOrder.setPlanDimensions("");
        wsOrder.setCurrecnyCode("CNY");
        wsOrder.setMsrCurrecnyCode("CNY");
        wsOrder.setWarehouseService(false);
        wsOrder.setCustomsClearanceService(false);
        wsOrder.setCustomsStatusCode("普通货物（001）");
        wsOrder.setDeliveryService(false);
        wsOrder.setArrivalCustomsClearanceService(false);
        wsOrder.setPickUpDeliveryService(false);
        wsOrder.setWarehouseService(false);
        wsOrder.setOutfieldService(false);
        wsOrder.setRowUuid(UUID.randomUUID().toString());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public WSInboundOrderResponse inboundOrder(Integer orgId, InboundOrderRequest orderRequest) {
        ValidUtil.valid(orgId, "参数错误：未获取到机构ID");
        ValidUtil.valid(orderRequest, INBOUND_ORDER_VALID_FIELD);
        LocalDate receiptDate = getLocalDate(orderRequest.getReceiptDate());

        WSOrder dbWsOrder = this.baseMapper.getOrderByOrderCode(orgId, orderRequest.getOrderCode());
        if(null == dbWsOrder || AFConstant.ORDER_STATUS.FORCE_CLOSE.equals(dbWsOrder.getOrderStatus())){
            throw new WSException(201021, "订单出重：订单不存在或已强制关闭");
        }
        if(dbWsOrder.getConfirmChargeWeight() != null){
            throw new WSException(201022, "订单出重：订单已出重");
        }
        if(!dbWsOrder.getAwbNumber().equals(orderRequest.getAwbNumber())){
            throw new WSException(201023, "订单出重：订单号与主单号不一致");
        }
        WSWarehouse wsWarehouse = this.baseMapper.getInboundWarehouse(orgId, orderRequest.getWarehouseCode(), dbWsOrder.getDepartureStation());
        if(null == wsWarehouse){
            throw new WSException(201024, "订单出重：未查询到有效货站信息");
        }
        String orderRowId = dbWsOrder.getRowUuid();

        dbWsOrder.setConfirmPieces(orderRequest.getOrderPieces());
        dbWsOrder.setConfirmWeight(orderRequest.getOrderGrossWeight());
        dbWsOrder.setConfirmVolume(orderRequest.getOrderVolume());
        dbWsOrder.setConfirmChargeWeight(orderRequest.getOrderChargeWeight());
        dbWsOrder.setReceiptDate(receiptDate);
        dbWsOrder.setDepartureWarehouseId(wsWarehouse.getWarehouseId());
        dbWsOrder.setEditorId(EFConstant.CREATOR_ID);
        dbWsOrder.setEditorName(EFConstant.CREATOR);
        dbWsOrder.setEditTime(new Date());
        dbWsOrder.setOutfieldService(true);
        dbWsOrder.setOrderStatus("货物出重");
        dbWsOrder.setDepartureWarehouseId(wsWarehouse.getWarehouseId());
        dbWsOrder.setRowUuid(UUID.randomUUID().toString());

        LambdaQueryWrapper<WSOrder> updateWrapper = Wrappers.lambdaQuery();
        updateWrapper.eq(WSOrder::getRowUuid, orderRowId);
        int result = this.baseMapper.update(dbWsOrder, updateWrapper);

        if(result == 0){
            throw new WSException(201025, "订单出重：操作失败");
        }
        WSInbound inbound = new WSInbound();
        inbound.setOrgId(dbWsOrder.getOrgId());
        inbound.setOrderId(dbWsOrder.getOrderId());
        inbound.setOrderUuid(dbWsOrder.getOrderUuid());
        inbound.setOrderChargeWeight(dbWsOrder.getConfirmChargeWeight());
        inbound.setOrderPieces(dbWsOrder.getConfirmPieces());
        inbound.setOrderVolume(dbWsOrder.getConfirmVolume());
        inbound.setOrderChargeWeight(dbWsOrder.getConfirmChargeWeight());
        inbound.setOrderGrossWeight(dbWsOrder.getConfirmChargeWeight());
        inbound.setCreatorName(EFConstant.CREATOR);
        inbound.setCreatorId(EFConstant.CREATOR_ID);
        inbound.setCreateTime(LocalDateTime.now());
        wsInboundMapper.insert(inbound);

        wsLogService.addOrderInboundLog(dbWsOrder);

        WSInboundOrderResponse wsInboundOrderResponse = new WSInboundOrderResponse();
        wsInboundOrderResponse.setCode(200);
        wsInboundOrderResponse.setMessage("操作成功");
        return wsInboundOrderResponse;
    }

    @Override
    public WSListOrderResponse listOrder(Integer orgId, ListOrderRequest orderRequest) {
        ValidUtil.valid(orgId, "参数错误：未获取到机构ID");
        String startDate = orderRequest.getFlightDateStart(),
                endDate = orderRequest.getFlightDateEnd();

        WSListOrderResponse wsListOrderResponse = new WSListOrderResponse();
        wsListOrderResponse.setCode(200);
        wsListOrderResponse.setMessage("操作成功");
        if(StringUtils.isEmpty(startDate) && StringUtils.isEmpty(endDate)){
            wsListOrderResponse.setOrderLists(new ArrayList<>());
            return wsListOrderResponse;
        }

        LambdaQueryWrapper<WSOrder> lambdaQueryWrapper = Wrappers.lambdaQuery();
        lambdaQueryWrapper.select(WSOrder::getOrderCode, WSOrder::getAwbNumber);

        if(StringUtils.isNotEmpty(startDate) && StringUtils.isNotEmpty(endDate)){
            lambdaQueryWrapper.between(WSOrder::getExpectDeparture, startDate, endDate);
        }else if(StringUtils.isNotEmpty(startDate)){
            lambdaQueryWrapper.ge(WSOrder::getExpectDeparture, startDate);
        }else{
            lambdaQueryWrapper.lt(WSOrder::getExpectDeparture, endDate);
        }
        lambdaQueryWrapper.eq(WSOrder::getOrgId, orgId);
        lambdaQueryWrapper.orderByAsc(WSOrder::getOrderCode, WSOrder::getCreateTime);
        List<WSOrder> wsOrderList = this.baseMapper.selectList(lambdaQueryWrapper);

        List<Order> orderLists = new ArrayList<>();
        wsOrderList.stream().forEach((item)->{
            Order orderList = new Order();
            orderList.setAwbNumber(item.getAwbNumber());
            orderList.setOrderCode(item.getOrderCode());
            orderLists.add(orderList);
        });

        wsListOrderResponse.setOrderLists(orderLists);
        return wsListOrderResponse;
    }

    @Override
    public WSDetailOrderResponse detailOrder(Integer orgId, String orderCode) {
        ValidUtil.valid(orgId, "参数错误：未获取到机构ID");
        ValidUtil.valid(orderCode, "参数错误：订单号不能为空");

        WSDetailOrderResponse response = new WSDetailOrderResponse();
        OrderDetail orderDetail = this.baseMapper.getOrderDetail(orgId, orderCode);
        if(null == orderDetail){
            return response;
        }
        Integer orderId = orderDetail.getOrderId();
        List<ShipperLetter> shipperLetterList = this.baseMapper.getOrderShipperLetter(orgId, orderId);
        List<OrderIncome> incomeList = this.baseMapper.getOrderIncome(orgId, orderId);
        List<OrderCost> costList = this.baseMapper.getOrderCost(orgId, orderId);

        response.setOrderDetail(orderDetail);
        response.setShipperLetterList(shipperLetterList);
        response.setOrderIncomeList(incomeList);
        response.setOrderCostList(costList);
        return response;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public WSEditOrderResponse editOrder(Integer orgId, EditOrderRequest orderRequest) {
        ValidUtil.valid(orgId, "参数错误：未获取到机构ID");
        ValidUtil.valid(orderRequest.getOrderCode(), "参数错误：业务单号");
        LocalDate expectDeparture = getLocalDate(orderRequest.getExpectDeparture());

        WSOrder dbWsOrder = this.baseMapper.getOrderByOrderCode(orgId, orderRequest.getOrderCode());
        if(null == dbWsOrder){
            throw new WSException(201031, "订单编辑：订单不存在");
        }
        if(AFConstant.ORDER_STATUS.FORCE_CLOSE.equals(dbWsOrder.getOrderStatus())){
            throw new WSException(201032, "订单编辑：订单强制关闭");
        }
        if(AFConstant.ORDER_STATUS.FINANCIAL_ACCOUNT_LOCK.equals(dbWsOrder.getOrderStatus())){
            throw new WSException(201043, "订单编辑：订单账务锁账");
        }
        //客户单号修改
        updateCoopCode(dbWsOrder, orderRequest);

        //仓库代码修改
        updateStorehouseCode(dbWsOrder, orderRequest);

        //修改始发港
        updateDepartAndArrival(dbWsOrder, orderRequest);

        //修改航班日期
        if(null != expectDeparture){
            dbWsOrder.setExpectDeparture(expectDeparture);
        }
        //修改客服和销售
        updateOrderServiceAndSale(dbWsOrder, orderRequest);

        //修改收发货人
        updateOrderShipperConsignee(dbWsOrder, orderRequest);

        //修改主单号
        updateAwbNumber(dbWsOrder, orderRequest);

        //修改基本信息
        updateOrderBaseInfo(dbWsOrder, orderRequest);

        String srcRowId = dbWsOrder.getRowUuid();
        dbWsOrder.setRowUuid(UUID.randomUUID().toString());
        LambdaQueryWrapper<WSOrder> lambdaQueryWrapper = Wrappers.lambdaQuery();
        lambdaQueryWrapper.eq(WSOrder::getRowUuid, srcRowId);
        int result = this.baseMapper.update(dbWsOrder, lambdaQueryWrapper);

        if(result == 0){
            throw new WSException(201042, "订单编辑：编辑失败");
        }
        wsLogService.addOrderEditLog(dbWsOrder);

        WSEditOrderResponse wsEditOrderResponse = new WSEditOrderResponse();
        wsEditOrderResponse.setOrderCode(dbWsOrder.getOrderCode());
        wsEditOrderResponse.setCode(200);
        wsEditOrderResponse.setMessage("操作成功");
        wsEditOrderResponse.setBusinessCode(result);
        return wsEditOrderResponse;
    }


    private void updateCoopCode(WSOrder dbWsOrder, EditOrderRequest orderRequest) {
        String coopCode = orderRequest.getCoopCode();
        if(StringUtils.isNotEmpty(coopCode)){
            WSCoop wsCoop = this.baseMapper.getCoopByCode(dbWsOrder.getOrgId(), orderRequest.getCoopCode());
            if(null == wsCoop){
                throw new WSException(201033, "订单编辑：客户不存在或未启用");
            }
            dbWsOrder.setCoopId(wsCoop.getCoopId());
        }
    }

    private void updateStorehouseCode(WSOrder dbWsOrder,  EditOrderRequest orderRequest){
        String storehouseCode = orderRequest.getStorehouseCode();
        if(StringUtils.isNotEmpty(storehouseCode)){
            WSWarehouse dbWsWarehouse = this.baseMapper.getWarehouseByCodeAndApCode(dbWsOrder.getOrgId(), orderRequest.getStorehouseCode());
            if(null == dbWsWarehouse){
                throw new WSException(201034, "订单编辑：未查询到有效货站信息");
            }
            dbWsOrder.setDepartureStorehouseId(dbWsWarehouse.getWarehouseId());
        }
    }

    private void updateDepartAndArrival(WSOrder dbWsOrder, EditOrderRequest orderRequest){
        String departCode = orderRequest.getDepartureStation(), arrvalCode = orderRequest.getArrivalStation();
        if(StringUtils.isNotEmpty(departCode)){
            WSAirPort dbDepartureStation = this.baseMapper.getAriPortByApCode(orderRequest.getDepartureStation());
            if(null == dbDepartureStation){
                throw new WSException(201035, "订单编辑：始发港不存在或未启用");
            }
            dbWsOrder.setDepartureStation(dbDepartureStation.getApCode());
        }
        if(StringUtils.isNotEmpty(arrvalCode)){
            WSAirPort dbArrivalStation = this.baseMapper.getAriPortByApCode(orderRequest.getArrivalStation());
            if(null == dbArrivalStation){
                throw new WSException(201036, "订单编辑：目的港不存在或未启用");
            }
            dbWsOrder.setArrivalStation(dbArrivalStation.getApCode());
        }
    }

    private void updateOrderServiceAndSale(WSOrder dbWsOrder, EditOrderRequest orderRequest) {
        String serviceName = orderRequest.getServiceName(), saleName = orderRequest.getSalesName();
        if(StringUtils.isNotEmpty(serviceName)){
            WSUser serviceUser = this.baseMapper.getUserByName(dbWsOrder.getOrgId(), serviceName);
            if(null == serviceUser){
                throw new WSException(201037, "订单编辑：客服不存在或未启用");
            }
            dbWsOrder.setServicerId(serviceUser.getUserId());
            dbWsOrder.setServicerName(serviceUser.buildUserName());
        }
        if(StringUtils.isNotEmpty(saleName)){
            WSUser saleUser = this.baseMapper.getUserByName(dbWsOrder.getOrgId(), saleName);
            if(null == saleUser){
                throw new WSException(201038, "订单编辑：销售不存在或未启用");
            }
            dbWsOrder.setSalesId(saleUser.getUserId());
            dbWsOrder.setSalesName(saleUser.buildUserName());
        }
    }

    private void updateOrderShipperConsignee(WSOrder dbWsOrder, EditOrderRequest orderRequest) {
        String consigneePrintRemark = orderRequest.getConsigneePrintRemark(),
         consignorPrintRemark = orderRequest.getConsignorPrintRemark();
        if(null != consigneePrintRemark){
            WSOrderShipperConsignee consignee = this.wsOrderShipperConsigneeMapper.getConsigneeByOrderId(dbWsOrder.getOrderId());
            if(null == consignee){
                throw new WSException(201039, "订单编辑：收货人信息不存在");
            }
            consignee.setScPrintRemark(consigneePrintRemark);
            consignee.setEditorId(EFConstant.CREATOR_ID);
            consignee.setEditorName(EFConstant.CREATOR);
            consignee.setEditTime(LocalDateTime.now());
            this.wsOrderShipperConsigneeMapper.updateById(consignee);
        }
        if(null != consignorPrintRemark){
            WSOrderShipperConsignee consignor = this.wsOrderShipperConsigneeMapper.getConsignorByOrderId(dbWsOrder.getOrderId());
            if(null == consignor){
                throw new WSException(201040, "订单编辑：发货人信息不存在");
            }
            consignor.setScPrintRemark(consignorPrintRemark);
            consignor.setEditorId(EFConstant.CREATOR_ID);
            consignor.setEditorName(EFConstant.CREATOR);
            consignor.setEditTime(LocalDateTime.now());
            this.wsOrderShipperConsigneeMapper.updateById(consignor);
        }

    }

    private void updateAwbNumber(WSOrder dbWsOrder, EditOrderRequest orderRequest) {
        String awbNumber = orderRequest.getAwbNumber();
        if(StringUtils.isEmpty(awbNumber)){
            return;
        }
        if(dbWsOrder.getAwbNumber().equals(awbNumber)){
            return;
        }

        WSAwbNumber wsAwbNumber = wsAwbNumberMapper.getAwbNumberByAwbNumber(dbWsOrder.getOrgId(), orderRequest.getAwbNumber());
        if(null == wsAwbNumber){
            throw new WSException(201041, "订单创建：主单号不存在或已使用");
        }

        //修改-原始主单号状态
        LambdaQueryWrapper<WSAwbNumber> updateCondition = Wrappers.lambdaQuery();
        updateCondition.eq(WSAwbNumber::getAwbNumber, dbWsOrder.getAwbNumber());
        WSAwbNumber wsAwbNumber1 = new WSAwbNumber();
        wsAwbNumber1.setAwbStatus("未使用");
        wsAwbNumberMapper.update(wsAwbNumber1, updateCondition);

        //修改新主单号状态
        updateAwbNumberStatus(wsAwbNumber);

        //修改订单表中主单号信息
        dbWsOrder.setAwbId(wsAwbNumber.getAwbId());
        dbWsOrder.setAwbUuid(wsAwbNumber.getAwbUuid());
        dbWsOrder.setAwbNumber(wsAwbNumber.getAwbNumber());
    }

    private void updateOrderBaseInfo(WSOrder wsOrder, EditOrderRequest orderRequest){
        //修改体重信息
        Integer planPieces = orderRequest.getPlanPieces();
        BigDecimal planWeight = orderRequest.getPlanWeight();
        Double planVolume = orderRequest.getPlanVolume();
        if(null != planPieces){
            wsOrder.setPlanPieces(planPieces);
        }
        if(null != planWeight){
            wsOrder.setPlanWeight(planWeight);
        }
        if(null != planVolume){
            wsOrder.setPlanVolume(planVolume);
        }
        //计算预报计费重量  （预报体积 * 1000000 /6000) AS tmpWeight > 预报毛重 ? tmpWeight : 预报毛重
        if(null == planVolume){
            planVolume = wsOrder.getPlanVolume();
        }
        if(null == planWeight){
            planWeight = wsOrder.getPlanWeight();
        }
        BigDecimal tmpWeight = BigDecimal.valueOf(planVolume * 1000000 /6000);
        if(tmpWeight.compareTo(planWeight) < 0){
            tmpWeight = planWeight;
        }
        wsOrder.setPlanChargeWeight(tmpWeight.setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue());
        //修改-空运单价
        if(null != orderRequest.getFreightUnitPrice()){
            wsOrder.setFreightUnitprice(orderRequest.getFreightUnitPrice());
        }
        //修改-库内操作备注
        if(null != orderRequest.getOperationRemark()){
            wsOrder.setOperationRemark(orderRequest.getOperationRemark());
        }
        //修改-卖价分泡比例
        if(null != orderRequest.getFreightProfitRatioRemark()){
            wsOrder.setFreightProfitRatioRemark(orderRequest.getFreightProfitRatioRemark());
        }
        //修改-订单备注
        if(null != orderRequest.getOrderRemark()){
            wsOrder.setOrderRemark(orderRequest.getOrderRemark());
        }
        //修改-价格备注
        if(null != orderRequest.getPriceRemark()){
            wsOrder.setPriceRemark(orderRequest.getPriceRemark());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public WSEditInboundOrderResponse editInboundOrder(Integer orgId, EditInboundOrderRequest orderRequest) {
        ValidUtil.valid(orgId, "参数错误：未获取到机构ID");
        ValidUtil.valid(orderRequest, INBOUND_ORDER_VALID_FIELD);
        LocalDate receiptDate = getLocalDate(orderRequest.getReceiptDate());

        WSOrder dbWsOrder = this.baseMapper.getOrderByOrderCode(orgId, orderRequest.getOrderCode());
        if(null == dbWsOrder){
            throw new WSException(201051, "订单出重编辑：订单不存在");
        }
        if(AFConstant.ORDER_STATUS.FORCE_CLOSE.equals(dbWsOrder.getOrderStatus()) || AFConstant.ORDER_STATUS.FINANCIAL_ACCOUNT_LOCK.equals(dbWsOrder.getOrderStatus())){
            throw new WSException(201052, "订单出重编辑：订单状态异常");
        }
        if(null == dbWsOrder.getConfirmChargeWeight()){
            throw new WSException(201053, "订单出重编辑：订单未出重");
        }
        if(!dbWsOrder.getAwbNumber().equals(orderRequest.getAwbNumber())){
            throw new WSException(201054, "订单出重编辑：订单号与主单号不一致");
        }

        WSWarehouse wsWarehouse = this.baseMapper.getInboundWarehouse(orgId, orderRequest.getWarehouseCode(), dbWsOrder.getDepartureStation());
        if(null == wsWarehouse){
            throw new WSException(201055, "订单出重编辑：未查询到有效货站信息");
        }
        List<WSInbound> orderInboundList = this.wsInboundMapper.getByOrderId(dbWsOrder.getOrderId());
        if(orderInboundList.size() == 0 || null == orderInboundList.get(0)){
            throw new WSException(201053, "订单出重编辑：订单未出重");
        }
        if(orderInboundList.size() > 1){
            throw new WSException(201056, "订单出重编辑：多条出重信息");
        }

        String tmpRowId = dbWsOrder.getRowUuid();
        dbWsOrder.setConfirmPieces(orderRequest.getOrderPieces());
        dbWsOrder.setConfirmWeight(orderRequest.getOrderGrossWeight());
        dbWsOrder.setConfirmVolume(orderRequest.getOrderVolume());
        dbWsOrder.setConfirmChargeWeight(orderRequest.getOrderChargeWeight());
        dbWsOrder.setReceiptDate(receiptDate);
        dbWsOrder.setDepartureWarehouseId(wsWarehouse.getWarehouseId());
        dbWsOrder.setEditorId(EFConstant.CREATOR_ID);
        dbWsOrder.setEditorName(EFConstant.CREATOR);
        dbWsOrder.setEditTime(new Date());
        dbWsOrder.setOutfieldService(true);
        dbWsOrder.setDepartureWarehouseId(wsWarehouse.getWarehouseId());
        dbWsOrder.setRowUuid(UUID.randomUUID().toString());

        LambdaQueryWrapper<WSOrder> updateWrapper = Wrappers.lambdaQuery();
        updateWrapper.eq(WSOrder::getRowUuid, tmpRowId);
        int result = this.baseMapper.update(dbWsOrder, updateWrapper);
        if(result == 0){
            throw new WSException(201057, "订单出重编辑：编辑失败");
        }

        WSInbound dbWsInbound = orderInboundList.get(0);
        dbWsInbound.setOrderChargeWeight(dbWsOrder.getConfirmChargeWeight());
        dbWsInbound.setOrderPieces(dbWsOrder.getConfirmPieces());
        dbWsInbound.setOrderVolume(dbWsOrder.getConfirmVolume());
        dbWsInbound.setOrderChargeWeight(dbWsOrder.getConfirmChargeWeight());
        dbWsInbound.setOrderGrossWeight(dbWsOrder.getConfirmChargeWeight());
        dbWsInbound.setEditorId(EFConstant.CREATOR_ID);
        dbWsInbound.setEditorName(EFConstant.CREATOR);
        dbWsInbound.setEditTime(LocalDateTime.now());
        result = this.wsInboundMapper.updateById(dbWsInbound);
        if(result == 0){
            throw new WSException(201058, "订单出重编辑：编辑失败");
        }

        wsLogService.addOrderInboundEditLog(dbWsOrder);

        WSEditInboundOrderResponse wsEditInboundOrderResponse = new WSEditInboundOrderResponse();
        wsEditInboundOrderResponse.setCode(200);
        wsEditInboundOrderResponse.setMessage("操作成功");
        return wsEditInboundOrderResponse;
    }

    private void updateAwbNumberStatus(WSAwbNumber wsAwbNumber) {
        wsAwbNumber.setAwbStatus("已配单");
        LambdaQueryWrapper<WSAwbNumber> lambdaQueryWrapper = Wrappers.lambdaQuery();
        lambdaQueryWrapper.eq(WSAwbNumber::getAwbUuid, wsAwbNumber.getAwbUuid());
        int result = wsAwbNumberMapper.update(wsAwbNumber, lambdaQueryWrapper);
        if(result == 0){
            throw new WSException(201008, "订单创建：主单号已配单");
        }
    }

    private void addShipperConsignee(WSOrder wsOrder, CreateOrderRequest orderRequest) {
        Integer orderId = wsOrder.getOrderId(),
                orgId = wsOrder.getOrgId();

        WSOrderShipperConsignee consignee = new WSOrderShipperConsignee();
        consignee.setOrderId(orderId);
        consignee.setOrgId(orgId);
        consignee.setScType(AFConstant.SHIPPER_CONSIGNEE);
        consignee.setScPrintRemark(orderRequest.getConsigneePrintRemark());
        consignee.setCreatorId(EFConstant.CREATOR_ID);
        consignee.setCreatorName(EFConstant.CREATOR);
        consignee.setCreateTime(LocalDateTime.now());
        wsOrderShipperConsigneeMapper.insert(consignee);

        WSOrderShipperConsignee consignor = new WSOrderShipperConsignee();
        consignor.setOrderId(orderId);
        consignor.setOrgId(orgId);
        consignor.setScType(AFConstant.SHIPPER_CONSIGNOR);
        consignor.setScPrintRemark(orderRequest.getConsignorPrintRemark());
        consignor.setCreatorId(EFConstant.CREATOR_ID);
        consignor.setCreatorName(EFConstant.CREATOR);
        consignor.setCreateTime(LocalDateTime.now());
        wsOrderShipperConsigneeMapper.insert(consignor);
    }

    private LocalDate getLocalDate(String dateStr) {
        if(StringUtils.isEmpty(dateStr)){
            return null;
        }
        LocalDate localDate = null;
        try{
             localDate = LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        }catch (Exception e){ }
        if(null == localDate){
            throw new WSException(101002, "参数错误：格式不正确");
        }
        return localDate;
    }

    private String getOrderCode(Integer orgId, String businessScope){
        String orderCode = this.baseMapper.maxOrderCode(orgId, businessScope);
        return loopBuildValidOrder(orgId,businessScope, orderCode);
    }

    private String loopBuildValidOrder(Integer orgId, String businessScope, String orderCode){
        String tmpOrderCode = buildOrderCodeNo(businessScope, orderCode);
        List<WSOrder> wsOrderList = getOrderByOrgIdAndCode(orgId, tmpOrderCode);
        if(wsOrderList.size() > 0){
            throw new WSException(201010, "订单创建：创建失败");
        }
        return tmpOrderCode;
    }

    private List<WSOrder> getOrderByOrgIdAndCode(Integer orgId, String orderCode){
        LambdaQueryWrapper<WSOrder> lambdaQueryWrapper = Wrappers.lambdaQuery();
        lambdaQueryWrapper.select(WSOrder::getOrderId, WSOrder::getOrderId, WSOrder::getOrderCode);
        lambdaQueryWrapper.eq(WSOrder::getOrgId, orgId);
        lambdaQueryWrapper.eq(WSOrder::getOrderCode, orderCode);
        return this.baseMapper.selectList(lambdaQueryWrapper);
    }

    private String buildOrderCodeNo(String businessScope, String orderCode){
        StringBuilder builder = new StringBuilder();
        if(StringUtils.isEmpty(orderCode)){
            builder.append(businessScope);
            builder.append("-");
            builder.append(LocalDate.now().format(DateTimeFormatter.ofPattern("yyMMdd")));
            builder.append("0001");
        }else{
            String[] showOrderCodeArr = orderCode.split("-");
            String shortOrderCode = showOrderCodeArr[1];
            builder.append(showOrderCodeArr[0]);
            builder.append("-");
            builder.append(shortOrderCode.substring(0, 6));
            Integer serial = Integer.valueOf(shortOrderCode.substring(6)) + 1;
            if(AFConstant.MAX_ORDER_NO.equals(serial)){
                throw new WSException(201009, "订单创建：当日订单数量已到达上限");
            }
            String serialNumber = String.format("%4s", serial.toString()).replace(" ", "0");
            builder.append(serialNumber);
        }
        return builder.toString();
    }

}
