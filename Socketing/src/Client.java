import java.io.*;
import java.net.*;

public class Client {
    private Socket tcpSocket;
    private DatagramSocket udpSocket;
    private BufferedReader in;
    private PrintWriter out;
    private InetAddress serverAddress;
    private int serverPort;
    private String username;
    private boolean useTCP;

    // Hardcoded DNS server settings
    private static final String DNS_SERVER_ADDRESS = "127.0.0.1";
    private static final int DNS_SERVER_PORT = 10500;

    public Client(String username, boolean useTCP) {
        this.username = username;
        this.useTCP = useTCP;

        try {
            String[] resolvedAddress = queryDNS(DNS_SERVER_ADDRESS, DNS_SERVER_PORT, username);
            if (resolvedAddress == null) {
                System.out.println("Failed to resolve server address.");
                return;
            }

            this.serverAddress = InetAddress.getByName(resolvedAddress[0]);
            this.serverPort = Integer.parseInt(resolvedAddress[1]);

            if (useTCP) {
                System.out.println("Connecting to TCP server...");
                tcpSocket = new Socket(serverAddress, serverPort);
                in = new BufferedReader(new InputStreamReader(tcpSocket.getInputStream()));
                out = new PrintWriter(tcpSocket.getOutputStream(), true);
                System.out.println("Connected to TCP server at " + serverAddress + ":" + serverPort);
            } else {
                System.out.println("Connecting to UDP server...");
                udpSocket = new DatagramSocket();
                System.out.println("Connected to UDP server at " + serverAddress + ":" + serverPort);
            }
        } catch (IOException e) {
            System.err.println("Error connecting to server: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private String[] queryDNS(String dnsServerAddress, int dnsServerPort, String username) {
        try (DatagramSocket socket = new DatagramSocket()) {
            
            byte[] sendBuffer = username.getBytes();
            DatagramPacket sendPacket = new DatagramPacket(sendBuffer, sendBuffer.length,
                    InetAddress.getByName(dnsServerAddress), dnsServerPort);
            socket.send(sendPacket);

            byte[] receiveBuffer = new byte[1024];
            DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
            socket.receive(receivePacket);

            String response = new String(receivePacket.getData(), 0, receivePacket.getLength()).trim();
            if (response.equals("Unknown")) {
                System.err.println("Username not found.");
                return null;
            }

            return response.split(" ");
        } catch (IOException e) {
            System.err.println("Error querying DNS server: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public void start() {
        if (useTCP) {
            new Thread(new IncomingTCPReader()).start();
        } else {
            new Thread(new IncomingUDPReader()).start();
        }

        try (BufferedReader consoleReader = new BufferedReader(new InputStreamReader(System.in))) {
            String message;
            while ((message = consoleReader.readLine()) != null) {
                if (message.toLowerCase().equals("info")) {
                    System.out.println("The different users you can connect to: ");
                    // Assuming navneServiceMap or other info is handled
                } else {
                    sendMessage(message);
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading from console: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void sendMessage(String message) {
        System.out.println("Sending message: " + message);
        if (useTCP) {
            out.println(username + ": " + message);
        } else {
            try {
                String fullMessage = username + ": " + message;
                byte[] sendBuffer = fullMessage.getBytes();
                DatagramPacket sendPacket = new DatagramPacket(sendBuffer, sendBuffer.length, serverAddress, serverPort);
                udpSocket.send(sendPacket);
            } catch (IOException e) {
                System.err.println("Error sending UDP message: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private class IncomingTCPReader implements Runnable {
        @Override
        public void run() {
            String message;
            try {
                while ((message = in.readLine()) != null) {
                    System.out.println("Received message: " + message);
                }
            } catch (IOException e) {
                System.err.println("Error reading from server: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private class IncomingUDPReader implements Runnable {
        @Override
        public void run() {
            byte[] receiveBuffer = new byte[1024];
            DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
            try {
                while (true) {
                    udpSocket.receive(receivePacket);
                    String message = new String(receivePacket.getData(), 0, receivePacket.getLength());
                    System.out.println("Received message: " + message);
                }
            } catch (IOException e) {
                System.err.println("Error reading from UDP server: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        if (args.length != 3) {
            System.out.println("Usage: java Client <server-username> <client-username> <1 for TCP | 2 for UDP>");
            return;
        }

        String serverUsername = args[0];
        String clientUsername = args[1];
        boolean useTCP = args[2].equals("1");

        Client client = new Client(serverUsername, useTCP);
        client.start();
    }
}