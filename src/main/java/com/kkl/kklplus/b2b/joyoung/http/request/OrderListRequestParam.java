package com.kkl.kklplus.b2b.joyoung.http.request;

import lombok.Data;

@Data
public class OrderListRequestParam extends RequestParam{
    /**
     * 必填，工单类型(1,已派工单)
     */
    private Integer orderType = 1;

    /**
     * 必填，获取工单数量
     */
    private Integer maxQty = 0;

    /**
     * 必填，获取N天内工单数
     */
    private Integer dates = 0;

    /**
     * 必填，厂商名称
     */
    private String companyName = "";
}

