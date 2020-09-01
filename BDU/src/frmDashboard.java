import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

import org.knowm.xchart.XChartPanel;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYChartBuilder;

import com.fazecast.jSerialComm.SerialPort;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;

import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.awt.event.ActionEvent;
import javax.swing.JTable;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;

public class frmDashboard extends JFrame {
	private static final long serialVersionUID = 3956270995168078625L;
	
	private SerialPort ports[];					// Stores all detected serial ports
	private final int intMaxDisplay = 30;		// Stores the length of the graph's X-axis
	
	// GUI Objects
	private JPanel paneDashboard;
	private JButton btnScanPorts;
	private JButton btnConnect;
	private JButton btnExport;
	private JComboBox<String> comboPorts = new JComboBox<String>();
	private JComboBox<Integer> comboBaud = new JComboBox<Integer>();
	private JProgressBar progressSignal;
	private JProgressBar progressAttention;
	private JProgressBar progressMeditation;
	private DefaultTableModel tblModel;
	private JTable tblDataReceived;
	private JPanel panelGraph;
	private XYChart chartAttentionMeditation;
	
	// Called when new data is ready to be added to the table as new row(s)
	public void UpdateProgressBarsTable(ArrayList<int[]> NewForceData) {
		// For each new row (transcribed packet) of data
		for (int intCtr = 0; intCtr < NewForceData.size(); intCtr++) {
			// Create a string array of values using that transcribed packet
			String strTableRow[] = new String[11];
			for (int intSCtr = 0; intSCtr < 11; intSCtr++) {
				strTableRow[intSCtr] = Integer.toString(NewForceData.get(intCtr)[intSCtr]);
			}
			
			// Create a new table row using string array
			tblModel.addRow(strTableRow);
			
			// Update the progress bars
			progressAttention.setValue(NewForceData.get(intCtr)[1]);
			progressMeditation.setValue(NewForceData.get(intCtr)[2]);
			
			// Required since code 200 means 'electrodes aren't contacting a person's skin'
			if (NewForceData.get(intCtr)[0] != 200) {
				progressSignal.setValue(255 - NewForceData.get(intCtr)[0]);
			} else {
				progressSignal.setValue(0);
			}
		}
		
		// Auto-scroll table
		scrollToVisible(this.tblDataReceived, this.tblDataReceived.getRowCount() - 1, 0);
	}
	
	// Called to scroll the table to display most recently added rows
	private void scrollToVisible(final JTable table, final int rowIndex, final int vColIndex) {
	    SwingUtilities.invokeLater(new Runnable() {
	        @Override
	        public void run() {
	            table.scrollRectToVisible(table.getCellRect(rowIndex, vColIndex, false));
	        }
	    });
	}
	
	// Called to prepare transcribed packet data for plotting on the graph
	public void GraphData(ArrayList<int[]> arrlistForceData) {
		double[][] doubleGraphData;
		ArrayList<int[]> arrlistGraphData = new ArrayList<int[]>();
		
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
	
	// Get list of all available ports and add this list to comboPorts
	private void ScanPorts() {
		ports = SerialPort.getCommPorts();
		
		for (SerialPort portAvail : ports) {
			comboPorts.addItem(portAvail.getSystemPortName());
		}
	}
	
	// Attempts to connect to the selected serial port within a new thread
	private boolean ConnectPort(int intPortIndex, int intBaudRate) {
		Main.input_stream_handler = new InputStreamHandler((byte) 0);
		boolean boolConnect = Main.input_stream_handler.ConnectPort(ports[intPortIndex], intBaudRate);
		
		if(boolConnect) {
			// Prepare graph
			chartAttentionMeditation.addSeries("Attention", new double[] {0}, new double[] {0});
			chartAttentionMeditation.addSeries("Meditation", new double[] {0}, new double[] {0});
			
			// Start new thread
			Thread input_stream_thread = new Thread(Main.input_stream_handler);
			input_stream_thread.start();
		} else {
			Main.input_stream_handler = null;
			JOptionPane.showMessageDialog(null, "Could not connect to " + ports[intPortIndex].getSystemPortName() + "!", "BDU", JOptionPane.ERROR_MESSAGE, null);
		}
		
		return boolConnect;
	}
	
	// Handles the appropriate enabling and disabling of GUI elements
	private void EnableGUI (boolean boolOption) {
		if (boolOption) {
			btnScanPorts.setEnabled(false);
			comboPorts.setEnabled(false);
			comboBaud.setEnabled(false);
			btnConnect.setEnabled(false);
			btnExport.setEnabled(true);
		} else {
			btnScanPorts.setEnabled(false);
			comboPorts.setEnabled(true);
			comboBaud.setEnabled(true);
			btnConnect.setEnabled(true);
			btnExport.setEnabled(false);
		}
	}
	
	// Allow data captured within the current session to be saved to a CSV file
	private void SaveCSV() {
		boolean boolSave = false;
		String strFilePath = "";
		JFileChooser jfChooser = new JFileChooser();
		jfChooser.setDialogTitle("Save As");
		jfChooser.setDialogType(JFileChooser.SAVE_DIALOG);
		jfChooser.setSelectedFile(new File("BDU_Log.csv"));
	    int int_return_val = jfChooser.showOpenDialog(null);
	    
	    // If a save location was chosen, store that chosen path
	    if (int_return_val == JFileChooser.APPROVE_OPTION) {
	    	boolSave = true;
	    	strFilePath = jfChooser.getSelectedFile().getAbsolutePath();
	    }
	    
	    if (boolSave) {
	    	// Check if file already exists
    		if(new File(strFilePath).isFile()) {
    			// Stop if user does not want to overwrite existing file
    			if (!(JOptionPane.showConfirmDialog(null, "File of the same name already exists. Overwrite?", "BDU", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION)) {
    				return;
    			}
    		}
    		
	    	try {
	    		// Create the file writer
	    		BufferedWriter CSVWriter = new BufferedWriter(new FileWriter(strFilePath, false));
	    		
	    		// Request cumulative log of all received data
	    		ArrayList<int[]> arrlistForceData = Main.input_stream_handler.GetAllData();
	    		
	    		// Write CSV Header
	    		CSVWriter.write("Signal,Attention,Meditation,Delta,Theta,Low Alpha,High Alpha,Low Beta,High Beta,Low Gamma,Medium Gamma");
	    		CSVWriter.newLine();
	    		
	    		// Write CSV data
	    		for (int intCtr = 0; intCtr < arrlistForceData.size(); intCtr++) {
	    			String strLine = "";
	    			for (int intACtr = 0; intACtr < arrlistForceData.get(intCtr).length; intACtr++) {
	    				strLine += arrlistForceData.get(intCtr)[intACtr];
	    				if (intACtr != arrlistForceData.get(intCtr).length - 1) {
	    					strLine += ",";
	    				}
	    			}
	    			CSVWriter.write(strLine);
	    			if (intCtr != arrlistForceData.size() - 1) {
	    				CSVWriter.newLine();
	    			}
	    		}
	    		
	    		CSVWriter.close();
			} catch (IOException e) {
				// Throw error
				JOptionPane.showMessageDialog(null, "Could not save the CSV file!", "BDU", JOptionPane.ERROR_MESSAGE, null);
			}	
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
	
	// Set up JFrame, JPanel and GUI elements
	public frmDashboard() {
		setIconImage(Toolkit.getDefaultToolkit().getImage(frmDashboard.class.getResource("/resources/graphics/AppIcon.png")));
		setResizable(false);
		setTitle("Live Monitoring Dashboard");
		addWindowListener(new java.awt.event.WindowAdapter() {
		    @Override
		    public void windowClosing(java.awt.event.WindowEvent windowEvent) {
		    	Close();
		    }
		});
		paneDashboard = new JPanel();
		paneDashboard.setBorder(new EmptyBorder(5, 5, 5, 5));
		paneDashboard.setPreferredSize(new Dimension(1300, 448));
		setContentPane(paneDashboard);
		paneDashboard.setLayout(null);
		
		// Pack and center the JFrame
		pack();
		setLocationRelativeTo(null);
		
		// BUTTONS
		// btnScanPorts
		btnScanPorts = new JButton("Scan Ports");
		btnScanPorts.setToolTipText("Scan system for serial ports");
		btnScanPorts.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ScanPorts();
				EnableGUI(false);
			}
		});
		btnScanPorts.setBounds(6, 6, 117, 29);
		paneDashboard.add(btnScanPorts);
		
		// btnConnect
		btnConnect = new JButton("Connect");
		btnConnect.setToolTipText("Begin receiving data from the headset");
		btnConnect.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (ConnectPort(comboPorts.getSelectedIndex(), (int) comboBaud.getSelectedItem())) {
					EnableGUI(true);
				}
			}
		});
		btnConnect.setBounds(6, 36, 660, 29);
		btnConnect.setEnabled(false);
		paneDashboard.add(btnConnect);
		
		// btnExport
		btnExport = new JButton("Export CSV");
		btnExport.setToolTipText("Export captured data to spreadsheet");
		btnExport.setEnabled(false);
		btnExport.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				SaveCSV();
			}
		});
		btnExport.setBounds(572, 385, 94, 57);
		paneDashboard.add(btnExport);
		
		// COMBOBOXES
		// comboPorts
		comboPorts.setBounds(130, 7, 292, 27);
		comboPorts.setEnabled(false);
		paneDashboard.add(comboPorts);
		
		//comboBaud
		int intBaudRates[] = new int[]{9600, 14400, 19200, 38400, 57600, 115200};
		for (int intCtr = 0; intCtr < intBaudRates.length; intCtr++) {
			comboBaud.addItem(intBaudRates[intCtr]);
		}
		comboBaud.setSelectedIndex(4);
		comboBaud.setEnabled(false);
		comboBaud.setBounds(506, 7, 160, 27);
		paneDashboard.add(comboBaud);
		
		// PROGRESS BARS
		// progressSignal
		progressSignal = new JProgressBar();
		progressSignal.setMinimum(0);
		progressSignal.setMaximum(255);
		progressSignal.setBounds(90, 382, 470, 20);
		paneDashboard.add(progressSignal);
		
		// progressAttention
		progressAttention = new JProgressBar();
		progressAttention.setMinimum(0);
		progressAttention.setMaximum(100);
		progressAttention.setBounds(90, 402, 470, 20);
		paneDashboard.add(progressAttention);
		
		// progressMeditation
		progressMeditation = new JProgressBar();
		progressMeditation.setMinimum(0);
		progressMeditation.setMaximum(100);
		progressMeditation.setBounds(90, 422, 470, 20);
		paneDashboard.add(progressMeditation);
		
		// LABELS
		// lblBaudRate
		JLabel lblBaudRate = new JLabel("Baud Rate:");
		lblBaudRate.setBounds(434, 11, 70, 16);
		paneDashboard.add(lblBaudRate);
		
		// lblDataReceived
		JLabel lblDataReceived = new JLabel("Data Received:");
		lblDataReceived.setBounds(6, 63, 100, 16);
		paneDashboard.add(lblDataReceived);
		
		// lblSignal
		JLabel lblSignal = new JLabel("Signal:");
		lblSignal.setBounds(6, 382, 84, 16);
		paneDashboard.add(lblSignal);
		
		// lblAttention
		JLabel lblAttention = new JLabel("Attention:");
		lblAttention.setBounds(6, 402, 84, 16);
		paneDashboard.add(lblAttention);
		
		// lblMeditation
		JLabel lblMeditation = new JLabel("Meditation:");
		lblMeditation.setBounds(6, 422, 84, 16);
		paneDashboard.add(lblMeditation);
		
		// TABLES
		// tblDataReceived
		String[] columnNames = {"Sig", "Atn", "Med", "Δ", "Θ", "Lα", "Hα", "Lβ", "Hβ", "Lγ", "Mγ"};
		tblModel = new DefaultTableModel(0, columnNames.length) ;
		tblModel.setColumnIdentifiers(columnNames);
		tblDataReceived = new JTable(tblModel);	
		JTableHeader table_header = tblDataReceived.getTableHeader();
        ((DefaultTableCellRenderer)table_header.getDefaultRenderer()).setHorizontalAlignment(JLabel.CENTER); 
		JScrollPane scroll = new JScrollPane(tblDataReceived);
		scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		scroll.setBounds(6, 80, 660, 290);
		tblDataReceived.setBounds(0, 0, 390, 270);
		paneDashboard.add(scroll);
		
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
		panelGraph.setBounds(678, 6, 616, 436);
		panelGraph.setBorder(BorderFactory.createLineBorder(Color.BLACK));
		paneDashboard.add(panelGraph);
	}
}