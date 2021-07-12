package com.kkl.kklplus.b2b.joyoung.service;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.google.gson.Gson;
import com.kkl.kklplus.b2b.joyoung.entity.B2BMaterialSearchModel;
import com.kkl.kklplus.b2b.joyoung.entity.JoyoungMaterialDeliver;
import com.kkl.kklplus.b2b.joyoung.entity.MaterialStatusEnum;
import com.kkl.kklplus.b2b.joyoung.mapper.JoyoungMaterialDeliverMapper;
import com.kkl.kklplus.b2b.joyoung.mq.sender.B2BMaterialStatusNotifyMQSender;
import com.kkl.kklplus.b2b.joyoung.utils.DateUtils;
import com.kkl.kklplus.common.exception.MSErrorCode;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.b2b.common.B2BProcessFlag;
import com.kkl.kklplus.entity.b2b.common.ThreeTuple;
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
import java.util.List;

/**
 *配件发货消息
 * @author chenxj
 * @date 2019/08/21
 */
@Slf4j
@Service
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class JoyoungMaterialDeliverService {

    @Autowired
    private B2BProcesslogService b2BProcesslogService;

    @Autowired
    private SysLogService sysLogService;

    @Resource
    private JoyoungMaterialDeliverMapper joyoungMaterialDeliverMapper;

    @Autowired
    private B2BMaterialStatusNotifyMQSender b2BMaterialUpdateMQSender;

    @Autowired
    private JoyoungMaterialService materialService;



    /**
     * 验证数据
     * @param materialDeliver
     * @return
     */
    public MSResponse validationData(JoyoungMaterialDeliver materialDeliver) {
        MSResponse msResponse = new MSResponse(MSErrorCode.SUCCESS);
        if(materialDeliver == null){
            msResponse.setErrorCode(MSErrorCode.FAILURE);
            msResponse.setMsg("配件发货资料不能为空！");
            return msResponse;
        }
        String kklMasterNo = materialDeliver.getKklMasterNo();
        if(StringUtils.isBlank(kklMasterNo)){
            msResponse.setErrorCode(MSErrorCode.FAILURE);
            msResponse.setMsg("快可立配件单号kklMasterNo不能为空！");
            return msResponse;
        }
        String expressCompany = materialDeliver.getExpressCompany();
        if(StringUtils.isBlank(expressCompany)){
            msResponse.setErrorCode(MSErrorCode.FAILURE);
            msResponse.setMsg("快递公司名称expressCompany不能为空！");
            return msResponse;
        }
        String expressNo = materialDeliver.getExpressNo();
        if(StringUtils.isBlank(expressNo)){
            msResponse.setErrorCode(MSErrorCode.FAILURE);
            msResponse.setMsg("快递单号expressNo不能为空！");
            return msResponse;
        }
        String deliveryDateStr = materialDeliver.getDeliveryDate();
        if(StringUtils.isBlank(deliveryDateStr)){
            msResponse.setErrorCode(MSErrorCode.FAILURE);
            msResponse.setMsg("发货时间deliveryDate不能为空！");
            return msResponse;
        }else{
            try {
                materialDeliver.setDeliveryDt(DateUtils.parse(deliveryDateStr, "yyyy-MM-dd HH:mm:ss").getTime());
            }catch (Exception e){
                msResponse.setErrorCode(MSErrorCode.FAILURE);
                msResponse.setMsg("发货时间deliveryDate日期格式错误，解析失败！");
                return msResponse;
            }
        }
        return msResponse;
    }

    public MSResponse processDeliver(JoyoungMaterialDeliver materialDeliver) {
        Gson gson = new Gson();
        B2BOrderProcesslog b2BProcesslog = new B2BOrderProcesslog();
        b2BProcesslog.setInterfaceName("pushOrder/materialDeliver");
        b2BProcesslog.setProcessFlag(B2BProcessFlag.PROCESS_FLAG_FAILURE.value);
        b2BProcesslog.setProcessTime(0);
        b2BProcesslog.setCreateById(1L);
        b2BProcesslog.setUpdateById(1L);
        b2BProcesslog.preInsert();
        b2BProcesslog.setQuarter(QuarterUtils.getQuarter(b2BProcesslog.getCreateDate()));
        b2BProcesslog.setInfoJson(gson.toJson(materialDeliver));
        b2BProcesslogService.insert(b2BProcesslog);
        MSResponse msResponse = new MSResponse(MSErrorCode.SUCCESS);
        String kklMasterNo = materialDeliver.getKklMasterNo();

        ThreeTuple<Long,Long,String> materialMsg = materialService.findMaterialByKklMasterNo(kklMasterNo);
        if(materialMsg == null ){
            msResponse.setErrorCode(MSErrorCode.FAILURE);
            msResponse.setMsg(kklMasterNo + "没找到对应的配件单！");
            b2BProcesslog.setProcessComment(msResponse.getMsg());
            b2BProcesslog.preUpdate();
            b2BProcesslogService.updateProcessFlag(b2BProcesslog);
            return msResponse;
        }
        // 根据快可立配件单号，查询是否已经存在过发货信息
        List<String> expressNos = joyoungMaterialDeliverMapper.findDeliverByMaterNo(kklMasterNo);
        materialDeliver.setProcessFlag(B2BProcessFlag.PROCESS_FLAG_ACCEPT.value);
        if(expressNos != null && expressNos.size() > 0){
            materialDeliver.setProcessFlag(B2BProcessFlag.PROCESS_FLAG_SUCESS.value);
        }
        try {
            Long kklMasterId = materialMsg.getBElement();
            materialDeliver.setExpressNo(StringUtils.left(materialDeliver.getExpressNo(),50));
            materialDeliver.setKklMasterId(kklMasterId);
            materialDeliver.preInsert();
            materialDeliver.setQuarter(QuarterUtils.getQuarter(materialDeliver.getCreateDate()));
            materialDeliver.setCreateById(1L);
            joyoungMaterialDeliverMapper.insert(materialDeliver);
            b2BProcesslog.setProcessFlag(B2BProcessFlag.PROCESS_FLAG_SUCESS.value);
            b2BProcesslog.preUpdate();
            b2BProcesslogService.updateProcessFlag(b2BProcesslog);
            String remark = materialDeliver.getRemark();
            if(expressNos != null && expressNos.size() > 0){
                // 为配件单原始数据设置备注，便于查数据
                String processComment = "此配件单存在多个快递单号:" +materialDeliver.getExpressNo()+","+ StringUtils.join(expressNos,",");
                materialService.updateProessComment(materialMsg.getAElement(),StringUtils.left(processComment,250));
            }else{
                materialService.updateStatus(materialMsg.getAElement(),
                        MaterialStatusEnum.SENDED.value,materialDeliver.getCreateDt());
                String expressNo = StringUtils.left(materialDeliver.getExpressNo(),20);
                MQB2BMaterialFormMessage.B2BMaterialFormMessage message =
                        MQB2BMaterialFormMessage.B2BMaterialFormMessage.newBuilder()
                                .setDataSource(B2BDataSourceEnum.JOYOUNG.id)
                                .setId(materialDeliver.getId())
                                .setNotifyType(MQB2BMaterialFormMessage.NotifyType.Delivery)
                                .setKklMasterId(kklMasterId)
                                .setExpressCompany(materialDeliver.getExpressCompany())
                                .setExpressNo(expressNo)
                                .setDeliveryDate(materialDeliver.getDeliveryDt())
                                .setRemark(remark != null ? remark : "")
                                .setNotifyDate(materialDeliver.getCreateDt())
                                .setQuarter(materialMsg.getCElement())
                                .build();
                b2BMaterialUpdateMQSender.send(message);
            }
        }catch (Exception e){
            b2BProcesslog.setProcessComment(StringUtils.left(e.getMessage(),250));
            b2BProcesslog.preUpdate();
            b2BProcesslogService.updateProcessFlag(b2BProcesslog);
            sysLogService.insert(1L,new Gson().toJson(materialDeliver),
                    "配件单发货操作异常：" + e.getMessage(),
                    "配件单发货操作异常","applyMaterial", "POST");
            msResponse.setErrorCode(MSErrorCode.FAILURE);
            msResponse.setMsg("配件单发货操作异常！");
            return msResponse;
        }
        return msResponse;
    }

    /**
     * 更新发货消息的处理状态
     * @return
     */
    public void updateProcessFlag(Long id,int processFlag,String processComment) {
        joyoungMaterialDeliverMapper.updateProcessFlag(id,processFlag,processComment);
    }

    /**
     * 获取未处理的发货消息
     * @param materialSearchModel
     * @return
     */
    public MSResponse<MSPage<JoyoungMaterialDeliver>> getList(B2BMaterialSearchModel materialSearchModel) {
        if (materialSearchModel.getPage() != null) {
            PageHelper.startPage(materialSearchModel.getPage().getPageNo(), materialSearchModel.getPage().getPageSize());
            Page<JoyoungMaterialDeliver> materialDeliverPage = joyoungMaterialDeliverMapper.getList(materialSearchModel);
            MSPage<JoyoungMaterialDeliver> returnPage = new MSPage<>();
            returnPage.setPageNo(materialDeliverPage.getPageNum());
            returnPage.setPageSize(materialDeliverPage.getPageSize());
            returnPage.setPageCount(materialDeliverPage.getPages());
            returnPage.setRowCount((int) materialDeliverPage.getTotal());
            returnPage.setList(materialDeliverPage.getResult());
            return new MSResponse<>(MSErrorCode.SUCCESS, returnPage);
        }else {
            return null;
        }
    }
}
