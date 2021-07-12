package com.kkl.kklplus.b2b.joyoung.controller;

import com.kkl.kklplus.b2b.joyoung.entity.JoyoungConsultingOrder;
import com.kkl.kklplus.b2b.joyoung.entity.JoyoungConsultingOrderLog;
import com.kkl.kklplus.b2b.joyoung.entity.JoyoungMaterialApply;
import com.kkl.kklplus.b2b.joyoung.entity.JoyoungMaterialDeliver;
import com.kkl.kklplus.b2b.joyoung.service.JoyoungConsultingOrderLogService;
import com.kkl.kklplus.b2b.joyoung.service.JoyoungConsultingOrderService;
import com.kkl.kklplus.b2b.joyoung.service.JoyoungMaterialApplyService;
import com.kkl.kklplus.b2b.joyoung.service.JoyoungMaterialDeliverService;
import com.kkl.kklplus.common.exception.MSErrorCode;
import com.kkl.kklplus.common.response.MSResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;

/**
 *九阳推送消息控制层
 * @author chenxj
 * @date 2019/08/19
 */
@Slf4j
@RestController
@RequestMapping("/push")
public class JoyoungPushController {

    @Autowired
    private JoyoungMaterialApplyService materialApplyService;

    @Autowired
    private JoyoungMaterialDeliverService materialDeliverService;

    @Autowired
    private JoyoungConsultingOrderService consultingOrderService;

    @Autowired
    private JoyoungConsultingOrderLogService consultingOrderLogService;

    @PostMapping("/applyMaterial")
    public MSResponse applyMaterial(@RequestBody JoyoungMaterialApply materialApply){
        MSResponse msResponse = materialApplyService.validationData(materialApply);
        if(msResponse.getCode() != MSErrorCode.CODE_VALUE_SUCCESS){
            log.error("applyMaterial->报错实体:{}",materialApply != null ? materialApply.toString() : "");
            return msResponse;
        }
        return materialApplyService.processApply(materialApply);
    }

    @PostMapping("/materialDeliver")
    public MSResponse materialDeliver(@RequestBody JoyoungMaterialDeliver materialDeliver){
        MSResponse msResponse = materialDeliverService.validationData(materialDeliver);
        if(msResponse.getCode() != MSErrorCode.CODE_VALUE_SUCCESS){
            log.error("materialDeliver->报错实体:{}",materialDeliver != null ? materialDeliver.toString() : "");
            return msResponse;
        }
        return materialDeliverService.processDeliver(materialDeliver);
    }

    @PostMapping("/newConsultingOrder")
    public MSResponse newConsultingOrder(@RequestBody JoyoungConsultingOrder consultingOrder){
        MSResponse msResponse = consultingOrderService.validationData(consultingOrder);
        if(msResponse.getCode() != MSErrorCode.CODE_VALUE_SUCCESS){
            log.error("newConsultingOrder->报错实体:{}",consultingOrder != null ? consultingOrder.toString() : "");
            return msResponse;
        }
        return consultingOrderService.newConsultingOrder(consultingOrder);
    }

    @PostMapping("/consultingLog")
    public MSResponse consultingLog(@RequestBody JoyoungConsultingOrderLog consultingOrderLog){
        MSResponse msResponse = consultingOrderLogService.validationData(consultingOrderLog);
        if(msResponse.getCode() != MSErrorCode.CODE_VALUE_SUCCESS){
            log.error("consultingLog->报错实体:{}",consultingOrderLog != null ? consultingOrderLog.toString() : "");
            return msResponse;
        }
        return consultingOrderLogService.consultingLog(consultingOrderLog);
    }
}
