package Ignore;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class TimeClientList {
    public static void main(String[] args) {
        String ipAddress = "";
        try {
            // Get the local host address
            InetAddress localHost = InetAddress.getLocalHost();
            ipAddress = localHost.getHostAddress();

            System.out.println("IP Address: " + ipAddress);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        // Cambia por IP del servidor en Hamachi
        String servidorRemoto = "192.168.1.44";
        String servidorLocal = "localhost";
        String serverAddress = servidorLocal; // Change this to the server's address if needed
        int serverPort = 12345;
        String nombreCliente = "Federico";
        List<String> listToSend = new ArrayList<>();
        listToSend.add(nombreCliente);
        listToSend.add(ipAddress);

        try (DatagramSocket socket = new DatagramSocket()) {
            // Serialize the list into a byte array
            ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
            ObjectOutputStream objStream = new ObjectOutputStream(byteStream);
            objStream.writeObject(listToSend);
            objStream.flush();
            byte[] byteArray = byteStream.toByteArray();

            // Create and send the DatagramPacket
            InetAddress address = InetAddress.getByName(serverAddress);
            DatagramPacket packet = new DatagramPacket(byteArray, byteArray.length, address, serverPort);
            socket.setSoTimeout(10000);
            long t0 = System.currentTimeMillis();
            System.out.println("Current Client Time: " + new Date(t0));
            socket.send(packet);

            System.out.println("Request sent to the server, awaiting response...\n");

            byte[] buffer = new byte[1024];
            DatagramPacket response = new DatagramPacket(buffer, buffer.length);
            socket.receive(response);

            long t1 = System.currentTimeMillis();

            ByteArrayInputStream ReceivedByteStream = new ByteArrayInputStream(response.getData());
            ObjectInputStream ReceivedObjStream = new ObjectInputStream(ReceivedByteStream);
            List<?> receivedList = (List<?>) ReceivedObjStream.readObject();

            String serverTimeStr = (String) receivedList.get(2);

            // Parsear serverTimeStr, añadir más campos
            long serverTime = Long.parseLong(serverTimeStr);

            // Calculate round-trip delay and adjusted time
            long roundTripDelay = (t1 - t0) / 2;
            long adjustedTime = serverTime + roundTripDelay;

            System.out.println("Client's name: " + receivedList.get(0));
            System.out.println("Client's IP: " + receivedList.get(1));
            System.out.println("Server Time: " + new Date(serverTime));
            System.out.println("Round-Trip Delay: " + roundTripDelay + "ms");
            System.out.println("Adjusted Client Time: " + new Date(adjustedTime));

        } catch (InterruptedIOException iioe) {
            System.err.println("Remote host timed out during read operation");
        } catch (IOException e) {
            System.err.println("Network I/O error - " + e);
        } catch (ClassNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }
}
