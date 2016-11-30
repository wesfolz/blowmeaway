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
        positionsInitialized = false;
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
/*
        timer = new CountDownTimer(timeInterval * 1000, 1000)
        {
            public void onTick(long millisUntilFinished)
            {
                timeLeft = millisUntilFinished / 1000;
                //game is complete
                if (isObjectiveComplete())
                {
                    timer.cancel(); //stop timer
                }
            }

            public void onFinish()
            {
                //finish activity
                objectiveFailed = !isObjectiveComplete();
                //timerView.setText("done!");
            }
        }.start();

        */
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

    public long getTimeLeft()
    {
        return timeLeft;
    }

    private Timer timer;

    private long timeLeft;

}
