package wesley.folz.blowme.graphics;

/**
 * Created by wesley on 7/3/2015.
 */
public class Wind extends Model
{
    public Wind()
    {
        setBounds( new float[]{0, 0, 0, 0} );
    }

    public float getxForce()
    {
        return xForce;
    }

    public void setxForce( float xForce )
    {
        this.xForce = xForce;
    }

    public float getyForce()
    {
        return yForce;
    }

    public void setyForce( float yForce )
    {
        this.yForce = yForce;
    }

    public void calculateWindForce()
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

    private float xForce;

    private float yForce;


}
