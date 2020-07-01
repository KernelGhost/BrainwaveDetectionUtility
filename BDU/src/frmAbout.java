import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.swing.JLabel;
import java.awt.Font;
import javax.swing.SwingConstants;

public class frmAbout extends JFrame {
	private static final long serialVersionUID = 7816880762813642189L;
	private JPanel contentPane;
	
	public frmAbout() {
		setIconImage(Toolkit.getDefaultToolkit().getImage(frmAbout.class.getResource("/resources/graphics/AppIcon.png")));
		setResizable(false);
		setTitle("About BDU");
		addWindowListener(new java.awt.event.WindowAdapter() {
		    @Override
		    public void windowClosing(java.awt.event.WindowEvent windowEvent) {
		    	// Open main menu
		    	Main.window_manager.OpenWindow(0);
		    }
		});
		setBounds(0, 0, 684, 342);
		setLocationRelativeTo(null);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		// LABELS
		// lblBrainwaveDetectionUtility
		JLabel lblBrainwaveDetectionUtility = new JLabel("<html><body style=\"text-align: left;  text-justify: inter-word;\">Brainwave Detection Utility (BDU) is designed to work with the \"Star Wars The Force Trainer II\" headset. Other NeuroSky products should work but are untested.<br/><br/>This program uses jSerialComm, a platform-independent serial port access for Java. You can learn more about it here: https://fazecast.github.io/jSerialComm/.<br/><br/>This program also uses XChart, a light-weight and convenient library for plotting data. You can learn more about it here: https://knowm.org/open-source/xchart/.<br/><br/>BDU is licensed to you under the Apache 2.0 License. See https://www.apache.org/licenses/LICENSE-2.0.txt for more information.</body></html>");
		lblBrainwaveDetectionUtility.setHorizontalAlignment(SwingConstants.LEFT);
		lblBrainwaveDetectionUtility.setVerticalAlignment(SwingConstants.TOP);
		lblBrainwaveDetectionUtility.setBounds(224, 103, 450, 229);
		contentPane.add(lblBrainwaveDetectionUtility);
		
		// lblHeading
		JLabel lblHeading = new JLabel("Brainwave Detection Utility");
		lblHeading.setHorizontalAlignment(SwingConstants.CENTER);
		lblHeading.setFont(new Font("Lucida Grande", Font.PLAIN, 35));
		lblHeading.setBounds(218, 6, 456, 48);
		contentPane.add(lblHeading);
		
		// lblVersion
		JLabel lblVersion = new JLabel("Version 2.0");
		lblVersion.setFont(new Font("Lucida Grande", Font.PLAIN, 20));
		lblVersion.setBounds(224, 46, 143, 25);
		contentPane.add(lblVersion);
		
		// lblAuthor
		JLabel lblAuthor = new JLabel("By KernelGhost");
		lblAuthor.setFont(new Font("Lucida Grande", Font.PLAIN, 15));
		lblAuthor.setBounds(224, 66, 143, 25);
		contentPane.add(lblAuthor);
		
		// IMAGES
		// AppIcon
		try {
			JLabel lblIcon = new JLabel("");
			lblIcon.setBounds(12, 55, 200, 200);
			BufferedImage imgIcon;
			imgIcon = ImageIO.read(getClass().getResource("/resources/graphics/AppIcon.png"));
			Image imgIconScaled = imgIcon.getScaledInstance(lblIcon.getWidth(), lblIcon.getHeight(), Image.SCALE_SMOOTH);
			ImageIcon icoIcon = new ImageIcon(imgIconScaled);
			lblIcon.setIcon(icoIcon);
			contentPane.add(lblIcon);
		} catch (IOException e1) {}
		
		// XChart
		try {
			JLabel lblXChart = new JLabel("");
			lblXChart.setToolTipText("XChart");
			lblXChart.setBounds(400, 47, 44, 44);
			BufferedImage imgIcon;
			imgIcon = ImageIO.read(getClass().getResource("/resources/graphics/XChart.png"));
			Image imgIconScaled = imgIcon.getScaledInstance(lblXChart.getWidth(), lblXChart.getHeight(), Image.SCALE_SMOOTH);
			ImageIcon icoIcon = new ImageIcon(imgIconScaled);
			lblXChart.setIcon(icoIcon);
			contentPane.add(lblXChart);
		} catch (IOException e1) {}
		
		// JSerialComm
		try {
			JLabel lblJSerialComm = new JLabel("");
			lblJSerialComm.setToolTipText("jSerialComm");
			lblJSerialComm.setBounds(340, 47, 51, 44);
			BufferedImage imgIcon;
			imgIcon = ImageIO.read(getClass().getResource("/resources/graphics/JSerialComm.png"));
			Image imgIconScaled = imgIcon.getScaledInstance(lblJSerialComm.getWidth(), lblJSerialComm.getHeight(), Image.SCALE_SMOOTH);
			ImageIcon icoIcon = new ImageIcon(imgIconScaled);
			lblJSerialComm.setIcon(icoIcon);
			contentPane.add(lblJSerialComm);
		} catch (IOException e1) {}
	}
}