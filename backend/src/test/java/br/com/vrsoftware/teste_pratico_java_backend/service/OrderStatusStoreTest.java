package br.com.vrsoftware.teste_pratico_java_backend.service;

import br.com.vrsoftware.teste_pratico_java_backend.domain.OrderStatus;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class OrderStatusStoreTest {

    private final OrderStatusStore store = new OrderStatusStore();

    @Test
    void find_unknownId_returnsEmpty() {
        assertThat(store.find(UUID.randomUUID())).isEmpty();
    }

    @Test
    void update_thenFind_returnsStoredStatus() {
        UUID id = UUID.randomUUID();

        store.update(id, OrderStatus.RECEIVED);

        assertThat(store.find(id)).contains(OrderStatus.RECEIVED);
    }

    @Test
    void update_calledTwice_overwritesWithLatestStatus() {
        UUID id = UUID.randomUUID();

        store.update(id, OrderStatus.PROCESSING);
        store.update(id, OrderStatus.SUCCESS);

        assertThat(store.find(id)).contains(OrderStatus.SUCCESS);
    }
}
