package wesley.folz.blowme.graphics.models;

import android.opengl.Matrix;
import android.os.SystemClock;

import wesley.folz.blowme.graphics.effects.MissileTrail;
import wesley.folz.blowme.util.Bounds;
import wesley.folz.blowme.util.GraphicsUtilities;

/**
 * Created by Wesley on 7/1/2017.
 */

public class Missile extends Model {

    public Missile(float x, float y) {
        super();
        scaleFactor = 0.0225f;
        xPos = x;
        yPos = y;

        initialXPos = xPos;
        initialYPos = yPos;

        xDirection = xPos / Math.abs(xPos);

        setBounds(new Bounds());

        getBounds().setBounds(xPos - 2.5f * scaleFactor, yPos - scaleFactor,
                xPos + 2.5f * scaleFactor,
                yPos + scaleFactor);

        trail = new MissileTrail(xPos, yPos, xPos - getBounds().getxLeft());
        //trail = new MissileTrail(getBounds().getxLeft(), yPos);
        //trail = new MissileTrail(getBounds().getxRight(), yPos);
    }


    @Override
    public void enableGraphics(GraphicsUtilities graphicsData) {
        //get dataVBO, orderVBO, program, texture handles
        dataVBO = graphicsData.modelVBOMap.get("missile");
        orderVBO = graphicsData.orderVBOMap.get("missile");
        numVertices = graphicsData.numVerticesMap.get("missile");
        programHandle = graphicsData.shaderProgramIdMap.get("texture");
        textureDataHandle = graphicsData.textureIdMap.get("missile_tex");
        trail.enableGraphics(graphicsData);
    }

    @Override
    public void initializeMatrices(float[] viewMatrix, float[] projectionMatrix,
            float[] lightPosInEyeSpace) {
        super.initializeMatrices(viewMatrix, projectionMatrix, lightPosInEyeSpace);
        trail.initializeMatrices(viewMatrix, projectionMatrix, lightPosInEyeSpace);
    }

    @Override
    public void draw() {
        super.draw();
        trail.draw();
    }

    @Override
    public void pauseGame() {
        super.pauseGame();
        trail.pauseGame();
    }

    @Override
    public void resumeGame() {
        super.resumeGame();
        trail.resumeGame();
        previousTime = System.nanoTime();
    }

    @Override
    public float[] createTransformationMatrix() {
        float[] rotation = new float[16];

        Matrix.setIdentityM(rotation, 0);

        // Create a rotation transformation for the triangle
        long time = SystemClock.uptimeMillis() % 36000L; //modulo makes rotation look smooth
        float angle = 0.2f * (float) time;

        Matrix.translateM(modelMatrix, 0, deltaX, deltaY, 0);

        Matrix.multiplyMM(rotation, 0, modelMatrix, 0, rotation, 0);

        float rotationAngle = -90.0f;
        if (yDirection != 0) {
            rotationAngle = 90.0f + (float) (180.0 * Math.atan2(yDirection, xDirection) / Math.PI);
        }

        trail.setRotationMatrix(rotationAngle);

        Matrix.rotateM(rotation, 0, rotationAngle, 0, 0, 1);

        //getBounds().calculateBounds(rotation);

        Matrix.rotateM(rotation, 0, angle, 0, -1, 0);

        return rotation;
    }

    @Override
    public void updatePosition(float x, float y) {

        if (firstUpdate) {
            previousTime = System.nanoTime();
            firstUpdate = false;
        }
        long time = System.nanoTime();
        float deltaTime = (time - previousTime) / 1000000000.0f;
        previousTime = time;//System.nanoTime();
        float displacementTime = INITIAL_VELOCITY * deltaTime;

        xDirection -= x;
        yDirection -= y;

        xVelocity = -INITIAL_VELOCITY * xDirection * displacementTime;
        yVelocity = -INITIAL_VELOCITY * yDirection * displacementTime;

        deltaX = displacementTime * xVelocity;
        deltaY = displacementTime * yVelocity;

        xPos += deltaX;
        yPos += deltaY;

        getBounds().setBounds(xPos - 4 * scaleFactor, yPos - scaleFactor, xPos + 4 * scaleFactor,
                yPos + scaleFactor);

        trail.updatePosition(deltaX, deltaY);
    }

    private float deltaX;
    private float deltaY;

    private float xVelocity;
    private float yVelocity;

    private float xDirection = 1.0f;

    private float yDirection = 0.0f;

    private float previousTime;

    private boolean firstUpdate = true;

    private static final float INITIAL_VELOCITY = 0.85f;

    private MissileTrail trail;
}
