package Opg2;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class TalkServer {
    public static void main(String[] args) {
        int port = 12345; // Vælg en port for serveren
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("TalkServer kører på port " + port);

            // Vent på klientforbindelse
            try (Socket clientSocket = serverSocket.accept()) {
                System.out.println("Klient forbundet: " + clientSocket.getInetAddress());

                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));

                String inputLine;
                boolean connected = true;

                while (connected) {
                    // Modtag besked fra klienten
                    inputLine = in.readLine();
                    System.out.println("Modtaget fra klient: " + inputLine);

                    if (inputLine.equalsIgnoreCase("END")) {
                        connected = false; // Lukker forbindelsen
                    } else {
                        // Send besked til klienten
                        System.out.print("Server: ");
                        String response = stdIn.readLine();
                        out.println(response);

                        if (response.equalsIgnoreCase("END")) {
                            connected = false; // Lukker forbindelsen
                        }
                    }
                }
            } catch (IOException e) {
                System.err.println("Fejl ved klientforbindelse: " + e.getMessage());
            }

        } catch (IOException e) {
            System.err.println("Kunne ikke starte serveren: " + e.getMessage());
        }
    }
}
