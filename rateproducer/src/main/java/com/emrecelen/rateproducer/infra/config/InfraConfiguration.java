package com.emrecelen.rateproducer.infra.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class InfraConfiguration {

    @Bean
    public MessageConverter jacksonMessageConverter() {
        return new JacksonJsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(
            ConnectionFactory cf,
            MessageConverter converter
    ) {
        RabbitTemplate t = new RabbitTemplate(cf);
        t.setMessageConverter(converter);
        return t;
    }

    @Bean
    public DirectExchange rateExchange() {
        return new DirectExchange("rate.exchange", true, false);
    }

    @Bean
    public Queue rateInputQueue() {
        return new Queue("rate.input.queue", true);
    }

    @Bean
    public Binding rateBinding() {
        return BindingBuilder
                .bind(rateInputQueue())
                .to(rateExchange())
                .with("rate.update");
    }
}
