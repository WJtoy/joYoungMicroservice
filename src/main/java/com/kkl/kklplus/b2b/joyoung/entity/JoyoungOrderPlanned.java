package com.kkl.kklplus.b2b.joyoung.entity;

import com.kkl.kklplus.entity.b2b.common.B2BBase;
import lombok.Data;

@Data
public class JoyoungOrderPlanned extends B2BBase<JoyoungOrderPlanned> {

    private String orderNo = "";
    private String engineerName = "";
    private String engineerMobile = "";
}
