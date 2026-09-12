package br.com.vrsoftware.teste_pratico_java_backend.service;

import br.com.vrsoftware.teste_pratico_java_backend.domain.OrderStatus;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class OrderStatusStore {

    private final Map<UUID, OrderStatus> statuses = new ConcurrentHashMap<>();

    public void update(UUID orderId, OrderStatus status) {
        statuses.put(orderId, status);
    }

    public Optional<OrderStatus> find(UUID orderId) {
        return Optional.ofNullable(statuses.get(orderId));
    }
}
