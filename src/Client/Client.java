package Client;

import Utils.Operations;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Client extends Thread {

    public List<ClientThread> threads;
    public final int port = 7801;
    public ObjectMapper objectMapper = new ObjectMapper();
    private boolean running = true;
    private final String serverIp;
    private final int serverPort;

    public Client(String serverIp, int serverPort) {
        this.serverIp = serverIp;
        this.serverPort = serverPort;
    }

    @Override
    public void run() {
        System.out.println("IP recebido no cliente: " + serverIp);
        System.out.println("Porta recebida no cliente: " + serverPort);

        connectClient();
    }

    public void connectClient() {

        try {
            Socket socket = new Socket(serverIp, serverPort);
            ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream input = new ObjectInputStream(socket.getInputStream());

            output.writeUTF(Operations.CONNECTING + ":" + port);
            output.flush();

            List<String> ips = readIpsList(input);
            Collections.sort(ips);

            ips.forEach(i -> System.out.println("IP: " + i));

            enviaImagemProsClientes(ips);

            while (running) {
                Thread.sleep(5000);
                
                output.writeUTF(Operations.UPDATE_IPS);
                output.flush();

                List<String> currentServerIps = readIpsList(input);
                Collections.sort(currentServerIps);
                if (!currentServerIps.equals(ips)) {
                    List<String> ipsLeft = new ArrayList<>();
                    for (String ip : ips) {
                        if (!currentServerIps.contains(ip)) {
                            ipsLeft.add(ip);
                        }
                    }

                    List<String> newIps = new ArrayList<>();
                    for (String ip : currentServerIps) {
                        if (!ips.contains(ip)) {
                            newIps.add(ip);
                        }
                    }

                    removeIpsLeft(ipsLeft);
                    createThreadsForNewIps(newIps);

                    ips = currentServerIps;
                }
            }

            threads.forEach(ClientThread::stopRunning);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void enviaImagemProsClientes(List<String> ips) {
        for (String ip : ips) {
            ClientThread thread = new ClientThread(ip);
            thread.run();
            threads.add(thread);
        }
    }

    private List<String> readIpsList(ObjectInputStream input) throws IOException {
        String jsonIps = input.readUTF();
        return objectMapper.readValue(jsonIps, new TypeReference<List<String>>() {
        });
    }

    private void removeIpsLeft(List<String> ipsLeft) {
        List<ClientThread> threadsToRemove = new ArrayList<>();

        for (String ip : ipsLeft) {
            for (ClientThread t : threads) {
                if (t.getIp().equals(ip)) {
                    threadsToRemove.add(t);
                    t.stopRunning();
                }
                break;
            }
        }

        threads.removeAll(threadsToRemove);
    }

    private void createThreadsForNewIps(List<String> newIps) {
        for (String ip : newIps) {
            ClientThread thread = new ClientThread(ip);
            thread.run();
            threads.add(thread);
        }
    }
}
