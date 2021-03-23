package com.efreight.prm.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.efreight.prm.entity.writeoff.WriteOffConfirm;
import com.efreight.prm.entity.writeoff.WriteOffInfo;
import com.efreight.prm.entity.writeoff.WriteOffList;
import com.efreight.prm.entity.writeoff.WriteOffQuery;

import java.util.List;

/**
 * @author lc
 * @date 2021/3/11 14:06
 * 核销服务
 */
public interface WriteOffService {

    /**
     * 核销信息
     * @param statementId 账单ID
     * @return
     */
    WriteOffInfo writeOffInfo(Integer statementId);

    /**
     * 确认核销
     * @param params
     * @return
     */
    int writeOffConfirm(WriteOffConfirm params);

    /**
     * 分页查询
     * @param query
     * @return
     */
    IPage<WriteOffList> pageQuery(WriteOffQuery query);

    /**
     * 删除核销单
     * @param rowId
     * @return
     */
    int deleteWriteOff(String rowId, String statementRowUuid);

    /**
     * 查询列表
     * @param query
     * @return
     */
    List<WriteOffList> listQuery(WriteOffQuery query);
}
