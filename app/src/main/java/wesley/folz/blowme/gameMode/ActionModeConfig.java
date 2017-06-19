package wesley.folz.blowme.gamemode;

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
import wesley.folz.blowme.graphics.models.Model;
import wesley.folz.blowme.graphics.models.RicochetObstacle;
import wesley.folz.blowme.graphics.models.Vortex;
import wesley.folz.blowme.ui.GamePlaySurfaceView;
import wesley.folz.blowme.util.GameModeUtilities;

/**
 * Created by Wesley on 9/24/2016.
 */

public class ActionModeConfig extends ModeConfig
{
    public ActionModeConfig(String level, ModeConfig mode, GamePlaySurfaceView surfaceView) {
        this.level = level;
        models = new ArrayList<>();
        obstacles = new ArrayList<>();
        fallingObjects = new ArrayList<>();
        explosions = new ArrayList<>();
        vortexes = new ArrayList<>();
        hazards = new ArrayList<>();
        obstaclesInQueue = new ArrayList<>();
        hazardsInQueue = new ArrayList<>();

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
    }

    @Override
    public void initializeGameObjects() {
        try {
            JSONObject jsonObject = new JSONObject(GameModeUtilities.readAsset(
                    "level_config/action/" + level + ".json"));
            JSONArray jsonArray = jsonObject.getJSONArray("Models");
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jso = jsonArray.getJSONObject(i);
                switch (jso.getString("model")) {
                    case "LevelParams":
                        timeRemaining = jso.getInt("time");
                        numCubesRemaining = jso.getInt("numCubes");
                        numRingsRemaining = jso.getInt("numRings");
                        break;

                    case "RicochetObstacle":
                        float pos[] = generateObstacleLocation(jso.getInt("xPos"),
                                jso.getInt("yPos"));
                        RicochetObstacle obstacle = new RicochetObstacle(pos[0], pos[1]);
                        obstacle.setTime(jso.getInt("time"));
                        if (obstacle.getTime() == 0) {
                            obstacles.add(obstacle);
                        } else {
                            obstaclesInQueue.add(obstacle);
                        }
                        models.add(obstacle);
                        break;

                    case "DestructiveObstacle":
                        float desPos[] = generateObstacleLocation(0, jso.getInt("yPos"));
                        desPos[0] = (float) jso.getInt("xPos") * 0.56f;
                        DestructiveObstacle destructiveObstacle = new DestructiveObstacle(desPos[0],
                                desPos[1]);
                        destructiveObstacle.setTime(jso.getInt("time"));
                        if (destructiveObstacle.getTime() == 0) {
                            hazards.add(destructiveObstacle);
                        } else {
                            hazardsInQueue.add(destructiveObstacle);
                        }
                        models.add(destructiveObstacle);
                        break;

                    case "Vortex":
                        Vortex v = new Vortex(jso.getString("type"), (float) jso.getDouble("xPos"));
                        vortexes.add(v);
                        models.add(v);
                        break;

                    case "FallingObject":
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
                        break;
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        for (int i = 0; i < (numCubesRemaining + numRingsRemaining); i++) {
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
    protected void initializationRoutine()
    {
        super.initializationRoutine();
        if (positionsInitialized)
        {
            startTiming();
        }
    }

    @Override
    protected void updateModelPositions() {
        background.updatePosition(0, 0);

        dispenser.updatePosition(0, 0);

        for (Vortex v : vortexes) {
            v.updatePosition(0, 0);
        }

        for (int i = 0; i < obstaclesInQueue.size(); i++) {
            RicochetObstacle o = obstaclesInQueue.get(i);
            if (o.getTime() <= elapsedTime) {
                obstacles.add(o);
                obstaclesInQueue.remove(o);
            }
        }

        for (int i = 0; i < hazardsInQueue.size(); i++) {
            DestructiveObstacle d = hazardsInQueue.get(i);
            if (d.getTime() <= elapsedTime) {
                hazards.add(d);
                hazardsInQueue.remove(d);
            }
        }

        for (int i = 0; i < obstacles.size(); i++) {
            RicochetObstacle o = obstacles.get(i);
            if (o.isOffscreen()) {
                models.remove(o);
                obstacles.remove(o);
            } else {
                o.updatePosition(0, 0);
            }
        }

        for (int i = 0; i < hazards.size(); i++) {
            DestructiveObstacle d = hazards.get(i);
            if (d.isOffscreen()) {
                models.remove(d);
                hazards.remove(d);
            } else {
                d.updatePosition(0, 0);
            }
        }

        boolean objectEffected;
        int modelCount = 0;
        for (FallingObject falObj : fallingObjects) {
            float xForce = 0;
            float yForce = 0;

            destructionInteraction(falObj);

            falObj = offscreenInteraction(falObj, modelCount);

            objectEffected = vortexInteraction(falObj);

            xForce = dispenseInteraction(falObj, objectEffected);

            //determine forces due to collisions with obstacles
            falObj.calculateRicochetCollisions(obstacles);

            if (windInteraction(falObj, objectEffected)) {
                xForce = fan.getWind().getxForce();
                yForce = fan.getWind().getyForce();
            }

            if (!falObj.isSpiraling()) {
                falObj.updatePosition(xForce, yForce);
            }
            if (falObj.isCollected()) {
                falObj.setCollected(false);
                switch (falObj.getType()) {
                    case "cube":
                        if (numCubesRemaining > 0) {
                            numCubesRemaining--;
                        }
                        break;
                    case "ring":
                        if (numRingsRemaining > 0) {
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

    private void startTiming()
    {
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask()
        {
            @Override
            public void run()
            {
                elapsedTime++;
                timeRemaining--;
                if (isObjectiveComplete())
                {
                    timer.cancel();
                }
                else
                {
                    if (timeRemaining <= 0)
                    {
                        objectiveFailed = true;// !isObjectiveComplete();
                        timer.cancel();
                    }
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
        locations[1] = (float) yCell * cellHeight + cellHeight / 2 + 3 * Border.YBOTTOM;

        return locations;
    }

    @Override
    public void resumeGame()
    {
        super.resumeGame();
        if (positionsInitialized)
        {
            startTiming();
        }
    }

    @Override
    public void pauseGame()
    {
        super.pauseGame();
        if (timer != null)
        {
            timer.cancel();
        }
    }

    @Override
    public void stopGame() {
        super.stopGame();
        if (timer != null) {
            timer.cancel();
        }
    }

    private Timer timer;

    private ArrayList<RicochetObstacle> obstaclesInQueue;

    private ArrayList<DestructiveObstacle> hazardsInQueue;

    private int elapsedTime = 0;

}
