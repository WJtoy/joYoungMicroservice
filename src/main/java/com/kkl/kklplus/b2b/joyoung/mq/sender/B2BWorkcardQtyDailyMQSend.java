package com.kkl.kklplus.b2b.joyoung.mq.sender;

import com.google.gson.Gson;
import com.kkl.kklplus.b2b.joyoung.service.SysLogService;
import com.kkl.kklplus.entity.b2b.mq.B2BMQConstant;
import com.kkl.kklplus.entity.b2b.pb.MQB2BWorkcardQtyDailyMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.support.CorrelationData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class B2BWorkcardQtyDailyMQSend implements RabbitTemplate.ConfirmCallback {

    @Autowired
    private RabbitTemplate kklRabbitTemplate;

    @Autowired
    private RetryTemplate kklRabbitRetryTemplate;

    @Autowired
    private SysLogService sysLogService;

    public void send(MQB2BWorkcardQtyDailyMessage.B2BWorkcardQtyDailyMessage message) {
        try {
            kklRabbitRetryTemplate.execute((RetryCallback<Object, Exception>) context -> {
                context.setAttribute(B2BMQConstant.MQ_B2BCENTER_B2BWORKCARDQTYDAILY, message);
                kklRabbitTemplate.convertAndSend(
                        B2BMQConstant.MQ_B2BCENTER_B2BWORKCARDQTYDAILY,
                        B2BMQConstant.MQ_B2BCENTER_B2BWORKCARDQTYDAILY,
                        message.toByteArray(),
                        new CorrelationData());
                return null;
            }, context -> {
                Object msgObj = context.getAttribute(B2BMQConstant.RETRY_CONTEXT_ATTRIBUTE_KEY_MESSAGE);
                MQB2BWorkcardQtyDailyMessage.B2BWorkcardQtyDailyMessage msg =
                        MQB2BWorkcardQtyDailyMessage.B2BWorkcardQtyDailyMessage.parseFrom((byte[])msgObj);
                Throwable throwable = context.getLastThrowable();
                sysLogService.insert(1L,new Gson().toJson(msg),
                        "工单数量统计队列发送失败：" + throwable.getLocalizedMessage() +"；错误原因：" + throwable.getCause().toString(),
                        "工单数量统计队列发送失败", B2BMQConstant.MQ_B2BCENTER_B2BWORKCARDQTYDAILY, null);
                log.error("工单数量统计队列发送失败", throwable.getLocalizedMessage(), msg);
                return null;
            });
        }catch (Exception e) {
            e.printStackTrace();
            log.error(e.getLocalizedMessage());
        }
    }

    @Override
    public void confirm(CorrelationData correlationData, boolean ack, String cause) {

    }
}
