package com.kkl.kklplus.b2b.joyoung.controller;

import com.kkl.kklplus.b2b.joyoung.entity.B2BMaterialSearchModel;
import com.kkl.kklplus.b2b.joyoung.entity.JoyoungMaterialApply;
import com.kkl.kklplus.b2b.joyoung.service.JoyoungMaterialApplyService;
import com.kkl.kklplus.common.exception.MSErrorCode;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.b2b.common.B2BProcessFlag;
import com.kkl.kklplus.entity.b2bcenter.sd.B2BOrderTransferResult;
import com.kkl.kklplus.entity.common.MSPage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 *配件单审核结果消息处理
 * @author chenxj
 * @date 2019/08/20
 */
@Slf4j
@RestController
@RequestMapping("materialApply")
public class JoyoungMaterialApplyController {

    @Autowired
    private JoyoungMaterialApplyService materialApplyService;

    /**
     * 更新审核结果消息处理状态
     * @param id
     * @return
     */
    @PostMapping("updateFlag/{id}")
    public MSResponse<String> updateFlag(@PathVariable("id") Long id){
        try {
            materialApplyService.updateProcessFlag(id, B2BProcessFlag.PROCESS_FLAG_SUCESS.value, "");
            return new MSResponse<>(MSErrorCode.SUCCESS);
        }catch (Exception e){
            log.error("JoyoungMaterialApplyController.updateFlag -> {}",e.getMessage());
            return new MSResponse<>(MSErrorCode.FAILURE);
        }
    }

    /**
     * 获取未处理的审核消息列表
     * @param materialSearchModel
     * @return
     */
    @PostMapping("getList")
    public MSResponse<MSPage<JoyoungMaterialApply>> getList(@RequestBody B2BMaterialSearchModel materialSearchModel){
        return materialApplyService.getList(materialSearchModel);
    }
}
