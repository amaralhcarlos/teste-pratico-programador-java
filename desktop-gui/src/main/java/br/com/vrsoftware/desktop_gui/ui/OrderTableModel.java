package br.com.vrsoftware.desktop_gui.ui;

import br.com.vrsoftware.desktop_gui.model.OrderStatus;
import br.com.vrsoftware.desktop_gui.model.OrderTracking;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class OrderTableModel extends AbstractTableModel {

    private static final String[] COLUMNS = {"ID do Pedido", "Status"};

    private final List<OrderTracking> orders = new ArrayList<>();

    @Override
    public int getRowCount() {
        return orders.size();
    }

    @Override
    public int getColumnCount() {
        return COLUMNS.length;
    }

    @Override
    public String getColumnName(int column) {
        return COLUMNS[column];
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        OrderTracking order = orders.get(rowIndex);
        return switch (columnIndex) {
            case 0 -> order.getId().toString();
            case 1 -> order.getStatus().getDescription();
            default -> "";
        };
    }

    public OrderTracking getOrderAt(int rowIndex) {
        return orders.get(rowIndex);
    }

    public void addOrder(OrderTracking order) {
        orders.add(order);
        int row = orders.size() - 1;
        fireTableRowsInserted(row, row);
    }

    public void updateStatus(UUID id, OrderStatus newStatus) {
        for (int i = 0; i < orders.size(); i++) {
            if (orders.get(i).getId().equals(id)) {
                orders.get(i).setStatus(newStatus);
                fireTableRowsUpdated(i, i);
                return;
            }
        }
    }

    public List<OrderTracking> getPendingOrders() {
        List<OrderTracking> pending = new ArrayList<>();
        for (OrderTracking order : orders) {
            if (order.isPending()) {
                pending.add(order);
            }
        }
        return pending;
    }
}
