package au.edu.federation.calikoleaptest;

import com.leapmotion.leap.Controller;
import com.leapmotion.leap.Frame;
import com.leapmotion.leap.Listener;

public class LeapListener extends Listener
{
    public void onConnect(Controller controller)
    {
        System.out.println("Connected");
    }

    public void onFrame(Controller controller)
    {
        Frame leapFrame = controller.frame();

        System.out.println( "Frame id: "    + leapFrame.id()              +
                            ", timestamp: " + leapFrame.timestamp()       +
                            ", hands: "     + leapFrame.hands().count()   +
                            ", fingers: "   + leapFrame.fingers().count() );
    }
}
