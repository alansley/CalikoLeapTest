/***
 * 
 * File       : WebSocketPointable.java
 * Description: A WebSocketPointable can contain an instance of Pointable data from a WebSocketFrame. The WebSocketFrame has a List of these.
 * Author     : Al Lansley
 * Date       : 12/02/2014
 * Version    : 0.3
 * 
 */

package au.edu.federation.leapwebsocket;

import com.leapmotion.leap.Vector;

import au.edu.federation.utils.Vec3f;

import java.io.Serializable;

import org.lwjgl.opengl.GL11;

//import org.lwjgl.util.*;//util.vector.Vec3f;

// A class used to store all the Pointable properties provided via the Leap WebSocket interface.
public class WebSocketPointable implements Serializable
{
	private static final long serialVersionUID = 1L;
	
	private float[] direction;
	private int handId;
	private int id;
	private float length;
	private float[] stabilizedTipPosition;
	private float timeVisible;
	private float[] tipPosition;
	private float[] tipVelocity;
	private boolean tool;
	private float touchDistance;
	private String touchZone;
	
	public static final float RADS_TO_DEGS = 3.14159f / 180.0f;
	
	// Constructor
	public WebSocketPointable()
	{
		// Instantiate arrays
		direction             = new float[3];
		stabilizedTipPosition = new float[3];
		tipPosition           = new float[3];
		tipVelocity           = new float[3];
	}
	
	// Placeholder draw method - draw whatever you want, however you want =P
	public void draw()
	{
		// Get the base of the finger as the tip position minus the direction it's facing multiplied by the length
		Vector basePosition = getTipPositionVector().minus( getDirectionVector().times(length) );
		
		GL11.glLineWidth(15.0f);
		GL11.glColor4f(1.0f,  1.0f,  1.0f,  1.0f);
		GL11.glBegin(GL11.GL_LINES);
			GL11.glVertex3f( tipPosition[0], tipPosition[1], tipPosition[2] );
			GL11.glVertex3d( basePosition.getX(), basePosition.getY(), basePosition.getZ() );
		GL11.glEnd();
	}

	// ----- Getters -----
	
	public long    getHandId()        { return handId;        }
	public int     getId()            { return id;            }
	public float   getLength()        { return length;        }
	public float   getTimeVisible()   { return timeVisible;   }
	public boolean isTool()           {	return tool;        }
	public float   getTouchDistance() { return touchDistance; }
	public String  getTouchZone()     { return touchZone;     }

	public float[] getDirection()          { return direction;                                                   }
	public Vector getDirectionVector()    { return new Vector( direction[0], direction[1], direction[2] );       }
	
	public float[] getTipPosition()       { return tipPosition;                                                  }
	public Vector getTipPositionVector()  { return new Vector( tipPosition[0], tipPosition[1], tipPosition[2] ); }
	
	public float[] getTipVelocity()       { return tipVelocity;                                                  }
	public Vector getTipVelocityVector()  { return new Vector( tipVelocity[0], tipVelocity[1], tipVelocity[2] ); }

	public float[] getStabilizedTipPosition()      { return stabilizedTipPosition;                                                                     }
	public Vector getStabilizedTipPositionVector() { return new Vector( stabilizedTipPosition[0], stabilizedTipPosition[1], stabilizedTipPosition[2]); }
	
	// ----- Setters -----
	
	public void setDirection(float[] direction)                         { this.direction = direction;                         }
	public void setHandId(int handId)                                   { this.handId = handId;                               }
	public void setId(int id)                                           { this.id = id;                                       }
	public void setLength(float length)                                 { this.length = length;                               }
	public void setStabilizedTipPosition(float[] stabilizedTipPosition) { this.stabilizedTipPosition = stabilizedTipPosition; }
	public void setTimeVisible(float timeVisible)                       { this.timeVisible = timeVisible;                     }
	public void setTipPosition(float[] tipPosition)                     { this.tipPosition = tipPosition;                     }
	public void setTipVelocity(float[] tipVelocity)                     { this.tipVelocity = tipVelocity;                     }
	public void setTool(boolean tool)                                   { this.tool = tool;                                   }
	public void setTouchDistance(float touchDistance)                   { this.touchDistance = touchDistance;                 }
	public void setTouchZone(String touchZone)                          { this.touchZone = touchZone;                         }
	
	// ----- Utility methods -----
	
	
	// Method to work out and return the joint locations on the finger
	// Note: The method returns an array of 4 Vec3f's in the following order:
	// - the tip location (index 0),
	// - the distal joint location (index 1),
	// - the proximal joint location (index 2), and
	// - the metacarpal joint location (index 3).
	public Vec3f[] getJointLocations()
	{
		
		
		// Locations for the finger tip, distal joint, proximal joint and metacarpal joint
		Vec3f[] jointLocations = new Vec3f[4];
		
		// Get the finger direction as a vector (from base to tip)
		//Vector fingerDirection = new Vector(-direction[0], -direction[1], -direction[2]);
		
		// Tip
		jointLocations[0] = new Vec3f(tipPosition[0], tipPosition[1], tipPosition[2]);
		
		// Distal Joint		
		float distalX = tipPosition[0] - (direction[0] * (1.0f / 5.23f) * length); 
		float distalY = tipPosition[1] - (direction[1] * (1.0f / 5.23f) * length);
		float distalZ = tipPosition[2] - (direction[2] * (1.0f / 5.23f) * length);
		jointLocations[1] = new Vec3f(distalX, distalY, distalZ);
		
		// Proximal Joint		
		float proximalX = tipPosition[0] - (direction[0] * (1.618f / 5.23f) * length); 
		float proximalY = tipPosition[1] - (direction[1] * (1.618f / 5.23f) * length);
		float proximalZ = tipPosition[2] - (direction[2] * (1.618f / 5.23f) * length);
		jointLocations[2] = new Vec3f(proximalX, proximalY, proximalZ);
		
		// Metacarpal Joint		
		float metacarpalX = tipPosition[0] - (direction[0] * (2.617f / 5.23f) * length); 
		float metacarpalY = tipPosition[1] - (direction[1] * (2.617f / 5.23f) * length);
		float metacarpalZ = tipPosition[2] - (direction[2] * (2.617f / 5.23f) * length);
		jointLocations[3] = new Vec3f(metacarpalX, metacarpalY, metacarpalZ);
		
		
		return jointLocations;
	}
	
	public Vector toDegrees(Vector v)
	{
		return v.times(RADS_TO_DEGS);
	}
	
	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
			
		sb.append( "----- Pointable Id: " + getId()  + " -----"                              + System.lineSeparator() );
		sb.append( "Attached to hand id    : " + getHandId()                                 + System.lineSeparator() );
		sb.append( "Time visible (secs)    : " + timeVisible                                 + System.lineSeparator() );
		sb.append( "Tip position           : " + getTipPositionVector().toString()           + System.lineSeparator() );
		sb.append( "Direction              : " + getDirectionVector().toString()             + System.lineSeparator() );
		sb.append( "Tip velocity           : " + getTipVelocityVector().toString()           + System.lineSeparator() );
		sb.append( "Length                 : " + length                                      + System.lineSeparator() );
		sb.append( "Touch distance         : " + touchDistance                               + System.lineSeparator() );
		sb.append( "Touch zone             : " + touchZone                                   + System.lineSeparator() );
		sb.append( "Stabilized tip position: " + getStabilizedTipPositionVector().toString() + System.lineSeparator() );
		sb.append( "Is a tool?             : " + tool                                        + System.lineSeparator() );
			
		return sb.toString();		
	}
}