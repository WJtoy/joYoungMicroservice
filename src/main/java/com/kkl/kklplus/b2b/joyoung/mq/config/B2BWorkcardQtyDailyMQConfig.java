package com.kkl.kklplus.b2b.joyoung.mq.config;

import com.kkl.kklplus.entity.b2b.mq.B2BMQConstant;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@EnableRabbit
@Configuration
public class B2BWorkcardQtyDailyMQConfig {

    @Bean
    public Queue b2BWorkcardQtyDailyQueue() {
        return new Queue(B2BMQConstant.MQ_B2BCENTER_B2BWORKCARDQTYDAILY, true);
    }

    @Bean
    DirectExchange b2BWorkcardQtyDailyExchange() {
        return new DirectExchange(B2BMQConstant.MQ_B2BCENTER_B2BWORKCARDQTYDAILY);
    }

    @Bean
    Binding bindingB2BWorkcardQtyDailyExchangeMessage(Queue b2BWorkcardQtyDailyQueue, DirectExchange b2BWorkcardQtyDailyExchange) {
        return BindingBuilder.bind(b2BWorkcardQtyDailyQueue).to(b2BWorkcardQtyDailyExchange).
                with(B2BMQConstant.MQ_B2BCENTER_B2BWORKCARDQTYDAILY);
    }
}
