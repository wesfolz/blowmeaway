package wesley.folz.blowme.graphics.models;

import android.opengl.Matrix;

import wesley.folz.blowme.graphics.Border;
import wesley.folz.blowme.util.Bounds;
import wesley.folz.blowme.util.GraphicsUtilities;

/**
 * Created by Wesley on 9/25/2016.
 */

public class DestructiveObstacle extends Model
{
    public DestructiveObstacle(float x, float y)
    {
        super();
        xPos = x;//+.01f;
        yPos = y;//GamePlayActivity.Y_EDGE_POSITION;

        deltaY = 0;

        //previousTime = System.nanoTime();

        setBounds(new Bounds());

        scaleFactor = 0.025f;

        initialXPos = xPos;
        initialYPos = yPos;

        getBounds().setBounds(xPos - xRadius, yPos - 4 * scaleFactor, xPos + xRadius,
                yPos + 4 * scaleFactor);
    }

    @Override
    public void resumeGame()
    {
        super.resumeGame();
        previousTime = System.currentTimeMillis();
    }

    @Override
    public void enableGraphics(GraphicsUtilities graphicsData)
    {
        //get dataVBO, orderVBO, program, texture handles
        dataVBO = graphicsData.modelVBOMap.get("short_spikes");
        orderVBO = graphicsData.orderVBOMap.get("short_spikes");
        numVertices = graphicsData.numVerticesMap.get("short_spikes");
        programHandle = graphicsData.shaderProgramIdMap.get("lighting");
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

        Matrix.rotateM(mvp, 0, 90 * (Math.abs(xPos) / xPos), 0, 0, 1);
        Matrix.rotateM(mvp, 0, 10, 1, 0, 0);

        return mvp;
    }


    @Override
    public void updatePosition(float x, float y)
    {
        if (previousTime == 0)
        {
            previousTime = System.currentTimeMillis();
        }
        long time = System.currentTimeMillis();
        float deltaTime = (time - previousTime) / 10000.0f;

        previousTime = time;

        deltaY = deltaTime;// * risingSpeed;
        //deltaY = 0.002f;
        yPos += deltaY;

        getBounds().setBounds(xPos - xRadius, yPos - 4 * scaleFactor, xPos + xRadius, yPos + 4 * scaleFactor);
    }

    public boolean isOffscreen()
    {
        return this.getBounds().getyBottom() > Border.YTOP;
    }

    private float deltaY;

    private float deltaX;

    private long previousTime = 0;

    private float risingSpeed = 0.1f;

    private static final float xRadius = 0.06f;

}
