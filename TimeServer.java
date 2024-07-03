import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.time.Instant;

public class TimeServer {
    public static void main(String[] args) {
        try (DatagramSocket socket = new DatagramSocket(12345)) {
            System.out.println("Time Server is running...");

            while (true) {
                byte[] buffer = new byte[256];
                DatagramPacket request = new DatagramPacket(buffer, buffer.length);

                socket.receive(request);
                // Tiempo de espera para simular latencia de red
                /*
                 * try {
                 * Thread.sleep(300);
                 * } catch (InterruptedException e) {
                 * // TODO Auto-generated catch block
                 * e.printStackTrace();
                 * }
                 */
                long currentTime = Instant.now().toEpochMilli();
                byte[] responseBuffer = Long.toString(currentTime).getBytes();

                InetAddress clientAddress = request.getAddress();
                int clientPort = request.getPort();
                DatagramPacket response = new DatagramPacket(responseBuffer, responseBuffer.length, clientAddress,
                        clientPort);
                socket.send(response);
            }
        } catch (IOException e) {
            System.err.println("Network I/O error - " + e);
        }
    }
}
