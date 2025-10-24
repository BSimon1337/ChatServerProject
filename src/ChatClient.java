import java.io.*;
import java.net.*;
import java.util.Scanner;

/**
 * TCP Chat Client that connects to ChatServer
 * Allows user to send messages and receive message count from server
 */
public class ChatClient {
    // Server connection details
    private static final String SERVER_ADDRESS = "localhost"; // Use "localhost" for same machine
    private static final int SERVER_PORT = 9090;

    public static void main(String[] args) {
        // Socket for connection to server
        Socket socket = null;
        // Stream to send data to server
        PrintWriter out = null;
        // Stream to receive data from server
        BufferedReader in = null;
        // Scanner to read user input from keyboard
        Scanner scanner = new Scanner(System.in);

        try {
            // Connect to the server
            System.out.println("Connecting to server at " + SERVER_ADDRESS + ":" + SERVER_PORT + "...");
            socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
            System.out.println("Connected to server!\n");

            // Set up output stream to send messages to server (true = auto-flush)
            out = new PrintWriter(socket.getOutputStream(), true);

            // Set up input stream to receive messages from server
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // Main loop for user interaction
            while (true) {
                // Prompt user for input
                System.out.print("Enter message (type 'exit' to quit): ");
                String userMessage = scanner.nextLine();

                // Send message to server
                out.println(userMessage);

                // Check if user wants to exit
                if (userMessage.equals("exit")) {
                    System.out.println("Disconnecting from server...");

                    // Sleep for 2 seconds as required by project specs
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        System.err.println("Sleep interrupted: " + e.getMessage());
                    }

                    break; // Exit the loop
                }

                // Receive and display server's reply
                String serverReply = in.readLine();
                if (serverReply != null) {
                    System.out.println("Server response: " + serverReply + "\n");
                } else {
                    // Server closed connection
                    System.out.println("Server closed the connection.");
                    break;
                }
            }

        } catch (UnknownHostException e) {
            System.err.println("Error: Could not find server at " + SERVER_ADDRESS);
            e.printStackTrace();
        } catch (IOException e) {
            System.err.println("Error: Could not connect to server or I/O error occurred");
            e.printStackTrace();
        } finally {
            // Clean up resources
            try {
                if (scanner != null) scanner.close();
                if (out != null) out.close();
                if (in != null) in.close();
                if (socket != null) socket.close();
                System.out.println("Connection closed.");
            } catch (IOException e) {
                System.err.println("Error closing resources: " + e.getMessage());
            }
        }
    }
}