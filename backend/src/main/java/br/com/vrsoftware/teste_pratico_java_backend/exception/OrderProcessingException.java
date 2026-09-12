package br.com.vrsoftware.teste_pratico_java_backend.exception;

public class OrderProcessingException extends RuntimeException {

    public OrderProcessingException(String message) {
        super(message);
    }
}
