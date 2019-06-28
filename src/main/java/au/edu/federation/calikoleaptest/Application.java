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
    Mat4f mvpMatrix  = Mat4f.createOrthographicProjectionMatrix(
            -(float)WIDTH/2.0f,   (float)WIDTH/2.0f,
            (float)HEIGHT/2.0f, -(float)HEIGHT/2.0f,
            1.0f,               -1.0f );

    FabrikChain2D chain = new FabrikChain2D(); // Create a new 2D chain

    // ----- Static properties -----

    static Controller leapController = new Controller();

    static Listener   leapListener   = new LeapListener(LeapListener.ListenerType.USB_LISTENER);
    //static Listener   leapListener   = new LeapListener(LeapListener.ListenerType.WEBSOCKET_LISTENER);

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
