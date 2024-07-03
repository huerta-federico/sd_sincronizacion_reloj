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
                byte[] buffer = new byte[1024];
                DatagramPacket request = new DatagramPacket(buffer, buffer.length);
                socket.receive(request);

                // Deserialize the byte array back into a List
                ByteArrayInputStream ReceivedbyteStream = new ByteArrayInputStream(request.getData());
                ObjectInputStream ReceivedobjStream = new ObjectInputStream(ReceivedbyteStream);
                // Verificar advertencia
                @SuppressWarnings("unchecked")
                List<String> receivedList = (List<String>) ReceivedobjStream.readObject();

                InetAddress clientAddress = request.getAddress();
                int clientPort = request.getPort();

                long currentTime = Instant.now().toEpochMilli();
                receivedList.add(Long.toString(currentTime));

                /*
                 * // Print the received list
                 * System.out.println("Received list:");
                 * for (Object item : receivedList) {
                 * System.out.println(item);
                 * }
                 */

                ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
                ObjectOutputStream objStream = new ObjectOutputStream(byteStream);
                objStream.writeObject(receivedList);
                objStream.flush();
                byte[] byteArray = byteStream.toByteArray();

                // Create and send the DatagramPacket
                DatagramPacket packet = new DatagramPacket(byteArray, byteArray.length, clientAddress, clientPort);
                socket.send(packet);

            }
        } catch (IOException e) {
            System.err.println("Network I/O error - " + e);
        } catch (Exception e) {
            System.err.println("Error" + e);
        }
    }
}
