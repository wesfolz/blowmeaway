package wesley.folz.blowme.ui;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ConfigurationInfo;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import wesley.folz.blowme.R;


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
        surfaceView = (GLSurfaceView) findViewById( R.id.surfaceView );


        // Check if the system supports OpenGL ES 2.0.
        final ActivityManager activityManager = (ActivityManager) getSystemService( Context.ACTIVITY_SERVICE);
        final ConfigurationInfo configurationInfo = activityManager.getDeviceConfigurationInfo();
        final boolean supportsEs2 = configurationInfo.reqGlEsVersion >= 0x20000;

        if (supportsEs2)
        {
            // Request an OpenGL ES 2.0 compatible context.
            surfaceView.setEGLContextClientVersion( 2 );

            // Set the renderer to our demo renderer, defined below.
            surfaceView.setRenderer( new GamePlayRenderer() );
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
                if( event.getAction() == MotionEvent.ACTION_DOWN )
                {
                    Toast.makeText( MyApplication.getAppContext(), " Down: X " + event.getRawX()
                            + " Y " + event.getRawY(), Toast.LENGTH_SHORT ).show();
                }
                if( event.getAction() == MotionEvent.ACTION_MOVE )
                {
                    Log.e( "blowme", "Move: X " + event.getRawX() + " Y " + event.getRawY() );
                }
                return true;
            }
        } );
        //setContentView( surfaceView );
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
    private GLSurfaceView surfaceView;

}
