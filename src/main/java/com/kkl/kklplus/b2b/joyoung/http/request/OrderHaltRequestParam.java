package com.kkl.kklplus.b2b.joyoung.http.request;

import lombok.Data;

@Data
public class OrderHaltRequestParam extends RequestParam{
    /**
     * 必填，工单号
     */
    private String orderNo = "";

    /**
     * 必填，挂起类型 1挂起2异常处理
     */
    private Integer type;

    /**
     * 必填，操作时间
     */
    private String operatorDate = "";

    /**
     * 必填，预约时间(格式：yyyy-MM-dd hh:mm:ss)
     */
    private String bookDate;

    /**
     * 备注信息
     */
    private String remark = "";
}
