package client.udp;

import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class DiscoveryListener {

    private static final int PORT = 12347;

    public static void main(String[] args) {
        try (DatagramSocket socket = new DatagramSocket(PORT)) {
            byte[] buffer = new byte[1024];
            System.out.println("Listening for UDP server broadcasts...");

            while (true) {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);
                String msg = new String(packet.getData(), 0, packet.getLength());
                if ("SERVER_ONLINE".equals(msg)) {
                    System.out.println("Server is online!");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
