package com.kkl.kklplus.b2b.joyoung.controller;


import com.google.gson.Gson;
import com.kkl.kklplus.b2b.joyoung.http.command.OperationCommand;
import com.kkl.kklplus.b2b.joyoung.http.request.OrderPlannedRequestParam;
import com.kkl.kklplus.b2b.joyoung.http.response.ResponseBody;
import com.kkl.kklplus.b2b.joyoung.http.response.ResponseData;
import com.kkl.kklplus.b2b.joyoung.http.utils.OkHttpUtils;
import com.kkl.kklplus.b2b.joyoung.service.B2BProcesslogService;
import com.kkl.kklplus.b2b.joyoung.service.JoyoungFailedProcessLogService;
import com.kkl.kklplus.b2b.joyoung.service.JoyoungOrderPlannedService;
import com.kkl.kklplus.b2b.joyoung.service.SysLogService;
import com.kkl.kklplus.common.exception.MSErrorCode;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.b2b.common.B2BProcessFlag;
import com.kkl.kklplus.entity.b2bcenter.rpt.B2BOrderProcesslog;
import com.kkl.kklplus.entity.joyoung.sd.JoyoungOrderPlanned;
import com.kkl.kklplus.utils.QuarterUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/joyoungOrderPlanned")
public class JoyoungOrderPlannedController {

    @Autowired
    private B2BProcesslogService b2BProcesslogService;

    @Autowired
    private SysLogService sysLogService;

    @Autowired
    private JoyoungOrderPlannedService joyoungOrderPlannedService;

    @Autowired
    private JoyoungFailedProcessLogService failedProcessLogService;

    @RequestMapping("/planned")
    public MSResponse orderPlanned(@RequestBody JoyoungOrderPlanned joyoungOrderPlanned){
        MSResponse msResponse = new MSResponse();
        if(joyoungOrderPlanned == null){
            msResponse.setErrorCode(MSErrorCode.FAILURE);
            msResponse.setMsg("信息不能为空！");
            return msResponse;
        }
        Gson gson = new Gson();
        msResponse.setErrorCode(MSErrorCode.SUCCESS);
        B2BOrderProcesslog b2BProcesslog = new B2BOrderProcesslog();
        b2BProcesslog.setInterfaceName(OperationCommand.OperationCode.PLANNED.serviceCode);
        b2BProcesslog.setProcessFlag(B2BProcessFlag.PROCESS_FLAG_FAILURE.value);
        b2BProcesslog.setProcessTime(0);
        b2BProcesslog.setCreateById(joyoungOrderPlanned.getCreateById());
        b2BProcesslog.setUpdateById(joyoungOrderPlanned.getUpdateById());
        b2BProcesslog.preInsert();
        b2BProcesslog.setQuarter(QuarterUtils.getQuarter(b2BProcesslog.getCreateDate()));
        OrderPlannedRequestParam reqBody = new OrderPlannedRequestParam();
        reqBody.setOrderNo(joyoungOrderPlanned.getOrderNo());
        reqBody.setEngineerName(joyoungOrderPlanned.getEngineerName());
        reqBody.setEngineerMobile(joyoungOrderPlanned.getEngineerMobile());
        OperationCommand command = OperationCommand.newInstance(OperationCommand.OperationCode.PLANNED, reqBody);
        ResponseBody<ResponseData> resBody = OkHttpUtils.postSyncGenericNew(command, ResponseData.class);
        String infoJson = gson.toJson(reqBody);
        b2BProcesslog.setInfoJson(infoJson);
        try {
            b2BProcesslogService.insert(b2BProcesslog);
            b2BProcesslog.setResultJson(resBody.getOriginalJson());
            joyoungOrderPlannedService.insert(joyoungOrderPlanned);
            if( resBody.getErrorCode() != ResponseBody.ErrorCode.SUCCESS.code){
                String errorMsg = StringUtils.left(resBody.getErrorMsg(),200);
                if(resBody.getErrorCode() >= ResponseBody.ErrorCode.REQUEST_INVOCATION_FAILURE.code){
                    msResponse.setErrorCode(new MSErrorCode(resBody.getErrorCode(),errorMsg));
                }
                msResponse.setThirdPartyErrorCode(new MSErrorCode(resBody.getErrorCode(),errorMsg));
                joyoungOrderPlanned.setProcessFlag(B2BProcessFlag.PROCESS_FLAG_FAILURE.value);
                joyoungOrderPlanned.setProcessComment(errorMsg);
                b2BProcesslog.setProcessFlag(B2BProcessFlag.PROCESS_FLAG_FAILURE.value);
                b2BProcesslog.setProcessComment(errorMsg);
                //记录错误日志
                failedProcessLogService.insertOrUpdateFailedLog
                        (b2BProcesslog,joyoungOrderPlanned.getUniqueId(),joyoungOrderPlanned.getB2bOrderId());
                joyoungOrderPlannedService.updateProcessFlag(joyoungOrderPlanned);

                b2BProcesslogService.updateProcessFlag(b2BProcesslog);
                return msResponse;
            }else{
                ResponseData responseData = resBody.getData();
                if(responseData != null && responseData.getResultFlag() == ResponseBody.ErrorCode.SUCCESS.code) {
                    joyoungOrderPlanned.setProcessFlag(B2BProcessFlag.PROCESS_FLAG_SUCESS.value);
                    joyoungOrderPlannedService.updateProcessFlag(joyoungOrderPlanned);
                    b2BProcesslog.setProcessFlag(B2BProcessFlag.PROCESS_FLAG_SUCESS.value);
                    b2BProcesslogService.updateProcessFlag(b2BProcesslog);
                }else{
                    msResponse.setThirdPartyErrorCode(new MSErrorCode(responseData.getResultFlag(),responseData.getResultMsg()));
                    joyoungOrderPlanned.setProcessFlag(B2BProcessFlag.PROCESS_FLAG_FAILURE.value);
                    joyoungOrderPlanned.setProcessComment(responseData == null?"":responseData.getResultMsg());
                    b2BProcesslog.setProcessFlag(B2BProcessFlag.PROCESS_FLAG_FAILURE.value);
                    b2BProcesslog.setProcessComment(responseData == null?"":responseData.getResultMsg());
                    //记录错误日志
                    failedProcessLogService.insertOrUpdateFailedLog
                            (b2BProcesslog,joyoungOrderPlanned.getUniqueId(),joyoungOrderPlanned.getB2bOrderId());
                    joyoungOrderPlannedService.updateProcessFlag(joyoungOrderPlanned);
                    b2BProcesslogService.updateProcessFlag(b2BProcesslog);
                }
                return msResponse;
            }
        }catch (Exception e) {
            msResponse.setErrorCode(MSErrorCode.FAILURE);
            msResponse.setMsg(StringUtils.left(e.getMessage(),200));
            b2BProcesslog.setProcessFlag(B2BProcessFlag.PROCESS_FLAG_FAILURE.value);
            b2BProcesslog.setProcessComment(e.getMessage());
            //记录错误日志
            failedProcessLogService.insertOrUpdateFailedLog
                    (b2BProcesslog,joyoungOrderPlanned.getUniqueId(),joyoungOrderPlanned.getB2bOrderId());
            b2BProcesslogService.updateProcessFlag(b2BProcesslog);
            String errorStr = "工单派单失败，原因是：";
            log.error(errorStr, e.getMessage());
            sysLogService.insert(1L,infoJson,errorStr + e.getMessage(),
                    errorStr, OperationCommand.OperationCode.PLANNED.serviceCode, "POST");
            return msResponse;
        }
    }
}
