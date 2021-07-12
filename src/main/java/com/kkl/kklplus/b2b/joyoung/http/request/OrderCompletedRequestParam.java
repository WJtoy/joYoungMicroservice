package com.kkl.kklplus.b2b.joyoung.http.request;

import com.google.common.collect.Lists;
import com.kkl.kklplus.entity.joyoung.sd.JoyoungOrderCompleted;
import lombok.Data;

import java.util.List;

@Data
public class OrderCompletedRequestParam extends RequestParam{

    /**
     * 必填，工单号
     */
    private String orderNo = "";

    /**
     * 可选，完工备注
     */
    private String finishNote;
    /**
     * 正价师傅上门时间
     */
    private Long completesdate;

    /**
     * 必填，工单产品明细集合
     */
    private List<JoyoungOrderCompleted.ProductDetail> items = Lists.newArrayList();
    /**
     * 是否电商好评(0否1是) 默认否
     */
    private Integer isPraise = 0;
    /**
     * 好评图片1
     */
    private String praisePhoto1;
    /**
     * 好评图片2
     */
    private String praisePhoto2;

}
