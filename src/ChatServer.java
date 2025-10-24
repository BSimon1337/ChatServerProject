import java.io.*;
import java.net.*;

/*
 * TCP Chat Server that handles multiple clients simultaneously
 * and counts total messages received since server start
 */

public class ChatServer {
    // Shared message counter - accessed by multiple threads
    private static int messageCount = 0;
    // Port number for the server to listen on
    private static final int PORT = 9090;

    public static void main(String[] args) {
        System.out.println("Chat server starting on port " + PORT + "...");

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server is running and waiting for clients...\n");

// Infinite loop to accept multiple client connections
            while (true) {
                // Wait for a client to connect (blocking call)
                Socket clientSocket = serverSocket.accept();

                // Print connection info when client connects
                System.out.println("New client connected from: " +
                        clientSocket.getInetAddress() +
                        ":" + clientSocket.getPort());

                // Create a new thread to handle this client
                // This allows server to handle multiple clients simultaneously
                ClientHandler handler = new ClientHandler(clientSocket);
                Thread thread = new Thread(handler);
                thread.start();
            }
        } catch (IOException e) {
            System.err.println("Server error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Synchronized method to increment message counter
     * Only one thread can execute this at a time (prevents race conditions)
     */
    private static synchronized int incrementAndGetCount() {
        messageCount++;
        return messageCount;
    }

    /**
     * Inner class that handles communication with a single client
     * Each instance runs in its own thread
     */
    static class ClientHandler implements Runnable {
        private Socket clientSocket;
        private BufferedReader in;
        private PrintWriter out;

        public ClientHandler(Socket socket) {
            this.clientSocket = socket;
        }

        @Override
        public void run() {
            try {
                // Set up input stream to read from client
                in = new BufferedReader(
                        new InputStreamReader(clientSocket.getInputStream())
                );

                // Set up output stream to send to client (true = auto-flush)
                out = new PrintWriter(clientSocket.getOutputStream(), true);

                String message;

                // Loop to continuously receive messages from this client
                while ((message = in.readLine()) != null) {
                    // Check if client wants to exit
                    if (message.equals("exit")) {
                        System.out.println("Client " + clientSocket.getInetAddress() +
                                ":" + clientSocket.getPort() +
                                " is disconnecting...");
                        break; // Exit the loop and end this thread
                    }

                    // Print received message with client info
                    System.out.println("Received from " +
                            clientSocket.getInetAddress() +
                            ":" + clientSocket.getPort() +
                            " - Message: " + message);

                    // Increment counter and get the new count (synchronized)
                    int currentCount = incrementAndGetCount();

                    // Send reply with total message count
                    out.println("Total messages received by server: " + currentCount);
                }

            } catch (IOException e) {
                System.err.println("Error handling client " +
                        clientSocket.getInetAddress() + ": " +
                        e.getMessage());
            } finally {
                // Clean up resources when client disconnects
                try {
                    if (in != null) in.close();
                    if (out != null) out.close();
                    if (clientSocket != null) clientSocket.close();
                    System.out.println("Client " + clientSocket.getInetAddress() +
                            " disconnected.\n");
                } catch (IOException e) {
                    System.err.println("Error closing resources: " + e.getMessage());
                }
            }
        }
    }
}