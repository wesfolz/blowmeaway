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
        super();
        timeLeft = 10;
    }

    @Override
    protected void initializationRoutine()
    {
        super.initializationRoutine();
        if (positionsInitialized)
        {
            timeLeft = 10;
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


    public long getTimeLeft()
    {
        return timeLeft;
    }

    private Timer timer;

    private long timeLeft;

}
