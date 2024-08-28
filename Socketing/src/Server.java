import java.io.*;
import java.net.*;
import java.util.*;

public class Server {
    private static Set<ClientHandler> clientHandlers = new HashSet<>();
    private static Set<InetSocketAddress> udpClients = new HashSet<>();
    private static final int TCPPort = 9566;
    private static final int UDPPort = 9566;

    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Usage: java Server 1 for TCP | 2 for UDP");
            return;
        }

        int modeDecider = Integer.parseInt(args[0]);

        if (modeDecider == 1) {
            startTCPServer();
        } else if (modeDecider == 2) {
            startUDPServer();
        } else {
            System.out.println("Invalid argument. Use 1 for TCP or 2 for UDP.");
        }
    }

    private static void startTCPServer() {
        try (ServerSocket serverSocket = new ServerSocket(TCPPort)) {
            System.out.println("TCP server started on port " + TCPPort);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("New TCP client connected from " + clientSocket.getInetAddress() + ":" + clientSocket.getPort());
                ClientHandler clientHandler = new ClientHandler(clientSocket);
                clientHandlers.add(clientHandler);
                new Thread(clientHandler).start();
            }
        } catch (IOException e) {
            System.err.println("Error starting TCP server: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void startUDPServer() {
        try (DatagramSocket datagramSocket = new DatagramSocket(UDPPort)) {
            System.out.println("UDP server started on port " + UDPPort);

            byte[] receiveBuffer = new byte[1024];
            DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);

            while (true) {
                datagramSocket.receive(receivePacket);
                String message = new String(receivePacket.getData(), 0, receivePacket.getLength());

                InetSocketAddress clientAddress = new InetSocketAddress(receivePacket.getAddress(), receivePacket.getPort());
                udpClients.add(clientAddress);

                broadcastUDPMessage(message, clientAddress, datagramSocket);
            }
        } catch (IOException e) {
            System.err.println("Error starting UDP server: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void broadcastUDPMessage(String message, InetSocketAddress senderAddress, DatagramSocket datagramSocket) {
        System.out.println("Broadcasting UDP message: " + message);
        byte[] sendBuffer = message.getBytes();

        for (InetSocketAddress clientAddress : udpClients) {
            if (!clientAddress.equals(senderAddress)) {
                try {
                    DatagramPacket sendPacket = new DatagramPacket(sendBuffer, sendBuffer.length, clientAddress.getAddress(), clientAddress.getPort());
                    datagramSocket.send(sendPacket);
                } catch (IOException e) {
                    System.err.println("Error sending UDP message: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }
    }

    public static void broadcastMessage(String message, ClientHandler sender) {
        System.out.println("Broadcasting TCP message: " + message);
        for (ClientHandler client : clientHandlers) {
            if (client != sender) {
                client.sendMessage(message);
            }
        }
    }

    private static class ClientHandler implements Runnable {
        private Socket socket;
        private PrintWriter out;
        private BufferedReader in;

        public ClientHandler(Socket socket) {
            this.socket = socket;
            try {
                this.out = new PrintWriter(socket.getOutputStream(), true);
                this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            } catch (IOException e) {
                System.err.println("Error setting up client handler: " + e.getMessage());
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            try {
                String message;
                while ((message = in.readLine()) != null) {
                    System.out.println("Received TCP message from client: " + message);
                    Server.broadcastMessage(message, this);
                }
            } catch (IOException e) {
                System.err.println("Error reading from TCP client: " + e.getMessage());
                e.printStackTrace();
            } finally {
                try {
                    socket.close();
                    System.out.println("TCP client disconnected from " + socket.getInetAddress() + ":" + socket.getPort());
                } catch (IOException e) {
                    System.err.println("Error closing TCP client socket: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }

        public void sendMessage(String message) {
            System.out.println("Sending TCP message to client: " + message);
            out.println(message);
        }
    }
}