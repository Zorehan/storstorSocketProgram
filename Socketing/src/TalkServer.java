import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class TalkServer {

    public static void main(String[] args) {
        int port = 8899; // Porten som serveren vil lytte på

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server lytter på port " + port);

            // Accepterer en klientforbindelse
            Socket clientSocket = serverSocket.accept();
            System.out.println("Forbundet med klient " + clientSocket.getInetAddress());

            // Opretter læser og skribent for at kommunikere med klienten
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
            BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));

            // Starter en tråd til at lytte efter beskeder fra klienten
            Thread listenerThread = new Thread(() -> {
                try {
                    String inputLine;
                    while ((inputLine = in.readLine()) != null) {
                        if (inputLine.equals("BYE")) {
                            System.out.println("Klienten afsluttede forbindelsen.");
                            break;
                        }
                        System.out.println("Klient: " + inputLine);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            listenerThread.start();

            // Hovedtråden kan bruges til at sende beskeder tilbage til klienten
            String userInput;
            while ((userInput = stdIn.readLine()) != null) {
                out.println(userInput);
                if (userInput.equals("BYE")) {
                    System.out.println("Forbindelsen afsluttes.");
                    break;
                }
            }

            // Lukker forbindelsen
            clientSocket.close();
            System.out.println("Forbindelsen til klienten er lukket.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}