package wesley.folz.blowme.graphics.models;

import android.opengl.Matrix;
import android.util.Log;

import wesley.folz.blowme.util.Bounds;
import wesley.folz.blowme.util.GraphicsUtilities;

/**
 * Created by Wesley on 9/24/2016.
 */

public class RicochetObstacle extends Model
{
    public RicochetObstacle()
    {
        super();
        xPos = 0;//+.01f;
        yPos = -1.5f;//GamePlayActivity.Y_EDGE_POSITION;


        previousTime = System.nanoTime();

        setBounds(new Bounds());

        scaleFactor = 0.1f;

        initialXPos = xPos;
        initialYPos = yPos;
    }

    @Override
    public void enableGraphics(GraphicsUtilities graphicsData)
    {
        //get dataVBO, orderVBO, program, texture handles
        dataVBO = graphicsData.modelVBOMap.get("cube");
        orderVBO = graphicsData.orderVBOMap.get("cube");
        numVertices = graphicsData.numVerticesMap.get("cube");
        programHandle = graphicsData.shaderProgramIdMap.get("lighting");
    }

    @Override
    public void resumeGame()
    {
        super.resumeGame();
        previousTime = System.nanoTime();
    }


    @Override
    public float[] createTransformationMatrix()
    {
        float[] mvp = new float[16];
        Matrix.setIdentityM(mvp, 0);

        Log.e("obstacle", "transform delta " + deltaY);

        //translate model matrix to new position
        Matrix.translateM(modelMatrix, 0, 0, deltaY, 0);

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
            //rotate 130 degrees about x-axis
            //Matrix.rotateM(modelMatrix, 0, 20, 1, 0, 0);
        }

        //Log.e( "blowme", "xpos: " + xPos + " ypos " + yPos );
    }

    @Override
    public void updatePosition(float x, float y)
    {
        long time = System.nanoTime();
        float deltaTime = (time - previousTime) / 1000000000.0f;
        previousTime = time;

        //deltaY = deltaTime * risingSpeed;
        deltaY = 0.002f;
        yPos += deltaY;
        Log.e("obstacle", "update delta " + deltaY);

        getBounds().setBounds(xPos - scaleFactor, yPos - scaleFactor, xPos + scaleFactor, yPos + scaleFactor);
    }

    private float deltaY;

    private float deltaX;

    private long previousTime;

    private float risingSpeed = 0.1f;

}
