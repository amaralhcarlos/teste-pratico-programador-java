package br.com.vrsoftware.teste_pratico_java_backend.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Value("${app.rabbitmq.queue}")
    private String queue;

    @Value("${app.rabbitmq.dlq}")
    private String dlq;

    @Value("${app.rabbitmq.dlx}")
    private String dlx;

    @Value("${app.rabbitmq.success-queue}")
    private String successQueue;

    @Value("${app.rabbitmq.failure-queue}")
    private String failureQueue;

    @Bean
    public Queue orderQueue() {
        return QueueBuilder.durable(queue)
                .withArgument("x-dead-letter-exchange", dlx)
                .withArgument("x-dead-letter-routing-key", dlq)
                .build();
    }

    @Bean
    public Queue orderDlq() {
        return QueueBuilder.durable(dlq).build();
    }

    @Bean
    public DirectExchange orderDlx() {
        return new DirectExchange(dlx);
    }

    @Bean
    public Binding orderDlqBinding() {
        return BindingBuilder.bind(orderDlq())
                .to(orderDlx())
                .with(dlq);
    }

    @Bean
    public Queue orderStatusSuccessQueue() {
        return QueueBuilder.durable(successQueue).build();
    }

    @Bean
    public Queue orderStatusFailureQueue() {
        return QueueBuilder.durable(failureQueue).build();
    }

    @Bean
    public MessageConverter messageConverter() {
        return new JacksonJsonMessageConverter();
    }
}
