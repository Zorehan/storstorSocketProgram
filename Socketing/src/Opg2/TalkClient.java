package Opg2;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

public class TalkClient {
    public static void main(String[] args) {
        String hostname = "localhost"; // IP-adresse eller hostname for serveren
        int port = 12345; // Porten skal matche serverens port

        try (Socket socket = new Socket(hostname, port)) {
            System.out.println("Forbundet til server: " + hostname + " på port " + port);

            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

            // Brug en BufferedReader til at læse fra konsollen
            BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
            String userInput;

            // Brug et flag for at kontrollere om forbindelsen skal lukkes
            boolean connected = true;

            while (connected) {
                // Send besked til serveren
                System.out.print("Klient: ");
                userInput = stdIn.readLine();
                out.println(userInput);

                if (userInput.equalsIgnoreCase("END")) {
                    connected = false; // Lukker forbindelsen
                } else {
                    // Modtag besked fra serveren
                    String response = in.readLine();
                    System.out.println("Modtaget fra server: " + response);
                    if (response.equalsIgnoreCase("END")) {
                        connected = false; // Lukker forbindelsen
                    }
                }
            }

        } catch (UnknownHostException e) {
            System.err.println("Ukendt host: " + hostname);
        } catch (IOException e) {
            System.err.println("Fejl ved forbindelse til server: " + e.getMessage());
        }
    }
}
