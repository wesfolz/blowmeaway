package wesley.folz.blowme.graphics.effects;

import android.opengl.Matrix;

import wesley.folz.blowme.R;
import wesley.folz.blowme.graphics.models.Model;
import wesley.folz.blowme.ui.GamePlayActivity;
import wesley.folz.blowme.util.Bounds;
import wesley.folz.blowme.util.GraphicsUtilities;

/**
 * Created by wesley on 7/3/2015.
 */
public class Wind extends Model
{
    public Wind()
    {
        super();
        interleavedData = new float[]{
                - 1.0f, 0.25f, 0.0f,    // top left
                 0.0f, 0.0f, 1.0f,      //normal
                 0.0f, 0.0f, 1.0f, 0.8f, //color
                - 1.0f, - 0.25f, 0.0f,  // bottom left
                0.0f, 0.0f, 1.0f,       //normal
                0.0f, 0.0f, 1.0f, 0.8f, //color
                1.0f, - 0.25f, 0.0f,    // bottom right
                0.0f, 0.0f, 1.0f,       //normal
                0.0f, 0.0f, 1.0f, 0.8f, //color
                1.0f, 0.25f, 0.0f,      // top right
                0.0f, 0.0f , 1.0f,      //normal
                0.0f, 0.0f, 1.0f, 0.8f};//color

        vertexOrder = new short[]{0, 1, 2, 0, 2, 3};


        //this.OBJ_FILE_RESOURCE = R.raw.cube;
        this.VERTEX_SHADER = R.raw.lighting_vertex_shader;
        this.FRAGMENT_SHADER = R.raw.lighting_fragment_shader;
        //GraphicsUtilities.readOBJFile(this);
        GraphicsUtilities.readShader(this);

        setSize( new float[]{2.0f, 0.5f} );
        setBounds( new Bounds( - 1.0f, - 0.25f, 1.0f, 0.25f ) );
        rotationMatrix = new float[16];
        Matrix.setIdentityM( rotationMatrix, 0 );
    }

    public float getxForce()
    {
        return xForce;
    }

    public void setxForce( float xForce )
    {
        this.xForce = xForce;
    }

    public float getyForce()
    {
        return yForce;
    }

    public void setyForce( float yForce )
    {
        this.yForce = yForce;
    }

    public float getMaxWindForce()
    {
        return maxWindForce;
    }

    public void setMaxWindForce(float maxWindForce)
    {
        this.maxWindForce = maxWindForce;
    }


    public void calculateWindForce()
    {
        xForce = (-1) * xPos * maxWindForce;
        yForce = (-1) * yPos * maxWindForce;
    }

    /**
     * TODO: Rotate fan so that it doesn't compete with blade rotation (make wind force a function of distance)
     * Rotate fan so that it's pointing towards the center of the screen
     *
     * @return - rotation matrix about z axis that points fan towards center of screen
     */
    private float[] calculateInwardRotation()
    {
        float[] rotationMatrix = new float[16];
        float inwardRotation;
        float cornerAngle = 45;//(float) (180 * Math.atan( GamePlayActivity.X_EDGE_POSITION /
        // GamePlayActivity.Y_EDGE_POSITION ) / Math
        //.PI);

        float xRatio = (90 - cornerAngle) / GamePlayActivity.X_EDGE_POSITION;
        float yRatio = cornerAngle / GamePlayActivity.Y_EDGE_POSITION;

        //on negative x edge
        if( xPos == (- GamePlayActivity.X_EDGE_POSITION) )
        {
            //inwardRotation = yRatio * ( yPos + (yPos/Math.abs(yPos))*(X_EDGE_POSITION + xPos) );
            inwardRotation = yRatio * yPos;
            //Log.e( "blowme", "negative x edge" );
        }
        //on positive x edge
        else if( xPos == GamePlayActivity.X_EDGE_POSITION )
        {
            inwardRotation = 180 - cornerAngle + yRatio * (GamePlayActivity.Y_EDGE_POSITION - yPos);
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

    @Override
    public float[] createTransformationMatrix()
    {
        float[] transformation = new float[16];

        Matrix.setIdentityM(modelMatrix, 0);

        Matrix.multiplyMM(transformation, 0, calculateInwardRotation(), 0, modelMatrix, 0);

        //Log.e( "blowme", "xmin " + transformation[0] + " ymin " + transformation[1] + " xmax "
        // + transformation[4] + " ymax " + transformation[5]);
        return transformation;
    }

    @Override
    public void updatePosition( float x, float y )
    {

    }

    public void setRotationMatrix( float[] rotationMatrix )
    {
        this.rotationMatrix = rotationMatrix;
    }

    private float xForce;

    private float yForce;

    private float maxWindForce = 1.8f;

    private float[] rotationMatrix;
}