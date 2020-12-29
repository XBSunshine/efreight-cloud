package com.efreight.afbase.service;

import java.util.List;

import com.efreight.afbase.entity.AfOrder;
import com.efreight.afbase.entity.AfShipperLetter;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * AF 订单管理 出口订单 托书信息 服务类
 * </p>
 *
 * @author qipm
 * @since 2019-10-10
 */
public interface AfShipperLetterService extends IService<AfShipperLetter> {
	
	List<AfShipperLetter> getListPage(AfShipperLetter bean);
	Boolean doSave(AfShipperLetter bean);
	Boolean doUpdate(AfShipperLetter bean);
	Boolean doDelete(AfShipperLetter bean);

    Boolean saveAiShippers(AfShipperLetter bean);
}
