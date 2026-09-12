package br.com.vrsoftware.desktop_gui.model;

import java.time.LocalDateTime;
import java.util.UUID;

public record Order(UUID id, String product, Integer quantity, LocalDateTime creationDate) {

}
