package wesley.folz.blowme.graphics.models;

import android.opengl.Matrix;

import wesley.folz.blowme.util.GraphicsUtilities;

/**
 * Created by Wesley on 10/15/2017.
 */

public class LauncherTube extends RicochetObstacle {
    public LauncherTube(float x, float y) {
        super(x, y);
        scaleFactor = 0.0225f;
        xDirection = 0;//xPos / Math.abs(xPos);
        yDirection = yPos / Math.abs(yPos);
    }

    @Override
    public void enableGraphics(GraphicsUtilities graphicsData) {
        //get dataVBO, orderVBO, program, texture handles
        dataVBO = graphicsData.modelVBOMap.get("launcher_tube");
        orderVBO = graphicsData.orderVBOMap.get("launcher_tube");
        numVertices = graphicsData.numVerticesMap.get("launcher_tube");
        programHandle = graphicsData.shaderProgramIdMap.get("texture");
        textureDataHandle = graphicsData.textureIdMap.get("launcher_tube_tex");
    }

    @Override
    public void initializeMatrices(float[] viewMatrix, float[] projectionMatrix,
            float[] lightPositionInEyeSpace) {
        super.initializeMatrices(viewMatrix, projectionMatrix, lightPositionInEyeSpace);
        if (!this.resuming) {
            //rotate 130 degrees about x-axis
            Matrix.rotateM(modelMatrix, 0, 90, 0, 1, 0);
            //Matrix.rotateM(modelMatrix, 0, 10, 0, 0, 1);
        }
    }

    @Override
    public float[] createTransformationMatrix() {
        float[] rotation = new float[16];

        Matrix.setIdentityM(rotation, 0);

        Matrix.translateM(modelMatrix, 0, 0, getDeltaY(), 0); //translate missile

        Matrix.multiplyMM(rotation, 0, modelMatrix, 0, rotation, 0); //spin missile

        float rotationAngle = 0;//-90.0f;
        if (yDirection != 0) {
            rotationAngle = 90.0f + (float) (180.0 * Math.atan2(yDirection, xDirection) / Math.PI);
        }

        Matrix.rotateM(rotation, 0, rotationAngle, 1, 0, 0);

        return rotation;
    }

    @Override
    public void updatePosition(float x, float y) {
        super.updatePosition(x, y);

        if (previousTime == 0) {
            previousTime = System.nanoTime();
        }

        long time = System.nanoTime();
        float deltaTime = (time - previousTime) / 1000000000.0f;
        previousTime = time;//System.nanoTime();

        if (Math.abs(xDirection) >= 0.5) {
            xMotion *= -1;
        }
        xDirection += xMotion * 0.01;//deltaTime;
        if (Math.abs(yDirection) >= 0.5) {
            yMotion *= -1;
        }
        yDirection += yMotion * 0.01;//deltaTime;
    }

    public float getxDirection() {
        return xDirection;
    }

    public float getyDirection() {
        return yDirection;
    }

    private float xDirection;
    private float yDirection;
    private float xMotion = 1;
    private float yMotion = 1;

    private long previousTime;

}
