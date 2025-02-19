package rabbitmq;

import com.rabbitmq.client.*;
import models.Order;
import services.OrderProcessor;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeoutException;

public class OrderConsumer {
    public static void startListening() throws IOException, TimeoutException {
        // Start listening for orders
        RabbitMQManager.getConnection();

        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
            Order order = OrderProcessor.processOrder(message);
            sendOrderResponse(order);
        };

        RabbitMQManager.getConnection().createChannel().basicConsume("ORDERS", true, deliverCallback, consumerTag -> {});
    }

    private static void sendOrderResponse(Order order) {
        try {
            String response = "{\"order_id\":" + order.getId() + ",\"order_status\":\"" + order.getStatus() + "\"}";
            RabbitMQManager.sendMessage("ORDERS_RESPONSE", response);
            System.out.println("Sent order response: " + response);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void closeConnection() throws IOException, TimeoutException {
        RabbitMQManager.closeConnection();
    }
}
