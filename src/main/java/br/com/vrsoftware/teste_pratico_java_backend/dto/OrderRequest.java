package br.com.vrsoftware.teste_pratico_java_backend.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.UUID;

public record OrderRequest(
        UUID id,
        @NotBlank(message = "Produto não pode ser vazio")
        String product,
        @NotNull(message = "Quantidade é obrigatória")
        @Min(value = 1, message = "Quantidade deve ser maior que zero")
        Integer quantity,
        LocalDateTime creationDate
) {
}
