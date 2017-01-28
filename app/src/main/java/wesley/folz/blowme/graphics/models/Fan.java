package wesley.folz.blowme.graphics.models;

import android.opengl.Matrix;
import android.os.SystemClock;
import android.util.Log;

import wesley.folz.blowme.graphics.effects.Wind;
import wesley.folz.blowme.ui.GamePlayActivity;
import wesley.folz.blowme.util.Bounds;
import wesley.folz.blowme.util.GraphicsUtilities;

/**
 * Created by wesley on 5/11/2015.
 */
public class Fan extends Model
{
    public Fan(float targetX, float targetY, float targetYAngle, float targetZAngle)
    {
        super();
        xPos = -GamePlayActivity.X_EDGE_POSITION;//+.01f;
        yPos = 0;
        setWind(new Wind());
        setSize(new float[]{0.5f, 0.5f});
        setBounds(new Bounds(xPos - getSize()[0] / 2, yPos - getSize()[1] / 2, xPos + getSize()
                [0] / 2, yPos + getSize()[1] / 2));

        scaleFactor = 0.03f;
        yAngle = -65;

        //    scaleFactor = 0.1f;

        initialXPos = xPos;
        initialYPos = yPos;
        this.setTargets(targetX, targetY, targetYAngle, targetZAngle);

    }

    public Fan() {
        this(-GamePlayActivity.X_EDGE_POSITION, 0, -65, 0);

    }

    public void setTargets(float targetX, float targetY, float targetYAngle, float targetZAngle) {
        this.targetYAngle = targetYAngle;
        this.targetZAngle = targetZAngle;
        this.targetX = targetX;
        this.targetY = targetY;
    }

    @Override
    public void enableGraphics(GraphicsUtilities graphicsData)
    {
        dataVBO = graphicsData.modelVBOMap.get("fan");
        orderVBO = graphicsData.orderVBOMap.get("fan");
        numVertices = graphicsData.numVerticesMap.get("fan");
        programHandle = graphicsData.shaderProgramIdMap.get("texture");
        textureDataHandle = graphicsData.textureIdMap.get("fan_test");
        getWind().enableGraphics(graphicsData);
        getWind().wBounds.enableGraphics(graphicsData);
    }

    @Override
    public void initializeMatrices(float[] viewMatrix, float[] projectionMatrix, float[] lightPosInEyeSpace)
    {
        super.initializeMatrices(viewMatrix, projectionMatrix, lightPosInEyeSpace);
        getWind().initializeMatrices(viewMatrix, projectionMatrix, lightPosInEyeSpace);
        getWind().wBounds.initializeMatrices(viewMatrix, projectionMatrix, lightPosInEyeSpace);
    }

    @Override
    public boolean initializationRoutine() {
        if (initialized) {
            return true;
        }
        if (initialTime == 0) {
            prevTime = System.nanoTime();
            initialTime = System.currentTimeMillis();
            initRoutineX = xPos;
            initRoutineY = yPos;
            initRoutineZAngle = inwardRotation % 360;
            initRoutineYAngle = yAngle % 360;
            parametricX = 0;
            parametricY = 0;
        }
        long time = System.currentTimeMillis();
        float deltaTime = (time - initialTime) / 1000.0f;
        time = System.nanoTime();
        float littleDelta = (time - prevTime) / 1000000000.0f;
        prevTime = time;

        if (deltaTime < GamePlayActivity.INITIALIZATION_TIME) {
            parametricX = (this.targetX - initRoutineX) * (littleDelta
                    / GamePlayActivity.INITIALIZATION_TIME);
            parametricY = (this.targetY - initRoutineY) * (littleDelta
                    / GamePlayActivity.INITIALIZATION_TIME);

            //parametricX = (this.targetX - initRoutineX)*(deltaTime/GamePlayActivity
            // .INITIALIZATION_TIME);
            //parametricY = (this.targetY - initRoutineY)*(deltaTime/GamePlayActivity
            // .INITIALIZATION_TIME);
            xPos += parametricX;
            yPos += parametricY;
            yAngle = (this.targetYAngle - initRoutineYAngle) * (deltaTime
                    / GamePlayActivity.INITIALIZATION_TIME) + initRoutineYAngle;
            inwardRotation = (this.targetZAngle - initRoutineZAngle) * (deltaTime
                    / GamePlayActivity.INITIALIZATION_TIME) + initRoutineZAngle;
        } else {
            parametricX = 0;//this.targetX - xPos;// (Fan.TARGET_X - initRoutineX) - parametricX;
            parametricY = 0;//this.targetY - yPos;// (Fan.TARGET_Y - initRoutineY) - parametricY;
            inwardRotation = this.targetZAngle;
            yAngle = this.targetYAngle;
            xPos = this.targetX;
            yPos = this.targetY;
            initialTime = 0;
            parametricAngle = (float) Math.PI;
            fingerRotation = 0;
            initialized = true;

            Matrix.setIdentityM(modelMatrix, 0);
            Matrix.translateM(modelMatrix, 0, xPos, yPos, 0);
            Matrix.setIdentityM(getWind().modelMatrix, 0);
            Matrix.translateM(getWind().modelMatrix, 0, xPos, yPos, 0);
        }
        getWind().updatePosition(parametricX, parametricY);
        getWind().setRotationMatrix(inwardRotation);

        return deltaTime >= GamePlayActivity.INITIALIZATION_TIME;
    }

    @Override
    public void draw()
    {
        super.draw();
        if (isBlowing()) {
            getWind().draw();
        }
    }

    @Override
    public void pauseGame() {
        super.pauseGame();
        getWind().pauseGame();
    }


    @Override
    public void resumeGame() {
        super.resumeGame();
        getWind().resumeGame();
    }

    private void calculateInwardParametricRotation()
    {
        //if parametricAngle = Pi -> inwardRotation = 0
        //if parametricAngle = Pi/2 -> inwardRotation = Pi/2
        //if parametricAngle = 0 -> inwardRotation = Pi
        //if parametricAngle = 3Pi/4 -> inwardRotation = -Pi/2

        inwardRotation =
                180 * (parametricAngle - (float) Math.PI) / (float) Math.PI + fingerRotation;

        getWind().setRotationMatrix(inwardRotation);
    }

    @Override
    public float[] createTransformationMatrix()
    {
        float[] bladeRotation = new float[16];
        float[] rotationMatrix = new float[16];
        Matrix.setIdentityM(rotationMatrix, 0);
        Matrix.setRotateM(rotationMatrix, 0, inwardRotation, 0, 0, 1);

        // Create a rotation transformation for the triangle
        long time = SystemClock.uptimeMillis() % 36000L; //modulo makes rotation look smooth
        float angle = 0.6f * (float) time;

        getWind().updatePosition(parametricX, parametricY);

        //don't use deltaX, otherwise it can be updated between the moveParametric
        //call and the translateM call
        Matrix.translateM(modelMatrix, 0, parametricX, parametricY, 0);
//        Matrix.translateM(rotationMatrix, 0, xPos, yPos, 0);


        //Matrix.scaleM( translationMatrix, 0, 0.05f, 0.05f, 0.05f );
        //Log.e( "blowme", "DeltaX " + deltaX + "DeltaY " + deltaY );

        parametricX = 0;
        parametricY = 0;
        deltaX = 0;
        deltaY = 0;

        Matrix.multiplyMM(bladeRotation, 0, modelMatrix, 0, rotationMatrix, 0);

        //rotate -65 degrees about y-axis
        Matrix.rotateM(bladeRotation, 0, yAngle, 0, 1, 0);
        //rotate 90 degrees about x-axis
        Matrix.rotateM(bladeRotation, 0, 90, 1, 0, 0);

        //Log.e("rotation", "fan angle " + fingerRotation);

        //Matrix.rotateM(bladeRotation, 0, fingerRotation, 1, 0, 0);

        //rotate -65 degrees about y-axis
        //Matrix.rotateM(bladeRotation, 0, -65, 0, 1, 0);
        //rotate 90 degrees about x-axis
        //Matrix.rotateM(bladeRotation, 0, 90, 1, 0, 0);

        //since fan is initially rotated 90 about x, translation occurs on Z instead of y
        //and rotation occurs about y instead of z
        Matrix.rotateM(bladeRotation, 0, angle, 0, -1, 0);

        // Combine the rotation matrix with the projection and camera view
        // Note that the mMVPMatrix factor *must be first* in order
        // for the matrix multiplication product to be correct.

        return bladeRotation;
    }

    //arc = deg*2*pi*r/2Pi
    //deg = arc*2Pi/
    private void moveParametric()
    {
        float arcLength;
        //if(stop)
        //    arcLength = 0;
        //else
        arcLength = Math.abs(deltaX) + Math.abs(deltaY);

        float a = GamePlayActivity.X_EDGE_POSITION;
        float b = GamePlayActivity.Y_EDGE_POSITION;
        float newX;
        float newY;
        float slowdown = 0.95f;

        if (clockwise)
        {
            parametricAngle -= arcLength / slowdown;
        }
        else
        {
            parametricAngle += arcLength / slowdown;
        }

        newX = a * (float) Math.cos(parametricAngle);
        newY = b * (float) Math.sin(parametricAngle);

        parametricX = newX - xPos;
        parametricY = newY - yPos;
        xPos = newX;
        yPos = newY;

        Log.e("moveParametric", "xPos " + xPos + " yPos " + yPos);

        getWind().xPos = xPos;
        getWind().yPos = yPos;

        getBounds().setBounds(xPos - getSize()[0] / 2, yPos - getSize()[1] / 2, xPos + getSize()
                [0] / 2, yPos + getSize()[1] / 2);
        //getWind().setBounds( new float[]{-0.4f, -0.25f, 0.4f, 0.25f} );
        //getWind().getBounds().calculateBounds(calculateInwardParametricRotation());
        //getWind().updatePosition(parametricX,parametricY);
        calculateInwardParametricRotation();
    }

    /**
     * Calculates the change in x and y position
     * @param x - new x position
     * @param y - new y position
     */
    @Override
    public void updatePosition(float x, float y)
    {
        // 0<= x <= 2
        // 0<= y <= 2
        deltaX = (x - initialX);
        deltaY = -(y - initialY); //invert to account for screen coordinates being inverted

        prevX = initialX;
        prevY = initialY;
        //deltaY > 0 -> moving up, deltaY < 0 -> moving down
        //deltaX < 0 -> moving left, deltaX > 0 -> moving right


        //determine if finger is moving clockwise or counter-clockwise
        //directions[updateCount % 10] = (((initialX - 1.0f) * (y - 1.0f)) - ((initialY - 1.0f) * (x - 1.0f))) > 0;

        clockwise = (((initialX - 1.0f) * (y - 1.0f)) - ((initialY - 1.0f) * (x - 1.0f))) > 0;
/*
        if (Math.abs(deltaY) > Math.abs(deltaX))
        {
            if (!(x < 1.1 && x > 0.9))
            {
                if (x < 1)
                {
                    clockwise = deltaY > 0;
                }
                else
                {
                    clockwise = deltaY < 0;
                }
            }
        }
        else
        {
            if (!(y < 1.1 && y > 0.9))
            {
                if (y < 1)
                {
                    clockwise = deltaX > 0;
                }
                else
                {
                    clockwise = deltaX < 0;
                }
            }
        }
*/
        stop = false;
        //update initial position
        initialX = x;
        initialY = y;

        updateCount++;

        moveParametric();
    }

    public void touch(float x)
    {
        clockwise = x < 1;
    }


    public Wind getWind()
    {
        return wind;
    }

    public void setWind(Wind wind)
    {
        this.wind = wind;
    }

    public void setInitialY(float initialY)
    {
        this.initialY = initialY;
    }

    public void setInitialX(float initialX)
    {
        this.initialX = initialX;
    }

    public void updateFingerRotation(float fingerRotation) {
        this.fingerRotation += fingerRotation;
        calculateInwardParametricRotation();
    }

    public void setInitialized(boolean initialized) {
        this.initialized = initialized;
    }

    public boolean isBlowing() {
        return blowing;
    }

    public void setBlowing(boolean blowing) {
        this.blowing = blowing;
    }

    private float deltaX;
    private float deltaY;

    private float parametricX;
    private float parametricY;
    private float parametricAngle = (float) Math.PI;
    private float initialX;
    private float initialY;
    private float prevX;
    private float prevY;
    private Wind wind;
    private boolean clockwise;
    private int updateCount;
    private boolean directions[] = new boolean[10];

    public boolean stop = true;

    private float prevTime;

    private float fingerRotation = 0;

    private long initialTime = 0;
    private float initRoutineX;
    private float initRoutineY;
    private float initRoutineZAngle;
    private float initRoutineYAngle;

    private float inwardRotation = 0;

    private boolean initialized = false;

    private boolean blowing = true;

    private float yAngle;
    private float targetYAngle;
    private float targetZAngle;
    private float targetX;
    private float targetY;
}
