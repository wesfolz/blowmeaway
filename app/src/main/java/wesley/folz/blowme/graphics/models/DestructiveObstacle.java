package wesley.folz.blowme.graphics.models;

import android.opengl.Matrix;

import wesley.folz.blowme.graphics.Border;
import wesley.folz.blowme.util.Bounds;
import wesley.folz.blowme.util.GraphicsUtilities;
import wesley.folz.blowme.util.Physics;

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

        //prevUpdateTime = System.nanoTime();

        setBounds(new Bounds());

        scaleFactor = 0.025f;

        initialXPos = xPos;
        initialYPos = yPos;

        getBounds().setBounds(xPos - xRadius, yPos - 4 * scaleFactor, xPos + xRadius,
                yPos + 4 * scaleFactor);
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
        Physics.rise(this);

        getBounds().setBounds(xPos - xRadius, yPos - 4 * scaleFactor, xPos + xRadius, yPos + 4 * scaleFactor);
    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }

    @Override
    public boolean isOffscreen()
    {
        return this.getBounds().getyBottom() > Border.YTOP || offscreen;
    }

    private int time = 0;

    private static final float RISING_SPEED = 0.1f;

    private static final float xRadius = 0.06f;

}