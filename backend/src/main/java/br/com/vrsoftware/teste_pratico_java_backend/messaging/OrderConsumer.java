package br.com.vrsoftware.teste_pratico_java_backend.messaging;

import br.com.vrsoftware.teste_pratico_java_backend.domain.Order;
import br.com.vrsoftware.teste_pratico_java_backend.domain.OrderStatus;
import br.com.vrsoftware.teste_pratico_java_backend.dto.OrderStatusResponse;
import br.com.vrsoftware.teste_pratico_java_backend.exception.OrderProcessingException;
import br.com.vrsoftware.teste_pratico_java_backend.service.OrderStatusStore;
import com.rabbitmq.client.Channel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Component
public class OrderConsumer {

    private static final Logger log = LogManager.getLogger(OrderConsumer.class);

    private double failureProbability = 0.2;
    private long minProcessingMillis = 1000L;
    private long maxProcessingMillis = 3000L;

    private final OrderStatusStore statusStore;
    private final OrderStatusPublisher statusPublisher;

    public OrderConsumer(OrderStatusStore statusStore, OrderStatusPublisher statusPublisher) {
        this.statusStore = statusStore;
        this.statusPublisher = statusPublisher;
    }

    @RabbitListener(queues = "${app.rabbitmq.queue}")
    public void handle(Order order,
                        Channel channel,
                        @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) throws IOException {

        UUID orderId = order.getId();
        log.info("Order received: {}", orderId);

        statusStore.update(orderId, OrderStatus.PROCESSING);
        log.info("Starting order processing: {}", orderId);

        try {
            simulateProcessing(orderId);

            statusStore.update(orderId, OrderStatus.SUCCESS);

            channel.basicAck(deliveryTag, false);

            statusPublisher.publishSuccess(OrderStatusResponse.success(orderId));
            log.info("Order processed successfully: {}", orderId);
            log.info("Success status published: {}", orderId);

        } catch (OrderProcessingException ex) {
            log.error("Failed to process order: {} | reason={}", orderId, ex.getMessage());

            statusStore.update(orderId, OrderStatus.FAILURE);

            statusPublisher.publishFailure(OrderStatusResponse.failure(orderId, ex.getMessage()));
            log.info("Failure status published: {}", orderId);

            channel.basicNack(deliveryTag, false, false);
            log.warn("Rejecting order and forwarding to DLQ: {}", orderId);

        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            log.error("Processing of order {} interrupted", orderId, ex);
            channel.basicNack(deliveryTag, false, false);
        }
    }

    private void simulateProcessing(UUID orderId) throws InterruptedException {
        long delayMillis = ThreadLocalRandom.current()
                .nextLong(minProcessingMillis, maxProcessingMillis + 1);

        log.info("Processing order: {} | processingTimeMs={}", orderId, delayMillis);
        Thread.sleep(delayMillis);

        double chance = ThreadLocalRandom.current().nextDouble();
        if (chance < failureProbability) {
            throw new OrderProcessingException(
                    "Falha simulada no processamento do pedido " + orderId);
        }
    }
}
