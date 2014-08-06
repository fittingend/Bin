
// LeapListener.java
// Andrew davison, July 2013, ad@fivedots.coe.psu.ac.th

/* Based on the SampleListener example in the Leap Motion SDK
   (\LeapSDK\samples\Sample.java)

   Report information on the current frame, one hand, and any gestures to the GUI
*/

package com.sample;
import java.io.IOException;
import java.lang.Math;

import com.leapmotion.leap.*;
import com.leapmotion.leap.Gesture.State;



public class LeapListener extends Listener
{
  
	private LeapViewer viewer;     // GUI for showing the Leap Controller data
  private boolean reportedBox = false;
                  // for reporting details about the interaction box


  public LeapListener(LeapViewer v)
  { super();
    viewer = v;
  }


  // state-triggered methods

  public void onInit(Controller controller)
  {  System.out.println("Initialized"); }


  public void onConnect(Controller controller)
  // listen for all gestures
  {
    System.out.println("Controller has been connected");
    controller.enableGesture(Gesture.Type.TYPE_SWIPE);
       // to and fro linear movement of a finger tip/hand in any direction

    controller.enableGesture(Gesture.Type.TYPE_CIRCLE);
       // clockwise rotation of a finger tip pointing at the screen

    controller.enableGesture(Gesture.Type.TYPE_SCREEN_TAP);
       // finger tip moves forward towards the screen, then back to the original postion;
       // the finger must pause briefly before beginning the tap

    controller.enableGesture(Gesture.Type.TYPE_KEY_TAP);
       // finger rotates down toward the palm, then back to the original postion;
       // the finger must pause briefly before beginning the tap

    Config config = controller.config();

    // key tap parameters
    config.setFloat("Gesture.KeyTap.MinDownVelocity", 30.0f);

    System.out.println("Key Tap MinDownVelocity: " +
                 config.getFloat("Gesture.KeyTap.MinDownVelocity"));
    System.out.println("Key Tap HistorySeconds: " + 
                 config.getFloat("Gesture.KeyTap.HistorySeconds"));
    System.out.println("Key Tap MinDistance: " + 
                 config.getFloat("Gesture.KeyTap.MinDistance"));
    System.out.println();

    // screen tap parameters
    config.setFloat("Gesture.ScreenTap.MinForwardVelocity", 30.0f);
    config.setFloat("Gesture.ScreenTap.MinDistance", 1.0f);

    System.out.println("Screen Tap MinDownVelocity: " +
                 config.getFloat("Gesture.ScreenTap.MinForwardVelocity"));
    System.out.println("Screen Tap HistorySeconds: " + 
                 config.getFloat("Gesture.ScreenTap.HistorySeconds"));
    System.out.println("Screen Tap MinDistance: " + 
                 config.getFloat("Gesture.ScreenTap.MinDistance"));
    System.out.println();
  }  // end of onConnect()


  public void onDisconnect(Controller controller)
  {  System.out.println("Disconnected");  }

  public void onExit(Controller controller)
  {  System.out.println("Exited");  }



  public void onFrame(Controller controller) 
  // fired when a frame is received from the Leap controller
  {
    viewer.clear();    // reset the GUI window

    // get the most recent frame
    Frame frame = controller.frame();

    // report frame info to the GUI
  
	viewer.setFrameInfo(frame.id(), frame.timestamp(), frame.hands().count(), 
		             frame.fingers().count(), frame.tools().count(),
		             frame.gestures().count());
	
	
    InteractionBox ib = frame.interactionBox();
    if (!reportedBox) {
      System.out.println("Interaction Box Info");
      System.out.println("  center: " + round1dp(ib.center()));
      System.out.println("  (x,y,z) dimensions: (" + round1dp(ib.width()) + ", " +
                      round1dp(ib.height()) + ", " + round1dp(ib.depth()) +")");
      reportedBox = true;
    }

    if (!frame.hands().isEmpty())
      examineHand( frame.hands().get(0), ib);     // only examine the first hand
    
   
    //examineGestures( frame.gestures(), controller);
  }  // end of onFrame()



  private void examineHand(Hand hand, InteractionBox ib) 
  {
    int fCount = 0;
    Vector avgPos = Vector.zero();

    // check if the hand has any fingers
    FingerList fingers = hand.fingers();
    if (!fingers.isEmpty()) {
      // Calculate the hand's average finger tip position
      fCount = fingers.count();
      for (Finger finger : fingers)
        avgPos = avgPos.plus(finger.tipPosition());
      avgPos = avgPos.divide(fingers.count());
    }

   String handType;
    if(hand.isRight()){
    	handType = "Right";
    }
    else
    	handType ="Left";
    
  
    // get the hand's normal vector and direction
    Vector normal = hand.palmNormal();
        // a unit vector pointing orthogonally downwards relative to the palm
    Vector direction = hand.direction();
        // a unit vector pointing from the palm position to the fingers

 
    // convert the palm to interaction box coordinates
    Vector palmIB = ib.normalizePoint(hand.palmPosition());

    // report hand info to the GUI
    viewer.setHandInfo(handType, round1dp(avgPos), round1dp( hand.sphereRadius()),
                        round1dp(hand.palmPosition()), round1dp(palmIB),
                        round1dp(hand.palmNormal()), round1dp(hand.direction()), round1dp(hand.pinchStrength()), round1dp(hand.grabStrength()));
    viewer.atOriginalPos(round1dp(hand.grabStrength()),round1dp(hand.direction()), hand.palmNormal(),round1dp(palmIB), round1dp(hand.pinchStrength()), fCount);
    viewer.setEx1Info(round1dp(hand.grabStrength()));
    
    for (Finger finger : hand.fingers()) {
    	viewer.speedWatcher(finger.tipVelocity());
    	
    }
    

  }  
  
  private Vector round1dp(Vector v)
  // round the x,y,z values in a vector to 1 dp
  {
    v.setX( (float)Math.round(v.getX()*10)/10 );
    v.setY( (float)Math.round(v.getY()*10)/10 );
    v.setZ( (float)Math.round(v.getZ()*10)/10 );
    return v;
  }


  private float round1dp(float f)
  // round a float to 1 dp
  {  return (float)Math.round(f*10)/10;  }


}  // end of LeapListener class
