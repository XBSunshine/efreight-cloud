package com.efreight.ws.afbase.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.efreight.ws.afbase.entity.WSLog;
import com.efreight.ws.afbase.entity.WSOrder;
import com.efreight.ws.afbase.mapper.WSLogMapper;
import com.efreight.ws.afbase.service.WSLogService;
import com.efreight.ws.common.contant.EFConstant;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

@Service
public class WSLogServiceImpl extends ServiceImpl<WSLogMapper, WSLog> implements WSLogService {

    @Override
    public int addOrderCreateLog(WSOrder wsOrder) {
        return addOrderLog(wsOrder, "AE订单", "订单创建");
    }

    @Override
    public int addOrderInboundLog(WSOrder wsOrder) {
        return addOrderLog(wsOrder, "操作出重", "保存出重");
    }

    @Override
    public int addOrderEditLog(WSOrder wsOrder) {
       return addOrderLog(wsOrder, "AE订单", "订单编辑");
    }

    @Override
    public int addOrderInboundEditLog(WSOrder wsOrder) {
        return addOrderLog(wsOrder, "操作出重", "编辑出重");
    }

    private int addOrderLog(WSOrder wsOrder, String pageName, String pageFunction){
        if(null == wsOrder || StringUtils.isEmpty(pageName) || StringUtils.isEmpty(pageFunction)){
            return 0;
        };
        WSLog wsLog  = new WSLog();
        String orderCode = wsOrder.getOrderCode();
        wsLog.setBusinessScope(orderCode.substring(0, 2));
        wsLog.setOrgId(wsOrder.getOrgId());
        wsLog.setOrderId(wsOrder.getOrderId());
        wsLog.setOrderUuid(wsOrder.getOrderUuid());
        wsLog.setAwbNumber(wsOrder.getAwbNumber());
        wsLog.setAwbUuid(wsOrder.getAwbUuid());
        wsLog.setOrderNumber(wsOrder.getOrderCode());
        wsLog.setCreatorId(EFConstant.CREATOR_ID);
        wsLog.setCreatorName(EFConstant.CREATOR);
        wsLog.setCreatTime(LocalDateTime.now());
        wsLog.setPageName(pageName);
        wsLog.setPageFunction(pageFunction);
        wsLog.setLogRemark("API接口传入数据");
        return this.baseMapper.insert(wsLog);
    }
}
