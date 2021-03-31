package Client;

import com.github.sarxos.webcam.Webcam;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class ClientThread extends JFrame implements Runnable {

    private String ip;
    private boolean running;
    private BufferedImage bImage;
    private ImageIcon icon;
    private JPanel contentPane;
    public static JLabel img_client_thread;

    public ClientThread(String ip) {
        this.ip = ip;

        EventQueue.invokeLater(() -> {
            ClientThread frame = new ClientThread();
            frame.setVisible(true);
        });
    }

    public ClientThread() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(100, 100, 450, 300);
        contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        contentPane.setLayout(new BorderLayout(0, 0));
        setContentPane(contentPane);

        img_client_thread = new JLabel("Client Thread IP: " + ip);
        img_client_thread.setHorizontalAlignment(SwingConstants.CENTER);
        contentPane.add(img_client_thread, BorderLayout.CENTER);
    }

    @Override
    public void run() {
        running = true;
        try {
            Socket outputSocket = new Socket(ip.split(":")[0], Integer.parseInt(ip.split(":")[1]));
            ObjectOutputStream output = new ObjectOutputStream(outputSocket.getOutputStream());

            ServerSocket inputServerSocket = new ServerSocket(7800);
            Socket inputSocket = inputServerSocket.accept();
            ObjectInputStream inputImage = new ObjectInputStream(inputSocket.getInputStream());

            Webcam cam = Webcam.getDefault();
            cam.open();
            while (running) {
                sendImage(cam, output);
                receiveImage(inputImage);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void receiveImage(ObjectInputStream inputImage) {
        try {
            icon = (ImageIcon) inputImage.readObject();
            img_client_thread.setIcon(icon);
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public String getIp() {
        return ip;
    }

    public void stopRunning() {
        this.running = false;
    }

    private void sendImage(Webcam cam, ObjectOutputStream output) throws IOException {
        bImage = cam.getImage();
        icon = new ImageIcon(bImage);
        output.writeObject(icon);
        output.flush();
    }

}
