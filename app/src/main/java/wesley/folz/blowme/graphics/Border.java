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

    //left side
    public static final float XLEFT = -0.5f;

    //right side
    public static final float XRIGHT = 0.5f;

    //top
    public static final float YTOP = -1.0f;

    //bottom
    public static final float YBOTTOM = 1.0f;

}
