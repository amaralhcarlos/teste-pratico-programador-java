package br.com.vrsoftware.teste_pratico_java_backend.messaging;

import br.com.vrsoftware.teste_pratico_java_backend.domain.Order;
import br.com.vrsoftware.teste_pratico_java_backend.domain.OrderStatus;
import br.com.vrsoftware.teste_pratico_java_backend.dto.StatusPedido;
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

    private static final double FAILURE_PROBABILITY = 0.2;
    private static final long MIN_PROCESSING_MILLIS = 1000L;
    private static final long MAX_PROCESSING_MILLIS = 3000L;

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
        log.info("Pedido recebido: {}", orderId);

        statusStore.update(orderId, OrderStatus.PROCESSANDO);
        log.info("Iniciando processamento do pedido: {}", orderId);

        try {
            simulateProcessing(orderId);

            statusStore.update(orderId, OrderStatus.SUCESSO);

            channel.basicAck(deliveryTag, false);

            statusPublisher.publishSuccess(StatusPedido.sucesso(orderId));
            log.info("Pedido processado com sucesso: {}", orderId);
            log.info("Status de sucesso publicado: {}", orderId);

        } catch (OrderProcessingException ex) {
            log.error("Falha ao processar pedido: {} | motivo={}", orderId, ex.getMessage());

            statusStore.update(orderId, OrderStatus.FALHA);

            statusPublisher.publishFailure(StatusPedido.falha(orderId, ex.getMessage()));
            log.info("Status de falha publicado: {}", orderId);

            channel.basicNack(deliveryTag, false, false);
            log.warn("Rejeitando pedido e encaminhando para DLQ: {}", orderId);

        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            log.error("Processamento do pedido {} interrompido", orderId, ex);
            channel.basicNack(deliveryTag, false, false);
        }
    }

    private void simulateProcessing(UUID orderId) throws InterruptedException {
        long delayMillis = ThreadLocalRandom.current()
                .nextLong(MIN_PROCESSING_MILLIS, MAX_PROCESSING_MILLIS + 1);

        log.info("Processando pedido: {} | tempoProcessamentoMs={}", orderId, delayMillis);
        Thread.sleep(delayMillis);

        double chance = ThreadLocalRandom.current().nextDouble();
        if (chance < FAILURE_PROBABILITY) {
            throw new OrderProcessingException(
                    "Falha simulada no processamento do pedido " + orderId);
        }
    }
}
