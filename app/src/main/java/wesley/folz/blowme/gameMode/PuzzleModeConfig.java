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
import wesley.folz.blowme.graphics.effects.Explosion;
import wesley.folz.blowme.graphics.models.DestructiveObstacle;
import wesley.folz.blowme.graphics.models.FallingObject;
import wesley.folz.blowme.graphics.models.Fan;
import wesley.folz.blowme.graphics.models.Model;
import wesley.folz.blowme.graphics.models.RicochetObstacle;
import wesley.folz.blowme.graphics.models.Vortex;
import wesley.folz.blowme.ui.GamePlaySurfaceView;
import wesley.folz.blowme.ui.RotationGestureDetector;
import wesley.folz.blowme.util.GameModeUtilities;
import wesley.folz.blowme.util.Physics;

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
    public void initializeGameObjects() {
        try {
            JSONObject jsonObject = new JSONObject(GameModeUtilities.readAsset(
                    "level_config/puzzle/" + level + ".json"));
            JSONArray jsonArray = jsonObject.getJSONArray("Models");
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jso = jsonArray.getJSONObject(i);
                switch (jso.getString("model")) {
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
                        DestructiveObstacle destructiveObstacle = new DestructiveObstacle(desPos[0],
                                desPos[1]);
                        models.add(destructiveObstacle);
                        hazards.add(destructiveObstacle);
                        break;

                    case "Vortex":
                        Vortex v = new Vortex(jso.getString("type"), jso.getInt("xPos"));
                        vortexes.add(v);
                        models.add(v);
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
        background.updatePosition(0, 0);
        for (Vortex v : vortexes) {
            v.updatePosition(0, 0);
        }

        boolean objectEffected;

        for (FallingObject falObj : fallingObjects) {
            float xForce = 0;
            float yForce = 0;

            objectEffected = false;
            Explosion objectExplosion = explosions.get(explosionIndex);

            if (!falObj.isOffscreen()) {
                for (DestructiveObstacle h : hazards) {
                    if (Physics.isCollision(h.getBounds(), falObj.getBounds())) {
                        objectExplosion.reinitialize(falObj.getxPos(), falObj.getyPos());
                        falObj.setOffscreen(true);
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

            if (falObj.isOffscreen() && !objectExplosion.isExploding()) {
                objectiveFailed = true;
            }

            int vortexCount = 0;
            for (Vortex vortex : vortexes) {
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
                        falObj.spiralOutOfVortex(vortex);
                    }
                    objectEffected = true;
                    break;
                } else {
                    vortex.setCollecting(false);
                }
                vortexCount++;
            }

            //falling object is being dispensed
            if (falObj.getyPos() > 0.95f && !objectEffected) {
                //falObj.updatePosition(100 * dispenser.getDeltaX(), 0);
                xForce = 100 * dispenser.getDeltaX();
                //objectEffected = true;
            }

            //determine forces due to collisions with obstacles
            falObj.calculateRicochetCollisions(obstacles);

            //calculate wind influence
            for (Fan f : fans) {
                if (Physics.isCollision(f.getWind().getBounds(), falObj.getBounds())
                        && !objectEffected) {
                    Physics.calculateWindForce(f.getWind(), falObj);
                    //Log.e("wind", "xforce " + fan.getWind().getxForce() + " yforce " + fan
                    // .getWind().getyForce());


                    //Log.e("mode", "wind collision");
                    //falObj.updatePosition(fan.getWind().getxForce(), fan.getWind()
                    // .getyForce());
                    xForce += f.getWind().getxForce();
                    yForce += f.getWind().getyForce();
                    //objectEffected = true;
                }
            }
            if (!falObj.isSpiraling()) {
                falObj.updatePosition(xForce, yForce);
            }
            if (falObj.isCollected()) {
                falObj.setCollected(false);
                if (numObjectsRemaining > 0) {
                    numObjectsRemaining--;
                }

                objectiveComplete = numObjectsRemaining == 0;
            }
        }
        for (Explosion e : explosions) {
            if (e.isExploding()) {
                e.updatePosition(0, 0);
            }
        }

        //line.updatePosition(0, 0);
    }

    @Override
    protected void drawModels() {
        background.draw();
        for (Fan f : fans) {
            f.draw();
        }

        dispenser.draw();

        for (RicochetObstacle o : obstacles) {
            o.draw();
        }
        for (DestructiveObstacle h : hazards) {
            h.draw();
        }

        for (FallingObject falObj : fallingObjects) {
            if (!falObj.isOffscreen()) {
                falObj.draw();
            }
        }

        for (Vortex v : vortexes) {
            v.draw();
        }

        for (Explosion e : explosions) {
            if (e.isExploding()) {
                e.draw();
            }
        }
        //line.draw();
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
            float glY = -1.0f * (y - 1) / 2.0f;

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

    private ArrayList<Fan> fans;

    private boolean puzzleStarted;

    private Timer timer;

    @Override
    public boolean OnRotation(RotationGestureDetector rotationDetector) {
        return false;
    }

    private RotationGestureDetector rotationDetector;
}
