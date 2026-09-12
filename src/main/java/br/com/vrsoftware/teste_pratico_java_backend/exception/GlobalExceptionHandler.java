package br.com.vrsoftware.teste_pratico_java_backend.exception;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.exc.InvalidFormatException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleValidationException(
            MethodArgumentNotValidException exception) {

        Map<String, String> errors = new HashMap<>();

        exception.getBindingResult()
                .getFieldErrors()
                .forEach(error ->
                        errors.put(error.getField(), error.getDefaultMessage())
                );

        return errors;
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleMessageNotReadableException(
            HttpMessageNotReadableException exception) {

        Map<String, String> errors = new HashMap<>();

        if (exception.getCause() instanceof InvalidFormatException invalidFormatException) {
            String field = invalidFormatException.getPath().stream()
                    .map(JacksonException.Reference::getPropertyName)
                    .reduce((first, last) -> last)
                    .orElse("body");

            errors.put(field, "Formato inválido para o campo '" + field + "'");
        } else {
            errors.put("body", "Corpo da requisição inválido ou malformado");
        }

        return errors;
    }
}
