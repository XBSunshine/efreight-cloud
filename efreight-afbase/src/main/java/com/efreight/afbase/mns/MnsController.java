package com.efreight.afbase.mns;

import lombok.extern.slf4j.Slf4j;
import java.math.BigDecimal;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;
import org.w3c.dom.Document;

import com.alibaba.druid.util.StringUtils;
import com.efreight.afbase.entity.AfOrderStorageMns;
import com.efreight.afbase.service.impl.AfOrderStorageMnsServiceImpl;
import com.efreight.afbase.utils.SpringUtil;
import com.efreight.afbase.utils.XmlApiUtils;
import Queue.MessageHandler;



@Slf4j
@RunWith(SpringRunner.class)
public class MnsController implements MessageHandler{

	private AfOrderStorageMnsServiceImpl service = SpringUtil.getBean(AfOrderStorageMnsServiceImpl.class);
    @Override
	public boolean processMessage(String arg0) {
		try {
			log.info("MNS开始请求");
//			log.info(arg0);
			arg0 = arg0.replace("&", "");

			Document doc = XmlApiUtils.parseXML(arg0,false);
				AfOrderStorageMns bean=new AfOrderStorageMns();
				bean.setMawbcode(XmlApiUtils.getNodeText(doc, "//MawbCode"));
				bean.setForwarder(XmlApiUtils.getNodeText(doc, "//Forwarder"));
				bean.setFilecontent(XmlApiUtils.getNodeText(doc, "//FileContent"));
				bean.setMsgsource(XmlApiUtils.getNodeText(doc, "//MsgSource"));
				bean.setReceiver(XmlApiUtils.getNodeText(doc, "//Receiver"));
				bean.setSender(XmlApiUtils.getNodeText(doc, "//Sender"));
				bean.setMsgid(XmlApiUtils.getNodeText(doc, "//Msgid"));
				bean.setSmi(XmlApiUtils.getNodeText(doc, "//Smi"));
				bean.setFsutype(XmlApiUtils.getNodeText(doc, "//FsuType"));
				bean.setAwb(XmlApiUtils.getNodeText(doc, "//Awb"));
				bean.setDep(XmlApiUtils.getNodeText(doc, "//Dep"));
				bean.setArr(XmlApiUtils.getNodeText(doc, "//Arr"));
				bean.setAwbpcs(XmlApiUtils.getNodeText(doc, "//AwbPcs"));
				bean.setAwbgwt(XmlApiUtils.getNodeText(doc, "//AwbGwt"));
				if (!StringUtils.isEmpty(XmlApiUtils.getNodeText(doc, "//Pcs"))) {
					bean.setPcs(Integer.parseInt(XmlApiUtils.getNodeText(doc, "//Pcs")));
				}
				if (!StringUtils.isEmpty(XmlApiUtils.getNodeText(doc, "//Gwt"))) {
					bean.setGwt(new BigDecimal(XmlApiUtils.getNodeText(doc, "//Gwt")));
				}
				bean.setUld(XmlApiUtils.getNodeText(doc, "//Uld"));
				bean.setFlightno(XmlApiUtils.getNodeText(doc, "//FlightNo"));
				bean.setFlightdate(XmlApiUtils.getNodeText(doc, "//FlightDate"));
				bean.setOccurplace(XmlApiUtils.getNodeText(doc, "//OccurPlace"));
				bean.setOccurtime(XmlApiUtils.getNodeText(doc, "//OccurTime"));
				bean.setNkgagentcode(XmlApiUtils.getNodeText(doc, "//NkgAgentCode"));
				bean.setCreatedate(XmlApiUtils.getNodeText(doc, "//CreateDate"));
				service.doSave(bean);
			return true;
		} catch (Exception e) {
			log.info(e.getMessage());
			return false;
		}
	}
}

