/***
 * 
 * File       : LeapWebSocket.java
 * Description: A LeapWebSocket uses the org.java WebSocket library to connect to the Leap driver's WebSocket to obtain data in JSON format.
 *              This data is then parsed into a WebSocketFrame object, and may optionally be written to file using a LeapDataRecorder.
 * Author     : Al Lansley
 * Date       : 14/08/2014
 * Version    : 0.5
 * 
 */

package au.edu.federation.leapwebsocket;

import au.edu.federation.leapdatarecorder.LeapDataRecorder;
import au.edu.federation.leapdatarecorder.LeapDataRecorder.Mode;

import com.google.gson.Gson;

import java.net.URI;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft;
import org.java_websocket.framing.Framedata;
import org.java_websocket.handshake.ServerHandshake;

// Class to connect to the Leap daemon via a WebSocket and retrieve frame data. The JSON string from the WebSocket is
// used to inflate a WebSocketFrame object which may be written to file via the LeapDataRecorder class. There is also
// a getter for the currentWebSocketFrame object so it can be used in other parts of your program, if desired.
public class LeapWebSocket extends WebSocketClient
{
	// Our LeapWebSocket has a single static instance of Gson object used to parse the json string
	private static Gson gson = new Gson();
	
	private static WebSocketFrame currentWebSocketFrame;
	
	// Single parameter constructor
	public LeapWebSocket(URI serverURI)
	{
		super(serverURI);
	}
	
	// Two parameter constructor specifying a specific draft of the WebSocket protocol
	public LeapWebSocket(URI serverUri , Draft draft)
	{
		super(serverUri, draft);
	}
	
	public WebSocketFrame getCurrentWebSocketFrame() { return currentWebSocketFrame; }

	@Override
	public void onOpen(ServerHandshake handshakeData)
	{
		System.out.println( "opened connection" );
		
		// If you plan to refuse connection based on IP or httpfields, overload the onWebsocketHandshakeReceivedAsClient method.
	}

	@Override
	public void onMessage(String jsonString)
	{
		//System.out.println( "Received JSON string: " + jsonString );
		
		// Get the current WebSocketFrame inflated from the JSON string
		currentWebSocketFrame = gson.fromJson(jsonString, WebSocketFrame.class);
	    
	    // If we're recording and the recorder is enabled...
	    if ( (LeapDataRecorder.getMode() == Mode.RECORD) && ( LeapDataRecorder.isEnabled() ) )
	    {
	    	// ...then write it to the file
	    	LeapDataRecorder.writeWebSocketFrame(currentWebSocketFrame);
	    }
	}

	/*@Override
	public void onFragment(Framedata fragment)
	{
		System.out.println( "received fragment: " + new String( fragment.getPayloadData().array() ) );
	}*/

	@Override
	public void onClose(int code, String reason, boolean remote)
	{
		// The codes are documented in class org.java_websocket.framing.CloseFrame
		System.out.println( "Connection closed by " + ( remote ? "remote peer" : "us" ) );
		
		// Close the leapDataRecorder - this flushes and closes all streams
		if ( LeapDataRecorder.isEnabled() )
		{
			LeapDataRecorder.close();
		}
	}

	@Override
	public void onError(Exception ex)
	{
		ex.printStackTrace();
		// if the error is fatal then onClose will be called additionally
	}

}
