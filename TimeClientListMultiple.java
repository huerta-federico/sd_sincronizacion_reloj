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

public class TimeClientListMultiple {
    public static void main(String[] args) {

        // Parámetros de conexión
        String clientIPAddress = "";
        try {
            // Get the local host address
            InetAddress localHost = InetAddress.getLocalHost();
            clientIPAddress = localHost.getHostAddress();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        String servidorRemoto = "192.168.1.44";
        String servidorLocal = "localhost";
        String serverAddress = servidorLocal; // Change this to the server's address if needed
        int serverPort = 12345;
        String clientName = "Federico";

        // Parámetros de sincronizaciónd de reloj
        long[] serverTimeRTT = new long[2];
        long averageRoundTripTime = 0;
        int attempts = 10;

        // Solicitudes al servidor
        for (int i = 0; i < attempts; i++) {
            serverTimeRTT = requestServerTime(clientIPAddress, clientName, serverAddress, serverPort, i);
            averageRoundTripTime = +serverTimeRTT[1];
        }

        // Cálculo del RTT promedio y ajuste del reloj
        averageRoundTripTime = averageRoundTripTime / attempts;
        long t0 = System.currentTimeMillis();
        long adjustedTime = serverTimeRTT[0] + averageRoundTripTime;
        long timeDifference = t0 - adjustedTime;

        // Salida de datos en consola
        System.out.println("\nClient's name: \t\t" + clientName);
        System.out.println("Client's IP: \t\t" + clientIPAddress);
        System.out.println("Round-Trip Delay: \t" + averageRoundTripTime + "ms");
        System.out.println("Current Client Time: \t" + new Date(t0));
        System.out.println("Server Time: \t\t" + new Date(serverTimeRTT[0]));
        System.out.println("Adjusted Client Time: \t" + new Date(adjustedTime));
        System.out.println("Time difference: \t" + timeDifference + "ms");

    }

    public static long[] requestServerTime(String clientIPAddress, String clientName, String serverAddress,
            int serverPort, int attempt) {

        try (DatagramSocket socket = new DatagramSocket()) {
            // Lista con la IP y nombre del cliente
            List<String> listToSend = new ArrayList<>();
            listToSend.add(clientName);
            listToSend.add(clientIPAddress);

            // Serialize the list into a byte array
            ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
            ObjectOutputStream objStream = new ObjectOutputStream(byteStream);
            objStream.writeObject(listToSend);
            objStream.flush();
            byte[] byteArray = byteStream.toByteArray();
            long rtt;
            long[] serverTimeRTT = new long[2];

            // Create and send the DatagramPacket
            InetAddress address = InetAddress.getByName(serverAddress);
            DatagramPacket packet = new DatagramPacket(byteArray, byteArray.length, address, serverPort);
            socket.setSoTimeout(10000);
            long t0 = System.currentTimeMillis();
            socket.send(packet);
            System.out.println("Request #" + (attempt + 1) + " sent to the server, awaiting response...");

            // Receive DatagramPacket
            byte[] buffer = new byte[1024];
            DatagramPacket response = new DatagramPacket(buffer, buffer.length);
            socket.receive(response);
            long t1 = System.currentTimeMillis();
            ByteArrayInputStream ReceivedByteStream = new ByteArrayInputStream(response.getData());
            ObjectInputStream ReceivedObjStream = new ObjectInputStream(ReceivedByteStream);
            List<?> receivedList = (List<?>) ReceivedObjStream.readObject();

            // Extract server time
            String serverTimeStr = (String) receivedList.get(2);
            long serverTime = Long.parseLong(serverTimeStr);

            // Calculate round-trip time
            rtt = (t1 - t0) / 2;

            // Return server time and RTT
            serverTimeRTT[0] = serverTime;
            serverTimeRTT[1] = rtt;
            return serverTimeRTT;

        } catch (InterruptedIOException iioe) {
            System.err.println("Remote host timed out during read operation");
        } catch (IOException e) {
            System.err.println("Network I/O error - " + e);
        } catch (ClassNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
