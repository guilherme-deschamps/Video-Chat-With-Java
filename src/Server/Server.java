package Server;

import Utils.Operations;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONArray;
import org.json.JSONObject;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

public class Server extends JFrame {

    private JPanel contentPane;
    public static JLabel img_server;
    private static List<String> clients = new ArrayList<>();

    /**
     * Launch the application.
     */
    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                Server frame = new Server();
                frame.setVisible(true);
            }
        });

        try {
            ServerSocket serverSocket = new ServerSocket(7800);
            System.out.println("Waiting..");

            while (true) {
                Socket s = serverSocket.accept();
                System.out.println("Connected!");

                ObjectInputStream input = new ObjectInputStream(s.getInputStream());
                ObjectOutputStream output = new ObjectOutputStream(s.getOutputStream());
                String commandType = input.readUTF();
                String ip = s.getLocalAddress().getHostAddress();

                switch (commandType) {
                    case Operations.DISCONNECTING:
                        break;
                    case Operations.UPDATE_IPS:
                        clients.remove(ip);
                        output.writeObject(clients);
                        output.flush();
                        clients.add(ip);
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
                s.close();

                System.out.println("Waiting..");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Create the frame.
     */
    public Server() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(100, 100, 450, 300);
        contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        contentPane.setLayout(new BorderLayout(0, 0));
        setContentPane(contentPane);

        img_server = new JLabel("Server");
        img_server.setHorizontalAlignment(SwingConstants.CENTER);
        contentPane.add(img_server, BorderLayout.CENTER);
    }
}
