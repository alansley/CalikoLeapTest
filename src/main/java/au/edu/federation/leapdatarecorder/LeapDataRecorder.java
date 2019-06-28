/***
 * 
 * File       : LeapDataRecorder.java
 * Description: A LeapDataRecorder can read or write LeapWebSocket data to/from files depending on if it's in RECORD or REPLAY mode.
 *              Before the LeapDataRecorder will do its stuff, you need to call enable() on it. This allows us to simply comment out
 *              the enable() call on a single line during development to nix all recording/playback functionality.
 * Author     : Al Lansley
 * Date       : 06/06/2014
 * Version    : 0.4
 * 
 */

package au.edu.federation.leapdatarecorder;

import au.edu.federation.leapwebsocket.WebSocketFrame;


// This class acts as a wrapper around the LeapDataReader and LeapDataWriter classes and just
// provides a single interface to use them both. You can use the LeapDataReader/LeapDataWriter
// classes on their own if you don't want to use this.
//
// Example usage:
// 
//		-------------- Set up leap data recorder -----------------
//
//		To record hand data, instantiate the LeapDataRecorder in RECORD mode:
//			leapDataRecorder = new LeapDataRecorder(Mode.RECORD, "leapTestData.gzip");
//		
//		When writing, optionally choose whether to compress the data stream or not (default is to compress the stream):
//			LeapDataRecorder.setCompressData(false);
//		
//		To replay recorded data, instantiate the LeapDataRecorder in REPLAY mode:
//			leapDataRecorder = new LeapDataRecorder(Mode.REPLAY, "leapTestData.gzip");
//		
// 		When replaying, optionally choose whether to loop the playback or not (default is to loop the playback). If you opt
//		not to loop the playback, when it reaches the end of the stream it'll simply return the last valid WebSocketFrame forever:
//			LeapDataRecorder.setLoopPlayback(false);
//	
// 		Regardless of whether we're recording or replaying, we need to enable LeapDataRecorder for it to do its thing.
// 		This allows us to simply comment this line when developing to disable all recording and replay:
//			LeapDataRecorder.enable();
//
public class LeapDataRecorder 
{
	
	// A LeapDataRecorder must operate in one of two modes: It's either RECORDing hand data, or it's REPLAYing hand data.
	public enum Mode { RECORD, REPLAY }

	// Static leap data recorder and reader instances
	private static LeapDataWriter leapDataWriter;
	private static LeapDataReader leapDataReader;
	
	// Static properties and flags
	private static Mode    mode;          // Recording hand data or replaying it?
	private static String  filename;      // Name of file to read from or write to
	private static boolean compressData;  // Should we compress the data into a zipped stream?
	private static boolean loopReplay;	  // Should we loop the replay?
	private static boolean enabled;       // Should we do anything at all? See constructor for further comments.

	public LeapDataRecorder(Mode modeArg, String filenameArg)
	{
		mode     = modeArg;
		filename = filenameArg;
		
		// We need to explicitly call enable() before we're going to start recording or playback
		// Note: This is so we can nix any playback or recording by commenting out the enable() call in out main
		enabled = false;
		
		// Defaults are to write compressed data and loop any replays
		compressData = true;
		loopReplay   = true;
		
		// Instantiate the appropriate object depending on whether we're replaying or recording data
		if (mode == Mode.REPLAY)
		{
			leapDataReader = new LeapDataReader(filename, loopReplay);
		}
		else
		{
			leapDataWriter = new LeapDataWriter(filename, compressData);
		}
	}
	
	public static void    enable()    { enabled = true; } // Method to set the flag to say we're running! Once enabled we cannot be disabled!
	public static boolean isEnabled() { return enabled; } // Method to check if we're running or not
	
	// Method to write a WebSocketFrame to file
	public static void writeWebSocketFrame(WebSocketFrame wsf)
	{
		leapDataWriter.writeWebSocketFrame(wsf);
	}
	
	// Method to read a WebSocketFrame from file
	public static WebSocketFrame readWebSocketFrame()
	{
		return leapDataReader.readWebSocketFrame();
	}	
	
	// Method to close the appropriate streams based on the current operating mode
	public static void close()
	{		
		if (mode == Mode.RECORD)
		{
			leapDataWriter.close();
		}
		else
		{
			leapDataReader.close();
		}
	}

	// ----- Getters -----	
	public static Mode getMode()                      { return mode;          } // You can get the current mode, but not change it!
	public static String getFilename()                { return filename;      }
	public static boolean getCompressData()           { return compressData;  }
	public static boolean getLoopReplay()             { return loopReplay;    } 
	
	// Setters
	public static void setCompressData(boolean value) {	compressData = value; }
	
 	public static void setLoopPlayback(boolean value)
 	{
 		// If we have a valid LeapDataReader object (i.e. this recorder was opened in REPLAY mode)...
 		if ( !(leapDataReader == null) )
 		{
 			// ...then change the loopPlayback flag as directed.
 			//
 			// Note: If this LeapDataRecorder was opened in RECORD mode then the leapDataReader object will be
 			// null so we never get here. We MUST use "==" instead of ".equals" here as you can't run a method
 			// (such as ".equals") on a null object!
 			leapDataReader.setLoopPlayback(value);
 		}
 	}
 	
}
