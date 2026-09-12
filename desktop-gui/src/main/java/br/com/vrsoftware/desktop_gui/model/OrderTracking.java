package br.com.vrsoftware.desktop_gui.model;

import java.time.LocalDateTime;
import java.util.UUID;

public class OrderTracking {

    private final UUID id;
    private final String product;
    private final Integer quantity;
    private final LocalDateTime creationDate;
    private volatile OrderStatus status;

    public OrderTracking(UUID id, String product, Integer quantity, LocalDateTime creationDate, OrderStatus status) {
        this.id = id;
        this.product = product;
        this.quantity = quantity;
        this.creationDate = creationDate;
        this.status = status;
    }

    public UUID getId() {
        return id;
    }

    public String getProduct() {
        return product;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public LocalDateTime getCreationDate() {
        return creationDate;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public boolean isPending() {
        return !status.isFinal();
    }
}
