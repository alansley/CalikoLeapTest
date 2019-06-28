/***
 * 
 * File       : LeapDataWrite.java
 * Description: A LeapDataWriter reads WebSocketFrames from the Leap WebSocket interface (accessed via a LeapWebSocket object)
 *              and writes them to file in serialized form. The file may optionally also be GZIP compressed.
 * Author     : Al Lansley
 * Date       : 12/02/2014
 * Version    : 0.2
 * 
 */

package au.edu.federation.leapdatarecorder;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.zip.GZIPOutputStream;

import org.java_websocket.WebSocketImpl;
import org.java_websocket.drafts.Draft;
import org.java_websocket.drafts.Draft_6455;

import au.edu.federation.leapwebsocket.LeapWebSocket;
import au.edu.federation.leapwebsocket.WebSocketFrame;
import org.java_websocket.enums.CloseHandshakeType;
import org.java_websocket.enums.HandshakeState;
import org.java_websocket.exceptions.InvalidDataException;
import org.java_websocket.exceptions.InvalidHandshakeException;
import org.java_websocket.framing.Framedata;
import org.java_websocket.handshake.*;

// Class to write a compressed stream of WebSocketFrames to a file
public class LeapDataWriter
{
	private String filename;
	
	private FileOutputStream   fileOutputStream;	
	private GZIPOutputStream   gzipOutputStream;
	private ObjectOutputStream objectOutputStream;
	
	private boolean writeCompressedStream;
	
	private LeapWebSocket leapWebSocket;
		
	// Constructor
	public LeapDataWriter(String theFilename, boolean writeCompressedStreamValue)
	{
		// The data we write is captured from the WebSocket interface, so connect to it.
		try {
			leapWebSocket = new LeapWebSocket(new URI("ws://localhost:6437"), new Draft_6455() );

			/*
			    @Override
				public HandshakeState acceptHandshakeAsClient(ClientHandshake request, ServerHandshake response) throws InvalidHandshakeException {
					return null;
				}

				@Override
				public HandshakeState acceptHandshakeAsServer(ClientHandshake handshakedata) throws InvalidHandshakeException {
					return null;
				}

				@Override
				public ByteBuffer createBinaryFrame(Framedata framedata) {
					return null;
				}

				@Override
				public List<Framedata> createFrames(ByteBuffer binary, boolean mask) {
					return null;
				}

				@Override
				public List<Framedata> createFrames(String text, boolean mask) {
					return null;
				}

				@Override
				public void processFrame(WebSocketImpl webSocketImpl, Framedata frame) throws InvalidDataException {

				}

				@Override
				public void reset() {

				}

				@Override
				public ClientHandshakeBuilder postProcessHandshakeRequestAsClient(ClientHandshakeBuilder request) throws InvalidHandshakeException {
					return null;
				}

				@Override
				public HandshakeBuilder postProcessHandshakeResponseAsServer(ClientHandshake request, ServerHandshakeBuilder response) throws InvalidHandshakeException {
					return null;
				}

				@Override
				public List<Framedata> translateFrame(ByteBuffer buffer) throws InvalidDataException {
					return null;
				}

				@Override
				public CloseHandshakeType getCloseHandshakeType() {
					return null;
				}

				@Override
				public Draft copyInstance() {
					return null;
				}
			};
			*/


			leapWebSocket.connect();
		}
		catch (URISyntaxException e1)
		{
			e1.printStackTrace();
			System.exit(-1);
		}
		
		filename = theFilename;
		
		writeCompressedStream = writeCompressedStreamValue;
		
		// Set up the streams
		try
		{
			fileOutputStream = new FileOutputStream(filename);
			
			// If we're writing a compressed stream then...
			if (writeCompressedStream)
			{
				// ...set up the gzipOutputStream and...
				gzipOutputStream = new GZIPOutputStream(fileOutputStream);
				
				// ...set the objectOutputStream to use it.
				objectOutputStream = new ObjectOutputStream(gzipOutputStream);
			}
			else //Otherwise we'll just use the fileOutputStream for the objectOutputStream
			{
				objectOutputStream = new ObjectOutputStream(fileOutputStream);
			}
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	// Method to write a WebSocketFrame to file
	public void writeWebSocketFrame(WebSocketFrame wsf)
	{
		try
		{
			objectOutputStream.writeObject(wsf);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	// Method to close the output streams
	public void close()
	{
		
		
		try
		{
			objectOutputStream.flush();
			objectOutputStream.close();
			
			// If we're writing a compressed stream then flush and close the gzipOutputStream
			if (writeCompressedStream)
			{
				gzipOutputStream.flush();
				gzipOutputStream.close();
			}
			
			fileOutputStream.flush();
			fileOutputStream.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		
		// Close our connection to the leap WebSocket interface
				leapWebSocket.close();
	}

} // End of LeapDataRecorder class
