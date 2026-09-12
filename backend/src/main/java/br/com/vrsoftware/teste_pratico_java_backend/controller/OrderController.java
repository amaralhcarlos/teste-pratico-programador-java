package br.com.vrsoftware.teste_pratico_java_backend.controller;


import br.com.vrsoftware.teste_pratico_java_backend.dto.OrderRequest;
import br.com.vrsoftware.teste_pratico_java_backend.dto.OrderResponse;
import br.com.vrsoftware.teste_pratico_java_backend.dto.OrderStatusResponse;
import br.com.vrsoftware.teste_pratico_java_backend.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<OrderResponse> create(
            @Valid @RequestBody OrderRequest request) {

        OrderResponse response = orderService.create(request);

        return ResponseEntity
                .accepted()
                .body(response);
    }

    @GetMapping("/status/{id}")
    public ResponseEntity<OrderStatusResponse> statusLookup(@PathVariable UUID id) {
        return orderService.getStatus(id)
                .map(status -> OrderStatusResponse.of(id, status))
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
