package Client;

import Utils.Operations;
import Utils.SocketUtils;

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
            System.out.println("ClienteServer Waiting at " + serverIp + ":" + port);

            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("Connected at ClienteServer!");

                ObjectInputStream input = new ObjectInputStream(socket.getInputStream());
                String commandType = input.readUTF();
                switch (commandType) {
                    case Operations.CONNECTING:
                        Socket s = new Socket(serverIp, serverPort);
                        socketUtils.sendServerMessage(s, Operations.GET_CLIENT_THREAD_PORT, port);
                        int clientThreadPort = socketUtils.readPortFromSocketResponse(new ObjectInputStream(s.getInputStream()));
                        System.out.println("Porta recebida pelo servidor: " + clientThreadPort);
                        createThreadForNewIp(clientThreadPort);
                        System.out.println("Criou a ClientThread e vai retornar a porta");
                        ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());
                        output.writeInt(clientThreadPort);
                        output.flush();
                        output.close();
                        System.out.println("Retornou a porta com sucesso");
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
        ClientThread clientThread = new ClientThread(threadPort, null);
        Thread thread = new Thread(clientThread);
        thread.start();
        threads.add(clientThread);
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
