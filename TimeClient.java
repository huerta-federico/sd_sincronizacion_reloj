import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import java.util.Date;


public class TimeClient {
    public static void main(String[] args) {
        String servidorRemoto = "192.168.1.81";
        String servidorLocal = "localhost";
        String serverAddress = servidorRemoto; // Change this to the server's address if needed
        int serverPort = 12345;

        try (DatagramSocket socket = new DatagramSocket()) {
            // Send request to the server
            byte[] buffer = new byte[256];
            InetAddress address = InetAddress.getByName(serverAddress);
            DatagramPacket request = new DatagramPacket(buffer, buffer.length, address, serverPort);
            socket.setSoTimeout(10000);

            //El reloj cliente está adelantado 10s
            long t0 = System.currentTimeMillis() + 10000;
            System.out.println("Current Client Time: " + new Date(t0));
            socket.send(request);

            // Receive response from the server
            DatagramPacket response = new DatagramPacket(buffer, buffer.length);
            socket.receive(response);

            //El reloj cliente está adelantado 10s
            long t1 = System.currentTimeMillis() + 10000;

            String serverTimeStr = new String(response.getData(), 0, response.getLength());
            long serverTime = Long.parseLong(serverTimeStr);

            // Calculate round-trip delay and adjusted time
            long roundTripDelay = (t1 - t0) / 2;
            long adjustedTime = serverTime + roundTripDelay;

            System.out.println("Server Time: " + new Date(serverTime));
            System.out.println("Round-Trip Delay: " + roundTripDelay + "ms");
            System.out.println("Adjusted Client Time: " + new Date(adjustedTime));
        } 
        catch (InterruptedIOException iioe) {
            System.err.println("Remote host timed out during read operation");
        } catch (IOException e) {
            System.err.println("Network I/O error - " + e);
        } 
    }
}
