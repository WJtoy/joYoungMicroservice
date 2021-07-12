package com.kkl.kklplus.b2b.joyoung.http.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

@Data
public class OrderCancelledRequestParam extends RequestParam{

    /**
     * 必填，工单号
     */
    private String orderNo = "";

    /**
     * 必填，取消人
     */
    private String cancelMan = "";

    /**
     * 必填，取消时间(格式：yyyy-MM-dd hh:mm:ss)
     */
    private String cancelDate;

    /**
     * 必填，取消原因
     */
    private String cancelRemark = "";
}
