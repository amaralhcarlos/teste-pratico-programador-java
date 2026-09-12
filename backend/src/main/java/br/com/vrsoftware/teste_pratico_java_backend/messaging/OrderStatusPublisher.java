package br.com.vrsoftware.teste_pratico_java_backend.messaging;

import br.com.vrsoftware.teste_pratico_java_backend.dto.StatusPedido;

public interface OrderStatusPublisher {

    void publishSuccess(StatusPedido statusPedido);

    void publishFailure(StatusPedido statusPedido);
}
