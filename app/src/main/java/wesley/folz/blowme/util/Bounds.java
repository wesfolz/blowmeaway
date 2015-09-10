package wesley.folz.blowme.util;

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

    public float[] getXCorners()
    {
        return xCorners;
    }

    public float[] getYCorners()
    {
        return yCorners;
    }

    public void calculateBounds( float[] rotation )
    {
        Matrix.multiplyMM( matrix, 0, rotation, 0, initialMatrix, 0 );
        //Matrix.translateM( matrix, 0, deltaX, -deltaY, 0 );
        determineBounds();

        //     Log.e( "blowme1", "ymin " + yMin + " yMax " + yMax );

/*        //on x edge
        if(Math.abs( xPos ) == GamePlayActivity.X_EDGE_POSITION)
        {
            setBounds( new float[]{xPos, yPos - this.getSize()[1]/2, 0.4f, 0.25f} );
        }
        //on y edge
        else
        {
            setBounds( new float[]{xPos - this.getSize()[0]/2, yPos, 0.4f, 0.25f} );
        }

        */
    }

    private void determineBounds()
    {
        xCorners[0] = matrix[0];
        yCorners[0] = matrix[1];
        xCorners[1] = matrix[4];
        yCorners[1] = matrix[5];
    }

    public void setBounds( float xMin, float yMin, float xMax, float yMax )
    {
        xCorners = new float[2];
        yCorners = new float[2];
        matrix = new float[]{
                xMin, yMin, 0, 1,
                xMax, yMax, 0, 1,
                0, 0, 0, 1,
                0, 0, 0, 1};

        initialMatrix = new float[]{
                xMin, yMin, 0, 1,
                xMax, yMax, 0, 1,
                0, 0, 0, 1,
                0, 0, 0, 1};
        determineBounds();
    }

    private float[] matrix;

    private float[] initialMatrix;

    private float[] xCorners;

    private float[] yCorners;
}
