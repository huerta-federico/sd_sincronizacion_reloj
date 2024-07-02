import java.net.*;
import java.io.*;
import java.util.Date;

public class TimeClient {
    public static void main(String[] args) {
        try {
            // Connect to the server on localhost, port 5000
            Socket socket = new Socket("localhost", 5000);

            // Create an input stream to read data from the server
            InputStream inputStream = socket.getInputStream();
            DataInputStream dataInputStream = new DataInputStream(inputStream);

            // Read the time sent by the server
            long time = dataInputStream.readLong();

            // Close streams and socket
            dataInputStream.close();
            inputStream.close();
            socket.close();

            // Print the time received from the server
            System.out.println("Current time received from server: " + new Date(time));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
