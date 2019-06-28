/***
 * 
 * File       : LeapDataReader.java
 * Description: A LeapDataReader reads WebSocketFrames from file so that they can be replayed. The file itself may or may not be GZIP compressed.
 * Author     : Al Lansley
 * Date       : 06/06/2014
 * Version    : 0.2
 * 
 */

package au.edu.federation.leapdatarecorder;

import java.io.EOFException;
import java.io.FileNotFoundException;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.zip.GZIPInputStream;

import au.edu.federation.leapwebsocket.WebSocketFrame;

// Class to write a compressed stream of SimpleFrames to a file
public class LeapDataReader {
	
	// Input streams
	private FileInputStream   fileInputStream;	
	private GZIPInputStream   gzipInputStream;
	private ObjectInputStream objectInputStream;
	
	// We'll keep a copy of the current WebSocketFrame that we read from the file
	private WebSocketFrame currentReaderWebSocketFrame;
	
	private String  filename;
	private boolean loopPlayback;
	private boolean reachedEndOfFile;

	// Constructor
	public LeapDataReader(String filename, boolean loopPlayback)
	{
		// Hand off the initialisation code to a separate function so the same LeapDataReader
		// object can be re-used to open and play another file.
		readFile(filename, loopPlayback);
	}
	
	public boolean readFile(String filename, boolean loopPlayback)
	{
		// Whether we managed to open the file successfully or not
		boolean fileOpenStatus = false;
		
		this.filename     = filename;
		this.loopPlayback = loopPlayback;
		
		reachedEndOfFile = false;
		
		// Instantiate the currentSimpleFrame.
		// Note: We use this so we can check whether we have a valid frame
		currentReaderWebSocketFrame = new WebSocketFrame();
		
		// Setup the file/gzip/object streams
		try
		{
			// Initialise streams
			// Note: It doesn't matter if we run an non-compressed stream through the gzipInputStream - it just passes it through untouched
			fileInputStream = new FileInputStream(filename);
			
			gzipInputStream = new GZIPInputStream(fileInputStream);
			
			objectInputStream = new ObjectInputStream(gzipInputStream);
			
			fileOpenStatus = true;
		}
		catch (FileNotFoundException e)
		{
			System.out.println("File not found: " + filename);
			System.exit(-1);
			//e.printStackTrace();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		
		return fileOpenStatus;		
	}
	
	// Method to read and return a WebSocketFrame from the file
	public WebSocketFrame readWebSocketFrame()
	{
		// We ALWAYS have to return a WebSocketFrame object, so we'll instantiate it in this scope
		WebSocketFrame wsf = new WebSocketFrame();
		
		// While there are frames to read from the file...
		if (!reachedEndOfFile)
		{
			try
			{				
				// Read the WebSocketFrame from the file
				wsf = (WebSocketFrame)objectInputStream.readObject();
			
				// If we successfully get the object without causing an exception then we'll
				// update the currentSimpleFrame to be the one we just got. This allows us
				// to pass back the last valid object if something goes wrong.
				currentReaderWebSocketFrame = wsf;
			}
			catch (EOFException eof)
			{
				System.out.println("Reached end of file!");
				reachedEndOfFile = true;
				
				// If we don't know that we've reached the end of the file, but in fact we have, we asign
				// the last currentWebSocketFrame to be what we return instead of the blank wsf object.
				wsf = currentReaderWebSocketFrame;
			}
			catch (IOException ioe)
			{
				ioe.printStackTrace();
			}
			catch (ClassNotFoundException e)
			{
				e.printStackTrace();
			}
		}
		else // If we've reached the end of the file... 
		{
			// If we're looping, we'll reload the file
			if (loopPlayback)
			{
				// Note: This resets the reachedEndOfFile flag to false
				readFile(filename, loopPlayback);				
			}
			else // ...and if we're not looping we'll just provide the final frame again and keep the reachedEndOfFile flag as true so we don't try to parse anything else
			{
				wsf = currentReaderWebSocketFrame;
			}
		}
		
		return wsf;
	}
	
	public void setLoopPlayback(boolean value)
	{
		loopPlayback = value;
	}
	
	// Method to close down all streams
	public void close()
	{
		try
		{
			objectInputStream.close();			
			gzipInputStream.close();			
			fileInputStream.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

}
