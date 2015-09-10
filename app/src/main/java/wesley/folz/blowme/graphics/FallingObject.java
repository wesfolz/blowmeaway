package wesley.folz.blowme.graphics;

import android.opengl.Matrix;

import wesley.folz.blowme.util.Bounds;
import wesley.folz.blowme.util.Physics;

/**
 * Created by wesley on 7/3/2015.
 */
public class FallingObject extends Model
{
    public FallingObject()
    {
        setBounds( new Bounds() );
        vertexData = new float[]{
                0.0f, 0.2f, 0.0f,   // top
                - 0.1f, 0.0f, 0.0f,   // bottom left
                0.1f, 0.0f, 0.0f}; // top right

        vertexOrder = new short[]{0, 1, 2};

        xPos = 0;

        yPos = - 0.6f;

        xVelocity = 0;

        yVelocity = 0;//0.1f;

        previousTime = System.nanoTime();
    }

    @Override
    public float[] createTransformationMatrix()
    {
        float[] transformation = new float[16];

        float[] result = new float[16];

        //updatePosition( 0, 0 );

        //Matrix.setIdentityM( transformation, 0 );

        //Matrix.translateM( transformation, 0, deltaX, deltaY, 0 );

        //Matrix.multiplyMM( result, 0, mvpMatrix, 0, transformation, 0 );

        Matrix.translateM( mvpMatrix, 0, deltaX, - deltaY, 0 );

        return mvpMatrix;
    }

    public float getDeltaX()
    {
        return deltaX;
    }

    public float getDeltaY()
    {
        return deltaY;
    }


    @Override
    public void initializeMatrix()
    {
        super.initializeMatrix();
        Matrix.translateM( mvpMatrix, 0, 0, 0.6f, 0 );

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

        yVelocity += force[1] * fallingTime;

        deltaX = fallingTime * xVelocity;

        deltaY = fallingTime * yVelocity;


        xPos += deltaX;
        yPos += deltaY;


        getBounds().setBounds( xPos - 0.1f, yPos - 0.1f, xPos + 0.1f, yPos + 0.1f );

        //  Log.e( "blowme", "xpos " + xPos + " ypos " + yPos );

    }

    private long previousTime;


    private float deltaX;

    private float deltaY;

    private float xVelocity;

    private float yVelocity;

    private static final float MASS = 2.0f;
}
