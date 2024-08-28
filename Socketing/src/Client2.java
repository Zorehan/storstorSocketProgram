
import java.io.*;
import java.net.*;
import java.util.HashMap;

public class Client2 {
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private String username;
    private static HashMap<String, String> navneServiceMap = new HashMap<>();

    public Client2(String serverAddress, int serverPort, String username) {
        this.username = username;
        try {
            System.out.println("Connecting to server...");
            socket = new Socket(serverAddress, serverPort);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
            System.out.println("Connected to server at " + serverAddress + ":" + serverPort);
        } catch (IOException e) {
            System.err.println("Error connecting to server: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void start() {
        new Thread(new IncomingReader()).start();
        new Thread(new OutgoingSender()).start();
//        try (BufferedReader consoleReader = new BufferedReader(new InputStreamReader(System.in))) {
//            String message;
//            while ((message = consoleReader.readLine()) != null) {
//                if (message.toLowerCase().equals("info")) {
//                    System.out.println("the different users you can connect to: ");
//                    System.out.println(navneServiceMap.toString());
//                } else {
//                    sendMessage(message);
//                }
//            }
//        } catch (IOException e) {
//            System.err.println("Error reading from console: " + e.getMessage());
//            e.printStackTrace();
//        }
    }

    public void sendMessage(String message) {
        System.out.println("Sending message: " + message);
        out.println(username + ": " + message);
    }

    private class IncomingReader implements Runnable {
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

    private class OutgoingSender implements Runnable {
        @Override
        public void run() {
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
    }

    public static void main(String[] args) {
        navneServiceMap.put("Alex", "192.168.1.10 8080");
        navneServiceMap.put("Mikkel", "10.10.131.204 9566");
        navneServiceMap.put("Gerg", "10.10.131.241 9566");

        if (args.length != 2) {
            System.out.println("Usage: java Client <server-username> <client-username>");
            return;
        }

        String serverUsername = args[0];
        String clientUsername = args[1];

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

        Client2 client = new Client2(ip, port, clientUsername);
        client.start();
    }
}