package com.kkl.kklplus.b2b.joyoung.service;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.google.gson.Gson;
import com.kkl.kklplus.b2b.joyoung.entity.B2BMaterialSearchModel;
import com.kkl.kklplus.b2b.joyoung.entity.JoyoungMaterialApply;
import com.kkl.kklplus.b2b.joyoung.entity.MaterialStatusEnum;
import com.kkl.kklplus.b2b.joyoung.mapper.JoyoungMaterialApplyMapper;
import com.kkl.kklplus.b2b.joyoung.mq.sender.B2BMaterialStatusNotifyMQSender;
import com.kkl.kklplus.common.exception.MSErrorCode;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.b2b.common.B2BProcessFlag;
import com.kkl.kklplus.entity.b2b.common.ThreeTuple;
import com.kkl.kklplus.entity.b2b.common.TwoTuple;
import com.kkl.kklplus.entity.b2bcenter.md.B2BDataSourceEnum;
import com.kkl.kklplus.entity.b2bcenter.rpt.B2BOrderProcesslog;
import com.kkl.kklplus.entity.common.MSPage;
import com.kkl.kklplus.entity.common.mq.message.MQB2BMaterialFormMessage;
import com.kkl.kklplus.utils.QuarterUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

/**
 *审核配件操作业务实体
 * @author chenxj
 * @date 2019/08/19
 */
@Slf4j
@Service
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class JoyoungMaterialApplyService {

    @Autowired
    private B2BProcesslogService b2BProcesslogService;

    @Autowired
    private SysLogService sysLogService;

    @Resource
    private JoyoungMaterialApplyMapper joyoungMaterialApplyMapper;

    @Autowired
    private B2BMaterialStatusNotifyMQSender b2BMaterialUpdateMQSender;

    @Autowired
    private JoyoungMaterialService materialService;

    /**
     * 检查审核配件单实体
     */
    public MSResponse validationData(JoyoungMaterialApply joyoungMaterialApply) {
        MSResponse msResponse = new MSResponse(MSErrorCode.SUCCESS);
        if(joyoungMaterialApply == null){
            msResponse.setErrorCode(MSErrorCode.FAILURE);
            msResponse.setMsg("审核资料不能为空！");
            return msResponse;
        }
        String kklMasterNo = joyoungMaterialApply.getKklMasterNo();
        if(StringUtils.isBlank(kklMasterNo)){
            msResponse.setErrorCode(MSErrorCode.FAILURE);
            msResponse.setMsg("快可立配件单号kklMasterNo不能为空！");
            return msResponse;
        }
        Integer auditStatus = joyoungMaterialApply.getAuditStatus();
        if(auditStatus == null || auditStatus > 1 || auditStatus < 0){
            msResponse.setErrorCode(MSErrorCode.FAILURE);
            msResponse.setMsg("审核状态auditStatus不能为空或值不为0或1！");
            return msResponse;
        }
        Integer returnFlag = joyoungMaterialApply.getReturnFlag();
        if(returnFlag == null){
            joyoungMaterialApply.setReturnFlag(0);
        }
        return msResponse;
    }

    /**
     * 处理审核结果
     * @param materialApply
     * @return
     */
    public MSResponse processApply(JoyoungMaterialApply materialApply) {
        Gson gson = new Gson();
        B2BOrderProcesslog b2BProcesslog = new B2BOrderProcesslog();
        b2BProcesslog.setInterfaceName("pushOrder/applyMaterial");
        b2BProcesslog.setProcessFlag(B2BProcessFlag.PROCESS_FLAG_FAILURE.value);
        b2BProcesslog.setProcessTime(0);
        b2BProcesslog.setCreateById(1L);
        b2BProcesslog.setUpdateById(1L);
        b2BProcesslog.preInsert();
        b2BProcesslog.setQuarter(QuarterUtils.getQuarter(b2BProcesslog.getCreateDate()));
        b2BProcesslog.setInfoJson(gson.toJson(materialApply));
        b2BProcesslogService.insert(b2BProcesslog);
        MSResponse msResponse = new MSResponse(MSErrorCode.SUCCESS);
        String kklMasterNo = materialApply.getKklMasterNo();
        ThreeTuple<Long,Long,String> materialMsg = materialService.findMaterialByKklMasterNo(kklMasterNo);
        if(materialMsg == null){
            msResponse.setErrorCode(MSErrorCode.FAILURE);
            msResponse.setMsg(kklMasterNo + "没找到对应的配件单！");
            b2BProcesslog.setProcessComment(msResponse.getMsg());
            b2BProcesslog.preUpdate();
            b2BProcesslogService.updateProcessFlag(b2BProcesslog);
            return msResponse;
        }
        Long id = joyoungMaterialApplyMapper.findMaterialApply(kklMasterNo);
        if(id != null && id > 0){
            msResponse.setErrorCode(MSErrorCode.FAILURE);
            msResponse.setMsg(kklMasterNo + "配件单已经审核了，不能重复审核！");
            b2BProcesslog.setProcessComment(msResponse.getMsg());
            b2BProcesslog.preUpdate();
            b2BProcesslogService.updateProcessFlag(b2BProcesslog);
            return msResponse;
        }
        try {
            Long kklMasterId = materialMsg.getBElement();
            materialApply.setKklMasterId(kklMasterId);
            materialApply.preInsert();
            materialApply.setQuarter(QuarterUtils.getQuarter(materialApply.getCreateDate()));
            materialApply.setCreateById(1L);
            joyoungMaterialApplyMapper.insert(materialApply);
            int staus = MaterialStatusEnum.APPROVED.value;
            //判断审核状态是否通过，0为不通过，status设置为驳回
            if(materialApply.getAuditStatus() == 0){
                staus = MaterialStatusEnum.REJECT.value;
            }
            //修改配件单状态
            materialService.updateStatus(materialMsg.getAElement(),staus,
                    materialApply.getCreateDt());
            b2BProcesslog.setProcessFlag(B2BProcessFlag.PROCESS_FLAG_SUCESS.value);
            b2BProcesslog.preUpdate();
            b2BProcesslogService.updateProcessFlag(b2BProcesslog);
            String remark = materialApply.getRemark();
            MQB2BMaterialFormMessage.B2BMaterialFormMessage message =
                    MQB2BMaterialFormMessage.B2BMaterialFormMessage.newBuilder()
                            .setDataSource(B2BDataSourceEnum.JOYOUNG.id)
                            .setId(materialApply.getId())
                            .setNotifyType(MQB2BMaterialFormMessage.NotifyType.Audit)
                            .setKklMasterId(kklMasterId)
                            .setAuditStatus(materialApply.getAuditStatus())
                            .setRemark(remark != null ? remark : "")
                            .setNotifyDate(System.currentTimeMillis())
                            .setQuarter(materialMsg.getCElement())
                            .build();
            b2BMaterialUpdateMQSender.send(message);
        }catch (Exception e){
            b2BProcesslog.setProcessComment(StringUtils.left(e.getMessage(),250));
            b2BProcesslog.preUpdate();
            b2BProcesslogService.updateProcessFlag(b2BProcesslog);
            sysLogService.insert(1L,new Gson().toJson(materialApply),
                    "审核配件单操作异常：" + e.getMessage(),
                    "审核配件单操作异常","applyMaterial", "POST");
            msResponse.setErrorCode(MSErrorCode.FAILURE);
            msResponse.setMsg("审核配件单操作异常！");
            return msResponse;
        }
        return msResponse;
    }

    /**
     * 更新审核消息的处理状态
     * @param id
     * @return
     */
    public void updateProcessFlag(Long id,int processFlag,String processComment) {
        joyoungMaterialApplyMapper.updateProcessFlag(id,processFlag,processComment);
    }

    public MSResponse<MSPage<JoyoungMaterialApply>> getList(B2BMaterialSearchModel materialSearchModel) {
        if (materialSearchModel.getPage() != null) {
            PageHelper.startPage(materialSearchModel.getPage().getPageNo(), materialSearchModel.getPage().getPageSize());
            Page<JoyoungMaterialApply> materialApplyPage = joyoungMaterialApplyMapper.getList(materialSearchModel);
            MSPage<JoyoungMaterialApply> returnPage = new MSPage<>();
            returnPage.setPageNo(materialApplyPage.getPageNum());
            returnPage.setPageSize(materialApplyPage.getPageSize());
            returnPage.setPageCount(materialApplyPage.getPages());
            returnPage.setRowCount((int) materialApplyPage.getTotal());
            returnPage.setList(materialApplyPage.getResult());
            return new MSResponse<>(MSErrorCode.SUCCESS, returnPage);
        }else {
            return null;
        }
    }
}
