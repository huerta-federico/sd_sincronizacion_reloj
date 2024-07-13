import java.net.*;
import java.io.*;

public class TimeServer {
    public static void main(String[] args) {
        try {
            // Create a server socket listening on port 5000
            ServerSocket serverSocket = new ServerSocket(5000);
            System.out.println("Time server started. Listening on port 5000...");

            while (true) {
                // Wait for a client connection
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client connected: " + clientSocket.getInetAddress().getHostAddress());

                // Get the current time
                long currentTime = System.currentTimeMillis();

                // Create an output stream to send the time to the client
                OutputStream outputStream = clientSocket.getOutputStream();
                DataOutputStream dataOutputStream = new DataOutputStream(outputStream);

                // Send the current time to the client
                dataOutputStream.writeLong(currentTime);
                dataOutputStream.flush();

                // Close streams and socket
                dataOutputStream.close();
                outputStream.close();
                clientSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
