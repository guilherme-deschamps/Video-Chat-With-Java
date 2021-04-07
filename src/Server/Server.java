package Server;

import Utils.Operations;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;

public class Server extends Thread {

    private List<String> clients = new ArrayList<>();
    private int port;
    private ServerSocket serverSocket;
    private int currentClientThreadPort = 1;

    public Server(int serverPort) {
        this.port = serverPort;
    }

    @Override
    public void run() {
        connectServer();
    }

    public void connectServer() {
        try {
            serverSocket = new ServerSocket(port);
            System.out.println("Waiting at " + getServerIp() + ":" + getServerPort());

            while (true) {
                Socket s = serverSocket.accept();

                ObjectInputStream input = new ObjectInputStream(s.getInputStream());
                String commandType = input.readUTF();
                String ip = s.getLocalAddress().getHostAddress();

                ObjectOutputStream output = new ObjectOutputStream(s.getOutputStream());
                switch (commandType) {
                    case Operations.DISCONNECTING:
                        break;
                    case Operations.UPDATE_IPS:
                        String removedIp = "";

                        for (String c : clients) {
                            if (c.contains(ip)) {
                                removedIp = c;
                                break;
                            }
                        }
                        clients.remove(removedIp);

                        output.writeUTF(new ObjectMapper().writeValueAsString(clients));
                        output.flush();
                        clients.add(removedIp);
                        break;
                    case Operations.GET_CLIENT_THREAD_PORT:
                        output.writeInt(7900 + currentClientThreadPort);
                        output.flush();
                        currentClientThreadPort++;
                        break;
                    default:
                        if (commandType.contains(Operations.CONNECTING)) {
                            int port = Integer.parseInt(commandType.split(":")[1]);

                            output.writeUTF(new ObjectMapper().writeValueAsString(clients));
                            output.flush();
                            clients.add(ip + ":" + port);
                        } else {
                            throw new IOException("Connection refused! Try again");
                        }
                }
                input.close();
                output.close();
                s.close();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int getServerPort() {
        return this.port;
    }

    public String getServerIp() {
        return serverSocket.getInetAddress().getHostAddress();
    }
}
