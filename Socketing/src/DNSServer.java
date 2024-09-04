import java.io.*;
import java.net.*;
import java.util.HashMap;

public class DNSServer {

    private static final int DNS_PORT = 10500; // Port for DNS server

    private static final HashMap<String, String> userServerMap = new HashMap<>();

    static {
        userServerMap.put("Alex", "192.168.1.10 8080");
        userServerMap.put("Mikkel", "192.168.1.11 8081");
        userServerMap.put("Gerg", "10.10.131.157 9566");
        userServerMap.put("Kasper", "10.10.132.45 6969");
    }

    public static void main(String[] args) {
        try (DatagramSocket socket = new DatagramSocket(DNS_PORT)) {
            System.out.println("DNS server started on port " + DNS_PORT);
            System.out.println(socket.getLocalAddress());
            System.out.println(socket.getInetAddress());
            byte[] receiveBuffer = new byte[1024];
            DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);

            while (true) {
                socket.receive(receivePacket);
                String request = new String(receivePacket.getData(), 0, receivePacket.getLength()).trim();
                InetAddress clientAddress = receivePacket.getAddress();
                int clientPort = receivePacket.getPort();

                String response = userServerMap.getOrDefault(request, "Unknown");
                byte[] sendBuffer = response.getBytes();

                DatagramPacket sendPacket = new DatagramPacket(sendBuffer, sendBuffer.length, clientAddress, clientPort);
                socket.send(sendPacket);
                System.out.println("DNS response sent to " + clientAddress + ":" + clientPort + " - " + response);
            }
        } catch (IOException e) {
            System.err.println("Error in DNS server: " + e.getMessage());
            e.printStackTrace();
        }
    }
}