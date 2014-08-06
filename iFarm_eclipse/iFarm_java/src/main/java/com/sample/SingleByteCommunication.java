package com.sample;

import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;

public class SingleByteCommunication implements SerialPortEventListener{
		public SerialPort serialPort;
	        /** The port we're normally going to use. */
		private static final String PORT_NAMES[] = { 
				"COM65", // Windows
				};
		/** Buffered input stream from the port */
		public static BufferedReader input;
		/** The output stream to the port */
		public static OutputStream output;
		/** Milliseconds to block while waiting for port open */
		public static final int TIME_OUT = 2000;
		/** Default bits per second for COM port. */
		public static final int DATA_RATE = 9600;

		public void initialize() {
			CommPortIdentifier portId = null;
			Enumeration portEnum = CommPortIdentifier.getPortIdentifiers();

			// iterate through, looking for the port
			while (portEnum.hasMoreElements()) {
				CommPortIdentifier currPortId = (CommPortIdentifier) portEnum.nextElement();
				for (String portName : PORT_NAMES) {
					if (currPortId.getName().equals(portName)) {
						portId = currPortId;
						break;
					}
				}
			}

			if (portId == null) {
				System.out.println("Could not find COM port.");
				return;
			}else{
				System.out.println("Found your Port");
			}

			try {
				// open serial port, and use class name for the appName.
				serialPort = (SerialPort) portId.open(this.getClass().getName(),TIME_OUT);

				// set port parameters
				serialPort.setSerialPortParams(DATA_RATE,
						SerialPort.DATABITS_8,
						SerialPort.STOPBITS_1,
						SerialPort.PARITY_NONE);

				// open the streams
				input = new BufferedReader(new InputStreamReader(serialPort.getInputStream()));
				output = serialPort.getOutputStream();
				char ch = 1;
				output.write(ch);

				// add event listeners
				serialPort.addEventListener(this);
				serialPort.notifyOnDataAvailable(true);
			} catch (Exception e) {
				System.err.println(e.toString());
			}
		}
		
		/**
		 * This should be called when you stop using the port.
		 * This will prevent port locking
		 */
		public synchronized void close() {
			if (serialPort != null) {
				serialPort.removeEventListener();
				serialPort.close();
			}
		}
		
		/**
		 * This Method can be called to print a single byte
		 * to the serial connection
		 */
		public synchronized void serialEvent(SerialPortEvent oEvent) {
			 if (oEvent.getEventType() == SerialPortEvent.DATA_AVAILABLE) {
			 try {
			 String inputLine=input.readLine();
			 System.out.println(inputLine);
			 } catch (Exception e) {
			 System.err.println(e.toString());
			 }
			 }
			 
			 }
			 
			 public static synchronized void writeData(String data) {
			 System.out.println("Sent: " + data);
			 try {
			 output.write(data.getBytes());
			 } catch (Exception e) {
			 System.out.println("could not write to port");
			 }
			 }
		
		/**
		 * Main Method is called when the Java Application starts
		 */
		public static void main(String[] args) throws Exception {
			SingleByteCommunication main = new SingleByteCommunication();
			main.initialize();
			System.out.println("Started");
			
		}
	}