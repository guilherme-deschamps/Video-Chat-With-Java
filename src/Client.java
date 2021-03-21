import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import com.github.sarxos.webcam.Webcam;

public class Client extends JFrame {
	
	private JPanel contentPane;
	public static JLabel img_client;
	
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
			Socket s = new Socket("127.0.0.1",7800);
			ObjectOutputStream output = new ObjectOutputStream(s.getOutputStream());
			ImageIcon icon;
			BufferedImage bImage;
			Webcam cam = Webcam.getDefault();
			cam.open();
			
			while(true) { 
				bImage = cam.getImage();
				icon   = new ImageIcon(bImage);
				output.writeObject(icon);
				output.flush();
				img_client.setIcon(icon);
			}
			
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
}
