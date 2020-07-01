import java.awt.Image;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.awt.event.ActionEvent;
import java.awt.Toolkit;
import java.awt.Font;
import javax.swing.SwingConstants;

public class frmMainMenu extends JFrame {
	private static final long serialVersionUID = 1060983793304206147L;
	private JPanel contentPane;
	
	public frmMainMenu() {
		setIconImage(Toolkit.getDefaultToolkit().getImage(frmMainMenu.class.getResource("/resources/graphics/AppIcon.png")));
		setTitle("Main Menu");
		setResizable(false);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(0, 0, 550, 300);
		setLocationRelativeTo(null);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		// LABELS
		// lblBrainwaveDetectionUtility
		JLabel lblBrainwaveDetectionUtility = new JLabel("Brainwave Detection Utility");
		lblBrainwaveDetectionUtility.setHorizontalAlignment(SwingConstants.CENTER);
		lblBrainwaveDetectionUtility.setFont(new Font("Lucida Grande", Font.PLAIN, 35));
		lblBrainwaveDetectionUtility.setBounds(6, 14, 465, 40);
		contentPane.add(lblBrainwaveDetectionUtility);
		
		// IMAGES
		// AppIcon
		try {
			JLabel lblIcon = new JLabel("");
			lblIcon.setBounds(480, 4, 50, 50);
			BufferedImage imgIcon;
			imgIcon = ImageIO.read(getClass().getResource("/resources/graphics/AppIcon.png"));
			Image imgIconScaled = imgIcon.getScaledInstance(lblIcon.getWidth(), lblIcon.getHeight(), Image.SCALE_SMOOTH);
			ImageIcon icoIcon = new ImageIcon(imgIconScaled);
			lblIcon.setIcon(icoIcon);
			contentPane.add(lblIcon);
		} catch (IOException e1) {}
		
		// DashIcon
		try {			
			JLabel lblDash = new JLabel("");
			lblDash.setBounds(20, 70, 40, 40);
			BufferedImage imgIcon;
			imgIcon = ImageIO.read(getClass().getResource("/resources/graphics/DashIcon.png"));
			Image imgIconScaled = imgIcon.getScaledInstance(lblDash.getWidth(), lblDash.getHeight(), Image.SCALE_SMOOTH);
			ImageIcon icoIcon = new ImageIcon(imgIconScaled);
			lblDash.setIcon(icoIcon);
			contentPane.add(lblDash);
		} catch (IOException e1) {}
		
		// GamesIcon
		try {
			JLabel lblGames = new JLabel("");
			lblGames.setBounds(20, 120, 40, 40);
			BufferedImage imgIcon;
			imgIcon = ImageIO.read(getClass().getResource("/resources/graphics/GamesIcon.png"));
			Image imgIconScaled = imgIcon.getScaledInstance(lblGames.getWidth(), lblGames.getHeight(), Image.SCALE_SMOOTH);
			ImageIcon icoIcon = new ImageIcon(imgIconScaled);
			lblGames.setIcon(icoIcon);
			contentPane.add(lblGames);
		} catch (IOException e1) {}
		
		// ArdIcon
		try {
			JLabel lblSerialOut = new JLabel("");
			lblSerialOut.setBounds(20, 168, 40, 40);
			BufferedImage imgIcon;
			imgIcon = ImageIO.read(getClass().getResource("/resources/graphics/ArdIcon.png"));
			Image imgIconScaled = imgIcon.getScaledInstance(lblSerialOut.getWidth(), lblSerialOut.getHeight(), Image.SCALE_SMOOTH);
			ImageIcon icoIcon = new ImageIcon(imgIconScaled);
			lblSerialOut.setIcon(icoIcon);
			contentPane.add(lblSerialOut);
		} catch (IOException e1) {}
		
		// AboutIcon
		try {
			JLabel lblAbout = new JLabel("");
			lblAbout.setBounds(20,220,40,40);
			BufferedImage imgIcon;
			imgIcon = ImageIO.read(getClass().getResource("/resources/graphics/AboutIcon.png"));
			Image imgIconScaled = imgIcon.getScaledInstance(lblAbout.getWidth(), lblAbout.getHeight(), Image.SCALE_SMOOTH);
			ImageIcon icoIcon = new ImageIcon(imgIconScaled);
			lblAbout.setIcon(icoIcon);
			contentPane.add(lblAbout);
		} catch (IOException e1) {}
		
		// BUTTONS
		// btnDashboard
		JButton btnDashboard = new JButton("Live Monitoring Dashboard");
		btnDashboard.setToolTipText("Monitor and export data sent by your headset");
		btnDashboard.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Main.window_manager.OpenWindow(1);
			}
		});
		btnDashboard.setBounds(74, 70, 456, 40);
		contentPane.add(btnDashboard);
		
		// btnVideoGames
		JButton btnVideoGames = new JButton("Play Airball");
		btnVideoGames.setToolTipText("Play airball with your headset");
		btnVideoGames.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Main.window_manager.OpenWindow(2);
			}
		});
		btnVideoGames.setBounds(74, 120, 456, 40);
		contentPane.add(btnVideoGames);
		
		// btnSerialOut
		JButton btnSerialOut = new JButton("Output Serial Data");
		btnSerialOut.setToolTipText("Interface your headset with an Arduino or similar device");
		btnSerialOut.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Main.window_manager.OpenWindow(3);
			}
		});
		btnSerialOut.setBounds(74, 170, 456, 40);
		contentPane.add(btnSerialOut);
		
		// btnAbout
		JButton btnAbout = new JButton("About");
		btnAbout.setToolTipText("About Brainwave Detection Utility");
		btnAbout.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Main.window_manager.OpenWindow(4);
			}
		});
		btnAbout.setBounds(74, 220, 456, 40);
		contentPane.add(btnAbout);
	}
}
