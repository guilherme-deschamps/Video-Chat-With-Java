package Utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class SocketUtils {

    private static SocketUtils instance;
    private ObjectMapper objectMapper = new ObjectMapper();
    private final String ip = "0.0.0.0";

    private SocketUtils() {
    }

    public static SocketUtils getInstance() {
        if (instance == null) {
            instance = new SocketUtils();
        }
        return instance;
    }

    public <T> T readSocketResponse(ObjectInputStream input, TypeReference<T> type) throws IOException {
        String jsonIps = input.readUTF();
        return objectMapper.readValue(jsonIps, type);
    }

    public int readPortFromSocketResponse(ObjectInputStream input) throws IOException {
        return input.read();
    }

    public void sendSocketMessage(Socket socket, String message) throws IOException {
        ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());

        output.writeUTF(message);
        output.flush();
    }

    public String getIp() {
        return ip;
    }
}
