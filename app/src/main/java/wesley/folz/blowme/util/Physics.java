package wesley.folz.blowme.util;

import android.graphics.PointF;

import wesley.folz.blowme.graphics.Border;
import wesley.folz.blowme.graphics.effects.Wind;
import wesley.folz.blowme.graphics.models.Model;

/**
 * Created by wesley on 7/3/2015.
 */
public abstract class Physics
{
    public static void calculateWindForce(Wind wind, Model object)
    {
        //calculate 2D euclidean distance between wind src and falling object
        float distance = (float) Math.sqrt( Math.pow( wind.getxPos() - object.getxPos(), 2 ) +
                Math.pow( wind.getyPos() - object.getyPos(), 2 ) );

        //wind force decreases over distance
        float totalForce = wind.getMaxWindForce() - distance;

        //wind can't pull object
        if (totalForce < 0)
        {
            totalForce = 0;
        }

        //constrain angle between 0 and +-360
        float angle = wind.getInwardRotation() % 360;

        //determine if y is positive or negative
        float ySign = (angle > 180 || (angle < 0 && angle > -180)) ? -1 : 1;

        //get absolute value of angle
        angle = Math.abs(angle);

        //determine if x is positive or negative
        float xSign = (angle < 90 || angle > 270) ? 1 : -1;

        //constrain angle between 0 and 180
        angle %= 180;

        //if angle is greater than 90 subtract 180 so that it goes 0->90->0
        if (angle > 90) {
            angle = 180 - angle;
        }

        //calculate x and y components of force
        //wind.setxForce((-1) * (wind.getxPos()/ GamePlayActivity.X_EDGE_POSITION) * (totalForce));
        //wind.setyForce((-1) * (wind.getyPos()/GamePlayActivity.Y_EDGE_POSITION) * (totalForce));

        //0 -> x=1 y=0, +-90 -> x=0 y=1
        // +-180 x=1 y=0, +-270 -> x=0 y=1
        wind.setxForce(xSign * ((90 - angle) / 90) * (totalForce));
        wind.setyForce(ySign * ((angle) / 90) * (totalForce));
    }


    public static boolean isTopBorderCollision(Bounds object)
    {
        return (object.getyTop() >= Border.YTOP);
    }

    public static COLLISION calculateCollision(Bounds b1, Bounds b2)
    {
        float leftRight = b2.getxRight() - b1.getxLeft();
        float rightLeft = b1.getxRight() - b2.getxLeft();
        float topBottom = b1.getyTop() - b2.getyBottom();
        float bottomTop = b2.getyTop() - b1.getyBottom();

        if (Physics.isCollision(b1, b2))
        {
            if (Math.min(leftRight, rightLeft) < Math.min(topBottom, bottomTop))
            {
                return leftRight < rightLeft ? COLLISION.RIGHT_LEFT : COLLISION.LEFT_RIGHT;
            }
            else
            {
                return topBottom < bottomTop ? COLLISION.BOTTOM_TOP : COLLISION.TOP_BOTTOM;
            }

        }
        return COLLISION.NONE;
    }

    /*

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
    */
    public static boolean isCollision(Bounds b1, Bounds b2) {
        //see if a corner of B1 is within B2
        //A -> b2 top left
        //B -> b2 top right
        //C -> b2 bottom right
        //D -> b2 bottom left
        //M -> b1 corner

        boolean collision = pointInBounds(b1.getTopLeft(), b2);
        collision |= pointInBounds(b1.getTopRight(), b2);
        collision |= pointInBounds(b1.getBottomLeft(), b2);
        collision |= pointInBounds(b1.getBottomRight(), b2);

        collision |= pointInBounds(b2.getTopLeft(), b1);
        collision |= pointInBounds(b2.getTopRight(), b1);
        collision |= pointInBounds(b2.getBottomLeft(), b1);
        collision |= pointInBounds(b2.getBottomRight(), b1);

        return collision;
    }

    private static boolean pointInBounds(PointF point, Bounds b) {
        PointF AM = new PointF(b.getTopLeft().x - point.x, b.getTopLeft().y - point.y);

        PointF AB = new PointF(b.getTopLeft().x - b.getTopRight().x,
                b.getTopLeft().y - b.getTopRight().y);

        PointF AD = new PointF(b.getTopLeft().x - b.getBottomLeft().x,
                b.getTopLeft().y - b.getBottomLeft().y);

        float AMdotAB = dotProduct(AM, AB);
        float ABdotAB = dotProduct(AB, AB);
        float AMdotAD = dotProduct(AM, AD);
        float ADdotAD = dotProduct(AD, AD);

        return (0 < AMdotAB && AMdotAB < ABdotAB) && (0 < AMdotAD && AMdotAD < ADdotAD);
    }

    private static float dotProduct(PointF a, PointF b) {
        return a.x * b.x + a.y * b.y;
    }


    public static float[] sumOfForces( float xForce, float yForce )
    {
        float[] sum = new float[]{0, 0};

        sum[0] = xForce;

        sum[1] = yForce + KINETIC_FRICTION - GRAVITY;

        return sum;
    }

    public enum COLLISION
    {
        NONE,
        LEFT_RIGHT,
        RIGHT_LEFT,
        TOP_BOTTOM,
        BOTTOM_TOP
    }


    public static final float GRAVITY = 9.8f;

    public static final float KINETIC_FRICTION = 9.75f;
}
