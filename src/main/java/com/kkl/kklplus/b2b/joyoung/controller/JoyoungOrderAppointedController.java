package com.kkl.kklplus.b2b.joyoung.controller;

import com.google.common.base.Supplier;
import com.google.gson.Gson;
import com.kkl.kklplus.b2b.joyoung.feign.MSDictFeign;
import com.kkl.kklplus.b2b.joyoung.http.command.OperationCommand;
import com.kkl.kklplus.b2b.joyoung.http.request.OrderAppointedRequestParam;
import com.kkl.kklplus.b2b.joyoung.http.request.OrderHaltRequestParam;
import com.kkl.kklplus.b2b.joyoung.http.response.ResponseBody;
import com.kkl.kklplus.b2b.joyoung.http.response.ResponseData;
import com.kkl.kklplus.b2b.joyoung.http.utils.OkHttpUtils;
import com.kkl.kklplus.b2b.joyoung.service.B2BProcesslogService;
import com.kkl.kklplus.b2b.joyoung.service.JoyoungFailedProcessLogService;
import com.kkl.kklplus.b2b.joyoung.service.JoyoungOrderAppointedService;
import com.kkl.kklplus.b2b.joyoung.service.SysLogService;
import com.kkl.kklplus.b2b.joyoung.utils.DateUtils;
import com.kkl.kklplus.common.exception.MSErrorCode;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.b2b.common.B2BProcessFlag;
import com.kkl.kklplus.entity.b2bcenter.rpt.B2BOrderProcesslog;
import com.kkl.kklplus.entity.joyoung.sd.JoyoungOrderAppointed;
import com.kkl.kklplus.entity.sys.SysDict;
import com.kkl.kklplus.utils.QuarterUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.List;
import java.util.stream.Stream;

@Slf4j
@RestController
@RequestMapping("/joyoungOrderAppointed")
public class JoyoungOrderAppointedController {

    @Autowired
    private B2BProcesslogService b2BProcesslogService;

    @Autowired
    private SysLogService sysLogService;

    @Autowired
    private JoyoungOrderAppointedService joyoungOrderAppointedService;

    @Autowired
    private JoyoungFailedProcessLogService failedProcessLogService;

    @Autowired
    private MSDictFeign msDictFeign;

    @RequestMapping("/appointed")
    public MSResponse orderAppointed(@RequestBody JoyoungOrderAppointed joyoungOrderAppointed){
        MSResponse msResponse = new MSResponse();
        if(joyoungOrderAppointed == null){
            msResponse.setErrorCode(MSErrorCode.FAILURE);
            msResponse.setMsg("信息不能为空！");
            return msResponse;
        }
        Integer reasonType = joyoungOrderAppointed.getReasonType();
        OperationCommand command;
        B2BOrderProcesslog b2BProcesslog = new B2BOrderProcesslog();
        // 3为正常预约
        if(reasonType == 3 || reasonType == null) {
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
            command = OperationCommand.newInstance(OperationCommand.OperationCode.APPOINTED, reqBody);
            b2BProcesslog.setInterfaceName(OperationCommand.OperationCode.APPOINTED.serviceCode);
        }else{
            MSResponse<List<SysDict>> response = msDictFeign.getListByType("JoyoungHaltType");
            if (!MSResponse.isSuccessCode(msResponse)) {
                return new MSResponse<>(new MSErrorCode(1000, "读取九阳挂起类型设定失败:" + msResponse.getMsg()));
            }
            List<SysDict> intervals = response.getData();
            if (ObjectUtils.isEmpty(intervals)) {
                return new MSResponse<>(new MSErrorCode(1000, "读取九阳挂起类型设定失败:请检查数据字典中是否设定"));
            }
            Supplier<Stream<SysDict>> streamSupplier = () -> intervals.stream();
            SysDict firstInterval = streamSupplier.get().filter(t -> t.getValue().equalsIgnoreCase(reasonType.toString())).findFirst().orElse(null);
            if (ObjectUtils.isEmpty(firstInterval)) {
                log.error("读取九阳挂起类型设定失败:请检查数据字典中是否设定:{}","");
                return new MSResponse<>(new MSErrorCode(1000, "读取九阳挂起类型设定失败:请检查数据字典中是否设定"));
            }
            if(joyoungOrderAppointed.getCreateDt() != null && joyoungOrderAppointed.getCreateDt() > 0){
                joyoungOrderAppointed.setCreateDt(System.currentTimeMillis());
            }
            Integer sort = firstInterval.getSort();
            String description = firstInterval.getDescription();
            OrderHaltRequestParam reqBody = new OrderHaltRequestParam();
            reqBody.setOrderNo(joyoungOrderAppointed.getOrderNo());
            reqBody.setOperatorDate(DateUtils.formatDate(
                    DateUtils.timeStampToDate(joyoungOrderAppointed.getCreateDt()
                    ),"yyyy-MM-dd HH:mm:ss"
            ));
            reqBody.setBookDate(DateUtils.formatDate(
                    DateUtils.timeStampToDate(joyoungOrderAppointed.getBookDate()
                    ),"yyyy-MM-dd HH:mm:ss"
            ));
            reqBody.setType(sort);
            String bookRemark = joyoungOrderAppointed.getBookRemark();
            if(StringUtils.isNotBlank(bookRemark)){
                reqBody.setRemark(bookRemark);
           }else{
                reqBody.setRemark(description);
            }
            command = OperationCommand.newInstance(OperationCommand.OperationCode.HALT, reqBody);
            b2BProcesslog.setInterfaceName(OperationCommand.OperationCode.HALT.serviceCode);
        }
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
            joyoungOrderAppointedService.insert(joyoungOrderAppointed);
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
                joyoungOrderAppointedService.updateProcessFlag(joyoungOrderAppointed);
                b2BProcesslogService.updateProcessFlag(b2BProcesslog);
                return msResponse;
            }else{
                ResponseData responseData = resBody.getData();
                if(responseData != null && responseData.getResultFlag() == ResponseBody.ErrorCode.SUCCESS.code) {
                    joyoungOrderAppointed.setProcessFlag(B2BProcessFlag.PROCESS_FLAG_SUCESS.value);
                    joyoungOrderAppointedService.updateProcessFlag(joyoungOrderAppointed);
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
                    joyoungOrderAppointedService.updateProcessFlag(joyoungOrderAppointed);
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
                    (b2BProcesslog,joyoungOrderAppointed.getUniqueId(),joyoungOrderAppointed.getB2bOrderId());
            b2BProcesslogService.updateProcessFlag(b2BProcesslog);
            log.error("工单预约单失败，原因是：{}", e.getMessage());
            sysLogService.insert(1L,infoJson, e.getMessage(),
                    "工单预约单失败", OperationCommand.OperationCode.APPOINTED.serviceCode, "POST");
            return msResponse;
        }
    }


}
