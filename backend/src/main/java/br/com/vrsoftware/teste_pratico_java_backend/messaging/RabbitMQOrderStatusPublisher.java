package br.com.vrsoftware.teste_pratico_java_backend.messaging;

import br.com.vrsoftware.teste_pratico_java_backend.dto.StatusPedido;
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
    public void publishSuccess(StatusPedido statusPedido) {
        rabbitTemplate.convertAndSend(successQueue, statusPedido);
    }

    @Override
    public void publishFailure(StatusPedido statusPedido) {
        rabbitTemplate.convertAndSend(failureQueue, statusPedido);
    }
}
