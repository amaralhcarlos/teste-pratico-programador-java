package br.com.vrsoftware.desktop_gui.ui;

import br.com.vrsoftware.desktop_gui.config.ApiConfig;
import br.com.vrsoftware.desktop_gui.model.Order;
import br.com.vrsoftware.desktop_gui.model.OrderStatus;
import br.com.vrsoftware.desktop_gui.model.OrderTracking;
import br.com.vrsoftware.desktop_gui.service.OrderService;
import br.com.vrsoftware.desktop_gui.service.OrderServiceException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingWorker;
import javax.swing.Timer;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

public class OrderFrame extends JFrame {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
    private static final String EMPTY_DETAIL = "-";

    private final OrderService orderService = new OrderService();
    private final OrderTableModel tableModel = new OrderTableModel();
    private final Set<UUID> ordersBeingQueried = new HashSet<>();

    private final JTextField productField = new JTextField(20);
    private final JTextField quantityField = new JTextField(6);
    private final JButton sendButton = new JButton("Enviar Pedido");
    private final JTable table = new JTable(tableModel);

    private final JLabel idDetailValue = new JLabel(EMPTY_DETAIL);
    private final JLabel productDetailValue = new JLabel(EMPTY_DETAIL);
    private final JLabel quantityDetailValue = new JLabel(EMPTY_DETAIL);
    private final JLabel statusDetailValue = new JLabel(EMPTY_DETAIL);
    private final JLabel creationDateDetailValue = new JLabel(EMPTY_DETAIL);

    public OrderFrame() {
        super("Sistema de Pedidos");
        buildInterface();
        startPolling();
    }

    private void buildInterface() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(650, 560);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        JPanel formPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        formPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));
        formPanel.add(new JLabel("Produto:"));
        formPanel.add(productField);
        formPanel.add(new JLabel("Quantidade:"));
        formPanel.add(quantityField);
        formPanel.add(sendButton);
        sendButton.addActionListener(e -> sendOrder());

        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                updateDetailsPanel();
            }
        });
        tableModel.addTableModelListener(e -> updateDetailsPanel());

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(10, 10, 10, 10),
                BorderFactory.createTitledBorder("Pedidos enviados")));

        add(formPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(buildDetailsPanel(), BorderLayout.SOUTH);
    }

    private JPanel buildDetailsPanel() {
        JPanel detailsPanel = new JPanel(new GridLayout(5, 2, 5, 5));
        detailsPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(0, 10, 10, 10),
                BorderFactory.createTitledBorder("Detalhes do Pedido")));

        detailsPanel.add(new JLabel("ID:"));
        detailsPanel.add(idDetailValue);
        detailsPanel.add(new JLabel("Produto:"));
        detailsPanel.add(productDetailValue);
        detailsPanel.add(new JLabel("Quantidade:"));
        detailsPanel.add(quantityDetailValue);
        detailsPanel.add(new JLabel("Status:"));
        detailsPanel.add(statusDetailValue);
        detailsPanel.add(new JLabel("Criado em:"));
        detailsPanel.add(creationDateDetailValue);

        return detailsPanel;
    }

    private void updateDetailsPanel() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow < 0) {
            idDetailValue.setText(EMPTY_DETAIL);
            productDetailValue.setText(EMPTY_DETAIL);
            quantityDetailValue.setText(EMPTY_DETAIL);
            statusDetailValue.setText(EMPTY_DETAIL);
            creationDateDetailValue.setText(EMPTY_DETAIL);
            return;
        }

        OrderTracking selectedOrder = tableModel.getOrderAt(selectedRow);
        idDetailValue.setText(selectedOrder.getId().toString());
        productDetailValue.setText(selectedOrder.getProduct());
        quantityDetailValue.setText(String.valueOf(selectedOrder.getQuantity()));
        statusDetailValue.setText(selectedOrder.getStatus().getDescription());
        creationDateDetailValue.setText(selectedOrder.getCreationDate() != null
                ? selectedOrder.getCreationDate().format(DATE_FORMATTER)
                : EMPTY_DETAIL);
    }

    private void sendOrder() {
        String product = productField.getText().trim();
        String quantityText = quantityField.getText().trim();

        if (product.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Informe o produto.", "Dados inválidos", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int quantity;
        try {
            quantity = Integer.parseInt(quantityText);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Informe uma quantidade numérica válida.", "Dados inválidos",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (quantity <= 0) {
            JOptionPane.showMessageDialog(this, "A quantidade deve ser maior que zero.", "Dados inválidos",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        Order order = new Order(UUID.randomUUID(), product, quantity, LocalDateTime.now());
        sendButton.setEnabled(false);

        new SwingWorker<UUID, Void>() {
            @Override
            protected UUID doInBackground() throws Exception {
                return orderService.sendOrder(order);
            }

            @Override
            protected void done() {
                sendButton.setEnabled(true);
                try {
                    UUID confirmedId = get();
                    tableModel.addOrder(new OrderTracking(
                            confirmedId, order.product(), order.quantity(), order.creationDate(),
                            OrderStatus.SENT_AWAITING_PROCESSING));
                    productField.setText("");
                    quantityField.setText("");
                    productField.requestFocusInWindow();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } catch (ExecutionException e) {
                    showSendError(e.getCause());
                }
            }
        }.execute();
    }

    private void startPolling() {
        Timer timer = new Timer(ApiConfig.POLLING_INTERVAL_MS, e -> queryPendingOrders());
        timer.setRepeats(true);
        timer.start();
    }

    private void queryPendingOrders() {
        for (OrderTracking order : tableModel.getPendingOrders()) {
            UUID id = order.getId();

            if (!ordersBeingQueried.add(id)) {
                continue;
            }

            new SwingWorker<OrderStatus, Void>() {
                @Override
                protected OrderStatus doInBackground() throws Exception {
                    return orderService.queryStatus(id);
                }

                @Override
                protected void done() {
                    ordersBeingQueried.remove(id);
                    try {
                        OrderStatus currentStatus = get();
                        if (currentStatus.isFinal()) {
                            tableModel.updateStatus(id, currentStatus);
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } catch (ExecutionException e) {
                        System.err.println("Failed to query status for order " + id + ": "
                                + e.getCause().getMessage());
                    }
                }
            }.execute();
        }
    }

    private void showSendError(Throwable cause) {
        String message = cause instanceof OrderServiceException
                ? cause.getMessage()
                : "Ocorreu um erro inesperado ao enviar o pedido.";
        JOptionPane.showMessageDialog(this, message, "Erro ao enviar pedido", JOptionPane.ERROR_MESSAGE);
        if (!(cause instanceof OrderServiceException)) {
            cause.printStackTrace();
        }
    }
}
