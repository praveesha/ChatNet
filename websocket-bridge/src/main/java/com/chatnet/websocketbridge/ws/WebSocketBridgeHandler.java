package com.chatdashboard.ws;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import java.io.*;
import java.net.Socket;

@Component
public class WebSocketBridgeHandler extends TextWebSocketHandler {
    private final String TCP_HOST = "localhost";
    private final int TCP_PORT = 5000; // same port your ChatServer uses

    private Socket tcpSocket;
    private PrintWriter out;
    private BufferedReader in;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        tcpSocket = new Socket(TCP_HOST, TCP_PORT);
        out = new PrintWriter(tcpSocket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(tcpSocket.getInputStream()));

        new Thread(() -> {
            try {
                String msg;
                while ((msg = in.readLine()) != null) {
                    session.sendMessage(new TextMessage(msg));
                }
            } catch (IOException e) {
                System.out.println("TCP read error: " + e.getMessage());
            }
        }).start();
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        if (out != null) out.println(message.getPayload());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        if (tcpSocket != null) tcpSocket.close();
    }
}
