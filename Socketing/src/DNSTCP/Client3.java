package DNSTCP;

import java.io.*;
import java.net.*;
import java.util.HashMap;

public class Client3 {
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private String username;

    public Client3(String serverAddress, int serverPort, String username) {
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
                    sendMessage(message);
                }
            } catch (IOException e) {
                System.err.println("Error reading from console: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Usage: java Client <server-username> <client-username>");
            return;
        }

        String serverUsername = args[0];
        String clientUsername = args[1];

        String serverAddress = lookupServerAddress(serverUsername);
        if (serverAddress == null) {
            System.out.println("Server address not found for username: " + serverUsername);
            return;
        }

        String[] parts = serverAddress.split(" ");
        String ip = parts[0];
        int port = Integer.parseInt(parts[1]);

        Client3 client = new Client3(ip, port, clientUsername);
        client.start();
    }

    private static String lookupServerAddress(String username) {
        try (Socket dnsSocket = new Socket("localhost", 5358);
             PrintWriter out = new PrintWriter(dnsSocket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(dnsSocket.getInputStream()))) {

            out.println("LOOKUP " + username);
            String response = in.readLine();
            if (response != null) {
                return response;
            } else {
                System.err.println("DNS lookup error: " + response);
                return null;
            }
        } catch (IOException e) {
            System.err.println("Error connecting to DNS server: " + e.getMessage());
            return null;
        }
    }
}