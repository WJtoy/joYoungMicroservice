package com.kkl.kklplus.b2b.joyoung.entity;

import com.kkl.kklplus.entity.b2b.common.B2BBase;
import lombok.Data;

@Data
public class JoyoungOrderCancelled extends B2BBase<JoyoungOrderCancelled>{
    private String orderNo = "";
    private String cancelMan = "";
    private Long cancelDate;
    private String cancelRemark = "";
}
