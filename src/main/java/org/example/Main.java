package org.example;

import cli.CLI;
import rabbitmq.OrderConsumer;
import rabbitmq.RabbitMQManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.TimeoutException;

public class Main {
    public static void main(String[] args) {
        try {
            // Initialize RabbitMQ connection and channels
            RabbitMQManager.getConnection();
            System.out.println("RabbitMQ connection established.");

            // Start the order consumer to listen for incoming orders
            OrderConsumer.startListening();
            System.out.println("Listening for orders...");

            // Start the CLI in a separate thread
            Thread cliThread = new Thread(() -> CLI.startCLI());
            cliThread.start();

            // Graceful shutdown (add your own logic to ensure that resources are freed on shutdown)
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    System.out.println("Shutting down...");
                    OrderConsumer.closeConnection();  // Close consumer connection
                    RabbitMQManager.closeConnection(); // Close RabbitMQ connection
                    System.out.println("Connections closed.");
                } catch (IOException | TimeoutException e) {
                    e.printStackTrace();
                }
            }));

            // Keep the application running so the consumer can listen for messages
            // You could replace this with other operations or even a scanner for CLI input if needed
            Thread.currentThread().join();  // This keeps the main thread alive indefinitely, waiting for messages

        } catch (IOException | TimeoutException | InterruptedException e) {
            e.printStackTrace();
            System.exit(1);  // Exit if there is a problem with the connection or consumer
        }
    }
}
