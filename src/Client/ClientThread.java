package Client;

import com.github.sarxos.webcam.Webcam;

import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ClientThread extends Thread {

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

}
