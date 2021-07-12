package com.kkl.kklplus.b2b.joyoung.service;

import com.google.gson.Gson;
import com.kkl.kklplus.b2b.joyoung.entity.JoyoungConsultingOrder;
import com.kkl.kklplus.b2b.joyoung.entity.JoyoungConsultingOrderLog;
import com.kkl.kklplus.b2b.joyoung.mapper.JoyoungConsultingOrderLogMapper;
import com.kkl.kklplus.b2b.joyoung.mq.sender.B2BCenterComplainProcessMQSender;
import com.kkl.kklplus.b2b.joyoung.mq.sender.B2BCenterReminderProcessMQSender;
import com.kkl.kklplus.common.exception.MSErrorCode;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.b2b.common.B2BProcessFlag;
import com.kkl.kklplus.entity.b2bcenter.md.B2BDataSourceEnum;
import com.kkl.kklplus.entity.b2bcenter.pb.MQB2BOrderComplainProcessMessage;
import com.kkl.kklplus.entity.b2bcenter.pb.MQB2BOrderReminderProcessMessage;
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
 *咨询单跟踪日志
 * @author chenxj
 * @date 2019/09/27
 */
@Slf4j
@Service
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class JoyoungConsultingOrderLogService {

    @Autowired
    private B2BProcesslogService b2BProcesslogService;

    @Autowired
    private SysLogService sysLogService;

    @Autowired
    private JoyoungConsultingOrderService consultingOrderService;

    @Autowired
    private B2BCenterComplainProcessMQSender complainProcessMQSender;

    @Autowired
    private B2BCenterReminderProcessMQSender reminderProcessMQSender;

    @Resource
    private JoyoungConsultingOrderLogMapper joyoungConsultingOrderLogMapper;



    /**
     * 验证咨询日志实体
     * @param consultingOrderLog
     * @return
     */
    public MSResponse validationData(JoyoungConsultingOrderLog consultingOrderLog) {
        MSResponse msResponse = new MSResponse(MSErrorCode.SUCCESS);
        if(consultingOrderLog == null){
            msResponse.setErrorCode(MSErrorCode.FAILURE);
            msResponse.setMsg("咨询单跟踪日志资料不能为空！");
            return msResponse;
        }
        String consultingNo = consultingOrderLog.getConsultingNo();
        if(StringUtils.isBlank(consultingNo)){
            msResponse.setErrorCode(MSErrorCode.FAILURE);
            msResponse.setMsg("九阳咨询单号consultingNo不能为空！");
            return msResponse;
        }
        Integer type = consultingOrderLog.getType();
        if(!(type == 10 || type == 11 || type == 20)){
            msResponse.setErrorCode(MSErrorCode.FAILURE);
            msResponse.setMsg("咨询单日志类型type不能为空或不为10/11/20！");
            return msResponse;
        }
        String content = consultingOrderLog.getContent();
        if(StringUtils.isBlank(content)){
            msResponse.setErrorCode(MSErrorCode.FAILURE);
            msResponse.setMsg("咨询内容content不能为空！");
            return msResponse;
        }
        return msResponse;
    }

    public MSResponse consultingLog(JoyoungConsultingOrderLog consultingOrderLog) {
        Gson gson = new Gson();
        B2BOrderProcesslog b2BProcesslog = new B2BOrderProcesslog();
        b2BProcesslog.setInterfaceName("pushOrder/consultingLog");
        b2BProcesslog.setProcessFlag(B2BProcessFlag.PROCESS_FLAG_FAILURE.value);
        b2BProcesslog.setProcessTime(0);
        b2BProcesslog.setCreateById(1L);
        b2BProcesslog.setUpdateById(1L);
        b2BProcesslog.preInsert();
        b2BProcesslog.setQuarter(QuarterUtils.getQuarter(b2BProcesslog.getCreateDate()));
        b2BProcesslog.setInfoJson(gson.toJson(consultingOrderLog));
        b2BProcesslogService.insert(b2BProcesslog);
        MSResponse msResponse = new MSResponse(MSErrorCode.SUCCESS);
        String consultingNo = consultingOrderLog.getConsultingNo();

        JoyoungConsultingOrder consultingMsg = consultingOrderService.findConsultingOrderByConsultingNo(consultingNo);
        if(consultingMsg == null ){
            msResponse.setErrorCode(MSErrorCode.FAILURE);
            msResponse.setMsg(consultingNo + "咨询单不存在！");
            b2BProcesslog.setProcessFlag(B2BProcessFlag.PROCESS_FLAG_SUCESS.value);
            b2BProcesslog.setProcessComment(msResponse.getMsg());
            b2BProcesslog.preUpdate();
            b2BProcesslogService.updateProcessFlag(b2BProcesslog);
            return msResponse;
        }
        try {
            consultingOrderLog.preInsert();
            consultingOrderLog.setQuarter(QuarterUtils.getQuarter(consultingOrderLog.getCreateDate()));
            consultingOrderLog.setCreateById(1L);
            this.insert(consultingOrderLog);
            b2BProcesslog.setProcessFlag(B2BProcessFlag.PROCESS_FLAG_SUCESS.value);
            b2BProcesslog.preUpdate();
            b2BProcesslogService.updateProcessFlag(b2BProcesslog);
            int consultingType = consultingMsg.getConsultingType();
            Long kklConsultingId = consultingMsg.getKklConsultingId();
            if(kklConsultingId != null && kklConsultingId > 0){
                // 判断咨询单类型发送对应的处理队列
                if(consultingType == 0){
                    senderReminderProcessMQ(consultingOrderLog,consultingMsg.getKklOrderId(),consultingMsg.getKklConsultingId());
                }else if (consultingType == 1){
                    senderComplainProcessMQ(consultingOrderLog,consultingMsg.getKklOrderId(),consultingMsg.getKklConsultingId());
                }else{
                    log.error("咨询单类型异常{}",new Gson().toJson(consultingOrderLog));
                }
            }
        }catch (Exception e){
            b2BProcesslog.setProcessComment(StringUtils.left(e.getMessage(),250));
            b2BProcesslog.preUpdate();
            b2BProcesslogService.updateProcessFlag(b2BProcesslog);
            sysLogService.insert(1L,new Gson().toJson(consultingOrderLog),
                    "咨询单跟踪日志异常：" + e.getMessage(),
                    "咨询单跟踪日志异常","pushOrder/consultingLog", "POST");
            msResponse.setErrorCode(MSErrorCode.FAILURE);
            msResponse.setMsg("咨询单跟踪日志异常！");
            return msResponse;
        }
        return msResponse;
    }

    /**
     * 发送投诉单处理队列
     * @param consultingOrderLog    日志
     * @param kklOrderId    快可立工单ID
     * @param kklConsultingId   快可立投诉单ID
     */
    private void senderComplainProcessMQ(JoyoungConsultingOrderLog consultingOrderLog, Long kklOrderId, Long kklConsultingId) {
        String content = consultingOrderLog.getContent();
        MQB2BOrderComplainProcessMessage.B2BOrderComplainProcessMessage message = MQB2BOrderComplainProcessMessage.B2BOrderComplainProcessMessage.newBuilder()
                .setMessageId(consultingOrderLog.getId())
                .setDataSource(B2BDataSourceEnum.JOYOUNG.id)
                .setKklOrderId(kklOrderId)
                .setKklComplainId(kklConsultingId)
                .setContent(content == null ? "" : content)
                .setOperationType(consultingOrderLog.getType())
                .setCreateAt(consultingOrderLog.getCreateDt()).build();
        complainProcessMQSender.send(message);
    }

    /**
     * 发送催单处理队列
     * @param consultingOrderLog    日志
     * @param kklOrderId    快可立工单ID
     * @param kklConsultingId   快可立投诉单ID
     */
    private void senderReminderProcessMQ(JoyoungConsultingOrderLog consultingOrderLog, Long kklOrderId, Long kklConsultingId) {
        String content = consultingOrderLog.getContent();
        MQB2BOrderReminderProcessMessage.B2BOrderReminderProcessMessage message = MQB2BOrderReminderProcessMessage.B2BOrderReminderProcessMessage.newBuilder()
                .setMessageId(consultingOrderLog.getId())
                .setDataSource(B2BDataSourceEnum.JOYOUNG.id)
                .setKklOrderId(kklOrderId)
                .setKklReminderId(kklConsultingId)
                .setContent(content == null ? "" : content)
                .setOperationType(consultingOrderLog.getType())
                .setCreateDate(consultingOrderLog.getCreateDt()).build();
        reminderProcessMQSender.send(message);
    }

    private void insert(JoyoungConsultingOrderLog consultingOrderLog) {
        joyoungConsultingOrderLogMapper.insert(consultingOrderLog);
    }
}
