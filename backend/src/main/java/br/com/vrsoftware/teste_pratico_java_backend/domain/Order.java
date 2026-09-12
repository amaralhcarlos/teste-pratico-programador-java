package br.com.vrsoftware.teste_pratico_java_backend.domain;

import java.time.LocalDateTime;
import java.util.UUID;

public class Order {

    private UUID id;
    private String product;
    private int quantity;
    private LocalDateTime creationDate;

    public Order(UUID id, String product, int quantity, LocalDateTime creationDate) {
        this.id = id;
        this.product = product;
        this.quantity = quantity;
        this.creationDate = creationDate;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getProduct() {
        return product;
    }

    public void setProduct(String product) {
        this.product = product;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public LocalDateTime getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(LocalDateTime creationDate) {
        this.creationDate = creationDate;
    }

    @Override
    public String toString() {
        return "Order{" +
                "id=" + id +
                ", product='" + product + '\'' +
                ", quantity=" + quantity +
                ", creationDate=" + creationDate +
                '}';
    }
}
