package br.com.vrsoftware.teste_pratico_java_backend.dto;

import br.com.vrsoftware.teste_pratico_java_backend.domain.OrderStatus;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record StatusPedido(
        UUID idPedido,
        OrderStatus status,
        LocalDateTime dataProcessamento,
        String mensagemErro
) {

    public static StatusPedido of(UUID idPedido, OrderStatus status) {
        return new StatusPedido(idPedido, status, null, null);
    }

    public static StatusPedido sucesso(UUID idPedido) {
        return new StatusPedido(idPedido, OrderStatus.SUCESSO, LocalDateTime.now(), null);
    }

    public static StatusPedido falha(UUID idPedido, String mensagemErro) {
        return new StatusPedido(idPedido, OrderStatus.FALHA, null, mensagemErro);
    }
}
