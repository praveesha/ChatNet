package server.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.*;

public class NioChatServer {
    private static final int PORT = 12345;
    private static Map<SocketChannel, String> clients = new HashMap<>();

    public static void main(String[] args) {
        try (Selector selector = Selector.open();
             ServerSocketChannel serverSocket = ServerSocketChannel.open()) {

            serverSocket.bind(new InetSocketAddress(PORT));
            serverSocket.configureBlocking(false);
            serverSocket.register(selector, SelectionKey.OP_ACCEPT);
            System.out.println("NIO Chat Server started on port " + PORT);

            ByteBuffer buffer = ByteBuffer.allocate(1024);

            while (true) {
                selector.select(); // blocks until an event occurs
                Iterator<SelectionKey> keys = selector.selectedKeys().iterator();

                while (keys.hasNext()) {
                    SelectionKey key = keys.next();
                    keys.remove();

                    if (!key.isValid()) continue;

                    if (key.isAcceptable()) {
                        SocketChannel client = serverSocket.accept();
                        client.configureBlocking(false);
                        client.register(selector, SelectionKey.OP_READ);
                        clients.put(client, "Anonymous");
                        System.out.println("Client connected: " + client.getRemoteAddress());
                    } else if (key.isReadable()) {
                        SocketChannel client = (SocketChannel) key.channel();
                        buffer.clear();
                        int read = client.read(buffer);

                        if (read == -1) {
                            removeClient(client);
                            client.close();
                            continue;
                        }

                        buffer.flip();
                        String msg = new String(buffer.array(), 0, buffer.limit()).trim();
                        if (!msg.isEmpty()) {
                            // If first message is username
                            if (!clients.containsValue(msg)) clients.put(client, msg.split(":")[0]);
                            broadcast(msg, client);
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void broadcast(String message, SocketChannel sender) {
        for (SocketChannel client : clients.keySet()) {
            try {
                if (client != sender) {
                    ByteBuffer buffer = ByteBuffer.wrap((message + "\n").getBytes());
                    client.write(buffer);
                }
            } catch (IOException e) {
                removeClient(client);
            }
        }
    }

    private static void removeClient(SocketChannel client) {
        String name = clients.get(client);
        clients.remove(client);
        System.out.println("Client disconnected: " + name);
        broadcast(name + " left the chat", client);
    }
}
