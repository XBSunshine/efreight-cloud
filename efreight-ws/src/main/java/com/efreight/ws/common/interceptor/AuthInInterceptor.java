package com.efreight.ws.common.interceptor;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.efreight.ws.common.annotation.EFWSAuthorize;
import com.efreight.ws.common.contant.EFConstant;
import com.efreight.ws.common.pojo.WSException;
import com.efreight.ws.hrs.entity.WSAPIConfig;
import com.efreight.ws.hrs.mapper.WSAPIConfigMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.headers.Header;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.message.Exchange;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;
import org.apache.cxf.service.Service;
import org.apache.cxf.service.invoker.MethodDispatcher;
import org.apache.cxf.service.model.BindingOperationInfo;
import org.springframework.stereotype.Component;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.annotation.Resource;
import javax.xml.namespace.QName;
import java.lang.reflect.Method;
import java.util.List;

@Slf4j
@Component
public class AuthInInterceptor extends AbstractPhaseInterceptor<SoapMessage> {

    @Resource
    private WSAPIConfigMapper wsapiConfigMapper;

    public AuthInInterceptor() {
        super(Phase.USER_LOGICAL);
    }

    @Override
    public void handleMessage(SoapMessage soapMessage) throws Fault {
        String token = getAuthToken(soapMessage),
                permission = getPermission(soapMessage);

        if(StringUtils.isEmpty(permission)){
            throw WSException.noDefinePermission();
        }
        List<WSAPIConfig> configList = wsapiConfigMapper.getByApiTypeAndAuth(token, permission);
        if(configList.size() > 1){
            throw WSException.multiAuth();
        }
        if(configList.size() == 0 || null == configList.get(0)){
            throw WSException.noPermission();
        }

        WSAPIConfig wsapiConfig = configList.get(0);
        Integer orgId = wsapiConfig.getOrgId();
        soapMessage.put(EFConstant.KEY_ORG_ID, orgId);
    }

    private String getPermission(SoapMessage soapMessage){
        Method actionMethod = getActionMethod(soapMessage);
        if(actionMethod.isAnnotationPresent(EFWSAuthorize.class)){
            EFWSAuthorize efwsAuthorize = actionMethod.getAnnotation(EFWSAuthorize.class);
            return efwsAuthorize.value();
        }
        return "";
    }

    private Method getActionMethod(SoapMessage soapMessage){
        Exchange exchange = soapMessage.getExchange();
        BindingOperationInfo bop = exchange.get(BindingOperationInfo.class);
        MethodDispatcher md = (MethodDispatcher) exchange.get(Service.class)
                .get(MethodDispatcher.class.getName());
        Method method = md.getMethod(bop);
        return method;
    }

    private String getAuthToken(SoapMessage soapMessage){
        Header header = soapMessage.getHeader(new QName(EFConstant.KEY_WS_AUTH));
        if(null == header){
            throw WSException.noAuthor();
        }
        Element ele = (Element) header.getObject();
        if(null == ele){
            throw WSException.noAuthor();
        }
        Node node = ele.getFirstChild();
        if(null == node){
            throw WSException.noAuthor();
        }
        String token = node.getTextContent();
        if(null == token || token.isEmpty()){
            throw WSException.noAuthor();
        }
        return token;
    }
}
