/***
 * 
 * File       : WebSocketHand.java
 * Description: A WebSocketHand can contain an instance of Hand data from a WebSocketFrame. The WebSocketFrame has a List of these.
 * Author     : Al Lansley
 * Date       : 12/02/2014
 * Version    : 0.3
 * 
 */

package au.edu.federation.leapwebsocket;

import com.leapmotion.leap.Vector;

import java.io.Serializable;

// A class used to store all the Hand properties provided via the Leap WebSocket interface.
// Further reading: https://developer.leapmotion.com/documentation/javascript/api/Leap.Hand.html
public class WebSocketHand implements Serializable
{
	private static final long serialVersionUID = 1L;
	
	private int id;                         // Note: API docs say this is a string, but it's always a number between 0 and 100 so I'm going to use an int.
	private float[] direction;
	private float[] palmNormal;
	private float[] palmPosition;
	private float[] palmVelocity;
	private float[][] r;                    // Note: This 3x3 rotation matrix is not mentioned in the API docs. Prolly used for pitch/roll/yaw extraction.
	private float[] sphereCenter;
	private float sphereRadius;
	private float[] stabilizedPalmPosition;
	private float timeVisible;              // Note: Time visible is in seconds
	
	// Constructor
	public WebSocketHand()
	{
		// Set initial values
		id = -1;
		sphereRadius = 0.0f;
		timeVisible  = 0.0f;
		
		// Instantiate objects / arrays
		direction              = new float[3];
		palmNormal             = new float[3];
		palmPosition           = new float[3];
		palmVelocity           = new float[3];
		r                      = new float[3][3];
		sphereCenter           = new float[3];		
		stabilizedPalmPosition = new float[3];		
	}

	// ----- Getters -----
	
	public int getId()             { return id;            }
	public float getTimeVisible()  { return timeVisible;   }
	public float getSphereRadius() { return sphereRadius;  }
	
	public float[] getPalmDirection()       { return direction;                                                     }	
	public Vector getPalmDirectionVector()  { return new Vector(direction[0], direction[1], direction[2]);          }

	public float[] getPalmNormal()          { return palmNormal;                                                    }	
	public Vector getPalmNormalVector()     { return new Vector(palmNormal[0], palmNormal[1], palmNormal[2]);       }
	
	public float[] getPalmPosition()        { return palmPosition;                                                  }	
	public Vector getPalmPositionVector()   { return new Vector(palmPosition[0], palmPosition[1], palmPosition[2]); }

	public float[] getPalmVelocity()        { return palmVelocity;                                                  }	
	public Vector getPalmVelocityVector()   { return new Vector(palmVelocity[0], palmVelocity[1], palmVelocity[2]); }
	
	public float[][] getR()                 { return r; }

	public float[] getSphereCenter()        { return sphereCenter;                                                  }	
	public Vector getSphereCenterVector()   { return new Vector(sphereCenter[0], sphereCenter[1], sphereCenter[2]); }
	
	public float[] getStabilizedPalmPosition()      { return stabilizedPalmPosition;                                                                      }
	public Vector getStabilizedPalmPositionVector() { return new Vector(stabilizedPalmPosition[0], stabilizedPalmPosition[1], stabilizedPalmPosition[2]); }
	
	// ----- Setters -----
	
	public void setId(int id)                                             {	this.id = id;	                                      }
	public void setDirection(float[] direction)                           { this.direction = direction;                           }
	public void setPalmNormal(float[] palmNormal)                         { this.palmNormal = palmNormal;                         }
	public void setPalmPosition(float[] palmPosition)                     { this.palmPosition = palmPosition;                     }
	public void setPalmVelocity(float[] palmVelocity)                     { this.palmVelocity = palmVelocity;                     }
	public void setR(float[][] r)                                         { this.r = r;	                                          }
	public void setSphereCenter(float[] sphereCenter)                     { this.sphereCenter = sphereCenter;                     }
	public void setSphereRadius(float sphereRadius)                       { this.sphereRadius = sphereRadius;                     }
	public void setStabilizedPalmPosition(float[] stabilizedPalmPosition) {	this.stabilizedPalmPosition = stabilizedPalmPosition; }
	public void setTimeVisible(float timeVisible)                         { this.timeVisible = timeVisible;                       }
	
	// ----- Utility methods -----

	public Vector getPitchVectorRads() { return new Vector( r[0][0], r[0][1], r[0][2] ); }	
	public Vector getYawVectorRads()   { return new Vector( r[1][0], r[1][1], r[1][2] ); }	
	public Vector getRollVectorRads()  { return new Vector( r[2][0], r[2][1], r[2][2] ); }
	
	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		
		sb.append( "----- Hand Id: " + getId()  + " -----"                                     + System.lineSeparator() );
		sb.append( "Time visible (secs): "      + timeVisible                                  + System.lineSeparator() );
		sb.append( "Palm position : "           + getPalmPositionVector().toString()           + System.lineSeparator() );
		sb.append( "Palm direction: "           + getPalmDirectionVector().toString()          + System.lineSeparator() );
		sb.append( "Palm normal   : "           + getPalmNormalVector().toString()             + System.lineSeparator() );
		sb.append( "Palm velocity : "           + getPalmVelocityVector().toString()           + System.lineSeparator() );
		sb.append( "MAYBE Palm pitch (rads): "  + getPitchVectorRads().toString()              + System.lineSeparator() );
		sb.append( "MAYBE Palm yaw   (rads): "  + getYawVectorRads().toString()                + System.lineSeparator() );
		sb.append( "MAYBE Palm roll  (rads): "  + getRollVectorRads().toString()               + System.lineSeparator() );
		sb.append( "Sphere center : "           + getSphereCenterVector().toString()           + System.lineSeparator() );
		sb.append( "Sphere radius : "           + getSphereRadius()                            + System.lineSeparator() );
		sb.append( "Stabilized palm position: " + getStabilizedPalmPositionVector().toString() + System.lineSeparator() );
		
		return sb.toString();	
	}
	
}