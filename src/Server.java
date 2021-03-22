import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

public class Server extends JFrame {

	private JPanel contentPane; 
	public static JLabel img_server;
	
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
			Socket s = serverSocket.accept();
			System.out.println("Connected!");
			ObjectInputStream objInput = new ObjectInputStream(s.getInputStream());
			ImageIcon icon;
				while(true) {
					icon       = (ImageIcon) objInput.readObject();
					img_server.setIcon(icon); 
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
