package com.efreight.afbase.service;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.efreight.afbase.entity.AfOrder;
import com.efreight.afbase.entity.AiOperationLook;
import com.efreight.afbase.entity.ImportLook;
import com.efreight.afbase.entity.OperationLook;
import org.dom4j.DocumentException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * AI 操作看板
 * </p>
 *
 * @author shihongkai
 * @since 2021-01-12
 */
public interface AiOperationLookService extends IService<AiOperationLook> {
    IPage queryLookList(Page page, AfOrder bean) throws DocumentException;

    Map<String,Object> queryHAWBList(String awbNumber);

    JSONObject distributionDeclare(ImportLook importLook) throws DocumentException;

    JSONObject originalStateDeclare(ImportLook importLook) throws DocumentException, Exception;

    JSONObject tallyStateDeclare(ImportLook importLook) throws DocumentException;
}
