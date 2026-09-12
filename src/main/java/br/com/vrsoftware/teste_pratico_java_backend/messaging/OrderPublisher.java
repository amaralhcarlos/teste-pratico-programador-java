package br.com.vrsoftware.teste_pratico_java_backend.messaging;

import br.com.vrsoftware.teste_pratico_java_backend.domain.Order;

public interface OrderPublisher {

    void publish(Order Order);
}

