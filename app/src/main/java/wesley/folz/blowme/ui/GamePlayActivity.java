package wesley.folz.blowme.ui;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ConfigurationInfo;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.ViewPager;
import android.transition.Scene;
import android.transition.Transition;
import android.transition.TransitionInflater;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.PopupWindow;
import android.widget.TextView;

import wesley.folz.blowme.R;
import wesley.folz.blowme.gamemode.ActionModeConfig;
import wesley.folz.blowme.gamemode.MenuModeConfig;
import wesley.folz.blowme.gamemode.ModeConfig;

public class GamePlayActivity extends Activity
{

    /*-----------------------------------------Constructors---------------------------------------*/

    /*---------------------------------------Override Methods-------------------------------------*/

    @Override
    protected void onCreate( Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );
        setContentView(R.layout.main_scene_containter_layout);
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

// Create the scene root for the scenes in this app
        sceneRoot = (ViewGroup) findViewById(R.id.scene_root);

// Create the scenes
        mainMenuScene = Scene.getSceneForLayout(sceneRoot, R.layout.activity_main_menu, this);
        gamePlayScene = Scene.getSceneForLayout(sceneRoot, R.layout.activity_game_play, this);

        transitionAnimation =
                TransitionInflater.from(this).
                        inflateTransition(R.transition.fade_transition);

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

        if( supportsEs2 )
        {
            // Request an OpenGL ES 2.0 compatible context.
            surfaceView.setEGLContextClientVersion( 2 );

            menuMode = new MenuModeConfig();//new ActionModeConfig();

            surfaceView.setRenderer(new GamePlayRenderer(menuMode));

            // Set the renderer to our demo renderer, defined below.
            //surfaceView.setRenderMode( GLSurfaceView.RENDERMODE_WHEN_DIRTY );
        }
        else
        {
            // This is where you could create an OpenGL ES 1.x compatible
            // renderer if you wanted to support both ES 1 and ES 2.
            return;
        }
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        surfaceView.onPause();
        if (timerHandler != null) {
            timerHandler.removeCallbacks(timerRunnable);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        surfaceView.onResume();
        //startTiming();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus)
    {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            //make window fullscreen
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
    }

    /*---------------------------------------Public Methods---------------------------------------*/

    /*-------------------------------------Protected Methods--------------------------------------*/

    protected void onPlayButtonClicked(View playButton)
    {
        TransitionManager.go(gamePlayScene, transitionAnimation);
        initializeGameMode();
    }

    /**
     * Resumes game play from a paused state, dismisses pauseWindow
     *
     * @param resumeButton - Resume button in pauseWindow that activated this method
     */
    protected void onResumeButtonClicked(View resumeButton)
    {
        pauseWindow.dismiss();
        surfaceView.resumeGame();
        startTiming();
    }

    /**
     * Pauses game and surface view, launches the pause popup window
     * @param pauseButton - Pause button in game play view that activated this method
     */
    protected void onPauseButtonClicked(View pauseButton)
    {
        Log.e("blowme", "pause");
        if (!surfaceView.getRenderer().isPaused())
        {
            View pauseView = getLayoutInflater().inflate(R.layout.pause_window_layout, null);
            pauseWindow = new PopupWindow(pauseView, ViewPager.LayoutParams.WRAP_CONTENT, ViewPager.LayoutParams.WRAP_CONTENT);
            surfaceView.pauseGame();
            pauseWindow.showAtLocation(pauseView, Gravity.CENTER, 10, 10);
        }
    }

    /**
     * Exits game play and transitions back to menu, dismisses any popup windows
     * @param exitButton - Exit button in pauseWindow that activated this method
     */
    protected void onExitGamePlayButtonClicked(View exitButton)
    {
        if (pauseWindow != null)
        {
            pauseWindow.dismiss();
        }
        if (resultsWindow != null)
        {
            resultsWindow.dismiss();
        }
        timerHandler.removeCallbacks(timerRunnable);
        gameMode.stopGame();
        surfaceView.resumeGame();
        TransitionManager.go(mainMenuScene, transitionAnimation);
        initializeMenuMode();
    }

    /**
     * Reinitializes game parameters, dismisses results window and resumes rendering
     * @param replayButton - Button in results window that activated this method
     */
    protected void onReplayGameButtonClicked(View replayButton)
    {
        resultsWindow.dismiss();
        this.initializeGameMode();
        surfaceView.resumeGame();
    }

    /*--------------------------------------Private Methods---------------------------------------*/

    private void displayGameResults(boolean objectiveComplete) {
        Log.e("diplaygame", "objective complete " + objectiveComplete);
        View resultsView = getLayoutInflater().inflate(R.layout.results_window_layout, null);
        resultsWindow = new PopupWindow(resultsView, ViewPager.LayoutParams.WRAP_CONTENT,
                ViewPager.LayoutParams.WRAP_CONTENT);
        TextView resultsText = (TextView) resultsWindow.getContentView().findViewById(
                R.id.resultsTextView);
        if (objectiveComplete) {
            resultsText.setText("Success!!");
        } else {
            resultsText.setText("Failure!!");
        }
        surfaceView.pauseGame();
        resultsWindow.showAtLocation(resultsView, Gravity.CENTER, 10, 10);
    }

    private void initializeGameMode() {
        gameMode = new ActionModeConfig();
        gameMode.initializeFromExistingMode(menuMode, surfaceView);
        surfaceView.getRenderer().setModeConfig(gameMode);

        cubes = (TextView) findViewById(R.id.cubeTextView);
        rings = (TextView) findViewById(R.id.ringTextView);
        timerView = (TextView) this.findViewById(R.id.timerTextView);

        //TODO: Possibly have fan constantly move in an a ellipse, user taps to change direction?
        //or user taps and holds and fan moves towards their finger?
        surfaceView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                final int action = MotionEventCompat.getActionMasked(event);
                //multiply by 2 because OpenGL coordinates go [-1,1] whereas screeen coordinates
                // only go [0,1]
                float x = 2 * event.getX() / WIDTH;
                float y = 2 * event.getY() / HEIGHT;

                gameMode.handleTouchDrag(action, x, y);

                return true;
            }
        });

        startTiming();
    }

    /**
     * Sets the mode of the renderer to a menuMode and disables the touch listener
     */
    private void initializeMenuMode() {
        surfaceView.getRenderer().setModeConfig(menuMode);
        surfaceView.setOnTouchListener(null);
    }


    public void startTiming()
    {
        timerHandler = new Handler();
        timerRunnable = new Runnable()
        {
            @Override
            public void run()
            {
                Log.e("timing", "timer handler");
                timerView.setText(Long.toString(gameMode.getTimeLeft()));
                cubes.setText(Integer.toString(gameMode.getNumCubesRemaining()));
                rings.setText(Integer.toString(gameMode.getNumRingsRemaining()));

                if (gameMode.isObjectiveComplete() || gameMode.isObjectiveFailed())
                {
                    displayGameResults(gameMode.isObjectiveComplete());
                    timerHandler.removeCallbacks(timerRunnable);
                }
                else
                {
                    timerHandler.postDelayed(this, 500);
                }
            }
        };
        timerHandler.postDelayed(timerRunnable, 0);
    }

    /*--------------------------------------Getters and Setters-----------------------------------*/


    /*----------------------------------------Public Fields---------------------------------------*/

    public static final float X_EDGE_POSITION = 0.35f;

    public static final float Y_EDGE_POSITION = 0.7f;

    /*--------------------------------------Protected Fields--------------------------------------*/

    /*---------------------------------------Private Fields---------------------------------------*/

    /**
     * View where OpenGL objects are drawn
     */
    private GamePlaySurfaceView surfaceView;

    private PopupWindow pauseWindow;

    private PopupWindow resultsWindow;

    private int WIDTH;

    private int HEIGHT;

    private boolean touchActionStarted = true;

    private long timeLeft = 0;

    private ModeConfig menuMode;

    private ActionModeConfig gameMode;

    private TextView timerView;

    private TextView cubes;

    private TextView rings;

    private ViewGroup sceneRoot;

    private Scene mainMenuScene;

    private Scene gamePlayScene;

    private Transition transitionAnimation;

    private Handler timerHandler;

    private Runnable timerRunnable;
}
