package Utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.sarxos.webcam.Webcam;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class SocketUtils {

    private static SocketUtils instance;
    private ObjectMapper objectMapper = new ObjectMapper();
    private final String ip = "0.0.0.0";
    private Webcam cam = Webcam.getDefault();
    private int clientNumber = 1;

    private SocketUtils() {
        cam.open();
    }

    public static SocketUtils getInstance() {
        if (instance == null) {
            instance = new SocketUtils();
        }
        return instance;
    }

    public synchronized <T> T readSocketResponse(ObjectInputStream input, TypeReference<T> type) throws IOException {
        String jsonIps = input.readUTF();
        return objectMapper.readValue(jsonIps, type);
    }

    public synchronized int readPortFromSocketResponse(ObjectInputStream input) throws IOException {
        return input.readInt();
    }

    public synchronized void sendServerMessage(Socket socket, String message, int port) throws IOException {
        ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());
        System.out.println("IP " + ip + ":" + port + " connecting to server!");

        output.writeUTF(message);
        output.flush();
    }

    public void sendThreadMessage(Socket socket, String message, int port) throws IOException {
        ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());
        System.out.println("IP " + ip + ":" + port + " connecting to " + socket.getInetAddress().getHostAddress() + ":" + socket.getPort());

        output.writeUTF(message);
        output.flush();
    }

    public String getIp() {
        return ip;
    }

    public Webcam getCam() {
        return cam;
    }

    public int getClientNumber() {
        clientNumber++;
        return clientNumber - 1;
    }
}
