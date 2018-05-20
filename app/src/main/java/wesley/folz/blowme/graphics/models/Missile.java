package wesley.folz.blowme.graphics.models;

import android.opengl.Matrix;

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

        getBounds().setBounds(xPos - 2.0f * scaleFactor, yPos - scaleFactor,
                xPos + 2.0f * scaleFactor,
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
    public void initializeMatrices(float[] viewMatrix, float[] perspectiveMatrix,
            float[] orthographicMatrix, float[] lightPosInEyeSpace) {
        super.initializeMatrices(viewMatrix, perspectiveMatrix, orthographicMatrix,
                lightPosInEyeSpace);
        trail.initializeMatrices(viewMatrix, perspectiveMatrix, orthographicMatrix,
                lightPosInEyeSpace);
    }

    @Override
    public void draw() {
        if (!offscreen) {
            super.draw();
            if (flying) {
                trail.draw();
            }
        }
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
        if (prevUpdateTime != 0) {
            prevUpdateTime = System.nanoTime();
        }
    }

    @Override
    public float[] createTransformationMatrix() {
        float[] rotation = new float[16];

        Matrix.setIdentityM(rotation, 0);

        Matrix.translateM(modelMatrix, 0, deltaX, deltaY, 0); //translate missile

        Matrix.multiplyMM(rotation, 0, modelMatrix, 0, rotation, 0);

        float rotationAngle = 0;//-90.0f;
        if (initialXPos > 0) {
            rotationAngle = 180.0f;
        }
        if (yDirection != 0) {
            rotationAngle = 180.0f - (float) (180.0 * Math.atan2(yDirection, xDirection) / Math.PI);
        }

        trail.setRotationMatrix(rotationAngle);

        Matrix.rotateM(rotation, 0, rotationAngle, 0, 0, 1);

        if (initialXPos > 0) {
            Matrix.rotateM(rotation, 0, 180, 1, 0, 0);
        }

        Matrix.scaleM(rotation, 0, stretch[0], stretch[1], 1);

        return rotation;
    }

    @Override
    public void updatePosition(float x, float y) {

        if (prevUpdateTime == 0) {
            prevUpdateTime = System.nanoTime();
        }

        long time = System.nanoTime();
        float deltaTime = (time - prevUpdateTime) / 1000000000.0f;
        prevUpdateTime = time;//System.nanoTime();

        if (flying) {
            xDirection -= x * KINETIC_FRICTION;
            yDirection += y * KINETIC_FRICTION;
            float xVelocity = -INITIAL_VELOCITY * xDirection;
            float yVelocity = INITIAL_VELOCITY * yDirection;
            float velocity = (float) Math.sqrt(xVelocity * xVelocity + yVelocity * yVelocity);
            float acceleration =
                    INITIAL_ACCELERATION;//1aqMath.max(0, INITIAL_ACCELERATION -
            // KINETIC_FRICTION*velocity);
            deltaX =
                    (INITIAL_VELOCITY * deltaTime + 0.5f * acceleration * deltaTime * deltaTime) * (
                            xVelocity / velocity);
            deltaY =
                    (INITIAL_VELOCITY * deltaTime + 0.5f * acceleration * deltaTime * deltaTime) * (
                            yVelocity / velocity);
        } else { //if not flying just move up
            deltaX = x;
            deltaY = y;// deltaTime * RISING_SPEED;
            //Physics.panUpDown(this, deltaTime);
        }

        xPos += deltaX;
        yPos += deltaY;

        getBounds().setBounds(xPos - 4 * scaleFactor, yPos - scaleFactor, xPos + 4 * scaleFactor,
                yPos + scaleFactor);

        trail.updatePosition(deltaX, deltaY);
    }

    public void setxDirection(float xDirection) {
        this.xDirection = xDirection;
    }

    public void setyDirection(float yDirection) {
        this.yDirection = yDirection;
    }

    public boolean isFlying() {
        return flying;
    }


    @Override
    public void setDeltaX(float deltaX) {
        this.deltaX = deltaX;
        trail.setDeltaX(deltaX);
    }

    @Override
    public void setDeltaY(float deltaY) {
        this.deltaY = deltaY;
        trail.setDeltaY(deltaY);
    }

    @Override
    public void setxPos(float xPos) {
        this.xPos = xPos;
        trail.setxPos(xPos);
    }

    @Override
    public void setyPos(float yPos) {
        this.yPos = yPos;
        trail.setyPos(yPos);
    }

    public void setStretch(float[] stretch) {
        super.setStretch(stretch);
        trail.setStretch(stretch);
    }

    public void setFlying(boolean flying) {
        this.flying = flying;
    }

    private boolean flying = false;

    private static final float RISING_SPEED = 0.1f;
    private static final float INITIAL_VELOCITY = 0.1f;
    private static final float INITIAL_ACCELERATION = 80.0f;
    private static final float KINETIC_FRICTION = 0.1f;

    private MissileTrail trail;
}