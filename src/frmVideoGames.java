import javax.swing.JFrame;

import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import com.fazecast.jSerialComm.SerialPort;

import java.awt.Toolkit;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.awt.event.ActionEvent;
import java.awt.Color;
import java.awt.Dimension;

import javax.swing.SwingConstants;
import java.awt.Font;
import javax.swing.JProgressBar;

public class frmVideoGames extends JFrame {
	private static final long serialVersionUID = 7251730932563120874L;

	private SerialPort ports[];		// Stores all detected serial ports
	
	// GUI Objects
	public AirballGame panelGame;
	private JButton btnScanPorts;
	private JButton btnBegin;
	private JComboBox<String> comboPorts = new JComboBox<String>();
	private JComboBox<Integer> comboBaud = new JComboBox<Integer>();
	private JProgressBar progressSignal = new JProgressBar();
	private JProgressBar progressAttention = new JProgressBar();
	private JPanel contentPane;
	
	// Get list of all available ports and add this list to comboPorts
	private void ScanPorts() {
		ports = SerialPort.getCommPorts();
		
		for (SerialPort portAvail : ports) {
			comboPorts.addItem(portAvail.getSystemPortName());
		}
	}
	
	// Attempts to connect to the selected serial port within a new thread
	private boolean ConnectPort(int intPortIndex, int intBaudRate) {
		Main.input_stream_handler = new InputStreamHandler((byte) 1);
		boolean boolConnect = Main.input_stream_handler.ConnectPort(ports[intPortIndex], intBaudRate);
		
		if(boolConnect) {
			// Start new thread
			Thread input_stream_thread = new Thread(Main.input_stream_handler);
			input_stream_thread.start();
		} else {
			Main.input_stream_handler = null;
			JOptionPane.showMessageDialog(null, "Could not connect to " + ports[intPortIndex].getSystemPortName() + "!", "BDU", JOptionPane.ERROR_MESSAGE, null);
		}
		
		return boolConnect;
	}
	
	private void GUIEnable(boolean boolEnable) {
		if (boolEnable) {
			btnScanPorts.setEnabled(false);
			comboPorts.setEnabled(false);
			comboBaud.setEnabled(false);
			btnBegin.setEnabled(false);
		} else {
			btnScanPorts.setEnabled(false);
			comboPorts.setEnabled(true);
			comboBaud.setEnabled(true);
			btnBegin.setEnabled(true);
		}
	}
	
	private void Close() {
		// Close the input stream thread if it was created
    	if (Main.input_stream_handler != null) {
    		Main.input_stream_handler.Terminate();
    	}
    	
    	// Open main menu
    	Main.window_manager.OpenWindow(0);
	}
	
	public void UpdateProgressBars(ArrayList<int[]> NewForceData) {
		// For each new transcribed packet of data
		for (int intCtr = 0; intCtr < NewForceData.size(); intCtr++) {
			// Update the progress bars
			progressAttention.setValue(NewForceData.get(intCtr)[1]);
			
			// Required since code 200 means 'electrodes aren't contacting a person's skin'
			if (NewForceData.get(intCtr)[0] != 200) {
				progressSignal.setValue(255 - NewForceData.get(intCtr)[0]);
			} else {
				progressSignal.setValue(0);
			}
		}
	}
	
	public frmVideoGames() {
		setResizable(false);
		setIconImage(Toolkit.getDefaultToolkit().getImage(frmVideoGames.class.getResource("/resources/graphics/AppIcon.png")));
		setTitle("Airball");
		addWindowListener(new java.awt.event.WindowAdapter() {
		    @Override
		    public void windowClosing(java.awt.event.WindowEvent windowEvent) {
		    	Close();
		    }
		});
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setPreferredSize(new Dimension(852, 492));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		// Pack and center the JFrame
		pack();
		setLocationRelativeTo(null);
		
		// LABELS
		// lblBaudRate
		JLabel lblBaudRate = new JLabel("Baud Rate:");
		lblBaudRate.setBounds(452, 10, 74, 16);
		contentPane.add(lblBaudRate);
		
		//lblAbout
		JLabel lblAbout = new JLabel("Stop the ball from hitting the floor! Relax to move left and concentrate to move right.");
		lblAbout.setFont(new Font("Lucida Grande", Font.PLAIN, 13));
		lblAbout.setHorizontalAlignment(SwingConstants.CENTER);
		lblAbout.setBounds(6, 61, 840, 16);
		contentPane.add(lblAbout);
		
		// BUTTONS
		// btnScanPorts
		btnScanPorts = new JButton("Scan Ports");
		btnScanPorts.setToolTipText("Scan system for serial ports");
		btnScanPorts.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				GUIEnable(false);
				ScanPorts();
			}
		});
		btnScanPorts.setBounds(6, 6, 114, 27);
		contentPane.add(btnScanPorts);
		
		// btnBegin
		btnBegin = new JButton("Begin Video Game");
		btnBegin.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (ConnectPort(comboPorts.getSelectedIndex(), (int) comboBaud.getSelectedItem())) {
					GUIEnable(true);
				}
			}
		});
		btnBegin.setEnabled(false);
		btnBegin.setBounds(6, 35, 840, 27);
		contentPane.add(btnBegin);
		
		// COMBOBOXES
		// comboPorts
		comboPorts.setEnabled(false);
		comboPorts.setBounds(120, 6, 320, 27);
		contentPane.add(comboPorts);
		
		// comboBaud
		int intBaudRates[] = new int[]{9600, 14400, 19200, 38400, 57600, 115200};
		for (int intCtr = 0; intCtr < intBaudRates.length; intCtr++) {
			comboBaud.addItem(intBaudRates[intCtr]);
		}
		comboBaud.setSelectedIndex(4);
		comboBaud.setEnabled(false);
		comboBaud.setBounds(526, 6, 320, 27);
		contentPane.add(comboBaud);
		
		// PANELS
		// panelGame
		panelGame = new AirballGame(840, 360);
		panelGame.setBorder(BorderFactory.createLineBorder(Color.black));
		panelGame.setBounds(6, 79, 840, 360);
		contentPane.add(panelGame);
		panelGame.setLayout(null);
		
		// PROGRESS BARS
		// progressSignal
		progressSignal.setStringPainted(true);
		progressSignal.setMinimum(0);
		progressSignal.setMaximum(255);
		progressSignal.setBounds(6, 447, 840, 20);
		progressSignal.setString("Signal");
		contentPane.add(progressSignal);
		
		// progressAttention
		progressAttention.setStringPainted(true);
		progressAttention.setMinimum(0);
		progressAttention.setMaximum(100);
		progressAttention.setBounds(6, 467, 840, 20);
		progressAttention.setString("Attention");
		contentPane.add(progressAttention);
	}
}