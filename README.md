**Order & Stock Management System (Java, MySQL, RabbitMQ)**
This project is a simple Order and Stock Management System built in Java 15 (without Spring) that processes stock updates from XML files and orders from RabbitMQ messages. It ensures that stock levels are maintained and orders are fulfilled based on product availability.

**Features**

✅ Process incoming orders from RabbitMQ
✅ Process stock updates from XML files
✅ Automatically move processed stock files to a separate folder
✅ Prevent stock from going negative
✅ Handle concurrent order processing using ExecutorService

**Project Structure**

/project-root
│── /src
│   ├── /cli               # CLI for manual debugging
│   ├── /database          # Database connection management
│   ├── /models            # Data models (Product, Order, OrderItem)
│   ├── /services          # Business logic (Stock & Order processing)
│   ├── /utils             # Utility classes
│   ├── Main.java          # Application entry point
│
│── /stocks_new            # Incoming XML stock files
│── /stocks_processed      # Processed XML files
│── /sql                   # Database schema & sample data
│── README.md              # Documentation
│── pom.xml                # Maven dependencies

**Installation & Setup**

**1. Database Setup (MySQL)**
Create a database:

CREATE DATABASE order_stock_db;
Use the provided SQL schema:

mysql -u root -p order_stock_db < sql/schema.sql

**2. Configure application.properties**
Modify application.properties with your database & RabbitMQ details:


DB_URL=jdbc:mysql://localhost:3306/order_stock_db
DB_USER=root
DB_PASSWORD=password

RABBITMQ_HOST=localhost
RABBITMQ_QUEUE=ORDERS

**3. Run the Application**

java -jar OrderStockManager.jar
Use the following commands:

status → Show stock levels & pending orders
process-stock <file> → Manually process a stock file
queue-stats → Show RabbitMQ queue status
exit → Shut down the app

**How It Works**
Stock Processing (stocks_new/*.xml)
The system watches for new XML files in the /stocks_new folder.
Processes stock updates and prevents negative stock.
Moves processed files to /stocks_processed.

**Example XML (stocks_new/example.xml)**
xml
Copiază
Editează
<stocks>
    <stock>
        <product_id>1</product_id>
        <quantity>50</quantity>
    </stock>
    <stock>
        <product_id>2</product_id>
        <quantity>-100</quantity> <!-- This will be rejected if it results in negative stock -->
    </stock>
</stocks>
Order Processing (RabbitMQ)
Orders are received as JSON messages from RabbitMQ.
If stock is available, the order status is RESERVED.
If stock is insufficient, the order status is INSUFFICIENT_STOCKS.

**Example Order JSON (RabbitMQ)**
{
  "client_name": "John Doe",
  "items": [
    { "product_id": 1, "quantity": 10 },
    { "product_id": 2, "quantity": 5 }
  ]
}

**Development Notes**
Uses Maven for dependency management.
Concurrency: Orders are processed using ExecutorService with 5 threads.

**Author**
🚀 Daniel Mandea
📧 Contact: danielmandea@gmail.com
