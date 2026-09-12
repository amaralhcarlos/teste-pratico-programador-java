package br.com.vrsoftware.teste_pratico_java_backend.messaging;

import br.com.vrsoftware.teste_pratico_java_backend.dto.OrderStatusResponse;

public interface OrderStatusPublisher {

    void publishSuccess(OrderStatusResponse orderStatusResponse);

    void publishFailure(OrderStatusResponse orderStatusResponse);
}
