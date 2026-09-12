package br.com.vrsoftware.teste_pratico_java_backend.messaging;

import br.com.vrsoftware.teste_pratico_java_backend.domain.Order;
import br.com.vrsoftware.teste_pratico_java_backend.domain.OrderStatus;
import br.com.vrsoftware.teste_pratico_java_backend.dto.OrderStatusResponse;
import br.com.vrsoftware.teste_pratico_java_backend.service.OrderStatusStore;
import com.rabbitmq.client.Channel;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class OrderConsumerTest {

    private static final long DELIVERY_TAG = 42L;

    @Mock
    private OrderStatusStore statusStore;

    @Mock
    private OrderStatusPublisher statusPublisher;

    @Mock
    private Channel channel;

    private OrderConsumer consumer;
    private Order order;
    private UUID orderId;

    @BeforeEach
    void setUp() {
        consumer = new OrderConsumer(statusStore, statusPublisher);
        orderId = UUID.randomUUID();
        order = new Order(orderId, "Teclado", 2, LocalDateTime.now());

        ReflectionTestUtils.setField(consumer, "minProcessingMillis", 0L);
        ReflectionTestUtils.setField(consumer, "maxProcessingMillis", 0L);
    }

    @AfterEach
    void clearInterruptFlag() {
        Thread.interrupted();
    }

    @Test
    void handle_onSuccess_acksAndPublishesSuccessStatus() throws Exception {
        ReflectionTestUtils.setField(consumer, "failureProbability", 0.0);

        consumer.handle(order, channel, DELIVERY_TAG);

        InOrder inOrder = inOrder(statusStore);
        inOrder.verify(statusStore).update(orderId, OrderStatus.PROCESSING);
        inOrder.verify(statusStore).update(orderId, OrderStatus.SUCCESS);

        verify(channel).basicAck(DELIVERY_TAG, false);
        verify(channel, never()).basicNack(anyLong(), anyBoolean(), anyBoolean());

        ArgumentCaptor<OrderStatusResponse> captor = ArgumentCaptor.forClass(OrderStatusResponse.class);
        verify(statusPublisher).publishSuccess(captor.capture());
        assertThat(captor.getValue().orderId()).isEqualTo(orderId);
        assertThat(captor.getValue().status()).isEqualTo(OrderStatus.SUCCESS);
        verify(statusPublisher, never()).publishFailure(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void handle_onSimulatedFailure_nacksAndPublishesFailureStatus() throws Exception {
        ReflectionTestUtils.setField(consumer, "failureProbability", 1.0);

        consumer.handle(order, channel, DELIVERY_TAG);

        InOrder inOrder = inOrder(statusStore);
        inOrder.verify(statusStore).update(orderId, OrderStatus.PROCESSING);
        inOrder.verify(statusStore).update(orderId, OrderStatus.FAILURE);

        verify(channel).basicNack(DELIVERY_TAG, false, false);
        verify(channel, never()).basicAck(anyLong(), anyBoolean());

        ArgumentCaptor<OrderStatusResponse> captor = ArgumentCaptor.forClass(OrderStatusResponse.class);
        verify(statusPublisher).publishFailure(captor.capture());
        assertThat(captor.getValue().orderId()).isEqualTo(orderId);
        assertThat(captor.getValue().status()).isEqualTo(OrderStatus.FAILURE);
        assertThat(captor.getValue().errorMessage()).contains(orderId.toString());
        verify(statusPublisher, never()).publishSuccess(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void handle_whenInterrupted_nacksWithoutPublishingStatus() throws Exception {
        Thread.currentThread().interrupt();

        consumer.handle(order, channel, DELIVERY_TAG);

        verify(statusStore).update(orderId, OrderStatus.PROCESSING);
        verify(statusStore, never()).update(orderId, OrderStatus.SUCCESS);
        verify(statusStore, never()).update(orderId, OrderStatus.FAILURE);

        verify(channel).basicNack(DELIVERY_TAG, false, false);
        verify(channel, never()).basicAck(anyLong(), anyBoolean());

        verifyNoInteractions(statusPublisher);

        assertThat(Thread.currentThread().isInterrupted()).isTrue();
    }
}
