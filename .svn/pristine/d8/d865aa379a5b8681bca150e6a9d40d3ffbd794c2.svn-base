package com.kkl.kklplus.b2b.joyoung.service;

import com.google.common.base.Supplier;
import com.google.gson.Gson;
import com.kkl.kklplus.b2b.joyoung.http.command.OperationCommand;
import com.kkl.kklplus.b2b.joyoung.http.request.OrderAppointedRequestParam;
import com.kkl.kklplus.b2b.joyoung.http.request.OrderHaltRequestParam;
import com.kkl.kklplus.b2b.joyoung.http.response.ResponseBody;
import com.kkl.kklplus.b2b.joyoung.http.response.ResponseData;
import com.kkl.kklplus.b2b.joyoung.http.utils.OkHttpUtils;
import com.kkl.kklplus.b2b.joyoung.utils.DateUtils;
import com.kkl.kklplus.common.exception.MSErrorCode;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.b2bcenter.rpt.B2BOrderProcesslog;
import com.kkl.kklplus.entity.joyoung.sd.JoyoungOrderAppointed;
import com.kkl.kklplus.b2b.joyoung.mapper.JoyoungOrderAppointedMapper;
import com.kkl.kklplus.entity.b2b.common.B2BProcessFlag;
import com.kkl.kklplus.entity.sys.SysDict;
import com.kkl.kklplus.utils.QuarterUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.stream.Stream;

@Slf4j
@Service
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class JoyoungOrderAppointedService {

    @Autowired
    private B2BProcesslogService b2BProcesslogService;

    @Autowired
    private SysLogService sysLogService;

    @Autowired
    private JoyoungFailedProcessLogService failedProcessLogService;

    @Resource
    private JoyoungOrderAppointedMapper joyoungOrderAppointedMapper;

    public void insert(JoyoungOrderAppointed joyoungOrderAppointed) {
        joyoungOrderAppointed.preInsert();
        joyoungOrderAppointed.setProcessFlag(B2BProcessFlag.PROCESS_FLAG_ACCEPT.value);
        joyoungOrderAppointed.setProcessTime(0);
        joyoungOrderAppointed.setQuarter(QuarterUtils.getQuarter(joyoungOrderAppointed.getCreateDate()));
        joyoungOrderAppointedMapper.insert(joyoungOrderAppointed);
    }

    public void updateProcessFlag(JoyoungOrderAppointed joyoungOrderAppointed) {
        joyoungOrderAppointed.preUpdate();
        joyoungOrderAppointed.setProcessComment(StringUtils.left(joyoungOrderAppointed.getProcessComment(), 200));
        joyoungOrderAppointedMapper.updateProcessFlag(joyoungOrderAppointed);
    }

    public Long autoBookDateNextHours() {
        Calendar createCalendar = Calendar.getInstance();
        createCalendar.setTime(new Date(System.currentTimeMillis()));
        createCalendar.add(Calendar.HOUR_OF_DAY, 1);
        createCalendar.set(Calendar.MINUTE, 0);
        createCalendar.set(Calendar.SECOND, 0);
        createCalendar.set(Calendar.MILLISECOND, 0);
        return createCalendar.getTimeInMillis();
    }

    public MSResponse orderApponited(JoyoungOrderAppointed joyoungOrderAppointed) {
        MSResponse msResponse = new MSResponse();
        if(joyoungOrderAppointed == null){
            msResponse.setErrorCode(MSErrorCode.FAILURE);
            msResponse.setMsg("信息不能为空！");
            return msResponse;
        }
        Integer reasonType = joyoungOrderAppointed.getReasonType();
        B2BOrderProcesslog b2BProcesslog = new B2BOrderProcesslog();
        // 正常预约
        if(reasonType == null){
            joyoungOrderAppointed.setReasonType(0);
        }
        OrderAppointedRequestParam reqBody = new OrderAppointedRequestParam();
        reqBody.setOrderNo(joyoungOrderAppointed.getOrderNo());
        reqBody.setBookMan(joyoungOrderAppointed.getBookMan());
        reqBody.setBookDate(DateUtils.formatDate(
                DateUtils.timeStampToDate(joyoungOrderAppointed.getBookDate()
                ),"yyyy-MM-dd HH:mm:ss"
        ));
        reqBody.setBookRemark(joyoungOrderAppointed.getBookRemark());
        OperationCommand command = OperationCommand.newInstance(OperationCommand.OperationCode.APPOINTED, reqBody);
        b2BProcesslog.setInterfaceName(OperationCommand.OperationCode.APPOINTED.serviceCode);
        Gson gson = new Gson();
        msResponse.setErrorCode(MSErrorCode.SUCCESS);
        b2BProcesslog.setProcessFlag(B2BProcessFlag.PROCESS_FLAG_FAILURE.value);
        b2BProcesslog.setProcessTime(0);
        b2BProcesslog.setCreateById(joyoungOrderAppointed.getCreateById());
        b2BProcesslog.setUpdateById(joyoungOrderAppointed.getUpdateById());
        b2BProcesslog.preInsert();
        b2BProcesslog.setQuarter(QuarterUtils.getQuarter(b2BProcesslog.getCreateDate()));
        ResponseBody<ResponseData> resBody = OkHttpUtils.postSyncGenericNew(command, ResponseData.class);
        String infoJson = gson.toJson(command.getReqBody());
        b2BProcesslog.setInfoJson(infoJson);
        try {
            b2BProcesslogService.insert(b2BProcesslog);
            b2BProcesslog.setResultJson(resBody.getOriginalJson());
            this.insert(joyoungOrderAppointed);
            if( resBody.getErrorCode() != ResponseBody.ErrorCode.SUCCESS.code){
                String errorMsg = StringUtils.left(resBody.getErrorMsg(),200);
                if(resBody.getErrorCode() >= ResponseBody.ErrorCode.REQUEST_INVOCATION_FAILURE.code){
                    msResponse.setErrorCode(new MSErrorCode(resBody.getErrorCode(),errorMsg));
                }
                msResponse.setThirdPartyErrorCode(new MSErrorCode(resBody.getErrorCode(),errorMsg));
                joyoungOrderAppointed.setProcessFlag(B2BProcessFlag.PROCESS_FLAG_FAILURE.value);
                joyoungOrderAppointed.setProcessComment(errorMsg);
                b2BProcesslog.setProcessFlag(B2BProcessFlag.PROCESS_FLAG_FAILURE.value);
                b2BProcesslog.setProcessComment(errorMsg);
                //记录错误日志
                failedProcessLogService.insertOrUpdateFailedLog
                        (b2BProcesslog,joyoungOrderAppointed.getUniqueId(),joyoungOrderAppointed.getB2bOrderId());
                this.updateProcessFlag(joyoungOrderAppointed);
                b2BProcesslogService.updateProcessFlag(b2BProcesslog);
                return msResponse;
            }else{
                ResponseData responseData = resBody.getData();
                if(responseData != null && responseData.getResultFlag() == ResponseBody.ErrorCode.SUCCESS.code) {
                    joyoungOrderAppointed.setProcessFlag(B2BProcessFlag.PROCESS_FLAG_SUCESS.value);
                    this.updateProcessFlag(joyoungOrderAppointed);
                    b2BProcesslog.setProcessFlag(B2BProcessFlag.PROCESS_FLAG_SUCESS.value);
                    b2BProcesslogService.updateProcessFlag(b2BProcesslog);
                }else{
                    msResponse.setThirdPartyErrorCode(new MSErrorCode(responseData.getResultFlag(),responseData.getResultMsg()));
                    joyoungOrderAppointed.setProcessFlag(B2BProcessFlag.PROCESS_FLAG_FAILURE.value);
                    joyoungOrderAppointed.setProcessComment(responseData == null?"":responseData.getResultMsg());
                    b2BProcesslog.setProcessFlag(B2BProcessFlag.PROCESS_FLAG_FAILURE.value);
                    b2BProcesslog.setProcessComment(responseData == null?"":responseData.getResultMsg());
                    //记录错误日志
                    failedProcessLogService.insertOrUpdateFailedLog
                            (b2BProcesslog,joyoungOrderAppointed.getUniqueId(),joyoungOrderAppointed.getB2bOrderId());
                    this.updateProcessFlag(joyoungOrderAppointed);
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
                    (b2BProcesslog,joyoungOrderAppointed.getUniqueId(),joyoungOrderAppointed.getB2bOrderId());
            b2BProcesslogService.updateProcessFlag(b2BProcesslog);
            log.error("工单预约单失败，原因是：{}", e.getMessage());
            sysLogService.insert(1L,infoJson, e.getMessage(),
                    "工单预约单失败", OperationCommand.OperationCode.APPOINTED.serviceCode, "POST");
            return msResponse;
        }
    }
}
