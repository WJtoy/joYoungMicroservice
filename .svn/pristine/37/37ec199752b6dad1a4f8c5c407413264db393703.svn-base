package com.kkl.kklplus.b2b.joyoung.mq.config;

import com.kkl.kklplus.entity.b2b.mq.B2BMQConstant;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 *投诉单处理队列
 * @author chenxj
 * @date 2019/10/12
 */
@EnableRabbit
@Configuration
public class B2BCenterComplainProcessMQConfig {

    @Bean
    public Queue b2bCenterComplainProcessQueue() {
        return new Queue(B2BMQConstant.MQ_B2BCENTER_COMPLAIN_PROCESS_CALLBACK_DELAY, true);
    }

    @Bean
    DirectExchange b2bCenterComplainProcessExchange() {
        return (DirectExchange) ExchangeBuilder.directExchange(B2BMQConstant.MQ_B2BCENTER_COMPLAIN_PROCESS_CALLBACK_DELAY).
                delayed().withArgument("x-delayed-type", "direct").build();
    }

    @Bean
    Binding bindingB2BCenterComplainProcessExchangeMessage(Queue b2bCenterComplainProcessQueue, DirectExchange b2bCenterComplainProcessExchange) {
        return BindingBuilder.bind(b2bCenterComplainProcessQueue)
                .to(b2bCenterComplainProcessExchange)
                .with(B2BMQConstant.MQ_B2BCENTER_COMPLAIN_PROCESS_CALLBACK_DELAY);
    }

}
