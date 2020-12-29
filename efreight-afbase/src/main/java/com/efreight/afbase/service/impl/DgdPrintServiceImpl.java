package com.efreight.afbase.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.efreight.afbase.entity.AfOrder;
import com.efreight.afbase.entity.DgdPrint;
import com.efreight.afbase.dao.DgdPrintMapper;
import com.efreight.afbase.entity.DgdPrintList;
import com.efreight.afbase.entity.Inbound;
import com.efreight.afbase.entity.procedure.AfPAwbPrintForMawbPrintProcedure;
import com.efreight.afbase.service.AfOrderService;
import com.efreight.afbase.service.DgdPrintListService;
import com.efreight.afbase.service.DgdPrintService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.efreight.afbase.utils.PDFUtils;
import com.efreight.common.security.util.SecurityUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * AF 出口订单 DGD 制单 服务实现类
 * </p>
 *
 * @author xiaobo
 * @since 2020-01-14
 */
@Service
@AllArgsConstructor
@Slf4j
public class DgdPrintServiceImpl extends ServiceImpl<DgdPrintMapper, DgdPrint> implements DgdPrintService {

    private final DgdPrintListService dgdPrintListService;

    private final AfOrderService afOrderService;

    @Override
    public List<DgdPrint> getList(String orderUuid) {
        LambdaQueryWrapper<DgdPrint> wrapper = Wrappers.<DgdPrint>lambdaQuery();
        wrapper.eq(DgdPrint::getOrderUuid, orderUuid).eq(DgdPrint::getOrgId, SecurityUtils.getUser().getOrgId()).orderByAsc(DgdPrint::getDgdPrintId);
        List<DgdPrint> dgdPrints = baseMapper.selectList(wrapper);
        HashMap<String, Integer> indexMap = new HashMap<>();
        indexMap.put("index", 1);
        dgdPrints.stream().forEach(dgdPrint -> {
            LambdaQueryWrapper<DgdPrintList> listWrapper = Wrappers.<DgdPrintList>lambdaQuery();
            listWrapper.eq(DgdPrintList::getOrgId, SecurityUtils.getUser().getOrgId()).eq(DgdPrintList::getDgdPrintId, dgdPrint.getDgdPrintId());
            List<DgdPrintList> list = dgdPrintListService.list(listWrapper);
            if (list.size() < 5) {
                int size = list.size();
                for (int i = 0; i < 5 - size; i++) {
                    list.add(new DgdPrintList());
                }
            }
            dgdPrint.setDgdPrintList(list.subList(0, 5));
            dgdPrint.setDgdPrintName("DGD-" + indexMap.get("index"));
            indexMap.put("index", indexMap.get("index") + 1);
        });

        return dgdPrints;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)

    public void saveDgdPrint(DgdPrint dgdPrint) {
        log.info("开始保存");

        //保存dgdPrint
        dgdPrint.setOrgId(SecurityUtils.getUser().getOrgId());
        LocalDateTime now = LocalDateTime.now();
        dgdPrint.setCreatorName(SecurityUtils.getUser().getUserCname() + " " + SecurityUtils.getUser().getUserEmail());
        dgdPrint.setCreatorId(SecurityUtils.getUser().getId());
        dgdPrint.setCreateTime(now);

        dgdPrint.setEditorName(SecurityUtils.getUser().getUserCname() + " " + SecurityUtils.getUser().getUserEmail());
        dgdPrint.setEditorId(SecurityUtils.getUser().getId());
        dgdPrint.setEditTime(now);
        baseMapper.insert(dgdPrint);

        //保存dgdPrintList
        HashMap<String, Integer> indexMap = new HashMap<>();
        indexMap.put("index", 1);
        dgdPrint.getDgdPrintList().stream().forEach(dgdPrintList -> {
            dgdPrintList.setOrgId(SecurityUtils.getUser().getOrgId());
            dgdPrintList.setDgdPrintId(dgdPrint.getDgdPrintId());
            dgdPrintList.setNo(indexMap.get("index"));
            indexMap.put("index", indexMap.get("index") + 1);
        });

        dgdPrintListService.saveBatch(dgdPrint.getDgdPrintList());
        log.info("保存成功");

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateDgdPrint(DgdPrint dgdPrint) {
        log.info("开始修改");
        //修改dgdPrint
        LocalDateTime now = LocalDateTime.now();
        dgdPrint.setEditorName(SecurityUtils.getUser().getUserCname() + " " + SecurityUtils.getUser().getUserEmail());
        dgdPrint.setEditorId(SecurityUtils.getUser().getId());
        dgdPrint.setEditTime(now);
        baseMapper.updateById(dgdPrint);


        //修改dgdPrintList

        //1.删除
        LambdaQueryWrapper<DgdPrintList> listWrapper = Wrappers.<DgdPrintList>lambdaQuery();
        listWrapper.eq(DgdPrintList::getDgdPrintId, dgdPrint.getDgdPrintId()).eq(DgdPrintList::getOrgId, SecurityUtils.getUser().getOrgId());
        dgdPrintListService.remove(listWrapper);

        //2.保存
        HashMap<String, Integer> indexMap = new HashMap<>();
        indexMap.put("index", 1);
        dgdPrint.getDgdPrintList().stream().forEach(dgdPrintList -> {
            dgdPrintList.setOrgId(SecurityUtils.getUser().getOrgId());
            dgdPrintList.setDgdPrintId(dgdPrint.getDgdPrintId());
            dgdPrintList.setNo(indexMap.get("index"));
            indexMap.put("index", indexMap.get("index") + 1);
        });

        dgdPrintListService.saveBatch(dgdPrint.getDgdPrintList());
        log.info("修改成功");
    }

    @Override
    public DgdPrint view(Integer dgdPrintId) {
        LambdaQueryWrapper<DgdPrintList> listWrapper = Wrappers.<DgdPrintList>lambdaQuery();
        listWrapper.eq(DgdPrintList::getDgdPrintId, dgdPrintId).eq(DgdPrintList::getOrgId, SecurityUtils.getUser().getOrgId());
        List<DgdPrintList> list = dgdPrintListService.list(listWrapper);
        if (list.size() < 5) {
            int size = list.size();
            for (int i = 0; i < 5 - size; i++) {
                list.add(new DgdPrintList());
            }
        }
        DgdPrint dgdPrint = baseMapper.selectById(dgdPrintId);
        dgdPrint.setDgdPrintList(list.subList(0, 5));
        return dgdPrint;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Integer dgdPrintId) {
        baseMapper.deleteById(dgdPrintId);

        LambdaQueryWrapper<DgdPrintList> listWrapper = Wrappers.<DgdPrintList>lambdaQuery();
        listWrapper.eq(DgdPrintList::getDgdPrintId, dgdPrintId).eq(DgdPrintList::getOrgId, SecurityUtils.getUser().getOrgId());
        dgdPrintListService.remove(listWrapper);
    }

    @Override
    public String printG(Integer dgdPrintId) {
        DgdPrint dgdPrint = this.view(dgdPrintId);
        LambdaQueryWrapper<AfOrder> wrapper = Wrappers.<AfOrder>lambdaQuery();
        wrapper.eq(AfOrder::getOrgId, SecurityUtils.getUser().getOrgId()).eq(AfOrder::getOrderId, dgdPrint.getOrderId());
        AfOrder afOrder = afOrderService.getOne(wrapper);
        dgdPrint.setAwbNumber(afOrder.getAwbNumber());
        dgdPrint.setDgdPrintName(afOrder.getOrderCode());
        String templateFilePath = PDFUtils.filePath + "/PDFtemplate/AF_DGD_FORMAT.pdf";
        return fillTemplate(dgdPrint, templateFilePath, PDFUtils.filePath + "/PDFtemplate/temp/dgdMake", PDFUtils.filePath);
    }

    @Override
    public String printT(Integer dgdPrintId) {
        DgdPrint dgdPrint = this.view(dgdPrintId);
        LambdaQueryWrapper<AfOrder> wrapper = Wrappers.<AfOrder>lambdaQuery();
        wrapper.eq(AfOrder::getOrgId, SecurityUtils.getUser().getOrgId()).eq(AfOrder::getOrderId, dgdPrint.getOrderId());
        AfOrder afOrder = afOrderService.getOne(wrapper);
        dgdPrint.setAwbNumber(afOrder.getAwbNumber());
        dgdPrint.setDgdPrintName(afOrder.getOrderCode());
        String templateFilePath = PDFUtils.filePath + "/PDFtemplate/AF_DGD_PRINT.pdf";
        return fillTemplate(dgdPrint, templateFilePath, PDFUtils.filePath + "/PDFtemplate/temp/dgdMake", PDFUtils.filePath);
    }

    public static String fillTemplate(DgdPrint dgdPrint, String templateFilePath, String savePath, String replacePath) {
        String saveFilename = PDFUtils.makeFileName(dgdPrint.getDgdPrintName() + "_" + dgdPrint.getDgdPrintId() + ".pdf");
        //得到文件的保存目录
        String newPDFPath = PDFUtils.makePath(saveFilename, savePath) + "/" + saveFilename;

        try {

            Map<String, String> valueData = new HashMap<>();
            //月份全部大写

            if (dgdPrint != null) {
                valueData.put("Input01", StrUtil.isBlank(dgdPrint.getShipperPrint()) ? "" : dgdPrint.getShipperPrint());
                valueData.put("Input02", StrUtil.isBlank(dgdPrint.getConsigneePrint()) ? "" : dgdPrint.getConsigneePrint());
                valueData.put("Input03", StrUtil.isBlank(dgdPrint.getAwbNumber()) ? "" : dgdPrint.getAwbNumber());
                valueData.put("Input04", StrUtil.isBlank(dgdPrint.getPageNumber()) ? "" : dgdPrint.getPageNumber());
                valueData.put("Input05", StrUtil.isBlank(dgdPrint.getPagesNumber()) ? "" : dgdPrint.getPagesNumber());
                valueData.put("Input06", StrUtil.isBlank(dgdPrint.getDepartureStationPrint()) ? "" : dgdPrint.getDepartureStationPrint());
                valueData.put("Input07", "");
                valueData.put("Input08", "");

                valueData.put("Input09", dgdPrint.getAircraftType() == 0 ? "" : "XXXXXXXXXXX\nXXXXXXXXXXX");
                valueData.put("Input10", dgdPrint.getAircraftType() == 1 ? "" : "XXXXXXXXXXX\nXXXXXXXXXXX");
                valueData.put("Input11", StrUtil.isBlank(dgdPrint.getArrivalStationPrint()) ? "" : dgdPrint.getArrivalStationPrint());
                valueData.put("Input12", dgdPrint.getShipmentType() == 0 ? "" : "XXXXXXXXXXXXXX");
                valueData.put("Input13", dgdPrint.getShipmentType() == 1 ? "" : "XXXXXXXXXXXXXX");
                valueData.put("Input14", StrUtil.isBlank(dgdPrint.getHandlingInfo()) ? "" : dgdPrint.getHandlingInfo());
                valueData.put("Input15", StrUtil.isBlank(dgdPrint.getNameTitleOfSignatory()) ? "" : dgdPrint.getNameTitleOfSignatory());
                valueData.put("Input16", StrUtil.isBlank(dgdPrint.getPlaceAndDate()) ? "" : dgdPrint.getPlaceAndDate());

                HashMap<String, Integer> indexMap = new HashMap<>();
                indexMap.put("index", 1);
                dgdPrint.getDgdPrintList().stream().forEach(dgdPrintList -> {
                    valueData.put("InputR_" + indexMap.get("index") + "1", StrUtil.isBlank(dgdPrintList.getUnIdNo()) ? "" : dgdPrintList.getUnIdNo());
                    valueData.put("InputR_" + indexMap.get("index") + "2", StrUtil.isBlank(dgdPrintList.getProperShippingName()) ? "" : dgdPrintList.getProperShippingName());
                    valueData.put("InputR_" + indexMap.get("index") + "3", StrUtil.isBlank(dgdPrintList.getClassOrDivision()) ? "" : dgdPrintList.getClassOrDivision());
                    valueData.put("InputR_" + indexMap.get("index") + "4", StrUtil.isBlank(dgdPrintList.getPackingGroup()) ? "" : dgdPrintList.getPackingGroup());
                    valueData.put("InputR_" + indexMap.get("index") + "5", StrUtil.isBlank(dgdPrintList.getQuantityAndTypeOfPacking()) ? "" : dgdPrintList.getQuantityAndTypeOfPacking());
                    valueData.put("InputR_" + indexMap.get("index") + "6", StrUtil.isBlank(dgdPrintList.getPackingInst()) ? "" : dgdPrintList.getPackingInst());
                    valueData.put("InputR_" + indexMap.get("index") + "7", StrUtil.isBlank(dgdPrintList.getAuthorization()) ? "" : dgdPrintList.getAuthorization());
                    indexMap.put("index", indexMap.get("index") + 1);
                });
            }

            //pdf填充数据以及下载
            PDFUtils.loadPDF(templateFilePath, newPDFPath, valueData);

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Exception : " + e.getMessage());
        }
        return newPDFPath.replace(replacePath, "");
    }
}
