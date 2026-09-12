package br.com.vrsoftware.teste_pratico_java_backend.messaging;

import br.com.vrsoftware.teste_pratico_java_backend.dto.OrderStatusResponse;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class RabbitMQOrderStatusPublisher implements OrderStatusPublisher {

    private final RabbitTemplate rabbitTemplate;

    @Value("${app.rabbitmq.success-queue}")
    private String successQueue;

    @Value("${app.rabbitmq.failure-queue}")
    private String failureQueue;

    public RabbitMQOrderStatusPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    public void publishSuccess(OrderStatusResponse orderStatusResponse) {
        rabbitTemplate.convertAndSend(successQueue, orderStatusResponse);
    }

    @Override
    public void publishFailure(OrderStatusResponse orderStatusResponse) {
        rabbitTemplate.convertAndSend(failureQueue, orderStatusResponse);
    }
}
