package com.kkl.kklplus.b2b.joyoung.controller;

import com.kkl.kklplus.b2b.joyoung.entity.JoyoungConsultingOrder;
import com.kkl.kklplus.b2b.joyoung.entity.JoyoungConsultingOrderProcess;
import com.kkl.kklplus.b2b.joyoung.service.JoyoungConsultingOrderProcessService;
import com.kkl.kklplus.b2b.joyoung.service.JoyoungConsultingOrderService;
import com.kkl.kklplus.common.exception.MSErrorCode;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.b2b.common.B2BProcessFlag;
import com.kkl.kklplus.entity.b2bcenter.sd.B2BOrderComplainProcess;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 *咨询单
 * @author chenxj
 * @date 2019/09/29
 */
@Slf4j
@RestController
@RequestMapping("/consultingOrder")
public class JoyoungConsultingOrderController {

    @Autowired
    private JoyoungConsultingOrderProcessService consultingOrderProcessService;

    @Autowired
    private JoyoungConsultingOrderService consultingOrderService;


    @PostMapping("process")
    public MSResponse process(@RequestBody JoyoungConsultingOrderProcess process){
        return consultingOrderProcessService.process(process);
    }

    /**
     * 更新审核结果消息处理状态
     * @param id
     * @return
     */
    @PostMapping("updateFlag/{id}/{kklConsultingId}")
    public MSResponse<String> updateFlag(@PathVariable("id") Long id,
                                         @PathVariable("kklConsultingId")Long kklConsultingId){
        try {
            consultingOrderService.updateProcessFlag
                    (id, kklConsultingId,B2BProcessFlag.PROCESS_FLAG_SUCESS.value, "");
            return new MSResponse<>(MSErrorCode.SUCCESS);
        }catch (Exception e){
            log.error("consultingOrder.updateFlag -> {}",e.getMessage());
            return new MSResponse<>(MSErrorCode.FAILURE);
        }
    }

}
