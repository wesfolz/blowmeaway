package wesley.folz.blowme.gamemode;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Wesley on 9/24/2016.
 */

public class ActionModeConfig extends ModeConfig
{
    public ActionModeConfig()
    {
        this("Action1");
    }

    public ActionModeConfig(String level) {
        this.level = level;
        loadLevel();
        initializeGameObjects();
    }

    private void loadLevel() {
        numRicochetObstacles = LevelData.getNumRicochetObstacles().get(level);
        numDestructiveObstacles = LevelData.getNumDestructiveObstacles().get(level);
        numFallingObjects = LevelData.getNumFallingObjects().get(level);
        numRings = LevelData.getNumRings().get(level);
        numCubes = LevelData.getNumCubes().get(level);
        numVortexes = LevelData.getNumVortexes().get(level);
        numRingVortexes = LevelData.getNumRingVortexes().get(level);
        numCubeVortexes = LevelData.getNumCubeVortexes().get(level);
        numCubesRemaining = LevelData.getNumCubesRemaining().get(level);
        numRingsRemaining = LevelData.getNumRingsRemaining().get(level);
        timeLeft = LevelData.getTimeLimit().get(level);
    }

    @Override
    protected void initializationRoutine()
    {
        super.initializationRoutine();
        if (positionsInitialized)
        {
            timeLeft = LevelData.getTimeLimit().get(level);
            startTiming();
        }
    }

    public void startTiming()
    {
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask()
        {
            @Override
            public void run()
            {
                timeLeft--;
                if (isObjectiveComplete())
                {
                    timer.cancel();
                }
                else
                {
                    if (timeLeft <= 0)
                    {
                        objectiveFailed = true;// !isObjectiveComplete();
                        timer.cancel();
                    }
                }

            }
        }, 0, 1000);
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

}
