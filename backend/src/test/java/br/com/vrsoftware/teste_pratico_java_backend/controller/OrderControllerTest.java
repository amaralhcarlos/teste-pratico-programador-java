package br.com.vrsoftware.teste_pratico_java_backend.controller;

import br.com.vrsoftware.teste_pratico_java_backend.domain.OrderStatus;
import br.com.vrsoftware.teste_pratico_java_backend.dto.OrderRequest;
import br.com.vrsoftware.teste_pratico_java_backend.dto.OrderResponse;
import br.com.vrsoftware.teste_pratico_java_backend.service.OrderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrderController.class)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OrderService orderService;

    @Test
    void create_validRequest_returns202WithOrderId() throws Exception {
        UUID id = UUID.randomUUID();
        when(orderService.create(any(OrderRequest.class))).thenReturn(new OrderResponse(id));

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"product\":\"Teclado\",\"quantity\":2,\"creationDate\":\"2026-01-01T12:00:00\"}"))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.id").value(id.toString()));
    }

    @Test
    void create_invalidRequest_returns400WithFieldErrors() throws Exception {
        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.product").exists())
                .andExpect(jsonPath("$.quantity").exists())
                .andExpect(jsonPath("$.creationDate").exists());
    }

    @Test
    void create_malformedJson_returns400() throws Exception {
        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"product\":\"Teclado\",\"quantity\":\"abc\",\"creationDate\":\"2026-01-01T12:00:00\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.quantity").exists());
    }

    @Test
    void statusLookup_found_returns200WithOrderStatusResponse() throws Exception {
        UUID id = UUID.randomUUID();
        when(orderService.getStatus(id)).thenReturn(Optional.of(OrderStatus.SUCCESS));

        mockMvc.perform(get("/api/orders/status/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value(id.toString()))
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.processedAt").doesNotExist())
                .andExpect(jsonPath("$.errorMessage").doesNotExist());
    }

    @Test
    void statusLookup_notFound_returns404() throws Exception {
        UUID id = UUID.randomUUID();
        when(orderService.getStatus(id)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/orders/status/{id}", id))
                .andExpect(status().isNotFound());
    }
}
