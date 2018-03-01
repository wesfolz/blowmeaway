package wesley.folz.blowme.graphics.models;

import android.opengl.Matrix;

import wesley.folz.blowme.util.GraphicsUtilities;

/**
 * Created by Wesley on 10/21/2016.
 */

public class OrbitingObject extends Model
{
    public OrbitingObject(String modelType, float x, float y, float angle)
    {
        super();

        this.type = modelType;

        xPos = x;

        yPos = y;

        zPos = 0;

        initialAngle = angle;

        scaleFactor = 0.015f;

        initialXPos = xPos;
        initialYPos = yPos;

        prevUpdateTime = System.nanoTime();

        //this.orthographicProjection = true;
    }

    @Override
    public void enableGraphics(GraphicsUtilities graphicsData)
    {
        //get dataVBO, orderVBO, program, texture handles
        dataVBO = graphicsData.modelVBOMap.get(type);
        orderVBO = graphicsData.orderVBOMap.get(type);
        numVertices = graphicsData.numVerticesMap.get(type);
        programHandle = graphicsData.shaderProgramIdMap.get("texture");
        textureDataHandle = graphicsData.textureIdMap.get("cube_wood");
    }

    @Override
    public float[] createTransformationMatrix()
    {
        float[] mvp = new float[16];
        Matrix.setIdentityM(mvp, 0);

        //translate model matrix to new position
        //Matrix.translateM(modelMatrix, 0, deltaX, 0, 0);

        //copy modelMatrix to separate matrix for return, (returning modelMatrix doesn't work)
        //Matrix.translateM(modelMatrix, 0, deltaX, deltaY, deltaZ);
        Matrix.multiplyMM(mvp, 0, modelMatrix, 0, mvp, 0);
        Matrix.translateM(mvp, 0, deltaX, deltaY, deltaZ);

        //Matrix.rotateM(mvp, 0, angle, 0, -1, 0);

        return mvp;
    }

    @Override
    public void initializeMatrices(float[] viewMatrix, float[] perspectiveMatrix,
            float[] orthographicMatrix, float[] lightPosInEyeSpace)
    {
        super.initializeMatrices(viewMatrix, perspectiveMatrix, orthographicMatrix,
                lightPosInEyeSpace);
        if (!this.resuming)
        {
            //rotate 130 degrees about x-axis
            Matrix.translateM(modelMatrix, 0, 0, 0, initialZPos);
        }

        //Log.e( "blowme", "xpos: " + xPos + " ypos " + yPos );
    }

    @Override
    public void updatePosition(float x, float y)
    {
        long time = System.nanoTime();// % 4000L;
        float angle =
                (float) (Math.PI / 180.0f) * (time - prevUpdateTime) / 10000000.0f;
        ; //convert to radians
        prevUpdateTime = time;

        float newX = 0.1f * (float) Math.cos(parametricAngle + initialAngle);
        float newZ = 0.1f * (float) Math.sin(parametricAngle + initialAngle);

        parametricAngle += angle;

        deltaX = newX;// - xPos;
        deltaZ = newZ;// - zPos;

        xPos = newX;
        zPos = newZ;

        deltaY = y;// - yPos;
        yPos = y;
    }

    private float parametricAngle = 0;

    private float initialAngle;

    private String type;
}
