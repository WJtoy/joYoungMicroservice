package com.kkl.kklplus.b2b.joyoung.entity;

import com.kkl.kklplus.entity.b2b.common.B2BBase;
import lombok.Data;

@Data
public class JoyoungOrderConfirmed extends B2BBase<JoyoungOrderConfirmed>{

    /**
     * 0为成功， 1表示接口异常或失败
     */
    private Integer resultFlag;
    /**
     * 工单号
     */
    private String orderNo = "";
    /**
     * 是否成功接收了此工单(2成功;1失败)
     */
    private Integer thirdSendFlag;
    /**
     * 接收工单备注信息
     */
    private String thirdSendMessage = "";
    /**
     * 外部单号
     */
    private String orderId;
}
