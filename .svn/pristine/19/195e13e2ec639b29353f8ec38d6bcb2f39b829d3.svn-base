package com.kkl.kklplus.b2b.joyoung.service;

import com.google.gson.Gson;
import com.kkl.kklplus.b2b.joyoung.entity.JoyoungConsultingOrder;
import com.kkl.kklplus.b2b.joyoung.entity.JoyoungOrderInfo;
import com.kkl.kklplus.b2b.joyoung.mapper.JoyoungConsultingOrderMapper;
import com.kkl.kklplus.b2b.joyoung.mq.sender.B2BCenterOrderComplainSender;
import com.kkl.kklplus.b2b.joyoung.mq.sender.B2BCenterReminderMQSender;
import com.kkl.kklplus.common.exception.MSErrorCode;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.b2b.common.B2BProcessFlag;
import com.kkl.kklplus.entity.b2bcenter.md.B2BDataSourceEnum;
import com.kkl.kklplus.entity.b2bcenter.pb.MQB2BOrderComplainMessage;
import com.kkl.kklplus.entity.b2bcenter.pb.MQB2BOrderReminderMessage;
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
 *九阳咨询单
 * @author chenxj
 * @date 2019/09/26
 */
@Slf4j
@Service
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class JoyoungConsultingOrderService {


    @Autowired
    private B2BProcesslogService b2BProcesslogService;

    @Autowired
    private SysLogService sysLogService;

    @Autowired
    private JoyoungOrderInfoService orderInfoService;

    @Autowired
    private B2BCenterReminderMQSender reminderMQSender;

    @Autowired
    private B2BCenterOrderComplainSender complainMQSender;

    @Resource
    private JoyoungConsultingOrderMapper joyoungConsultingOrderMapper;

    /**
     * 验证咨询单实体
     * @param consultingOrder
     * @return
     */
    public MSResponse validationData(JoyoungConsultingOrder consultingOrder) {
        MSResponse msResponse = new MSResponse(MSErrorCode.SUCCESS);
        if(consultingOrder == null){
            msResponse.setErrorCode(MSErrorCode.FAILURE);
            msResponse.setMsg("咨询单资料不能为空！");
            return msResponse;
        }
        String orderNo = consultingOrder.getOrderNo();
        if(StringUtils.isBlank(orderNo)){
            msResponse.setErrorCode(MSErrorCode.FAILURE);
            msResponse.setMsg("九阳工单号orderNo不能为空！");
            return msResponse;
        }
        String consultingNo = consultingOrder.getConsultingNo();
        if(StringUtils.isBlank(consultingNo)){
            msResponse.setErrorCode(MSErrorCode.FAILURE);
            msResponse.setMsg("九阳咨询单号consultingNo不能为空！");
            return msResponse;
        }
        Integer consultingType = consultingOrder.getConsultingType();
        if(consultingType == null || consultingType < 0 || consultingType > 1){
            msResponse.setErrorCode(MSErrorCode.FAILURE);
            msResponse.setMsg("咨询单类型consultingType不能为空或不为0/1！");
            return msResponse;
        }
        String content = consultingOrder.getContent();
        if(StringUtils.isBlank(content)){
            msResponse.setErrorCode(MSErrorCode.FAILURE);
            msResponse.setMsg("咨询内容content不能为空！");
            return msResponse;
        }
        return msResponse;
    }

    /**
     * 新建咨询单
     * @param consultingOrder
     * @return
     */
    public MSResponse newConsultingOrder(JoyoungConsultingOrder consultingOrder) {
        Gson gson = new Gson();
        B2BOrderProcesslog b2BProcesslog = new B2BOrderProcesslog();
        b2BProcesslog.setInterfaceName("pushOrder/newConsultingOrder");
        b2BProcesslog.setProcessFlag(B2BProcessFlag.PROCESS_FLAG_FAILURE.value);
        b2BProcesslog.setProcessTime(0);
        b2BProcesslog.setCreateById(1L);
        b2BProcesslog.setUpdateById(1L);
        b2BProcesslog.preInsert();
        b2BProcesslog.setQuarter(QuarterUtils.getQuarter(b2BProcesslog.getCreateDate()));
        b2BProcesslog.setInfoJson(gson.toJson(consultingOrder));
        b2BProcesslogService.insert(b2BProcesslog);
        MSResponse msResponse = new MSResponse(MSErrorCode.SUCCESS);
        String orderNo = consultingOrder.getOrderNo();
        JoyoungOrderInfo orderInfo = orderInfoService.findOrderByOrderNo(orderNo);
        if(orderInfo == null ){
            msResponse.setErrorCode(MSErrorCode.FAILURE);
            msResponse.setMsg(orderNo + "工单不存在！");
            b2BProcesslog.setProcessFlag(B2BProcessFlag.PROCESS_FLAG_SUCESS.value);
            b2BProcesslog.setProcessComment(msResponse.getMsg());
            b2BProcesslog.preUpdate();
            b2BProcesslogService.updateProcessFlag(b2BProcesslog);
            return msResponse;
        }
        Long kklOrderId = orderInfo.getKklOrderId();
        if(kklOrderId == null || kklOrderId ==0){
            msResponse.setErrorCode(MSErrorCode.FAILURE);
            msResponse.setMsg(orderNo + "工单未接单！");
            b2BProcesslog.setProcessFlag(B2BProcessFlag.PROCESS_FLAG_SUCESS.value);
            b2BProcesslog.setProcessComment(msResponse.getMsg());
            b2BProcesslog.preUpdate();
            b2BProcesslogService.updateProcessFlag(b2BProcesslog);
            return msResponse;
        }
        String consultingNo = consultingOrder.getConsultingNo();
        JoyoungConsultingOrder consultingMsg = this.findConsultingOrderByConsultingNo(consultingNo);
        if(consultingMsg != null ){
            msResponse.setErrorCode(MSErrorCode.FAILURE);
            msResponse.setMsg(consultingNo + "咨询单已经存在！");
            b2BProcesslog.setProcessFlag(B2BProcessFlag.PROCESS_FLAG_SUCESS.value);
            b2BProcesslog.setProcessComment(msResponse.getMsg());
            b2BProcesslog.preUpdate();
            b2BProcesslogService.updateProcessFlag(b2BProcesslog);
            return msResponse;
        }
        try {
            consultingOrder.setKklOrderId(kklOrderId);
            consultingOrder.preInsert();
            consultingOrder.setQuarter(QuarterUtils.getQuarter(consultingOrder.getCreateDate()));
            consultingOrder.setCreateById(1L);
            this.insert(consultingOrder);
            b2BProcesslog.setProcessFlag(B2BProcessFlag.PROCESS_FLAG_SUCESS.value);
            b2BProcesslog.preUpdate();
            b2BProcesslogService.updateProcessFlag(b2BProcesslog);
            int consultingType =consultingOrder.getConsultingType();
            // 判断咨询单类型发送对应的单据队列
            if(consultingType == 0){
                senderReminderMQ(consultingOrder);
            }else if (consultingType == 1){
                senderComplainMQ(consultingOrder,orderInfo);
            }else{
                log.error("咨询单类型异常{}",new Gson().toJson(consultingOrder));
            }
        }catch (Exception e){
            b2BProcesslog.setProcessComment(StringUtils.left(e.getMessage(),250));
            b2BProcesslog.preUpdate();
            b2BProcesslogService.updateProcessFlag(b2BProcesslog);
            sysLogService.insert(1L,new Gson().toJson(consultingOrder),
                    "咨询单创建异常：" + e.getMessage(),
                    "咨询单创建异常","pushOrder/newConsultingOrder", "POST");
            msResponse.setErrorCode(MSErrorCode.FAILURE);
            msResponse.setMsg("咨询单创建异常！");
            return msResponse;
        }
        return msResponse;
    }

    /**
     * 发送创建投诉单
     * @param consultingOrder
     */
    private void senderComplainMQ(JoyoungConsultingOrder consultingOrder,JoyoungOrderInfo orderInfo) {
        MQB2BOrderComplainMessage.B2BOrderComplainMessage complainMessage = MQB2BOrderComplainMessage.B2BOrderComplainMessage.newBuilder()
                .setB2BComplainId(consultingOrder.getId())
                .setB2BComplainNo(consultingOrder.getConsultingNo())
                .setOrderId(consultingOrder.getKklOrderId())
                .setCreateAt(consultingOrder.getCreateDt())
                .setContent(consultingOrder.getContent())
                .setB2BOrderNo(consultingOrder.getOrderNo())
                .setDataSource(B2BDataSourceEnum.JOYOUNG.id).build();
        complainMQSender.send(complainMessage);
    }
    /**
     * 发送创建催单
     * @param consultingOrder
     */
    private void senderReminderMQ(JoyoungConsultingOrder consultingOrder) {
        MQB2BOrderReminderMessage.B2BOrderReminderMessage message = MQB2BOrderReminderMessage.B2BOrderReminderMessage.newBuilder()
                .setB2BReminderId(consultingOrder.getId())
                .setB2BReminderNo(consultingOrder.getConsultingNo())
                .setDataSource(B2BDataSourceEnum.JOYOUNG.id)
                .setContent(consultingOrder.getContent())
                .setCreateDate(consultingOrder.getCreateDt())
                .setKklOrderId(consultingOrder.getKklOrderId())
                .setB2BQuarter(consultingOrder.getQuarter()).build();
        reminderMQSender.send(message);
    }

    private void insert(JoyoungConsultingOrder consultingOrder) {
        joyoungConsultingOrderMapper.insert(consultingOrder);
    }

    public JoyoungConsultingOrder findConsultingOrderByConsultingNo(String consultingNo) {
        return joyoungConsultingOrderMapper.findConsultingOrderByConsultingNo(consultingNo);
    }

    public void updateProcessFlag(Long id,Long kklConsultingId, int processFlag, String processComment) {
        joyoungConsultingOrderMapper.updateProcessFlag(id,kklConsultingId,processFlag,processComment);
    }
}
