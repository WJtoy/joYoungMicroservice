package com.kkl.kklplus.b2b.joyoung.service;


import com.google.gson.Gson;
import com.kkl.kklplus.b2b.joyoung.http.command.OperationCommand;
import com.kkl.kklplus.b2b.joyoung.http.request.OrderProcesslogRequestParam;
import com.kkl.kklplus.b2b.joyoung.http.response.ResponseBody;
import com.kkl.kklplus.b2b.joyoung.http.response.ResponseData;
import com.kkl.kklplus.b2b.joyoung.http.utils.OkHttpUtils;
import com.kkl.kklplus.b2b.joyoung.mapper.JoyoungOrderInfoMapper;
import com.kkl.kklplus.b2b.joyoung.mapper.JoyoungOrderProcesslogMapper;
import com.kkl.kklplus.b2b.joyoung.utils.DateUtils;
import com.kkl.kklplus.common.exception.MSErrorCode;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.b2b.common.B2BProcessFlag;
import com.kkl.kklplus.entity.b2bcenter.rpt.B2BOrderProcesslog;
import com.kkl.kklplus.entity.joyoung.sd.JoyoungOrderProcessLog;
import com.kkl.kklplus.utils.QuarterUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

@Slf4j
@Service
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class JoyoungOrderProcesslogService {

    @Resource
    private JoyoungOrderProcesslogMapper joyoungOrderProcesslogMapper;

    @Resource
    private JoyoungOrderInfoMapper joyoungOrderInfoMapper;

    @Autowired
    private B2BProcesslogService b2BProcesslogService;

    @Autowired
    private SysLogService sysLogService;

    public MSResponse saveOrderProcesslog(JoyoungOrderProcessLog orderProcessLog) {
        MSResponse msResponse = new MSResponse();
        if(orderProcessLog == null){
            msResponse.setErrorCode(MSErrorCode.FAILURE);
            msResponse.setMsg("信息不能为空！");
            return msResponse;
        }
        Gson gson = new Gson();
        msResponse.setErrorCode(MSErrorCode.SUCCESS);
        B2BOrderProcesslog b2BProcesslog = new B2BOrderProcesslog();
        b2BProcesslog.setInterfaceName(OperationCommand.OperationCode.SAVEPROCESSLOG.serviceCode);
        b2BProcesslog.setProcessFlag(B2BProcessFlag.PROCESS_FLAG_FAILURE.value);
        b2BProcesslog.setProcessTime(0);
        b2BProcesslog.setCreateById(orderProcessLog.getCreateById());
        b2BProcesslog.setUpdateById(1L);
        b2BProcesslog.preInsert();
        b2BProcesslog.setQuarter(QuarterUtils.getQuarter(b2BProcesslog.getCreateDate()));
        String orderNo = joyoungOrderInfoMapper.findOrderNoByKklOrderId(orderProcessLog.getOrderId());
        if(StringUtils.isBlank(orderNo)){
            msResponse.setErrorCode(MSErrorCode.FAILURE);
            msResponse.setMsg("没有找到对应的工单号orderNo！");
            return msResponse;
        }
        orderProcessLog.setB2bOrderNo(orderNo);
        OrderProcesslogRequestParam reqBody = new OrderProcesslogRequestParam();
        reqBody.setOrderNo(orderProcessLog.getB2bOrderNo());
        reqBody.setOperationName(orderProcessLog.getOperatorName());
        reqBody.setLogDate(DateUtils.formatDate(
                DateUtils.timeStampToDate(orderProcessLog.getLogDate()
                ),"yyyy-MM-dd HH:mm:ss"
        ));
        reqBody.setLogType(orderProcessLog.getLogType());
        reqBody.setLogText(orderProcessLog.getLogContent());
        OperationCommand command = OperationCommand.newInstance(OperationCommand.OperationCode.SAVEPROCESSLOG, reqBody);
        ResponseBody<ResponseData> resBody = OkHttpUtils.postSyncGenericNew(command, ResponseData.class);
        String infoJson = gson.toJson(reqBody);
        b2BProcesslog.setInfoJson(infoJson);
        try {
            b2BProcesslogService.insert(b2BProcesslog);
            b2BProcesslog.setResultJson(resBody.getOriginalJson());
            this.insert(orderProcessLog);
            if( resBody.getErrorCode() != ResponseBody.ErrorCode.SUCCESS.code){
                String errorMsg = StringUtils.left(resBody.getErrorMsg(),200);
                if(resBody.getErrorCode() >= ResponseBody.ErrorCode.REQUEST_INVOCATION_FAILURE.code){
                    msResponse.setErrorCode(new MSErrorCode(resBody.getErrorCode(),errorMsg));
                }
                msResponse.setThirdPartyErrorCode(new MSErrorCode(resBody.getErrorCode(),errorMsg));
                orderProcessLog.setProcessFlag(B2BProcessFlag.PROCESS_FLAG_FAILURE.value);
                orderProcessLog.setProcessComment(errorMsg);
                this.updateProcessFlag(orderProcessLog);
                b2BProcesslog.setProcessFlag(B2BProcessFlag.PROCESS_FLAG_FAILURE.value);
                b2BProcesslog.setProcessComment(errorMsg);
                b2BProcesslogService.updateProcessFlag(b2BProcesslog);
                return msResponse;
            }else{
                ResponseData responseData = resBody.getData();
                if(responseData != null && responseData.getResultFlag() == ResponseBody.ErrorCode.SUCCESS.code) {
                    orderProcessLog.setProcessFlag(B2BProcessFlag.PROCESS_FLAG_SUCESS.value);
                    this.updateProcessFlag(orderProcessLog);
                    b2BProcesslog.setProcessFlag(B2BProcessFlag.PROCESS_FLAG_SUCESS.value);
                    b2BProcesslogService.updateProcessFlag(b2BProcesslog);
                }else{
                    msResponse.setThirdPartyErrorCode(new MSErrorCode(responseData.getResultFlag(),responseData.getResultMsg()));
                    orderProcessLog.setProcessFlag(B2BProcessFlag.PROCESS_FLAG_FAILURE.value);
                    orderProcessLog.setProcessComment(responseData == null?"":responseData.getResultMsg());
                    this.updateProcessFlag(orderProcessLog);
                    b2BProcesslog.setProcessFlag(B2BProcessFlag.PROCESS_FLAG_FAILURE.value);
                    b2BProcesslog.setProcessComment(responseData == null?"":responseData.getResultMsg());
                    b2BProcesslogService.updateProcessFlag(b2BProcesslog);
                }
                return msResponse;
            }
        }catch (Exception e) {
            msResponse.setErrorCode(MSErrorCode.FAILURE);
            msResponse.setMsg(StringUtils.left(e.getMessage(),200));
            b2BProcesslog.setProcessFlag(B2BProcessFlag.PROCESS_FLAG_FAILURE.value);
            b2BProcesslog.setProcessComment(e.getMessage());
            b2BProcesslogService.updateProcessFlag(b2BProcesslog);
            String errorStr = "工单日志同步失败，原因是：";
            log.error(errorStr, e.getMessage());
            sysLogService.insert(1L,infoJson,errorStr + e.getMessage(),
                    errorStr, OperationCommand.OperationCode.SAVEPROCESSLOG.serviceCode, "POST");
            return msResponse;
        }
    }

    private void insert(JoyoungOrderProcessLog orderProcessLog) {
        orderProcessLog.preInsert();
        orderProcessLog.setProcessFlag(B2BProcessFlag.PROCESS_FLAG_ACCEPT.value);
        orderProcessLog.setProcessTime(0);
        orderProcessLog.setUpdateById(1L);
        orderProcessLog.setQuarter(QuarterUtils.getQuarter(orderProcessLog.getCreateDate()));
        joyoungOrderProcesslogMapper.insert(orderProcessLog);
    }
    private void updateProcessFlag(JoyoungOrderProcessLog orderProcessLog) {
        orderProcessLog.preUpdate();
        orderProcessLog.setProcessComment(StringUtils.left(orderProcessLog.getProcessComment(), 200));
        joyoungOrderProcesslogMapper.updateProcessFlag(orderProcessLog);
    }

}
