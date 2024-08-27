import java.io.*;
import java.net.*;

public class Client {
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private String username;

    public Client(String serverAddress, int serverPort, String username) {
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

    public static void main(String[] args) {
        if (args.length != 3) {
            System.out.println("Usage: java Client <server-address> <port> <username>");
            return;
        }
        String serverAddress = args[0];
        int serverPort = Integer.parseInt(args[1]);
        String username = args[2];

        Client client = new Client(serverAddress, serverPort, username);
        client.start();
    }
}