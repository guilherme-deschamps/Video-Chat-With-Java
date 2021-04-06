package Client;

import Utils.Operations;
import Utils.SocketUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;
import java.lang.reflect.Type;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
        System.out.println("IP recebido no cliente: " + serverIp);
        System.out.println("Porta recebida no cliente: " + clientPort);

        connectClient();
    }

    public void connectClient() {

        try {
            Socket socket = new Socket(serverIp, serverPort);
            List<String> ips = connectClientToServer(socket);

            //ips.forEach(i -> System.out.println("IP: " + i));

            List<ClientThread> threads = criaThreadsParaClientes(ips);
            ClientServerSocketThread clientServerThread = new ClientServerSocketThread(clientPort, serverIp, serverPort, threads);
            clientServerThread.start();

            while (running) {

                socket.getInputStream().close();
                //socket.getOutputStream().close();
                socket.close();

                socket = new Socket(serverIp, serverPort);
                sendConnectingMessage(socket, Operations.UPDATE_IPS);

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

//                    List<String> newIps = new ArrayList<>();
//                    for (String ip : currentServerIps) {
//                        if (!ips.contains(ip)) {
//                            newIps.add(ip);
//                        }
//                    }

//                    removeIpsLeft(ipsLeft);
//                    createThreadsForNewIps(newIps);

                    ips = currentServerIps;
                }

                Thread.sleep(2000);
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
        sendConnectingMessage(socket, Operations.CONNECTING + ":" + clientPort);

        List<String> ips = socketUtils.readSocketResponse(new ObjectInputStream(socket.getInputStream()),
                new TypeReference<List<String>>() {
                });
        Collections.sort(ips);
        return ips;
    }

    private List<ClientThread> criaThreadsParaClientes(List<String> ips) throws IOException {
        List<ClientThread> threads = new ArrayList<>();

        for (String ip : ips) {
            System.out.println("Novo cliente conectando-se em: "+socketUtils.getIp()+": "+ Integer.parseInt(ip.split(":")[1]));
            Socket socket = new Socket(socketUtils.getIp(), Integer.parseInt(ip.split(":")[1]));
            sendConnectingMessage(socket, Operations.CONNECTING);
            int receivedPort = socket.getInputStream().read();
            socket.getOutputStream().close();
            socket.getInputStream().close();

            Socket socketServer = new Socket(serverIp, serverPort);
            socketUtils.sendSocketMessage(socketServer, Operations.GET_CLIENT_THREAD_PORT);
            int port = socketUtils.readPortFromSocketResponse(new ObjectInputStream(socketServer.getInputStream()));

            ClientThread thread = new ClientThread(port, receivedPort);
            thread.run();
            threads.add(thread);
        }

        return threads;
    }

//    private void createThreadsForNewIps(List<String> newIps) throws IOException {
//        for (String ip : newIps) {
//            Socket socket = new Socket(ip.split(":")[0], Integer.parseInt(ip.split(":")[1]));
//            sendConnectingMessage(socket, Operations.CONNECTING);
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
