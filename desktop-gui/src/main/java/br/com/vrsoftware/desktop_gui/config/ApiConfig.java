package br.com.vrsoftware.desktop_gui.config;

public final class ApiConfig {

    private ApiConfig() {
    }

    public static final String BASE_URL = "http://localhost:8080";

    public static final String ORDERS_PATH = "/api/orders";
    public static final String ORDER_STATUS_PATH = "/api/orders/status/";

    public static final int POLLING_INTERVAL_MS = 4000;
    public static final int CONNECT_TIMEOUT_SECONDS = 5;
    public static final int REQUEST_TIMEOUT_SECONDS = 5;
}
