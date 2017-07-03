package wesley.folz.blowme.gamemode;

import android.opengl.Matrix;
import android.support.v4.view.MotionEventCompat;
import android.util.Log;
import android.view.MotionEvent;

import java.util.ArrayList;

import wesley.folz.blowme.R;
import wesley.folz.blowme.graphics.Background;
import wesley.folz.blowme.graphics.Line;
import wesley.folz.blowme.graphics.effects.Explosion;
import wesley.folz.blowme.graphics.models.DestructiveObstacle;
import wesley.folz.blowme.graphics.models.Dispenser;
import wesley.folz.blowme.graphics.models.FallingObject;
import wesley.folz.blowme.graphics.models.Fan;
import wesley.folz.blowme.graphics.models.Missile;
import wesley.folz.blowme.graphics.models.Model;
import wesley.folz.blowme.graphics.models.RicochetObstacle;
import wesley.folz.blowme.graphics.models.Vortex;
import wesley.folz.blowme.ui.GamePlayActivity;
import wesley.folz.blowme.ui.GamePlaySurfaceView;
import wesley.folz.blowme.util.GraphicsUtilities;
import wesley.folz.blowme.util.Physics;

/**
 * Created by Wesley on 9/24/2016.
 */

public abstract class ModeConfig
{
    public ModeConfig()
    {
    }

    public void reinitialize() {
        fan.setTargets(TARGET_X, TARGET_Y, TARGET_Y_ANGLE, TARGET_Z_ANGLE);
        for (Model m : models) {
            m.setInitialized(false);
        }
        positionsInitialized = false;
    }

    public void enableModelGraphics()
    {
        graphicsData = new GraphicsUtilities();

        //initialize data
        graphicsData.storeModelData("dispenser", R.raw.uv_dispenser);
        //graphicsData.storeModelData("short_spikes", R.raw.uv_spikes);
        graphicsData.storeModelData("ring", R.raw.uv_ring);

        graphicsData.storeModelData("cube", R.raw.uv_cube);
        //graphicsData.storeModelData("fan", R.raw.uv_fan);
        graphicsData.storeModelData("fan", R.raw.fan_orig);
        graphicsData.storeModelData("vortex", R.raw.uv_vortex);

        graphicsData.storeModelData("missile", R.raw.missile);

        graphicsData.storeTexture("missile_tex", R.drawable.colored_missile_texture);

        graphicsData.storeTexture("yellow_circle", R.drawable.yellow_circle);
        graphicsData.storeTexture("sky", R.drawable.sky_texture);
        graphicsData.storeTexture("grid", R.drawable.grid);
        graphicsData.storeTexture("cube_wood", R.drawable.cube_wood_tex);
        graphicsData.storeTexture("brick", R.drawable.cube_brick_tex);

        graphicsData.storeTexture("fan_test", R.drawable.fan_orig_tex);
        graphicsData.storeTexture("vortex_tex", R.drawable.vortex_tex);

        graphicsData.storeTexture("grey_circle", R.drawable.grey_circle);
        graphicsData.storeTexture("sun", R.drawable.sun);

        String[] particleAttributes = new String[]{"position", "direction", "normalVector", "speed", "color"};
        String[] modelAttributes = new String[]{"position", "color", "normalVector"};

        graphicsData.storeShader("dust_cloud", R.raw.dust_cloud_vertex_shader, R.raw.dust_cloud_fragment_shader, particleAttributes);
        graphicsData.storeShader("explosion", R.raw.particle_vertex_shader, R.raw.particle_fragment_shader, particleAttributes);
        graphicsData.storeShader("wind", R.raw.wind_vertex_shader, R.raw.wind_fragment_shader, particleAttributes);
        graphicsData.storeShader("texture", R.raw.texture_vertex_shader, R.raw.texture_fragment_shader, modelAttributes);
        graphicsData.storeShader("lighting", R.raw.lighting_vertex_shader, R.raw.lighting_fragment_shader, modelAttributes);

        for (FallingObject fo : fallingObjects) {
            fo.enableGraphics(graphicsData);
        }

        for (Vortex v : vortexes) {
            v.enableGraphics(graphicsData);
        }

        background.enableGraphics(graphicsData);
        fan.enableGraphics(graphicsData);
        for (Model m : models)
        {
            //          m.enableGraphics(graphicsData);
        }
        Log.e("pause", "enable graphics mode");
    }

    public void surfaceGraphicsChanged(int width, int height)
    {
        float ratio = (float) width / height;

        // Set the camera position (View matrix)
        Matrix.setLookAtM(viewMatrix, 0, 0, 0, 5f, 0, 0, 0.0f, 0, 1.0f, 0);

        // this projection matrix is applied to object coordinates
        // in the onDrawFrame() method
        //Matrix.frustumM( projectionMatrix, 0, - ratio, ratio, - 1, 1, 1, 10 );
        Matrix.orthoM(projectionMatrix, 0, -ratio, ratio, -1, 1, 1, 10);

        float[] mLightPosInWorldSpace = new float[4];
        float[] mLightPosInModelSpace = new float[]{0.0f, 0.0f, 0.0f, 1.0f};
        float[] mLightModelMatrix = new float[16];

        // Calculate position of the light. Push into the distance.
        Matrix.setIdentityM(mLightModelMatrix, 0);
        Matrix.translateM(mLightModelMatrix, 0, 0.0f, 0.0f, 3.0f);

        Matrix.multiplyMV(mLightPosInWorldSpace, 0, mLightModelMatrix, 0, mLightPosInModelSpace, 0);
        Matrix.multiplyMV(lightPosInEyeSpace, 0, viewMatrix, 0, mLightPosInWorldSpace, 0);
        //Matrix.multiplyMV(lightPosInEyeSpace, 0, projectionMatrix, 0, lightPosInEyeSpace, 0);

        for (Model m : models)
        {
            m.initializeMatrices(viewMatrix, projectionMatrix, lightPosInEyeSpace);
        }

        Log.e("pause", "surface changed mode");
    }

    protected abstract void initializeGameObjects();

    protected void initializeFromExistingMode(ModeConfig mode, GamePlaySurfaceView surfaceView)
    {
        this.graphicsData = mode.graphicsData;
        this.projectionMatrix = mode.projectionMatrix;
        this.viewMatrix = mode.viewMatrix;
        this.lightPosInEyeSpace = mode.lightPosInEyeSpace;
        this.background = mode.background;
        if (this.fan != null) {
            Log.e("existingmode", "this fan " + this.fan.getyPos() + " x " + this.fan.getxPos()
                    + " mode fan " + mode.fan.getyPos() + " x " + mode.fan.getxPos());
        }
        this.fan = mode.fan;
        this.dispenser = mode.dispenser;

        if (surfaceView == null) {
            models.add(background);
            models.add(fan);
            models.add(dispenser);
        } else {
            int modelCount = 0;
            for (Model m : models) {
                if (m.getClass() == fan.getClass()) {
                    models.set(modelCount, fan);
                } else {
                    if (m.getClass() == background.getClass()) {
                        models.set(modelCount, background);
                    } else {

                        if (m.getClass() == dispenser.getClass()) {
                            models.set(modelCount, dispenser);
                        }
                    }
                }
                m.initializeMatrices(viewMatrix, projectionMatrix, lightPosInEyeSpace);
                modelCount++;
            }

            surfaceView.queueEvent(new Runnable() {
                @Override
                public void run() {
                    for (Model m : models) {
                        m.enableGraphics(graphicsData);
                    }
                }
            });

            if (this.fan != null) {
                Log.e("existingmode", "this fan " + this.fan.getyPos() + " x " + this.fan.getxPos()
                        + " mode fan " + mode.fan.getyPos() + " x " + mode.fan.getxPos());
            }
        }
    }

    protected void initializationRoutine()
    {
        boolean initialized = true;
        for (Model m : models)
        {
            initialized &= m.initializationRoutine();
        }
        positionsInitialized = initialized;
        fanReadyToMove = positionsInitialized;
    }

    protected float dispenseInteraction(FallingObject falObj, boolean objectEffected) {
        float xForce = 0;
        //falling object is being dispensed
        if (falObj.getyPos() > 0.95f && !objectEffected) {
            //falObj.updatePosition(100 * dispenser.getDeltaX(), 0);
            xForce = 100 * dispenser.getDeltaX();
            //objectEffected = true;
        }
        return xForce;
    }


    protected boolean windInteraction(Model falObj, boolean objectEffected) {
        boolean windCollision = false;
        //calculate wind influence
        if (Physics.isCollision(falObj.getBounds(), fan.getWind().getBounds())
                && !objectEffected) {
            Physics.calculateWindForce(fan.getWind(), falObj);
            windCollision = true;
        }
        return windCollision;
    }

    protected FallingObject offscreenInteraction(FallingObject falObj, int modelCount) {
        if (falObj.isOffscreen()) {
            Log.e("collision", "offscreen " + falObj.getType());
            try {
                models.remove(falObj);
                //falObj = falObj.getClass().getConstructor(float.class).newInstance
                // (dispenser.getxPos());
                String type = falObj.getType();
                falObj = new FallingObject(type, dispenser.getxPos());
                fallingObjects.set(modelCount, falObj);
                falObj.enableGraphics(graphicsData);
                falObj.initializeMatrices(viewMatrix, projectionMatrix, lightPosInEyeSpace);
                models.add(falObj);
            } catch (Exception e) {
                Log.e("error", e.getMessage());
            }
        }
        return falObj;
    }

    protected boolean missileInteraction(Model model) {
        Explosion objectExplosion = explosions.get(explosionIndex);
        boolean didExplode = false;

        if (!model.isOffscreen()) {
            for (Missile m : missiles) {
                if (Physics.isCollision(m.getBounds(), model.getBounds())) {
                    objectExplosion.reinitialize(model.getxPos(), model.getyPos());
                    model.setOffscreen(true);
                    m.setOffscreen(true);
                    didExplode = true;
                    //rotate through all explosions
                    //this is done to avoid creating new explosion objects during gameplay
                    //since generating the particles is costly
                    if (explosionIndex < explosions.size() - 1) {
                        explosionIndex++;
                    } else {
                        explosionIndex = 0;
                    }
                    break;
                }
            }
        }
        return didExplode;
    }

    protected boolean destructionInteraction(Model model) {
        Explosion objectExplosion = explosions.get(explosionIndex);

        boolean didExplode = false;

        if (!model.isOffscreen()) {
            for (DestructiveObstacle h : hazards) {
                if (Physics.isCollision(h.getBounds(), model.getBounds())) {
                    objectExplosion.reinitialize(model.getxPos(), model.getyPos());
                    model.setOffscreen(true);
                    didExplode = true;
                    //rotate through all explosions
                    //this is done to avoid creating new explosion objects during gameplay
                    //since generating the particles is costly
                    if (explosionIndex < explosions.size() - 1) {
                        explosionIndex++;
                    } else {
                        explosionIndex = 0;
                    }
                    break;
                }
            }
        }
        return didExplode;
    }

    protected boolean vortexInteraction(FallingObject falObj) {
        boolean objectEffected = false;
        int vortexCount = 0;
        for (Vortex vortex : vortexes) {
            //Log.e("collision", "is collision " + Physics.isCollision(vortex.getBounds(), falObj
            // .getBounds()) + " spiraling "
            //       + falObj.isSpiraling());
            //vortex position - falling object position
            if (Physics.isCollision(vortex.getBounds(), falObj.getBounds())
                    || (falObj.isSpiraling() && vortex.isCollecting()
                    && vortexCount == falObj.getCollectingVortexIndex())) {
                vortex.setCollecting(true);
                //falObj.travelOnVector(vortex.getxPos() - falObj.getxPos(), vortex.getyPos()
                // - falObj.getyPos());
                falObj.setCollectingVortexIndex(vortexCount);
                if (vortex.getType().equals(falObj.getType())) {
                    falObj.spiralIntoVortex(vortex.getxPos());
                } else {
                    Log.e("collision", "spiraling");
                    falObj.spiralOutOfVortex(vortex);
                }
                objectEffected = true;
                break;
            } else {
                vortex.setCollecting(false);
            }
            vortexCount++;
        }
        return objectEffected;
    }

    public void updatePositionsAndDrawModels()
    {
        if (!positionsInitialized)
        {
            initializationRoutine();
        }
        else
        {
            updateModelPositions();
        }

        drawModels();
    }

    protected abstract void updateModelPositions();

    protected void drawModels()
    {
        background.draw();
        fan.draw();

        dispenser.draw();

        for (RicochetObstacle o : obstacles)
        {
            o.draw();
        }
        for (DestructiveObstacle h : hazards)
        {
            h.draw();
        }

        for (Missile m : missiles) {
            m.draw();
        }

        for (FallingObject falObj : fallingObjects)
        {
            falObj.draw();
        }

        for (Vortex v : vortexes)
        {
            v.draw();
        }

        for (Explosion e : explosions)
        {
            if (e.isExploding())
            {
                e.updatePosition(0, 0);
                e.draw();
            }
        }
        //line.draw();
    }

    public void handleTouch(int action, float x, float y)
    {
        Log.e("touch", "touch");
        switch (action)
        {
            case MotionEvent.ACTION_UP:
                //gameMode.handleFanMovementDown(x, y);
                fan.stop = true;
                break;

            case MotionEvent.ACTION_DOWN:
                //gameMode.handleFanMovementDown(x, y);
                //fan.updatePosition(x, y);
                fan.touch(x);
                fan.stop = false;
                break;
        }
    }

    public void handleTouchDrag(MotionEvent event, float x, float y)
    {
        final int action = MotionEventCompat.getActionMasked(event);
        if (fanReadyToMove) {
            switch (action) {
                case MotionEvent.ACTION_POINTER_UP:
                    touchActionStarted = true;
                    //gameMode.handleFanMovementDown(x, y);
                    break;
                case MotionEvent.ACTION_POINTER_DOWN:
                    //gameMode.handleFanMovementDown(x, y);
                    touchActionStarted = true;
                    break;

                case MotionEvent.ACTION_UP:
                    touchActionStarted = true;
                    //gameMode.handleFanMovementDown(x, y);
                    break;

                case MotionEvent.ACTION_DOWN:
                    //gameMode.handleFanMovementDown(x, y);
                    touchActionStarted = true;
                    break;

                case MotionEvent.ACTION_MOVE:
                    if (touchActionStarted) {
                        this.handleFanMovementDown(x, y);
                        touchActionStarted = false;
                    }
                    this.handleFanMovementMove(x, y);

                    //Log.e( "blowme", "Move: X " + (event.getRawX() / WIDTH) + " Y " +
                    // (event.getRawY() / HEIGHT) );
                    break;
            }
        }
    }

    public void handleFanMovementDown(float x, float y)
    {
        if (fanReadyToMove) {
            fan.setInitialX(x);
            fan.setInitialY(y);
            initialPositionSet = true;
        }
    }

    protected void handleFanMovementMove(float x, float y)
    {
        if (fanReadyToMove && initialPositionSet) {
            fan.updatePosition(x, y);
        }

        // obstacles.get(0).updatePosition(x, y);
        // fallingObjects.get(0).updatePosition(x, y);
    }

    public void pauseGame()
    {
        Log.e("pause", "pause mode");
        fanReadyToMove = false;
        for (Model m : models)
        {
            m.pauseGame();
        }
    }

    public void resumeGame()
    {
        Log.e("pause", "resume mode");
        fanReadyToMove = true;
        for (Model m : models)
        {
            m.resumeGame();
        }
    }

    public void stopGame() {
        Log.e("pause", "stop mode");
    }

    public long getTimeRemaining() {
        return timeRemaining;
    }


    long timeRemaining = 10;

    public GraphicsUtilities getGraphicsData()
    {
        return graphicsData;
    }

    public boolean isObjectiveComplete()
    {
        return objectiveComplete;
    }

    public boolean isObjectiveFailed()
    {
        return objectiveFailed;
    }

    public int getNumCubesRemaining()
    {
        return numCubesRemaining;
    }

    public int getNumRingsRemaining()
    {
        return numRingsRemaining;
    }


    public String getLevel() {
        return level;
    }

    protected ArrayList<Model> models;
    ArrayList<RicochetObstacle> obstacles;
    ArrayList<DestructiveObstacle> hazards;
    ArrayList<FallingObject> fallingObjects;
    ArrayList<Explosion> explosions;
    ArrayList<Vortex> vortexes;
    ArrayList<Missile> missiles;

    protected Fan fan;
    protected Background background;
    Dispenser dispenser;
    protected Line line;

    protected float[] viewMatrix = new float[16];
    protected float[] projectionMatrix = new float[16];
    protected float[] lightPosInEyeSpace = new float[16];

    private boolean draw;

    protected GraphicsUtilities graphicsData;

    int explosionIndex = 0;

    //initial game parameters:

    //dynamic game parameters
    int numCubesRemaining;
    int numRingsRemaining;

    boolean objectiveComplete = false;

    protected boolean touchActionStarted;

    boolean positionsInitialized;

    boolean objectiveFailed = false;

    boolean fanReadyToMove = false;

    boolean initialPositionSet = false;

    protected String level;

    protected float TARGET_X = -GamePlayActivity.X_EDGE_POSITION;
    protected float TARGET_Y = 0;
    protected float TARGET_Z_ANGLE = 0;
    protected float TARGET_Y_ANGLE = -65;
}
