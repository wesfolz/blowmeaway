package wesley.folz.blowme.ui;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.util.Log;

/**
 * Created by Wesley on 9/10/2016.
 */
public class GamePlaySurfaceView extends GLSurfaceView
{
    /*-----------------------------------------Constructors---------------------------------------*/

    public GamePlaySurfaceView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }

    /*---------------------------------------Override Methods-------------------------------------*/

    @Override
    public void onPause()
    {
        super.onPause();
        Log.e("appflow", "onPause");
        this.pauseGame();
    }

    @Override
    public void onResume()
    {
        super.onResume();
        Log.e("appflow", "onResume");
        this.resumeGame();
    }

    @Override
    public void setRenderer(GLSurfaceView.Renderer renderer)
    {
        super.setRenderer(renderer);
        this.renderer = (GamePlayRenderer) renderer;
    }

    /*---------------------------------------Public Methods---------------------------------------*/

    public void pauseGame()
    {
        Log.e("appflow", "pause game");
        this.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
        renderer.pauseGame();
    }

    public void resumeGame() {
        Log.e("appflow", "resume game");
        this.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
        renderer.resumeGame();
    }

    /*-------------------------------------Protected Methods--------------------------------------*/

    /*--------------------------------------Private Methods---------------------------------------*/

    /*--------------------------------------Getters and Setters-----------------------------------*/

    public GamePlayRenderer getRenderer() {
        return renderer;
    }

    /*----------------------------------------Public Fields---------------------------------------*/

    /*--------------------------------------Protected Fields--------------------------------------*/

    /*---------------------------------------Private Fields---------------------------------------*/

    private GamePlayRenderer renderer;
}
