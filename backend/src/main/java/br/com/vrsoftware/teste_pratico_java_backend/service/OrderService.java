package br.com.vrsoftware.teste_pratico_java_backend.service;

import br.com.vrsoftware.teste_pratico_java_backend.domain.Order;
import br.com.vrsoftware.teste_pratico_java_backend.domain.OrderStatus;
import br.com.vrsoftware.teste_pratico_java_backend.dto.OrderRequest;
import br.com.vrsoftware.teste_pratico_java_backend.dto.OrderResponse;
import br.com.vrsoftware.teste_pratico_java_backend.messaging.OrderPublisher;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class OrderService {

    private static final Logger log =
            LogManager.getLogger(OrderService.class);
    private final OrderPublisher publisher;
    private final OrderStatusStore statusStore;

    public OrderService(OrderPublisher publisher, OrderStatusStore statusStore) {
        this.publisher = publisher;
        this.statusStore = statusStore;
    }

    public OrderResponse create(OrderRequest request) {

        Order order = new Order(
                UUID.randomUUID(),
                request.product(),
                request.quantity(),
                request.creationDate()
        );


        log.info(
                "Publishing order to RabbitMQ | order={}",
                order
        );

        publisher.publish(order);

        statusStore.update(order.getId(), OrderStatus.RECEIVED);

        log.info(
                "Order successfully published to RabbitMQ | orderId={}",
                order.getId()
        );

        return new OrderResponse(order.getId());
    }

    public Optional<OrderStatus> getStatus(UUID orderId) {
        return statusStore.find(orderId);
    }
}
