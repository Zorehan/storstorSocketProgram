package DNSTCP;

import java.io.*;
import java.net.*;
import java.util.HashMap;

public class DNS3 {
    private static final int DNS_PORT = 5358;
    private static HashMap<String, String> navneServiceMap = new HashMap<>();

    public static void main(String[] args) {
        // Tilføj nogle foruddefinerede brugernavne og deres tilhørende IP-adresser og porte
        navneServiceMap.put("Alex", "192.168.1.10 8080");
        navneServiceMap.put("Mikkel", "10.10.131.204 9566");
        navneServiceMap.put("Gerg", "10.10.131.241 9566");

        try (ServerSocket serverSocket = new ServerSocket(DNS_PORT)) {
            System.out.println("DNS server started on port " + DNS_PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("New DNS request from " + clientSocket.getInetAddress() + ":" + clientSocket.getPort());
                new Thread(new DnsRequestHandler(clientSocket)).start();
            }
        } catch (IOException e) {
            System.err.println("Error starting DNS server: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static class DnsRequestHandler implements Runnable {
        private Socket socket;

        public DnsRequestHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                 PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

                String request = in.readLine();
                System.out.println("Received DNS request: " + request);

                if (request.startsWith("LOOKUP")) {
                    String[] parts = request.split(" ");
                    if (parts.length == 2) {
                        String username = parts[1];
                        String address = navneServiceMap.get(username);

                        if (address != null) {
                            out.println(address);
                        } else {
                            out.println("ERROR Username not found");
                        }
                    } else {
                        out.println("ERROR Invalid lookup request");
                    }
                } else {
                    out.println("ERROR Unknown request");
                }
            } catch (IOException e) {
                System.err.println("Error handling DNS request: " + e.getMessage());
                e.printStackTrace();
            } finally {
                try {
                    socket.close();
                } catch (IOException e) {
                    System.err.println("Error closing DNS client socket: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }
    }
}

