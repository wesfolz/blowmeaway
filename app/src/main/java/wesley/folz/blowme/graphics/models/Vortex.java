package wesley.folz.blowme.graphics.models;

import android.opengl.Matrix;
import android.os.SystemClock;

import java.util.ArrayList;

import wesley.folz.blowme.graphics.effects.DustCloud;
import wesley.folz.blowme.ui.GamePlayActivity;
import wesley.folz.blowme.util.Bounds;
import wesley.folz.blowme.util.GraphicsUtilities;

/**
 * Created by Wesley on 9/11/2016.
 */
public class Vortex extends Model
{
    public Vortex(String collectionType, float x, int capacity)
    {
        super();

        type = collectionType;
        setBounds(new Bounds());
        setSize(new float[]{0.3f, 0.3f});

        visualYOffset = 0.35f;

        xPos = x;//+.01f;
        yPos = -1.0f;//0.935f;//GamePlayActivity.Y_EDGE_POSITION;

        scaleFactor = 0.04f;

        initialXPos = xPos;
        initialYPos = yPos;
        yPos = yPos + visualYOffset;

        scaleCount = 1.0f;

        dustCloud = new DustCloud(xPos, 1.0f);

        this.capacity = capacity;

        orbitingObjects = new ArrayList<>();

        for (int i = 0; i < capacity; i++)
        {
            orbitingObjects.add(new OrbitingObject(type, xPos, -1.0f,
                    (float) i * 2.0f * (float) Math.PI / (float) capacity));
            //orbitingObjects.add(new OrbitingObject(type, xPos, -1.0f,
            //        (float) Math.PI + i * (float) Math.PI / 2));
        }
    }

    public Vortex(String collectionType, float x) {
        this(collectionType, x, 3);
    }

    @Override
    public void enableGraphics(GraphicsUtilities graphicsData)
    {
        dataVBO = graphicsData.modelVBOMap.get("vortex");
        orderVBO = graphicsData.orderVBOMap.get("vortex");
        numVertices = graphicsData.numVerticesMap.get("vortex");
        programHandle = graphicsData.shaderProgramIdMap.get("texture");
        textureDataHandle = graphicsData.textureIdMap.get("vortex_tex");

        dustCloud.enableGraphics(graphicsData);
        for (OrbitingObject orbitingObject : orbitingObjects)
        {
            orbitingObject.enableGraphics(graphicsData);
        }
    }

    @Override
    public void draw()
    {
        super.draw();
        dustCloud.draw();
        for (OrbitingObject orbitingObject : orbitingObjects)
        {
            orbitingObject.draw();
        }
    }

    @Override
    public float[] createTransformationMatrix()
    {
        float[] mvp = new float[16];

        long time = SystemClock.uptimeMillis();// % 4000L;
        float angle = 0.40f * ((int) time);

        Matrix.setIdentityM(mvp, 0);

        //translate model matrix to new position
        //Matrix.translateM(modelMatrix, 0, deltaX, 0, 0);

        //copy modelMatrix to separate matrix for return, (returning modelMatrix doesn't work)
        //Matrix.translateM(modelMatrix, 0, 0.0f, deltaY, 0.0f);
        Matrix.multiplyMM(mvp, 0, modelMatrix, 0, mvp, 0);
        Matrix.scaleM(mvp, 0, 1.0f, scaleCount, 1.0f);
        Matrix.translateM(mvp, 0, 0.0f, deltaY, 0.0f);

        Matrix.rotateM(mvp, 0, angle, 0, -1, 0);

        return mvp;
    }

    @Override
    public void initializeMatrices(float[] viewMatrix, float[] perspectiveMatrix,
            float[] orthographicMatrix, float[] lightPosInEyeSpace)
    {
        super.initializeMatrices(viewMatrix, perspectiveMatrix, orthographicMatrix,
                lightPosInEyeSpace);
        dustCloud.initializeMatrices(viewMatrix, perspectiveMatrix, orthographicMatrix,
                lightPosInEyeSpace);
        for (OrbitingObject orbitingObject : orbitingObjects)
        {
            orbitingObject.initializeMatrices(viewMatrix, perspectiveMatrix, orthographicMatrix,
                    lightPosInEyeSpace);
        }
    }

    @Override
    public boolean initializationRoutine()
    {
        if (initialized) {
            updatePosition(0, 0);
            return true;
        }
        if (initialTime == 0)
        {
            initialTime = System.currentTimeMillis();
        }
        long time = System.currentTimeMillis();
        float deltaTime = (time - initialTime) / 1000.0f;

        if (deltaTime < GamePlayActivity.INITIALIZATION_TIME) {
            scaleCount = (deltaTime / GamePlayActivity.INITIALIZATION_TIME);
        } else {
            scaleCount = 1.0f;
            initialTime = 0;
            initialized = true;
        }

        deltaY = scaleCount / 5.0f;
        //updatePosition(0, 0);
        dustCloud.updatePosition(0, deltaY);
        for (OrbitingObject orbitingObject : orbitingObjects) {
            orbitingObject.updatePosition(0, deltaY);
        }
        getBounds().setBounds(xPos - getSize()[0] / 2.0f, yPos - 0.21f, xPos + getSize()[0] / 2.0f,
                yPos - 0.1f);

        return initialized;
    }

    @Override
    public boolean removalRoutine() {
        if (offscreen) {
            return true;
        }

        if (initialTime == 0) {
            initialTime = System.currentTimeMillis();
        }
        long time = System.currentTimeMillis();
        float deltaTime = (time - initialTime) / 1000.0f;

        if (deltaTime < GamePlayActivity.INITIALIZATION_TIME) {
            scaleCount = ((GamePlayActivity.INITIALIZATION_TIME - deltaTime)
                    / GamePlayActivity.INITIALIZATION_TIME);
        } else {
            scaleCount = 0.0f;
            initialTime = 0;
            offscreen = true;
        }

        deltaY = scaleCount / 5.0f;

        dustCloud.updatePosition(0, deltaY);
        for (OrbitingObject orbitingObject : orbitingObjects) {
            orbitingObject.updatePosition(0, deltaY);
        }
        getBounds().setBounds(xPos - getSize()[0] / 2.0f, yPos - 0.21f,
                xPos + getSize()[0] / 2.0f, yPos - 0.1f);

        return offscreen;
    }

    @Override
    public void updatePosition(float x, float y)
    {
        if (!initialized) {
            initializationRoutine();
        } else if (numCollected >= capacity) {
            removalRoutine();
        } else {
            dustCloud.updatePosition(0, 0);
            for (OrbitingObject orbitingObject : orbitingObjects) {
                orbitingObject.updatePosition(0, deltaY);
            }
            getBounds().setBounds(xPos - getSize()[0] / 2.0f, yPos - 0.21f,
                    xPos + getSize()[0] / 2.0f, yPos - 0.1f);
        }
    }

    @Override
    public void pauseGame() {
        super.pauseGame();
        dustCloud.pauseGame();
        for (OrbitingObject orbitingObject : orbitingObjects) {
            orbitingObject.pauseGame();
        }
    }

    @Override
    public void resumeGame() {
        super.resumeGame();
        dustCloud.resumeGame();
        for (OrbitingObject orbitingObject : orbitingObjects) {
            orbitingObject.resumeGame();
        }
    }

    public String getType()
    {
        return type;
    }

    public boolean isCollecting()
    {
        return collecting;
    }

    public void setCollecting(boolean collecting)
    {
        this.collecting = collecting;
    }

    public void setNumCollected() {
        numCollected++;
        if (orbitingObjects.size() > 0) {
            orbitingObjects.remove(0);
        }
        //else if(numCollected >= capacity)
    }

    public int getCapacity() {
        return capacity;
    }

    private DustCloud dustCloud;

    private float scaleCount = 1.0f;

    private boolean collecting = false;

    private ArrayList<OrbitingObject> orbitingObjects;

    private String type;

    private long initialTime = 0;

    private int capacity;

    private int numCollected = 0;
}