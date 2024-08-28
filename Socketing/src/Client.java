import java.io.*;
import java.net.*;
import java.util.HashMap;

public class Client {

    private Socket tcpSocket;
    private DatagramSocket udpSocket;
    private BufferedReader in;
    private PrintWriter out;
    private InetAddress serverAddress;
    private int serverPort;
    private String username;
    private boolean useTCP;
    private static HashMap<String, String> navneServiceMap = new HashMap<>();

    public Client(String serverAddress, int serverPort, String username, boolean useTCP) {
        this.username = username;
        this.serverPort = serverPort;
        this.useTCP = useTCP;

        try {
            if (useTCP) {
                System.out.println("Connecting to TCP server...");
                tcpSocket = new Socket(serverAddress, serverPort);
                in = new BufferedReader(new InputStreamReader(tcpSocket.getInputStream()));
                out = new PrintWriter(tcpSocket.getOutputStream(), true);
                System.out.println("Connected to TCP server at " + serverAddress + ":" + serverPort);
            } else {
                System.out.println("Connecting to UDP server...");
                udpSocket = new DatagramSocket();
                this.serverAddress = InetAddress.getByName(serverAddress);
                System.out.println("Connected to UDP server at " + serverAddress + ":" + serverPort);
            }
        } catch (IOException e) {
            System.err.println("Error connecting to server: " + e.getMessage());
            e.printStackTrace();
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
                    System.out.println(navneServiceMap.toString());
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
        navneServiceMap.put("Alex", "192.168.1.10 8080");
        navneServiceMap.put("Mikkel", "192.168.1.11 8081");
        navneServiceMap.put("Gerg", "10.10.131.157 9566");

        if (args.length != 3) {
            System.out.println("Usage: java Client <server-username> <client-username> <1 for TCP | 2 for UDP>");
            return;
        }

        String serverUsername = args[0];
        String clientUsername = args[1];
        boolean useTCP = args[2].equals("1");

        String addressPort = navneServiceMap.get(serverUsername);

        if (addressPort == null) {
            System.out.println("Server username not found in the service map.");
            return;
        }

        String[] parts = addressPort.split(" ");
        if (parts.length != 2) {
            System.out.println("Invalid address and port format.");
            return;
        }

        String ip = parts[0];
        int port;
        try {
            port = Integer.parseInt(parts[1]);
        } catch (NumberFormatException e) {
            System.out.println("Invalid port number format.");
            return;
        }

        Client client = new Client(ip, port, clientUsername, useTCP);
        client.start();
    }
}