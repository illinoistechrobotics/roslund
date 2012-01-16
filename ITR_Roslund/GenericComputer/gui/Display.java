//    Copyright (C) 2011  Illinois Institute of Technology Robotics
//	  <robotics@iit.edu>
//
//    This program is free software; you can redistribute it and/or modify
//    it under the terms of the GNU General Public License as published by
//    the Free Software Foundation; either version 2 of the License, or
//    (at your option) any later version.
//
//    This program is distributed in the hope that it will be useful,
//    but WITHOUT ANY WARRANTY; without even the implied warranty of
//    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//    GNU General Public License for more details.
//
//    You should have received a copy of the GNU General Public License along
//    with this program; if not, write to the Free Software Foundation, Inc.,
//    51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.

package gui;

import java.awt.*;
import java.awt.event.*;
import java.awt.EventQueue;
import java.io.*;
import javax.swing.*;

import general.EventEnum;
import general.RobotEvent;
import general.Joystick;
import general.Communication;
import general.Timer;
import general.RobotQueue;

import net.java.games.input.*;
import gnu.io.*;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class Display {
	
	private RobotQueue queue = null;
	private Communication comm = null;
	private Display dis = this;
	public JFrame frmItrGenericrobot;

	public Display(RobotQueue q, Communication c) {
		this.queue = q;
		this.comm = c;
		c.setDisplay(this);
		runDisplay();
	}
	
	public void runDisplay(){
		initialize();
		redirectSystemStreams();
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					frmItrGenericrobot.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
	

	public JComboBox comboBox_Controller;
	public JComboBox comboBox_Serial;
	public JComboBox comboBox_Baud;
	public JButton btnRefresh;
	public JButton btnStart;
	public JButton btnStatus;
	public JTextArea txtrA;
	public JLabel lblData_X;
	public JLabel lblData_Y;
	public JLabel lblData_X1;
	public JLabel lblData_Y1;
	public JSlider sldAxis_X;
	public JSlider sldAxis_Y;
	public JSlider sldAxis_X1;
	public JSlider sldAxis_Y1;
	public JButton[] btnBut = new JButton[12];
	public JButton[] btnD_Pad = new JButton[9];
	
	public JSlider sdlMot1;
	public JSlider sdlMot2;
	public JSlider sdlMot3;
	public JSlider sdlMot4;
	public JLabel lblData_Mot1;
	public JLabel lblData_Mot2;
	public JLabel lblData_Mot3;
	public JLabel lblData_Mot4;

	/**
	 * Initialize the contents of the frame.
	 * This is where all of the graphical components are made and added
	 */
	private void initialize() {
		frmItrGenericrobot = new JFrame();
		frmItrGenericrobot.setIconImage(Toolkit.getDefaultToolkit().getImage(Display.class.getResource("/gui/logo.png")));
		frmItrGenericrobot.setTitle("ITR Generic_Robot");
		frmItrGenericrobot.setResizable(false);
		frmItrGenericrobot.getContentPane().setBackground(new Color(240, 240, 240));
		frmItrGenericrobot.setBounds(100, 100, 650, 650);
		frmItrGenericrobot.addWindowListener(new ExitListener());
		frmItrGenericrobot.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		
		frmItrGenericrobot.getContentPane().setLayout(null);
		
		JLabel lblController = new JLabel("Controller");
		lblController.setBounds(15, 42, 60, 14);
		frmItrGenericrobot.getContentPane().add(lblController);
		
		comboBox_Controller = new JComboBox();
		comboBox_Controller.setBounds(72, 39, 100, 20);
		//populates the Joysticks found on the computer
		Controller[] con = Joystick.getJoysticks();
		int i=0;
		while(i<con.length && con[i] != null){
			comboBox_Controller.addItem(con[i]);
			i++;
		}
		frmItrGenericrobot.getContentPane().add(comboBox_Controller);
		
		JLabel lblSerial = new JLabel("Serial");
		lblSerial.setBounds(182, 42, 34, 14);
		frmItrGenericrobot.getContentPane().add(lblSerial);
		
		comboBox_Serial = new JComboBox();
		comboBox_Serial.setBounds(218, 39, 100, 20);
		CommPortIdentifier[] com = Communication.getSerialPorts();
		for(i=0; i<com.length; i++){
			comboBox_Serial.addItem(com[i].getName());
		}
		frmItrGenericrobot.getContentPane().add(comboBox_Serial);
		
		JLabel lblBaud = new JLabel("Baud");
		lblBaud.setBounds(328, 42, 34, 14);
		frmItrGenericrobot.getContentPane().add(lblBaud);
		
		comboBox_Baud = new JComboBox();
		comboBox_Baud.setBounds(364, 39, 100, 20);
		comboBox_Baud.addItem(new Integer(57600));
		comboBox_Baud.addItem(new Integer(9600));
		comboBox_Baud.addItem(new Integer(19200));
		comboBox_Baud.addItem(new Integer(38400));
		comboBox_Baud.addItem(new Integer(115200));
		frmItrGenericrobot.getContentPane().add(comboBox_Baud);
		
		btnRefresh = new JButton("F5");
		btnRefresh.setBounds(474, 38, 52, 23);
		btnRefresh.addActionListener(new refreshBtnListener());
		frmItrGenericrobot.getContentPane().add(btnRefresh);
		
		btnStart = new JButton("Start");
		btnStart.setBounds(530, 38, 66, 23);
		btnStart.addActionListener(new startBtnListener());
		frmItrGenericrobot.getContentPane().add(btnStart);
		
		btnStatus = new JButton("");
		btnStatus.setBounds(602, 38, 29, 24);
		btnStatus.setEnabled(false);
		btnStatus.setBackground(Color.RED);
		frmItrGenericrobot.getContentPane().add(btnStatus);
		
		JLabel lblMessages = new JLabel("Messages");
		lblMessages.setBounds(10, 460, 80, 14);
		frmItrGenericrobot.getContentPane().add(lblMessages);
		
		JLabel lblJoyStick = new JLabel("Joysticks");
		lblJoyStick.setBounds(34, 79, 60, 14);
		frmItrGenericrobot.getContentPane().add(lblJoyStick);
		
		JLabel lblX = new JLabel("X");
		lblX.setBounds(34, 99, 13, 14);
		frmItrGenericrobot.getContentPane().add(lblX);
		
		JLabel lblY = new JLabel("Y");
		lblY.setBounds(34, 119, 13, 14);
		frmItrGenericrobot.getContentPane().add(lblY);
		
		JLabel lblX1 = new JLabel("X1");
		lblX1.setBounds(174, 99, 20, 14);
		frmItrGenericrobot.getContentPane().add(lblX1);
		
		JLabel lblY1 = new JLabel("Y1");
		lblY1.setBounds(174, 119, 20, 14);
		frmItrGenericrobot.getContentPane().add(lblY1);
		
		lblData_X = new JLabel("127");
		lblData_X.setBounds(50, 99, 40, 14);
		frmItrGenericrobot.getContentPane().add(lblData_X);
		
		lblData_Y = new JLabel("127");
		lblData_Y.setBounds(50, 119, 40, 14);
		frmItrGenericrobot.getContentPane().add(lblData_Y);
		
		lblData_X1 = new JLabel("127");
		lblData_X1.setBounds(195, 99, 40, 14);
		frmItrGenericrobot.getContentPane().add(lblData_X1);
		
		lblData_Y1 = new JLabel("127");
		lblData_Y1.setBounds(195, 119, 47, 14);
		frmItrGenericrobot.getContentPane().add(lblData_Y1);
		
		sldAxis_X = new JSlider();
		sldAxis_X.setEnabled(false);
		sldAxis_X.setMajorTickSpacing(255);
		sldAxis_X.setPaintLabels(true);
		sldAxis_X.setMaximum(255);
		sldAxis_X.setValue(127);
		sldAxis_X.setBounds(34, 249, 134, 40);
		frmItrGenericrobot.getContentPane().add(sldAxis_X);
		
		sldAxis_Y = new JSlider();
		sldAxis_Y.setEnabled(false);
		sldAxis_Y.setOrientation(SwingConstants.VERTICAL);
		sldAxis_Y.setMajorTickSpacing(255);
		sldAxis_Y.setPaintLabels(true);
		sldAxis_Y.setMaximum(255);
		sldAxis_Y.setValue(127);
		sldAxis_Y.setBounds(91, 149, 40, 100);
		frmItrGenericrobot.getContentPane().add(sldAxis_Y);
		
		sldAxis_X1 = new JSlider();
		sldAxis_X1.setEnabled(false);
		sldAxis_X1.setMajorTickSpacing(255);
		sldAxis_X1.setPaintLabels(true);
		sldAxis_X1.setMaximum(255);
		sldAxis_X1.setValue(127);
		sldAxis_X1.setBounds(174, 249, 134, 40);
		frmItrGenericrobot.getContentPane().add(sldAxis_X1);
		
		sldAxis_Y1 = new JSlider();
		sldAxis_Y1.setEnabled(false);
		sldAxis_Y1.setOrientation(SwingConstants.VERTICAL);
		sldAxis_Y1.setMajorTickSpacing(255);
		sldAxis_Y1.setPaintLabels(true);
		sldAxis_Y1.setMaximum(255);
		sldAxis_Y1.setValue(127);
		sldAxis_Y1.setBounds(231, 149, 40, 100);
		frmItrGenericrobot.getContentPane().add(sldAxis_Y1);
		
		JLabel lblButtons = new JLabel("Buttons");
		lblButtons.setBounds(334, 79, 47, 14);
		frmItrGenericrobot.getContentPane().add(lblButtons);
		
		btnBut[0] = new JButton("1");
		btnBut[0].setEnabled(false);
		btnBut[0].setBackground(new Color(0, 0x66, 0xff));
		btnBut[0].setBounds(498, 198, 45, 20);
		frmItrGenericrobot.getContentPane().add(btnBut[0]);
		
		btnBut[1] = new JButton("2");
		btnBut[1].setEnabled(false);
		btnBut[1].setBackground(new Color(0, 0x66, 0xff));
		btnBut[1].setBounds(537, 229, 45, 20);
		frmItrGenericrobot.getContentPane().add(btnBut[1]);
		
		btnBut[2] = new JButton("3");
		btnBut[2].setEnabled(false);
		btnBut[2].setBackground(new Color(0, 0x66, 0xff));
		btnBut[2].setBounds(569, 198, 45, 20);
		frmItrGenericrobot.getContentPane().add(btnBut[2]);
		
		btnBut[3] = new JButton("4");
		btnBut[3].setEnabled(false);
		btnBut[3].setBackground(new Color(0, 0x66, 0xff));
		btnBut[3].setBounds(537, 167, 45, 20);
		frmItrGenericrobot.getContentPane().add(btnBut[3]);
		
		btnBut[4] = new JButton("5");
		btnBut[4].setEnabled(false);
		btnBut[4].setBackground(new Color(0, 0x66, 0xff));
		btnBut[4].setBounds(344, 116, 45, 20);
		frmItrGenericrobot.getContentPane().add(btnBut[4]);
		
		btnBut[5] = new JButton("6");
		btnBut[5].setEnabled(false);
		btnBut[5].setBackground(new Color(0, 0x66, 0xff));
		btnBut[5].setBounds(537, 116, 45, 20);
		frmItrGenericrobot.getContentPane().add(btnBut[5]);
		
		btnBut[6] = new JButton("7");
		btnBut[6].setEnabled(false);
		btnBut[6].setBackground(new Color(0, 0x66, 0xff));
		btnBut[6].setBounds(344, 96, 45, 20);
		frmItrGenericrobot.getContentPane().add(btnBut[6]);
		
		btnBut[7] = new JButton("8");
		btnBut[7].setEnabled(false);
		btnBut[7].setBackground(new Color(0, 0x66, 0xff));
		btnBut[7].setBounds(537, 96, 45, 20);
		frmItrGenericrobot.getContentPane().add(btnBut[7]);
		
		btnBut[8] = new JButton("9");
		btnBut[8].setEnabled(false);
		btnBut[8].setBackground(new Color(0, 0x66, 0xff));
		btnBut[8].setBounds(344, 188, 50, 20);
		frmItrGenericrobot.getContentPane().add(btnBut[8]);
		
		btnBut[9] = new JButton("10");
		btnBut[9].setEnabled(false);
		btnBut[9].setBackground(new Color(0, 0x66, 0xff));
		btnBut[9].setBounds(393, 188, 50, 20);
		frmItrGenericrobot.getContentPane().add(btnBut[9]);
		
		btnBut[10] = new JButton("11");
		btnBut[10].setEnabled(false);
		btnBut[10].setBackground(new Color(0, 0x66, 0xff));
		btnBut[10].setBounds(344, 219, 50, 20);
		frmItrGenericrobot.getContentPane().add(btnBut[10]);
		
		btnBut[11] = new JButton("12");
		btnBut[11].setEnabled(false);
		btnBut[11].setBackground(new Color(0, 0x66, 0xff));
		btnBut[11].setBounds(393, 219, 50, 20);
		frmItrGenericrobot.getContentPane().add(btnBut[11]);
		
		JLabel lblD_Pad = new JLabel("D-Pad");
		lblD_Pad.setBounds(44, 310, 46, 14);
		frmItrGenericrobot.getContentPane().add(lblD_Pad);
		
		btnD_Pad[0] = new JButton("0");
		btnD_Pad[0].setEnabled(false);
		btnD_Pad[0].setBackground(new Color(0, 0x66, 0xff));
		btnD_Pad[0].setBounds(99, 370, 45, 20);
		frmItrGenericrobot.getContentPane().add(btnD_Pad[0]);
		
		btnD_Pad[1] = new JButton("1");
		btnD_Pad[1].setEnabled(false);
		btnD_Pad[1].setBackground(new Color(0, 0x66, 0xff));
		btnD_Pad[1].setBounds(44, 370, 45, 20);
		frmItrGenericrobot.getContentPane().add(btnD_Pad[1]);
		
		btnD_Pad[2] = new JButton("2");
		btnD_Pad[2].setEnabled(false);
		btnD_Pad[2].setBackground(new Color(0, 0x66, 0xff));
		btnD_Pad[2].setBounds(69, 350, 45, 20);
		frmItrGenericrobot.getContentPane().add(btnD_Pad[2]);
		
		btnD_Pad[3] = new JButton("3");
		btnD_Pad[3].setEnabled(false);
		btnD_Pad[3].setBackground(new Color(0, 0x66, 0xff));
		btnD_Pad[3].setBounds(99, 330, 45, 20);
		frmItrGenericrobot.getContentPane().add(btnD_Pad[3]);
		
		btnD_Pad[4] = new JButton("4");
		btnD_Pad[4].setEnabled(false);
		btnD_Pad[4].setBackground(new Color(0, 0x66, 0xff));
		btnD_Pad[4].setBounds(129, 350, 45, 20);
		frmItrGenericrobot.getContentPane().add(btnD_Pad[4]);
		
		btnD_Pad[5] = new JButton("5");
		btnD_Pad[5].setEnabled(false);
		btnD_Pad[5].setBackground(new Color(0, 0x66, 0xff));
		btnD_Pad[5].setBounds(154, 370, 45, 20);
		frmItrGenericrobot.getContentPane().add(btnD_Pad[5]);
		
		btnD_Pad[6] = new JButton("6");
		btnD_Pad[6].setEnabled(false);
		btnD_Pad[6].setBackground(new Color(0, 0x66, 0xff));
		btnD_Pad[6].setBounds(129, 390, 45, 20);
		frmItrGenericrobot.getContentPane().add(btnD_Pad[6]);
		
		btnD_Pad[7] = new JButton("7");
		btnD_Pad[7].setEnabled(false);
		btnD_Pad[7].setBackground(new Color(0, 0x66, 0xff));
		btnD_Pad[7].setBounds(99, 410, 45, 20);
		frmItrGenericrobot.getContentPane().add(btnD_Pad[7]);
		
		btnD_Pad[8] = new JButton("8");
		btnD_Pad[8].setEnabled(false);
		btnD_Pad[8].setBackground(new Color(0, 0x66, 0xff));
		btnD_Pad[8].setBounds(69, 390, 45, 20);
		frmItrGenericrobot.getContentPane().add(btnD_Pad[8]);
		
		JLabel lblVaribles = new JLabel("Varibles");
		lblVaribles.setBounds(335, 310, 66, 14);
		frmItrGenericrobot.getContentPane().add(lblVaribles);
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(0, 483, 644, 139);
		frmItrGenericrobot.getContentPane().add(scrollPane);
		
		txtrA = new JTextArea();
		scrollPane.setViewportView(txtrA);
		txtrA.setColumns(50);
		txtrA.setRows(1);
		txtrA.setEditable(false);
		
		sdlMot1 = new JSlider();
		sdlMot1.setMinimum(-128);
		sdlMot1.setValue(0);
		sdlMot1.setPaintLabels(true);
		sdlMot1.setOrientation(SwingConstants.VERTICAL);
		sdlMot1.setMaximum(127);
		sdlMot1.setMajorTickSpacing(255);
		sdlMot1.setEnabled(false);
		sdlMot1.setBounds(338, 358, 40, 100);
		frmItrGenericrobot.getContentPane().add(sdlMot1);
		
		sdlMot2 = new JSlider();
		sdlMot2.setMinimum(-128);
		sdlMot2.setValue(0);
		sdlMot2.setPaintLabels(true);
		sdlMot2.setOrientation(SwingConstants.VERTICAL);
		sdlMot2.setMaximum(127);
		sdlMot2.setMajorTickSpacing(255);
		sdlMot2.setEnabled(false);
		sdlMot2.setBounds(418, 358, 40, 100);
		frmItrGenericrobot.getContentPane().add(sdlMot2);
		
		sdlMot3 = new JSlider();
		sdlMot3.setMinimum(-128);
		sdlMot3.setValue(0);
		sdlMot3.setPaintLabels(true);
		sdlMot3.setOrientation(SwingConstants.VERTICAL);
		sdlMot3.setMaximum(127);
		sdlMot3.setMajorTickSpacing(255);
		sdlMot3.setEnabled(false);
		sdlMot3.setBounds(498, 358, 40, 100);
		frmItrGenericrobot.getContentPane().add(sdlMot3);
		
		sdlMot4 = new JSlider();
		sdlMot4.setMinimum(-128);
		sdlMot4.setValue(0);
		sdlMot4.setPaintLabels(true);
		sdlMot4.setOrientation(SwingConstants.VERTICAL);
		sdlMot4.setMaximum(127);
		sdlMot4.setMajorTickSpacing(255);
		sdlMot4.setEnabled(false);
		sdlMot4.setBounds(578, 358, 40, 100);
		frmItrGenericrobot.getContentPane().add(sdlMot4);
		
		JLabel lblMot1 = new JLabel("MOT 1");
		lblMot1.setBounds(308, 335, 40, 14);
		frmItrGenericrobot.getContentPane().add(lblMot1);
		
		lblData_Mot1 = new JLabel("0");
		lblData_Mot1.setBounds(351, 335, 40, 14);
		frmItrGenericrobot.getContentPane().add(lblData_Mot1);
		
		JLabel lblMot2 = new JLabel("MOT 2");
		lblMot2.setBounds(388, 335, 40, 14);
		frmItrGenericrobot.getContentPane().add(lblMot2);
		
		lblData_Mot2 = new JLabel("0");
		lblData_Mot2.setBounds(431, 335, 40, 14);
		frmItrGenericrobot.getContentPane().add(lblData_Mot2);
		
		JLabel lblMot3 = new JLabel("MOT 3");
		lblMot3.setBounds(468, 335, 40, 14);
		frmItrGenericrobot.getContentPane().add(lblMot3);
		
		lblData_Mot3 = new JLabel("0");
		lblData_Mot3.setBounds(511, 335, 40, 14);
		frmItrGenericrobot.getContentPane().add(lblData_Mot3);
		
		JLabel lblMot4 = new JLabel("MOT 4");
		lblMot4.setBounds(544, 335, 40, 14);
		frmItrGenericrobot.getContentPane().add(lblMot4);
		
		lblData_Mot4 = new JLabel("0");
		lblData_Mot4.setBounds(591, 335, 40, 14);
		frmItrGenericrobot.getContentPane().add(lblData_Mot4);
		
	}
	
	/**
	 * Start button listener that opens the serial port, creates the timer thread, and creates the joystick thread
	 * Also changes the text of the state button to stop 
	 * When stop closes the serial port and destroys the timer and joystick threads changes into start
	 */
	private Timer timer = null;
	private Joystick joy = null;
	private class startBtnListener implements ActionListener{
	  	public void actionPerformed(ActionEvent event){
		  		
	  		if(((JButton)event.getSource()).getText().equals("Start")){
	  			((JButton)event.getSource()).setText("Stop");
				comm.OpenSerial((Integer)comboBox_Baud.getSelectedItem(), (String)comboBox_Serial.getSelectedItem());
				
				joy = new Joystick(queue, (Controller)comboBox_Controller.getSelectedItem(),dis);
				queue.put(new RobotEvent(EventEnum.ROBOT_EVENT_CMD_START,(short)0,0));
				Thread joyt = joy;
				joyt.start();
								
				timer = new Timer(queue,comm);
				timer.setTimers(false, false, false, false, false); //set which timers should run
				Thread timert = timer;
				timert.start();
				
				btnRefresh.setEnabled(false);
	  		}
	  		else if(((JButton)event.getSource()).getText().equals("Stop")){
	  			((JButton)event.getSource()).setText("Start");
	  			btnStatus.setBackground(Color.RED);
	  			timer.stopThread();
	  			joy.stopThread();
	  			queue.flush(); //clear the buffer of data since we are stopping 
	  			queue.put(new RobotEvent(EventEnum.ROBOT_EVENT_CMD_STOP,(short)0,0));
	  			btnRefresh.setEnabled(true);
	  		}	
	   	}
	}
	
	/**
	 * Refreshes the serial port list and joystick list when F5 is pressed
	 */
	private class refreshBtnListener implements ActionListener{
		public void actionPerformed(ActionEvent event){
			
			comboBox_Serial.removeAllItems();
			CommPortIdentifier[] com = Communication.getSerialPorts();
			for(int i=0; i<com.length; i++){
				comboBox_Serial.addItem(com[i].getName());
			}	
			comboBox_Controller.removeAllItems();
			Controller[] con = Joystick.getJoysticks();
			int i=0;
			while(i<con.length && con[i] != null){
				comboBox_Controller.addItem(con[i]);
				i++;
			}
			comboBox_Controller.updateUI();
			comboBox_Controller.updateUI();
		}
	}
	
	private class ExitListener extends WindowAdapter{
		public void windowClosing(WindowEvent event){
  			if(timer != null)
  				timer.stopThread();
  			if(joy != null)
  				joy.stopThread();
			queue.flush(); //clear the buffer of data since we are stopping 
  			queue.put(new RobotEvent(EventEnum.ROBOT_EVENT_CMD_STOP,(short)0,0));
  			queue.put(new RobotEvent(EventEnum.ROBOT_EVENT_CMD_SHUTDOWN,(short)0,0));
		}
	}
	
	/**
	 * changes the System.out and System.err to print to the textbox area 
	 */
	private void redirectSystemStreams() {  
		  OutputStream out = new OutputStream() {  
		    @Override  
		    public void write(int b) throws IOException {  
		      txtrA.append(String.valueOf((char) b)); 
		      txtrA.setCaretPosition(txtrA.getDocument().getLength());
		      txtrA.repaint();
		    }  
		  
		    @Override  
		    public void write(byte[] b, int off, int len) throws IOException {  
		      txtrA.append(new String(b, off, len));
		      txtrA.setCaretPosition(txtrA.getDocument().getLength());
		      txtrA.repaint();
		    }  
		  
		    @Override  
		    public void write(byte[] b) throws IOException {  
		      write(b, 0, b.length); 
		      txtrA.setCaretPosition(txtrA.getDocument().getLength());
		      txtrA.repaint();
		    }  
		  }; 
		  System.setOut(new PrintStream(out, true));  
		  System.setErr(new PrintStream(out, true)); 
	}
	
	public void changeRobotStatus(int stat){
		if(stat == 0){
			btnStatus.setBackground(Color.GREEN);
		}
		if(stat == 1){
			btnStatus.setBackground(Color.RED);
		}
	}
	
	public void updateButtonGUI(RobotEvent ev){
		if(ev.getValue() == 0)
			btnBut[ev.getIndex()].setBackground(new Color(0, 0x66, 0xff));
		else
			btnBut[ev.getIndex()].setBackground(Color.RED);
	}
	
	public void updateAxisGUI(RobotEvent ev){
		switch(ev.getIndex()){
		case 0:
			dis.sldAxis_X.setValue(ev.getValue());
			dis.lblData_X.setText(Integer.toString(ev.getValue()));
			break;
		case 1:
			dis.sldAxis_Y.setValue(ev.getValue());
			dis.lblData_Y.setText(Integer.toString(ev.getValue()));
			break;
		case 2:
			dis.sldAxis_X1.setValue(ev.getValue());
			dis.lblData_X1.setText(Integer.toString(ev.getValue()));
			break;
		case 3:
			dis.sldAxis_Y1.setValue(ev.getValue());
			dis.lblData_Y1.setText(Integer.toString(ev.getValue()));
			break;
		}
	}
	
	public void updateHatGUI(RobotEvent ev){
		for(int i=0; i<9; i++){
			if(ev.getValue() ==i)
				btnD_Pad[i].setBackground(Color.RED);
			else
				btnD_Pad[i].setBackground(new Color(0, 0x66, 0xff));
		}
	}
}


