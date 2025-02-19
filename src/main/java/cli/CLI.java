package cli;

import database.DatabaseManager;
import rabbitmq.RabbitMQManager;
import services.StockProcessor;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

public class CLI {

    private static final String INPUT_FOLDER = "stocks_new";
    public static void startCLI() {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in))) {
            String command;

            while (true) {
                System.out.print("Enter command: ");
                command = reader.readLine();

                switch (command) {
                    case "status":
                        showStatus();
                        break;
                    case "process-stock":
                        processStockFiles();
                        break;
                    case "queue-stats":
                        showQueueStats();
                        break;
                    case "exit":
                        gracefulShutdown();
                        return;
                    default:
                        System.out.println("Unknown command: " + command);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void showStatus() {
        System.out.println("Showing current stock levels and pending orders...");

        // Query and display stock and pending orders (simplified for illustration)
        try (var conn = DatabaseManager.getConnection(); var stmt = conn.createStatement()) {
            var stockResult = stmt.executeQuery("SELECT * FROM product");
            while (stockResult.next()) {
                System.out.println("Product: " + stockResult.getString("name") +
                        " | Stock: " + stockResult.getInt("stock"));
            }

            var ordersResult = stmt.executeQuery("SELECT * FROM orders WHERE status = 'RESERVED'");
            while (ordersResult.next()) {
                System.out.println("Pending Order: " + ordersResult.getInt("id") +
                        " | Client: " + ordersResult.getString("client_name"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void processStockFiles() {
        try {
            File inputDir = new File(INPUT_FOLDER);
            File[] xmlFiles = inputDir.listFiles((dir, name) -> name.endsWith(".xml"));

            if (xmlFiles != null) {
                for (File xmlFile : xmlFiles) {
                    // Call the stock processor to handle the file
                    StockProcessor.processStockFile(xmlFile);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private static void showQueueStats() {
        RabbitMQManager.showQueueStats();
    }

    private static void gracefulShutdown() {
        try {
            System.out.println("Shutting down gracefully...");
            RabbitMQManager.closeConnection();
            DatabaseManager.closeConnection();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
