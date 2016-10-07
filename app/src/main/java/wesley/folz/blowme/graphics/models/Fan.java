package wesley.folz.blowme.graphics.models;

import android.opengl.Matrix;
import android.os.SystemClock;

import wesley.folz.blowme.R;
import wesley.folz.blowme.graphics.effects.Wind;
import wesley.folz.blowme.ui.GamePlayActivity;
import wesley.folz.blowme.util.Bounds;
import wesley.folz.blowme.util.GraphicsReader;

/**
 * Created by wesley on 5/11/2015.
 */
public class Fan extends Model
{
    public Fan()
    {
        super();
        this.OBJ_FILE_RESOURCE = R.raw.fan;
        this.VERTEX_SHADER = R.raw.texture_vertex_shader;
        this.FRAGMENT_SHADER = R.raw.texture_fragment_shader;
        GraphicsReader.readOBJFile(this);
        GraphicsReader.readShader(this);
        xPos = -GamePlayActivity.X_EDGE_POSITION;//+.01f;
        yPos = 0;
        setWind(new Wind());
        setSize(new float[]{0.5f, 0.5f});
        setBounds(new Bounds(xPos - getSize()[0] / 2, yPos - getSize()[1] / 2, xPos + getSize()
                [0] / 2, yPos + getSize()[1] / 2));

        scaleFactor = 0.03f;

        initialXPos = xPos;
        initialYPos = yPos;
    }

    private float[] calculateInwardParametricRotation()
    {
        //if parametricAngle = Pi -> inwardRotation = 0
        //if parametricAngle = Pi/2 -> inwardRotation = Pi/2
        //if parametricAngle = 0 -> inwardRotation = Pi
        //if parametricAngle = 3Pi/4 -> inwardRotation = -Pi/2

        float inwardRotation = 180 * ((float) Math.PI - parametricAngle) / (float) Math.PI;

        float[] rotationMatrix = new float[16];
        Matrix.setRotateM(rotationMatrix, 0, inwardRotation, 0, 0, 1);
        return rotationMatrix;
    }

    @Override
    public float[] createTransformationMatrix()
    {
        float[] bladeRotation = new float[16];

        // Create a rotation transformation for the triangle
        long time = SystemClock.uptimeMillis();// % 4000L;
        float angle = 0.40f * ((int) time);

        if (deltaX != 0 || deltaY != 0)
        {
            moveParametric();
        }
        //don't use deltaX, otherwise it can be updated between the moveParametric
        //call and the translateM call
        Matrix.translateM(modelMatrix, 0, parametricX, -parametricY, 0);

        //Matrix.scaleM( translationMatrix, 0, 0.05f, 0.05f, 0.05f );
        //Log.e( "blowme", "DeltaX " + deltaX + "DeltaY " + deltaY );

        parametricX = 0;
        parametricY = 0;
        deltaX = 0;
        deltaY = 0;

        Matrix.multiplyMM(bladeRotation, 0, modelMatrix, 0, calculateInwardParametricRotation(), 0);

        //rotate -65 degrees about y-axis
        Matrix.rotateM(bladeRotation, 0, -65, 0, 1, 0);
        //rotate 90 degrees about x-axis
        Matrix.rotateM(bladeRotation, 0, 90, 1, 0, 0);


        //since fan is initially rotated 90 about x, translation occurs on Z instead of y
        //and rotation occurs about y instead of z
        Matrix.rotateM(bladeRotation, 0, angle, 0, -1, 0);

        // Combine the rotation matrix with the projection and camera view
        // Note that the mMVPMatrix factor *must be first* in order
        // for the matrix multiplication product to be correct.

        return bladeRotation;
    }

    //arc = deg*2*pi*r/2Pi
    //deg = arc*2Pi/
    private void moveParametric()
    {
        float arcLength = Math.abs(deltaY) + Math.abs(deltaX);

        float a = GamePlayActivity.X_EDGE_POSITION;
        float b = GamePlayActivity.Y_EDGE_POSITION;
        float newX;
        float newY;
        float slowdown = 0.85f;

        if (clockwise)
        {
            parametricAngle += arcLength / slowdown;
        }
        else
        {
            parametricAngle -= arcLength / slowdown;
        }

        newX = a * (float) Math.cos(parametricAngle);
        newY = b * (float) Math.sin(parametricAngle);

        parametricX = newX - xPos;
        parametricY = newY - yPos;
        xPos = newX;
        yPos = newY;

        getWind().xPos = xPos;
        getWind().yPos = yPos;

        getBounds().setBounds(xPos - getSize()[0] / 2, yPos - getSize()[1] / 2, xPos + getSize()
                [0] / 2, yPos + getSize()[1] / 2);
        //getWind().setBounds( new float[]{-0.4f, -0.25f, 0.4f, 0.25f} );
        getWind().getBounds().calculateBounds(calculateInwardParametricRotation());
    }

    /**
     * Calculates the change in x and y position
     \     *
     * @param x - new x position
     * @param y - new y position
     */
    @Override
    public void updatePosition(float x, float y)
    {
        deltaX = (x - initialX);
        deltaY = (y - initialY);

        //deltaY < 0 -> moving up, deltaY > 0 -> moving down
        //deltaX < 0 -> moving left, deltaX > 0 -> moving right

        //determine if finger is moving clockwise or counter-clockwise
        clockwise = (((initialX - 1.0f) * (y - 1.0f)) - ((initialY - 1.0f) * (x - 1.0f))) > 0;

        //update initial position
        initialX = x;
        initialY = y;

        //moveParametric();
    }

    public Wind getWind()
    {
        return wind;
    }

    public void setWind(Wind wind)
    {
        this.wind = wind;
    }

    public void setInitialY(float initialY)
    {
        this.initialY = initialY;
    }

    public void setInitialX(float initialX)
    {
        this.initialX = initialX;
    }

    private float deltaX;
    private float deltaY;

    private float parametricX;
    private float parametricY;
    private float parametricAngle = (float) Math.PI;
    private float initialX;
    private float initialY;
    private Wind wind;
    private boolean clockwise;
}
