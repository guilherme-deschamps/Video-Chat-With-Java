package Client;

import Utils.Operations;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.sarxos.webcam.Webcam;
import org.json.JSONArray;
import org.json.JSONObject;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class Client extends JFrame {

    private JPanel contentPane;
    public static JLabel img_client;
    public static List<ClientThread> threads;
    public static final int port = 7801;
    public static ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Launch the application.
     */
    public static void main(String[] args) {

        EventQueue.invokeLater(new Runnable() {
            public void run() {
                Client frame = new Client();
                frame.setVisible(true);
            }
        });

        try {
            Socket socket = new Socket("127.0.0.1", 7800);
            ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream input = new ObjectInputStream(socket.getInputStream());

            output.writeUTF(Operations.CONNECTING + ":" + port);
            output.flush();

            String jsonIps = input.readUTF();
            List<String> ips = objectMapper.readValue(jsonIps, new TypeReference<List<String>>() {
            });

            ips.forEach(i -> System.out.println("IP: " + i));

            ImageIcon icon;
            BufferedImage bImage;
            Webcam cam = Webcam.getDefault();
            cam.open();
            while (true) {
                bImage = cam.getImage();
                icon = new ImageIcon(bImage);
//                output.writeObject(icon);
//                output.flush();
                img_client.setIcon(icon);
            }

//            enviaImagemProsClientes(ips);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Create the frame.
     */
    public Client() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(100, 100, 450, 300);
        contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        contentPane.setLayout(new BorderLayout(0, 0));
        setContentPane(contentPane);

        img_client = new JLabel("Client");
        img_client.setHorizontalAlignment(SwingConstants.CENTER);
        contentPane.add(img_client, BorderLayout.CENTER);
    }

    private static void enviaImagemProsClientes(List<String> ips) throws IOException {
        for(String ip : ips){
            ClientThread thread = new ClientThread(ip);
            thread.start();
            threads.add(thread);
        }
    }
}
