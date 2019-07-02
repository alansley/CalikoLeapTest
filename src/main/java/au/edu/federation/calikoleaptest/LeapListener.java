package au.edu.federation.calikoleaptest;

import au.edu.federation.leapwebsocket.LeapWebSocket;
import com.leapmotion.leap.Controller;
import com.leapmotion.leap.Frame;
import com.leapmotion.leap.Listener;
import org.java_websocket.drafts.Draft_6455;

import java.net.URI;
import java.net.URISyntaxException;

public class LeapListener extends Listener
{
    // A listener is either a a USB listener or a websocket listener
    public enum ListenerType { USB_LISTENER, WEBSOCKET_LISTENER }

    // Store our listener type
    private static ListenerType leapListenerType;

    // Websocket we can listen on if we're not a USB listener
    private static LeapWebSocket leapWebSocket;

    // Constructor
    LeapListener(ListenerType lt)
    {
        // Call the superclass (Listener) constructor
        super();

        // Assign the listener type
        leapListenerType = lt;
    }

    @Override
    public void onInit(Controller controller)
    {
        System.out.println("LeapListener initialized.");

        // If we're using the WebSocket interface then initialise a LeapWebSocket
        if (leapListenerType == ListenerType.WEBSOCKET_LISTENER)
        {
            // Initialise the leap socket to capture the web socket data. Default port is 6437.
            try
            {
                leapWebSocket = new LeapWebSocket( new URI( "ws://localhost:6437" ), new Draft_6455() );
            }
            catch (URISyntaxException e)
            {
                e.printStackTrace();
                System.exit(0);
            }
        }
    }

    @Override
    public void onConnect(Controller controller)
    {
        System.out.println("LeapListener connected to device.");

        // If we're a web socket listener then connect to the web socket!
        if (leapListenerType == ListenerType.WEBSOCKET_LISTENER)
        {
            // Connect to the leap WebSocket
            leapWebSocket.connect();
        }
    }

    @Override
    public void onFrame(Controller controller)
    {
        Frame leapFrame = controller.frame();

        // Update from the USB obtained frame if that's what we're listening to...
        if (leapListenerType == ListenerType.USB_LISTENER)
        {
            Application.updateFrame(leapFrame);
            System.out.println("Updating frame @ " + leapFrame.timestamp());
        }

        System.out.println( "Frame id: " + leapFrame.id() + ", hands: " + leapFrame.hands().count() + ", fingers: " + leapFrame.fingers().count() );
    }

    @Override
    public void onExit(Controller leapController)
    {
        System.out.println("LeapListener exiting.");

        // If we're a web socket listener then close our connection to the web socket
        if (leapListenerType == ListenerType.WEBSOCKET_LISTENER)
        {
            leapWebSocket.close();
        }
    }

}
