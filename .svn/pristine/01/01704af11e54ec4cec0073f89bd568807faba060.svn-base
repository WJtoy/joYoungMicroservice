package com.kkl.kklplus.b2b.joyoung.controller;

import com.google.gson.Gson;
import com.kkl.kklplus.b2b.joyoung.http.command.OperationCommand;
import com.kkl.kklplus.b2b.joyoung.http.request.OrderCancelledRequestParam;
import com.kkl.kklplus.b2b.joyoung.http.response.ResponseBody;
import com.kkl.kklplus.b2b.joyoung.http.response.ResponseData;
import com.kkl.kklplus.b2b.joyoung.http.utils.OkHttpUtils;
import com.kkl.kklplus.b2b.joyoung.service.*;
import com.kkl.kklplus.b2b.joyoung.utils.DateUtils;
import com.kkl.kklplus.common.exception.MSErrorCode;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.b2b.common.B2BProcessFlag;
import com.kkl.kklplus.entity.b2bcenter.rpt.B2BOrderProcesslog;
import com.kkl.kklplus.entity.jinjing.sd.JinJingOrderAppointed;
import com.kkl.kklplus.entity.joyoung.sd.JoyoungOrderAppointed;
import com.kkl.kklplus.entity.joyoung.sd.JoyoungOrderCancelled;
import com.kkl.kklplus.utils.QuarterUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;

@Slf4j
@RestController
@RequestMapping("/joyoungOrderCancelled")
public class JoyoungOrderCancelledController {

    @Autowired
    private B2BProcesslogService b2BProcesslogService;

    @Autowired
    private SysLogService sysLogService;

    @Autowired
    private JoyoungOrderCancelledService joyoungOrderCancelledService;

    @Autowired
    private JoyoungFailedProcessLogService failedProcessLogService;

    @Autowired
    private JoyoungOrderAppointedService orderAppointedService;

    @RequestMapping("/cancelled")
    public MSResponse orderCancelled(@RequestBody JoyoungOrderCancelled joyoungOrderCancelled){
        MSResponse msResponse = new MSResponse();
        if(joyoungOrderCancelled == null){
            msResponse.setErrorCode(MSErrorCode.FAILURE);
            msResponse.setMsg("信息不能为空！");
            return msResponse;
        }
        JoyoungOrderAppointed orderAppointed = new JoyoungOrderAppointed();
        orderAppointed.setReasonType(3);
        orderAppointed.setB2bOrderId(joyoungOrderCancelled.getB2bOrderId());
        orderAppointed.setBookMan(joyoungOrderCancelled.getCancelMan());
        orderAppointed.setOrderNo(joyoungOrderCancelled.getOrderNo());
        orderAppointed.setBookDate(orderAppointedService.autoBookDateNextHours());
        orderAppointed.setBookRemark("");
        orderAppointed.setCreateById(joyoungOrderCancelled.getCreateById());
        orderAppointed.setUpdateById(joyoungOrderCancelled.getCreateById());
        orderAppointed.setCreateDt(System.currentTimeMillis());
        orderAppointedService.orderApponited(orderAppointed);

        Gson gson = new Gson();
        msResponse.setErrorCode(MSErrorCode.SUCCESS);
        B2BOrderProcesslog b2BProcesslog = new B2BOrderProcesslog();
        b2BProcesslog.setInterfaceName(OperationCommand.OperationCode.CANCELLED.serviceCode);
        b2BProcesslog.setProcessFlag(B2BProcessFlag.PROCESS_FLAG_FAILURE.value);
        b2BProcesslog.setProcessTime(0);
        b2BProcesslog.setCreateById(joyoungOrderCancelled.getCreateById());
        b2BProcesslog.setUpdateById(joyoungOrderCancelled.getUpdateById());
        b2BProcesslog.preInsert();
        b2BProcesslog.setQuarter(QuarterUtils.getQuarter(b2BProcesslog.getCreateDate()));
        OrderCancelledRequestParam reqBody = new OrderCancelledRequestParam();
        reqBody.setOrderNo(joyoungOrderCancelled.getOrderNo());
        reqBody.setCancelMan(joyoungOrderCancelled.getCancelMan());
        reqBody.setCancelDate(DateUtils.formatDate(
                DateUtils.timeStampToDate(joyoungOrderCancelled.getCancelDate()
                ),"yyyy-MM-dd HH:mm:ss"
        ));
        reqBody.setCancelRemark(joyoungOrderCancelled.getCancelRemark());
        OperationCommand command = OperationCommand.newInstance(OperationCommand.OperationCode.CANCELLED, reqBody);
        ResponseBody<ResponseData> resBody = OkHttpUtils.postSyncGenericNew(command, ResponseData.class);
        String infoJson = gson.toJson(reqBody);
        b2BProcesslog.setInfoJson(infoJson);
        try {
            b2BProcesslogService.insert(b2BProcesslog);
            b2BProcesslog.setResultJson(resBody.getOriginalJson());
            joyoungOrderCancelledService.insert(joyoungOrderCancelled);
            if( resBody.getErrorCode() != ResponseBody.ErrorCode.SUCCESS.code){
                String errorMsg = StringUtils.left(resBody.getErrorMsg(),200);
                if(resBody.getErrorCode() >= ResponseBody.ErrorCode.REQUEST_INVOCATION_FAILURE.code){
                    msResponse.setErrorCode(new MSErrorCode(resBody.getErrorCode(),errorMsg));
                }
                msResponse.setThirdPartyErrorCode(new MSErrorCode(resBody.getErrorCode(),errorMsg));
                b2BProcesslog.setProcessFlag(B2BProcessFlag.PROCESS_FLAG_FAILURE.value);
                b2BProcesslog.setProcessComment(errorMsg);
                joyoungOrderCancelled.setProcessFlag(B2BProcessFlag.PROCESS_FLAG_FAILURE.value);
                joyoungOrderCancelled.setProcessComment(errorMsg);
                //记录错误日志
                failedProcessLogService.insertOrUpdateFailedLog
                        (b2BProcesslog,joyoungOrderCancelled.getUniqueId(),joyoungOrderCancelled.getB2bOrderId());
                joyoungOrderCancelledService.updateProcessFlag(joyoungOrderCancelled);
                b2BProcesslogService.updateProcessFlag(b2BProcesslog);
                return msResponse;
            }else{
                ResponseData responseData = resBody.getData();
                if(responseData != null && responseData.getResultFlag() == ResponseBody.ErrorCode.SUCCESS.code) {
                    joyoungOrderCancelled.setProcessFlag(B2BProcessFlag.PROCESS_FLAG_SUCESS.value);
                    joyoungOrderCancelledService.updateProcessFlag(joyoungOrderCancelled);
                    b2BProcesslog.setProcessFlag(B2BProcessFlag.PROCESS_FLAG_SUCESS.value);
                    b2BProcesslogService.updateProcessFlag(b2BProcesslog);
                }else{
                    msResponse.setThirdPartyErrorCode(new MSErrorCode(responseData.getResultFlag(),responseData.getResultMsg()));
                    b2BProcesslog.setProcessFlag(B2BProcessFlag.PROCESS_FLAG_FAILURE.value);
                    b2BProcesslog.setProcessComment(responseData == null?"":responseData.getResultMsg());
                    joyoungOrderCancelled.setProcessFlag(B2BProcessFlag.PROCESS_FLAG_FAILURE.value);
                    joyoungOrderCancelled.setProcessComment(responseData == null?"":responseData.getResultMsg());
                    //记录错误日志
                    failedProcessLogService.insertOrUpdateFailedLog
                            (b2BProcesslog,joyoungOrderCancelled.getUniqueId(),joyoungOrderCancelled.getB2bOrderId());
                    joyoungOrderCancelledService.updateProcessFlag(joyoungOrderCancelled);
                    b2BProcesslogService.updateProcessFlag(b2BProcesslog);
                }
                return msResponse;
            }
        }catch (Exception e) {
            msResponse.setErrorCode(MSErrorCode.FAILURE);
            msResponse.setMsg(StringUtils.left(e.getMessage(),200));
            b2BProcesslog.setProcessFlag(B2BProcessFlag.PROCESS_FLAG_FAILURE.value);
            b2BProcesslog.setProcessComment(StringUtils.left(e.getMessage(),200));
            //记录错误日志
            failedProcessLogService.insertOrUpdateFailedLog
                    (b2BProcesslog,joyoungOrderCancelled.getUniqueId(),joyoungOrderCancelled.getB2bOrderId());
            b2BProcesslogService.updateProcessFlag(b2BProcesslog);
            log.error("工单取消失败，原因是：{}", e.getMessage());
            sysLogService.insert(1L,infoJson, e.getMessage(),
                    "工单取消失败", OperationCommand.OperationCode.CANCELLED.serviceCode, "POST");
            return msResponse;
        }
    }
}