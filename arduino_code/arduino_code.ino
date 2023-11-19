// Author: Rohan Barar (KernelGhost)
// Intended for use with the 'Output Serial Data' function in the 'Brainwave Detection Utility' software.

#define PACKET_SIZE 36            // The expected size of the byte array packets sent from the computer
#define DATA_SIZE 11              // The number of pieces of information encoded in a single packet (Signal, Attention, Meditation + 8 ASIC_EEG_POWER Values)
byte bytePacket[PACKET_SIZE];     // Stores each data packet sent from the computer
uint32_t intData[DATA_SIZE];      // Stores the decoded data from each packet
uint8_t intCtr;                   // Counter used to correctly group recieved bytes into byte arrays
bool boolOn;                      // Stores the attention state

void setup() {
  Serial.begin(57600);            // Set a baud rate
  intCtr = 0;                     // Initialise the counter
  boolOn = false;                 // Initialise the attention state
  pinMode(LED_BUILTIN, OUTPUT);   // Using this LED to indicate if focussing or not
  digitalWrite(LED_BUILTIN, LOW); // LED to be turned off initially
}

void RunLogic() {
  // Modify this subroutine to perform different actions based on the received data.
  // In this example, the Arduino turns an LED on or off based on the attention value.
  
  const byte UPPER_TRIGGER = 55;  // Turn the light on if attention is greater than or equal to this
  const byte LOWER_TRIGGER = 45;  // Turn the light off if attention is less than or equal to this
  
  if (boolOn) {
    if (intData[1] <= LOWER_TRIGGER) {
      // To turn the LED off from an on state, an attention value less than or equal to 40 is required
      digitalWrite(LED_BUILTIN, LOW);
      boolOn = false;
    }
  } else {
    // To turn the LED on from an off state, an attention value greater than or equal to 60 is required
    if (intData[1] >= UPPER_TRIGGER) {
      digitalWrite(LED_BUILTIN, HIGH);
      boolOn = true;
    }
  }

  // Enable code below to debug Arduino (echoes back decoded data via serial output)
  /*
  String strDebug = "";
  for (int intCtr = 0; intCtr < DATA_SIZE; intCtr++) {
    strDebug.concat(intData[intCtr]);
    if (intCtr < 10) {
      strDebug.concat(", ");
    }
  }
  Serial.println(strDebug);
  */
}

boolean ProcessPacket() {
  boolean boolSuccess = true; // Flag set to false if unexpected value encountered
  int intPCtr = 3;            // Begin at index 3 to skip header
  
  while (intPCtr < PACKET_SIZE - 1) {
    if (bytePacket[intPCtr] == (byte) 0x02) {
      // This is the Signal Strength (0-255)
      intData[0] = (uint32_t)bytePacket[intPCtr + 1];
      intPCtr += 2;
    } else if (bytePacket[intPCtr] == (byte) 0x83) {
      // This is the ASIC_EEG_POWER (8 3-byte unsigned integers of raw brainwave data)
      if (bytePacket[intPCtr + 1] == (byte) 0x18) {
        intPCtr += 2;
        for (int intLCtr = 0; intLCtr < DATA_SIZE-3; intLCtr++) {
          intData[3 + intLCtr] = 
            ((uint32_t)bytePacket[intPCtr] << 16) |
            ((uint32_t)bytePacket[intPCtr + 1] << 8) |
            ((uint32_t)bytePacket[intPCtr + 2]);
          intPCtr += 3;
        }
      } else {
        // The length of this section did not match the expected length of 24 (0x18)
        // Discard packet as invalid
        boolSuccess = false;
        intPCtr = PACKET_SIZE;
      }
    } else if (bytePacket[intPCtr] == (byte) 0x04) {
      // This is the Attention value (0-100)
      intData[1] = (uint32_t)bytePacket[intPCtr + 1];
      intPCtr += 2;
    } else if (bytePacket[intPCtr] == (byte) 0x05) {
      // This is the Meditation value (0-100)
      intData[2] = (uint32_t)bytePacket[intPCtr + 1];
      intPCtr += 2;
    } else {
      // We encountered an unexpected value
      // Discard packet as invalid
      boolSuccess = false;
      intPCtr = PACKET_SIZE;
    }
  }
  
  return boolSuccess;
}

boolean CheckPacket() {
  /*  This function attempts to decode the received data packet
   *  into the 11 distinct pieces of information we are expecting.
   *  Recall that this is the signal strength, attention value,
   *  meditation value, delta value, theta value, low alpha value,
   *  high alpha value, low beta value, high beta value, low gamma
   *  value and medium gamma value.
   */

   /* The format of the packet is as follows:
   *  [           HEADER           ] [                                                                  PAYLOAD                                                                 ] [CHECKSUM]
   *  [0xAA] [0xAA] [PAYLOAD LENGTH] [0x02] [SIGNAL STRENGTH] [0x83] [0x18] [8 3-BYTE UNSIGNED INTEGERS OF RAW BRAINWAVE DATA] [0x04] [ATTENTION VALUE] [0x05] [MEDITATION VALUE] [CHECKSUM]
   *  0      1      2                3      4                 5      6      7-30                                               31     32                33     34                 35
   *  Note that the 24 bytes of raw brainwave data are sent in this order:
   *    > Bytes 7-9:    Delta
   *    > Bytes 10-12:  Theta
   *    > Bytes 13-15:  Low Alpha
   *    > Bytes 16-18:  High Alpha
   *    > Bytes 19-21:  Low Beta
   *    > Bytes 22-24:  High Beta
   *    > Bytes 25-27:  Low Gamma
   *    > Bytes 28-30:  Medium Gamma
   */
  
  byte byteCheckSum = 0;        // Used to store calculated checksum for comparison
  boolean boolSuccess = true;   // Flag set to false if unexpected data encountered or checksum failure

  // Ensure the header of the data packet is present
  if ((bytePacket[0] == (byte) 0xAA) & (bytePacket[1] == (byte) 0xAA)) {
    // Calculate the checksum to ensure data has been received correctly
    for (int intChkSumCtr = 0; intChkSumCtr < PACKET_SIZE - 4; intChkSumCtr++) {
      // Sum all the bytes that comprise the payload within the data packet (bytes 3 to 34 inclusive)
      // We only care about the lowest 8 bits of the sum, so store the result as a byte
      byteCheckSum += bytePacket[intChkSumCtr + 3];
    }

    // Take the bit inverse of the lowest 8 bits of the sum
    byteCheckSum = ~byteCheckSum;

    // Compare result to checksum in data packet
    if (byteCheckSum != bytePacket[35]) {
      // Discard packet as invalid
      boolSuccess = false;
    }
  } else {
    // Discard packet as invalid
    boolSuccess = false;
  }
  
  // If initial checks were OK, proceed to decode the payload of the packet
  if (boolSuccess) {
    boolSuccess = ProcessPacket();
  }
  
  return boolSuccess;
}

void loop() {
  if (Serial.available() > 0) {
    bytePacket[intCtr] = Serial.read();
    intCtr++;

    // If the expected length of data has been received
    if (intCtr == PACKET_SIZE) {
      // If able to correctly decode the packet
      if (ProcessPacket()) {
        // Run desired code
        RunLogic();
      }
      intCtr = 0; // Reset the counter for the next packet
    }
  }
}
