package wesley.folz.blowme.graphics;

import android.opengl.Matrix;
import android.util.Log;

/**
 * Created by wesley on 7/3/2015.
 */
public class FallingObject extends Model
{
    public FallingObject()
    {
        vertexData = new float[]{
                0.0f, 0.2f, 0.0f,   // top
                - 0.1f, 0.0f, 0.0f,   // bottom left
                0.1f, 0.0f, 0.0f}; // top right

        vertexOrder = new short[]{0, 1, 2};

        xVelocity = 0;

        yVelocity = 0.1f;

        previousTime = System.nanoTime();
    }

    @Override
    public float[] createTransformationMatrix()
    {
        float[] transformation = new float[16];

        float[] result = new float[16];

        updatePosition( 0, 0 );

        //Matrix.setIdentityM( transformation, 0 );

        //Matrix.translateM( transformation, 0, deltaX, deltaY, 0 );

        //Matrix.multiplyMM( result, 0, mvpMatrix, 0, transformation, 0 );

        Matrix.translateM( mvpMatrix, 0, deltaX, - deltaY, 0 );

        return mvpMatrix;
    }

    /**
     * @param x - x component of wind force
     * @param y - y component of wind force
     */
    @Override
    public void updatePosition( float x, float y )
    {
        float time = (System.nanoTime() - previousTime) / 1000000000.0f;
        previousTime = System.nanoTime();
        float fallingTime = MASS * time;

        Log.e( "blowme", "Falling time: " + fallingTime );

        deltaX = fallingTime * xVelocity;

        deltaY = fallingTime * yVelocity;

    }

    private long previousTime;


    private float deltaX;

    private float deltaY;

    private float xVelocity;

    private float yVelocity;

    private static final float MASS = 2.0f;
}
