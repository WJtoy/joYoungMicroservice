package com.kkl.kklplus.b2b.joyoung.entity;

import com.kkl.kklplus.entity.b2b.common.B2BBase;
import lombok.Data;

@Data
public class FailedProcessLog extends B2BBase<FailedProcessLog> {
    private String interfaceName;
    private String infoJson;
    private String resultJson;
}
