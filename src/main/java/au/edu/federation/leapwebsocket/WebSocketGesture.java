/***
 * 
 * File       : WebSocketGesture.java
 * Description: A WebSocketGesture can an instance of Gesture data from a WebSocketFrame. The WebSocketFrame has a list of these.
 * Author     : Al Lansley
 * Date       : 12/02/2014
 * Version    : 0.3
 * 
 */

package au.edu.federation.leapwebsocket;

import java.io.Serializable;
import java.util.List;
import java.util.ArrayList;

// A class used to store all the Gesture properties provided via the Leap WebSocket interface.
// Further details: https://developer.leapmotion.com/documentation/javascript/api/Leap.Gesture.html
public class WebSocketGesture implements Serializable
{
	private static final long serialVersionUID = 1L;
	
	private long duration;              // The elapsed duration of the recognised movement up to the frame containing this Gesture object in microseconds.
	private List<Integer> handIds;      // The list of hand ids associated with this gesture, if any.
	private int id;                     // All Gesture objects belonging to the same recognised movement share the same ID value.
	private List<Integer> pointableIds; // The list of fingers and tools associated with this Gesture, if any.
	String state;                       // Recognised movements occur over time and have a beginning, a mideele and an edit. Possible values: start, update, stop.
	String type;                        // The gesture type. Possible values: circle, swipe, screenTap, keyTap
	
	public WebSocketGesture()
	{
		id = -1;
		duration = 0L;
		
		// Instantiate objects / lists
		handIds      = new ArrayList<Integer>();
		pointableIds = new ArrayList<Integer>();
		state        = "";
		type         = "";
	}
	
	// ----- Getters -----
	
	public long getDuration()              { return duration;     }	
	public List<Integer> getHandIds()      { return handIds;      }	
	public int getId()                     { return id;           }	
	public List<Integer> getPointableIds() { return pointableIds; }	
	public String getState()               { return state;        }	
	public String getType()                { return type;         }
	
	// ----- Setters -----
	
	public void setDuration(long duration)                  { this.duration = duration;         }
	public void setHandIds(List<Integer> handIds)           { this.handIds = handIds;           }
	public void setId(int id)                               { this.id = id;                     }
	public void setPointableIds(List<Integer> pointableIds) { this.pointableIds = pointableIds; }
	public void setState(String state)                      { this.state = state;               }
	public void setType(String type)                        { this.type = type;                 }

	// ----- Utility methods -----
	
	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
			
		sb.append( "----- Gesture Id: " + getId()  + " -----" + System.lineSeparator() );
		
		// List all hand ids involved in the gesture
		for (Integer i : handIds)
		{
			sb.append( "Hand Id: " + i + System.lineSeparator() );	
		}

		// List all pointable ids involved in the gesture
		for (Integer i : pointableIds)
		{
			sb.append( "Pointable Id: " + i + System.lineSeparator() );	
		}
		
		sb.append( "State : " + state + System.lineSeparator() );
		sb.append( "Type  : " + type  + System.lineSeparator() );
			
		return sb.toString();	
	}
	
}
