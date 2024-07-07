import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.time.Instant;
import java.util.List;

public class TimeServerList {
    public static void main(String[] args) {
        try (DatagramSocket socket = new DatagramSocket(12345)) {
            System.out.println("Server is running...");

            while (true) {
                // A la espera de solicitudes
                byte[] buffer = new byte[1024];
                DatagramPacket request = new DatagramPacket(buffer, buffer.length);
                socket.receive(request);

                // Convertir la secuencia de bytes a una lista
                ByteArrayInputStream ReceivedbyteStream = new ByteArrayInputStream(request.getData());
                ObjectInputStream ReceivedobjStream = new ObjectInputStream(ReceivedbyteStream);
                @SuppressWarnings("unchecked") // Verificar y depurar advertencia, podría ser Lista<?>
                List<String> receivedList = (List<String>) ReceivedobjStream.readObject();

                // Obtiene la IP y puerto del cliente para enviar la respuesta
                InetAddress clientAddress = request.getAddress();
                int clientPort = request.getPort();

                // Obtiene la hora del servidor y lo agrega a la lista
                long currentTime = Instant.now().toEpochMilli();
                receivedList.add(Long.toString(currentTime));

                // Convierte la lista a una secuencia de bytes
                ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
                ObjectOutputStream objStream = new ObjectOutputStream(byteStream);
                objStream.writeObject(receivedList);
                objStream.flush();
                byte[] byteArray = byteStream.toByteArray();

                // Crea y envía el DatagramPacket
                DatagramPacket packet = new DatagramPacket(byteArray, byteArray.length, clientAddress, clientPort);
                socket.send(packet);

            }
        } catch (IOException e) {
            System.err.println("Network I/O error - " + e); // Control de excepciones
        } catch (Exception e) {
            System.err.println("Error" + e);
        }
    }
}
