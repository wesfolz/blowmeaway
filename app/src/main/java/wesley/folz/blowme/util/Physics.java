package wesley.folz.blowme.util;

import wesley.folz.blowme.graphics.Border;
import wesley.folz.blowme.graphics.FallingObject;
import wesley.folz.blowme.graphics.Wind;

/**
 * Created by wesley on 7/3/2015.
 */
public abstract class Physics
{
    public static void calculateWindForce( Wind wind, FallingObject object )
    {
        //calculate 2D euclidean distance between wind src and falling object
        float distance = (float) Math.sqrt( Math.pow( wind.getxPos() - object.getxPos(), 2 ) +
                Math.pow( wind.getyPos() - object.getyPos(), 2 ) );

        //wind force decreases over distance
        float totalForce = wind.getMaxWindForce() - 2.0f * distance;

        //wind can't pull object
        if (totalForce < 0)
        {
            totalForce = 0;
        }
        //calculate x and y components of force
        wind.setxForce((-1) * wind.getxPos() * (totalForce));
        wind.setyForce((-1) * wind.getyPos() * (totalForce));
    }

    public static boolean isBorderCollision( Bounds object )
    {
        return (object.getxRight() >= Border.XRIGHT ||
                object.getxLeft() <= Border.XLEFT);
    }

    public static boolean isTopBorderCollision(Bounds object)
    {
        return (object.getyTop() <= Border.YTOP);
    }


    public static boolean isCollision( Bounds b1, Bounds b2 )
    {
        float b1xMin = Math.min(b1.getxLeft(), b1.getxRight());
        float b1xMax = Math.max(b1.getxLeft(), b1.getxRight());
        float b1yMin = Math.min(b1.getyTop(), b1.getyBottom());
        float b1yMax = Math.max(b1.getyTop(), b1.getyBottom());

        float b2xMin = Math.min(b2.getxLeft(), b2.getxRight());
        float b2xMax = Math.max(b2.getxLeft(), b2.getxRight());
        float b2yMin = Math.min(b2.getyTop(), b2.getyBottom());
        float b2yMax = Math.max(b2.getyTop(), b2.getyBottom());

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
