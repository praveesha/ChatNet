# WebSocket Bridge

A Spring Boot application that provides WebSocket connectivity for ChatNet.

## Getting Started

### Prerequisites
- Java 17 or higher
- Maven 3.6 or higher

### Running the Application

1. Clone the repository
2. Navigate to the project directory
3. Run the application:
   ```bash
   ./mvnw spring-boot:run
   ```
   
   Or on Windows:
   ```cmd
   mvnw.cmd spring-boot:run
   ```

The application will start on port 8080.

### Building the Application

To build the application:
```bash
./mvnw clean package
```

### Running Tests

To run the tests:
```bash
./mvnw test
```

## Project Structure

```
src/
├── main/
│   ├── java/
│   │   └── com/chatnet/websocketbridge/
│   │       └── WebSocketBridgeApplication.java
│   └── resources/
│       └── application.properties
└── test/
    └── java/
        └── com/chatnet/websocketbridge/
            └── WebSocketBridgeApplicationTests.java
```

## Features

- Spring Boot 3.1.5
- WebSocket support
- RESTful API endpoints
- Comprehensive testing setup
