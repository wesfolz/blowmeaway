package wesley.folz.blowme.graphics;

import android.opengl.Matrix;

import wesley.folz.blowme.graphics.models.Model;
import wesley.folz.blowme.util.GraphicsUtilities;

/**
 * Created by Wesley on 9/10/2016.
 */
public class Background extends Model
{

    public Background()
    {
        super();
        interleavedData = new float[]{
                -1.0f, 1.0f, -1.0f,    // top left
                0.0f, 0.0f, 1.0f,      //normal
                0.53f, 0.81f, 0.98f, 0.8f, //color
                0.0f, 0.0f,            //texture
                -1.0f, -3.0f, -1.0f,  // bottom left
                0.0f, 0.0f, 1.0f,       //normal
                0.53f, 0.81f, 0.98f, 0.8f, //color
                0.0f, 1.0f,           //texture
                1.0f, -3.0f, -1.0f,    // bottom right
                0.0f, 0.0f, 1.0f,       //normal
                0.53f, 0.81f, 0.98f, 0.8f, //color
                1.0f, 1.0f,                //texture
                1.0f, 1.0f, -1.0f,      // top right
                0.0f, 0.0f, 1.0f,      //normal
                0.53f, 0.81f, 0.98f, 0.8f,//color
                1.0f, 0.0f              //texture
        };

        vertexOrder = new short[]{0, 1, 2, 2, 3, 0};
    }

    @Override
    public void enableGraphics(GraphicsUtilities graphicsData)
    {
        super.enableGraphics(graphicsData);
        programHandle = graphicsData.shaderProgramIdMap.get("texture");
        textureDataHandle = graphicsData.textureIdMap.get("sky");
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
        long time = System.nanoTime();
        float deltaTime = (time - previousTime) / 1000000000.0f;
        previousTime = time;

        //deltaY = deltaTime * risingSpeed;
        deltaY = 0.002f;
        yPos += deltaY;

        if (yPos >= 2)
        {
            yPos = 0;
            deltaY = -2;
        }
    }

    private long previousTime;
    private float deltaY;
}
