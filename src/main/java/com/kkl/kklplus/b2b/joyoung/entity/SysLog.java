package com.kkl.kklplus.b2b.joyoung.entity;

import com.kkl.kklplus.entity.common.MSBase;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SysLog extends MSBase<SysLog>{

    /**
     * 日志类型
     */
    private Integer type;
    /**
     * 用户代理
     */
    private String userAgent;
    /**
     * 请求URI
     */
    private String requestUri;
    /**
     * 操作方式,如POST,GET
     */
    private String method;
    /**
     * 操作提交的数据
     */
    private String params;
    /**
     * 异常信息
     */
    private String exception;

    private String title;
    /**
     * 分片标志
     */
    private String quarter;

    private Long createDt;

}
