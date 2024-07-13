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
            // Obtener la IP del equipo local
            InetAddress localHost = InetAddress.getLocalHost();
            clientIPAddress = localHost.getHostAddress();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        String serverAddress = "25.4.142.34"; // Cambiar por la IP del servidor
        int serverPort = 12345;
        String clientName = "Federico";

        // Parámetros de sincronizaciónd de reloj
        long[] serverTimeRTT = new long[2];
        long averageRoundTripTime = 0;
        int attempts = 10;

        // Solicitudes al servidor
        for (int i = 0; i < attempts; i++) {
            try {
                serverTimeRTT = requestServerTime(clientIPAddress, clientName, serverAddress, serverPort, i);
                averageRoundTripTime = +serverTimeRTT[1];
            } catch (Exception e) {
                e.printStackTrace();
            }
            
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

            // Convertir la lista a una secuencia de bytes
            ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
            ObjectOutputStream objStream = new ObjectOutputStream(byteStream);
            objStream.writeObject(listToSend);
            objStream.flush();
            byte[] byteArray = byteStream.toByteArray();
            long rtt;
            long[] serverTimeRTT = new long[2];

            // Crear y enviar el DatagramPacket
            InetAddress address = InetAddress.getByName(serverAddress);
            DatagramPacket packet = new DatagramPacket(byteArray, byteArray.length, address, serverPort);
            socket.setSoTimeout(10000);
            long t0 = System.currentTimeMillis();
            socket.send(packet);
            System.out.println("Request #" + (attempt + 1) + " sent to the server, awaiting response...");

            // Recibir el DatagramPacket
            byte[] buffer = new byte[1024];
            DatagramPacket response = new DatagramPacket(buffer, buffer.length);
            socket.receive(response);
            long t1 = System.currentTimeMillis();
            ByteArrayInputStream ReceivedByteStream = new ByteArrayInputStream(response.getData());
            ObjectInputStream ReceivedObjStream = new ObjectInputStream(ReceivedByteStream);
            List<?> receivedList = (List<?>) ReceivedObjStream.readObject();

            // Extraer la hora del servidor
            String serverTimeStr = (String) receivedList.get(2);
            long serverTime = Long.parseLong(serverTimeStr);

            // Calcular tiempo de retorno
            rtt = (t1 - t0) / 2;

            // Devolver hora del servidor y tiempo de retorno
            serverTimeRTT[0] = serverTime;
            serverTimeRTT[1] = rtt;
            return serverTimeRTT;
            
        } catch (InterruptedIOException iioe) { // Control de excepciones
            System.err.println("Remote host timed out during read operation");
        } catch (IOException e) {
            System.err.println("Network I/O error - " + e);
        } catch (ClassNotFoundException e) {
            System.err.println("Error" + e);
        } catch (Exception e) {
            System.err.println("Error" + e);
        }
        return null;
    }

}
