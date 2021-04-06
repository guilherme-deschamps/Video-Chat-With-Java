package Client;

import Utils.SocketUtils;
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

    private SocketUtils socketUtils = SocketUtils.getInstance();
    private int port;
    private Integer socketPort;
    private boolean running;
    private BufferedImage bImage;
    private ImageIcon icon;
    private JPanel contentPane;
    public static JLabel img_client_thread;

    public ClientThread(int port, Integer socketPort) {
        this.port = port;
        this.socketPort = socketPort;

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

        img_client_thread = new JLabel("Client Thread IP: " + socketUtils.getIp());
        img_client_thread.setHorizontalAlignment(SwingConstants.CENTER);
        contentPane.add(img_client_thread, BorderLayout.CENTER);
    }

    @Override
    public void run() {
        running = true;

        try {

            Socket socket;
            if (socketPort == null) {
                ServerSocket serverSocket = new ServerSocket(port);
                socket = serverSocket.accept();
            } else {
                socket = new Socket(socketUtils.getIp(), socketPort);
            }

            ObjectOutputStream output    = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream inputImage = new ObjectInputStream(socket.getInputStream());

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

    public int getPort() {
        return port;
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
