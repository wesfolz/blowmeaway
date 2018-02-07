package wesley.folz.blowme.graphics;

import android.opengl.Matrix;

import wesley.folz.blowme.graphics.models.Model;
import wesley.folz.blowme.util.GraphicsUtilities;
import wesley.folz.blowme.util.Physics;

/**
 * Created by Wesley on 9/10/2016.
 */
public class Background extends Model
{
    public Background()
    {
        super();
        float corner = 1.0f;
        float length = 1.0f;
        interleavedData = new float[]{
                -corner, length * corner, -1.0f,    // top left
                0.0f, 0.0f,            //texture
                0.0f, 0.0f, 1.0f,      //normal
                -corner, -length * corner, -1.0f,  // bottom left
                0.0f, 1.0f,           //texture
                0.0f, 0.0f, 1.0f,       //normal
                corner, -length * corner, -1.0f,    // bottom right
                1.0f, 1.0f,                //texture
                0.0f, 0.0f, 1.0f,       //normal
                corner, length * corner, -1.0f,      // top right
                1.0f, 0.0f,              //texture
                0.0f, 0.0f, 1.0f      //normal
        };

        vertexOrder = new short[]{0, 1, 2, 2, 3, 0};
    }

    @Override
    public void enableGraphics(GraphicsUtilities graphicsData)
    {
        super.enableGraphics(graphicsData);
        programHandle = graphicsData.shaderProgramIdMap.get("texture");
        textureDataHandle = graphicsData.textureIdMap.get("planet");
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
    public boolean initializationRoutine()
    {
        updatePosition(0, 0);
        return true;
    }

    @Override
    public void updatePosition(float x, float y)
    {
        Physics.rise(this);

        if (yPos >= 2)
        {
            yPos = 0;
            deltaY = -2;
        }
    }

    public float getDeltaY() {
        return deltaY;
    }

    private static final float RISING_SPEED = 0.1f;
}