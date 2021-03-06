package Client;

import Utils.Operations;
import Utils.SocketUtils;
import com.fasterxml.jackson.core.type.TypeReference;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class Client extends Thread {

    SocketUtils socketUtils = SocketUtils.getInstance();
    private boolean running = true;
    private final String serverIp;
    private final int serverPort;
    private final int clientPort = (int) (7801 + (Math.random() * 100));

    public Client(String serverIp, int serverPort) {
        this.serverIp = serverIp;
        this.serverPort = serverPort;
    }

    @Override
    public void run() {
        System.out.println("Porta recebida no cliente: " + clientPort);

        connectClient();
    }

    public void connectClient() {

        try {
            Socket socket = new Socket(serverIp, serverPort);
            List<String> ips = connectClientToServer(socket);

            List<ClientThread> threads = criaThreadsParaClientes(ips);
            ClientServerSocketThread clientServerThread = new ClientServerSocketThread(clientPort, serverIp, serverPort, threads);
            clientServerThread.start();

            while (running) {

                socket.getInputStream().close();
                socket.close();

                socket = new Socket(serverIp, serverPort);
                socketUtils.sendServerMessage(socket, Operations.UPDATE_IPS, clientPort);

                ObjectInputStream input = new ObjectInputStream(socket.getInputStream());
                List<String> currentServerIps = socketUtils.readSocketResponse(input, new TypeReference<List<String>>() {
                });
                Collections.sort(currentServerIps);
                if (!currentServerIps.equals(ips)) {
                    List<String> ipsLeft = new ArrayList<>();
                    for (String ip : ips) {
                        if (!currentServerIps.contains(ip)) {
                            ipsLeft.add(ip);
                        }
                    }

                    ips = currentServerIps;
                }

                Thread.sleep(new Random().nextInt(3000) + 1000);
            }

            socket.getOutputStream().close();
            socket.getInputStream().close();
            socket.close();

            threads.forEach(ClientThread::stopRunning);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private List<String> connectClientToServer(Socket socket) throws IOException {
        socketUtils.sendServerMessage(socket, Operations.CONNECTING + ":" + clientPort, clientPort);

        List<String> ips = socketUtils.readSocketResponse(new ObjectInputStream(socket.getInputStream()),
                new TypeReference<List<String>>() {
                });
        Collections.sort(ips);
        return ips;
    }

    private List<ClientThread> criaThreadsParaClientes(List<String> ips) throws IOException {
        List<ClientThread> threads = new ArrayList<>();

        for (String ip : ips) {
            Socket socket = new Socket(socketUtils.getIp(), Integer.parseInt(ip.split(":")[1]));
            socketUtils.sendThreadMessage(socket, Operations.CONNECTING, clientPort);
            int receivedPort = new ObjectInputStream(socket.getInputStream()).readInt();
            socket.getOutputStream().close();

            Socket socketServer = new Socket(serverIp, serverPort);
            socketUtils.sendServerMessage(socketServer, Operations.GET_CLIENT_THREAD_PORT, clientPort);
            int port = socketUtils.readPortFromSocketResponse(new ObjectInputStream(socketServer.getInputStream()));

            ClientThread clientThread = new ClientThread(port, receivedPort);
            Thread thread = new Thread(clientThread);
            thread.start();
            threads.add(clientThread);
        }

        return threads;
    }

//    private void createThreadsForNewIps(List<String> newIps) throws IOException {
//        for (String ip : newIps) {
//            Socket socket = new Socket(ip.split(":")[0], Integer.parseInt(ip.split(":")[1]));
//            socketUtils.sendSocketMessage(socket, Operations.CONNECTING);
//            int receivedPort = socket.getInputStream().read();
//            socket.getOutputStream().close();
//            socket.getInputStream().close();
//
//            ClientThread thread = new ClientThread(ip, socketUtils.getIp()) + ":" + receivedPort);
//            thread.run();
////            threads.add(thread);
//        }
//    }

    private void sendConnectingMessage(Socket socket, String message) throws IOException {
        ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());

        output.writeUTF(message);
        output.flush();
    }
}
