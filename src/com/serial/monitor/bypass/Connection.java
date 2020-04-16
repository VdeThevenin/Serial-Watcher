package com.serial.monitor.bypass;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.UnsupportedCommOperationException;

public class Connection {
	private static Connection instance;
	private String serial1Name = "";
	private String serial2Name = "";

	private InputStream in1;
	private OutputStream out1;
	
	private InputStream in2;
	private OutputStream out2;
	
	private OutputStream outw;
	
	private Connection() {

	}

	public static Connection getInstance() {
		if (instance == null)
			instance = new Connection();
		return instance;
	}

	public String getSerial1Name() {
		return serial1Name;
	}

	public void setSerial1Name(String serial1Name) {
		this.serial1Name = serial1Name;
	}

	public String getSerial2Name() {
		return serial2Name;
	}

	public void setSerial2Name(String serial2Name) {
		this.serial2Name = serial2Name;
	}

	public void ConnectPorts(String port1, String port2, String watcher)
			throws NoSuchPortException, PortInUseException, UnsupportedCommOperationException, IOException {
		CommPortIdentifier port1Identifier = CommPortIdentifier.getPortIdentifier(port1);
		CommPortIdentifier port2Identifier = CommPortIdentifier.getPortIdentifier(port2);
		CommPortIdentifier watcherIdentifier = CommPortIdentifier.getPortIdentifier(watcher);
		if (port1Identifier.isCurrentlyOwned() || port2Identifier.isCurrentlyOwned()
				|| watcherIdentifier.isCurrentlyOwned()) {
			System.out.println("Error: Port is currently in use");
		} else {
			CommPort commPort1 = port1Identifier.open(this.getClass().getName(), 2000);
			CommPort commPort2 = port2Identifier.open(this.getClass().getName(), 2000);
			CommPort commWatcher = watcherIdentifier.open(this.getClass().getName(), 2000);

			SerialPort serialPort1 = (SerialPort) commPort1;
			serialPort1.setSerialPortParams(115200, SerialPort.DATABITS_8, SerialPort.STOPBITS_1,
					SerialPort.PARITY_NONE);
			in1 = serialPort1.getInputStream();
			out1 = serialPort1.getOutputStream();

			SerialPort serialPort2 = (SerialPort) commPort2;
			serialPort2.setSerialPortParams(115200, SerialPort.DATABITS_8, SerialPort.STOPBITS_1,
					SerialPort.PARITY_NONE);
			in2 = serialPort2.getInputStream();
			out2 = serialPort2.getOutputStream();

			SerialPort serialWatcher = (SerialPort) commWatcher;
			serialWatcher.setSerialPortParams(115200, SerialPort.DATABITS_8, SerialPort.STOPBITS_1,
					SerialPort.PARITY_NONE);
			outw = serialWatcher.getOutputStream();

			setSerial1Name(serialPort1.getName());
			setSerial2Name(serialPort2.getName());

			(new Thread(new SerialReader(in1, out2, outw, 1))).start();
			(new Thread(new SerialReader(in2, out1, outw, 2))).start();

		}
	}

	public void disconnectAll() throws IOException{
		in1.close();
		in2.close();
		out1.close();
		out2.close();
		outw.close();
	}
	
	public static class SerialReader implements Runnable {
		InputStream in;
		OutputStream outOtherPort;
		OutputStream outWatcher;
		int direction = -1;
		static int lastDir = -1;

		public SerialReader(InputStream in) {
			this.in = in;
		}

		public SerialReader(InputStream in, OutputStream outWatcher) {
			this.in = in;
			this.outWatcher = outWatcher;

		}

		public SerialReader(InputStream in, OutputStream outOtherPort, OutputStream outWatcher, int dir) {
			this.in = in;
			this.outWatcher = outWatcher;
			this.outOtherPort = outOtherPort;
			this.direction = dir;
		}

		private static String charToHex(char item) {
			String ret = "";
			char hi;
			char lo;

			hi = (char) (item >> 4 & 0x0f);
			lo = (char) (item & 0x0f);

			ret += nibbleToHex(hi);
			ret += nibbleToHex(lo);
			return ret;
		}

		private static char nibbleToHex(char nibble) {
			// return (char) (nibble + 0x30);
			switch (nibble) {
			case 0x0:
				return '0';
			case 0x1:
				return '1';
			case 0x2:
				return '2';
			case 0x3:
				return '3';
			case 0x4:
				return '4';
			case 0x5:
				return '5';
			case 0x6:
				return '6';
			case 0x7:
				return '7';
			case 0x8:
				return '8';
			case 0x9:
				return '9';
			case 0xa:
				return 'A';
			case 0xb:
				return 'B';
			case 0xc:
				return 'C';
			case 0xd:
				return 'D';
			case 0xe:
				return 'E';
			case 0xf:
				return 'F';
			default:
				return '*';
			}
		}

		public void run() {
			String stamp = "";
			byte[] buffer = new byte[1024];
			int len = -1;
			try {

				while ((len = this.in.read(buffer)) > -1) {
					synchronized (this.outWatcher) {
						if (len > 0) {
							if (direction != lastDir) {
								if (direction == 1) {
									stamp = "\n\r[" + Connection.getInstance().getSerial1Name().replace("//./", "")
											+ "]";
								} else {
									stamp = "\n\r[" + Connection.getInstance().getSerial2Name().replace("//./", "")
											+ "]";
								}
								for (char stp : stamp.toCharArray()) {
									this.outWatcher.write(stp);
								}

								lastDir = direction;
							}
							int c = 0;
							while (c < len) {
								String hex = charToHex((char) buffer[c]);
								this.outWatcher.write(hex.getBytes()[0]);
								this.outWatcher.write(hex.getBytes()[1]);
								this.outWatcher.write(' ');
								this.outOtherPort.write(buffer[c++]);
							}
						}
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/** */
	public static class SerialWriter implements Runnable {
		OutputStream out;

		public SerialWriter(OutputStream out) {
			this.out = out;
		}

		public void run() {
			try {
				int c = 0;
				while ((c = System.in.read()) > -1) {
					this.out.write(c);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
