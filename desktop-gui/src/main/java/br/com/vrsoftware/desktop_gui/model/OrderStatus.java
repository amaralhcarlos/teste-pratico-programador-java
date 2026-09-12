package br.com.vrsoftware.desktop_gui.model;

public enum OrderStatus {

    SENT_AWAITING_PROCESSING("ENVIADO, AGUARDANDO PROCESSO"),
    SUCCESS("SUCESSO"),
    FAILURE("FALHA");

    private final String description;

    OrderStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public boolean isFinal() {
        return this == SUCCESS || this == FAILURE;
    }
}
