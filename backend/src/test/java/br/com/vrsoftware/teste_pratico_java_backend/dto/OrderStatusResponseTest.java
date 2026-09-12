package br.com.vrsoftware.teste_pratico_java_backend.dto;

import br.com.vrsoftware.teste_pratico_java_backend.domain.OrderStatus;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

class OrderStatusResponseTest {

    private final JsonMapper mapper = JsonMapper.builder().build();

    @Test
    void of_leavesProcessedAtAndErrorMessageNull() {
        UUID id = UUID.randomUUID();

        OrderStatusResponse status = OrderStatusResponse.of(id, OrderStatus.PROCESSING);

        assertThat(status.orderId()).isEqualTo(id);
        assertThat(status.status()).isEqualTo(OrderStatus.PROCESSING);
        assertThat(status.processedAt()).isNull();
        assertThat(status.errorMessage()).isNull();
    }

    @Test
    void success_setsStatusSuccessAndProcessedAt() {
        UUID id = UUID.randomUUID();

        OrderStatusResponse status = OrderStatusResponse.success(id);

        assertThat(status.status()).isEqualTo(OrderStatus.SUCCESS);
        assertThat(status.errorMessage()).isNull();
        assertThat(status.processedAt()).isCloseTo(LocalDateTime.now(), within(5, ChronoUnit.SECONDS));
    }

    @Test
    void failure_setsStatusFailureAndErrorMessage() {
        UUID id = UUID.randomUUID();

        OrderStatusResponse status = OrderStatusResponse.failure(id, "Falha simulada");

        assertThat(status.status()).isEqualTo(OrderStatus.FAILURE);
        assertThat(status.errorMessage()).isEqualTo("Falha simulada");
        assertThat(status.processedAt()).isNull();
    }

    @Test
    void serialization_omitsNullFields() {
        String json = mapper.writeValueAsString(OrderStatusResponse.of(UUID.randomUUID(), OrderStatus.RECEIVED));

        assertThat(json).doesNotContain("processedAt");
        assertThat(json).doesNotContain("errorMessage");
    }

    @Test
    void serialization_success_includesProcessedAtButNotErrorMessage() {
        String json = mapper.writeValueAsString(OrderStatusResponse.success(UUID.randomUUID()));

        assertThat(json).contains("processedAt");
        assertThat(json).doesNotContain("errorMessage");
    }

    @Test
    void serialization_failure_includesErrorMessageButNotProcessedAt() {
        String json = mapper.writeValueAsString(OrderStatusResponse.failure(UUID.randomUUID(), "erro"));

        assertThat(json).contains("errorMessage");
        assertThat(json).doesNotContain("processedAt");
    }
}
