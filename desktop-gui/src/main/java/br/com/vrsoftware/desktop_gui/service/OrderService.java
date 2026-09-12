package br.com.vrsoftware.desktop_gui.service;

import br.com.vrsoftware.desktop_gui.config.ApiConfig;
import br.com.vrsoftware.desktop_gui.model.Order;
import br.com.vrsoftware.desktop_gui.model.OrderStatus;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.IOException;
import java.net.ConnectException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

public class OrderService {

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public OrderService() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(ApiConfig.CONNECT_TIMEOUT_SECONDS))
                .build();
        this.objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .disable(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE);
    }

    public UUID sendOrder(Order order) throws OrderServiceException {
        String requestBody;
        try {
            requestBody = objectMapper.writeValueAsString(
                    new OrderRequestBody(order.id(), order.product(), order.quantity(), order.creationDate()));
        } catch (JsonProcessingException e) {
            throw new OrderServiceException("Erro ao montar os dados do pedido.", e);
        }

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(ApiConfig.BASE_URL + ApiConfig.ORDERS_PATH))
                .timeout(Duration.ofSeconds(ApiConfig.REQUEST_TIMEOUT_SECONDS))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> response = send(request);
        int status = response.statusCode();

        if (status == 200 || status == 201 || status == 202) {
            try {
                OrderCreatedResponse created = objectMapper.readValue(response.body(), OrderCreatedResponse.class);
                return created.id() != null ? created.id() : order.id();
            } catch (JsonProcessingException e) {
                throw new OrderServiceException("O servidor retornou uma resposta em formato inesperado.", e);
            }
        }

        throw new OrderServiceException(errorMessage(status, response.body()));
    }

    public OrderStatus queryStatus(UUID id) throws OrderServiceException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(ApiConfig.BASE_URL + ApiConfig.ORDER_STATUS_PATH + id))
                .timeout(Duration.ofSeconds(ApiConfig.REQUEST_TIMEOUT_SECONDS))
                .GET()
                .build();

        HttpResponse<String> response = send(request);
        int status = response.statusCode();

        if (status == 200) {
            try {
                StatusQueryResponse body = objectMapper.readValue(response.body(), StatusQueryResponse.class);
                return mapStatus(body.status());
            } catch (JsonProcessingException e) {
                throw new OrderServiceException("O servidor retornou uma resposta em formato inesperado.", e);
            }
        }

        throw new OrderServiceException(errorMessage(status, response.body()));
    }

    private HttpResponse<String> send(HttpRequest request) throws OrderServiceException {
        try {
            return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (ConnectException e) {
            throw new OrderServiceException(
                    "Não foi possível conectar ao servidor. Verifique se o backend está em execução em "
                            + ApiConfig.BASE_URL + ".", e);
        } catch (HttpTimeoutException e) {
            throw new OrderServiceException("Tempo limite excedido ao comunicar com o servidor.", e);
        } catch (IOException e) {
            throw new OrderServiceException("Erro de comunicação com o servidor: " + e.getMessage(), e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new OrderServiceException("A comunicação com o servidor foi interrompida.", e);
        }
    }

    private OrderStatus mapStatus(String backendStatus) {
        if (backendStatus == null) {
            return OrderStatus.SENT_AWAITING_PROCESSING;
        }
        return switch (backendStatus) {
            case "SUCCESS" -> OrderStatus.SUCCESS;
            case "FAILURE" -> OrderStatus.FAILURE;
            default -> OrderStatus.SENT_AWAITING_PROCESSING;
        };
    }

    private String errorMessage(int status, String body) {
        return switch (status) {
            case 400 -> "Dados inválidos: " + extractValidationMessages(body);
            case 404 -> "Pedido não encontrado no servidor.";
            case 500 -> "Erro interno no servidor ao processar o pedido.";
            default -> "O servidor retornou um erro inesperado (HTTP " + status + ").";
        };
    }

    private String extractValidationMessages(String body) {
        try {
            Map<String, String> errors = objectMapper.readValue(body, new TypeReference<Map<String, String>>() {
            });
            if (errors.isEmpty()) {
                return "verifique os campos informados.";
            }
            return String.join("; ", errors.values());
        } catch (Exception e) {
            return "verifique os campos informados.";
        }
    }

    private record OrderRequestBody(UUID id, String product, Integer quantity, LocalDateTime creationDate) {
    }

    private record OrderCreatedResponse(UUID id) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record StatusQueryResponse(UUID orderId, String status) {
    }
}
