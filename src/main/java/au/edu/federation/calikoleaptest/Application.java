package au.edu.federation.calikoleaptest;

import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.*;

import au.edu.federation.caliko.*;
import au.edu.federation.utils.*;
import au.edu.federation.caliko.visualisation.*;

import com.leapmotion.leap.*;

public class Application
{
    // We need to strongly reference callback instances.
    private GLFWErrorCallback errorCB;
    private GLFWKeyCallback   keyCB;

    static int WIDTH  = 800; // Window width
    static int HEIGHT = 600; // Window height
    private long window;     // Window handle

    static Mat4f viewMatrix = new Mat4f(1.0f);

    // 3D projection matrix. Params: Left, Right, Top, Bottom, Near, Far
    static Mat4f projMatrix  = Mat4f.createPerspectiveProjectionMatrix(90.0f, 1.66f, 0.1f, 1000.0f);

    // ModelViewProjectionMatrix
    static Mat4f mvpMatrix;

    // Grids to give perspective
    private static Grid upperGrid, lowerGrid;

    private static Line3D line;

    static Colour4f leapIKHandColour   = Utils.RED;
    static Colour4f calikoIKHandColour = Utils.BLUE;

    //static FabrikStructure3D rightHandStructure = new FabrikStructure3D("right hand structure");
    static FabrikChain3D[] fingerChain = new FabrikChain3D[5];

    // ----- Leap Static properties -----
    static Controller leapController = new Controller();
    static Listener   leapListener   = new LeapListener(LeapListener.ListenerType.USB_LISTENER);
    //static Listener   leapListener   = new LeapListener(LeapListener.ListenerType.WEBSOCKET_LISTENER);
    static Frame frame;
    static Hand hand;
    static Finger firstFinger;
    static FingerList fingerList;


    // ----- Constants -----
    public static final float GOLDEN_RATIO              = 1.6181033f;
    public static final float HAND_RATIO                = 5.23606797f;
    public static final float DISTAL_RATIO              = 1.0f / HAND_RATIO;
    public static final float PROXIMAL_RATIO            = GOLDEN_RATIO / HAND_RATIO;
    public static final float METACARPOPHALANGEAL_RATIO = (GOLDEN_RATIO * GOLDEN_RATIO) / HAND_RATIO;
    public static final float CARPOMETACARPEL_RATIO     = (GOLDEN_RATIO * GOLDEN_RATIO * GOLDEN_RATIO) / HAND_RATIO;



    public static final float VERTICAL_OFFSET    = 200.0f;
    public static final float LEAP_DRAW_OFFSET   = -150.0f;
    public static final float CALIKO_DRAW_OFFSET = 150.0f;

    private static void drawFingersLeapIK()
    {
        // Loop over all fingers in the list of fingers
        for (Finger f : fingerList)
        {
            // Loop over finger bones drawing each. Bone order is:
            // TYPE_METACARPAL   - prevJoint at wrist
            // TYPE_PROXIMAL     - prevJoint at 'finger base'   - this is the third 'base-of-finger' joint
            // TYPE_INTERMEDIATE -                              - this is the second 'knuckle' joint
            // TYPE_DISTAL       - nextJoint at finger tip      - this is the first joint, closest to fingertip
            int count = 0;
            for ( Bone.Type boneType : Bone.Type.values() )
            {
                Bone bone = f.bone(boneType);

                Vector boneStart = bone.prevJoint();
                Vector boneEnd   = bone.nextJoint();
                Vec3f start      = new Vec3f( boneStart.getX() + LEAP_DRAW_OFFSET, boneStart.getY() - VERTICAL_OFFSET, boneStart.getZ() );
                Vec3f end        = new Vec3f( boneEnd.getX()   + LEAP_DRAW_OFFSET, boneEnd.getY()   - VERTICAL_OFFSET, boneEnd.getZ()   );

                if (count % 2 == 0)
                {
                    line.draw(start, end, Utils.BLUE, 5.0f, mvpMatrix);
                }
                else
                {
                    line.draw(start, end, Utils.MID_BLUE, 5.0f, mvpMatrix);
                }
                ++count;
            }

        } // End of loop over fingers
    }

    private static void drawFingersCalikoIK()
    {
        // Loop over all fingers in the list of fingers
        int count = 0;
        for (Finger f : fingerList)
        {
            // Get the tip and base location of the finger
            Vector tipLocation  = f.tipPosition();
            Vector baseLocation = f.direction().opposite().times( f.length() );

            // Use golden-ratio based anatomical model to calculate joint locations.
            //Vec3f tip  = new Vec3f( tipLocation.getX() + CALIKO_DRAW_OFFSET,  tipLocation.getY()  - VERTICAL_OFFSET, tipLocation.getZ()  );
            //Vec3f base = new Vec3f( baseLocation.getX() + CALIKO_DRAW_OFFSET, baseLocation.getY() - VERTICAL_OFFSET, baseLocation.getZ() );

            // Create joints for fingers in a straight line along finger direction then solve them to get the curve
            //Vec3f tip = new Vec3f(tipLocation.getX(), tipLocation.getY(), tipLocation.getZ());
            //Vec3f direction = new Vec3f(f.direction)
            //Vec3f distalJoint = new Vec

            Vec3f  tipToBaseDir = new Vec3f( f.direction().opposite().getX(), f.direction().opposite().getY(), f.direction().opposite().getZ()  );
            float  len          = f.length();
            //System.out.println(count + " " + len);

            // Construct joint locations using golden ration based anatomical model
            Vec3f tip                 = new Vec3f( tipLocation.getX(), tipLocation.getY(), tipLocation.getZ()                       );
            Vec3f distal              = new Vec3f( tip.plus( tipToBaseDir.times(DISTAL_RATIO).times(len)                          ) );
            Vec3f proximal            = new Vec3f( distal.plus( tipToBaseDir.times(PROXIMAL_RATIO).times(len)                     ) );
            Vec3f metacarpophalangeal = new Vec3f( proximal.plus( tipToBaseDir.times(METACARPOPHALANGEAL_RATIO).times(len)        ) );

            // The location of the metacrpophalangeal joint is fixed relative to the hand, so we obtain it by taking a direction from the fingertip to the hand
            Hand h = frame.hands().rightmost();
            Vector handPosition = h.palmPosition();
            Vec3f armDir = new Vec3f(h.arm().direction().opposite().getX(),h.arm().direction().opposite().getY(), h.arm().direction().opposite().getZ() );

            Vec3f carpometacarpel     = new Vec3f( metacarpophalangeal.plus( armDir.times(CARPOMETACARPEL_RATIO).times(len) ) );



            System.out.println(handPosition);

            //Vec3f carpometacarpel     = new Vec3f( handPosition.getX(), handPosition.getY(), handPosition.getZ() );

            // Push to the right and up
            tip.x                 += CALIKO_DRAW_OFFSET;
            tip.y                 -= VERTICAL_OFFSET;
            distal.x              += CALIKO_DRAW_OFFSET;
            distal.y              -= VERTICAL_OFFSET;
            proximal.x            += CALIKO_DRAW_OFFSET;
            proximal.y            -= VERTICAL_OFFSET;
            metacarpophalangeal.x += CALIKO_DRAW_OFFSET;
            metacarpophalangeal.y -= VERTICAL_OFFSET;
            carpometacarpel.x     += CALIKO_DRAW_OFFSET;
            carpometacarpel.y     -= VERTICAL_OFFSET;

            FabrikBone3D carpoBone  = new FabrikBone3D(carpometacarpel, metacarpophalangeal); carpoBone.setColour(Utils.RED);
            FabrikBone3D metaBone   = new FabrikBone3D(metacarpophalangeal, proximal);        metaBone.setColour(Utils.MID_RED);
            FabrikBone3D distilBone = new FabrikBone3D(proximal, distal);                     distilBone.setColour(Utils.RED);
            FabrikBone3D tipBone    = new FabrikBone3D(distal, tip);                          tipBone.setColour(Utils.MID_RED);

            fingerChain[count] = new FabrikChain3D();

            // Only add a 4th segment if we are NOT a thumb!
            if (count != 0) { fingerChain[count].addBone(carpoBone); }

            fingerChain[count].addBone(metaBone);
            fingerChain[count].addBone(distilBone);
            fingerChain[count].addBone(tipBone);

            fingerChain[count].solveForTarget( tip.minus( tipToBaseDir.times( f.length() * 0.09f) ) );

            FabrikLine3D.draw(fingerChain[count], 5.0f, mvpMatrix);

            /*
            line.draw(tip, distal, Utils.RED, 5.0f, mvpMatrix);
            line.draw(distal, proximal, Utils.GREEN, 5.0f, mvpMatrix);
            line.draw(proximal, metacarpophalangeal, Utils.BLUE, 5.0f, mvpMatrix);
            line.draw(metacarpophalangeal, carpometacarpel, Utils.YELLOW, 5.0f, mvpMatrix);
             */

            // Increment counter to move to next finger on next pass
            ++count;
        }
    }

    // Assign new frame by reference
    public static void updateFrame(Frame f)
    {
        frame = f;

        if (frame != null && frame.isValid() && frame.hands().count() > 0)
        {
            // 	Get the leftmost hand
            hand = frame.hands().leftmost();

            // Get all fingers on the hand
            fingerList = hand.fingers();
        }
        else
        {
            //System.out.println("Got a frame, but no hands visible.");
        }
    }

    public void run()
    {
        try
        {
            init();
            loop();
        }
        finally
        {
            // Free the keyboard callback and destroy the window
            keyCB.close();
            glfwDestroyWindow(window);

            // Terminate GLFW and free the error callback
            glfwTerminate();
            glfwSetErrorCallback(null).free();
        }
    }

    private void init()
    {
        // ----- LWJGL / GLFW / OpenGL Setup -----

        // Setup an error callback. The default implementation
        // will print the error message in System.err.
        glfwSetErrorCallback(errorCB = GLFWErrorCallback.createPrint(System.err));

        // Initialize GLFW. Most GLFW functions will not work before doing this.
        if ( !glfwInit() )
            throw new IllegalStateException("Unable to initialize GLFW");

        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE); // The window will be resizable. NOTE: Window hints must be provided before the window is created.
        glfwWindowHint(GLFW_SAMPLES, 8);     // Ask for 8x Anti-aliasing (no guarantee we'll get it, but we can ask for it).

        // Create the window
        window = glfwCreateWindow(WIDTH, HEIGHT, "Caliko Leap Test v0.1 | Left/Blue hand is LeapIK, Right/Red hand is Caliko IK", NULL, NULL);
        if (window == NULL)
        {
            throw new RuntimeException("Failed to create the GLFW window.");
        }

        // Setup a key callback
        glfwSetKeyCallback(window, keyCB = new GLFWKeyCallback() {
            @Override
            public void invoke(long window, int key, int scancode, int action, int mods)
            {
                if ( key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE )
                    glfwSetWindowShouldClose(window, true);
            }
        });

        // Get the resolution of the primary monitor
        GLFWVidMode vidMode = glfwGetVideoMode( glfwGetPrimaryMonitor() );

        // Center our window
        glfwSetWindowPos(window, (vidMode.width() - WIDTH) / 2,(vidMode.height() - HEIGHT) / 2);

        glfwMakeContextCurrent(window); // Make the OpenGL context current
        glfwSwapInterval(1);            // Enable v-sync
        glfwShowWindow(window);         // Make the window visible

        // This line is critical for LWJGL's interoperation with GLFW's OpenGL context, or any context that is managed
        // externally. LWJGL detects the context that is current in the current thread, creates the GLCapabilities
        // instance and makes the OpenGL bindings available for use.
        GL.createCapabilities();

        // ----- Setup grids -----
        upperGrid = new Grid(1000.0f, 1000.0f, 300.0f, 20);
        lowerGrid = new Grid(1000.0f, 1000.0f, -300.0f, 20);

        // Move view matrix backwards and generate MVP matrix
        viewMatrix.translate(0.0f, 0.0f,-200.0f);
        mvpMatrix = projMatrix.times(viewMatrix);

        // Create a Line3D we'll use to draw things (we have to wait until we have a valid OpenGL context to do this)
        line = new Line3D();

        // Create empty FabrikChain3D objects
        for (int loop = 0; loop < 5; ++loop)
        {
            fingerChain[loop] = new FabrikChain3D();
        }
    }

    private void loop()
    {
        // Set the clear color
        glClearColor(0.0f, 0.0f, 0.0f, 0.0f);



        // Run the rendering loop until the user has attempted to close the window or has pressed the ESCAPE key.
        while ( !glfwWindowShouldClose(window) )
        {
            // Clear colour and depth buffers + draw perspective grids
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
            upperGrid.draw(mvpMatrix);
            lowerGrid.draw(mvpMatrix);

            // Got a hand containing fingers? Draw!
            if (fingerList != null && !fingerList.isEmpty())
            {
                drawFingersLeapIK();
                drawFingersCalikoIK();
            }

            glfwSwapBuffers(window); // Swap colour buf.
            glfwPollEvents();        // Poll for events.
        }
    }

    public static void main(String[] args)
    {
        // Bind a listener to the Leap device
        leapController.addListener(leapListener);

        new Application().run();

        // Remove the listener from the Leap device
        leapController.removeListener(leapListener);
    }
}
