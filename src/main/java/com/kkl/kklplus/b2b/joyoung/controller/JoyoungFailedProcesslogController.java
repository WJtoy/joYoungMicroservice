package com.kkl.kklplus.b2b.joyoung.controller;

import com.google.gson.Gson;
import com.kkl.kklplus.b2b.joyoung.http.command.OperationCommand;
import com.kkl.kklplus.b2b.joyoung.http.request.OrderCancelledRequestParam;
import com.kkl.kklplus.b2b.joyoung.http.request.OrderCompletedRequestParam;
import com.kkl.kklplus.b2b.joyoung.http.response.ResponseBody;
import com.kkl.kklplus.b2b.joyoung.http.response.ResponseData;
import com.kkl.kklplus.b2b.joyoung.http.utils.OkHttpUtils;
import com.kkl.kklplus.b2b.joyoung.service.B2BProcesslogService;
import com.kkl.kklplus.b2b.joyoung.service.JoyoungFailedProcessLogService;
import com.kkl.kklplus.common.exception.MSErrorCode;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.b2b.common.B2BProcessFlag;
import com.kkl.kklplus.entity.b2bcenter.md.B2BDataSourceEnum;
import com.kkl.kklplus.entity.b2bcenter.md.B2BInterfaceIdEnum;
import com.kkl.kklplus.entity.b2bcenter.rpt.B2BOrderProcesslog;
import com.kkl.kklplus.entity.b2bcenter.rpt.B2BProcessLogSearchModel;
import com.kkl.kklplus.entity.b2bcenter.sd.B2BRetryOperationData;
import com.kkl.kklplus.entity.common.MSPage;
import com.kkl.kklplus.utils.QuarterUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


@Slf4j
@RestController
@RequestMapping("/failedLog")
public class JoyoungFailedProcesslogController {

    @Autowired
    private JoyoungFailedProcessLogService failedProcessLogService;

    @Autowired
    private B2BProcesslogService b2BProcesslogService;

    @PostMapping("getList")
    public MSResponse<MSPage<B2BOrderProcesslog>> getProcessLogList(@RequestBody B2BProcessLogSearchModel processLogSearchModel){
        MSPage<B2BOrderProcesslog> msPage = failedProcessLogService.getList(processLogSearchModel,
                B2BInterfaceIdEnum.getById(processLogSearchModel.getB2bInterfaceId()).code);
        return new MSResponse<>(MSErrorCode.SUCCESS, msPage);
    }
    /**
     * 重发数据
     * @param retryOperationData 重发数据实体
     * @return 返回重发结果
     */
    @PostMapping("retryData")
    public MSResponse retryData(@RequestBody B2BRetryOperationData retryOperationData){
        MSErrorCode errorCode = checkRetryData(retryOperationData);
        Integer interfaceId = retryOperationData.getB2bInterfaceId();
        B2BInterfaceIdEnum interfaceIdEnum = B2BInterfaceIdEnum.getById(interfaceId);
        if(MSErrorCode.SUCCESS.getCode() != errorCode.getCode()){
            return new MSResponse<>(errorCode);
        }
        MSErrorCode responseErrorCode;
        if(interfaceIdEnum.id == B2BInterfaceIdEnum.JOYOUNG_TO_COMPLETE_BILLS.id){
            responseErrorCode = retryCompleteData(retryOperationData);
        }else if(interfaceIdEnum.id == B2BInterfaceIdEnum.JOYOUNG_TO_CANCEL_BILLS.id){
            responseErrorCode = retryCancelData(retryOperationData);
        }else{
            MSResponse response = new MSResponse<>(MSErrorCode.FAILURE);
            response.setMsg("暂时不支持"+interfaceIdEnum.description+"接口重发");
            return response;
        }
        if(MSErrorCode.SUCCESS.getCode() != responseErrorCode.getCode()){
            return new MSResponse<>(responseErrorCode);
        }
        return new MSResponse<>(MSErrorCode.SUCCESS);
    }
    /**
     * 退单重发实现
     * @param retryOperationData 重发实体
     */
    private MSErrorCode retryCancelData(B2BRetryOperationData retryOperationData) {
        String dataJson = retryOperationData.getDataJson();
        try {
            Gson gson = new Gson();
            OrderCancelledRequestParam reqBody = gson.fromJson(dataJson, OrderCancelledRequestParam.class);
            OperationCommand command = OperationCommand.newInstance(OperationCommand.OperationCode.CANCELLED, reqBody);
            ResponseBody<ResponseData> resBody = OkHttpUtils.postSyncGenericNew(command, ResponseData.class);
            return processResult(
                    gson.toJson(reqBody),
                    OperationCommand.OperationCode.CANCELLED.serviceCode,
                    resBody,
                    retryOperationData.getProcessLogId());
        }catch (Exception e){
            log.error("重发退单报错->{}:{}",dataJson,e.getMessage());
            return new MSErrorCode(MSErrorCode.FAILURE.getCode(), e.getMessage());
        }
    }

    /**
     * 完工重发实现
     * @param retryOperationData 重发实体
     */
    private MSErrorCode retryCompleteData(B2BRetryOperationData retryOperationData) {
        String dataJson = retryOperationData.getDataJson();
        try {
            Gson gson = new Gson();
            OrderCompletedRequestParam reqBody = gson.fromJson(dataJson, OrderCompletedRequestParam.class);
            Long completesdate = reqBody.getCompletesdate();
            if(completesdate == 0){
                reqBody.setCompletesdate(null);
            }
            //赋值原来的好评信息
            B2BOrderProcesslog oldLog = failedProcessLogService.getLogById(retryOperationData.getProcessLogId());
            OrderCompletedRequestParam oldReq = gson.fromJson(oldLog.getInfoJson(), OrderCompletedRequestParam.class);
            reqBody.setIsPraise(oldReq.getIsPraise()!=null?oldReq.getIsPraise():0);
            reqBody.setPraisePhoto1(oldReq.getPraisePhoto1());
            reqBody.setPraisePhoto2(oldReq.getPraisePhoto2());

            OperationCommand command = OperationCommand.newInstance(OperationCommand.OperationCode.COMPLETED, reqBody);
            ResponseBody<ResponseData> resBody = OkHttpUtils.postSyncGenericNew(command, ResponseData.class);
            return processResult(
                    gson.toJson(reqBody),
                    OperationCommand.OperationCode.COMPLETED.serviceCode,
                    resBody,
                    retryOperationData.getProcessLogId());
        }catch (Exception e){
            log.error("重发完工报错->{}:{}",dataJson,e.getMessage());
            return new MSErrorCode(MSErrorCode.FAILURE.getCode(), e.getMessage());
        }
    }

    /**
     * 处理请求结果
     * @param infoJson  请求数据
     * @param apiUrl    请求接口
     * @param resBody   返回结果
     * @param failedId  失败日志ID
     * @return
     */
    private MSErrorCode processResult(String infoJson,String apiUrl,ResponseBody<ResponseData> resBody,Long failedId){
        B2BOrderProcesslog b2BProcesslog = new B2BOrderProcesslog();
        b2BProcesslog.setInterfaceName(apiUrl);
        b2BProcesslog.setInfoJson(infoJson);
        b2BProcesslog.setProcessFlag(B2BProcessFlag.PROCESS_FLAG_FAILURE.value);
        b2BProcesslog.setProcessTime(0);
        b2BProcesslog.setCreateById(1L);
        b2BProcesslog.setUpdateById(1L);
        b2BProcesslog.preInsert();
        b2BProcesslog.setQuarter(QuarterUtils.getQuarter(b2BProcesslog.getCreateDate()));
        b2BProcesslog.setResultJson(resBody.getOriginalJson());
        MSErrorCode errorCode = new MSErrorCode();
        if (resBody.getErrorCode() != ResponseBody.ErrorCode.SUCCESS.code) {
            b2BProcesslog.setProcessFlag(B2BProcessFlag.PROCESS_FLAG_FAILURE.value);
            b2BProcesslog.setProcessComment(StringUtils.left(resBody.getErrorMsg(),250));
            errorCode.setCode(MSErrorCode.FAILURE.getCode());
            errorCode.setMsg(resBody.getErrorMsg());
        } else {
            ResponseData responseData = resBody.getData();
            if(responseData != null && responseData.getResultFlag() == ResponseBody.ErrorCode.SUCCESS.code) {
                b2BProcesslog.setProcessFlag(B2BProcessFlag.PROCESS_FLAG_SUCESS.value);
                //成功则关闭失败日志
                failedProcessLogService.closeFailedLogById(failedId);
                errorCode = MSErrorCode.SUCCESS;
            }else {
                String errorStr = responseData == null ? "":responseData.getResultMsg();
                b2BProcesslog.setProcessFlag(B2BProcessFlag.PROCESS_FLAG_FAILURE.value);
                b2BProcesslog.setProcessComment(StringUtils.left(errorStr,250));
                errorCode.setCode(MSErrorCode.FAILURE.getCode());
                errorCode.setMsg(errorStr);
            }
        }
        //记录本次操作记录
        b2BProcesslogService.insert(b2BProcesslog);
        return errorCode;
    }

    /**
     * 检查参数
     * @param retryOperationData 重发实体
     * @return 返回检查数据结果
     */
    private MSErrorCode checkRetryData(B2BRetryOperationData retryOperationData) {
        if(retryOperationData == null){
            return new MSErrorCode(MSErrorCode.FAILURE.getCode(),"数据为NULL");
        }
        if(retryOperationData.getB2bInterfaceId() == null || retryOperationData.getB2bInterfaceId() == 0){
            return new MSErrorCode(MSErrorCode.FAILURE.getCode(),"接口名称为NULL");
        }
        if(StringUtils.isBlank(retryOperationData.getDataJson())){
            return new MSErrorCode(MSErrorCode.FAILURE.getCode(),"参数数据为NULL");
        }
        return MSErrorCode.SUCCESS;
    }

    /**
     * 关闭相关日志数据
     * @param retryOperationData 重发数据实体
     * @return 返回关闭结果
     */
    @PutMapping("close")
    public MSResponse closeLog(@RequestBody B2BRetryOperationData retryOperationData){
        try {
            failedProcessLogService.closeFailedLogById(retryOperationData.getProcessLogId());
            return new MSResponse<>(MSErrorCode.SUCCESS);
        }catch (Exception e){
            log.error("关闭日志报错->{}:{}",retryOperationData.getB2bOrderNo(),e.getMessage());
            MSResponse response = new MSResponse<>(MSErrorCode.FAILURE);
            response.setMsg(e.getMessage());
            return response;
        }
    }

    /**
     * 根据日志ID查询日志
     * @param id 日志id
     * @return 返回详细原始log
     */
    @GetMapping("getLog/{id}")
    public MSResponse<B2BOrderProcesslog> getLogById(@PathVariable("id") Long id){
        B2BOrderProcesslog log = failedProcessLogService.getLogById(id);
        log.setDataSource(B2BDataSourceEnum.JOYOUNG.id);
        return new MSResponse<>(MSErrorCode.SUCCESS, log);
    }
}
