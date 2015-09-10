package wesley.folz.blowme.graphics;

/**
 * Created by wesley on 7/19/2015.
 */
public class Border extends Model
{
    public Border()
    {

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

}
