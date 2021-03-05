package com.efreight.afbase.service.impl;

import Common.DateUtil;
import com.alibaba.druid.util.StringUtils;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.efreight.afbase.dao.AiOperationLookMapper;
import com.efreight.afbase.entity.*;
import com.efreight.afbase.entity.shipping.APIType;
import com.efreight.afbase.service.AiOperationLookService;
import com.efreight.afbase.utils.DateUtils;
import com.efreight.afbase.utils.SendUtils;
import com.efreight.common.core.exception.CheckedException;
import com.efreight.common.core.utils.RedisUtil;
import com.efreight.common.security.service.EUserDetails;
import com.efreight.common.security.util.SecurityUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dom4j.*;
import org.dom4j.io.SAXReader;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
* @Description:  AI订单操作看板
* @Param:
* @return:
* @Author: shihongkai
* @Date: 2021/1/12
*/
@Service
@AllArgsConstructor
@Slf4j
public class AiOperationLookServiceImpl extends ServiceImpl<AiOperationLookMapper, AiOperationLook> implements AiOperationLookService{

    private final CacheManager cacheManager;
    private RedisTemplate redisTemplate;
    /**
    * @Description:AI订单操作看板查询
    * @Param:  page, OperationLook
    * @return:  List<AiOperationLook>
    * @Author: shihongkai
    * @Date: 2021/1/12
    */
    @Override
    public IPage queryLookList(Page page, AfOrder bean) throws DocumentException {
        HashMap<String, ImportLook> map = new HashMap<>();
        bean.setOrgId(SecurityUtils.getUser().getOrgId());
        bean.setCurrentUserId(SecurityUtils.getUser().getId());
        bean.setBusinessScope("AI");
        //根据筛选条件分页查询订单
        IPage<AfOrder> afOrderIPage = baseMapper.getListPage(page, bean);
        //判断是查询按钮查询还是翻页查询
        if(page.getCurrent()==1){
            //清空redis   是否清空  暂定   等待实测一站式2.0接口查询效率

            //调用一站式2.0进口看板接口
            EUserDetails user = SecurityUtils.getUser();
            String apiType = APIType.ALL_WORK;
            OrgInterface config = baseMapper.getShippingBillConfig(user.getOrgId(), apiType);
            if (config == null ) {
                throw new CheckedException("没有配置，请联系管理员");
            }
            HashMap<String, String> paramMap=new HashMap<String, String>();
            paramMap.put("forwarder",config.getAppid());
            paramMap.put("mawbCode",bean.getAwbNumber().replace("-",""));

            String flightDateBegin = bean.getFlightDateBegin();
            String flightDateEnd = bean.getFlightDateEnd();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            if(StringUtils.isEmpty(flightDateBegin)&&StringUtils.isEmpty(flightDateEnd)){
                Date firstDay = DateUtils.getBeforeOrAfterDate(new Date(),-3);
                Date lastDay = DateUtils.getBeforeOrAfterDate(new Date(),3);
                flightDateBegin=sdf.format(firstDay);
                flightDateEnd=sdf.format(lastDay);
            }
            paramMap.put("createTimeStart",flightDateBegin);
            paramMap.put("createTimeEnd",flightDateEnd);
            map = doSend(config.getUrlPost()+"IMP_Dashboard",paramMap);
            if (!map.isEmpty()){
                //存入redis缓存
                RedisUtil.putObject("operation_look",map,5, TimeUnit.MINUTES);
            }
        }else{
            //通过redis获取一站式2.0缓存的数据
            map = (HashMap<String, ImportLook>) RedisUtil.get("operation_look");
        }
        HashMap<String,Object> data = new HashMap<>();
        //比较数据库和一站式2.0的缓存/接口数据进行比较
        List<ImportLook> dataList = setData(afOrderIPage.getRecords(), map);
        IPage<ImportLook> importLookPage = new Page<>();
        importLookPage.setTotal(afOrderIPage.getTotal());
        importLookPage.setSize(afOrderIPage.getSize());
        importLookPage.setPages(afOrderIPage.getPages());
        importLookPage.setRecords(dataList);
        importLookPage.setCurrent(afOrderIPage.getCurrent());
        return importLookPage;
    }

    @Override
    public Map<String,Object> queryHAWBList(String awbNumber) {
        Map<String,Object> map = new HashMap<>();
        EUserDetails user = SecurityUtils.getUser();
        String apiType = APIType.ALL_WORK;
        OrgInterface config = baseMapper.getShippingBillConfig(user.getOrgId(), apiType);
        if (config == null ) {
            throw new CheckedException("没有配置，请联系管理员");
        }
        HashMap<String, String> paramMap=new HashMap<String, String>();
        paramMap.put("forwarder",config.getAppid());
        paramMap.put("mawbCode",awbNumber.replace("-",""));
        HashMap<String, ImportLook> awbMap = doSend(config.getUrlPost()+"IMP_DashboardM",paramMap);
        ImportLook awb = new ImportLook();
        if(awbMap!=null){
            for (String keys : awbMap.keySet()){
                awb=awbMap.get(keys);
            }
        }
        String[] split = awbNumber.split("-");
        if(split.length<2){
            awbNumber = awbNumber.substring(0,3)+"-"+awbNumber.substring(3,awbNumber.length());
        }
        //根据主单号查询所有分单
        List<AfOrder> hawbList = baseMapper.getHAWBList(awbNumber);
        HashMap<String, ImportLook> importMap = doSend(config.getUrlPost()+"IMP_Dashboard",paramMap);
        List<ImportLook> dataList = setData(hawbList, importMap);
        //判断map中是否还有数据  如果有数据  就和订单数据做并集
        if(importMap.size()>0){
            for (String keys:importMap.keySet()) {
                String[] s = keys.split("_");
                if(s.length==2){
                    ImportLook importLook = importMap.get(keys);
                    dataList.add(importLook);
                }
            }
        }
        map.put("awb",awb);
        map.put("hawb",dataList);
        return map;
    }

    @Override
    public JSONObject distributionDeclare(ImportLook importLook) throws DocumentException {
        EUserDetails user = SecurityUtils.getUser();
        String apiType = APIType.ALL_WORK;
        OrgInterface config = baseMapper.getShippingBillConfig(user.getOrgId(), apiType);
        if (config == null ) {
            throw new CheckedException("没有配置，请联系管理员");
        }
        String substring = importLook.getMawbCode().substring(0,3);
        String substring1 = importLook.getMawbCode().substring(3, importLook.getMawbCode().length());
        importLook.setMawbCode(substring+"-"+substring1);
        InputStream resourceAsStream = this.getClass().getResourceAsStream("/parameter/DistributionDeclare.xml");
        SAXReader reader = new SAXReader();
        Document document = reader.read(resourceAsStream);
        Element rootElem = document.getRootElement();

        Element messageInfo = rootElem.element("MessageInfo");
        Element manifestInfo = rootElem.element("ManifestInfo");
        Element manifest = rootElem.element("Manifest");
        Element consignment = manifest.element("Declaration").element("Consignment");

        messageInfo.element("Forwarder").setText(config.getAppid());
        messageInfo.element("Handler").setText(user.getUserCname());
        manifestInfo.element("MawbCode").setText(importLook.getMawbCode());
        manifestInfo.element("FlightNo").setText(importLook.getMft1201Flightno());
        manifestInfo.element("FlightDate").setText(importLook.getMft1201Flightdate());
        manifest.element("Head").element("MessageType").setText("MT3202");
        manifest.element("Head").element("Version").setText("1.0");
        consignment.element("ConsignmentPackaging").element("QuantityQuantity").setText(importLook.getTotalPieces());
        consignment.element("TotalGrossMassMeasure").setText(importLook.getTotalWeight());
        consignment.element("ConsignmentItem").element("SequenceNumeric").setText("1");
        consignment.element("ConsignmentItem").element("Commodity").element("CargoDescription").setText(importLook.getGoodEname());

        HashMap<String, String> bodyMap = new HashMap<>();
        bodyMap.put("data",document.asXML());
        System.out.println(document.asXML());
        //调用外部接口查询分单数据
        String objStr = SendUtils.doSend(config.getUrlPost()+"Mft3202_Decleare",bodyMap);
        System.out.println(objStr);
        //解析返回数据
        JSONObject json = JSONObject.parseObject(objStr);
        return json;
    }

    @Override
    public JSONObject originalStateDeclare(ImportLook importLook) throws Exception {
        EUserDetails user = SecurityUtils.getUser();
        String apiType = APIType.ALL_WORK;
        OrgInterface config = baseMapper.getShippingBillConfig(user.getOrgId(), apiType);
        if (config == null ) {
            throw new CheckedException("没有配置，请联系管理员");
        }
        //查询分单信息
        AfOperateOrder afOperateOrderByOrderCode = baseMapper.getAfOperateOrderByOrderCode(importLook);
        //查询收发货人信息
        List<AfOrderShipperConsignee> afOrderShipperConsigneeList = baseMapper.getAfOrderShipperConsignee(afOperateOrderByOrderCode.getOrderId());
        InputStream resourceAsStream = this.getClass().getResourceAsStream("/parameter/OriginalStateDeclare.xml");
        SAXReader reader = new SAXReader();
        Document document = reader.read(resourceAsStream);
        Element rootElem = document.getRootElement();

        Element masterAWBDetails = rootElem.element("MasterAWBDetails");
        Element housewaybillDetails = rootElem.element("ConsolidationList").element("HousewaybillDetails");
        Element shipper = housewaybillDetails.element("Shipper");
        Element consignee = housewaybillDetails.element("Consignee");

        rootElem.element("Forwarder").setText(config.getAppid());
        rootElem.element("handler").setText("");
        masterAWBDetails.element("AWBNumber").setText(afOperateOrderByOrderCode.getAwbNumber());
        housewaybillDetails.element("HWBNumber").setText(afOperateOrderByOrderCode.getHawbNumber());
        housewaybillDetails.element("CustomsStatusCode").setText(afOperateOrderByOrderCode.getGoodsType());
        housewaybillDetails.element("Origin").setText(afOperateOrderByOrderCode.getDepartureStation());
        housewaybillDetails.element("Destination").setText(afOperateOrderByOrderCode.getArrivalStation());
        housewaybillDetails.element("Pieces").setText(afOperateOrderByOrderCode.getPlanPieces()==null?"":String.valueOf(afOperateOrderByOrderCode.getPlanPieces()));
        housewaybillDetails.element("Weight").setText(afOperateOrderByOrderCode.getPlanWeight()==null?"":String.valueOf(afOperateOrderByOrderCode.getPlanWeight()));
        housewaybillDetails.element("VolumeCode").setText("MC");
        housewaybillDetails.element("Volume").setText(afOperateOrderByOrderCode.getPlanVolume()==null?"":String.valueOf(afOperateOrderByOrderCode.getPlanVolume()));
        housewaybillDetails.element("NatureOfGoods").setText(afOperateOrderByOrderCode.getGoodsNameEn());
        housewaybillDetails.element("NatureOfGoodsCN").setText(afOperateOrderByOrderCode.getGoodsNameCn());
        housewaybillDetails.element("CargoInfo").element("BusinessType").setText(afOperateOrderByOrderCode.getBusinessType());
        housewaybillDetails.element("ChargeDeclarations").element("WeightPPCC").setText("PP");
        for (int i = 0; i < afOrderShipperConsigneeList.size(); i++) {
            AfOrderShipperConsignee aosc = afOrderShipperConsigneeList.get(i);
            if(aosc.getScType()==0){
                shipper.element("Name").setText(aosc.getScName());
                shipper.element("Address").setText(aosc.getScAddress());
                shipper.element("CountryCode").setText(aosc.getNationCode());
                shipper.element("Tel").setText(aosc.getTelNumber());
            }else if(aosc.getScType()==1){
                consignee.element("Name").setText(aosc.getScName());
                consignee.element("Address").setText(aosc.getScAddress());
                consignee.element("CountryCode").setText(aosc.getNationCode());
                consignee.element("Tel").setText(aosc.getTelNumber());
            }
        }
        HashMap<String, String> bodyMap = new HashMap<>();
        bodyMap.put("data",document.asXML());
        System.out.println(document.asXML());
        //调用外部接口查询分单数据
        String objStr = SendUtils.doSend(config.getUrlPost()+"Mft1201_Decleare",bodyMap);
        System.out.println(objStr);
        //解析返回数据
        JSONObject json = JSONObject.parseObject(objStr);
        return json;
    }

    @Override
    public JSONObject tallyStateDeclare(ImportLook importLook) throws DocumentException {
        EUserDetails user = SecurityUtils.getUser();
        String apiType = APIType.ALL_WORK;
        OrgInterface config = baseMapper.getShippingBillConfig(user.getOrgId(), apiType);
        if (config == null ) {
            throw new CheckedException("没有配置，请联系管理员");
        }

        //查询分单信息
        AfOperateOrder afOperateOrderByOrderCode = baseMapper.getAfOperateOrderByOrderCode(importLook);
        InputStream resourceAsStream = this.getClass().getResourceAsStream("/parameter/TallyStateDeclare.xml");
        SAXReader reader = new SAXReader();
        Document document = reader.read(resourceAsStream);
        Element rootElem = document.getRootElement();

        Element messageInfo = rootElem.element("MessageInfo");
        Element manifestInfo = rootElem.element("ManifestInfo");
        Element manifest = rootElem.element("Manifest");
        Element head = manifest.element("Head");
        Element declaration = manifest.element("Declaration");
        Element borderTransportMeans = declaration.element("BorderTransportMeans");
        Element consignment = declaration.element("Consignment");

        messageInfo.element("Forwarder").setText(config.getAppid());
        messageInfo.element("Handler").setText(user.getUserCname());
        manifestInfo.element("MawbCode").setText(afOperateOrderByOrderCode.getAwbNumber());
        manifestInfo.element("HawbCode").setText(afOperateOrderByOrderCode.getHawbNumber());
        manifestInfo.element("FlightNo").setText(afOperateOrderByOrderCode.getExpectFlight());
        manifestInfo.element("FlightDate").setText(afOperateOrderByOrderCode.getExpectArrival()==null?"":afOperateOrderByOrderCode.getExpectArrival().toString());
        head.element("MessageType").setText("MT5201");
        head.element("Version").setText("1.0");
        borderTransportMeans.element("TypeCode").setText("4");
        consignment.element("ConsignmentPackaging").element("QuantityQuantity").setText(afOperateOrderByOrderCode.getPlanPieces()==null?"0":String.valueOf(afOperateOrderByOrderCode.getPlanPieces()));
        consignment.element("TotalGrossMassMeasure").setText(afOperateOrderByOrderCode.getPlanWeight()==null?"0":afOperateOrderByOrderCode.getPlanWeight().toString());

        HashMap<String, String> bodyMap = new HashMap<>();
        bodyMap.put("data",document.asXML());
        //调用外部接口查询分单数据
        String objStr = SendUtils.doSend(config.getUrlPost()+"Mft5201_Decleare",bodyMap);
        System.out.println(objStr);
        //解析返回数据
        JSONObject json = JSONObject.parseObject(objStr);
        return json;
    }

    public List<ImportLook> setData(List<AfOrder> list,Map<String,ImportLook> map){
        List<ImportLook> dataList = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            AfOrder afOrder = list.get(i);
            ImportLook il = new ImportLook();
            il.setOrderId(afOrder.getOrderId());
            il.setOrderCode(afOrder.getOrderCode());
            il.setMawbCode(afOrder.getAwbNumber());
            il.setHawbCode(afOrder.getHawbNumber());
            il.setVolume(afOrder.getPlanVolume()!=null?afOrder.getPlanVolume().toString():null);
            il.setPieces(afOrder.getPlanPieces()!=null?afOrder.getPlanPieces().toString():null);
            il.setWeight(afOrder.getPlanWeight()!=null?afOrder.getPlanWeight().toString():null);
            il.setGoodEname(afOrder.getGoodsNameEn());
            il.setDeparture(afOrder.getDepartureStation());
            il.setDestination(afOrder.getArrivalStation());
            ImportLook importLook =map==null?null:map.get(afOrder.getAwbNumber().replace("-","")+"_"+afOrder.getHawbNumber());
            if(importLook!=null){
                il.setMft1201Status(importLook.getMft1201Status());
                il.setMft5201Status(importLook.getMft5201Status());
                il.setMft6202Recv(map.get(afOrder.getAwbNumber().replace("-","")+"_").getMft6202Recv());
                il.setMft3202Status(map.get(afOrder.getAwbNumber().replace("-","")+"_").getMft3202Status());
                il.setExistsMft5201(importLook.getExistsMft5201());
                il.setExistsMft1201(importLook.getExistsMft1201());
                il.setExistsMft3202(importLook.getExistsMft3202());
                //比较件数是否一样
                if((afOrder.getPlanPieces()==null&&!StringUtils.isEmpty(importLook.getPieces()))||(!afOrder.getPlanPieces().equals(importLook.getPieces())&&!StringUtils.isEmpty(importLook.getPieces()))){
                    il.setPieces(importLook.getPieces());
                }
                //比较重量和预计毛重是否一样
                if((afOrder.getPlanWeight()==null&&!StringUtils.isEmpty(importLook.getWeight()))||(!afOrder.getPlanWeight().equals(importLook.getWeight())&&!StringUtils.isEmpty(importLook.getWeight()))){
                    il.setWeight(importLook.getWeight());
                }
                //比较英文品名是否一样
                if((StringUtils.isEmpty(afOrder.getGoodsNameEn())&&!StringUtils.isEmpty(importLook.getGoodEname()))||(!afOrder.getGoodsNameEn().equals(importLook.getGoodEname())&&!StringUtils.isEmpty(importLook.getGoodEname()))){
                    il.setGoodEname(importLook.getGoodEname());
                }
                //比较始发地是否一样
                if((StringUtils.isEmpty(afOrder.getDepartureStation())&&!StringUtils.isEmpty(importLook.getDeparture()))||(!afOrder.getDepartureStation().equals(importLook.getDeparture())&&!StringUtils.isEmpty(importLook.getDeparture()))){
                    il.setDeparture(importLook.getDeparture());
                }
                //比较目的地是否一样
                if((StringUtils.isEmpty(afOrder.getArrivalStation())&&!StringUtils.isEmpty(importLook.getDestination()))||(!afOrder.getArrivalStation().equals(importLook.getDestination())&&!StringUtils.isEmpty(importLook.getDestination()))){
                    il.setDestination(importLook.getDestination());
                }
                //数据比较完成就根据key删除map中的数据
                map.remove(afOrder.getAwbNumber().replace("-","")+"_"+afOrder.getHawbNumber());
            }
            dataList.add(il);
        }
        return dataList;
    }

    public HashMap<String,ImportLook> doSend(String url,HashMap<String, String> paramMap){
        //调用外部接口查询分单数据
        String objStr = SendUtils.doSend(url,paramMap);
        //解析返回数据
        JSONObject json = JSONObject.parseObject(objStr);
        String code  = json.getString("code");
        HashMap<String,ImportLook> importMap = new HashMap<>();
        if ("01".equals(code)) {
            List list = JSON.parseObject(json.getJSONObject("data").getString("records"), List.class);
            for (int i = 0; i < list.size(); i++) {
                JSONObject json1 = JSONObject.parseObject(list.get(i).toString());
                ImportLook importLook = JSON.toJavaObject(json1, ImportLook.class);
                importMap.put(importLook.getMawbCode()+"_"+importLook.getHawbCode(),importLook);
            }
        }
        return importMap;
    }
}
