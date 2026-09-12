package br.com.vrsoftware.teste_pratico_java_backend.service;

import br.com.vrsoftware.teste_pratico_java_backend.domain.Order;
import br.com.vrsoftware.teste_pratico_java_backend.domain.OrderStatus;
import br.com.vrsoftware.teste_pratico_java_backend.dto.OrderRequest;
import br.com.vrsoftware.teste_pratico_java_backend.dto.OrderResponse;
import br.com.vrsoftware.teste_pratico_java_backend.messaging.OrderPublisher;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderPublisher publisher;

    @Mock
    private OrderStatusStore statusStore;

    private OrderService orderService;

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        orderService = new OrderService(publisher, statusStore);
    }

    @Test
    void create_generatesFreshServerSideIdIgnoringRequestId() {
        UUID requestId = UUID.randomUUID();
        OrderRequest request = new OrderRequest(requestId, "Playstation 2", 2, LocalDateTime.now());

        OrderResponse response = orderService.create(request);

        ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
        InOrder inOrder = inOrder(publisher, statusStore);
        inOrder.verify(publisher).publish(captor.capture());
        Order publishedOrder = captor.getValue();

        inOrder.verify(statusStore).update(publishedOrder.getId(), OrderStatus.RECEIVED);

        assertThat(publishedOrder.getId()).isNotNull();
        assertThat(publishedOrder.getId()).isNotEqualTo(requestId);
        assertThat(publishedOrder.getProduct()).isEqualTo(request.product());
        assertThat(publishedOrder.getQuantity()).isEqualTo(request.quantity());
        assertThat(publishedOrder.getCreationDate()).isEqualTo(request.creationDate());

        assertThat(response.id()).isEqualTo(publishedOrder.getId());
    }

    @Test
    void getStatus_delegatesToStatusStore_whenFound() {
        UUID orderId = UUID.randomUUID();
        when(statusStore.find(orderId)).thenReturn(Optional.of(OrderStatus.SUCCESS));

        Optional<OrderStatus> result = orderService.getStatus(orderId);

        assertThat(result).contains(OrderStatus.SUCCESS);
    }

    @Test
    void getStatus_delegatesToStatusStore_whenNotFound() {
        UUID orderId = UUID.randomUUID();
        when(statusStore.find(orderId)).thenReturn(Optional.empty());

        Optional<OrderStatus> result = orderService.getStatus(orderId);

        assertThat(result).isEmpty();
    }
}
