package com.kkl.kklplus.b2b.joyoung.entity;

import com.kkl.kklplus.entity.b2b.common.B2BBase;
import lombok.Data;

/**
 *咨询单
 * @author chenxj
 * @date 2019/09/25
 */
@Data
public class JoyoungConsultingOrder extends B2BBase<JoyoungConsultingOrder>{

    /**
     * 九阳咨询单号
     */
    private String consultingNo;

    /**
     * 九阳单号
     */
    private String orderNo;

    /**
     * 咨询类型0催单1投诉单
     */
    private Integer consultingType;

    /**
     * 咨询内容
     */
    private String content;

    /**
     * 快可立咨询单ID
     */
    private Long kklConsultingId;

    private Long kklOrderId;
}
