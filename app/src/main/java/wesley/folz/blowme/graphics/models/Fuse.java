package wesley.folz.blowme.graphics.models;

import android.opengl.Matrix;

import wesley.folz.blowme.util.GraphicsUtilities;
import wesley.folz.blowme.util.Physics;

/**
 * Created by Wesley on 12/28/2017.
 */

public class Fuse extends RicochetObstacle {
    public Fuse(float x, float y) {
        super(x, y);
        scaleFactor = 0.0225f;
        yDirection = 0;//xPos / Math.abs(xPos);
        xDirection = xPos / Math.abs(xPos);
    }

    @Override
    public void enableGraphics(GraphicsUtilities graphicsData) {
        //get dataVBO, orderVBO, program, texture handles
        dataVBO = graphicsData.modelVBOMap.get("fuse");
        orderVBO = graphicsData.orderVBOMap.get("fuse");
        numVertices = graphicsData.numVerticesMap.get("fuse");
        programHandle = graphicsData.shaderProgramIdMap.get("lighting");
        //textureDataHandle = graphicsData.textureIdMap.get("launcher_tube_tex");
    }

    @Override
    public void initializeMatrices(float[] viewMatrix, float[] projectionMatrix,
            float[] lightPositionInEyeSpace) {
        super.initializeMatrices(viewMatrix, projectionMatrix, lightPositionInEyeSpace);
    }

    @Override
    public float[] createTransformationMatrix() {
        float[] rotation = new float[16];

        Matrix.setIdentityM(rotation, 0);

        Matrix.translateM(modelMatrix, 0, 0, getDeltaY(), 0); //translate missile

        Matrix.multiplyMM(rotation, 0, modelMatrix, 0, rotation, 0); //spin missile

        float rotationAngle = 0;//-90.0f;
        if (initialXPos > 0) {
            rotationAngle = 180.0f;
        }
        if (yDirection != 0) {
            rotationAngle = 180.0f - (float) (180.0 * Math.atan2(yDirection, xDirection) / Math.PI);
        }

        Matrix.rotateM(rotation, 0, rotationAngle, 0, 0, 1);

        if (initialXPos > 0) {
            Matrix.rotateM(rotation, 0, 180, 1, 0, 0);
        }

        return rotation;
    }

    @Override
    public void updatePosition(float x, float y) {
        //super.updatePosition(x, y);

        Physics.panUpDown(this, Physics.rise(this));
    }
}