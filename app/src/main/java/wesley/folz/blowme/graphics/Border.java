package wesley.folz.blowme.graphics;

import android.opengl.GLSurfaceView;

/**
 * Created by wesley on 7/19/2015.
 */
public class Border extends Model
{
    public Border()
    {
        super();
    }

    @Override
    public float[] createTransformationMatrix()
    {
        return new float[0];
    }

    @Override
    public void updatePosition( float x, float y )
    {

    }

    public static final float XMIN = - 0.5f;

    public static final float XMAX = 0.5f;

    public static final float YMIN = - 1.0f;

    public static final float YMAX = 1.0f;

}
