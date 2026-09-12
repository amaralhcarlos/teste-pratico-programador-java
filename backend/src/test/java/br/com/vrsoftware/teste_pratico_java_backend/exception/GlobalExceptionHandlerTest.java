package br.com.vrsoftware.teste_pratico_java_backend.exception;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import tools.jackson.databind.exc.InvalidFormatException;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Mock
    private MethodArgumentNotValidException validationException;

    @Mock
    private BindingResult bindingResult;

    @Test
    void handleValidationException_buildsFieldToMessageMap() {
        FieldError productError = new FieldError("orderRequest", "product", "Produto não pode ser vazio");
        FieldError quantityError = new FieldError("orderRequest", "quantity", "Quantidade é obrigatória");

        when(validationException.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(productError, quantityError));

        Map<String, String> result = handler.handleValidationException(validationException);

        assertThat(result)
                .containsEntry("product", "Produto não pode ser vazio")
                .containsEntry("quantity", "Quantidade é obrigatória")
                .hasSize(2);
    }

    @Test
    void handleMessageNotReadableException_withInvalidFormatCause_buildsFieldSpecificMessage() {
        InvalidFormatException cause = new InvalidFormatException(null, "bad value", "abc", Integer.class);
        cause.prependPath(new Object(), "quantity");
        HttpMessageNotReadableException exception =
                new HttpMessageNotReadableException("parse error", cause, null);

        Map<String, String> result = handler.handleMessageNotReadableException(exception);

        assertThat(result)
                .containsEntry("quantity", "Formato inválido para o campo 'quantity'")
                .hasSize(1);
    }

    @Test
    void handleMessageNotReadableException_withOtherCause_buildsGenericMessage() {
        HttpMessageNotReadableException exception =
                new HttpMessageNotReadableException("parse error", new RuntimeException("boom"), null);

        Map<String, String> result = handler.handleMessageNotReadableException(exception);

        assertThat(result)
                .containsEntry("body", "Corpo da requisição inválido ou malformado")
                .hasSize(1);
    }
}
