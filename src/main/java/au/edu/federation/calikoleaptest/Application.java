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

    int WIDTH = 800; int HEIGHT = 600; // Window width and height
    private long window;               // Window handle

    // 2D projection matrix. Params: Left, Right, Top, Bottom, Near, Far
    static Mat4f mvpMatrix  = Mat4f.createOrthographicProjectionMatrix(
            -(float)WIDTH/2.0f,   (float)WIDTH/2.0f,
            (float)HEIGHT/2.0f, -(float)HEIGHT/2.0f,
            1.0f,               -1.0f );

    private static Grid upperGrid, lowerGrid;

    private static Line3D line;

    static FabrikStructure3D handStructure = new FabrikStructure3D("hand structure");
    static FabrikChain3D[] fingerChainArray;

    FabrikChain2D chain = new FabrikChain2D(); // Create a new 2D chain

    // ----- Leap Static properties -----
    static Controller leapController = new Controller();
    static Listener   leapListener   = new LeapListener(LeapListener.ListenerType.USB_LISTENER);
    //static Listener   leapListener   = new LeapListener(LeapListener.ListenerType.WEBSOCKET_LISTENER);
    static Frame frame;
    static Hand hand;
    static Finger firstFinger;
    static FingerList fingerList;


    // ----- Constants -----
    public static final float GOLDEN_RATIO = 1.6181033f;
    public static final float HAND_RATIO   = 5.23606797f;

    private static void drawFingersStockIK()
    {
        // Loop over all fingers
        int fingerNumber = 0;
        for (Finger f : fingerList)
        {
            //System.out.println("Num fingers visible: " + fingerList.count());

            //System.out.println("Finger id: " + f.id() + " has length: " + f.length() );

            Colour4f colour = new Colour4f();
            switch (fingerNumber)
            {
                case 0:
                    colour.set(1.0f, 0.0f, 0.0f, 1.0f);
                    break;
                case 1:
                    colour.set(1.0f, 0.4f, 0.0f, 1.0f);
                    break;
                case 2:
                    colour.set(1.0f, 1.0f, 0.0f, 1.0f);
                    break;
                case 3:
                    colour.set(0.0f, 1.0f, 1.0f, 1.0f);
                    break;
                case 4:
                    colour.set(0.0f, 0.0f, 1.0f, 1.0f);
                    break;
            }


            int inToOut = 0;

            // Bone order is:
            // TYPE_METACARPAL   - prevjoint at wrist
            // TYPE_PROXIMAL     - prevJoint at 'finger base'
            // TYPE_INTERMEDIATE
            // TYPE_DISTAL       - nextJoint at finger tip
            for( Bone.Type boneType : Bone.Type.values() )
            {
                Bone bone = f.bone(boneType);


                //System.out.println(fingerNumber + " is of type: " + boneType);

                Vector boneStart = bone.prevJoint();
                Vector boneEnd   = bone.nextJoint();
                Vec3f start = new Vec3f( boneStart.getX(), boneStart.getY() - 200.0f, boneStart.getZ() );// * -1.0f );
                Vec3f end   = new Vec3f( boneEnd.getX(),   boneEnd.getY() - 200.0f,   boneEnd.getZ()   );//* -1.0f );

                line3D.draw(start, end, colour, 5.0f, mvpMatrix);

                // ... Use the bone
            }

            ++fingerNumber;

        }
    }

    private static void drawFingersCalikoIK()
    {
        // Loop over all fingers

        for (int loop = 0; loop < 5; ++loop)
        {
            Colour4f colour = new Colour4f();
            switch (loop)
            {
                case 0:
                    colour.set(1.0f, 0.0f, 0.0f, 1.0f);
                    break;
                case 1:
                    colour.set(1.0f, 0.4f, 0.0f, 1.0f);
                    break;
                case 2:
                    colour.set(1.0f, 1.0f, 0.0f, 1.0f);
                    break;
                case 3:
                    colour.set(0.0f, 1.0f, 1.0f, 1.0f);
                    break;
                case 4:
                    colour.set(0.0f, 0.0f, 1.0f, 1.0f);
                    break;
            }


            // Need to update chain details from frame (both base and tip)
            // Then draw

            //System.out.println("****************************Num bones is: " + fingerChainArray[loop].getChain().size());

    		/*
    		au.edu.federation.caliko.utils.Mat4f tempMvp = new au.edu.federation.caliko.utils.Mat4f();
    		tempMvp.setFromArray(mvpMatrix.toArray());
    		FabrikLine3D.draw(fingerChainArray[loop], tempMvp);
    		*/

            for (FabrikBone3D fb : fingerChainArray[loop].getChain())
            {
                //System.out.println("Attempting to draw fabrik finger: " + loop);

                au.edu.federation.caliko.utils.Vec3f start = fb.getStartLocation();
                au.edu.federation.caliko.utils.Vec3f end   = fb.getEndLocation();


                Vec3f s = new Vec3f(start.x, start.y - 200.0f, start.z);
                Vec3f e = new Vec3f(end.x, end.y - 200.0f, end.z);

                //System.out.println("For this bone, s is: " + s.toString() + " and e is: " + e.toString());

                line3D.draw(s, e, colour, 5.0f, mvpMatrix);
            }


        }
    }

    // Assign new frame by reference
    public static void updateFrame(Frame f)
    {
        frame = f;

        if (!calikoInitialised)
        {
            if ( frame != null && frame.isValid() )
            {
                int handCount = frame.hands().count();

                if (handCount > 1)
                {
                    System.out.println("> 1 HAND DETECTED! WE ONLY DEAL WITH ONE HAND AT A TIME!");
                }
                else if (handCount < 1)
                {
                    calikoInitialised = false;
                    //System.out.println("NO HANDS DETECTED!");
                }
                else
                {
                    // 	Get the first (and only) hand
                    hand = frame.hands().get(0);

                    // Get all fingers on the hand
                    fingerList = hand.fingers();

                    float confidence = hand.confidence();
                    //System.out.println("Hand confidence = " + confidence);

                    // Got a good, unobstructed view of the hand containing 5 fingers?
                    if (confidence > 0.95f && fingerList.count() == 5)
                    {

                        // Initialise our fabrik chain array with finger details
                        int count = 0;
                        for (Finger finger : fingerList)
                        {
                            addFingerBonesToChain(finger, fingerChainArray[count]);
                            ++count;
                        }

                        calikoInitialised = true;
                    }
                }


            }
        }
        else // Initialised? Update base and tip locations!
        {
            int count = 0;
            for (Finger finger : fingerList)
            {
                Vector leapBaseLocation = finger.bone(Bone.Type.TYPE_METACARPAL).prevJoint();
                Vector leapTipLocation  = finger.bone(Bone.Type.TYPE_DISTAL).nextJoint();

                Vec3f baseLoc = new Vec3f(leapBaseLocation.getX(), leapBaseLocation.getY(), leapBaseLocation.getZ());
                Vec3f tipLoc  = new Vec3f(leapTipLocation.getX(), leapTipLocation.getY(), leapTipLocation.getZ());

                fingerChainArray[count].setBaseLocation(baseLoc);
                float solveDist = fingerChainArray[count].updateTarget(tipLoc); // IMPORTANT: This triggers IK configuration recalculation!

                System.out.println("Got solve distance of: " + solveDist);

                ++count;
            }








        }

    }



    public void run()
    {
        // Create our chain
        FabrikBone2D base = new FabrikBone2D(new Vec2f(), new Vec2f(0.0f, 50.0f));
        chain.addBone(base);
        for (int boneLoop = 0; boneLoop < 5; ++boneLoop)
        {
            chain.addConsecutiveBone(new Vec2f(0.0f, 1.0f), 50.0f);
        }

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
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE); // the window will be resizable

        // Create the window
        window = glfwCreateWindow(WIDTH, HEIGHT, "Hello, Caliko!", NULL, NULL);
        if (window == NULL)
            throw new RuntimeException("Failed to create the GLFW window");

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
        GLFWVidMode vidmode = glfwGetVideoMode( glfwGetPrimaryMonitor() );

        // Center our window
        glfwSetWindowPos(window, (vidmode.width() - WIDTH) / 2,
                (vidmode.height() - HEIGHT) / 2);

        glfwMakeContextCurrent(window); // Make the OpenGL context current
        glfwSwapInterval(1);            // Enable v-sync
        glfwShowWindow(window);         // Make the window visible
    }

    private void loop()
    {
        // This line is critical for LWJGL's interoperation with GLFW's
        // OpenGL context, or any context that is managed externally.
        // LWJGL detects the context that is current in the current thread,
        // creates the GLCapabilities instance and makes the OpenGL
        // bindings available for use.
        GL.createCapabilities();

        // Set the clear color
        glClearColor(0.0f, 0.0f, 0.0f, 0.0f);

        Vec2f offset = new Vec2f(150.0f, 0.0f);
        Vec2f target = new Vec2f(100.0f, 100.0f);

        // Run the rendering loop until the user has attempted to close
        // the window or has pressed the ESCAPE key.
        while ( glfwWindowShouldClose(window) == false ) {
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); // Clear buffers
            chain.solveForTarget( target.plus(offset) );        // Solve the chain
            FabrikLine2D.draw(chain, 3.0f, mvpMatrix);          // Draw the chain
            glfwSwapBuffers(window);                            // Swap colour buf.

            // Rotate the offset 1 degree per frame
            offset = Vec2f.rotateDegs(offset, 1.0f);

            // Poll for window events. The key callback above will only be
            // invoked during this call.
            glfwPollEvents();
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
