package Client;

import Utils.Operations;
import Utils.SocketUtils;
import com.fasterxml.jackson.core.type.TypeReference;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ClientServerSocketThread extends Thread {

    SocketUtils socketUtils = SocketUtils.getInstance();
    private final int port;
    private final String serverIp;
    private final int serverPort;
    private final List<ClientThread> threads;

    @Override
    public void run() {
        try {
            ServerSocket serverSocket = new ServerSocket(port);
            System.out.println("Waiting at " + serverIp + ":" + port);

            Socket socket = serverSocket.accept();
            System.out.println("Connected!");

            ObjectInputStream input = new ObjectInputStream(socket.getInputStream());
            while (true) {
                String commandType = input.readUTF();
                switch (commandType) {
                    case Operations.CONNECTING:
                        Socket s = new Socket(serverIp, serverPort);
                        socketUtils.sendSocketMessage(s, Operations.GET_CLIENT_THREAD_PORT);
                        int clientThreadPort = socketUtils.readPortFromSocketResponse(new ObjectInputStream(s.getInputStream()));
                        createThreadForNewIp(clientThreadPort);
                        ObjectOutputStream output = new ObjectOutputStream(s.getOutputStream());
                        output.write(clientThreadPort);
                        output.flush();
                        output.close();
                        s.getInputStream().close();
                        break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public ClientServerSocketThread(int port, String serverIp, int serverPort, List<ClientThread> threads) {
        this.port = port;
        this.serverIp = serverIp;
        this.serverPort = serverPort;
        this.threads = threads;
    }

    private void createThreadForNewIp(int threadPort) {
        ClientThread thread = new ClientThread(threadPort, null);
        thread.run();
        threads.add(thread);
    }

    private void removeIpsLeft(List<String> ipsLeft) {
        List<ClientThread> threadsToRemove = new ArrayList<>();

        for (String ip : ipsLeft) {
            for (ClientThread t : threads) {
                if (t.getPort() == Integer.parseInt(ip.split(":")[1])) {
                    threadsToRemove.add(t);
                    t.stopRunning();
                }
                break;
            }
        }

        threads.removeAll(threadsToRemove);
    }

}
