package wesley.folz.blowme.graphics.effects;

import android.opengl.Matrix;

/**
 * Created by Wesley on 9/30/2016.
 */

public class Shattering extends ParticleSystem
{
    @Override
    protected void generateParticles()
    {
    }


    @Override
    public float[] createTransformationMatrix()
    {
        float[] mvp = new float[16];
        Matrix.setIdentityM(mvp, 0);

        //copy modelMatrix to separate matrix for return, (returning modelMatrix doesn't work)
        Matrix.multiplyMM(mvp, 0, modelMatrix, 0, mvp, 0);

        return mvp;
    }

    @Override
    public void updatePosition(float x, float y)
    {

    }
}
