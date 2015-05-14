package wesley.folz.blowme.graphics;

import android.opengl.Matrix;
import android.os.SystemClock;

/**
 * Created by wesley on 5/11/2015.
 */
public class Fan extends Model
{
    public Fan()
    {
        super();

    }

    @Override
    public float[] createTransformationMatrix()
    {
        float[] mRotationMatrix = new float[16];

        float[] bladeRotation = new float[16];

        // Create a rotation transformation for the triangle
        long time = SystemClock.uptimeMillis();// % 4000L;
        float angle = 0.40f * ((int) time);
        Matrix.setRotateM( mRotationMatrix, 0, angle, 0, - 1, 0 );

        // Combine the rotation matrix with the projection and camera view
        // Note that the mMVPMatrix factor *must be first* in order
        // for the matrix multiplication product to be correct.
        Matrix.multiplyMM( bladeRotation, 0, initialTransformationMatrix, 0, mRotationMatrix, 0 );
        return bladeRotation;
    }

    @Override
    public void initializeMatrix()
    {
        super.initializeMatrix();

        float[] initialRotation = new float[16];
        float[] secondRotation = new float[16];
        float[] first = new float[16];

        Matrix.setRotateM( initialRotation, 0, 45, 0, 1, 0 );
        Matrix.setRotateM( secondRotation, 0, 90, 1, 0, 0 );

        Matrix.multiplyMM( first, 0, mvpMatrix, 0, initialRotation, 0 );
        Matrix.multiplyMM( initialTransformationMatrix, 0, first, 0, secondRotation, 0 );
        Matrix.scaleM( initialTransformationMatrix, 0, 0.1f, 0.1f, 0.1f );
    }

    private final float[] initialTransformationMatrix = new float[16];

}
