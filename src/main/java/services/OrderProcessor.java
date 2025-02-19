package services;

import database.DatabaseManager;
import models.Order;
import org.json.JSONArray;
import org.json.JSONObject;

import java.sql.*;

public class OrderProcessor {

    public static Order processOrder(String jsonOrder) {
        Connection conn = null; // Declare connection outside of try-with-resources block for proper management
        try {
            conn = DatabaseManager.getConnection(); // Open connection at the start of method

            JSONObject orderJson = new JSONObject(jsonOrder);
            String clientName = orderJson.getString("client_name");
            JSONArray items = orderJson.getJSONArray("items");

            // Insert new order
            String insertOrderQuery = "INSERT INTO orders (client_name, status) VALUES (?, 'RESERVED')";
            try (PreparedStatement orderStmt = conn.prepareStatement(insertOrderQuery, Statement.RETURN_GENERATED_KEYS)) {
                orderStmt.setString(1, clientName);
                orderStmt.executeUpdate();

                ResultSet generatedKeys = orderStmt.getGeneratedKeys();
                if (!generatedKeys.next()) {
                    throw new SQLException("Failed to retrieve order ID.");
                }
                int orderId = generatedKeys.getInt(1);

                boolean hasInsufficientStock = false;

                // Insert order items and check stock
                String insertOrderItemQuery = "INSERT INTO order_items (order_id, product_id, quantity) VALUES (?, ?, ?)";
                try (PreparedStatement itemStmt = conn.prepareStatement(insertOrderItemQuery)) {
                    for (int i = 0; i < items.length(); i++) {
                        JSONObject item = items.getJSONObject(i);
                        int productId = item.getInt("product_id");
                        int quantity = item.getInt("quantity");

                        // Check stock before adding order item
                        if (!hasSufficientStock(conn, productId, quantity)) {
                            hasInsufficientStock = true;
                        } else {
                            // Update stock only if there's sufficient stock
                            updateProductStock(conn, productId, quantity);
                        }

                        itemStmt.setInt(1, orderId);
                        itemStmt.setInt(2, productId);
                        itemStmt.setInt(3, quantity);
                        itemStmt.addBatch();
                    }
                    itemStmt.executeBatch();
                }

                // Update order status
                String updateOrderStatus = "UPDATE orders SET status = ? WHERE id = ?";
                try (PreparedStatement statusStmt = conn.prepareStatement(updateOrderStatus)) {
                    statusStmt.setString(1, hasInsufficientStock ? "INSUFFICIENT_STOCKS" : "RESERVED");
                    statusStmt.setInt(2, orderId);
                    statusStmt.executeUpdate();
                }

                return new Order(orderId, clientName, hasInsufficientStock ? "INSUFFICIENT_STOCKS" : "RESERVED");

            }
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        } finally {
            // Make sure to close the connection when done
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static boolean hasSufficientStock(Connection conn, int productId, int requestedQuantity) throws SQLException {
        String stockQuery = "SELECT stock FROM product WHERE id = ?";

        try (PreparedStatement stmt = conn.prepareStatement(stockQuery)) {
            stmt.setInt(1, productId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                int availableStock = rs.getInt("stock");
                return availableStock >= requestedQuantity;
            }
        }
        return false;
    }

    private static void updateProductStock(Connection conn, int productId, int quantity) throws SQLException {
        String updateStockQuery = "UPDATE product SET stock = stock - ? WHERE id = ?";

        try (PreparedStatement stmt = conn.prepareStatement(updateStockQuery)) {
            stmt.setInt(1, quantity);
            stmt.setInt(2, productId);
            stmt.executeUpdate();
        }
    }
}
