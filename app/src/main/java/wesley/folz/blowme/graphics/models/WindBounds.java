package wesley.folz.blowme.graphics.models;

import android.opengl.Matrix;

import wesley.folz.blowme.util.Bounds;
import wesley.folz.blowme.util.GraphicsUtilities;

/**
 * Created by Wesley on 1/1/2017.
 */

//TODO: This is a test class, get rid of it eventually

public class WindBounds extends Model {

    public WindBounds(Bounds bnds) {
        super();

        interleavedData = new float[]{
                -1.0f, 1.0f, 0.0f,    // top left
                0.0f, 0.0f,            //texture
                0.0f, 0.0f, 1.0f,      //normal
                -1.0f, -3.0f, 0.0f,  // bottom left
                0.0f, 1.0f,           //texture
                0.0f, 0.0f, 1.0f,       //normal
                1.0f, -3.0f, 0.0f,    // bottom right
                1.0f, 1.0f,                //texture
                0.0f, 0.0f, 1.0f,       //normal
                1.0f, 1.0f, 0.0f,      // top right
                1.0f, 0.0f,              //texture
                0.0f, 0.0f, 1.0f      //normal
        };

        interleavedData[0] = bnds.getTopLeft().x;
        interleavedData[1] = bnds.getTopLeft().y;

        interleavedData[8] = bnds.getBottomLeft().x;
        interleavedData[9] = bnds.getBottomLeft().y;

        interleavedData[16] = bnds.getBottomRight().x;
        interleavedData[17] = bnds.getBottomRight().y;

        interleavedData[24] = bnds.getTopRight().x;
        interleavedData[25] = bnds.getTopRight().y;

        vertexOrder = new short[]{0, 1, 2, 2, 3, 0};
    }

    @Override
    public void enableGraphics(GraphicsUtilities graphicsData) {
        super.enableGraphics(graphicsData);
        programHandle = graphicsData.shaderProgramIdMap.get("texture");
        textureDataHandle = graphicsData.textureIdMap.get("sun");
    }

    @Override
    public float[] createTransformationMatrix() {
        float[] mvp = new float[16];
        Matrix.setIdentityM(mvp, 0);

        //translate model matrix to new position
        //Matrix.translateM(modelMatrix, 0, 0, deltaY, 0);

        //copy modelMatrix to separate matrix for return, (returning modelMatrix doesn't work)
        Matrix.multiplyMM(mvp, 0, modelMatrix, 0, mvp, 0);

        return mvp;
    }

    @Override
    public boolean initializationRoutine() {
        updatePosition(0, 0);
        return true;
    }

    @Override
    public void updatePosition(float x, float y) {
        interleavedData[0] = bnds.getTopLeft().x;
        interleavedData[1] = bnds.getTopLeft().y;

        interleavedData[8] = bnds.getBottomLeft().x;
        interleavedData[9] = bnds.getBottomLeft().y;

        interleavedData[16] = bnds.getBottomRight().x;
        interleavedData[17] = bnds.getBottomRight().y;

        interleavedData[24] = bnds.getTopRight().x;
        interleavedData[25] = bnds.getTopRight().y;
        super.enableGraphics(null);
        /*
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
        */
    }

    private long previousTime;
    private float deltaY;

    public Bounds bnds = new Bounds();

}
