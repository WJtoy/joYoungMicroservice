package com.kkl.kklplus.b2b.joyoung.mq.config;

import com.kkl.kklplus.entity.b2b.mq.B2BMQConstant;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 *催单队列
 * @author chenxj
 * @date 2019/10/12
 */
@EnableRabbit
@Configuration
public class B2BCenterReminderProcessMQConfig {

    @Bean
    public Queue b2bCenterReminderProcessQueue() {
        return new Queue(B2BMQConstant.MQ_B2BCENTER_PROCESS_B2BORDER_REMINDER_RETRY, true);
    }

    @Bean
    DirectExchange b2bCenterReminderProcessExchange() {
        return (DirectExchange) ExchangeBuilder.directExchange(B2BMQConstant.MQ_B2BCENTER_PROCESS_B2BORDER_REMINDER_RETRY).
                delayed().withArgument("x-delayed-type", "direct").build();
    }

    @Bean
    Binding bindingB2BCenterReminderProcessExchangeMessage(Queue b2bCenterReminderProcessQueue, DirectExchange b2bCenterReminderProcessExchange) {
        return BindingBuilder.bind(b2bCenterReminderProcessQueue)
                .to(b2bCenterReminderProcessExchange)
                .with(B2BMQConstant.MQ_B2BCENTER_PROCESS_B2BORDER_REMINDER_RETRY);
    }

}
