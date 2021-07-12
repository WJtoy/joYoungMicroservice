package com.kkl.kklplus.b2b.joyoung.http.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

@Data
public class OrderAppointedRequestParam extends RequestParam{
    /**
     * 必填，工单号
     */
    private String orderNo = "";

    /**
     * 必填，预约人
     */
    private String bookMan = "";

    /**
     * 必填，预约时间(格式：yyyy-MM-dd hh:mm:ss)
     */
    private String bookDate;

    /**
     * 必填，预约备注信息
     */
    private String bookRemark = "";
}
