package br.com.vrsoftware.teste_pratico_java_backend.messaging;

import br.com.vrsoftware.teste_pratico_java_backend.domain.Order;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class RabbitMQOrderPublisher implements OrderPublisher {

    private final RabbitTemplate rabbitTemplate;

    @Value("${app.rabbitmq.queue}")
    private String queue;

    public RabbitMQOrderPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    public void publish(Order order) {
        rabbitTemplate.convertAndSend(
                queue,
                order
        );
    }
}

