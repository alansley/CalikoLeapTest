/***
 * 
 * File       : WebSocketFrame.java
 * Description: A WebSocketFrame can contain all the frame data provided by a Leap sensor over the WebSocket interface.
 * Author     : Al Lansley
 * Date       : 12/02/2014
 * Version    : 0.3
 * 
 */

package au.edu.federation.leapwebsocket;

import com.leapmotion.leap.Frame;
import com.leapmotion.leap.Gesture;
import com.leapmotion.leap.Hand;
import com.leapmotion.leap.Pointable;
import com.leapmotion.leap.Vector;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

// Class used to store Leap frame data as gathered from the web socket stream
// Further details: https://developer.leapmotion.com/documentation/javascript/api/Leap.Frame.html
public class WebSocketFrame implements Serializable
{	
	private static final long serialVersionUID = 1L;
	
	// Note: When converted to a map, the Leap websocket provides the following keys:
	// currentFrameRate, gestures, hands, id, interactionBox, pointables, r, s, t, timestamp
	
	private double currentFrameRate;
	private List<WebSocketGesture> gestures;
	private List<WebSocketHand> hands;
	private long id;
	private WebSocketInteractionBox interactionBox;
	private List<WebSocketPointable> pointables;
	
	private float[][] r;                             // 3x3 rotation matrix
	private float s;                                 // Scaling value
	private float[] t;                               // Translation coordinates
	private long timestamp;                          // The frame capture time in microseconds elapsed since the Leap started
	
	// Standard constructor for a WebSocketFrame that gets populated from the inflated JSON string
	public WebSocketFrame()
	{
		id = -1;
		
		// Instantiate objects / arrays
		gestures       = new ArrayList<WebSocketGesture>();
		hands          = new ArrayList<WebSocketHand>();
		pointables     = new ArrayList<WebSocketPointable>();		
		r              = new float[3][3];
		t              = new float[3];		
		interactionBox = new WebSocketInteractionBox();
	}
	
	// Constructor for a WebSocketFrame that gets populated from a standard Leap Frame object
	// Note: This is NOT called when replaying data from file, only when working with live Leap frame data
	public WebSocketFrame(Frame f)
	{		
		// Instantiate arrays
		gestures       = new ArrayList<WebSocketGesture>();
		hands          = new ArrayList<WebSocketHand>();
		pointables     = new ArrayList<WebSocketPointable>();		
		r              = new float[3][3];
		t              = new float[3];		
		interactionBox = new WebSocketInteractionBox();
		
		
		
		// Populate hands list
		for ( Hand h : f.hands() )
		{
			// Create a new WebSocketHand which we'll populate
			WebSocketHand wsh = new WebSocketHand();
			
			// Set all properties
			wsh.setId( h.id() );
			wsh.setDirection( h.direction().toFloatArray() );
			wsh.setPalmNormal( h.palmNormal().toFloatArray() );
			wsh.setPalmPosition( h.palmPosition().toFloatArray() );
			wsh.setPalmVelocity( h.palmVelocity().toFloatArray() );
			
			// TODO: Set r 3x3 matrix.
			// Note: I can't really do this yet as I don't know what details the r matrix is actually storing. I know it's some form of rotation,
			// though it's probably the orientation of the x/y/z axes in hand space as unit vectors.
			wsh.setR( new float[][] { { 0.0f, 0.0f, 0.0f }, {0.0f, 0.0f, 0.0f}, {0.0f, 0.0f, 0.0f} } );
			
			wsh.setSphereCenter( h.sphereCenter().toFloatArray() );
			wsh.setSphereRadius( h.sphereRadius() );
			wsh.setStabilizedPalmPosition( h.stabilizedPalmPosition().toFloatArray() );
			wsh.setTimeVisible( h.timeVisible() );
			
			// Finally, add the WebSocketHand we've just constructed to the WebSocketFrame hands list
			hands.add(wsh);			
		}
		
		// Populate pointables list
		for ( Pointable p : f.pointables() )
		{			
			// Create a new WebSocketHand which we'll populate
			WebSocketPointable wsp = new WebSocketPointable();
										
			// Set all properties
			wsp.setDirection            ( p.direction().toFloatArray()             );
			wsp.setHandId               ( p.hand().id()                            );
			wsp.setId                   ( p.id()                                   );
			wsp.setLength               ( p.length()                               );
			wsp.setStabilizedTipPosition( p.stabilizedTipPosition().toFloatArray() );
			wsp.setTimeVisible          ( p.timeVisible()                          );
			wsp.setTipPosition          ( p.tipPosition().toFloatArray()           );
			wsp.setTipVelocity          ( p.tipVelocity().toFloatArray()           );
			wsp.setTool                 ( p.isTool()                               );
			wsp.setTouchDistance        ( p.touchDistance()                        );
			wsp.setTouchZone            ( p.touchZone().toString()                 );
						
			// Finally, add the WebSocketPointable we've just constructed to the WebSocketFrame pointables list
			pointables.add(wsp);			
		}
		
		// TODO: Populate r 3x3 matrix.
		// Note: I can't really do this yet as I don't know what details the r matrix is actually storing. I know it's some form of rotation,
		// though it's probably the orientation of the x/y/z axes in hand space as unit vectors.
		r = ( new float[][] { { 0.0f, 0.0f, 0.0f }, {0.0f, 0.0f, 0.0f}, {0.0f, 0.0f, 0.0f} } );
		
		// TODO: Populate t vector
		// Note: This is something do with the frame translation. Need to find out exactly what this is
		t = new float[] { 0.0f, 0.0f, 0.0f };
		
		// Populate interaction box
		interactionBox.setCenter( f.interactionBox().center().toFloatArray() );
		
		float width  = f.interactionBox().width();
		float height = f.interactionBox().height();
		float depth  = f.interactionBox().depth();		
		interactionBox.setSize ( new float[] { width, height, depth } );
		
		
		
		// Populate gestures list
		for ( Gesture g : f.gestures() )
		{
			// Create a WebSocketGesture which we'll populate
			WebSocketGesture wsg = new WebSocketGesture();
			
			// Set all properties
			wsg.setDuration( g.duration() );
			
			// Set hand Ids
			List<Integer> gestureHandIds = new ArrayList<Integer>();
			for ( Hand h : g.hands() )
			{
				gestureHandIds.add( h.id() );
			}
			wsg.setHandIds(gestureHandIds);
			
			// Set gesture Id
			wsg.setId( g.id() );
			
			// Set pointable Ids
			List<Integer> gesturePointableIds = new ArrayList<Integer>();
			for ( Pointable p : g.pointables() )
			{
				gesturePointableIds.add( p.id() );
			}
			wsg.setPointableIds(gesturePointableIds);
			
			// Set state
			wsg.setState( g.state().toString() );
						
			// Set type
			wsg.setType( g.type().toString() );
						
			// Finally, add the WebSocketGesture we've just constructed to the WebSocketFrame
			gestures.add(wsg);
		}
		
	}
	
	// Method to return a list of all the pointables associated with a specific hand (as identified by its hand id)
	public List<WebSocketPointable> getPointablesOnHand(int handId)
	{
		List<WebSocketPointable> webSocketPointableList = new ArrayList<WebSocketPointable>();
		
		for (WebSocketPointable wsp : pointables)
		{
			if (wsp.getHandId() == handId)
			{
				webSocketPointableList.add(wsp);
			}
		}
		
		return webSocketPointableList;
	}
	
	// ----- Getters -----
	
	public long getId()                                { return id;                             }
	public double getCurrentFrameRate()                { return currentFrameRate;               }
	public WebSocketInteractionBox getInteractionBox() { return interactionBox;                 }
	
	public List<WebSocketHand> hands()                 { return hands;                          }
	public List<WebSocketPointable> pointables()       { return pointables;                     }
	public List<WebSocketGesture> gestures()           { return gestures;                       }	
		
	// TODO: r is a 3x3 matrix containing some overall rotation settings (of the frame?) - investigate more.
	public float[][] getR()                            { return r;                              }

	// TODO: s contains some details regarding the overall scaling (of the frame?) - investigate more.
	public float getS()                                { return s;                              }
	
	// TODO: t is a vector containing the overall translation settings - investigate more.
	public float[] getT()                              { return t;                              }
	public Vector getTVector()                         { return new Vector( t[0], t[1], t[2] ); }
	
	public long getTimestamp()                         { return timestamp;                      }
	
	// ----- Utility methods -----
	
	public void scalePointableLengths(float scaleFactor)
	{
		for (WebSocketPointable wsp : pointables)
		{
			wsp.setLength( wsp.getLength() * scaleFactor);
		}
	}
	
	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		
		sb.append("---------- Simple Frame ID: " + id + " ----------"                      + System.lineSeparator());
		sb.append("Current frame rate: " + currentFrameRate                                + System.lineSeparator());
		sb.append("Timestamp         : " + timestamp                                       + System.lineSeparator());
		sb.append("Interaction box center: " + interactionBox.getCenterVector().toString() + System.lineSeparator());		
		sb.append("Interaction box size  : " + interactionBox.getSizeVector().toString()   + System.lineSeparator());
		
		// List all detected gestures
		if (gestures.isEmpty())
		{
			sb.append("No gestures detected." + System.lineSeparator());
		}
		else
		{
			for (WebSocketGesture sg : gestures)
			{
				sb.append( sg.toString() );
			}
		}
		
		// List all detected hands
		if (hands.isEmpty())
		{
			sb.append("No hands detected." + System.lineSeparator());
		}
		else
		{
			for (WebSocketHand sh : hands)
			{
				sb.append( sh.toString() );
			}
		}
		
		// List all detected pointables
		if (pointables.isEmpty())
		{
			sb.append("No pointables detected." + System.lineSeparator());
		}
		else
		{
			for (WebSocketPointable sp : pointables)
			{
				sb.append( sp.toString() );
			}
		}
		
		return sb.toString();	
	}
	
	// We're going to specify a simple isValid check which simply returns true if the id is not -1 (which
	// is the initial value set on creation of a WebSocketFrame - it later gets updated to a valid value) 
	public boolean isValid()
	{
		return (id != -1);
	}
	
}