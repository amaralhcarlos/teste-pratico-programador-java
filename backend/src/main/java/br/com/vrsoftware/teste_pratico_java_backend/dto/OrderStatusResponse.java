package br.com.vrsoftware.teste_pratico_java_backend.dto;

import br.com.vrsoftware.teste_pratico_java_backend.domain.OrderStatus;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record OrderStatusResponse(
        UUID orderId,
        OrderStatus status,
        LocalDateTime processedAt,
        String errorMessage
) {

    public static OrderStatusResponse of(UUID orderId, OrderStatus status) {
        return new OrderStatusResponse(orderId, status, null, null);
    }

    public static OrderStatusResponse success(UUID orderId) {
        return new OrderStatusResponse(orderId, OrderStatus.SUCCESS, LocalDateTime.now(), null);
    }

    public static OrderStatusResponse failure(UUID orderId, String errorMessage) {
        return new OrderStatusResponse(orderId, OrderStatus.FAILURE, null, errorMessage);
    }
}
