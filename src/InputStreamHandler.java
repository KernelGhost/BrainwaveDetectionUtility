import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;

import com.fazecast.jSerialComm.SerialPort;

import javax.swing.*;

/*  This class is responsible for communication with the force trainer headset,
 *  conversion of the serial datastream into individual packets and further
 *  transcription of these packets into integer arrays of values.
 */

public class InputStreamHandler implements Runnable {
	public SerialPort port = null;										// Stores the serial connection
	public byte[] byteTempPacket = null;								// Stores semi-formed data packets
	public ArrayList<int[]> arrlistForceData = new ArrayList<>();	// Stores parsed data
	public byte byteMode;												// Stores which window is open
	public boolean boolRun;                 	      // Stores if the thread should be alive
	private final long startMillis = System.currentTimeMillis(); //timestamp for when it started

	ArrayList<boolean[]> manualFactors = new ArrayList<>(); //factors such as marker, etc
	
	public InputStreamHandler(byte byteMode) {
		// Store what mode we are running this thread in
		this.byteMode = byteMode;
	}
	
	public void run() {
		// Start the thread
		boolRun = true;
		StartStream();
    }
	
	public void Terminate() {
		// Stop the thread, close port
        boolRun = false;
		port.closePort();
	}
	
	// For use in CSV file export
	public ArrayList<int[]> GetAllData() {
		return arrlistForceData;
	}
	
	// For use in preparing serial connection before starting thread
	public boolean ConnectPort(SerialPort portFT, int intBaudRate) {
		boolean boolConnect = true;
		port = portFT;
		
		// Try and open the port
		if (port.openPort()) {
			// Configure baud and timeout options
			port.setBaudRate(intBaudRate);
			port.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, 0, 0);
		} else {
			boolConnect = false;
		}
		
		return boolConnect;
	}

	int lastFilteredIndex = 0;
	
	// Begin processing data stream
	public void StartStream() {
		byte[] newData;	// Stores data received from the serial connection
		int numRead;	// Stores the length of the data received
		while (boolRun) {
			// Is there new data?
			if (port.bytesAvailable() > 0) {
				// Receive the new data and store the length
				newData = new byte[port.bytesAvailable()];
				numRead = port.readBytes(newData, newData.length) - 1;
				
				// Break the continuous stream of data into discrete data packets
				ArrayList<byte[]> byteNewData = SplitStream(newData, numRead);
				
				// Process the data packets into usable information
				ArrayList<int[]> arrlistNewData = ParsePacket(byteNewData);
				
				// Add new information to a cumulative store of all information
				arrlistForceData.addAll(arrlistNewData);
				if(arrlistForceData.size() % DataFiltering.FRAME_SIZE == 0 && arrlistForceData.size() > lastFilteredIndex) {
					DataFiltering filtering = Main.data_filtering;
					filtering.averageDifferences.add(filtering.averageDifferenceCurrent());
					System.out.println(filtering.averageDifferences.get(filtering.averageDifferences.size() - 1));
					lastFilteredIndex = arrlistForceData.size();
					if(lastFilteredIndex % DataFiltering.ANALYSIS_PERIOD == 0) filtering.analyzeForSpike(DataFiltering.ANALYSIS_PERIOD);
				}
				// Update graphs, tables, etc. within other windows
				Main.window_manager.UpdateWindow(byteMode, arrlistNewData, arrlistForceData);
			}
		}
		
		port.closePort();	// Close the connection
		return;				// Terminate the thread
	}
	
	// Takes in the received data stream and breaks it into discrete packets
	public ArrayList<byte[]> SplitStream(byte[] byteData, int intLength) {
		// Stores each extracted packet from data stream
		ArrayList<byte[]> ArrlistNewPackets = new ArrayList<>();
		
		// Stores the current position within the array of received data
		int intCtr = 0;
		
		// Go though all the new received bytes that have not been split yet
		while (intCtr <= intLength) {
			// It is safe to proceed to read the next 3 bytes (potentially [0xAA] [0xAA] [LEN])
			if (intCtr + 2 <= intLength) {
				// Is this the start of a data packet?
				if ((byteData[intCtr] == (byte) 0xAA) & (byteData[intCtr + 1] == (byte) 0xAA)) {
					// Store the length of the payload
					int intPayloadLength = (int) (byteData[intCtr + 2] & 0xFF);
					// Is the payload length <= 169 bytes? (i.e. Is the packet length <= 173 bytes?)
					if (intPayloadLength <= 169) {
						// Create byte array of the correct length to store the packet
						byte[] bytePacket = new byte[intPayloadLength + 4];
						
						// Is the packet fully formed? (i.e. Does everything up to and including the checksum exist?)
						if (intCtr + intPayloadLength + 3 <= intLength) {
							// Store the entire data packet and work on the checksum
							int intSum = 0;
							for (int intPCtr = 0; intPCtr < intPayloadLength + 4; intPCtr++) {
								bytePacket[intPCtr] = byteData[intCtr + intPCtr];
								
								if ((intPCtr > 2) & (intPCtr < intPayloadLength + 3)) {
									intSum += (int) (bytePacket[intPCtr] & 0xFF);
								}
							}
							
							// Calculate the checksum
							intSum &= 0xFF; // Truncate to the trailing 8 binary digits
							intSum = (~intSum & 0xFF); // Calculate the one's compliment of the trailing 8 binary digits
							
							// Compare the checksum
							if ((int) (intSum & 0xFF) == ((int) (byteData[intCtr + intPayloadLength + 3]) & 0xFF)) {
								// Successfully grabbed packet. Store payload.
								ArrlistNewPackets.add(bytePacket);
							}
							
							// Set counter to start position of next packet (if not available yet, loop will exit).
							intCtr += intPayloadLength + 4;
						} else {
							// The packet is not fully formed. This means this is the last packet in the stream.
							// The rest of this packet can be expected in the next byte array, so we store it.
							byteTempPacket = new byte[intLength - intCtr + 1];
							for (int intTemp = 0; intTemp < (intLength - intCtr + 1); intTemp++) {
								byteTempPacket[intTemp] = byteData[intCtr + intTemp];
							}
							
							// Adjust the counter to break out of the while loop
							intCtr = intLength + 1;
						}
					} else {
						// Packet is too large and thus invalid. Discard.
						// Set counter to start position of next packet (if not available yet, loop will exit).
						intCtr += intPayloadLength + 4;
					}
				} else {
					// Was there a semi-formed packet before this?
					if (byteTempPacket == null) {
						// There was no previous packet. This must be the start of the stream.
						// We wait for the beginning of a valid packet. Increment counter.
						intCtr += 1;
					} else {
						// There was a packet before this. Add it to the start of the new array.
						
						int intTempLen = byteTempPacket.length;
				        byte[] byteTempResult = new byte[intTempLen + intLength];
				        
				        System.arraycopy(byteTempPacket, 0, byteTempResult, 0, intTempLen);
				        System.arraycopy(byteData, 0, byteTempResult, intTempLen, intLength);
						
				        byteData = new byte[intTempLen + intLength];
				        byteData = byteTempResult;
						
				        // Set it back to null since we have incorporated it now
						byteTempPacket = null;
					}
				}
			} else {
				// The packet is not fully formed. This means this is the last packet in the stream.
				// The rest of this packet can be expected in the next byte array, so we store it.
				byteTempPacket = new byte[intLength - intCtr + 1];
				for (int intTemp = 0; intTemp < byteTempPacket.length; intTemp++) {
					byteTempPacket[intTemp] = byteData[intCtr + intTemp];
				}
				
				// Adjust the counter to break out of the while loop
				intCtr += byteTempPacket.length;
			}
		}
		
		// Return all new packets
		return ArrlistNewPackets;
	}
	
	// Takes in discrete packets and decodes the encoded information
	public ArrayList<int[]> ParsePacket(ArrayList<byte[]> ArrlistNewPackets) {
		ArrayList<int[]> ArrlistForceData = new ArrayList<int[]>();
		
		for (int intCtr = 0; intCtr < ArrlistNewPackets.size(); intCtr++) {
			
			// Used to store payload section of packet
			byte[] bytePayload = Arrays.copyOfRange(ArrlistNewPackets.get(intCtr), 3, ArrlistNewPackets.get(intCtr).length - 1);
			
			// Used to store the decoded data
			int intForceData[] = new int[] {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}; //the last value is time!
			
			// Used to store if data was written or not (signal, attention, meditation & asic_eeg_power)
			boolean boolForceData[] = new boolean[] {false, false, false, false};
			
			// Used to store position in packet
			int intPCtr = 0;
			
			// Used to store the extended code level
			int intExCode;
			
			while (intPCtr < bytePayload.length) {
				// Count the number of extended code (EXCODE) bytes (0x55)
				// At the time of writing, no values exist that use an extended code level above 0 
				
				intExCode = 0;
				
				while (bytePayload[intExCode] == (byte) 0x55) {
					intExCode++;
				}
				
				// Update pointer counter
				intPCtr += intExCode;
				int code = bytePayload[intPCtr] & 0xFF;
				// Check if the code is between 0x00 and 0x7F (inclusive)
				if (code <= 0x7F) {
					// This means there is no specified length (the value has a length of 1 byte)
					if (intExCode == 0) {
						// If the extended code level is 0
						switch(bytePayload[intPCtr]) {
							case (byte) 0x01:
								// Battery Level (x/127)
								// We ignore this value
								break;
							case (byte) 0x02:
								// Signal Quality (x/255)
								intForceData[0] = (int) (bytePayload[intPCtr + 1] & 0xFF);
								boolForceData[0] = true;
								break;
							case (byte) 0x03:
								// Heart Rate (x/255)
								// We ignore this value
								break;
							case (byte) 0x04:
								// Attention (x/100)
								intForceData[1] = (int) (bytePayload[intPCtr + 1] & 0xFF);
								boolForceData[1] = true;
								break;
							case (byte) 0x05:
								// Meditation (x/100)
								intForceData[2] = (int) (bytePayload[intPCtr + 1] & 0xFF);
								boolForceData[2] = true;
								break;
							case (byte) 0x06:
								// 8-Bit RAW Wave Value (x/255)
								// We ignore this value
								break;
							case (byte) 0x07:
								// RAW MARKER Section Start (0)
								// We ignore this value
								break;
						}
						
						intPCtr += 2;
					} else {
						// If the extended code level is above 0
						// Unsupported. Stop processing packet.
						intPCtr = bytePayload.length;
					}
				} else {
					// This means that the length of the value is specified in the next byte
					if (intExCode == 0) {
						// If the extended code level is 0
						switch(bytePayload[intPCtr]) {
							case (byte) 0x80:
								// RAW Wave Value (Length = 2)
								// We ignore this value
								intPCtr += 4;
								break;
							case (byte) 0x81:
								// EEG_POWER (Length = 32)
								// We ignore this value
								intPCtr += 34;
								break;
							case (byte) 0x83:
								// ASIC_EEG_POWER (Length = 24)
								if (bytePayload[intPCtr + 1] == (byte) 0x18) {
							  		intPCtr += 2;
							  		for (int intLCtr = 1; intLCtr <= 8; intLCtr++) {
										byte[] value = Arrays.copyOfRange(bytePayload, intPCtr, intPCtr + 3);
										intForceData[intLCtr + 2] = convertLittleEndianToBigEndian(value);
								  		intPCtr += 3;
							  		}
							  		boolForceData[3] = true;
								} else {
									// Specified length is incorrect for excode/code combination specified.
									intPCtr += 26;
								}
								break;
							case (byte) 0x86:
								// RRINTERVAL (Length = 2)
								// We ignore this value
								intPCtr += 4;
								break;
							default:
								// Unsupported. Stop processing packet.
								intPCtr = bytePayload.length;
								break;
						}
					} else {
						// If the extended code level is above 0
						// Unsupported. Stop processing packet.
						intPCtr = bytePayload.length;
					}
				}
			}
			
			boolean boolSuccess = true;
			for (int intSCtr = 0; intSCtr < boolForceData.length; intSCtr++) {
				boolSuccess &= boolForceData[intSCtr];
			}
			if (boolSuccess) {
				intForceData[11] = (int) (System.currentTimeMillis() - startMillis);
				ArrlistForceData.add(intForceData);
				//prunes the first element of the arraylist if it is greater than 1000
				if(ArrlistForceData.size() > 2000) {
					ArrlistForceData.remove(0);
				}
				manualFactors.add(new boolean[]{frmDashboard.spacePressed});
			}
		}
		
		return ArrlistForceData;
	}

	public static int convertLittleEndianToBigEndian(byte[] littleEndianBytes) {
		if (littleEndianBytes.length != 3) {
			throw new IllegalArgumentException("Input should be a 3-byte long array");
		}
		int bigEndian = 0;
		bigEndian |= (littleEndianBytes[2] & 0xFF) << 16;
		bigEndian |= (littleEndianBytes[1] & 0xFF) << 8;
		bigEndian |= (littleEndianBytes[0] & 0xFF);
		return bigEndian;
	}
}