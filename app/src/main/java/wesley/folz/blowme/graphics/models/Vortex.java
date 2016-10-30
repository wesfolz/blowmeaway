package wesley.folz.blowme.graphics.models;

import android.opengl.Matrix;
import android.os.SystemClock;

import wesley.folz.blowme.util.Bounds;
import wesley.folz.blowme.util.GraphicsUtilities;

/**
 * Created by Wesley on 9/11/2016.
 */
public class Vortex extends Model
{
    public Vortex(String collectionType, float x)
    {
        super();

        type = collectionType;
        setBounds(new Bounds());

        xPos = x;//+.01f;
        yPos = -1.0f;//0.935f;//GamePlayActivity.Y_EDGE_POSITION;

        scaleFactor = 0.04f;

        initialXPos = xPos;
        initialYPos = yPos;
        yPos = -0.65f;

        for (int i = 0; i < 3; i++)
        {
            orbitingObjects[i] = new OrbitingObject(type, xPos, -1.0f, (float) Math.PI + i * (float) Math.PI / 2);
        }
    }

    @Override
    public void enableGraphics(GraphicsUtilities graphicsData)
    {
        dataVBO = graphicsData.modelVBOMap.get("vortex");
        orderVBO = graphicsData.orderVBOMap.get("vortex");
        numVertices = graphicsData.numVerticesMap.get("vortex");
        programHandle = graphicsData.shaderProgramIdMap.get("lighting");
        for (OrbitingObject orbitingObject : orbitingObjects)
        {
            orbitingObject.enableGraphics(graphicsData);
        }
    }

    @Override
    public void draw()
    {
        super.draw();
        for (OrbitingObject orbitingObject : orbitingObjects)
        {
            orbitingObject.draw();
        }
    }

    @Override
    public float[] createTransformationMatrix()
    {
        float[] mvp = new float[16];

        long time = SystemClock.uptimeMillis();// % 4000L;
        float angle = 0.40f * ((int) time);

        Matrix.setIdentityM(mvp, 0);

        //translate model matrix to new position
        //Matrix.translateM(modelMatrix, 0, deltaX, 0, 0);

        //copy modelMatrix to separate matrix for return, (returning modelMatrix doesn't work)
        Matrix.multiplyMM(mvp, 0, modelMatrix, 0, mvp, 0);
        Matrix.scaleM(mvp, 0, 1.0f, scaleCount, 1.0f);
        Matrix.translateM(mvp, 0, 0.0f, deltaY, 0.0f);

        Matrix.rotateM(mvp, 0, angle, 0, -1, 0);

        return mvp;
    }

    @Override
    public void initializeMatrices(float[] viewMatrix, float[] projectionMatrix, float[] lightPositionInEyeSpace)
    {
        super.initializeMatrices(viewMatrix, projectionMatrix, lightPositionInEyeSpace);
        if (!this.resuming)
        {
            //rotate 130 degrees about x-axis
            Matrix.rotateM(modelMatrix, 0, 10, 1, 0, 0);
            Matrix.scaleM(modelMatrix, 0, 1.0f, 0.01f, 1.0f);
        }

        for (OrbitingObject orbitingObject : orbitingObjects)
        {
            orbitingObject.initializeMatrices(viewMatrix, projectionMatrix, lightPositionInEyeSpace);
        }

        //Log.e( "blowme", "xpos: " + xPos + " ypos " + yPos );
    }

    @Override
    public void updatePosition(float x, float y)
    {
        if (scaleCount < 100)
        {
            scaleCount++;
            deltaY = scaleCount / 500f;
        }
        for (OrbitingObject orbitingObject : orbitingObjects)
        {
            orbitingObject.updatePosition(0, deltaY);
        }
        getBounds().setBounds(xPos - 0.15f, yPos - 0.21f, xPos + 0.15f, yPos - 0.1f);
    }


    public boolean isCollecting()
    {
        return collecting;
    }

    public void setCollecting(boolean collecting)
    {
        this.collecting = collecting;
    }

    private float deltaY = 0;

    private float scaleCount = 1.0f;

    private boolean collecting = false;

    private OrbitingObject[] orbitingObjects = new OrbitingObject[3];

    private String type;

}
