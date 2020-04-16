package com.serial.monitor.bypass;

import java.awt.EventQueue;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import gnu.io.CommPortIdentifier;
import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.UnsupportedCommOperationException;

import javax.swing.JFrame;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.awt.event.ActionEvent;

public class MainWindow {

	private JFrame frame;
	private Boolean connected = false;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					MainWindow window = new MainWindow();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public MainWindow() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setBounds(100, 100, 341, 185);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);

		JComboBox<String> serial1ComboBox = new JComboBox<String>(getSerialPorts());
		//JComboBox<String> serial1ComboBox = new JComboBox<String>();
		serial1ComboBox.setBounds(12, 23, 91, 22);
		frame.getContentPane().add(serial1ComboBox);

		JComboBox<String> serial2ComboBox = new JComboBox<String>(getSerialPorts());
		//JComboBox<String> serial2ComboBox = new JComboBox<String>();
		serial2ComboBox.setBounds(218, 23, 91, 22);
		frame.getContentPane().add(serial2ComboBox);

		JComboBox<String> watcherComboBox = new JComboBox<String>(getSerialPorts());
		//JComboBox<String> watcherComboBox = new JComboBox<String>();
		watcherComboBox.setBounds(115, 60, 91, 22);
		frame.getContentPane().add(watcherComboBox);

		JLabel lbl1stport = new JLabel("1st PORT");
		lbl1stport.setBounds(12, 3, 73, 16);
		frame.getContentPane().add(lbl1stport);

		JLabel lbl2ndport = new JLabel("2nd PORT");
		lbl2ndport.setBounds(219, 3, 73, 16);
		frame.getContentPane().add(lbl2ndport);

		JLabel lblbypass = new JLabel("watcher");
		lblbypass.setBounds(115, 41, 56, 16);
		frame.getContentPane().add(lblbypass);

		JButton Connect = new JButton("Connect");
		Connect.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				String serial1Name = (String) serial1ComboBox.getSelectedItem();
				String serial2Name = (String) serial2ComboBox.getSelectedItem();
				String watcherName = (String) watcherComboBox.getSelectedItem();

				Connection conn = Connection.getInstance();

				try {
					if (!connected) {
						conn.ConnectPorts(serial1Name, serial2Name, watcherName);
						connected = true;
						Connect.setText("Disconnect");
					} else {
						conn.disconnectAll();
						connected = false;
						Connect.setText("Connect");
					}
				} catch (NoSuchPortException | PortInUseException | UnsupportedCommOperationException | IOException e) {
					e.printStackTrace();
				}
			}
		});
		Connect.setBounds(100, 101, 114, 25);
		frame.getContentPane().add(Connect);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private String[] getSerialPorts() {

		Enumeration portList = CommPortIdentifier.getPortIdentifiers();
		ArrayList<CommPortIdentifier> ports = Collections.list(portList);
		String[] ret = new String[ports.size()];
		int i = 0;
		for (CommPortIdentifier port : ports) {
			ret[i++] = port.getName();
		}
		while (portList.hasMoreElements()) {
			CommPortIdentifier portId = (CommPortIdentifier) portList.nextElement();
			if (portId.getPortType() == CommPortIdentifier.PORT_PARALLEL) {
				System.out.println(portId.getName());
			} else {
				System.out.println(portId.getName());
			}

		}
		return ret;
	}
}
