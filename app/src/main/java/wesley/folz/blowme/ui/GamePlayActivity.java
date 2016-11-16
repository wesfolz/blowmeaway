package wesley.folz.blowme.ui;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ConfigurationInfo;
import android.graphics.Point;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.PopupWindow;
import android.widget.TextView;

import wesley.folz.blowme.R;
import wesley.folz.blowme.gamemode.ActionModeConfig;
import wesley.folz.blowme.gamemode.ModeConfig;

public class GamePlayActivity extends Activity
{
    @Override
    protected void onCreate( Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_game_play );
        getWindow().addFlags( WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON | WindowManager
                .LayoutParams.FLAG_FULLSCREEN );

        //make window fullscreen
        final View decorView = getWindow().getDecorView();
        decorView.setOnSystemUiVisibilityChangeListener( new View
                .OnSystemUiVisibilityChangeListener()
        {
            @Override
            public void onSystemUiVisibilityChange( int visibility )
            {
                decorView.setSystemUiVisibility( View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY );
            }
        } );

        cubes = (TextView) findViewById(R.id.cubeTextView);
        rings = (TextView) findViewById(R.id.ringTextView);
        timerView = (TextView) this.findViewById(R.id.timerTextView);
        //surfaceView = new GLSurfaceView( this );
        surfaceView = (GamePlaySurfaceView) findViewById(R.id.surfaceView);

        // Check if the system supports OpenGL ES 2.0.
        final ActivityManager activityManager = (ActivityManager) getSystemService( Context
                .ACTIVITY_SERVICE );
        final ConfigurationInfo configurationInfo = activityManager.getDeviceConfigurationInfo();
        final boolean supportsEs2 = configurationInfo.reqGlEsVersion >= 0x20000;

        Point p = new Point();
        getWindowManager().getDefaultDisplay().getSize( p );
        WIDTH = p.x;
        HEIGHT = p.y;

        //Log.e( "blowme", "WIDTH " + WIDTH + " HEIGHT " + HEIGHT );

        gameMode = new ActionModeConfig();

        if( supportsEs2 )
        {
            // Request an OpenGL ES 2.0 compatible context.
            surfaceView.setEGLContextClientVersion( 2 );

            // Set the renderer to our demo renderer, defined below.
            surfaceView.setRenderer(new GamePlayRenderer(gameMode));
            //surfaceView.setRenderMode( GLSurfaceView.RENDERMODE_WHEN_DIRTY );
        }
        else
        {
            // This is where you could create an OpenGL ES 1.x compatible
            // renderer if you wanted to support both ES 1 and ES 2.
            return;
        }
        //TODO: Possibly have fan constantly move in an a ellipse, user taps to change direction?
        //or user taps and holds and fan moves towards their finger?
        surfaceView.setOnTouchListener( new View.OnTouchListener()
        {
            @Override
            public boolean onTouch( View v, MotionEvent event )
            {
                final int action = MotionEventCompat.getActionMasked( event );
                //multiply by 2 because OpenGL coordinates go [-1,1] whereas screeen coordinates only go [0,1]
                float x = 2 * event.getX() / WIDTH;
                float y = 2 * event.getY() / HEIGHT;

                gameMode.handleTouchDrag(action, x, y);

                return true;
            }
        } );

        startTiming(10);
    }

    protected void onPauseButtonClicked(View pauseButton)
    {
        Log.e("blowme", "pause");
        if (!surfaceView.getRenderer().isPaused())
        {
            timer.cancel();
            View pauseView = getLayoutInflater().inflate(R.layout.pause_window_layout, null);
            pauseWindow = new PopupWindow(pauseView, ViewPager.LayoutParams.WRAP_CONTENT, ViewPager.LayoutParams.WRAP_CONTENT);
            surfaceView.pauseGame();
            pauseWindow.showAtLocation(pauseView, Gravity.CENTER, 10, 10);
        }
    }

    protected void onResumeButtonClicked(View resumeButton)
    {
        pauseWindow.dismiss();
        surfaceView.resumeGame();
        startTiming(timeLeft);
    }

    protected void onExitGamePlayButtonClicked(View exitButton)
    {
        this.finish();
    }

    protected void onReplayGameButtonClicked(View replayButton)
    {
        resultsWindow.dismiss();
        this.recreate();
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        surfaceView.onPause();
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        surfaceView.onResume();
    }

    @Override
    public void onWindowFocusChanged( boolean hasFocus )
    {
        super.onWindowFocusChanged( hasFocus );
        if( hasFocus )
        {
            //make window fullscreen
            getWindow().getDecorView().setSystemUiVisibility( View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY );
        }
    }

    public void startTiming(long timeInterval)
    {
        timer = new CountDownTimer(timeInterval * 1000, 1000)
        {
            public void onTick(long millisUntilFinished)
            {
                timeLeft = millisUntilFinished / 1000;
                timerView.setText(Long.toString(timeLeft));
                cubes.setText(Integer.toString(gameMode.getNumCubesRemaining()));
                rings.setText(Integer.toString(gameMode.getNumRingsRemaining()));
                //game is complete
                if (gameMode.isObjectiveComplete())
                {
                    timer.cancel(); //stop timer
                    displayGameResults(true); //display results
                }
            }

            public void onFinish()
            {
                //finish activity
                displayGameResults(gameMode.isObjectiveComplete());
                //timerView.setText("done!");
            }
        }.start();
    }

    private void displayGameResults(boolean objectiveComplete)
    {
        View resultsView = getLayoutInflater().inflate(R.layout.results_window_layout, null);
        resultsWindow = new PopupWindow(resultsView, ViewPager.LayoutParams.WRAP_CONTENT, ViewPager.LayoutParams.WRAP_CONTENT);
        TextView resultsText = (TextView) resultsWindow.getContentView().findViewById(R.id.resultsTextView);
        if (objectiveComplete)
        {
            resultsText.setText("Success!!");
        }
        else
        {
            resultsText.setText("Failure!!");
        }
        surfaceView.pauseGame();
        resultsWindow.showAtLocation(resultsView, Gravity.CENTER, 10, 10);
    }


    /**
     * View where OpenGL objects are drawn
     */
    private GamePlaySurfaceView surfaceView;

    private PopupWindow pauseWindow;

    private PopupWindow resultsWindow;

    public static final float X_EDGE_POSITION = 0.35f;

    public static final float Y_EDGE_POSITION = 0.7f;

    private int WIDTH;

    private int HEIGHT;

    private boolean touchActionStarted = true;

    private CountDownTimer timer;

    private long timeLeft = 0;

    private ModeConfig gameMode;

    private TextView timerView;

    private TextView cubes;

    private TextView rings;

}
