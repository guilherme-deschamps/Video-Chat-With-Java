import Client.Client;
import Server.Server;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class Main extends JFrame {

    private static Server server;
    private JPanel contentPane;

    public static void main(String[] args) {
        server = new Server(7800);
        server.start();

        EventQueue.invokeLater(() -> {
            Main frame = new Main();
            frame.setVisible(true);
        });
    }

    /**
     * Create the frame.
     */
    public Main() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(100, 100, 450, 300);
        contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        contentPane.setLayout(new BorderLayout(0, 0));

        JButton button = new JButton("Adicionar Client");
        button.addActionListener(e -> {
            Client client = new Client(server.getServerIp(), server.getServerPort());
            client.start();
        });

        contentPane.add(button);
        setContentPane(contentPane);
    }

}
