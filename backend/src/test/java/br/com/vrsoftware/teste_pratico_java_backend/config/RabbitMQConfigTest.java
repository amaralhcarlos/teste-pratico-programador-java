package br.com.vrsoftware.teste_pratico_java_backend.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

class RabbitMQConfigTest {

    private static final String QUEUE = "orders.inbound.test";
    private static final String DLQ = "orders.inbound.test.dlq";
    private static final String DLX = "orders.inbound.test.dlx";
    private static final String SUCCESS_QUEUE = "orders.status.success.test";
    private static final String FAILURE_QUEUE = "orders.status.failure.test";

    private RabbitMQConfig config;

    @BeforeEach
    void setUp() {
        config = new RabbitMQConfig();
        ReflectionTestUtils.setField(config, "queue", QUEUE);
        ReflectionTestUtils.setField(config, "dlq", DLQ);
        ReflectionTestUtils.setField(config, "dlx", DLX);
        ReflectionTestUtils.setField(config, "successQueue", SUCCESS_QUEUE);
        ReflectionTestUtils.setField(config, "failureQueue", FAILURE_QUEUE);
    }

    @Test
    void orderQueue_hasDeadLetterArguments() {
        Queue queue = config.orderQueue();

        assertThat(queue.getName()).isEqualTo(QUEUE);
        assertThat(queue.isDurable()).isTrue();
        assertThat(queue.getArguments())
                .containsEntry("x-dead-letter-exchange", DLX)
                .containsEntry("x-dead-letter-routing-key", DLQ);
    }

    @Test
    void orderDlqBinding_bindsDlqToDlxWithDlqRoutingKey() {
        Binding binding = config.orderDlqBinding();

        assertThat(binding.getDestination()).isEqualTo(DLQ);
        assertThat(binding.getExchange()).isEqualTo(DLX);
        assertThat(binding.getRoutingKey()).isEqualTo(DLQ);
    }

    @Test
    void messageConverter_returnsJacksonJsonMessageConverter() {
        MessageConverter converter = config.messageConverter();

        assertThat(converter).isInstanceOf(JacksonJsonMessageConverter.class);
    }
}
