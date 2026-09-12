package br.com.vrsoftware.teste_pratico_java_backend.messaging;

import br.com.vrsoftware.teste_pratico_java_backend.dto.OrderStatusResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RabbitMQOrderStatusPublisherTest {

    private static final String SUCCESS_QUEUE = "orders.status.success.test";
    private static final String FAILURE_QUEUE = "orders.status.failure.test";

    @Mock
    private RabbitTemplate rabbitTemplate;

    private RabbitMQOrderStatusPublisher publisher;

    @BeforeEach
    void setUp() {
        publisher = new RabbitMQOrderStatusPublisher(rabbitTemplate);
        ReflectionTestUtils.setField(publisher, "successQueue", SUCCESS_QUEUE);
        ReflectionTestUtils.setField(publisher, "failureQueue", FAILURE_QUEUE);
    }

    @Test
    void publishSuccess_sendsToSuccessQueue() {
        OrderStatusResponse orderStatusResponse = OrderStatusResponse.success(UUID.randomUUID());

        publisher.publishSuccess(orderStatusResponse);

        verify(rabbitTemplate).convertAndSend(SUCCESS_QUEUE, orderStatusResponse);
        verify(rabbitTemplate, never()).convertAndSend(eq(FAILURE_QUEUE), any(OrderStatusResponse.class));
    }

    @Test
    void publishFailure_sendsToFailureQueue() {
        OrderStatusResponse orderStatusResponse = OrderStatusResponse.failure(UUID.randomUUID(), "boom");

        publisher.publishFailure(orderStatusResponse);

        verify(rabbitTemplate).convertAndSend(FAILURE_QUEUE, orderStatusResponse);
        verify(rabbitTemplate, never()).convertAndSend(eq(SUCCESS_QUEUE), any(OrderStatusResponse.class));
    }
}
