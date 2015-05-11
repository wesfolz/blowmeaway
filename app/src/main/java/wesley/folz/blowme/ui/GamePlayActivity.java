package wesley.folz.blowme.ui;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ConfigurationInfo;
import android.opengl.GLSurfaceView;
import android.os.Bundle;


public class GamePlayActivity extends Activity
{

    @Override
    protected void onCreate( Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );
        //setContentView( R.layout.activity_game_play );

        surfaceView = new GLSurfaceView( this );
        //surfaceView = (GLSurfaceView) findViewById( R.id.surfaceView );


        // Check if the system supports OpenGL ES 2.0.
        final ActivityManager activityManager = (ActivityManager) getSystemService( Context.ACTIVITY_SERVICE);
        final ConfigurationInfo configurationInfo = activityManager.getDeviceConfigurationInfo();
        final boolean supportsEs2 = configurationInfo.reqGlEsVersion >= 0x20000;

        if (supportsEs2)
        {
            // Request an OpenGL ES 2.0 compatible context.
            surfaceView.setEGLContextClientVersion(2);

            // Set the renderer to our demo renderer, defined below.
            surfaceView.setRenderer(new GamePlayRenderer());
            surfaceView.setRenderMode( GLSurfaceView.RENDERMODE_WHEN_DIRTY );
        }
        else
        {
            // This is where you could create an OpenGL ES 1.x compatible
            // renderer if you wanted to support both ES 1 and ES 2.
            return;
        }
        setContentView( surfaceView );

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


    /**
     * View where OpenGL objects are drawn
     */
    private GLSurfaceView surfaceView;

}
