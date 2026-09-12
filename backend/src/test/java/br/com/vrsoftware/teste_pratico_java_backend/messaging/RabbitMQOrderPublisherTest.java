package br.com.vrsoftware.teste_pratico_java_backend.messaging;

import br.com.vrsoftware.teste_pratico_java_backend.domain.Order;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@ExtendWith(MockitoExtension.class)
class RabbitMQOrderPublisherTest {

    private static final String QUEUE = "orders.inbound.test";

    @Mock
    private RabbitTemplate rabbitTemplate;

    private RabbitMQOrderPublisher publisher;

    @BeforeEach
    void setUp() {
        publisher = new RabbitMQOrderPublisher(rabbitTemplate);
        ReflectionTestUtils.setField(publisher, "queue", QUEUE);
    }

    @Test
    void publish_sendsOrderToConfiguredQueue() {
        Order order = new Order(UUID.randomUUID(), "Teclado", 2, LocalDateTime.now());

        publisher.publish(order);

        verify(rabbitTemplate).convertAndSend(QUEUE, order);
        verifyNoMoreInteractions(rabbitTemplate);
    }
}
