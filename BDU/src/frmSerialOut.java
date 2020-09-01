import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;
import javax.swing.text.BadLocationException;

import org.knowm.xchart.XChartPanel;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYChartBuilder;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortDataListener;
import com.fazecast.jSerialComm.SerialPortEvent;

import java.awt.Color;
import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JComboBox;
import javax.swing.JTextPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;

import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.io.IOException;
import java.util.ArrayList;
import java.awt.event.ActionEvent;
import java.awt.Toolkit;
import java.awt.Font;

@SuppressWarnings("unused")
public class frmSerialOut extends JFrame {
	private static final long serialVersionUID = -3989920599003472769L;
	
	private SerialPort ports[];				// Stores all detected serial ports
	private SerialPort OutputDevice;		// Stores the selected output device
	private final int intMaxDisplay = 30;	// Stores the length of the graph's X axis
	
	// GUI Objects
	private JPanel contentPane;
	private JComboBox<String> comboFT = new JComboBox<String>();
	private JComboBox<String> comboOut = new JComboBox<String>();
	private JComboBox<Integer> comboBaud = new JComboBox<Integer>();
	private JButton btnBegin;
	private JButton btnScanPorts;
	private JTextPane txtLog;
	private XYChart chartAttentionMeditation;
	private JPanel panelGraph;
	
	// Get list of all available ports and add this list to comboFT and comboOut
	private void ScanPorts() {
		ports = SerialPort.getCommPorts();
		
		for (SerialPort portAvail : ports) {
			comboFT.addItem(portAvail.getSystemPortName());
			comboOut.addItem(portAvail.getSystemPortName());
		}
	}
	
	// Attempts to connect to the selected serial ports
	private boolean ConnectPorts(int intForceTrainer, int intOutputDevice, int intBaudRate) {
		boolean boolForceTrainer = false;
		boolean boolOutputDevice = false;
		
		OutputDevice = ports[intOutputDevice];
		SerialPort ForceTrainer = ports[intForceTrainer];
		
		if (ForceTrainer != OutputDevice) {
			// Attempt to establish a connection with both devices
			Main.input_stream_handler = new InputStreamHandler((byte) 2);
			boolForceTrainer = Main.input_stream_handler.ConnectPort(ForceTrainer, intBaudRate);
			boolOutputDevice = OutputDevice.openPort();
			
			if (boolForceTrainer & boolOutputDevice) {
				// Prepare graph
				chartAttentionMeditation.addSeries("Attention", new double[] {0}, new double[] {0});
				chartAttentionMeditation.addSeries("Meditation", new double[] {0}, new double[] {0});
				
				// Configure output device baud and timeout options
				OutputDevice.setBaudRate(intBaudRate);
				OutputDevice.setComPortTimeouts(SerialPort.TIMEOUT_WRITE_BLOCKING, 0, 0);
				
				// Enable code to listen for and print output device input
				/*
				OutputDevice.addDataListener(new SerialPortDataListener() {
					@Override
				    public int getListeningEvents() {
						return SerialPort.LISTENING_EVENT_DATA_AVAILABLE;
					}
				    @Override
				    public void serialEvent(SerialPortEvent event) {
				       if (event.getEventType() != SerialPort.LISTENING_EVENT_DATA_AVAILABLE) {
				    	   return;
				       }
				       byte[] newData = new byte[OutputDevice.bytesAvailable()];
				       OutputDevice.readBytes(newData, newData.length);
				       System.out.print(new String(newData));
				    }
				});
				*/
				
				// Start new thread
				Thread input_stream_thread = new Thread(Main.input_stream_handler);
				input_stream_thread.start();
			} else {
				// Display error message
				if (boolForceTrainer == false & boolOutputDevice == false) {
					JOptionPane.showMessageDialog(null, "Could not connect to either " + ports[intForceTrainer].getSystemPortName() + " or " + OutputDevice.getSystemPortName() + ".", "BDU", JOptionPane.ERROR_MESSAGE, null);
				} else if (boolForceTrainer == false) {
					JOptionPane.showMessageDialog(null, "Could not connect to " + ports[intForceTrainer].getSystemPortName() + ".", "BDU", JOptionPane.ERROR_MESSAGE, null);
				} else {
					JOptionPane.showMessageDialog(null, "Could not connect to " + OutputDevice.getSystemPortName() + ".", "BDU", JOptionPane.ERROR_MESSAGE, null);
				}
				
				Main.input_stream_handler = null;
			}
		} else {
			// Display error message
			JOptionPane.showMessageDialog(null, "Selected devices cannot be the same.", "BDU", JOptionPane.ERROR_MESSAGE, null);
		}
		
		return (boolForceTrainer & boolOutputDevice);
	}
	
	public void SendData(ArrayList<int[]> NewData) {
		for (int intCtr = 0; intCtr < NewData.size(); intCtr++) {
			// Form a data packet of length 36 containing SIGNAL, ATTENTION, MEDITATION and ASIC_EEG_POWER data
			byte byteNewPacket[] = new byte[36];
			
			// Write Packet Header
			byteNewPacket[0] = (byte) 0xAA;
			byteNewPacket[1] = (byte) 0xAA;
			byteNewPacket[2] = (byte) 0x20;
			
			// Write SIGNAL
			byteNewPacket[3] = (byte) 0x02;
			byteNewPacket[4] = (byte) (NewData.get(intCtr)[0] & 0xFF);
			
			// Write ATTENTION
			byteNewPacket[31] = (byte) 0x04;
			byteNewPacket[32] = (byte) (NewData.get(intCtr)[1] & 0xFF);
			
			// Write MEDITATION
			byteNewPacket[33] = (byte) 0x05;
			byteNewPacket[34] = (byte) (NewData.get(intCtr)[2] & 0xFF);
			
			// Write ASIC_EEG_POWER
			byteNewPacket[5] = (byte) 0x83;
			byteNewPacket[6] = (byte) 0x18;
			for (int intLCtr = 0; intLCtr <= 7; intLCtr++) {
				byteNewPacket[7 + (3 * intLCtr)] = (byte) ((NewData.get(intCtr)[3 + intLCtr] >>> 16) & 0xFF);
				byteNewPacket[8 + (3 * intLCtr)] = (byte) ((NewData.get(intCtr)[3 + intLCtr] >>> 8) & 0xFF);
				byteNewPacket[9 + (3 * intLCtr)] = (byte) (NewData.get(intCtr)[3 + intLCtr] & 0xFF);
	  		}
			
			// Write Checksum
			int intSum = 0;
			for (int intCCtr = 3; intCCtr < byteNewPacket.length - 1; intCCtr++) {
				intSum += (int) (byteNewPacket[intCCtr] & 0xFF);
			}
			intSum &= 0xFF; // Truncate to the trailing 8 binary digits
			byteNewPacket[35] = (byte) (~intSum & 0xFF); // Calculate the one's compliment of the trailing 8 binary digits
			
			// Print data packet to text box
			try {
				for (int intP = 0; intP < byteNewPacket.length; intP++) {
					txtLog.getDocument().insertString(txtLog.getDocument().getLength(), String.format("%02x", byteNewPacket[intP]), null);
				}
				
				txtLog.getDocument().insertString(txtLog.getDocument().getLength(), "\n", null);
			} catch (BadLocationException e) {
				// Display error message
				JOptionPane.showMessageDialog(null, "Error writing data packets to window.", "BDU", JOptionPane.ERROR_MESSAGE, null);
				
				// Return to main menu
				Close();
			}
			
			// Send data packets to output device
			try {
				OutputDevice.getOutputStream().write(byteNewPacket);
				OutputDevice.getOutputStream().flush();
			} catch (IOException e) {
				// Display error message
				JOptionPane.showMessageDialog(null, "Error sending data packets to output device.", "BDU", JOptionPane.ERROR_MESSAGE, null);
				
				// Return to main menu
				Close();
			}
		}
	}
	
	// Called to prepare transcribed packet data for plotting on the graph
	public void GraphData(ArrayList<int[]> arrlistForceData) {
		ArrayList<int[]> arrlistGraphData = new ArrayList<int[]>();
		double[][] doubleGraphData;
		
		/*  The length of the graph's X-axis is limited by the variable 'intMaxDisplay'
		 *  so we need to trim the cumulative array list of all transcribed packets to
		 *  this size or less.
		 */
		
		if (arrlistForceData.size() > intMaxDisplay) {
			// Trim to the most recent 30 transcribed packets
			doubleGraphData = new double [2][intMaxDisplay];
			for (int intCtr = 1; intCtr <= intMaxDisplay; intCtr++) {
				arrlistGraphData.add(arrlistForceData.get(arrlistForceData.size() - (intMaxDisplay + 1) + intCtr));
				for (int intACtr = 0; intACtr < 2; intACtr++) {
					doubleGraphData[intACtr][intCtr - 1] = arrlistGraphData.get(intCtr - 1)[intACtr + 1];
				}
			}
		} else {
			// Send all transcribed packets
			doubleGraphData = new double [2][arrlistForceData.size()];
			for (int intCtr = 1; intCtr <= arrlistForceData.size(); intCtr++) {
				arrlistGraphData.add(arrlistForceData.get(intCtr - 1));
				for (int intACtr = 0; intACtr < 2; intACtr++) {
					doubleGraphData[intACtr][intCtr - 1] = arrlistGraphData.get(intCtr - 1)[intACtr + 1];
				}
			}
		}
		
		// If something new was added, update the graph
		if (arrlistForceData.size() > 0) {
			UpdateGraph(doubleGraphData);
		}
	}
	
	// Called when new data is ready to be plotted on the graph
	private void UpdateGraph(double[][] doubleGraphData) {
	    SwingUtilities.invokeLater(new Runnable() {
	        @Override
	        public void run() {
	        	chartAttentionMeditation.updateXYSeries("Attention", null, doubleGraphData[0], null);
	        	chartAttentionMeditation.updateXYSeries("Meditation", null, doubleGraphData[1], null);
				panelGraph.repaint();
	        }
	    });
	}
	
	private void GUIEnable (boolean boolEnable) {
		if (boolEnable) {
			btnScanPorts.setEnabled(false);
			comboFT.setEnabled(true);
			comboOut.setEnabled(true);
			comboBaud.setEnabled(true);
			btnBegin.setEnabled(true);
		} else {
			btnScanPorts.setEnabled(false);
			comboFT.setEnabled(false);
			comboOut.setEnabled(false);
			comboBaud.setEnabled(false);
			btnBegin.setEnabled(false);
		}
	}
	
	private void Close() {
		// Close the input stream thread if it was created
    	if (Main.input_stream_handler != null) {
    		Main.input_stream_handler.Terminate();
    	}
    	
		if (OutputDevice != null) {
    		OutputDevice.closePort();
    	}
		
		// Open main menu
    	Main.window_manager.OpenWindow(0);
	}
	
	public frmSerialOut() {
		setIconImage(Toolkit.getDefaultToolkit().getImage(frmSerialOut.class.getResource("/resources/graphics/AppIcon.png")));
		setTitle("Output Serial Data");
		setResizable(false);
	    contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setPreferredSize(new Dimension(830, 408));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		addWindowListener(new java.awt.event.WindowAdapter() {
		    @Override
		    public void windowClosing(java.awt.event.WindowEvent windowEvent) {
		    	Close();
		    }
		});
		
		// Pack and center the JFrame
		pack();
		setLocationRelativeTo(null);
		
		// LABELS
		// lblForceTrainer
		JLabel lblForceTrainer = new JLabel("Force Trainer:");
		lblForceTrainer.setBounds(6, 40, 92, 15);
		contentPane.add(lblForceTrainer);
		
		// lblOutputDevice
		JLabel lblOutputDevice = new JLabel("Output Device:");
		lblOutputDevice.setBounds(6, 67, 100, 16);
		contentPane.add(lblOutputDevice);
		
		// lblUniversalBaudRate
		JLabel lblUniversalBaudRate = new JLabel("Baud Rate:");
		lblUniversalBaudRate.setBounds(6, 95, 92, 16);
		contentPane.add(lblUniversalBaudRate);
		
		// BUTTONS
		// btnScanPorts
		btnScanPorts = new JButton("Scan Ports");
		btnScanPorts.setToolTipText("Scan system for serial ports");
		btnScanPorts.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// Add ports to comboboxes
				ScanPorts();
				
				// Enable GUI
				GUIEnable(true);
			}
		});
		btnScanPorts.setBounds(6, 6, 212, 26);
		contentPane.add(btnScanPorts);
		
		// btnBegin
		btnBegin = new JButton("Begin Serial Data Output");
		btnBegin.setToolTipText("Begin transmitting data from your headset");
		btnBegin.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (ConnectPorts(comboFT.getSelectedIndex(), comboOut.getSelectedIndex(), (int) comboBaud.getSelectedItem())) {
					GUIEnable(false);
				}
			}
		});
		btnBegin.setEnabled(false);
		btnBegin.setBounds(6, 122, 212, 34);
		contentPane.add(btnBegin);
		
		// COMBOBOXES
		// comboFT
		comboFT.setEnabled(false);
		comboFT.setBounds(101, 33, 117, 30);
		contentPane.add(comboFT);
		
		// comboOut
		comboOut.setEnabled(false);
		comboOut.setBounds(101, 62, 117, 27);
		contentPane.add(comboOut);
		
		// comboBaud
		int intBaudRates[] = new int[]{9600, 14400, 19200, 38400, 57600, 115200};
		for (int intCtr = 0; intCtr < intBaudRates.length; intCtr++) {
			comboBaud.addItem(intBaudRates[intCtr]);
		}
		comboBaud.setSelectedIndex(4);
		comboBaud.setEnabled(false);
		comboBaud.setBounds(101, 91, 117, 27);
		contentPane.add(comboBaud);
		
		// TEXTPANES
		// txtLog
		txtLog = new JTextPane();
		txtLog.setFont(new Font("Courier", Font.PLAIN, 12));
		txtLog.setEditable(false);
		txtLog.setBorder(BorderFactory.createLineBorder(Color.BLACK));
		txtLog.setBounds(0, 0, 594, 150);
		JScrollPane scroll = new JScrollPane(txtLog);
		scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		scroll.getVerticalScrollBar().addAdjustmentListener(new AdjustmentListener() {  
	        public void adjustmentValueChanged(AdjustmentEvent e) {  
	            e.getAdjustable().setValue(e.getAdjustable().getMaximum());  
	        }
	    });
		scroll.setBounds(230, 6, 594, 150);
		contentPane.add(scroll);
		
		// GRAPHS
		// chartAtnMed
		chartAttentionMeditation = new XYChartBuilder().title("Attention & Meditation Values").xAxisTitle("Time").yAxisTitle("Percentage (%)").build();
		chartAttentionMeditation.getStyler().setLegendVisible(true);
		chartAttentionMeditation.getStyler().setXAxisTicksVisible(false);
		chartAttentionMeditation.getStyler().setYAxisMax((double) 100);
		chartAttentionMeditation.getStyler().setYAxisMin((double) 0);
		chartAttentionMeditation.getStyler().setXAxisMin((double) 0);
		chartAttentionMeditation.getStyler().setXAxisMax((double) intMaxDisplay);
		panelGraph = new XChartPanel<XYChart>(chartAttentionMeditation);
		panelGraph.setBounds(6, 168, 818, 234);
		panelGraph.setBorder(BorderFactory.createLineBorder(Color.BLACK));
		contentPane.add(panelGraph);
	}
}