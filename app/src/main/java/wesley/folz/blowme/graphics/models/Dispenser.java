package wesley.folz.blowme.graphics.models;

import android.opengl.Matrix;
import android.util.Log;

import wesley.folz.blowme.graphics.Border;
import wesley.folz.blowme.ui.GamePlayActivity;
import wesley.folz.blowme.util.GraphicsUtilities;

/**
 * Created by wesley on 7/25/2015.
 */
public class Dispenser extends Model
{
    public Dispenser()
    {
        super();
        xPos = 0;//+.01f;
        yPos = 0.935f;//GamePlayActivity.Y_EDGE_POSITION;
        initialRotation = new float[16];
        motionMultiplier = 1;

        scaleFactor = 0.2f;

        initialXPos = xPos;
        initialYPos = yPos;
    }

    @Override
    public void enableGraphics(GraphicsUtilities graphicsData)
    {
        dataVBO = graphicsData.modelVBOMap.get("dispenser");
        orderVBO = graphicsData.orderVBOMap.get("dispenser");
        numVertices = graphicsData.numVerticesMap.get("dispenser");
        programHandle = graphicsData.shaderProgramIdMap.get("lighting");
    }

    @Override
    public float[] createTransformationMatrix()
    {
        float[] mvp = new float[16];

        Matrix.setIdentityM(mvp, 0);

        //translate model matrix to new position
        Matrix.translateM( modelMatrix, 0, deltaX, 0, 0 );

        //copy modelMatrix to separate matrix for return, (returning modelMatrix doesn't work)
        Matrix.multiplyMM(mvp, 0, modelMatrix, 0, mvp, 0);

        return mvp;
    }

    @Override
    public void initializeMatrices(float[] viewMatrix, float[] projectionMatrix, float[] lightPositionInEyeSpace)
    {
        super.initializeMatrices(viewMatrix, projectionMatrix, lightPositionInEyeSpace);

        if (!this.resuming)
        {
            //rotate 50 degrees about x-axis
            Matrix.rotateM(modelMatrix, 0, 10, 1, 0, 0);

            Matrix.scaleM(modelMatrix, 0, 1.0f, 0.5f, 1.0f);
        }

        //Log.e( "blowme", "xpos: " + xPos + " ypos " + yPos );
    }

    public float getDeltaX() {
        return deltaX;
    }


    public void setTargetX(float targetX) {
        this.targetX = targetX;
    }

    @Override
    public boolean initializationRoutine()
    {
        if (targetX >= Border.XRIGHT) {
            updatePosition(0, 0);
            return true;
        }
        // deltaX = (targetX - xPos)/100.0f;
        Log.e("json", "deltaX " + deltaX + " target x " + targetX + " xPos " + xPos);
        initializeCount++;
        if (initializeCount >= 100) {
            deltaX = 0;
            xPos = targetX;
            return true;
        }
        return true;
    }

    @Override
    public void updatePosition( float x, float y )
    {
        //long time = SystemClock.uptimeMillis()% 10000L;
        deltaX = 0.01f;//* ((int) time);

        if( xPos >= GamePlayActivity.X_EDGE_POSITION )
            motionMultiplier = - 1;
        if( xPos <= -GamePlayActivity.X_EDGE_POSITION )
            motionMultiplier = 1;
        deltaX *= motionMultiplier;
        xPos += deltaX;//*motionMultiplier;
        //Log.e( "blowme", "motionmultiplier " + motionMultiplier + " deltax " + deltaX + " xpos "
        //       + xPos );
    }

    private float deltaX;

    private int motionMultiplier;

    private float[] initialRotation;

    private float targetX = Border.XRIGHT;

    private int initializeCount = 0;
}
