package wesley.folz.blowme.graphics;

import android.opengl.Matrix;
import android.os.SystemClock;
import android.util.Log;

import wesley.folz.blowme.R;
import wesley.folz.blowme.util.Bounds;
import wesley.folz.blowme.util.GraphicsReader;
import wesley.folz.blowme.util.Physics;

/**
 * Created by wesley on 7/3/2015.
 */
public class FallingObject extends Model
{
    public FallingObject()
    {
        this(0);
    }

    public FallingObject(float dispenserX) {
        super();

        setBounds(new Bounds());

        this.OBJ_FILE_RESOURCE = R.raw.cube;
        this.VERTEX_SHADER = R.raw.texture_vertex_shader;
        this.FRAGMENT_SHADER = R.raw.texture_fragment_shader;
        GraphicsReader.readShader(this);
        GraphicsReader.readOBJFile(this);

        xPos = (float) (Math.random() - 0.5);

        if (xPos > 0.35)
            xPos = 0.35f;
        if (xPos <= -0.35)
            xPos = -0.35f;

        xPos = dispenserX;

        yPos = -1.0f;//- 1.0f;

        xVelocity = 0;

        yVelocity = 0.5f;//0;//0.1f;

        previousTime = System.nanoTime();

        scaleFactor = 0.03f;

        initialXPos = xPos;
        initialYPos = -yPos;

        Log.e("blowme", "new triangle");
    }


    @Override
    public float[] createTransformationMatrix()
    {
        float[] transformation = new float[16];
        firstCall = false;

        //float[] result = new float[16];

        long time = SystemClock.uptimeMillis();// % 4000L;
        float angle = 0.1f * ((int) time);
        long deltaT = time - prevRenderTime;

        //updatePosition( 0, 0 );

        //Matrix.setIdentityM( transformation, 0 );

        //Matrix.translateM( transformation, 0, deltaX, deltaY, 0 );

        //Matrix.multiplyMM( result, 0, mvpMatrix, 0, transformation, 0 );
        if (deltaT > 50) {
            Log.e("vortex", String.valueOf(vortexX) + " " + String.valueOf(vortexY));
            Matrix.scaleM(modelMatrix, 0, vortexX, vortexY, 1);
            prevRenderTime = time;
        }
        Matrix.translateM(modelMatrix, 0, deltaX, -deltaY, 0);
        Matrix.setRotateM(transformation, 0, angle, 1, 1, 1);
        Matrix.multiplyMM(transformation, 0, modelMatrix, 0, transformation, 0);

        return transformation;
    }

    public float getDeltaX()
    {
        return deltaX;
    }

    public float getDeltaY()
    {
        return deltaY;
    }

    public boolean isOffscreen()
    {
//        return false;
        if (firstCall || collectedCount >= 10) {
            return true;
        }
        return this.getBounds().getyTop() > Border.YBOTTOM /*|| this.getBounds().getYCorners()
               [1] < Border.YTOP */;
    }

    @Override
    public void resumeGame()
    {
        super.resumeGame();
        previousTime = System.nanoTime();
    }

    public void travelOnVector(float xComponent, float yComponent) {
        float time = (System.nanoTime() - previousTime) / 1000000000.0f;
        previousTime = System.nanoTime();
        float fallingTime = MASS * time;
        deltaX = 5 * fallingTime * xComponent;
        deltaY = 5 * fallingTime * yComponent;
        xPos += deltaX;
        yPos += deltaY;

        float normalizedX = Math.abs(xComponent / (Math.max(Math.abs(xComponent), Math.abs(yComponent))));

        float normalizedY = Math.abs(yComponent / (Math.max(Math.abs(xComponent), Math.abs(yComponent))));

        Log.e("normalized x", String.valueOf(normalizedX));
        Log.e("normalized y", String.valueOf(normalizedY));

        float stretchFactor = 0.5f * (normalizedY - normalizedX);
        Log.e("stretch factor", String.valueOf(stretchFactor));

        vortexX = 1.0f - stretchFactor;
        vortexY = 1.0f + stretchFactor;
        collectedCount++;
    }


    /**
     * TODO: make movement after wind collision look behave correctly
     *
     * @param x - x component of wind force
     * @param y - y component of wind force
     */
    @Override
    public void updatePosition( float x, float y )
    {
        float time = (System.nanoTime() - previousTime) / 1000000000.0f;
        previousTime = System.nanoTime();
        float fallingTime = MASS * time;

        float[] force = Physics.sumOfForces( x, y );

        xVelocity += force[0] * fallingTime;

        //reflection of falling object off of side border
        //if object collides of off right border, xVelocity must be negative
        if (this.getBounds().getxRight() >= Border.XRIGHT)
            xVelocity = -1 * Math.abs(xVelocity);
            //if object collides of off left border, xVelocity must be positive
        else if (this.getBounds().getxLeft() <= Border.XLEFT)
            xVelocity = Math.abs(xVelocity);

        //reflection of falling object off of top border, yVelocity must be positive
        if (Physics.isTopBorderCollision(this.getBounds()))
        {
            yVelocity = Math.abs(yVelocity);
        }

        yVelocity += force[1] * fallingTime;

        deltaX = fallingTime * xVelocity;

        deltaY = fallingTime * yVelocity;

        xPos += deltaX;
        yPos += deltaY;

        getBounds().setBounds(xPos - 0.1f, yPos - 0.1f, xPos + 0.1f, yPos + 0.1f);

        //Log.e("blowme", "xpos " + xPos + " ypos " + yPos);
        //Log.e("blowme", "ycorners0 " + getBounds().getYCorners()[0] + " ycorners1 " + getBounds().getYCorners()[1]);

    }

    private boolean firstCall = true;

    private int collectedCount = 0;

    private long previousTime;

    private long prevRenderTime;

    private float deltaX;

    private float deltaY;

    private float xVelocity;

    private float yVelocity;

    private static final float MASS = 2.0f;

    private float vortexX = 1.0f;

    private float vortexY = 1.0f;
}
