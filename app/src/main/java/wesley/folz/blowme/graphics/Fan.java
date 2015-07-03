package wesley.folz.blowme.graphics;

import android.opengl.Matrix;
import android.os.SystemClock;
import android.util.Log;

/**
 * Created by wesley on 5/11/2015.
 */
public class Fan extends Model
{
    public Fan()
    {
        super();
        xPos = - X_EDGE_POSITION;//+.01f;
        yPos = 0;
    }

    @Override
    public float[] createTransformationMatrix()
    {
        float[] mRotationMatrix = new float[16];

        float[] bladeRotation = new float[16];

        float[] first = new float[16];

        float[] inwardRotation = new float[16];

        //float[] translation = new float[16];

        //float[] translationMatrix = new float[16];

        // Create a rotation transformation for the triangle
        long time = SystemClock.uptimeMillis();// % 4000L;
        float angle = 0.40f * ((int) time);

        moveAlongEdge();

        Matrix.scaleM( mvpMatrix, 0, 20f, 20f, 20f );
        //Matrix.setIdentityM( translation, 0 );
        //Matrix.translateM( translation, 0, deltaX, - deltaY, 0 );
        //Matrix.multiplyMM( translationMatrix, 0, mvpMatrix, 0, translation, 0 );
        Matrix.translateM( mvpMatrix, 0, deltaX, - deltaY, 0 );
        Matrix.scaleM( mvpMatrix, 0, 0.05f, 0.05f, 0.05f );
        //Matrix.scaleM( translationMatrix, 0, 0.05f, 0.05f, 0.05f );
        //Log.e( "blowme", "DeltaX " + deltaX + "DeltaY " + deltaY );


        deltaY = 0;
        deltaX = 0;
/*
        Matrix.scaleM( mvpMatrix, 0, 20f, 20f, 20f );
        Matrix.setIdentityM( translation, 0 );
        Matrix.translateM( translation, 0, 0.4f * (float) Math.cos( ellipse ), 0.6f * (float)
        Math.sin
                ( ellipse ), 0 );
        Matrix.multiplyMM( translationMatrix, 0, mvpMatrix, 0, translation, 0 );
        Matrix.scaleM( mvpMatrix, 0, 0.05f, 0.05f, 0.05f );
        Matrix.scaleM( translationMatrix, 0, 0.05f, 0.05f, 0.05f );
*/

        //Matrix.multiplyMM( first, 0, translationMatrix, 0, initialRotation, 0 );

        Matrix.multiplyMM( inwardRotation, 0, mvpMatrix, 0, calculateInwardRotation(), 0 );
        Matrix.multiplyMM( first, 0, inwardRotation, 0, initialRotation, 0 );
        Matrix.multiplyMM( initialTransformationMatrix, 0, first, 0, secondRotation, 0 );


        //since fan is initially rotated 90 about x, translation occurs on Z instead of y
        //and rotation occurs about y instead of z
        Matrix.setRotateM( mRotationMatrix, 0, angle, 0, - 1, 0 );

        // Combine the rotation matrix with the projection and camera view
        // Note that the mMVPMatrix factor *must be first* in order
        // for the matrix multiplication product to be correct.
        Matrix.multiplyMM( bladeRotation, 0, initialTransformationMatrix, 0, mRotationMatrix, 0 );
        return bladeRotation;
    }

    @Override
    public void initializeMatrix()
    {
        super.initializeMatrix();
        //rotate -45 degrees about y-axis
        Matrix.setRotateM( initialRotation, 0, - 65, 0, 1, 0 );
        //rotate 90 degrees about x-axis
        Matrix.setRotateM( secondRotation, 0, 90, 1, 0, 0 );
        //Matrix.setIdentityM( secondRotation, 0 );
        //Matrix.scaleM( initialTransformationMatrix, 0, 0.1f, 0.1f, 0.1f );
        Matrix.translateM( mvpMatrix, 0, - X_EDGE_POSITION, 0, 0 );
        Matrix.scaleM( mvpMatrix, 0, 0.05f, 0.05f, 0.05f );

        Log.e( "blowme", "xpos: " + xPos + " ypos " + yPos );
    }

    /**
     * Move fan along edge of screen
     * TODO: Adjust x and y edge values to be consistent
     * TODO: round corners slightly for smoother motion
     */
    private void moveAlongEdge()
    {
        //x can't move, should be equal to the edge
        if( Math.abs( yPos ) < Y_EDGE_POSITION )
        {
            //move to edge if past it
            if( xPos > 0 )
            {
                deltaX = X_EDGE_POSITION - xPos;
                xPos = X_EDGE_POSITION;
            }
            else
            {
                deltaX = - 1 * X_EDGE_POSITION - xPos;
                xPos = - 1 * X_EDGE_POSITION;
            }
        }
        else
        {
            //move to edge if past it
            if( Math.abs( xPos ) > X_EDGE_POSITION )
            {
                if( xPos > 0 )
                {
                    deltaX = X_EDGE_POSITION - xPos;
                    xPos = X_EDGE_POSITION;
                }
                else
                {
                    deltaX = - 1 * X_EDGE_POSITION - xPos;
                    xPos = - 1 * X_EDGE_POSITION;
                }
            }
            else
            {
                xPos += deltaX;
            }
        }

        //y can't move should be equal to the edge
        if( Math.abs( xPos ) < X_EDGE_POSITION )
        {
            //move to edge if past it
            if( yPos > 0 )
            {
                deltaY = Y_EDGE_POSITION - yPos;
                yPos = Y_EDGE_POSITION;
            }
            else
            {
                deltaY = - 1 * Y_EDGE_POSITION - yPos;
                yPos = - 1 * Y_EDGE_POSITION;
            }
        }
        //update y position
        else
        {
            //move to edge if past it
            if( Math.abs( yPos ) > Y_EDGE_POSITION )
            {
                if( yPos > 0 )
                {
                    deltaY = Y_EDGE_POSITION - yPos;
                    yPos = Y_EDGE_POSITION;
                }
                else
                {
                    deltaY = - 1 * Y_EDGE_POSITION - yPos;
                    yPos = - 1 * Y_EDGE_POSITION;
                }
            }
            else
            {
                yPos += deltaY;
            }
        }
    }

    /**
     * TODO: Rotate fan so that it doesn't compete with blade rotation
     * Rotate fan so that it's pointing towards the center of the screen
     *
     * @return - rotation matrix about z axis that points fan towards center of screen
     */
    private float[] calculateInwardRotation()
    {
        float[] rotationMatrix = new float[16];
        float inwardRotation;
        float cornerAngle = (float) (180 * Math.atan( X_EDGE_POSITION / Y_EDGE_POSITION ) / Math
                .PI);

        float xRatio = (90 - cornerAngle) / X_EDGE_POSITION;
        float yRatio = cornerAngle / Y_EDGE_POSITION;

        //on negative x edge
        if( xPos == (- X_EDGE_POSITION) )
        {
            //inwardRotation = yRatio * ( yPos + (yPos/Math.abs(yPos))*(X_EDGE_POSITION + xPos) );
            inwardRotation = yRatio * yPos;
            //Log.e( "blowme", "negative x edge" );
        }
        //on positive x edge
        else if( xPos == X_EDGE_POSITION )
        {
            inwardRotation = 180 - cornerAngle + yRatio * (Y_EDGE_POSITION - yPos);
            //Log.e( "blowme", "negative x edge" );
        }
        //on y edge
        else
        {
            inwardRotation = (yPos / Math.abs( yPos )) * (xRatio * xPos + 90);
            //Log.e( "blowme", "negative y edge" );
        }

        Matrix.setRotateM( rotationMatrix, 0, inwardRotation, 0, 0, 1 );
        return rotationMatrix;
    }

    /**
     * Calculates the change in x and y position
     *
     * @param x - new x position
     * @param y - new y position
     */
    @Override
    public void updatePosition( float x, float y )
    {
        deltaX = x - initialX;
        deltaY = y - initialY;

        //update initial position
        initialX = x;
        initialY = y;
    }

    public void setInitialY( float initialY )
    {
        this.initialY = initialY;
    }

    public void setInitialX( float initialX )
    {
        this.initialX = initialX;
    }


    private final float[] initialTransformationMatrix = new float[16];

    private static final float X_EDGE_POSITION = 0.4f;

    private static final float Y_EDGE_POSITION = 0.6f;

    public float deltaX;

    public float deltaY;

    private float initialX;

    private float initialY;

    private final float[] secondRotation = new float[16];
    private final float[] initialRotation = new float[16];

}
