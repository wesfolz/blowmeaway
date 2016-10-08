package wesley.folz.blowme.graphics.models;

import android.opengl.Matrix;

import wesley.folz.blowme.graphics.Border;
import wesley.folz.blowme.util.Bounds;
import wesley.folz.blowme.util.GraphicsUtilities;

/**
 * Created by Wesley on 9/24/2016.
 */

public class RicochetObstacle extends Model
{
    public RicochetObstacle(float x, float y)
    {
        super();
        xPos = x;//+.01f;
        yPos = y;//GamePlayActivity.Y_EDGE_POSITION;

        deltaY = 0;
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
        //Log.e("obstacle", "ypos " + yPos);

        getBounds().setBounds(xPos - scaleFactor, yPos - scaleFactor, xPos + scaleFactor, yPos + scaleFactor);
    }

    public boolean isOffscreen()
    {
        return this.getBounds().getyBottom() > Border.YTOP;
    }

    private float deltaY;

    private float deltaX;

    private long previousTime;

    private float risingSpeed = 0.1f;

}
