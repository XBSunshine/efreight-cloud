package com.efreight.afbase.utils;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

import com.sun.org.apache.xpath.internal.XPathAPI;

import lombok.extern.slf4j.Slf4j;

import javax.xml.parsers.DocumentBuilderFactory;

import java.io.StringReader;

/**
 * xml To bean
 * bean to xml
 */
@Slf4j
public class XmlApiUtils {

	public static Document parseXML(String xmlStr, boolean namespaceaware)
			throws Exception {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(namespaceaware);
		factory.setIgnoringComments(true);
		factory.setIgnoringElementContentWhitespace(true);
		StringReader reader = new StringReader(xmlStr);
		InputSource is = new InputSource(reader);
		Document doc = null;
		doc = factory.newDocumentBuilder().parse(is);
		return doc;
	}
	public static String getNodeText(Node doc, String xpath) {
		String result = "";
		try {
			result = XPathAPI.selectSingleNode(doc, xpath).getTextContent();
			if (result != null) {
				result = result.trim();
				if ("/n".equals(result))
					result = "";
				if ("/r".equals(result))
					result = "";
				if ("/r/n".equals(result))
					result = "";
				if ("null".equalsIgnoreCase(result)) {
					result = "";
				}
			}
		} catch (Exception e) {
//			log.info(e.getMessage());
		}
		return result;
	}

}
