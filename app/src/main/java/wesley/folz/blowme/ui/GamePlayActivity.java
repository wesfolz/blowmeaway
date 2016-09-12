package wesley.folz.blowme.ui;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ConfigurationInfo;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.PopupWindow;

import wesley.folz.blowme.R;
import wesley.folz.blowme.graphics.Fan;


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
        fan = new Fan();

        if( supportsEs2 )
        {
            // Request an OpenGL ES 2.0 compatible context.
            surfaceView.setEGLContextClientVersion( 2 );

            // Set the renderer to our demo renderer, defined below.
            surfaceView.setRenderer( new GamePlayRenderer( fan ) );
            //surfaceView.setRenderMode( GLSurfaceView.RENDERMODE_WHEN_DIRTY );
        }
        else
        {
            // This is where you could create an OpenGL ES 1.x compatible
            // renderer if you wanted to support both ES 1 and ES 2.
            return;
        }
        surfaceView.setOnTouchListener( new View.OnTouchListener()
        {
            @Override
            public boolean onTouch( View v, MotionEvent event )
            {
                final int action = MotionEventCompat.getActionMasked( event );
                float x = 2 * event.getX() / WIDTH;
                float y = 2 * event.getY() / HEIGHT;

                switch( action )
                {
                    case MotionEvent.ACTION_DOWN:
                        fan.setInitialX( x );
                        fan.setInitialY( y );
                        //Log.e( "blowme", "x " + event.getX() + " y " + event.getY() );
                        break;

                    case MotionEvent.ACTION_MOVE:
                        fan.updatePosition( x, y );
                        surfaceView.requestRender();
                        //Log.e( "blowme", "Move: X " + (event.getRawX() / WIDTH) + " Y " +
                        // (event.getRawY() / HEIGHT) );
                        break;
                }
                return true;
            }
        } );
        //setContentView( surfaceView );
    }

    protected void onPauseButtonClicked(View pauseButton)
    {
        Log.e("blowme", "pause");
        if (!surfaceView.getRenderer().isPaused())
        {
            View pauseView = getLayoutInflater().inflate(R.layout.pause_window_layout, null);
            pauseWindow = new PopupWindow(pauseView, ViewPager.LayoutParams.WRAP_CONTENT, ViewPager.LayoutParams.WRAP_CONTENT);
            surfaceView.onPause();
            pauseWindow.showAtLocation(pauseView, Gravity.CENTER, 10, 10);
        }
    }

    protected void onResumeButtonClicked(View resumeButton)
    {
        pauseWindow.dismiss();
        surfaceView.onResume();
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

    /**
     * View where OpenGL objects are drawn
     */
    private GamePlaySurfaceView surfaceView;

    private PopupWindow pauseWindow;

    public static final float X_EDGE_POSITION = 0.35f;

    public static final float Y_EDGE_POSITION = 0.7f;

    private Fan fan;

    private int WIDTH;

    private int HEIGHT;
}
