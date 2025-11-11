# NIO Echo Server - Member 3

High-Performance NIO Echo Server using Java NIO (Non-blocking I/O) with Selector pattern.

## Key Concepts Demonstrated
- **Non-blocking I/O**: Uses `SocketChannel` and `ServerSocketChannel` in non-blocking mode
- **Selector Pattern**: Single-threaded server handling multiple clients using `Selector`
- **ByteBuffer Handling**: Efficient buffer management for reading/writing data
- **Partial Reads/Writes**: Handles incomplete I/O operations gracefully

## How to Run

### 1. Compile and Start Server
```bash
cd server/nio
javac NIOEchoServer.java
java server.nio.NIOEchoServer
```

### 2. Test with Simple Client
```bash
cd client/nio
javac NIOTestClient.java
java client.nio.NIOTestClient
```

### 3. Load Test (100 concurrent clients)
```bash
javac LoadTestClient.java
java client.nio.LoadTestClient
```

## Architecture
- **Single Thread**: One thread handles all clients using selector
- **Event-Driven**: Responds to I/O events (accept, read) as they occur
- **Scalable**: Can handle thousands of concurrent connections
- **Memory Efficient**: ByteBuffer reuse and minimal object allocation

## Performance Benefits
- No thread-per-client overhead
- Reduced context switching
- Lower memory footprint
- Higher concurrent connection capacity