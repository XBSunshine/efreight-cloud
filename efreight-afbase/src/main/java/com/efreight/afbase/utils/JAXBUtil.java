package com.efreight.afbase.utils;

import org.springframework.util.Assert;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.StringReader;
import java.io.StringWriter;

/**
 * xml To bean
 * bean to xml
 */
public class JAXBUtil {

    /**
     * 将对象转换为XMl格式
     * @param entity
     * @return
     * @throws Exception
     */
    public static String beanToXML(Object entity) throws Exception {
        if(null == entity){return null;}

        // 获取JAXB的上下文环境，需要传入具体的 Java bean -> 这里使用Student
        JAXBContext context = JAXBContext.newInstance(entity.getClass());
        // 创建 Marshaller 实例
        Marshaller marshaller = context.createMarshaller();
        // 设置转换参数 -> 这里举例是告诉序列化器是否格式化输出
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        //删除xml头信息
        marshaller.setProperty(Marshaller.JAXB_FRAGMENT, true);
        // 构建输出环境 -> 这里使用标准输出，输出到控制台Console
        StringWriter stringWriter = new StringWriter();
        // 将所需对象序列化 -> 该方法没有返回值
        marshaller.marshal(entity, stringWriter);
        return stringWriter.toString();
    }

    /**
     * 将XMl字符串，转换为实体对象
     * @param xml 字符串
     * @param entityClass 实体Class对象
     * @param <T>
     * @return
     * @throws JAXBException
     */
    public static<T> T xmlToBean(String xml, Class<T> entityClass) throws JAXBException {
        Assert.hasLength(xml, "[xml]非法参数!");
        Assert.notNull(entityClass, "[entityClass]非法参数!");

        // 获取JAXB的上下文环境，需要传入具体的 Java bean -> 这里使用Student
        JAXBContext context = JAXBContext.newInstance(entityClass);
        // 创建 UnMarshaller 实例
        Unmarshaller unmarshaller = context.createUnmarshaller();
        StringReader stringReader = new StringReader(xml);
        // 加载需要转换的XML数据 -> 这里使用InputStream，还可以使用File，Reader等
        return (T) unmarshaller.unmarshal(stringReader);
    }

}
