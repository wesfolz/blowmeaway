package wesley.folz.blowme.gamemode;

import android.support.v4.view.MotionEventCompat;
import android.util.Log;
import android.view.MotionEvent;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import wesley.folz.blowme.graphics.Border;
import wesley.folz.blowme.graphics.Wormhole;
import wesley.folz.blowme.graphics.effects.Explosion;
import wesley.folz.blowme.graphics.models.DestructiveObstacle;
import wesley.folz.blowme.graphics.models.FallingObject;
import wesley.folz.blowme.graphics.models.Fan;
import wesley.folz.blowme.graphics.models.Missile;
import wesley.folz.blowme.graphics.models.MissileLauncher;
import wesley.folz.blowme.graphics.models.Model;
import wesley.folz.blowme.graphics.models.RicochetObstacle;
import wesley.folz.blowme.graphics.models.SpikeStrip;
import wesley.folz.blowme.graphics.models.Vortex;
import wesley.folz.blowme.ui.GamePlaySurfaceView;
import wesley.folz.blowme.ui.RotationGestureDetector;
import wesley.folz.blowme.util.GameModeUtilities;

/**
 * Created by Wesley on 9/24/2016.
 */

public class PuzzleModeConfig extends ModeConfig implements
        RotationGestureDetector.OnRotationGestureListener
{
    public PuzzleModeConfig(String level, ModeConfig mode, GamePlaySurfaceView surfaceView) {
        this.level = level;
        models = new ArrayList<>();
        obstacles = new ArrayList<>();
        fallingObjects = new ArrayList<>();
        explosions = new ArrayList<>();
        vortexes = new ArrayList<>();
        hazards = new ArrayList<>();
        fans = new ArrayList<>();
        missileLaunchers = new ArrayList<>();

        initializeFromExistingMode(mode, null);
        //Log.e("json", "fanx " + fan.getxPos() + " fany " + fan.getyPos());

        initializeGameObjects();

        for (Model m : models) {
            m.initializeMatrices(viewMatrix, perspectiveMatrix, orthographicMatrix,
                    lightPosInEyeSpace);
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
    public void initializeGameObjects() {
        try {
            JSONObject jsonObject = new JSONObject(GameModeUtilities.readAsset(
                    "level_config/puzzle/" + level + ".json"));
            JSONArray jsonArray = jsonObject.getJSONArray("Models");
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jso = jsonArray.getJSONObject(i);
                switch (jso.getString("model")) {
                    case "LevelParams":
                        numVortexes = jso.getInt("numVortexes");
                        break;
                    case "RicochetObstacle":
                        float pos[] = generateObstacleLocation(jso.getInt("xPos"),
                                jso.getInt("yPos"));
                        RicochetObstacle obstacle = new RicochetObstacle(pos[0], pos[1]);
                        models.add(obstacle);
                        obstacles.add(obstacle);
                        break;

                    case "DestructiveObstacle":
                        float desPos[] = generateObstacleLocation(0, jso.getInt("yPos"));
                        desPos[0] = (float) jso.getInt("xPos") * 0.56f;
                        DestructiveObstacle destructiveObstacle = new SpikeStrip(desPos[0],
                                desPos[1]);
                        models.add(destructiveObstacle);
                        hazards.add(destructiveObstacle);
                        break;

                    case "Wormhole":
                        float pos1[] = generateObstacleLocation(jso.getInt("x1"),
                                jso.getInt("y1"));
                        float pos2[] = generateObstacleLocation(jso.getInt("x2"),
                                jso.getInt("y2"));
                        wormhole = new Wormhole(pos1[0], pos1[1], pos2[0], pos2[1]);
                        wormhole.setBackground(background);
                        models.add(wormhole);
                        break;

                    case "MissileLauncher":
                        float mlPos[] = generateObstacleLocation(0, jso.getInt("yPos"));
                        mlPos[0] = (float) jso.getInt("xPos");
                        MissileLauncher ml = new MissileLauncher(mlPos[0], mlPos[1],
                                (float) jso.getDouble("pathTime"));
                        ml.setMove(false);
                        ml.setInitialAngle((float) jso.getDouble("angle"));
                        models.add(ml);
                        missileLaunchers.add(ml);
                        break;

                    case "Vortex":
                        generateVortex(jso.getString("type"), jso.getInt("index"),
                                jso.getInt("capacity"), false);
                        break;

                    case "FallingObject":
                        numObjectsRemaining++;
                        FallingObject fo = new FallingObject(jso.getString("type"));
                        models.add(fo);
                        fallingObjects.add(fo);
                        break;

                    case "Dispenser":
                        //dispenser = new Dispenser();
                        dispenser.setTargetX(jso.getInt("xPos"));
                        //models.add(dispenser);
                        break;

                    case "Background":
                        //background = new Background();
                        //models.add(background);
                        break;

                    case "Fan":
                        numFans++;
                        if (numFans > 1) {
                            Fan f = new Fan();
                            models.add(f);
                            fans.add(f);
                        } else {
                            fans.add(fan);
                        }
                        break;
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        for (int i = 0; i < numObjectsRemaining; i++) {
            Explosion explosion = new Explosion();
            models.add(explosion);
            explosions.add(explosion);
        }

        //Log.e("json", "num vortexes " + numVortexes);

        positionsInitialized = false;

        //keeps object matrices from reinitializing
        this.fan.pauseGame();
        this.fan.resumeGame();
        this.dispenser.pauseGame();
        this.dispenser.resumeGame();
        this.background.pauseGame();
        this.background.resumeGame();

        //line =  new Line();
        //models.add(line);
    }

    @Override
    protected void initializationRoutine() {
        boolean initialized = true;
        for (Model m : models) {
            initialized &= m.initializationRoutine();
        }
        fanReadyToMove = initialized;
        positionsInitialized = initialized & puzzleStarted;
        if (positionsInitialized) {
            startTiming();
        }
    }

    @Override
    protected void updateModelPositions() {
        //dispenser.updatePosition(0, 0);

        if (wormhole != null) {
            wormhole.updatePosition(0, 0);
        }

        background.updatePosition(0, 0);
        for (Vortex v : vortexes) {
            v.updatePosition(0, 0);
        }

        updateMissileLaunchers();

        int modelCount = 0;
        for (FallingObject falObj : fallingObjects) {

            if (fallingObjectInteractions(falObj, modelCount, false)) {
                objectiveFailed = true;
            }

            if (falObj.isCollected()) {
                falObj.setCollected(false);
                if (numObjectsRemaining > 0) {
                    numObjectsRemaining--;
                }

                objectiveComplete = numObjectsRemaining == 0;
            }
        }
    }

    private void startTiming() {
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                timeRemaining--;
                if (isObjectiveComplete()) {
                    timer.cancel();
                } else if (timeRemaining <= 0) {
                    objectiveFailed = true;// !isObjectiveComplete();
                    timer.cancel();
                }

            }
        }, 0, 1000);
    }

    private void updateMissileLaunchers() {
        float xForce;
        float yForce;

        for (MissileLauncher ml : missileLaunchers) {
            ml.getFuse().setIgnited(true);
            xForce = 0;
            yForce = 0;
            Missile m = ml.getMissile();
            if (!m.isOffscreen()) {
                if (!wormholeInteraction(wormhole, m)) {
                    for (Fan f : fans) {
                        if (windInteraction(m, f)) {
                            xForce += f.getWind().getxForce();
                            yForce += f.getWind().getyForce();
                        }
                    }
                    ml.updatePosition(xForce,
                            yForce); //this must not be called if there's a wormhole interaction
                }
            }
        }
    }


    private float[] generateObstacleLocation(int xCell, int yCell) {
        //screen dimensions: -0.5 <= x <= 0.5, -1 <= y <= 1
        //five x locations
        int numXLocations = 5;
        float cellWidth = (Border.XRIGHT - Border.XLEFT) / (float) numXLocations;

        int numYLocations = 8;
        //eight y locations
        float cellHeight = (Border.YTOP - Border.YBOTTOM) / (float) numYLocations;

        float locations[] = new float[2];
        //Border.XLEFT + cellWidth/2 <= locations[0] <= Border.XRIGHT - cellWidth/2
        locations[0] = (float) xCell * cellWidth + cellWidth / 2 + Border.XLEFT;
        // [-1, 1]
        locations[1] = (float) yCell * cellHeight + cellHeight / 2 + Border.YBOTTOM;

        return locations;
    }

    @Override
    public void handleTouchDrag(MotionEvent event, float x, float y) {
        final int action = MotionEventCompat.getActionMasked(event);
        rotationDetector.onTouchEvent(event);
        if (rotationDetector.isRotating()) {
            Log.e("rotation", "angle " + rotationDetector.getAngle());
            fan.updateFingerRotation(rotationDetector.getAngle());
            //      Log.e("rotate", "rotation " + fan.getFingerRotation());
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
                        Log.e("rotate", "no rotation");

                        //Log.e( "blowme", "Move: X " + (event.getRawX() / WIDTH) + " Y " +
                        // (event.getRawY() / HEIGHT) );
                        break;
                }
            }
        }
    }

    @Override
    public void handleFanMovementDown(float x, float y) {
        if (fanReadyToMove) {
            double distance;
            double minDistance = 100;
            float glX = (x - 1) / 2.0f;
            float glY = -1.0f * (y - 1);// / 2.0f;

            for (Fan f : fans) {
                distance = Math.sqrt(
                        (f.getxPos() - glX) * (f.getxPos() - glX) + (f.getyPos() - glY) * (
                                f.getyPos() - glY));
                if (distance < minDistance) {
                    minDistance = distance;
                    fan = f;
                }
            }
            fan.setInitialX(x);
            fan.setInitialY(y);
            initialPositionSet = true;
        }
    }

    public void setPuzzleStarted(boolean puzzleStarted) {
        this.puzzleStarted = puzzleStarted;
    }

    private int numObjectsRemaining = 0;
    private int numFans = 0;

    private boolean puzzleStarted;

    private Timer timer;

    @Override
    public boolean OnRotation(RotationGestureDetector rotationDetector) {
        return false;
    }

    private RotationGestureDetector rotationDetector;
}
