package wesley.folz.blowme.gamemode;

import android.support.v4.view.MotionEventCompat;
import android.util.Log;
import android.view.MotionEvent;

import java.util.ArrayList;
import java.util.Random;

import wesley.folz.blowme.graphics.Border;
import wesley.folz.blowme.graphics.Wormhole;
import wesley.folz.blowme.graphics.effects.Explosion;
import wesley.folz.blowme.graphics.models.DestructiveObstacle;
import wesley.folz.blowme.graphics.models.FallingObject;
import wesley.folz.blowme.graphics.models.Missile;
import wesley.folz.blowme.graphics.models.MissileLauncher;
import wesley.folz.blowme.graphics.models.Model;
import wesley.folz.blowme.graphics.models.RicochetObstacle;
import wesley.folz.blowme.graphics.models.SpikeStrip;
import wesley.folz.blowme.graphics.models.Vortex;
import wesley.folz.blowme.ui.GamePlaySurfaceView;
import wesley.folz.blowme.ui.RotationGestureDetector;

/**
 * Created by Wesley on 9/24/2016.
 */

public class EndlessModeConfig extends ModeConfig implements
        RotationGestureDetector.OnRotationGestureListener
{
    public EndlessModeConfig(ModeConfig mode, GamePlaySurfaceView surfaceView) {
        models = new ArrayList<>();

        initializeFromExistingMode(mode, null);
        //Log.e("json", "fanx " + fan.getxPos() + " fany " + fan.getyPos());

        initializeGameObjects();

        for (Model m : models) {
            m.initializeMatrices(viewMatrix, projectionMatrix, lightPosInEyeSpace);
        }

        surfaceView.queueEvent(new Runnable() {
            @Override
            public void run() {
                for (Model m : models) {
                    m.enableGraphics(graphicsData);
                }
            }
        });

        rotationDetector = new RotationGestureDetector(this);
    }

    @Override
    protected void initializeGameObjects() {
        int numRicochetObstacles = 2;
        int numDestructiveObstacles = 2;
        int numRings = 1;
        int numCubes = 1;
        int numFallingObjects = numCubes + numRings;
        int numRingVortexes = 1;
        int numCubeVortexes = 1;
        numVortexes = numRingVortexes + numCubeVortexes;
        int numMissileLaunchers = 1;
        numCubesRemaining = 1;
        numRingsRemaining = 1;
        models = new ArrayList<>();
        obstacles = new ArrayList<>();
        fallingObjects = new ArrayList<>();
        explosions = new ArrayList<>();
        vortexes = new ArrayList<>();
        hazards = new ArrayList<>();
        missileLaunchers = new ArrayList<>();

        //background = new Background();
        models.add(background);

        wormhole = new Wormhole(0.2f, 0.2f, -0.2f, -0.5f);
        wormhole.setBackground(background);
        models.add(wormhole);

        //fan = new Fan();
        models.add(fan);

        //dispenser = new Dispenser();
        models.add(dispenser);

        for (int i = 0; i < numRicochetObstacles; i++) {
            float pos[] = generateRandomLocation();
            RicochetObstacle ro = new RicochetObstacle(pos[0], pos[1]);
            models.add(ro);
            obstacles.add(ro);
        }

        for (int i = 0; i < numDestructiveObstacles; i++) {
            float pos[] = generateRandomLocation();
            float xLoc;
            if (Math.random() > 0.5) {
                xLoc = -0.56f;//Border.XLEFT;
            } else {
                xLoc = 0.56f;//Border.XRIGHT;
            }
            DestructiveObstacle destObj = new SpikeStrip(xLoc, pos[1]);
            models.add(destObj);
            hazards.add(destObj);
        }

        for (int i = 0; i < numMissileLaunchers; i++) {
            MissileLauncher ml = new MissileLauncher(-1, -0.5f);
            models.add(ml);
            missileLaunchers.add(ml);
        }

        for (int i = 0; i < numRings; i++) {
            FallingObject fo = new FallingObject("ring");
            models.add(fo);
            fallingObjects.add(fo);
        }

        for (int i = 0; i < numCubes; i++) {
            FallingObject fo = new FallingObject("cube");
            models.add(fo);
            fallingObjects.add(fo);
        }

        //create array list of different vortex type strings
        ArrayList<String> vortexTypes = new ArrayList<>(numVortexes);
        for (int i = 0; i < numRingVortexes; i++) {
            vortexTypes.add("ring");
        }
        for (int i = 0; i < numCubeVortexes; i++) {
            vortexTypes.add("cube");
        }

        //numVortexes=2 -> x1=-0.5
        for (int i = 0; i < numVortexes; i++) {
            generateVortex(vortexTypes.get(i), i, 3, false);
        }

        for (int i = 0; i < numFallingObjects + numMissileLaunchers; i++) {
            Explosion explosion = new Explosion();
            models.add(explosion);
            explosions.add(explosion);
        }

        //sparkler = new Sparkler();
        //models.add(sparkler);

        positionsInitialized = false;

        //keeps object matrices from reinitializing
        this.fan.pauseGame();
        this.fan.resumeGame();
        this.dispenser.pauseGame();
        this.dispenser.resumeGame();
        this.background.pauseGame();
        this.background.resumeGame();

        Log.e("pause", "constructor mode");
    }

    @Override
    protected void updateModelPositions() {
        int modelCount = 0;

        background.updatePosition(0, 0);

        dispenser.updatePosition(0, 0);

        wormhole.updatePosition(0, 0);

        updateMissileLaunchers();

        for (Vortex v : vortexes) {
            v.updatePosition(0, 0);
        }

        updateRicochetObstacles();

        updateDestructiveObstacles();

        for (FallingObject falObj : fallingObjects) {

            if (fallingObjectInteractions(falObj, modelCount, true)) {
                numLives--;
            }

            if (falObj.isCollected()) {
                falObj.setCollected(false);
                score++;
            }
            modelCount++;
        }
        objectiveFailed = numLives <= 0;
    }

    private void updateRicochetObstacles() {
        int modelCount = 0;
        for (RicochetObstacle o : obstacles) {
            missileInteraction(o);
            if (o.isOffscreen()) {
                models.remove(o);
                float pos[] = generateRandomLocation();
                o = new RicochetObstacle(pos[0], pos[1]);
                obstacles.set(modelCount, o);
                o.enableGraphics(graphicsData);
                o.initializeMatrices(viewMatrix, projectionMatrix, lightPosInEyeSpace);
                models.add(o);
            } else if (!wormholeInteraction(wormhole, o)) {
                o.updatePosition(0, 0);
            }
            modelCount++;
        }
    }

    private void updateDestructiveObstacles() {
        int modelCount = 0;
        for (DestructiveObstacle h : hazards) {
            missileInteraction(h);
            if (h.isOffscreen()) {
                models.remove(h);
                float pos[] = generateRandomLocation();
                float x;
                if (Math.random() > 0.5) {
                    x = -0.56f;//Border.XLEFT;
                } else {
                    x = 0.56f;//Border.XRIGHT;
                }
                h = new SpikeStrip(x, pos[1]);
                hazards.set(modelCount, h);
                h.enableGraphics(graphicsData);
                h.initializeMatrices(viewMatrix, projectionMatrix, lightPosInEyeSpace);
                models.add(h);
            } else {
                h.updatePosition(0, 0);
            }
            modelCount++;
        }
    }

    private void updateMissileLaunchers() {
        float xForce;
        float yForce;
        int modelCount = 0;
        for (MissileLauncher ml : missileLaunchers) {
            xForce = 0;
            yForce = 0;
            Missile m = ml.getMissile();
            if (ml.isOffscreen()) {
                float pos[] = generateRandomLocation();
                float x;
                if (Math.random() > 0.5) {
                    x = -0.44f;//Border.XLEFT;
                } else {
                    x = 0.44f;//Border.XRIGHT;
                }
                ml = new MissileLauncher(x, pos[1]);
                missileLaunchers.set(modelCount, ml);
                ml.enableGraphics(graphicsData);
                ml.initializeMatrices(viewMatrix, projectionMatrix, lightPosInEyeSpace);
                models.add(ml);
                modelCount++;
            }
            if (!m.isOffscreen()) {
                if (!wormholeInteraction(wormhole, m)) {
                    if (windInteraction(m)) {
                        xForce = fan.getWind().getxForce();
                        yForce = fan.getWind().getyForce();
                    }
                    ml.updatePosition(xForce, yForce);
                }
            }
        }
    }

    private float[] generateRandomLocation() {
        Random rand = new Random();
        //screen dimensions: -0.5 <= x <= 0.5, -1 <= y <= 1
        //five x locations
        int numXLocations = 5;
        float cellWidth = (Border.XRIGHT - Border.XLEFT) / (float) numXLocations;

        int numYLocations = 8;
        //eight y locations
        float cellHeight = (Border.YTOP - Border.YBOTTOM) / (float) numYLocations;

        if (xLocations.isEmpty()) {
            for (int i = 0; i < numXLocations; i++) {
                xLocations.add(i);
            }
        }

        if (yLocations.isEmpty()) {
            for (int i = 0; i < numYLocations; i++) {
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

    @Override
    public void handleTouchDrag(MotionEvent event, float x, float y) {

        final int action = MotionEventCompat.getActionMasked(event);
        rotationDetector.onTouchEvent(event);
        if (rotationDetector.isRotating()) {
            Log.e("rotation", "angle " + rotationDetector.getAngle());
            fan.updateFingerRotation(rotationDetector.getAngle());
        } else {
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
                        break;
                }
            }
        }
    }

    public int getNumLives() {
        return numLives;
    }

    public int getScore() {
        return score;
    }

    private ArrayList<Integer> xLocations = new ArrayList<>();
    private ArrayList<Integer> yLocations = new ArrayList<>();

    private int numLives = 3;
    private int score = 0;

    @Override
    public boolean OnRotation(RotationGestureDetector rotationDetector) {
        return false;
    }

    private RotationGestureDetector rotationDetector;
}
