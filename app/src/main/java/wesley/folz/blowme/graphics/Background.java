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
                0.0f, 1.0f,            //texture
                -1.0f, -1.0f, -1.0f,  // bottom left
                0.0f, 0.0f, 1.0f,       //normal
                0.53f, 0.81f, 0.98f, 0.8f, //color
                0.0f, 0.0f,           //texture
                1.0f, -1.0f, -1.0f,    // bottom right
                0.0f, 0.0f, 1.0f,       //normal
                0.53f, 0.81f, 0.98f, 0.8f, //color
                1.0f, 0.0f,                //texture
                1.0f, 1.0f, -1.0f,      // top right
                0.0f, 0.0f, 1.0f,      //normal
                0.53f, 0.81f, 0.98f, 0.8f,//color
                1.0f, 1.0f              //texture
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
        float[] identity = new float[16];

        Matrix.setIdentityM(identity, 0);

        return identity;
    }

    @Override
    public void updatePosition(float x, float y)
    {

    }
}
