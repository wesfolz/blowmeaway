package wesley.folz.blowme.util;

import android.graphics.PointF;
import android.opengl.Matrix;

/**
 * Created by wesley on 7/10/2015.
 */
public class Bounds
{
    public Bounds()
    {
        this( 0, 0, 0, 0 );
    }

    public Bounds( float xMin, float yMin, float xMax, float yMax )
    {
        setBounds( xMin, yMin, xMax, yMax );
    }

    public float getyTop()
    {
        return yTop;
    }


    public float getyBottom()
    {
        return yBottom;
    }

    public float getxRight()
    {
        return xRight;
    }

    public float getxLeft()
    {
        return xLeft;
    }

    public void calculateBounds(float[] transformation)
    {
        float[] mvp = new float[16];

        Matrix.multiplyMM(mvp, 0, transformation, 0, matrix, 0);

        determineBounds(mvp);
    }

    private void determineBounds(float[] matrix)
    {
        xLeft = matrix[0];
        yBottom = matrix[1];
        xRight = matrix[4];
        yTop = matrix[5];

        topLeft = new PointF(matrix[8], matrix[9]);
        topRight = new PointF(matrix[4], matrix[5]);
        bottomLeft = new PointF(matrix[0], matrix[1]);
        bottomRight = new PointF(matrix[12], matrix[13]);
    }
    public void setBounds( float xMin, float yMin, float xMax, float yMax )
    {
        //xCorners = new float[2];
        //yCorners = new float[2];
        matrix = new float[]{
                xMin, yMin, 0, 1,
                xMax, yMax, 0, 1,
                xMin, yMax, 0, 1,
                xMax, yMin, 0, 1};

        determineBounds(matrix);
    }

    public PointF getTopLeft() {
        return topLeft;
    }

    public PointF getBottomLeft() {
        return bottomLeft;
    }

    public PointF getTopRight() {
        return topRight;
    }

    public PointF getBottomRight() {
        return bottomRight;
    }


    private float[] matrix;

    //private float[] xCorners;

    //private float[] yCorners;

    private float yTop;

    private float yBottom;

    private float xRight;

    private float xLeft;


    private PointF topLeft = new PointF();

    private PointF bottomLeft = new PointF();

    private PointF topRight = new PointF();

    private PointF bottomRight = new PointF();
}
