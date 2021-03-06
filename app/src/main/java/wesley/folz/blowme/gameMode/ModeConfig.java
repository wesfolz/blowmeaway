package wesley.folz.blowme.gamemode;

import android.opengl.Matrix;
import android.support.v4.view.MotionEventCompat;
import android.util.Log;
import android.view.MotionEvent;

import java.util.ArrayList;
import java.util.Iterator;

import wesley.folz.blowme.R;
import wesley.folz.blowme.graphics.Background;
import wesley.folz.blowme.graphics.Wormhole;
import wesley.folz.blowme.graphics.effects.Explosion;
import wesley.folz.blowme.graphics.models.DestructiveObstacle;
import wesley.folz.blowme.graphics.models.Dispenser;
import wesley.folz.blowme.graphics.models.FallingObject;
import wesley.folz.blowme.graphics.models.Fan;
import wesley.folz.blowme.graphics.models.Missile;
import wesley.folz.blowme.graphics.models.MissileLauncher;
import wesley.folz.blowme.graphics.models.Model;
import wesley.folz.blowme.graphics.models.RicochetObstacle;
import wesley.folz.blowme.graphics.models.Vortex;
import wesley.folz.blowme.graphics.models.WormholeDistortion;
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
        graphicsData.storeModelData("spike", R.raw.spike);
        graphicsData.storeModelData("ring", R.raw.uv_ring);

        graphicsData.storeModelData("cube", R.raw.uv_cube);
        graphicsData.storeModelData("fan", R.raw.fan_orig);
        graphicsData.storeModelData("vortex", R.raw.uv_vortex);
        graphicsData.storeModelData("missile", R.raw.missile);
        graphicsData.storeModelData("fuse", R.raw.fuse);
        graphicsData.storeModelData("launcher_stand", R.raw.launcher_stand);
        graphicsData.storeModelData("launcher_tube", R.raw.launcher_tube);
        graphicsData.storeModelData("wormhole", R.raw.wormhole);
        graphicsData.storeModelData("wormhole_core", R.raw.wormhole_core);

        graphicsData.storeTexture("missile_tex", R.drawable.colored_missile_texture);
        graphicsData.storeTexture("yellow_circle", R.drawable.yellow_circle);
        graphicsData.storeTexture("sky", R.drawable.sky_texture);
        graphicsData.storeTexture("planet", R.drawable.planet);
        graphicsData.storeTexture("grid", R.drawable.grid);
        graphicsData.storeTexture("cube_wood", R.drawable.cube_wood_tex);
        graphicsData.storeTexture("brick", R.drawable.cube_brick_tex);
        graphicsData.storeTexture("launcher_stand_tex", R.drawable.launcher_stand_texture);
        graphicsData.storeTexture("launcher_tube_tex", R.drawable.launcher_tube_texture);
        graphicsData.storeTexture("spike_tex", R.drawable.spike);
        graphicsData.storeTexture("fan_test", R.drawable.fan_orig_tex);
        graphicsData.storeTexture("vortex_tex", R.drawable.vortex_tex);

        graphicsData.storeTexture("grey_circle", R.drawable.grey_circle);
        graphicsData.storeTexture("sun", R.drawable.sun);

        String[] particleAttributes = new String[]{"position", "direction", "normalVector", "speed", "color"};
        String[] modelAttributes = new String[]{"position", "color", "normalVector"};

        graphicsData.storeShader("dust_cloud", R.raw.dust_cloud_vertex_shader, R.raw.dust_cloud_fragment_shader, particleAttributes);
        graphicsData.storeShader("explosion", R.raw.particle_vertex_shader, R.raw.particle_fragment_shader, particleAttributes);
        graphicsData.storeShader("sparkler", R.raw.sparkler_vertex_shader,
                R.raw.sparkler_fragment_shader, particleAttributes);
        graphicsData.storeShader("wind", R.raw.wind_vertex_shader, R.raw.wind_fragment_shader, particleAttributes);
        graphicsData.storeShader("texture", R.raw.texture_vertex_shader, R.raw.texture_fragment_shader, modelAttributes);
        graphicsData.storeShader("lighting", R.raw.lighting_vertex_shader, R.raw.lighting_fragment_shader, modelAttributes);
        graphicsData.storeShader("default", R.raw.defaultvertexshader, R.raw.defaultfragmentshader,
                modelAttributes);
        graphicsData.storeShader("specular", R.raw.specular_vertex_shader,
                R.raw.specular_fragment_shader, modelAttributes);
        graphicsData.storeShader("distortion", R.raw.distortion_vertex_shader,
                R.raw.texture_fragment_shader, modelAttributes);


        for (Model m : models) {
            m.enableGraphics(graphicsData);
        }
        /*
        for (FallingObject fo : fallingObjects) {
            fo.enableGraphics(graphicsData);
        }

        for (Vortex v : vortexes)
            v.enableGraphics(graphicsData);

        background.enableGraphics(graphicsData);
        fan.enableGraphics(graphicsData);

        if (wormhole != null) {
            wormhole.enableGraphics(graphicsData);
        }
        */
    }

    public void surfaceGraphicsChanged(int width, int height)
    {
        if (!resuming) {
            float ratio = (float) width / height;
            float eyeZ = 5.0f;

            Log.e("ratio", "ratio " + ratio);
            // Set the camera position (View matrix)
            Matrix.setLookAtM(viewMatrix, 0, 0, 0, eyeZ, 0, 0, 0.0f, 0, 1.0f, 0);

            // this projection matrix is applied to object coordinates
            // in the onDrawFrame() method
            float zNear = 1;
            float zFar = 1000;
            float size = 1 / eyeZ;
            Matrix.frustumM(perspectiveMatrix, 0, -ratio * size, ratio * size, -size, size, zNear,
                    zFar);
            Matrix.orthoM(orthographicMatrix, 0, -ratio, ratio, -1, 1, 4, 10);

            float[] mLightPosInWorldSpace = new float[4];
            float[] mLightPosInModelSpace = new float[]{0.0f, 0.0f, 0.0f, 1.0f};
            float[] mLightModelMatrix = new float[16];

            // Calculate position of the light. Push into the distance.
            Matrix.setIdentityM(mLightModelMatrix, 0);
            Matrix.translateM(mLightModelMatrix, 0, 0.0f, 0.0f, 3.0f);

            Matrix.multiplyMV(mLightPosInWorldSpace, 0, mLightModelMatrix, 0, mLightPosInModelSpace,
                    0);
            Matrix.multiplyMV(lightPosInEyeSpace, 0, viewMatrix, 0, mLightPosInWorldSpace, 0);
            //Matrix.multiplyMV(lightPosInEyeSpace, 0, perspectiveMatrix, 0, lightPosInEyeSpace, 0);

            for (Model m : models) {
                m.initializeMatrices(viewMatrix, perspectiveMatrix, orthographicMatrix,
                        lightPosInEyeSpace);
            }

            Log.e("pause", "surface changed mode");
        }
    }

    protected abstract void initializeGameObjects();

    void initializeFromExistingMode(ModeConfig mode, GamePlaySurfaceView surfaceView)
    {
        this.graphicsData = mode.graphicsData;
        this.perspectiveMatrix = mode.perspectiveMatrix;
        this.orthographicMatrix = mode.orthographicMatrix;
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
                m.initializeMatrices(viewMatrix, perspectiveMatrix, orthographicMatrix,
                        lightPosInEyeSpace);
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

    private void exitRoutine() {
        boolean exited = true;
        fan.setTargets(MenuModeConfig.FAN_TARGET_X, MenuModeConfig.FAN_TARGET_Y,
                MenuModeConfig.FAN_TARGET_Y_ANGLE, MenuModeConfig.FAN_TARGET_Z_ANGLE);
        for (Model m : models) {
            exited &= m.removalRoutine();
            Log.e("exitRoutine", m.getClass().getName() + " " + exited);
        }

        exiting = !exited;
        modeComplete = exited;
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

    float dispenseInteraction(FallingObject falObj) {
        float xForce = 0;
        //falling object is being dispensed
        if (falObj.getyPos() > 0.95f) {
            //falObj.updatePosition(100 * dispenser.getDeltaX(), 0);
            xForce = 100 * dispenser.getDeltaX();
            //objectEffected = true;
        }
        return xForce;
    }

    boolean windInteraction(Model falObj, Fan f) {
        boolean windCollision = false;
        //calculate wind influence
        if (Physics.isCollision(f.getWind().getBounds(), falObj.getBounds())) {
            Physics.calculateWindForce(f.getWind(), falObj);
            windCollision = true;
        }
        return windCollision;
    }

    FallingObject offscreenInteraction(FallingObject falObj, int modelCount, boolean respawn) {
        if (falObj.isOffscreen() && respawn) {
            try {
                int index = models.indexOf(falObj);
                //models.remove(falObj);
                //falObj = falObj.getClass().getConstructor(float.class).newInstance
                // (dispenser.getxPos());
                String type = falObj.getType();
                falObj = new FallingObject(type, dispenser.getxPos());
                fallingObjects.set(modelCount, falObj);
                falObj.enableGraphics(graphicsData);
                falObj.initializeMatrices(viewMatrix, perspectiveMatrix, orthographicMatrix,
                        lightPosInEyeSpace);
                models.set(index, falObj);
            } catch (Exception e) {
                Log.e("error", e.getMessage());
            }
        }
        return falObj;
    }

    boolean missileInteraction(Model model) {
        Explosion objectExplosion = explosions.get(explosionIndex);
        boolean didExplode = false;

        if (!model.isOffscreen()) {
            for (MissileLauncher ml : missileLaunchers) {
                Missile m = ml.getMissile();
                if (Physics.isCollision(m.getBounds(), model.getBounds()) && !m.isOffscreen()) {
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

    boolean destructionInteraction(Model model) {
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

    void vortexInteraction(FallingObject falObj, boolean respawn) {
        int vortexCount = 0;
        for (Iterator<Vortex> iterator = vortexes.iterator(); iterator.hasNext(); ) {
            Vortex vortex = iterator.next();
            if (Physics.isCollision(vortex.getBounds(), falObj.getBounds())
                    || (falObj.isSpiralIn() && vortex.isCollecting()
                    && vortexCount == falObj.getCollectingVortexIndex())) {

                falObj.setCollectingVortexIndex(vortexCount);
                if (vortex.getType().equals(falObj.getType())) {
                    falObj.setSpiralIn(true, vortex);
                } else {
                    falObj.setSpiralOut(true, vortex);
                }
                break;
            } else {
                vortex.setCollecting(false);
            }
            if (vortex.isOffscreen()) {
                models.remove(vortex);
                if (respawn) {
                    generateVortex(vortex.getType(), vortexCount, vortex.getCapacity(), true);
                } else {
                    iterator.remove();
                }
            }
            vortexCount++;
        }
    }

    private boolean wormholeCollision(Wormhole wormhole, Model model, boolean first) {
        WormholeDistortion d1;
        WormholeDistortion d2;

        if (first) {
            d1 = wormhole.getDistortion1();
            d2 = wormhole.getDistortion2();
        } else {
            d1 = wormhole.getDistortion2();
            d2 = wormhole.getDistortion1();
        }

        if (Physics.isCollision(d1.getBounds(), model.getBounds())) {
            float[] vector = Physics.calculateDistanceVector(
                    d1.getxPos(),
                    d1.getyPos(), model.getxPos(), model.getyPos());
            model.setTransporting(true);
            if (Math.abs(vector[0]) > 0.01f || Math.abs(vector[1]) > 0.01f) {
                //Physics.travelOnVector(model, vector[0], vector[1], 1.0f);
                model.setStretch(Physics.travelOnVector(model, vector[0], vector[1],
                        5.0f));
            } else {
                Physics.transport(d2, model);
                model.setStretch(new float[]{1, 1});
                wormhole.setRemove(true);
                model.setTransporting(false);
            }
            return true;
        }
        return false;
    }

    boolean wormholeInteraction(Wormhole wormhole, Model model) {
        if (wormhole != null) {
            if (!model.isOffscreen() && !wormhole.isOffscreen() && (model.isTransporting()
                    || !wormhole.isRemove())) {
                if (wormholeCollision(wormhole, model, true)) {
                    return true;
                }
                if (wormholeCollision(wormhole, model, false)) {
                    return true;
                }
            }
        }
        return false;
    }

    boolean fallingObjectInteractions(FallingObject falObj, int modelCount,
            boolean respawn) {
        float yForce = 0;
        boolean destroyed = destructionInteraction(falObj) | missileInteraction(falObj);

        falObj = offscreenInteraction(falObj, modelCount, respawn);

        if (!respawn) {
            destroyed |= falObj.isOffscreen();
        }

        //determine forces due to collisions with obstacles
        falObj.calculateRicochetCollisions(obstacles);

        vortexInteraction(falObj, respawn);

        float xForce = dispenseInteraction(falObj);

        for (Fan f : fans) {
            if (windInteraction(falObj, f)) {
                xForce += f.getWind().getxForce();
                yForce += f.getWind().getyForce();
            }
        }

        if (!wormholeInteraction(wormhole, falObj)) {
            falObj.updatePosition(xForce, yForce);
        }

        return destroyed;
    }

    void generateVortex(String type, int index, int capacity, boolean reinitialize) {
        Vortex v = new Vortex(type,
                (float) (index + 1) * (2.0f / (numVortexes + 1.0f)) - 1, capacity);
        if (reinitialize) {
            v.enableGraphics(graphicsData);
            v.initializeMatrices(viewMatrix, perspectiveMatrix, orthographicMatrix,
                    lightPosInEyeSpace);
            vortexes.set(index, v);
        } else {
            vortexes.add(v);
        }
        models.add(v);
    }

    public void updatePositionsAndDrawModels()
    {
        if (exiting) {
            exitRoutine();
        } else
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
        for (Model m : models) {
            if (!m.isOffscreen()) {
                m.draw();
            }
        }
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

    void handleFanMovementMove(float x, float y)
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
        resuming = true;
        for (Model m : models)
        {
            m.resumeGame();
        }
    }

    public void stopGame() {
        Log.e("pause", "stop mode");
        exiting = true;
    }

    public boolean isModeComplete() {
        return modeComplete;
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
    ArrayList<MissileLauncher> missileLaunchers;
    ArrayList<Fan> fans;

    Wormhole wormhole;

    protected Fan fan;
    protected Background background;
    Dispenser dispenser;

    protected float[] viewMatrix = new float[16];
    protected float[] perspectiveMatrix = new float[16];
    protected float[] orthographicMatrix = new float[16];
    protected float[] lightPosInEyeSpace = new float[16];

    private boolean draw;

    protected GraphicsUtilities graphicsData;

    int explosionIndex = 0;

    //initial game parameters:

    //dynamic game parameters
    int numCubesRemaining;
    int numRingsRemaining;

    boolean objectiveComplete = false;

    boolean touchActionStarted;

    boolean positionsInitialized;

    boolean objectiveFailed = false;

    boolean fanReadyToMove = false;

    boolean initialPositionSet = false;

    protected String level;

    int numVortexes = 0;

    float TARGET_X = -GamePlayActivity.X_EDGE_POSITION;
    float TARGET_Y = 0;
    float TARGET_Z_ANGLE = 0;
    float TARGET_Y_ANGLE = -65;

    private boolean modeComplete = false;

    private boolean exiting = false;

    private boolean resuming = false;
}