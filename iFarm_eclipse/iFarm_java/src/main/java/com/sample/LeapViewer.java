package com.sample;

//simple GUI
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.text.DefaultCaret;


//Drools
import org.drools.KnowledgeBase;
import org.drools.KnowledgeBaseFactory;

import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderFactory;
import org.drools.builder.ResourceType;

import org.drools.io.ResourceFactory;
import org.drools.runtime.StatefulKnowledgeSession;


//Leap motion
import com.leapmotion.leap.*;

//Arduino communication
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;

import com.sample.SingleByteCommunication;

//RabbitMQ communication with Unity
import java.io.IOException;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

public class LeapViewer extends JFrame implements ActionListener
{

  private JTextField frameID_TF, timeHands_TF, frameHands_TF, frameFingers_TF, 
                     frameTools_TF, frameGestures_TF,
                     handType_TF, handTips_TF, handRadius_TF,
                     handPalm_TF, handPalmIB_TF, 
                     handPalmNormal_TF, handPinch_TF, handPalmDir, handGrab_TF, handGrab1_TF;   
  
  private boolean Ex1;      
  private boolean Ex1ready = true;   
  private JCheckBox Ex1_CB, Ex1_CBoriginalPos;
  private JRadioButton Ex1Button;
  private JTextArea textArea;
  private boolean alreadyExecuted = true;
  private int counter = 1;
  private int counterSet=1;
  private boolean detected = true;
  
  private LeapListener listener;
  private Controller controller;
  
  KnowledgeBase kbase;
 
  private String[] languages = {"Beginner", "Intermediate", "Advanced" };
  private String diffLevel;
  
  protected Channel channel;
  protected Connection connection;
  protected String endPointName;
		   
  private JComboBox comboBox = new JComboBox();
  private int count = 0;
		 
  public static BufferedReader input;
  public static OutputStream output;
  
  public static synchronized void writeData(String data) {
  System.out.println("Sent: " + data);
  try {
  output.write(data.getBytes());
  } catch (Exception e) {
  System.out.println("could not write to port");
  }
  }
  
  
  public void EndPoint(String endpointName) throws IOException {
      this.endPointName = endpointName;
		
      //Create a connection factory
      ConnectionFactory factory = new ConnectionFactory();
	    
      //hostname of your rabbitmq server
      factory.setHost("localhost");
		
      //getting a connection
      connection = factory.newConnection();
	    
      //creating a channel
      channel = connection.createChannel();
	    
      //declaring a queue for this channel. If queue does not exist,
      //it will be created on the server.
      channel.queueDeclare(endpointName, false, false, false, null);
 }
	

  public void close() throws IOException{
      this.channel.close();
      this.connection.close();
  }
  
  

  
  
  
  public LeapViewer()
  {
    super("Leap Controller Viewer");
    
    try {
		EndPoint("javaToUnity");
	} catch (IOException e1) {
		// TODO Auto-generated catch block
		e1.printStackTrace();
	}
    
    final KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
    kbuilder.add( ResourceFactory.newClassPathResource( "Drools.drl",
                                                                LeapViewer.class ),
                          ResourceType.DRL );

    final KnowledgeBase base = KnowledgeBaseFactory.newKnowledgeBase();
    kbase = base;
    kbase.addKnowledgePackages( kbuilder.getKnowledgePackages() );

    buildGUI();
    

    // Create listener and controller
    listener = new LeapListener(this);
    controller = new Controller();
    controller.addListener(listener);
    
    
    
    addWindowListener( new WindowAdapter() {
      public void windowClosing(WindowEvent e)
      { controller.removeListener(listener);    // Remove the sample listener when done
        System.exit(0);
      }
    });

    pack();
    setResizable(false);
    setLocationRelativeTo(null);  // center the window 
    setVisible(true);

    try {
      Thread.sleep(5000);
    }
    catch(InterruptedException e) {}

    if (!controller.isConnected()) {
      System.out.println("Cannot connect to Leap");
      System.exit(1);
    }
    
   
    
  } // end of LeapViewer()
  
	  
	  


  private void buildGUI()
  {
	Container c = getContentPane();
	// use BoxLayout: align components vertically
	c.setLayout( new BoxLayout(c, BoxLayout.Y_AXIS) );   

    // ------------------------- Frame Info -------------------

    JPanel framePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    framePanel.setBorder( BorderFactory.createTitledBorder("Frame Info") );

    frameID_TF = new JTextField(6);
    labelText(framePanel, "ID", frameID_TF);

    timeHands_TF = new JTextField(10);
    labelText(framePanel, "Timestamp", timeHands_TF);

    frameHands_TF = new JTextField(2);
    labelText(framePanel, "No. Hands", frameHands_TF);

    frameFingers_TF = new JTextField(2);
    labelText(framePanel, "No. Fingers", frameFingers_TF);

    frameTools_TF = new JTextField(2);
    labelText(framePanel, "No. Tools", frameTools_TF);
          // a tool is longer, thinner, and straighter than a finger

    frameGestures_TF = new JTextField(2);
    labelText(framePanel, "No. Gestures", frameGestures_TF);

    c.add(framePanel);


    // ------------------------- Hand Info -------------------

    JPanel handPanel = new JPanel();
    handPanel.setLayout( new BoxLayout(handPanel, BoxLayout.Y_AXIS) );
    handPanel.setBorder( BorderFactory.createTitledBorder("Hand Info") );

    JPanel hPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    handPanel.add(hPanel);

    handType_TF = new JTextField(5);
    labelText(hPanel, "Hand Type", handType_TF);

    handTips_TF = new JTextField(10);
    labelText(hPanel, "Avg. Tip Pos", handTips_TF);

    handPalm_TF = new JTextField(10);
    labelText(hPanel, "Palm Pos", handPalm_TF);
       // center of the palm measured in millimeters from the Leap origin

    handPalmIB_TF = new JTextField(10);
    labelText(hPanel, "Box Palm Pos", handPalmIB_TF);
       // center of the palm measured in millimeters from the interaction box origin


    JPanel hand2Panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    handPanel.add(hand2Panel);

    // 3 axes for hand rotation
    handPalmNormal_TF = new JTextField(10);
    labelText(hand2Panel, "Palm normal vector", handPalmNormal_TF);

    handPalmDir = new JTextField(10);
    labelText(hand2Panel, "Palm direction", handPalmDir);

    handPinch_TF = new JTextField(3);   
    labelText(hand2Panel, "Pinch", handPinch_TF);

    handRadius_TF = new JTextField(5);
    labelText(hand2Panel, "Radius", handRadius_TF);
        /* The curvature of the sphere decreases as the fingers 
           are curled, defining a sphere with a smaller radius */

    handGrab_TF = new JTextField(3);   
    labelText(hand2Panel, "Grabbing", handGrab_TF);

    c.add(handPanel);


 // -------------------------Exercise panel-------------------

    JPanel gesturesPanel = new JPanel();
    gesturesPanel.setLayout( new BoxLayout(gesturesPanel, BoxLayout.Y_AXIS) );  
    gesturesPanel.setBorder( BorderFactory.createTitledBorder("Choose your exercise and difficulty level") );

    c.add(gesturesPanel);


    // --------- Ex1------------------

    JPanel Ex1Panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    gesturesPanel.add(Ex1Panel);

    Ex1Button = new JRadioButton("Exercise 1");
    Ex1Panel.add(Ex1Button);
    
    
  for(int i = 0; i < languages.length; i++)
		   comboBox.addItem(languages[count++]);

		 comboBox.addActionListener(new ActionListener() {
		   public void actionPerformed(ActionEvent e) {
			   
			   diffLevel =String.valueOf(((JComboBox)e.getSource()).getSelectedItem());
			   
		  textArea.append ("\nYou have selected : " +diffLevel+ "\nPlace your hand at the starting position\n ");
		  
		  if(diffLevel == "Beginner"){
			  String message = "1";
		       
		       try {
					channel.basicPublish("", endPointName, null, message.getBytes());
					System.out.println("Sent to Queue: "+message);
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
		       
			  
		  }
		  if(diffLevel == "Intermediate"){
			  String message = "2";
		       
		       try {
					channel.basicPublish("", endPointName, null, message.getBytes());
					System.out.println("Sent to Queue: "+message);
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
		       
			  
		  }
		  if(diffLevel == "Advanced"){
			  String message = "3";
		       
		       try {
					channel.basicPublish("", endPointName, null, message.getBytes());
					System.out.println("Sent to Queue: "+message);
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
		       
			  
		  }
		  
		  
		  
		   }
		 });
		  
		

	Ex1Panel.add(comboBox);
	
  
    
    Ex1_CB = new JCheckBox("Detected");
    Ex1Panel.add(Ex1_CB);
    Ex1_CBoriginalPos = new JCheckBox ("Hand at original position");
    Ex1Panel.add(Ex1_CBoriginalPos);

    handGrab1_TF = new JTextField(5);
    labelText(Ex1Panel, "Grabbing", handGrab1_TF);


    Ex1Button.setMnemonic(KeyEvent.VK_B);
    Ex1Button.setActionCommand("Exercise1");
    Ex1Button.setSelected(false);
    Ex1Button.addActionListener(this);


    // --------- Ex2 ------------------

    JPanel Ex2Panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    gesturesPanel.add(Ex2Panel);

    JRadioButton Ex2Button = new JRadioButton("Exercise 2");
    
    Ex2Panel.add(Ex2Button);
    Ex2Button.setMnemonic(KeyEvent.VK_C);
    Ex2Button.setActionCommand("Exercise2");
    Ex2Button.setSelected(false);
    Ex2Button.addActionListener(this);
    

    
    
 // --------- DAL exercise ------------------

    JPanel ExDALPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    gesturesPanel.add(ExDALPanel);

    JRadioButton ExDALButton = new JRadioButton("Daily Activity Exercise");
    
   ExDALPanel.add(ExDALButton);
    ExDALButton.setMnemonic(KeyEvent.VK_D);
    ExDALButton.setActionCommand("Exercise DAL");
    ExDALButton.setSelected(false);
    ExDALButton.addActionListener(this);
    

    ButtonGroup group = new ButtonGroup();
    group.add(Ex1Button);
    group.add(Ex2Button);
    group.add(ExDALButton);
    
    
  //-------------------------Text area panel------------------

    JPanel textPanel = new JPanel();
    textPanel.setLayout( new BoxLayout(textPanel, BoxLayout.Y_AXIS) );  
    textPanel.setBorder( BorderFactory.createTitledBorder("Text area") );

    c.add(textPanel);
    JPanel textPanelin = new JPanel(new FlowLayout(FlowLayout.LEFT));
    textPanel.add(textPanelin);

    textArea = new JTextArea(5,80);
    textPanelin.add(textArea);

    DefaultCaret caret = (DefaultCaret)textArea.getCaret();
    caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);

    JScrollPane scrollPane = new JScrollPane();
    scrollPane.setViewportView(textArea);
    textPanelin.add(scrollPane, BorderLayout.CENTER);

  }

  private void labelText(JPanel p, String label, JTextField tf)
  { p.add( new JLabel(label + ":"));
    p.add(tf);
  }

  public void actionPerformed(ActionEvent e) { 
	    if ("Exercise1".equals(e.getActionCommand())){
	    	textArea.append("\nExercise 1 is activated\nPlease select the difficulty level\n");
	    	setEx1(Ex1Button.isSelected());
	    	String message = "0";
		       
		       try {
					channel.basicPublish("", endPointName, null, message.getBytes());
					System.out.println("Sent to Queue: "+message);
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
		       
	    }
	    
	    if ("Exercise2".equals(e.getActionCommand())){
	    	textArea.append("\nExercise 2 is activated\nPlease select the difficulty level\n");
	    
	    }
	    
	    if ("Exercise DAL".equals(e.getActionCommand())){
	    	textArea.append("\n Daily Exercise Activity is activated\nPlease select the type of exercise\n");
	    	writeData("6");
	    }
  }
  // ---------------- update the fields ---------------

  public void clear()
  // clear all the textfields
  {
    frameID_TF.setText(""); 
    timeHands_TF.setText(""); frameHands_TF.setText("");
    frameFingers_TF.setText(""); frameTools_TF.setText("");
    frameGestures_TF.setText("");

    handType_TF.setText(""); handTips_TF.setText("");
    handRadius_TF.setText(""); 
    handPalm_TF.setText(""); handPalmIB_TF.setText("");
    handPalmNormal_TF.setText(""); handPinch_TF.setText("");
    handPalmDir.setText(""); handGrab_TF.setText("");

    Ex1_CB.setSelected(false);
    Ex1_CBoriginalPos.setSelected(false);
    handGrab_TF.setText("");

    
  }  // end of clear()

  
  

  public void setFrameInfo(long id, long timestamp, int hCount, int fCount,
                           int tCount, int gCount) 
  {
    frameID_TF.setText(""+id); timeHands_TF.setText(""+timestamp);
    frameHands_TF.setText(""+hCount);
    frameFingers_TF.setText(""+fCount); frameTools_TF.setText(""+tCount);
    frameGestures_TF.setText(""+gCount);
    
    if((fCount ==5 ||fCount ==10) && detected ==true){
    	writeData("4");
    	detected =false;
    	
    	
    	
    }
    if(fCount==0 && detected==false){
    	writeData("1");
    	detected=true;
    }
  }  // end of setFrameInfo();


  public void setHandInfo(String handType, Vector tipsPos, float sphRadius,
                 Vector palmPos, Vector palmPosIB,
                 Vector palmNormal, Vector palmDirection, float pinchStrength, float grabStrength)
  {
    handType_TF.setText(""+handType); handTips_TF.setText(""+tipsPos);
    handRadius_TF.setText(""+sphRadius); 
    handPalm_TF.setText(""+palmPos); handPalmIB_TF.setText(""+palmPosIB);
    handPalmNormal_TF.setText(""+palmNormal); handPalmDir.setText(""+palmDirection);
    handPinch_TF.setText(""+pinchStrength);
    handGrab_TF.setText(""+grabStrength);
    
 
		 

	 
  }
  
  public void atOriginalPos(float grabStrength, Vector palmDirection, Vector palmNormal, Vector palmIB, float pinch, int fCount){
	  
	 float zValue = palmDirection.getZ();
	 float yValue = palmNormal.getY();
	 Icon icon = new ImageIcon("C:\\Users\\techfest2012\\Desktop\\Ex1.gif");
	 
	 
	 if (grabStrength==0 && pinch ==0 && 0.8< Math.abs(zValue) && Math.abs(zValue)<1.1 && 0.8< Math.abs(yValue) && Math.abs(yValue)<1.1){
		  if (palmIB.getX()>0.4 && palmIB.getX()<0.7){
			  
			  Ex1_CBoriginalPos.setSelected(true);
			  
			  if (Ex1ready==true && Ex1==true && diffLevel != null){
				  writeData("2");
				  JOptionPane.showMessageDialog(null, "Carry out your exercise", "Exercise 1 : "+diffLevel, JOptionPane.INFORMATION_MESSAGE, icon);
				  Ex1ready=false;
				
				  
				  
			  }
		  }
		  
	  }
	 
	 
	
	    
  }
  
  
	  
	 
  public void setEx1Info(float grabStrength) {
	  
  
	handGrab1_TF.setText(""+grabStrength);
	setEx1(Ex1Button.isSelected());
	
	StatefulKnowledgeSession ksession = kbase.newStatefulKnowledgeSession();
	
	
	if(alreadyExecuted==true) {
		if(grabStrength > 0.8 && Ex1 ==true && Ex1ready ==false){
	    	Ex1_CB.setSelected(true);
	    	State a = new State ("Ex1");
	    	ksession.insert(a);
	        ksession.fireAllRules();
	        textArea.append("Ex1:started\n");
	        ksession.dispose();
	        alreadyExecuted =false;
	        
		}
	}
		
	
	if(alreadyExecuted==false && Ex1_CBoriginalPos.isSelected()==true) {
		if(grabStrength <0.1 && Ex1 ==true){
			
	    	Ex1_CB.setSelected(true);
	    	
	    	State b = new State ("Ex1 Done");
	    	ksession.insert(b);
	        ksession.fireAllRules();
	        
	        textArea.append("Ex1: " + counter + " repetition done\n");
	        
	        ksession.dispose();
	        alreadyExecuted =true;
	        counter++;
	        }
	}
	
	
	if(counter ==6){
		writeData("5");
		JOptionPane.showMessageDialog(null, "Exercise 1 : "+diffLevel+ "\nYou've completed "+counterSet+" set(s) of " +(counter-1) );
		counter=0;
		counterSet++;
	}
	}
	
		
  public void speedWatcher (Vector tipVelocity)	{
	  
	  if ( diffLevel== "Beginner" ){
		  
		  if (tipVelocity.getX() >40 &&tipVelocity.getY() >40 && tipVelocity.getZ() >40 && alreadyExecuted ==false){
			  
			  writeData("3");
			  JOptionPane.showMessageDialog(null, "Do it again!");
			  counter = counter -1;
		  }
		  
	  }
	  
	  if ( diffLevel== "Intermediate" ){
             
		  if (tipVelocity.getX() >60 &&tipVelocity.getY() >60 && tipVelocity.getZ() >60 && alreadyExecuted ==false){
			  
			  JOptionPane.showMessageDialog(null, "Do it again!");
			  counter = counter -1;
		  }
		  
	  }
	  
	  
	 
  }
	
   
  
  public void setEx1(boolean selected){
	  if (selected==true){
		  Ex1 = true;
	  }else
		  Ex1= false;
  }
  
  
  public void Ex1Done (float grabStrength, boolean Ex1){
	  if(grabStrength > 0.8 && Ex1 ==true){
	    	Ex1_CB.setSelected(true);
	    
	  }
	    	
  }
  
  

  public static void main(String[] args) throws IOException 
  { 
	 
	  
	  SingleByteCommunication obj = new SingleByteCommunication();
	  
	  obj.initialize();
	  
	  input = SingleByteCommunication.input;
	  output = SingleByteCommunication.output;
	
	  new LeapViewer();
	 
	 
  
  
  }

} // end of LeapViewer class


