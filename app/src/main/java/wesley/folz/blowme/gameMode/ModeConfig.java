package wesley.folz.blowme.gamemode;

import android.opengl.Matrix;
import android.util.Log;
import android.view.MotionEvent;

import java.util.ArrayList;
import java.util.Random;

import wesley.folz.blowme.R;
import wesley.folz.blowme.graphics.Background;
import wesley.folz.blowme.graphics.Border;
import wesley.folz.blowme.graphics.Line;
import wesley.folz.blowme.graphics.effects.Explosion;
import wesley.folz.blowme.graphics.models.DestructiveObstacle;
import wesley.folz.blowme.graphics.models.Dispenser;
import wesley.folz.blowme.graphics.models.FallingObject;
import wesley.folz.blowme.graphics.models.Fan;
import wesley.folz.blowme.graphics.models.Model;
import wesley.folz.blowme.graphics.models.RicochetObstacle;
import wesley.folz.blowme.graphics.models.Vortex;
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

    public void initializeGameObjects() {
        numRicochetObstacles = 2;
        numDestructiveObstacles = 2;
        numFallingObjects = 2;
        numRings = 1;
        numCubes = 1;
        numVortexes = 2;
        numRingVortexes = 1;
        numCubeVortexes = 1;
        numCubesRemaining = 1;
        numRingsRemaining = 1;
        models = new ArrayList<>();
        obstacles = new ArrayList<>();
        fallingObjects = new ArrayList<>();
        explosions = new ArrayList<>();
        vortexes = new ArrayList<>();
        hazards = new ArrayList<>();

        fan = new Fan();
        models.add(fan);

        for (int i = 0; i < numRings; i++)
        {
            FallingObject fo = new FallingObject("ring");
            models.add(fo);
            fallingObjects.add(fo);
        }

        for (int i = 0; i < numCubes; i++)
        {
            FallingObject fo = new FallingObject("cube");
            models.add(fo);
            fallingObjects.add(fo);
        }

        for (int i = 0; i < numFallingObjects; i++)
        {
            Explosion explosion = new Explosion();
            models.add(explosion);
            explosions.add(explosion);
        }

        for (int i = 0; i < numRicochetObstacles; i++)
        {
            float pos[] = generateRandomLocation();
            RicochetObstacle ro = new RicochetObstacle(pos[0], pos[1]);
            models.add(ro);
            obstacles.add(ro);
        }

        for (int i = 0; i < numDestructiveObstacles; i++)
        {
            float pos[] = generateRandomLocation();
            float xLoc;
            if (Math.random() > 0.5)
            {
                xLoc = -0.56f;//Border.XLEFT;
            }
            else
            {
                xLoc = 0.56f;//Border.XRIGHT;
            }
            DestructiveObstacle destObj = new DestructiveObstacle(xLoc, pos[1]);
            models.add(destObj);
            hazards.add(destObj);
        }

        //create array list of different vortex type strings
        ArrayList<String> vortexTypes = new ArrayList<>(numVortexes);
        for (int i = 0; i < numRingVortexes; i++)
        {
            vortexTypes.add("ring");
        }
        for (int i = 0; i < numCubeVortexes; i++)
        {
            vortexTypes.add("cube");
        }

        //numVortexes=2 -> x1=-0.5
        for (int i = 0; i < numVortexes; i++)
        {
            Vortex v = new Vortex(vortexTypes.get(i), (float) (i + 1) * (2.0f / (numVortexes + 1.0f)) - 1);
            models.add(v);
            vortexes.add(v);
        }

        dispenser = new Dispenser();
        models.add(dispenser);

        background = new Background();
        models.add(background);

        positionsInitialized = false;

        //line =  new Line();
        //models.add(line);
        Log.e("pause", "constructor mode");
    }


    public void enableModelGraphics()
    {
        graphicsData = new GraphicsUtilities();

        //initialize data
        graphicsData.storeModelData("fan", R.raw.fan);
        graphicsData.storeModelData("vortex", R.raw.vortex_open_top);
        graphicsData.storeModelData("cube", R.raw.cube);
        graphicsData.storeModelData("dispenser", R.raw.triangle_collector);
        graphicsData.storeModelData("short_spikes", R.raw.short_spikes);
        graphicsData.storeModelData("ring", R.raw.ring);


        graphicsData.storeTexture("wood", R.raw.wood_texture);
        graphicsData.storeTexture("brick", R.raw.brick_texture);
        graphicsData.storeTexture("yellow_circle", R.raw.yellow_circle);
        graphicsData.storeTexture("sky", R.raw.sky_texture);
        graphicsData.storeTexture("grid", R.raw.grid);

        graphicsData.storeTexture("grey_circle", R.raw.grey_circle);

        String[] particleAttributes = new String[]{"position", "direction", "normalVector", "speed", "color"};
        String[] modelAttributes = new String[]{"position", "color", "normalVector"};

        graphicsData.storeShader("dust_cloud", R.raw.dust_cloud_vertex_shader, R.raw.dust_cloud_fragment_shader, particleAttributes);
        graphicsData.storeShader("explosion", R.raw.particle_vertex_shader, R.raw.particle_fragment_shader, particleAttributes);
        graphicsData.storeShader("wind", R.raw.wind_vertex_shader, R.raw.wind_fragment_shader, particleAttributes);
        graphicsData.storeShader("texture", R.raw.texture_vertex_shader, R.raw.texture_fragment_shader, modelAttributes);
        graphicsData.storeShader("lighting", R.raw.lighting_vertex_shader, R.raw.lighting_fragment_shader, modelAttributes);

        for (Model m : models)
        {
            m.enableGraphics(graphicsData);
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

    public void initializeFromExistingMode(ModeConfig mode, GamePlaySurfaceView surfaceView)
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

    protected void updateModelPositions()
    {
        background.updatePosition(0, 0);

        //fan.updatePosition(0, 0);
        //fan.getWind().updatePosition(0, 0);

        dispenser.updatePosition(0, 0);

        for (Vortex v : vortexes)
        {
            v.updatePosition(0, 0);
        }

        int modelCount = 0;
        for (RicochetObstacle o : obstacles)
        {
            if (o.isOffscreen())
            {
                models.remove(o);
                float pos[] = generateRandomLocation();
                o = new RicochetObstacle(pos[0], pos[1]);
                obstacles.set(modelCount, o);
                o.enableGraphics(graphicsData);
                o.initializeMatrices(viewMatrix, projectionMatrix, lightPosInEyeSpace);
                models.add(o);
            }
            else
            {
                o.updatePosition(0, 0);
            }
            modelCount++;
        }

        modelCount = 0;
        for (DestructiveObstacle h : hazards)
        {
            if (h.isOffscreen())
            {
                models.remove(h);
                float pos[] = generateRandomLocation();
                float x;
                if (Math.random() > 0.5)
                {
                    x = -0.56f;//Border.XLEFT;
                }
                else
                {
                    x = 0.56f;//Border.XRIGHT;
                }
                h = new DestructiveObstacle(x, pos[1]);
                hazards.set(modelCount, h);
                h.enableGraphics(graphicsData);
                h.initializeMatrices(viewMatrix, projectionMatrix, lightPosInEyeSpace);
                models.add(h);
            }
            else
            {
                h.updatePosition(0, 0);
            }
            modelCount++;
        }

        boolean objectEffected;
        modelCount = 0;
        for (FallingObject falObj : fallingObjects)
        {
            float xForce = 0;
            float yForce = 0;

            objectEffected = false;

            Explosion objectExplosion = explosions.get(explosionIndex);

            for(DestructiveObstacle h: hazards)
            {
                if (Physics.isCollision(h.getBounds(), falObj.getBounds()))
                {
                    objectExplosion.reinitialize(falObj.getxPos(), falObj.getyPos());
                    falObj.setOffscreen(true);
                    //rotate through all explosions
                    //this is done to avoid creating new explosion objects during gameplay
                    //since generating the particles is costly
                    if (explosionIndex < explosions.size() - 1)
                    {
                        explosionIndex++;
                    }
                    else
                    {
                        explosionIndex = 0;
                    }
                    break;
                }
            }

            if (falObj.isOffscreen())
            {
                try
                {
                    models.remove(falObj);
                    //falObj = falObj.getClass().getConstructor(float.class).newInstance(dispenser.getxPos());
                    String type = falObj.getType();
                    falObj = new FallingObject(type, dispenser.getxPos());
                    fallingObjects.set(modelCount, falObj);
                    falObj.enableGraphics(graphicsData);
                    falObj.initializeMatrices(viewMatrix, projectionMatrix, lightPosInEyeSpace);
                    models.add(falObj);
                }
                catch (Exception e)
                {
                    Log.e("error", e.getMessage());
                }
            }

            int vortexCount = 0;
            for (Vortex vortex : vortexes)
            {
                //vortex position - falling object position
                if (Physics.isCollision(vortex.getBounds(), falObj.getBounds())
                        || (falObj.isSpiraling() && vortex.isCollecting() && vortexCount == falObj.getCollectingVortexIndex()))
                {
                    vortex.setCollecting(true);
                    //falObj.travelOnVector(vortex.getxPos() - falObj.getxPos(), vortex.getyPos() - falObj.getyPos());
                    falObj.setCollectingVortexIndex(vortexCount);
                    if (vortex.getType().equals(falObj.getType()))
                    {
                        falObj.spiralIntoVortex(vortex.getxPos());
                    }
                    else
                    {
                        falObj.spiralOutOfVortex(vortex);
                    }
                    objectEffected = true;
                    break;
                }
                else
                {
                    vortex.setCollecting(false);
                }
                vortexCount++;
            }
            //falling object is being dispensed
            if (falObj.getyPos() > 0.95f && !objectEffected)
            {
                //falObj.updatePosition(100 * dispenser.getDeltaX(), 0);
                xForce = 100 * dispenser.getDeltaX();
                //objectEffected = true;
            }

            //determine forces due to collisions with obstacles
            falObj.calculateRicochetCollisions(obstacles);

            //calculate wind influence
            if (Physics.isCollision(fan.getWind().getBounds(), falObj.getBounds()) && !objectEffected)
            {
                Physics.calculateWindForce(fan.getWind(), falObj);
                //Log.e("wind", "xforce " + fan.getWind().getxForce() + " yforce " + fan.getWind().getyForce());
                //Log.e("mode", "wind collision");
                //falObj.updatePosition(fan.getWind().getxForce(), fan.getWind().getyForce());
                xForce = fan.getWind().getxForce();
                yForce = fan.getWind().getyForce();
                objectEffected = true;
            }

            if (!falObj.isSpiraling())
            {
                falObj.updatePosition(xForce, yForce);
            }
            if (falObj.isCollected())
            {
                falObj.setCollected(false);
                switch (falObj.getType())
                {
                    case "cube":
                        if (numCubesRemaining > 0)
                        {
                            numCubesRemaining--;
                        }
                        break;
                    case "ring":
                        if (numRingsRemaining > 0)
                        {
                            numRingsRemaining--;
                        }
                        break;
                    default:
                        break;
                }
                objectiveComplete = (numCubesRemaining == 0) & (numRingsRemaining == 0);
            }
            modelCount++;
        }
        //line.updatePosition(0, 0);
    }

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

    protected float[] generateRandomLocation()
    {
        Random rand = new Random();
        //screen dimensions: -0.5 <= x <= 0.5, -1 <= y <= 1
        //five x locations
        int numXLocations = 5;
        float cellWidth = (Border.XRIGHT - Border.XLEFT) / (float) numXLocations;

        int numYLocations = 8;
        //eight y locations
        float cellHeight = (Border.YTOP - Border.YBOTTOM) / (float) numYLocations;

        if (xLocations.isEmpty())
        {
            for (int i = 0; i < numXLocations; i++)
            {
                xLocations.add(i);
            }
        }

        if (yLocations.isEmpty())
        {
            for (int i = 0; i < numYLocations; i++)
            {
                yLocations.add(i);
            }
        }

        int xIndex = rand.nextInt(xLocations.size());
        int yIndex = rand.nextInt(yLocations.size());

        int xCell = xLocations.get(xIndex);
        int yCell = yLocations.get(yIndex);

        float locations[] = new float[2];
        //Border.XLEFT + cellWidth/2 <= locations[0] <= Border.XRIGHT - cellWidth/2
        locations[0] = (float) xCell * cellWidth + cellWidth / 2 + Border.XLEFT;
        //3*YTOP -> want objects not visible initially
        // [-1, -3]
        locations[1] = (float) yCell * cellHeight + cellHeight / 2 + 3 * Border.YBOTTOM;

        xLocations.remove(xIndex);
        yLocations.remove(yIndex);

        return locations;
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

    public void handleTouchDrag(int action, float x, float y)
    {
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

    public void handleFanMovementMove(float x, float y)
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
        for (Model m : models)
        {
            m.pauseGame();
        }
    }

    public void resumeGame()
    {
        Log.e("pause", "resume mode");
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


    protected long timeRemaining = 10;

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
    protected ArrayList<RicochetObstacle> obstacles;
    protected ArrayList<DestructiveObstacle> hazards;
    protected ArrayList<FallingObject> fallingObjects;
    protected ArrayList<Explosion> explosions;
    protected ArrayList<Vortex> vortexes;

    protected Fan fan;
    protected Background background;
    protected Dispenser dispenser;
    protected Line line;

    protected float[] viewMatrix = new float[16];
    protected float[] projectionMatrix = new float[16];
    protected float[] lightPosInEyeSpace = new float[16];

    private boolean draw;

    protected GraphicsUtilities graphicsData;

    protected int explosionIndex = 0;

    private ArrayList<Integer> xLocations = new ArrayList<>();
    private ArrayList<Integer> yLocations = new ArrayList<>();

    //initial game parameters:
    protected int numRicochetObstacles;
    protected int numDestructiveObstacles;
    protected int numFallingObjects;
    protected int numRings;
    protected int numCubes;
    protected int numVortexes;
    protected int numRingVortexes;
    protected int numCubeVortexes;

    //dynamic game parameters
    protected int numCubesRemaining;
    protected int numRingsRemaining;

    protected boolean objectiveComplete = false;

    private boolean touchActionStarted;

    protected boolean positionsInitialized;

    protected boolean objectiveFailed = false;

    protected boolean fanReadyToMove = false;

    protected boolean initialPositionSet = false;

    protected String level;
}
