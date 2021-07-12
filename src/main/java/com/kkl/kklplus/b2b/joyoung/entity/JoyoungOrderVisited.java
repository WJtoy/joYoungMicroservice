package com.kkl.kklplus.b2b.joyoung.entity;

import com.kkl.kklplus.entity.b2b.common.B2BBase;
import lombok.Data;

@Data
public class JoyoungOrderVisited extends B2BBase<JoyoungOrderVisited> {

    private String orderNo = "";
    private String visitMan = "";
    private Long visitDate;
    private String remark = "";
}
