package com.kkl.kklplus.b2b.joyoung.controller;

import com.google.gson.Gson;
import com.kkl.kklplus.b2b.joyoung.http.command.OperationCommand;
import com.kkl.kklplus.b2b.joyoung.http.request.MaterialRequestParam;
import com.kkl.kklplus.b2b.joyoung.http.response.ResponseBody;
import com.kkl.kklplus.b2b.joyoung.http.response.ResponseData;
import com.kkl.kklplus.b2b.joyoung.http.utils.OkHttpUtils;
import com.kkl.kklplus.b2b.joyoung.service.B2BProcesslogService;
import com.kkl.kklplus.b2b.joyoung.service.JoyoungMaterialCloseService;
import com.kkl.kklplus.b2b.joyoung.service.JoyoungMaterialService;
import com.kkl.kklplus.b2b.joyoung.service.SysLogService;
import com.kkl.kklplus.common.exception.MSErrorCode;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.b2b.common.B2BProcessFlag;
import com.kkl.kklplus.entity.b2bcenter.rpt.B2BOrderProcesslog;
import com.kkl.kklplus.entity.common.material.B2BMaterial;
import com.kkl.kklplus.entity.common.material.B2BMaterialClose;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;


@Slf4j
@RestController
@RequestMapping("/material")
public class JoyoungMaterialController {

    @Autowired
    private B2BProcesslogService b2BProcesslogService;

    @Autowired
    private SysLogService sysLogService;

    @Autowired
    private JoyoungMaterialService joyoungMaterialService;

    @Autowired
    private JoyoungMaterialCloseService joyoungMaterialCloseService;

    @PostMapping("/newMaterial")
    public MSResponse material(@RequestBody B2BMaterial material){
        MSResponse msResponse = validationData(material);
        if(!MSResponse.isSuccessCode(msResponse)){
            log.error("配件单验证失败:{}->{}",msResponse.getMsg(),material.toString());
            return msResponse;
        }
        Gson gson = new Gson();
        //封装b2b操作记录日志
        msResponse.setErrorCode(MSErrorCode.SUCCESS);

        B2BOrderProcesslog b2BProcesslog = new B2BOrderProcesslog();
        b2BProcesslog.setInterfaceName(OperationCommand.OperationCode.MATERIAL.serviceCode);
        b2BProcesslog.setProcessFlag(B2BProcessFlag.PROCESS_FLAG_FAILURE.value);
        b2BProcesslog.setProcessTime(0);
        b2BProcesslog.setCreateById(material.getCreateById());
        b2BProcesslog.setUpdateById(material.getCreateById());
        b2BProcesslog.preInsert();
        b2BProcesslog.setQuarter(material.getQuarter());
        //创建业务数据实体
        MaterialRequestParam materialRequestParam = new MaterialRequestParam();
        if(material.getReturnFlag() != null){
            material.setReturnFlag(material.getReturnFlag()+1);
            materialRequestParam.setReturnFlag(material.getReturnFlag());
        }
        if(material.getReturnFlag() == null){
            material.setReturnFlag(0);
        }
        materialRequestParam.setKklMasterNo(material.getKklMasterNo());
        materialRequestParam.setKklOrderNo(material.getKklOrderNo());
        materialRequestParam.setOrderNo(material.getOrderNo());
        materialRequestParam.setUserName(material.getUserName());
        materialRequestParam.setUserMobile(material.getUserMobile());
        materialRequestParam.setUserAddress(material.getUserAddress());
        if(material.getPics() != null) {
            materialRequestParam.setPics(material.getPics());
        }
        materialRequestParam.setMasterCount(material.getMasterCount());
        materialRequestParam.setDescription(material.getDescription());
        List<B2BMaterial.MaterItem> items = material.getMaterItems();
        List<MaterialRequestParam.MaterItem> jItems = new ArrayList<>();
        for(B2BMaterial.MaterItem item : items){
            MaterialRequestParam.MaterItem jItme = new MaterialRequestParam.MaterItem();
            jItme.setMaterialDesc(item.getMaterialDesc());
            jItme.setProductName(item.getProductName());
            jItme.setQty(item.getQty());
            jItems.add(jItme);
        }
        materialRequestParam.setMaterItems(jItems);
        //封装数据发送请求
        OperationCommand command = OperationCommand.newInstance(OperationCommand.OperationCode.MATERIAL, materialRequestParam);
        //http请求接收结果
        ResponseBody<ResponseData> responseBody = OkHttpUtils.postSyncGenericNew(command, ResponseData.class);
        String infoJson = gson.toJson(materialRequestParam);
        b2BProcesslog.setInfoJson(infoJson);
        try {
            //新增B2B日志记录
            b2BProcesslogService.insert(b2BProcesslog);
            b2BProcesslog.setResultJson(responseBody.getOriginalJson());

            //按快可立配件单ID返回Id(有就修改数据,没有则新增数据)
            Long kklMasterId = joyoungMaterialService.returnIdByKklMasterId(material.getKklMasterId());
            if(kklMasterId == null){
                //新增配件单信息操作
                joyoungMaterialService.insert(material);
            }else {
                material.setId(kklMasterId);
                //修改配件单信息操作
                joyoungMaterialService.updateMaterialInfo(material);
            }
            if( responseBody.getErrorCode() != ResponseBody.ErrorCode.SUCCESS.code){
                String errorMsg = StringUtils.left(responseBody.getErrorMsg(),200);
                msResponse.setErrorCode(new MSErrorCode(responseBody.getErrorCode(),errorMsg));
                msResponse.setThirdPartyErrorCode(new MSErrorCode(responseBody.getErrorCode(),errorMsg));
                material.setProcessFlag(B2BProcessFlag.PROCESS_FLAG_FAILURE.value);
                material.setProcessComment(errorMsg);
                joyoungMaterialService.updateProcessFlag(material);
                b2BProcesslog.setProcessFlag(B2BProcessFlag.PROCESS_FLAG_FAILURE.value);
                b2BProcesslog.setProcessComment(errorMsg);
                b2BProcesslogService.updateProcessFlag(b2BProcesslog);
                return msResponse;
            }else{
                ResponseData responseData = responseBody.getData();
                if(responseData != null && responseData.getResultFlag() == ResponseBody.ErrorCode.SUCCESS.code) {
                    material.setProcessFlag(B2BProcessFlag.PROCESS_FLAG_SUCESS.value);
                    joyoungMaterialService.updateProcessFlag(material);
                    b2BProcesslog.setProcessFlag(B2BProcessFlag.PROCESS_FLAG_SUCESS.value);
                    b2BProcesslogService.updateProcessFlag(b2BProcesslog);
                }else{
                    String resultMsg = responseData == null ? "":responseData.getResultMsg();
                    msResponse.setErrorCode(new MSErrorCode(responseBody.getErrorCode(),resultMsg));
                    msResponse.setThirdPartyErrorCode(new MSErrorCode(responseData.getResultFlag(),resultMsg));
                    material.setProcessFlag(B2BProcessFlag.PROCESS_FLAG_FAILURE.value);
                    material.setProcessComment(resultMsg);
                    joyoungMaterialService.updateProcessFlag(material);
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
            String errorStr = "创建配件单失败，原因是：";
            log.error(errorStr, e.getMessage());
            sysLogService.insert(1L,infoJson,errorStr + e.getMessage(),
                    errorStr, OperationCommand.OperationCode.MATERIAL.serviceCode, "POST");
            return msResponse;
        }
    }

    private MSResponse validationData(B2BMaterial material) {
        MSResponse msResponse = new MSResponse(MSErrorCode.SUCCESS);
        if(material == null){
            msResponse.setErrorCode(MSErrorCode.FAILURE);
            msResponse.setMsg("配件单不能为空！");
            return msResponse;
        }
        Long kklOrderId = material.getKklOrderId();
        if(kklOrderId == null || kklOrderId == 0){
            msResponse.setErrorCode(MSErrorCode.FAILURE);
            msResponse.setMsg("快可立工单ID kklOrderId不能为空！");
            return msResponse;
        }
        Long kklMasterId = material.getKklMasterId();
        if(kklMasterId == null || kklMasterId == 0){
            msResponse.setErrorCode(MSErrorCode.FAILURE);
            msResponse.setMsg("快可立配件单ID kklMasterId不能为空！");
            return msResponse;
        }
        String kklOrderNo = material.getKklOrderNo();
        if(StringUtils.isBlank(kklOrderNo)){
            msResponse.setErrorCode(MSErrorCode.FAILURE);
            msResponse.setMsg("快可立工单号 kklOrderNo不能为空！");
            return msResponse;
        }
        String kklMasterNo = material.getKklMasterNo();
        if(StringUtils.isBlank(kklMasterNo)){
            msResponse.setErrorCode(MSErrorCode.FAILURE);
            msResponse.setMsg("快可立配件单号kklMasterNo不能为空！");
            return msResponse;
        }
        String orderNo = material.getOrderNo();
        if(StringUtils.isBlank(orderNo)){
            msResponse.setErrorCode(MSErrorCode.FAILURE);
            msResponse.setMsg("九阳单号orderNo不能为空！");
            return msResponse;
        }
        String userName = material.getUserName();
        if(StringUtils.isBlank(userName)){
            msResponse.setErrorCode(MSErrorCode.FAILURE);
            msResponse.setMsg("用户名userName不能为空！");
            return msResponse;
        }
        String userMobile = material.getUserMobile();
        if(StringUtils.isBlank(userMobile)){
            msResponse.setErrorCode(MSErrorCode.FAILURE);
            msResponse.setMsg("用户电话userMobile不能为空！");
            return msResponse;
        }
        String userAddress = material.getUserAddress();
        if(StringUtils.isBlank(userAddress)){
            msResponse.setErrorCode(MSErrorCode.FAILURE);
            msResponse.setMsg("用户地址userAddress不能为空！");
            return msResponse;
        }
        List<B2BMaterial.MaterItem> materItems = material.getMaterItems();
        if(materItems == null || materItems.size() == 0){
            msResponse.setErrorCode(MSErrorCode.FAILURE);
            msResponse.setMsg("配件项目不能为空！");
            return msResponse;
        }
        for(B2BMaterial.MaterItem materItem : materItems){
            int qty = materItem.getQty();
            if(qty == 0){
                msResponse.setErrorCode(MSErrorCode.FAILURE);
                msResponse.setMsg("配件数量不能为0！");
                return msResponse;
            }
            String materialDesc = materItem.getMaterialDesc();
            if(StringUtils.isBlank(materialDesc)){
                msResponse.setErrorCode(MSErrorCode.FAILURE);
                msResponse.setMsg("配件描述materialDesc不能为空！");
                return msResponse;
            }
            String productName = materItem.getProductName();
            if(StringUtils.isBlank(productName)){
                msResponse.setErrorCode(MSErrorCode.FAILURE);
                msResponse.setMsg("产品名称productName不能为空！");
                return msResponse;
            }
        }
        return msResponse;
    }

    /**
     * 配件关闭
     * @param materialClose
     * @return
     */
    @PostMapping("/close")
    public MSResponse materialClose(@RequestBody B2BMaterialClose materialClose){
        MSResponse msResponse = new MSResponse();
        if(materialClose == null){
            msResponse.setErrorCode(MSErrorCode.FAILURE);
            msResponse.setMsg("信息不能为空！");
            return msResponse;
        }
        return joyoungMaterialCloseService.closeMaterial(materialClose,0L);
    }
    /**
     * 配件关闭 根据快可立工单ID关闭多个配件单
     * @param materialClose
     * @return
     */
    @PostMapping("/closeByOrderId")
    public MSResponse closeByOrderId(@RequestBody B2BMaterialClose materialClose){
        MSResponse msResponse = new MSResponse();
        if(materialClose == null){
            msResponse.setErrorCode(MSErrorCode.FAILURE);
            msResponse.setMsg("信息不能为空！");
            return msResponse;
        }
        return joyoungMaterialCloseService.closeMaterialByOrderId(materialClose);
    }

}
