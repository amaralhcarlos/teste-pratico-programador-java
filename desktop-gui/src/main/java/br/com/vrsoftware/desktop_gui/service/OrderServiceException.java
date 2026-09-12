package br.com.vrsoftware.desktop_gui.service;

public class OrderServiceException extends Exception {

    public OrderServiceException(String message) {
        super(message);
    }

    public OrderServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
