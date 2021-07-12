package com.kkl.kklplus.b2b.joyoung.mq.config;

import com.kkl.kklplus.entity.b2b.mq.B2BMQConstant;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 *配件消息队列
 * @author chenxj
 * @date 2019/08/20
 */
@EnableRabbit
@Configuration
public class B2BMaterialStatusNotifyMQConfig {

    @Bean
    public Queue b2bMaterialStatusNotifyQueue() {
        return new Queue(B2BMQConstant.MQ_B2B_MATERIAL_STATUS_NOTIFY_RETRY, true);
    }

    @Bean
    DirectExchange b2bMaterialStatusNotifyExchange() {
        return (DirectExchange) ExchangeBuilder.directExchange(B2BMQConstant.MQ_B2B_MATERIAL_STATUS_NOTIFY_RETRY)
                .delayed().withArgument("x-delayed-type", "direct").build();
    }

    @Bean
    Binding bindingB2BMaterialStatusNotifyExchangeMessage(Queue b2bMaterialStatusNotifyQueue, DirectExchange b2bMaterialStatusNotifyExchange) {
        return BindingBuilder.bind(b2bMaterialStatusNotifyQueue)
                .to(b2bMaterialStatusNotifyExchange)
                .with(B2BMQConstant.MQ_B2B_MATERIAL_STATUS_NOTIFY_RETRY);
    }
}
