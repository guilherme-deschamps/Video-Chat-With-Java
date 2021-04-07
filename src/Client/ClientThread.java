package Client;

import Utils.SocketUtils;
import com.github.sarxos.webcam.Webcam;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Base64;

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

    }

    public ClientThread() {
    }

    public void startFrame() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(100, 100, 450, 300);
        contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        contentPane.setLayout(new BorderLayout(0, 0));
        setContentPane(contentPane);

        img_client_thread = new JLabel("Client " + socketUtils.getClientNumber());
        img_client_thread.setHorizontalAlignment(SwingConstants.CENTER);
        contentPane.add(img_client_thread, BorderLayout.CENTER);
        this.setVisible(true);
    }

    @Override
    public void run() {
        running = true;
        startFrame();

        try {

            Socket socket;
            if (socketPort == null) {
                System.out.println("ClientThread server sendo criado na porta: " + port);
                ServerSocket serverSocket = new ServerSocket(port);
                socket = serverSocket.accept();
            } else {
                socket = new Socket(socketUtils.getIp(), socketPort);
            }

            ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream inputImage = new ObjectInputStream(socket.getInputStream());

            while (running) {
                sendImage(socketUtils.getCam(), output);
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
            System.out.println("IMAGEM RECEBIDA: " + icon.getImage().toString() + "; ICON HASH CODE: " + icon.getImage().hashCode());
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
        System.out.println("IMAGEM ENVIADA: " + icon.getImage().toString() +"; ICON HASH CODE: " + icon.getImage().hashCode());
        output.writeObject(icon);
        output.flush();
    }

}
