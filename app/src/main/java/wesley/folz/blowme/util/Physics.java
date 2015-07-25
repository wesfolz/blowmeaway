package wesley.folz.blowme.util;

import wesley.folz.blowme.graphics.FallingObject;
import wesley.folz.blowme.graphics.Wind;

/**
 * Created by wesley on 7/3/2015.
 */
public abstract class Physics
{
    public static void calculateWindForce( Wind wind, FallingObject object )
    {
        float distance = (float) Math.sqrt( Math.pow( wind.getxPos() - object.getxPos(), 2 ) +
                Math.pow( wind.getyPos() - object.getyPos(), 2 ) );
        float displacement = wind.getWindDisplacement() - distance;
        if( displacement < 0 )
            displacement = 0;
        wind.setxForce( (- 1) * wind.getxPos() * (displacement) );
        wind.setyForce( (- 1) * wind.getyPos() * (displacement) );
    }

    public static boolean isCollision( Bounds b1, Bounds b2 )
    {
        float b1xMin = Math.min( b1.getxCorners()[0], b1.getxCorners()[1] );
        float b1xMax = Math.max( b1.getxCorners()[0], b1.getxCorners()[1] );
        float b1yMin = Math.min( b1.getyCorners()[0], b1.getyCorners()[1] );
        float b1yMax = Math.max( b1.getyCorners()[0], b1.getyCorners()[1] );

        float b2xMin = Math.min( b2.getxCorners()[0], b2.getxCorners()[1] );
        float b2xMax = Math.max( b2.getxCorners()[0], b2.getxCorners()[1] );
        float b2yMin = Math.min( b2.getyCorners()[0], b2.getyCorners()[1] );
        float b2yMax = Math.max( b2.getyCorners()[0], b2.getyCorners()[1] );

        return (b1xMin <= b2xMax && b1yMin <= b2yMax && b1xMax >= b2xMin && b1yMax >= b2yMin);
    }

    public static float[] sumOfForces( float xForce, float yForce )
    {
        float[] sum = new float[]{0, 0};

        sum[0] = xForce;

        sum[1] = yForce + GRAVITY - KINETIC_FRICTION;

        return sum;
    }


    public static final float GRAVITY = 9.8f;

    public static final float KINETIC_FRICTION = 9.75f;
}
