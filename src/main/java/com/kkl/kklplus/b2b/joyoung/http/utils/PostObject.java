package com.kkl.kklplus.b2b.joyoung.http.utils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import lombok.Data;
import org.dom4j.Element;

/**
 * PostObject对象,执行请求返回数据
 *
 */
@Data
public class PostObject implements Serializable{
    private static final long serialVersionUID = -6368908435514325803L;
    public static String SOAP1_1="1.1";
    public static String SOAP1_2="1.2";

    /**
     * SoapAction: TargetNamespace/Operation
     */
    private String soapAction;
    /**
     * SoapVersion
     */
    private String soapVersion=SOAP1_1;
    /**
     * Operation:调用WebService的方法
     */
    private String operation;
    private String targetNamespace;
    private String content;
    private Map<String,String> parameter= new LinkedHashMap<String,String>();

    /**
     * 把参数组装成SOAP消息
     */
    public void buildContent(){
        if(this.soapVersion.equals(SOAP1_1)){
            this.content = processParamSOAP11();
        }else if(this.soapVersion.equals(SOAP1_2)){
            this.content = processParamSOAP12();
        }else{
            this.content = processParamSOAP11();
        }
    }

    /**
     * 构建SOAP1.1 Content
     * @return
     */
    private String processParamSOAP11(){
        StringBuffer sb = new StringBuffer();
        sb.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>");
        sb.append("<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:exp=\""+this.targetNamespace+"\">");
        sb.append("<soapenv:Header/>");
        sb.append("<soapenv:Body>");
        sb.append("<exp:"+this.operation+">");
        for(Map.Entry<String, String> entry: this.parameter.entrySet()) {
            sb.append("<exp:"+entry.getKey()+">" + entry.getValue() + "</exp:"+entry.getKey()+">");
        }
        sb.append("</exp:"+this.operation+">");
        sb.append("</soapenv:Body>");
        sb.append("</soapenv:Envelope>");
        return sb.toString();
    }
    /**
     * 构建SOAP1.2 Content
     * @return
     */
    private String processParamSOAP12(){
        StringBuffer sb = new StringBuffer();//
        sb.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>");
        sb.append("<soapenv:Envelope xmlns:soapenv=\"http://www.w3.org/2003/05/soap-envelope/\" xmlns:exp=\""+this.targetNamespace+"\">");
        sb.append("<soapenv:Header/>");
        sb.append("<soapenv:Body>");
        sb.append("<exp:"+this.operation+">");
        for(Map.Entry<String, String> entry: this.parameter.entrySet()) {
            sb.append("<exp:"+entry.getKey()+">" + entry.getValue() + "</exp:"+entry.getKey()+">");
        }
        sb.append("</exp:"+this.operation+">");
        sb.append("</soapenv:Body>");
        sb.append("</soapenv:Envelope>");
        return sb.toString();
    }

    /**
     * 获取数据
     * @param element
     * @return
     */
    @SuppressWarnings("unchecked")
    public List<String> getElementList(Element element){
        List<String> result = new ArrayList<String>();
        for (Iterator<Element> iterator1 = element.elementIterator(); iterator1.hasNext();) {
            Element temp = iterator1.next();
            Iterator<Element> iterator2 = temp.elementIterator();
            //判断是否存在子元素
            if(!iterator2.hasNext()){
                result.add(temp.getStringValue());
            }else{
                StringBuffer one = new StringBuffer();
                int count = 0;
                //对象类：属性名+值
                while (iterator2.hasNext()) {
                    Element e = iterator2.next();
                    if(count == 0){
                        count ++;
                        one.append("\""+e.getName()+"\":\"");
                        one.append(e.getStringValue());
                        one.append("\"");
                    }else{
                        count ++;
                        one.append(",\""+e.getName()+"\":\"");
                        one.append(e.getStringValue());
                        one.append("\"");
                    }
                }
                result.add("{"+one.toString()+"}");
            }
        }
        return result;
    }

}