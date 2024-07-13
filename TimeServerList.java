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
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class TimeServerList {

    private static final Logger logger = Logger.getLogger(TimeServerList.class.getName());

    public static void main(String[] args) {
        setupLogger();

        try (DatagramSocket socket = new DatagramSocket(12345)) {
            logger.info("Server is running...");

            while (true) {
                // A la espera de solicitudes
                byte[] buffer = new byte[1024];
                DatagramPacket request = new DatagramPacket(buffer, buffer.length);
                socket.receive(request);

                // Log la dirección IP y puerto del cliente
                InetAddress clientAddress = request.getAddress();
                int clientPort = request.getPort();
                logger.info("Received request from " + clientAddress + ":" + clientPort);

                // Convertir la secuencia de bytes a una lista
                ByteArrayInputStream receivedByteStream = new ByteArrayInputStream(request.getData());
                ObjectInputStream receivedObjStream = new ObjectInputStream(receivedByteStream);
                @SuppressWarnings("unchecked") // Verificar y depurar advertencia, podría ser Lista<?>
                List<String> receivedList = (List<String>) receivedObjStream.readObject();

                // Log la lista recibida
                logger.info("Received list: " + receivedList);

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

                // Log la respuesta enviada
                logger.info("Sent response to " + clientAddress + ":" + clientPort + " with list: " + receivedList);

            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Network I/O error - " + e.getMessage(), e); // Control de excepciones
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error - " + e.getMessage(), e); // Control de excepciones generales
        }
    }

    private static void setupLogger() {
        try {
            FileHandler fileHandler = new FileHandler("server.log", true);
            SimpleFormatter formatter = new SimpleFormatter();
            fileHandler.setFormatter(formatter);
            logger.addHandler(fileHandler);
            logger.setLevel(Level.ALL);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error setting up logger: " + e.getMessage(), e);
        }
    }
}
