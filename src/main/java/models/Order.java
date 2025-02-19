package models;

import java.util.List;

public class Order {
    private int id;
    private String clientName;
    private String status;
    private List<OrderItem> items;

    public Order() {}

    public Order(int id, String clientName, String status, List<OrderItem> items) {
        this.id = id;
        this.clientName = clientName;
        this.status = status;
        this.items = items;
    }
    public Order(int id, String clientName, String status) {
        this.id = id;
        this.clientName = clientName;
        this.status = status;
    }
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getClientName() { return clientName; }
    public void setClientName(String clientName) { this.clientName = clientName; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public List<OrderItem> getItems() { return items; }
    public void setItems(List<OrderItem> items) { this.items = items; }

    @Override
    public String toString() {
        return "Order{id=" + id + ", clientName='" + clientName + "', status='" + status + "', items=" + items + "}";
    }
}
