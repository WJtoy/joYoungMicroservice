package com.kkl.kklplus.b2b.joyoung.controller;


import com.kkl.kklplus.b2b.joyoung.service.*;
import com.kkl.kklplus.common.exception.MSErrorCode;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.joyoung.sd.JoyoungOrderProcessLog;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 九阳工单处理日志（跟踪日志）
 */
@Slf4j
@RestController
@RequestMapping("/joyoungOrderProcesslog")
public class JoyoungOrderProcesslogController {

    @Autowired
    private JoyoungOrderProcesslogService joyoungOrderProcesslogService;

    @RequestMapping("/saveOrderProcesslog")
    public MSResponse saveOrderProcesslog(@RequestBody JoyoungOrderProcessLog orderProcessLog){
        MSResponse msResponse = new MSResponse();
        if(orderProcessLog == null){
            msResponse.setErrorCode(MSErrorCode.FAILURE);
            msResponse.setMsg("信息不能为空！");
            return msResponse;
        }
        //暂无日志类型，默认跟进
        orderProcessLog.setLogType("跟进");
        return joyoungOrderProcesslogService.saveOrderProcesslog(orderProcessLog);
    }
}
