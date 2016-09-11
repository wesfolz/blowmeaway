package wesley.folz.blowme.ui;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;

/**
 * Created by Wesley on 9/10/2016.
 */
public class GamePlaySurfaceView extends GLSurfaceView
{
    public GamePlaySurfaceView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }

    @Override
    public void onPause()
    {
        super.onPause();
        renderer.pauseGame();
    }

    @Override
    public void onResume()
    {
        super.onResume();
        renderer.resumeGame();
    }

    @Override
    public void setRenderer(GLSurfaceView.Renderer renderer)
    {
        super.setRenderer(renderer);
        this.renderer = (GamePlayRenderer) renderer;
    }

    public GamePlayRenderer getRenderer()
    {
        return renderer;
    }

    private GamePlayRenderer renderer;

}
