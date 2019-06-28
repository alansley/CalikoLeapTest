/***
 * 
 * File       : WebSocketHand.java
 * Description: A WebSocketInteractionBox can contain the InteractionBox data from a WebSocketFrame.
 * Author     : Al Lansley
 * Date       : 12/02/2014
 * Version    : 0.2
 * 
 */

package au.edu.federation.leapwebsocket;

import com.leapmotion.leap.Vector;

import java.io.Serializable;

// A class used to store all the InteractionBox properties provided via the Leap WebSocket interface.
public class WebSocketInteractionBox implements Serializable
{
	private static final long serialVersionUID = 1L;
	
	private float[] center = new float[3];
	private float[] size   = new float[3];
	
	// Constructor
	public WebSocketInteractionBox()
	{
		center = new float[3];
		size   = new float[3];
	}
	
	// ----- Getters -----
	
	public void setCenter(float[] center) {
		this.center = center;
	}

	public void setSize(float[] size) {
		this.size = size;
	}

	public float[] getCenter()      { return center;                                       }
	public Vector getCenterVector() { return new Vector( center[0], center[1], center[2]); }
	
	public float[] getSize()        { return size;                                         }
	public Vector getSizeVector()   { return new Vector( size[0], size[1], size[2] );      }
}
