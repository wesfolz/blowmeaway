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
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.PopupWindow;
import android.widget.TextView;

import java.util.Locale;

import wesley.folz.blowme.R;
import wesley.folz.blowme.gamemode.ActionModeConfig;
import wesley.folz.blowme.gamemode.EndlessModeConfig;
import wesley.folz.blowme.gamemode.MenuModeConfig;
import wesley.folz.blowme.gamemode.ModeConfig;
import wesley.folz.blowme.gamemode.PuzzleModeConfig;
import wesley.folz.blowme.util.ImageAdapter;

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
        levelSelectScene = Scene.getSceneForLayout(sceneRoot, R.layout.level_select_layout, this);
        puzzleGamePlayScene = Scene.getSceneForLayout(sceneRoot, R.layout.puzzle_game_play_layout,
                this);
        endlessGamePlayScene = Scene.getSceneForLayout(sceneRoot, R.layout.endless_game_play_layout,
                this);

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
        Log.e("appflow", "onPause");
        surfaceView.onPause();

        if (gameHandler != null) {
            gameHandler.removeCallbacks(gameRunnable);
        }
        if (pauseWindow != null) {
            pauseWindow.dismiss();
        }
        if (resultsWindow != null) {
            resultsWindow.dismiss();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.e("appflow", "onResume");
        surfaceView.onResume();
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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        //TODO: go back to last scene, or exit if in main scene
    }

    /*---------------------------------------Public Methods---------------------------------------*/

    /*-------------------------------------Protected Methods--------------------------------------*/

    protected void onActionButtonClicked(View actionButton)
    {
        TransitionManager.go(levelSelectScene, transitionAnimation);
        TextView modeTitle = (TextView) findViewById(R.id.modeBannerText);
        modeTitle.setText(getResources().getString(R.string.action_mode));
        GridView gridview = (GridView) findViewById(R.id.level_grid);
        gridview.setAdapter(new ImageAdapter(this));

        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v,
                    int position, long id) {
                TransitionManager.go(gamePlayScene, transitionAnimation);
                initializeActionMode("action" + (position + 1));
            }
        });
    }

    protected void onEndlessButtonClicked(View endlessButton) {
        TransitionManager.go(endlessGamePlayScene, transitionAnimation);
        //TransitionManager.go(gamePlayScene, transitionAnimation);
        initializeEndlessMode();
    }

    protected void onPuzzleButtonClicked(View endlessButton) {
        TransitionManager.go(levelSelectScene, transitionAnimation);
        TextView modeTitle = (TextView) findViewById(R.id.modeBannerText);
        modeTitle.setText(getResources().getString(R.string.puzzle_mode));
        GridView gridview = (GridView) findViewById(R.id.level_grid);
        gridview.setAdapter(new ImageAdapter(this));

        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v,
                    int position, long id) {
                TransitionManager.go(puzzleGamePlayScene, transitionAnimation);
                initializePuzzleMode("puzzle" + (position + 1));
            }
        });
        //initializePuzzleMode();
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
        if (gameHandler != null) {
            gameHandler.removeCallbacks(gameRunnable);
        }

        if (gameMode != null) {
            gameMode.stopGame();
        }
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
        switch (currentGameMode) {
            case "action":
                this.initializeActionMode(gameMode.getLevel());
                break;
            case "endless":
                this.initializeEndlessMode();
                break;
            case "puzzle":
                this.initializePuzzleMode(gameMode.getLevel());
                break;
        }
        surfaceView.resumeGame();
    }

    protected void onStartPuzzleButtonClicked(View startPuzzleButton) {
        PuzzleModeConfig puzzle = (PuzzleModeConfig) gameMode;
        puzzle.setPuzzleStarted(true);
        findViewById(R.id.startPuzzleButton).setVisibility(View.INVISIBLE);
        surfaceView.setOnTouchListener(null);
    }

    /*--------------------------------------Private Methods---------------------------------------*/

    private void displayGameResults(boolean objectiveComplete) {
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
        //gameMode.initializeFromExistingMode(menuMode, surfaceView);
        surfaceView.getRenderer().setModeConfig(gameMode);

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
    }

    private void initializeActionMode(String level) {
        currentGameMode = "action";
        gameMode = new ActionModeConfig(level, menuMode, surfaceView);

        cubes = (TextView) findViewById(R.id.cubeTextView);
        rings = (TextView) findViewById(R.id.ringTextView);
        timerView = (TextView) this.findViewById(R.id.timerTextView);

        initializeGameMode();

        startActionModeHandler();
    }

    private void initializeEndlessMode() {
        currentGameMode = "endless";
        gameMode = new EndlessModeConfig(menuMode, surfaceView);

        numLivesView = (TextView) findViewById(R.id.numLivesTextView);
        scoreTextView = (TextView) findViewById(R.id.timerTextView);

        initializeGameMode();
        startEndlessModeHandler();
    }

    private void initializePuzzleMode(String level) {
        currentGameMode = "puzzle";
        gameMode = new PuzzleModeConfig(level, menuMode, surfaceView);
        cubes = (TextView) findViewById(R.id.cubeTextView);
        rings = (TextView) findViewById(R.id.ringTextView);
        timerView = (TextView) this.findViewById(R.id.timerTextVIew);
        findViewById(R.id.startPuzzleButton).setVisibility(View.VISIBLE);
        initializeGameMode();
        startPuzzleModeHandler();
    }

    /**
     * Sets the mode of the renderer to a menuMode and disables the touch listener
     */
    private void initializeMenuMode() {
        //if(gameMode != null)
        //    menuMode.initializeFromExistingMode(gameMode, surfaceView);
        surfaceView.getRenderer().setModeConfig(menuMode);
        surfaceView.setOnTouchListener(null);
    }

    public void startActionModeHandler() {
        gameHandler = new Handler();
        gameRunnable = new Runnable() {
            @Override
            public void run() {
                timerView.setText(
                        String.format(Locale.getDefault(), "%d", gameMode.getTimeRemaining()));
                cubes.setText(
                        String.format(Locale.getDefault(), "%d", gameMode.getNumCubesRemaining()));
                rings.setText(
                        String.format(Locale.getDefault(), "%d", gameMode.getNumRingsRemaining()));

                if (gameMode.isObjectiveComplete() || gameMode.isObjectiveFailed()) {
                    displayGameResults(gameMode.isObjectiveComplete());
                    gameHandler.removeCallbacks(gameRunnable);
                } else {
                    gameHandler.postDelayed(this, 500);
                }
            }
        };
        gameHandler.postDelayed(gameRunnable, 0);
    }

    public void startEndlessModeHandler() {
        gameHandler = new Handler();
        gameRunnable = new Runnable() {
            @Override
            public void run() {
                EndlessModeConfig endlessMode = (EndlessModeConfig) gameMode;
                scoreTextView.setText(
                        String.format(Locale.getDefault(), "%d", endlessMode.getScore()));
                numLivesView.setText(
                        String.format(Locale.getDefault(), "%d", endlessMode.getNumLives()));
                if (gameMode.isObjectiveFailed()) {
                    displayGameResults(false);
                    gameHandler.removeCallbacks(gameRunnable);
                } else {
                    gameHandler.postDelayed(this, 500);
                }
            }
        };
        gameHandler.postDelayed(gameRunnable, 0);
    }

    public void startPuzzleModeHandler()
    {
        gameHandler = new Handler();
        gameRunnable = new Runnable()
        {
            @Override
            public void run()
            {
                timerView.setText(
                        String.format(Locale.getDefault(), "%d", gameMode.getTimeRemaining()));
                cubes.setText(
                        String.format(Locale.getDefault(), "%d", gameMode.getNumCubesRemaining()));
                rings.setText(
                        String.format(Locale.getDefault(), "%d", gameMode.getNumRingsRemaining()));

                if (gameMode.isObjectiveComplete() || gameMode.isObjectiveFailed())
                {
                    gameHandler.removeCallbacks(gameRunnable);
                    displayGameResults(gameMode.isObjectiveComplete());
                }
                else
                {
                    gameHandler.postDelayed(this, 500);
                }
            }
        };
        gameHandler.postDelayed(gameRunnable, 0);
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

    private ModeConfig menuMode;

    private ModeConfig gameMode;

    private TextView timerView;

    private TextView cubes;

    private TextView rings;

    private TextView numLivesView;

    private TextView scoreTextView;

    private ViewGroup sceneRoot;

    private Scene mainMenuScene;

    private Scene gamePlayScene;

    private Scene levelSelectScene;

    private Scene puzzleGamePlayScene;

    private Scene endlessGamePlayScene;

    private Transition transitionAnimation;

    private Runnable gameRunnable;

    private Handler gameHandler;

    private String currentGameMode;
}
