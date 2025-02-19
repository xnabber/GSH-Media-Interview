package services;

import database.DatabaseManager;
import org.w3c.dom.*;
import javax.xml.parsers.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.*;

public class StockProcessor {
    private static final String PROCESSED_FOLDER = "stocks_processed";

    public static void processStockFile(File xmlFile) {
        Connection conn = null;
        try {
            // Establish connection at the start of the method
            conn = DatabaseManager.getConnection();

            // Use BufferedReader to read the file content
            BufferedReader reader = new BufferedReader(new FileReader(xmlFile));
            StringBuilder xmlContent = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                xmlContent.append(line);
            }
            reader.close();

            // Convert the XML string to InputStream to parse it
            InputStream inputStream = new ByteArrayInputStream(xmlContent.toString().getBytes());

            // Parse the XML content using DocumentBuilder
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(inputStream);
            doc.getDocumentElement().normalize();

            NodeList stockList = doc.getElementsByTagName("stock");

            for (int i = 0; i < stockList.getLength(); i++) {
                Node node = stockList.item(i);

                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element element = (Element) node;
                    int productId = Integer.parseInt(element.getElementsByTagName("product_id").item(0).getTextContent());
                    int quantity = Integer.parseInt(element.getElementsByTagName("quantity").item(0).getTextContent());

                    updateStock(conn, productId, quantity);
                }
            }

            // Move processed file to the processed folder
            Path sourcePath = xmlFile.toPath();
            Path destinationPath = Paths.get(PROCESSED_FOLDER, xmlFile.getName());

            // Debugging: Print the paths to confirm
            System.out.println("Source Path: " + sourcePath.toAbsolutePath());
            System.out.println("Destination Path: " + destinationPath.toAbsolutePath());

            // Move the processed file
            try {
                Files.move(sourcePath, destinationPath, StandardCopyOption.REPLACE_EXISTING);
                System.out.println("Stock file processed and moved to 'processed' folder.");
            } catch (IOException e) {
                System.out.println("Failed to move stock file to 'processed' folder: " + e.getMessage());
            }

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error processing stock file.");
        } finally {
            // Ensure connection is closed after all operations
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    private static void updateStock(Connection conn, int productId, int quantity) {
        String checkStockQuery = "SELECT stock FROM product WHERE id = ?";
        String updateStockQuery = "UPDATE product SET stock = ? WHERE id = ?";

        try (PreparedStatement checkStmt = conn.prepareStatement(checkStockQuery)) {
            checkStmt.setInt(1, productId);
            ResultSet rs = checkStmt.executeQuery();

            if (rs.next()) {
                int currentStock = rs.getInt("stock");
                int newStock = currentStock + quantity;

                // Prevent negative stock
                if (newStock < 0) {
                    System.out.println("Error: Updating product ID " + productId + " would result in negative stock. Skipping update.");
                    return;
                }

                try (PreparedStatement updateStmt = conn.prepareStatement(updateStockQuery)) {
                    updateStmt.setInt(1, newStock);
                    updateStmt.setInt(2, productId);
                    int affectedRows = updateStmt.executeUpdate();

                    if (affectedRows == 0) {
                        System.out.println("Warning: Product ID " + productId + " not found or no rows affected.");
                    } else {
                        System.out.println("Stock updated successfully for product ID " + productId + ". New stock: " + newStock);
                    }
                }
            } else {
                System.out.println("Warning: Product ID " + productId + " not found in the database.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
