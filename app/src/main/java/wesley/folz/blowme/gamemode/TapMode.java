package wesley.folz.blowme.gamemode;

import android.support.v4.view.MotionEventCompat;
import android.util.Log;
import android.view.MotionEvent;

import java.util.ArrayList;
import java.util.Random;

import wesley.folz.blowme.graphics.Border;
import wesley.folz.blowme.graphics.effects.Explosion;
import wesley.folz.blowme.graphics.models.DestructiveObstacle;
import wesley.folz.blowme.graphics.models.FallingObject;
import wesley.folz.blowme.graphics.models.Fan;
import wesley.folz.blowme.graphics.models.Model;
import wesley.folz.blowme.graphics.models.RicochetObstacle;
import wesley.folz.blowme.graphics.models.SpikeStrip;
import wesley.folz.blowme.graphics.models.Vortex;
import wesley.folz.blowme.ui.GamePlaySurfaceView;
import wesley.folz.blowme.util.Physics;

/**
 * Created by Wesley on 1/27/2017.
 */

public class TapMode extends ModeConfig {

    public TapMode(ModeConfig mode, GamePlaySurfaceView surfaceView) {
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
    }

    @Override
    protected void initializeGameObjects() {
        int numRicochetObstacles = 2;
        int numDestructiveObstacles = 2;
        int numFallingObjects = 2;
        int numRings = 1;
        int numCubes = 1;
        int numVortexes = 2;
        int numRingVortexes = 1;
        int numCubeVortexes = 1;
        numCubesRemaining = 1;
        numRingsRemaining = 1;
        models = new ArrayList<>();
        obstacles = new ArrayList<>();
        fallingObjects = new ArrayList<>();
        explosions = new ArrayList<>();
        vortexes = new ArrayList<>();
        hazards = new ArrayList<>();

        //fan = new Fan();
        models.add(fan);

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

        for (int i = 0; i < numFallingObjects; i++) {
            Explosion explosion = new Explosion();
            models.add(explosion);
            explosions.add(explosion);
        }

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
            Vortex v = new Vortex(vortexTypes.get(i),
                    (float) (i + 1) * (2.0f / (numVortexes + 1.0f)) - 1);
            models.add(v);
            vortexes.add(v);
        }

        int side = 1;
        float counter = 0;
        float angle = 0;
        for (int i = 0; i < numFans; i++) {
            if (i >= 5) {
                side = -1;
                counter = i - 5;
                angle = 180;
            }
            //          if(i == 0)
            //          {
            //              fan.setTargets(side*Border.XLEFT, Border.YTOP - counter*0.3f, -65,
            // angle);
            //              fans.add(fan);
            //          }
            //          else {
            Fan f = new Fan(side * Border.XLEFT, Border.YTOP - counter * 0.3f, -65, angle);
            models.add(f);
            fans.add(f);
            //         }
            counter++;
        }

        //dispenser = new Dispenser();
        models.add(dispenser);

        //background = new Background();
        models.add(background);

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
        Log.e("pause", "constructor mode");
    }

    @Override
    protected void updateModelPositions() {
        background.updatePosition(0, 0);

        //fan.updatePosition(0, 0);
        //fan.getWind().updatePosition(0, 0);

        dispenser.updatePosition(0, 0);

        for (Vortex v : vortexes) {
            v.updatePosition(0, 0);
        }

        int modelCount = 0;
        for (RicochetObstacle o : obstacles) {
            if (o.isOffscreen()) {
                models.remove(o);
                float pos[] = generateRandomLocation();
                o = new RicochetObstacle(pos[0], pos[1]);
                obstacles.set(modelCount, o);
                o.enableGraphics(graphicsData);
                o.initializeMatrices(viewMatrix, perspectiveMatrix, orthographicMatrix,
                        lightPosInEyeSpace);
                models.add(o);
            } else {
                o.updatePosition(0, 0);
            }
            modelCount++;
        }

        modelCount = 0;
        for (DestructiveObstacle h : hazards) {
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
                h.initializeMatrices(viewMatrix, perspectiveMatrix, orthographicMatrix,
                        lightPosInEyeSpace);
                models.add(h);
            } else {
                h.updatePosition(0, 0);
            }
            modelCount++;
        }

        boolean objectEffected;
        modelCount = 0;
        for (FallingObject falObj : fallingObjects) {
            float xForce = 0;
            float yForce = 0;

            objectEffected = false;

            Explosion objectExplosion = explosions.get(explosionIndex);

            for (DestructiveObstacle h : hazards) {
                if (Physics.isCollision(h.getBounds(), falObj.getBounds())) {
                    numLives--;
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

            if (falObj.isOffscreen()) {
                try {
                    models.remove(falObj);
                    //falObj = falObj.getClass().getConstructor(float.class).newInstance
                    // (dispenser.getxPos());
                    String type = falObj.getType();
                    falObj = new FallingObject(type, dispenser.getxPos());
                    fallingObjects.set(modelCount, falObj);
                    falObj.enableGraphics(graphicsData);
                    falObj.initializeMatrices(viewMatrix, perspectiveMatrix, orthographicMatrix,
                            lightPosInEyeSpace);
                    models.add(falObj);
                } catch (Exception e) {
                    Log.e("error", e.getMessage());
                }
            }

            int vortexCount = 0;
            for (Vortex vortex : vortexes) {
                //vortex position - falling object position
                if (Physics.isCollision(vortex.getBounds(), falObj.getBounds())
                        || (falObj.isSpiralIn() && vortex.isCollecting()
                        && vortexCount == falObj.getCollectingVortexIndex())) {
                    vortex.setCollecting(true);
                    //falObj.travelOnVector(vortex.getxPos() - falObj.getxPos(), vortex.getyPos()
                    // - falObj.getyPos());
                    falObj.setCollectingVortexIndex(vortexCount);
                    if (vortex.getType().equals(falObj.getType())) {
                        falObj.setSpiralIn(true, vortex);
                    } else {
                        falObj.setSpiralOut(true, vortex);
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
            for (Fan f : fans) {
                if (f.isBlowing()) {
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
            }

            falObj.updatePosition(xForce, yForce);

            if (falObj.isCollected()) {
                falObj.setCollected(false);
                score++;
            }
            modelCount++;
        }
        objectiveFailed = numLives <= 0;

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
                    break;
            }
        }
    }

    @Override
    public void handleFanMovementDown(float x, float y) {
        if (fanReadyToMove) {
            double distance;
            double minDistance = 100;
            //[-0.5, 0.5]
            float glX = (x - 1) / 2.0f;
            //[-1.0, 1.0]
            float glY = -1.0f * (y - 1);// / 2.0f;

            Log.e("touch", "glx " + glX + " gly " + glY + " x " + x + " y " + y);

            for (Fan f : fans) {
                distance = Math.sqrt(
                        (f.getxPos() - glX) * (f.getxPos() - glX) + (f.getyPos() - glY) * (
                                f.getyPos() - glY));
                if (distance < minDistance) {
                    minDistance = distance;
                    fan = f;
                }
            }
            fan.setBlowing(!fan.isBlowing());
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

    private ArrayList<Fan> fans;

    private int numLives = 3;
    private int score = 0;

    private int numFans = 10;

}
