package com.kkl.kklplus.b2b.joyoung.entity;

import com.kkl.kklplus.entity.b2b.common.B2BBase;
import lombok.Data;

/**
 *咨询单跟踪日志
 * @author chenxj
 * @date 2019/09/25
 */
@Data
public class JoyoungConsultingOrderLog extends B2BBase<JoyoungConsultingOrderLog>{

    /**
     * 九阳咨询单号
     */
    private String consultingNo;
    /**
     * 操作类型 10 普通日志 11 异常日志 20关闭咨询单
     */
    private Integer type;
    /**
     * 跟进内容
     */
    private String content;

}
