package wesley.folz.blowme.ui;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.Log;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import wesley.folz.blowme.graphics.Background;
import wesley.folz.blowme.graphics.Dispenser;
import wesley.folz.blowme.graphics.FallingObject;
import wesley.folz.blowme.graphics.Fan;
import wesley.folz.blowme.util.Physics;

/**
 * Created by wesley on 5/10/2015.
 */
public class GamePlayRenderer implements GLSurfaceView.Renderer
{

    public GamePlayRenderer( Fan f )
    {
        fan = f;
        triangle = new FallingObject();
        //line = new Line();
        dispenser = new Dispenser();
        background = new Background();

    }

    /**
     * Called when the surface is created or recreated.
     * <p/>
     * Called when the rendering thread
     * starts and whenever the EGL context is lost. The EGL context will typically
     * be lost when the Android device awakes after going to sleep.
     * <p/>
     * Since this method is called at the beginning of rendering, as well as
     * every time the EGL context is lost, this method is a convenient place to put
     * code to create resources that need to be created when the rendering
     * starts, and that need to be recreated when the EGL context is lost.
     * Textures are an example of a resource that you might want to create
     * here.
     * <p/>
     * Note that when the EGL context is lost, all OpenGL resources associated
     * with that context will be automatically deleted. You do not need to call
     * the corresponding "glDelete" methods such as glDeleteTextures to
     * manually delete these lost resources.
     * <p/>
     *
     * @param gl     the GL interface. Use <code>instanceof</code> to
     *               test if the interface supports GL11 or higher interfaces.
     * @param config the EGLConfig of the created surface. Can be used
     */
    @Override
    public void onSurfaceCreated( GL10 gl, EGLConfig config )
    {
        // Set the background frame color
        GLES20.glClearColor( 1.0f, 1.0f, 1.0f, 1.0f );
        GLES20.glEnable(GLES20.GL_CULL_FACE);
        GLES20.glCullFace(GLES20.GL_BACK);
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        //GLES20.glEnable(GLES20.GL_BLEND);
        //GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_SRC_COLOR);

        //GLES20.glEnable(GLES20.GL_BLEND);
        //GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
        //fan = new Fan();
        fan.enableGraphics();
        fan.getWind().enableGraphics();
        triangle.enableGraphics();
        //line.enableGraphics();
        dispenser.enableGraphics();
        background.enableGraphics();
    }

    /**
     * Called when the surface changed size.
     * <p/>
     * Called after the surface is created and whenever
     * the OpenGL ES surface size changes.
     * <p/>
     * Typically you will set your viewport here. If your camera
     * is fixed then you could also set your projection matrix here:
     * <pre class="prettyprint">
     * void onSurfaceChanged(GL10 gl, int width, int height) {
     * gl.glViewport(0, 0, width, height);
     * // for a fixed camera, set the projection too
     * float ratio = (float) width / height;
     * gl.glMatrixMode(GL10.GL_PROJECTION);
     * gl.glLoadIdentity();
     * gl.glFrustumf(-ratio, ratio, -1, 1, 1, 10);
     * }
     * </pre>
     *
     * @param gl     the GL interface. Use <code>instanceof</code> to
     *               test if the interface supports GL11 or higher interfaces.
     * @param width
     * @param height
     */
    @Override
    public void onSurfaceChanged( GL10 gl, int width, int height )
    {
        GLES20.glViewport( 0, 0, width, height );

        float ratio = (float) width / height;

        // Set the camera position (View matrix)
        Matrix.setLookAtM(viewMatrix, 0, 0, 0, 5f, 0, 0, 0.0f, 0, 1.0f, 0);

        // this projection matrix is applied to object coordinates
        // in the onDrawFrame() method
        //Matrix.frustumM( projectionMatrix, 0, - ratio, ratio, - 1, 1, 1, 10 );
        Matrix.orthoM(projectionMatrix, 0, -ratio, ratio, -1, 1, 1, 10);

        float[] mLightPosInWorldSpace = new float[4];
        float[] mLightPosInModelSpace = new float[]{0.0f, 0.0f, 0.0f, 1.0f};
        float[] mLightModelMatrix = new float[16];

        // Calculate position of the light. Push into the distance.
        Matrix.setIdentityM(mLightModelMatrix, 0);
        Matrix.translateM(mLightModelMatrix, 0, 0.0f, 0.0f, 3.0f);

        Matrix.multiplyMV(mLightPosInWorldSpace, 0, mLightModelMatrix, 0, mLightPosInModelSpace, 0);
        Matrix.multiplyMV(lightPosInEyeSpace, 0, viewMatrix, 0, mLightPosInWorldSpace, 0);
        //Matrix.multiplyMV(lightPosInEyeSpace, 0, projectionMatrix, 0, lightPosInEyeSpace, 0);

        fan.initializeMatrices(viewMatrix, projectionMatrix, lightPosInEyeSpace);
        fan.getWind().initializeMatrices(viewMatrix, projectionMatrix, lightPosInEyeSpace);
        triangle.initializeMatrices(viewMatrix, projectionMatrix, lightPosInEyeSpace);
        //line.setProjectionMatrix( projectionMatrix );
        //line.initializeMatrices();
        dispenser.initializeMatrices(viewMatrix, projectionMatrix, lightPosInEyeSpace);
        background.initializeMatrices(viewMatrix, projectionMatrix, lightPosInEyeSpace);
    }

    /**
     * Called to draw the current frame.
     * <p/>
     * This method is responsible for drawing the current frame.
     * <p/>
     * The implementation of this method typically looks like this:
     * <pre class="prettyprint">
     * void onDrawFrame(GL10 gl) {
     * gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
     * //... other gl calls to render the scene ...
     * }
     * </pre>
     *
     * @param gl the GL interface. Use <code>instanceof</code> to
     *           test if the interface supports GL11 or higher interfaces.
     */
    @Override
    public void onDrawFrame( GL10 gl )
    {
        // Redraw background color
        GLES20.glClear( GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT );
        background.draw();

//        fan.getWind().draw();
        fan.draw();
//        fan.drawLight();
        dispenser.updatePosition(0, 0);
        dispenser.draw();
        //triangle.enableGraphics();
        //fan.getWind().calculateWindForce();
        Physics.calculateWindForce(fan.getWind(), triangle);

        //Log.e( "blowme", "min bounds " + fan.getWind().getBounds().getyCorners()[0] + " max
        // bounds " +
        //        fan.getWind().getBounds().getyCorners()[1] );

        if( Physics.isCollision( fan.getWind().getBounds(), triangle.getBounds() ) )
        {
            triangle.updatePosition( fan.getWind().getxForce(), fan.getWind().getyForce() );
            //Log.e( "blowme", "True" );
            //Log.e( "blowme", "wind bounds " + triangle.getBounds().getyMin() + " triangle
            // bounds " + triangle.getBounds().getyMax() );
        }
        else
        {
            triangle.updatePosition( 0, 0 );
            //Log.e( "blowme", "False" );
        }
        if( triangle.isOffscreen() )
        {
            Log.e( "blowme", "offscreen" );
            triangle = new FallingObject();
            triangle.enableGraphics();
            triangle.initializeMatrices(viewMatrix, projectionMatrix, lightPosInEyeSpace);
        }
        //triangle.drawLight();

        //triangle.updatePosition( 0, 0 );

        triangle.draw();

        //line.updatePosition( fan.deltaX, fan.deltaY );
        //line.draw();
    }

    public static int loadShader( int type, String shaderCode )
    {
        // create a vertex vertexshader type (GLES20.GL_VERTEX_SHADER)
        // or a fragment vertexshader type (GLES20.GL_FRAGMENT_SHADER)
        int shader = GLES20.glCreateShader( type );

        // add the source code to the vertexshader and compile it
        GLES20.glShaderSource( shader, shaderCode );
        GLES20.glCompileShader( shader );

        return shader;
    }

    public void pauseGame()
    {
        paused = true;
        fan.pauseGame();
        triangle.pauseGame();
        dispenser.pauseGame();
        fan.getWind().pauseGame();
    }

    public void resumeGame()
    {
        if (paused)
        {
            paused = false;
            fan.resumeGame();
            triangle.resumeGame();
            dispenser.resumeGame();
            fan.getWind().resumeGame();
        }
    }


    public boolean isPaused()
    {
        return paused;
    }


    private Fan fan;

    private FallingObject triangle;

    private Dispenser dispenser;

    private Background background;

    private boolean paused;

    // mMVPMatrix is an abbreviation for "Model View Projection Matrix"
    private float[] projectionMatrix = new float[16];

    private float[] viewMatrix = new float[16];

    private float[] lightPosInEyeSpace = new float[16];
}
