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

    public ClientThread(String ip){
        this.ip = ip;
    }

    @Override
    public void run() {
        try {
            Socket s = new Socket(ip.split("/")[0], Integer.parseInt(ip.split("/")[1]));
            ObjectOutputStream output = new ObjectOutputStream(s.getOutputStream());

            ImageIcon icon;
            BufferedImage bImage;
            Webcam cam = Webcam.getDefault();
            cam.open();
            while (true) {
                bImage = cam.getImage();
                icon = new ImageIcon(bImage);
                output.writeObject(icon);
                output.flush();
//                img_client.setIcon(icon);
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
