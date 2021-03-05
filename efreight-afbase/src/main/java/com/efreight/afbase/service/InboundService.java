package com.efreight.afbase.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.efreight.afbase.entity.Inbound;

import java.util.List;

public interface InboundService extends IService<Inbound> {

    IPage getPage(Page page, Inbound inbound);

    void delete(String number, String flag, String pageName);

    List<Inbound> inboundView(String number, String flag);

    void saveInbound(List<Inbound> data);

    List<Inbound> detailView(String number, String flag);
    List<Inbound> detailView2(String awb_uuid,String order_uuid);
    
    void modifyInbound(Inbound inbound);
}
