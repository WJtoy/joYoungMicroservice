package com.kkl.kklplus.b2b.joyoung.service;

import com.google.gson.Gson;
import com.kkl.kklplus.b2b.joyoung.entity.MaterialStatusEnum;
import com.kkl.kklplus.b2b.joyoung.http.command.OperationCommand;
import com.kkl.kklplus.b2b.joyoung.http.request.MaterialCloseRequestParam;
import com.kkl.kklplus.b2b.joyoung.http.response.ResponseBody;
import com.kkl.kklplus.b2b.joyoung.http.response.ResponseData;
import com.kkl.kklplus.b2b.joyoung.http.utils.OkHttpUtils;
import com.kkl.kklplus.b2b.joyoung.mapper.JoyoungMaterialCloseMapper;
import com.kkl.kklplus.common.exception.MSErrorCode;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.b2b.common.B2BProcessFlag;
import com.kkl.kklplus.entity.b2bcenter.rpt.B2BOrderProcesslog;
import com.kkl.kklplus.entity.common.material.B2BMaterial;
import com.kkl.kklplus.entity.common.material.B2BMaterialClose;
import com.kkl.kklplus.utils.QuarterUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

@Slf4j
@Service
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class JoyoungMaterialCloseService {

    @Resource
    private JoyoungMaterialCloseMapper joyoungMaterialCloseMapper;

    @Autowired
    private B2BProcesslogService b2BProcesslogService;

    @Autowired
    private JoyoungMaterialService joyoungMaterialService;

    @Autowired
    private SysLogService sysLogService;

    /**
     * 插入数据
     * @param materialClose
     */
    public void insert(B2BMaterialClose materialClose){
        materialClose.preInsert();
        materialClose.setProcessFlag(B2BProcessFlag.PROCESS_FLAG_ACCEPT.value);
        materialClose.setProcessTime(0);
        materialClose.setQuarter(QuarterUtils.getQuarter(materialClose.getCreateDate()));
        joyoungMaterialCloseMapper.insert(materialClose);
    }

    /**
     * 修改数据
     * @param materialClose
     */
    public void updateProcessFlag(B2BMaterialClose materialClose){
        materialClose.preUpdate();
        materialClose.setProcessComment(StringUtils.left(materialClose.getProcessComment(),200));
        joyoungMaterialCloseMapper.updateProcessFlag(materialClose);
    }

    public MSResponse closeMaterial(B2BMaterialClose materialClose,Long b2bMasterId) {
        MSResponse msResponse = new MSResponse();
        Gson gson = new Gson();
        //封装b2b操作记录日志
        msResponse.setErrorCode(MSErrorCode.SUCCESS);
        if(b2bMasterId == null || b2bMasterId <= 0) {
            //按快可立配件单ID返回Id
            B2BMaterial material = joyoungMaterialService.findMaterialByKklMasterId(materialClose.getKklMasterId());
            if(material == null || material.getStatus() == MaterialStatusEnum.CLOSED.value){
                log.error("配件单已关闭或不存在->{}",materialClose.toString());
                return msResponse;
            }else{
                b2bMasterId = material.getId();
            }
        }
        B2BOrderProcesslog b2BProcesslog = new B2BOrderProcesslog();
        b2BProcesslog.setInterfaceName(OperationCommand.OperationCode.MATERIALCLOSE.serviceCode);
        b2BProcesslog.setProcessFlag(B2BProcessFlag.PROCESS_FLAG_FAILURE.value);
        b2BProcesslog.setProcessTime(0);
        b2BProcesslog.setCreateById(materialClose.getCreateById());
        b2BProcesslog.setUpdateById(materialClose.getCreateById());
        b2BProcesslog.preInsert();
        b2BProcesslog.setQuarter(QuarterUtils.getQuarter(b2BProcesslog.getCreateDate()));
        //创建业务数据实体
        MaterialCloseRequestParam materialCloseRequestParam = new MaterialCloseRequestParam();
        materialCloseRequestParam.setKklMasterNo(materialClose.getKklMasterNo());
        materialCloseRequestParam.setCloseType(materialClose.getCloseType());
        materialCloseRequestParam.setRemark(materialClose.getRemark());
        //封装数据发送请求
        OperationCommand command = OperationCommand.newInstance(OperationCommand.OperationCode.MATERIALCLOSE, materialCloseRequestParam);
        //http请求接收结果
        ResponseBody<ResponseData> responseBody = OkHttpUtils.postSyncGenericNew(command, ResponseData.class);
        String infoJson = gson.toJson(materialCloseRequestParam);
        b2BProcesslog.setInfoJson(infoJson);
        try {
            //新增B2B日志记录
            b2BProcesslogService.insert(b2BProcesslog);
            b2BProcesslog.setResultJson(responseBody.getOriginalJson());
            this.insert(materialClose);
            if( responseBody.getErrorCode() != ResponseBody.ErrorCode.SUCCESS.code){
                String errorMsg = StringUtils.left(responseBody.getErrorMsg(),200);
                msResponse.setErrorCode(new MSErrorCode(responseBody.getErrorCode(),errorMsg));
                msResponse.setThirdPartyErrorCode(new MSErrorCode(responseBody.getErrorCode(),errorMsg));
                materialClose.setProcessFlag(B2BProcessFlag.PROCESS_FLAG_FAILURE.value);
                materialClose.setProcessComment(errorMsg);
                this.updateProcessFlag(materialClose);
                b2BProcesslog.setProcessFlag(B2BProcessFlag.PROCESS_FLAG_FAILURE.value);
                b2BProcesslog.setProcessComment(errorMsg);
                b2BProcesslogService.updateProcessFlag(b2BProcesslog);
                return msResponse;
            }else{
                ResponseData responseData = responseBody.getData();
                if(responseData != null && responseData.getResultFlag() == ResponseBody.ErrorCode.SUCCESS.code) {
                    materialClose.setProcessFlag(B2BProcessFlag.PROCESS_FLAG_SUCESS.value);
                    this.updateProcessFlag(materialClose);
                    b2BProcesslog.setProcessFlag(B2BProcessFlag.PROCESS_FLAG_SUCESS.value);
                    b2BProcesslogService.updateProcessFlag(b2BProcesslog);
                    //修改配件单已关闭的状态(4)
                    joyoungMaterialService.updateStatus(b2bMasterId, MaterialStatusEnum.CLOSED.value,System.currentTimeMillis());
                }else{
                    String resultMsg = responseData == null ? "":responseData.getResultMsg();
                    msResponse.setErrorCode(new MSErrorCode(responseBody.getErrorCode(),resultMsg));
                    msResponse.setThirdPartyErrorCode(new MSErrorCode(responseData.getResultFlag(),resultMsg));
                    materialClose.setProcessFlag(B2BProcessFlag.PROCESS_FLAG_FAILURE.value);
                    materialClose.setProcessComment(resultMsg);
                    this.updateProcessFlag(materialClose);
                    b2BProcesslog.setProcessFlag(B2BProcessFlag.PROCESS_FLAG_FAILURE.value);
                    b2BProcesslog.setProcessComment(resultMsg);
                    b2BProcesslogService.updateProcessFlag(b2BProcesslog);
                }
                return msResponse;
            }
        }catch (Exception e){
            msResponse.setErrorCode(MSErrorCode.FAILURE);
            msResponse.setMsg(StringUtils.left(e.getMessage(),200));
            b2BProcesslog.setProcessFlag(B2BProcessFlag.PROCESS_FLAG_FAILURE.value);
            b2BProcesslog.setProcessComment(StringUtils.left(e.getMessage(),200));
            b2BProcesslogService.updateProcessFlag(b2BProcesslog);
            String errorStr = "配件单关闭通知失败，原因是：";
            log.error("配件单关闭通知失败，原因是：{}->{}", materialClose.toString(),e.getMessage());
            sysLogService.insert(1L,infoJson,errorStr + e.getMessage(),
                    errorStr, OperationCommand.OperationCode.MATERIALCLOSE.serviceCode, "POST");
            return msResponse;
        }
    }

    /**
     * 根据快可立工单ID 关闭配件
     * @return
     */
    public MSResponse closeMaterialByOrderId(B2BMaterialClose materialClose) {
        Long orderId = materialClose.getKklOrderId();
        int closeType = materialClose.getCloseType();
        String remark = materialClose.getRemark();
        Long createById = materialClose.getCreateById();
        if(createById == null){
            log.error("创建人没参数:{}",materialClose.toString());
            createById = 1L;
        }
        List<B2BMaterial> materials = joyoungMaterialService.findMaterialsByKklOrderId(orderId);
        boolean flag = true;
        for(B2BMaterial material : materials){
            if(material.getStatus() != MaterialStatusEnum.CLOSED.value &&
                    material.getStatus() != MaterialStatusEnum.REJECT.value) {
                B2BMaterialClose closeEntity = new B2BMaterialClose();
                closeEntity.setKklMasterId(material.getKklMasterId());
                closeEntity.setKklMasterNo(material.getKklMasterNo());
                closeEntity.setCloseType(closeType);
                closeEntity.setRemark(remark);
                closeEntity.setCreateById(createById);
                MSResponse response = this.closeMaterial(closeEntity, material.getId());
                if (!MSResponse.isSuccessCode(response)) {
                    flag = false;
                }
            }
        }
        if(flag){
            return new MSResponse(MSErrorCode.SUCCESS);
        }else{
            return new MSResponse(new MSErrorCode(MSErrorCode.FAILURE.getCode(),"配件单关闭失败！"));
        }
    }
}
