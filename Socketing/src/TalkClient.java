import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;

public class TalkClient {

    public static void main(String[] args) {
        String host = "10.10.139.131"; // IP-adressen på serveren
        int port = 8899;          // Porten som serveren lytter på

        try (Socket socket = new Socket(host, port)) {
            System.out.println("Forbundet til serveren på " + host + ":" + port);

            // Opretter læser og skribent for at kommunikere med serveren
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));

            // Starter en tråd til at lytte efter beskeder fra serveren
            Thread listenerThread = new Thread(() -> {
                try {
                    String inputLine;
                    while ((inputLine = in.readLine()) != null) {
                        if (inputLine.equals("BYE")) {
                            System.out.println("Serveren afsluttede forbindelsen.");
                            break;
                        }
                        System.out.println("Server: " + inputLine);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            listenerThread.start();

            // Hovedtråden kan bruges til at sende beskeder til serveren
            String userInput;
            while ((userInput = stdIn.readLine()) != null) {
                out.println(userInput);
                if (userInput.equals("BYE")) {
                    System.out.println("Forbindelsen afsluttes.");
                    break;
                }
            }

            // Lukker forbindelsen
            socket.close();
            System.out.println("Forbindelsen til serveren er lukket.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}