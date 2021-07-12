package com.kkl.kklplus.b2b.joyoung.service;

import com.google.gson.Gson;
import com.kkl.kklplus.b2b.joyoung.entity.JoyoungConsultingOrderProcess;
import com.kkl.kklplus.b2b.joyoung.http.command.OperationCommand;
import com.kkl.kklplus.b2b.joyoung.http.request.ConsultingOrderProcessRequestParam;
import com.kkl.kklplus.b2b.joyoung.http.response.ResponseBody;
import com.kkl.kklplus.b2b.joyoung.http.response.ResponseData;
import com.kkl.kklplus.b2b.joyoung.http.utils.OkHttpUtils;
import com.kkl.kklplus.b2b.joyoung.mapper.JoyoungConsultingOrderProcessMapper;
import com.kkl.kklplus.common.exception.MSErrorCode;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.b2b.common.B2BProcessFlag;
import com.kkl.kklplus.entity.b2bcenter.rpt.B2BOrderProcesslog;
import com.kkl.kklplus.utils.QuarterUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

/**
 *咨询单处理
 * @author chenxj
 * @date 2019/09/29
 */
@Slf4j
@Service
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class JoyoungConsultingOrderProcessService {

    @Autowired
    private B2BProcesslogService b2BProcesslogService;

    @Autowired
    private SysLogService sysLogService;

    @Resource
    private JoyoungConsultingOrderProcessMapper joyoungConsultingOrderProcessMapper;



    public MSResponse process(JoyoungConsultingOrderProcess consultingOrderProcess) {
        MSResponse msResponse = new MSResponse();
        if(consultingOrderProcess == null){
            msResponse.setErrorCode(MSErrorCode.FAILURE);
            msResponse.setMsg("信息不能为空！");
            return msResponse;
        }
        Gson gson = new Gson();
        //封装b2b操作记录日志
        msResponse.setErrorCode(MSErrorCode.SUCCESS);
        B2BOrderProcesslog b2BProcesslog = new B2BOrderProcesslog();
        b2BProcesslog.setInterfaceName(OperationCommand.OperationCode.MATERIAL.serviceCode);
        b2BProcesslog.setProcessFlag(B2BProcessFlag.PROCESS_FLAG_FAILURE.value);
        b2BProcesslog.setProcessTime(0);
        b2BProcesslog.setCreateById(consultingOrderProcess.getCreateById());
        b2BProcesslog.setUpdateById(consultingOrderProcess.getCreateById());
        b2BProcesslog.preInsert();
        b2BProcesslog.setQuarter(QuarterUtils.getQuarter(b2BProcesslog.getCreateDate()));
        consultingOrderProcess.preInsert();
        consultingOrderProcess.setQuarter(QuarterUtils.getQuarter(consultingOrderProcess.getCreateDate()));
        //创建业务数据实体
        ConsultingOrderProcessRequestParam processRequestParam = new ConsultingOrderProcessRequestParam();
        processRequestParam.setConsultingNo(consultingOrderProcess.getConsultingNo());
        processRequestParam.setType(consultingOrderProcess.getType());
        processRequestParam.setProcessContent(consultingOrderProcess.getProcessContent());
        //封装数据发送请求
        OperationCommand command = OperationCommand.newInstance(OperationCommand.OperationCode.CONSULTINGORDERPROCESS, processRequestParam);
        //http请求接收结果
        ResponseBody<ResponseData> responseBody = OkHttpUtils.postSyncGenericNew(command, ResponseData.class);
        String infoJson = gson.toJson(processRequestParam);
        b2BProcesslog.setInfoJson(infoJson);
        try {
            //新增B2B日志记录
            b2BProcesslogService.insert(b2BProcesslog);
            b2BProcesslog.setResultJson(responseBody.getOriginalJson());
            this.insert(consultingOrderProcess);
            ResponseData responseData = responseBody.getData();
            if( responseBody.getErrorCode() == ResponseBody.ErrorCode.SUCCESS.code
                    && responseData != null && responseData.getResultFlag() == ResponseBody.ErrorCode.SUCCESS.code ){
                consultingOrderProcess.setProcessFlag(B2BProcessFlag.PROCESS_FLAG_SUCESS.value);
                this.updateProcessFlag(consultingOrderProcess);
                b2BProcesslog.setProcessFlag(B2BProcessFlag.PROCESS_FLAG_SUCESS.value);
                b2BProcesslogService.updateProcessFlag(b2BProcesslog);
                return msResponse;
            }else{
                String errorMsg = StringUtils.left(responseBody.getErrorMsg(),200);
                msResponse.setThirdPartyErrorCode(new MSErrorCode(responseBody.getErrorCode(),errorMsg));
                //改编码错误，则需要重试
                if(responseBody.getErrorCode() >= ResponseBody.ErrorCode.REQUEST_INVOCATION_FAILURE.code){
                    msResponse.setErrorCode(new MSErrorCode(responseBody.getErrorCode(),errorMsg));
                }
                if(responseBody.getErrorCode() == ResponseBody.ErrorCode.SUCCESS.code){
                    errorMsg = responseData == null ? "":responseData.getResultMsg();
                    msResponse.setThirdPartyErrorCode(new MSErrorCode(responseData.getResultFlag(),errorMsg));
                }
                //记录错误日志
                consultingOrderProcess.setProcessFlag(B2BProcessFlag.PROCESS_FLAG_FAILURE.value);
                consultingOrderProcess.setProcessComment(errorMsg);
                this.updateProcessFlag(consultingOrderProcess);
                b2BProcesslog.setProcessFlag(B2BProcessFlag.PROCESS_FLAG_FAILURE.value);
                b2BProcesslog.setProcessComment(errorMsg);
                b2BProcesslogService.updateProcessFlag(b2BProcesslog);

                return msResponse;
            }
        }catch (Exception e){
            msResponse.setErrorCode(MSErrorCode.FAILURE);
            msResponse.setMsg(StringUtils.left(e.getMessage(),200));
            b2BProcesslog.setProcessFlag(B2BProcessFlag.PROCESS_FLAG_FAILURE.value);
            b2BProcesslog.setProcessComment(StringUtils.left(e.getMessage(),200));
            b2BProcesslogService.updateProcessFlag(b2BProcesslog);
            String errorStr = "咨询单处理失败，原因是：";
            log.error(errorStr, e.getMessage());
            sysLogService.insert(1L,infoJson,errorStr + e.getMessage(),
                    errorStr, OperationCommand.OperationCode.MATERIAL.serviceCode, "POST");
            return msResponse;
        }
    }

    private void updateProcessFlag(JoyoungConsultingOrderProcess consultingOrderProcess) {
        consultingOrderProcess.preUpdate();
        consultingOrderProcess.setProcessComment(StringUtils.left(consultingOrderProcess.getProcessComment(),200));
        joyoungConsultingOrderProcessMapper.updateProcessFlag(consultingOrderProcess);
    }

    private void insert(JoyoungConsultingOrderProcess consultingOrderProcess) {
        consultingOrderProcess.preInsert();
        consultingOrderProcess.setQuarter(QuarterUtils.getQuarter(consultingOrderProcess.getCreateDate()));
        joyoungConsultingOrderProcessMapper.insert(consultingOrderProcess);
    }
}
