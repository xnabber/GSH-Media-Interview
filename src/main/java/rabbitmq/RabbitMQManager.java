package rabbitmq;

import com.rabbitmq.client.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeoutException;

public class RabbitMQManager {
    private static final String QUEUE_ORDERS = "ORDERS";
    private static final String QUEUE_RESPONSES = "ORDERS_RESPONSE";
    private static final String RABBITMQ_HOST = "localhost";
    private static final ConnectionFactory factory = new ConnectionFactory();
    private static Connection connection;
    private static Channel channel;

    static {
        factory.setHost(RABBITMQ_HOST);
    }

    public static Connection getConnection() throws IOException, TimeoutException {
        connection = factory.newConnection();
        channel = connection.createChannel();

        // Declare the necessary queues
        channel.queueDeclare("ORDERS", true, false, false, null);
        channel.queueDeclare("ORDERS_RESPONSE", true, false, false, null);
        return connection;
    }

    public static void closeConnection() throws IOException, TimeoutException {
        if (channel != null) {
            channel.close();
        }
        if (connection != null) {
            connection.close();
        }
    }

    public static void showQueueStats() {
        System.out.println("Fetching RabbitMQ queue stats...");
        try {
            // Get stats for the "ORDERS" queue
            AMQP.Queue.DeclareOk ordersQueueStats = channel.queueDeclarePassive(QUEUE_ORDERS);
            System.out.println("ORDERS Queue stats:");
            System.out.println("  - Message count: " + ordersQueueStats.getMessageCount());
            System.out.println("  - Consumer count: " + ordersQueueStats.getConsumerCount());

            // Get stats for the "ORDERS_RESPONSE" queue
            AMQP.Queue.DeclareOk responseQueueStats = channel.queueDeclarePassive(QUEUE_RESPONSES);
            System.out.println("ORDERS_RESPONSE Queue stats:");
            System.out.println("  - Message count: " + responseQueueStats.getMessageCount());
            System.out.println("  - Consumer count: " + responseQueueStats.getConsumerCount());

        } catch (IOException e) {
            System.err.println("Error fetching RabbitMQ queue stats: " + e.getMessage());
            e.printStackTrace();
        }
    }


    public static void sendMessage(String queueName, String message) throws IOException {
        channel.basicPublish("", queueName, null, message.getBytes(StandardCharsets.UTF_8));
    }
}
