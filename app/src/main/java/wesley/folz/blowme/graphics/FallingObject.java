package wesley.folz.blowme.graphics;

import android.opengl.Matrix;
import android.os.Looper;
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
        super();
        setBounds(new Bounds());

        this.OBJ_FILE_RESOURCE = R.raw.cube;
        this.VERTEX_SHADER = R.raw.fan_vertex_shader;
        this.FRAGMENT_SHADER = R.raw.fan_fragment_shader;
        GraphicsReader.readShader(this);
        GraphicsReader.readOBJFile(this);

        xPos = (float) (Math.random() - 0.5);

        if( xPos > 0.35 )
            xPos = 0.35f;
        if( xPos <= - 0.35 )
            xPos = - 0.35f;

        yPos = -0.85f;//- 1.0f;

        xVelocity = 0;

        yVelocity = 0;//0.1f;

        previousTime = System.nanoTime();

        scaleFactor = 0.05f;

        initialXPos = xPos;
        initialYPos = -yPos;

        Log.e("blowme", "new triangle");
    }

    @Override
    public float[] createTransformationMatrix()
    {
        float[] transformation = new float[16];

        float[] result = new float[16];

        float[] mvp = new float[16];
        long time = SystemClock.uptimeMillis();// % 4000L;
        float angle = 0.1f * ((int) time);

        //updatePosition( 0, 0 );

        //Matrix.setIdentityM( transformation, 0 );

        //Matrix.translateM( transformation, 0, deltaX, deltaY, 0 );

        //Matrix.multiplyMM( result, 0, mvpMatrix, 0, transformation, 0 );

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
        return this.getBounds().getyTop() > Border.YBOTTOM /*|| this.getBounds().getYCorners()
               [1] < Border.YTOP */;
    }

    @Override
    public void resumeGame()
    {
        super.resumeGame();
        previousTime = System.nanoTime();
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

        //reflection of falling object off of side border simply changes sign of xVelocity
        if( Physics.isBorderCollision( this.getBounds() ) )
        {
            xVelocity = - xVelocity;
        }

        //reflection of falling object off of top border simply changes sign of yVelocity
        if (Physics.isTopBorderCollision(this.getBounds()))
        {
            yVelocity = -yVelocity;
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

    private long previousTime;


    private float deltaX;

    private float deltaY;

    private float xVelocity;

    private float yVelocity;

    private static final float MASS = 2.0f;
}
