package com.kkl.kklplus.b2b.joyoung.controller;

import com.google.gson.Gson;
import com.kkl.kklplus.b2b.joyoung.http.command.OperationCommand;
import com.kkl.kklplus.b2b.joyoung.http.request.OrderCompletedRequestParam;
import com.kkl.kklplus.b2b.joyoung.http.response.ResponseBody;
import com.kkl.kklplus.b2b.joyoung.http.response.ResponseData;
import com.kkl.kklplus.b2b.joyoung.http.utils.OkHttpUtils;
import com.kkl.kklplus.b2b.joyoung.service.B2BProcesslogService;
import com.kkl.kklplus.b2b.joyoung.service.JoyoungFailedProcessLogService;
import com.kkl.kklplus.b2b.joyoung.service.JoyoungOrderCompletedService;
import com.kkl.kklplus.b2b.joyoung.service.SysLogService;
import com.kkl.kklplus.common.exception.MSErrorCode;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.b2b.common.B2BProcessFlag;
import com.kkl.kklplus.entity.b2bcenter.rpt.B2BOrderProcesslog;
import com.kkl.kklplus.entity.joyoung.sd.JoyoungOrderCompleted;
import com.kkl.kklplus.utils.QuarterUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/joyoungOrderCompleted")
public class JoyoungOrderCompletedController {

    @Autowired
    private B2BProcesslogService b2BProcesslogService;

    @Autowired
    private SysLogService sysLogService;

    @Autowired
    private JoyoungOrderCompletedService joyoungOrderCompletedService;

    @Autowired
    private JoyoungFailedProcessLogService failedProcessLogService;

    @RequestMapping("/completed")
    public MSResponse orderCompleted(@RequestBody JoyoungOrderCompleted joyoungOrderCompleted){
        MSResponse msResponse = new MSResponse();
        if(joyoungOrderCompleted == null){
            msResponse.setErrorCode(MSErrorCode.FAILURE);
            msResponse.setMsg("信息不能为空！");
            return msResponse;
        }
        Gson gson = new Gson();
        msResponse.setErrorCode(MSErrorCode.SUCCESS);
        B2BOrderProcesslog b2BProcesslog = new B2BOrderProcesslog();
        b2BProcesslog.setInterfaceName(OperationCommand.OperationCode.COMPLETED.serviceCode);
        b2BProcesslog.setProcessFlag(B2BProcessFlag.PROCESS_FLAG_FAILURE.value);
        b2BProcesslog.setProcessTime(0);
        b2BProcesslog.setCreateById(joyoungOrderCompleted.getCreateById());
        b2BProcesslog.setUpdateById(joyoungOrderCompleted.getUpdateById());
        b2BProcesslog.preInsert();
        b2BProcesslog.setQuarter(QuarterUtils.getQuarter(b2BProcesslog.getCreateDate()));
        OrderCompletedRequestParam reqBody = new OrderCompletedRequestParam();
        reqBody.setOrderNo(joyoungOrderCompleted.getOrderNo());
        reqBody.setFinishNote(joyoungOrderCompleted.getFinishNote());
        reqBody.setItems(joyoungOrderCompleted.getItems());
        Integer isPraise = joyoungOrderCompleted.getIsPraise();
        if(isPraise == null){
            joyoungOrderCompleted.setIsPraise(0);
            reqBody.setIsPraise(0);
        }else {
            reqBody.setIsPraise(isPraise);
        }
        String praisePhoto1 = joyoungOrderCompleted.getPraisePhoto1();
        String praisePhoto2 = joyoungOrderCompleted.getPraisePhoto2();
        reqBody.setPraisePhoto1(praisePhoto1);
        reqBody.setPraisePhoto2(praisePhoto2);
        reqBody.setPraisePhoto1(StringUtils.trimToEmpty(praisePhoto1));
        reqBody.setPraisePhoto2(StringUtils.trimToEmpty(praisePhoto2));
        Long appCompleteDate = joyoungOrderCompleted.getAppCompleteDate();
        if(appCompleteDate == null || appCompleteDate == 0){
            appCompleteDate = System.currentTimeMillis();
            joyoungOrderCompleted.setAppCompleteDate(appCompleteDate);
        }
        reqBody.setCompletesdate(appCompleteDate);
        OperationCommand command = OperationCommand.newInstance(OperationCommand.OperationCode.COMPLETED, reqBody);
        ResponseBody<ResponseData> resBody = OkHttpUtils.postSyncGenericNew(command, ResponseData.class);
        String infoJson = gson.toJson(reqBody);
        b2BProcesslog.setInfoJson(infoJson);
        try {
            b2BProcesslogService.insert(b2BProcesslog);
            b2BProcesslog.setResultJson(resBody.getOriginalJson());
            joyoungOrderCompletedService.insert(joyoungOrderCompleted);
            if( resBody.getErrorCode() != ResponseBody.ErrorCode.SUCCESS.code){
                String errorMsg = StringUtils.left(resBody.getErrorMsg(),200);
                if(resBody.getErrorCode() >= ResponseBody.ErrorCode.REQUEST_INVOCATION_FAILURE.code){
                    msResponse.setErrorCode(new MSErrorCode(resBody.getErrorCode(),errorMsg));
                }
                b2BProcesslog.setProcessFlag(B2BProcessFlag.PROCESS_FLAG_FAILURE.value);
                b2BProcesslog.setProcessComment(errorMsg);
                msResponse.setThirdPartyErrorCode(new MSErrorCode(resBody.getErrorCode(),errorMsg));
                //记录错误日志
                failedProcessLogService.insertOrUpdateFailedLog
                        (b2BProcesslog,joyoungOrderCompleted.getUniqueId(),joyoungOrderCompleted.getB2bOrderId());
                joyoungOrderCompleted.setProcessFlag(B2BProcessFlag.PROCESS_FLAG_FAILURE.value);
                joyoungOrderCompleted.setProcessComment(errorMsg);
                joyoungOrderCompletedService.updateProcessFlag(joyoungOrderCompleted);
                b2BProcesslogService.updateProcessFlag(b2BProcesslog);
                return msResponse;
            }else{
                ResponseData responseData = resBody.getData();
                if(responseData != null && responseData.getResultFlag() == ResponseBody.ErrorCode.SUCCESS.code) {
                    joyoungOrderCompleted.setProcessFlag(B2BProcessFlag.PROCESS_FLAG_SUCESS.value);
                    joyoungOrderCompletedService.updateProcessFlag(joyoungOrderCompleted);
                    b2BProcesslog.setProcessFlag(B2BProcessFlag.PROCESS_FLAG_SUCESS.value);
                    b2BProcesslogService.updateProcessFlag(b2BProcesslog);
                }else{
                    msResponse.setThirdPartyErrorCode(new MSErrorCode(responseData.getResultFlag(),responseData.getResultMsg()));
                    b2BProcesslog.setProcessFlag(B2BProcessFlag.PROCESS_FLAG_FAILURE.value);
                    b2BProcesslog.setProcessComment(responseData == null?"":responseData.getResultMsg());
                    joyoungOrderCompleted.setProcessFlag(B2BProcessFlag.PROCESS_FLAG_FAILURE.value);
                    joyoungOrderCompleted.setProcessComment(responseData == null?"":responseData.getResultMsg());
                    //记录错误日志
                    failedProcessLogService.insertOrUpdateFailedLog
                            (b2BProcesslog,joyoungOrderCompleted.getUniqueId(),joyoungOrderCompleted.getB2bOrderId());
                    joyoungOrderCompletedService.updateProcessFlag(joyoungOrderCompleted);
                    b2BProcesslogService.updateProcessFlag(b2BProcesslog);
                }
                return msResponse;
            }
        }catch (Exception e) {
            msResponse.setErrorCode(MSErrorCode.FAILURE);
            msResponse.setMsg(StringUtils.left(e.getMessage(),200));
            b2BProcesslog.setProcessFlag(B2BProcessFlag.PROCESS_FLAG_FAILURE.value);
            b2BProcesslog.setProcessComment(StringUtils.left(e.getMessage(),200));
            failedProcessLogService.insertOrUpdateFailedLog
                    (b2BProcesslog,joyoungOrderCompleted.getUniqueId(),joyoungOrderCompleted.getB2bOrderId());
            b2BProcesslogService.updateProcessFlag(b2BProcesslog);
            String errorStr = "工单完工失败，原因是：";
            log.error(errorStr, e.getMessage());
            sysLogService.insert(1L,infoJson,errorStr + e.getMessage(),
                    errorStr, OperationCommand.OperationCode.COMPLETED.serviceCode, "POST");
            return msResponse;
        }
    }
}
