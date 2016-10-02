package wesley.folz.blowme.graphics.effects;

import android.opengl.Matrix;
import android.os.SystemClock;

import java.util.Random;

import wesley.folz.blowme.R;
import wesley.folz.blowme.util.GraphicsReader;

/**
 * Created by Wesley on 9/30/2016.
 */

public class DustCloud extends ParticleSystem
{

    public DustCloud(float x, float y)
    {
        super();
        this.VERTEX_SHADER = R.raw.dust_cloud_vertex_shader;
        this.FRAGMENT_SHADER = R.raw.dust_cloud_fragment_shader;
        this.TEXTURE_RESOURCE = R.raw.grey_circle;
        GraphicsReader.readShader(this);
        xPos = x;
        yPos = y;//GamePlayActivity.Y_EDGE_POSITION;

        initialXPos = xPos;
        initialYPos = -yPos;

        xRadius = 0.1f;
        yRadius = 0.01f;
        zRadius = 0.1f;

        generateParticles();
    }

    @Override
    protected void generateParticles()
    {
        int numParticles = 10000;
        vertexOrder = new short[numParticles];
        int numAttributes = 8;

        interleavedData = new float[numAttributes * numParticles];

        for (int i = 0; i < numParticles; i++)
        {
            //color
            interleavedData[numAttributes * i] = 1.0f;//color[0];//(float)Math.random();
            interleavedData[numAttributes * i + 1] = 1.0f;//color[1];//(float)Math.random();
            interleavedData[numAttributes * i + 2] = 1.0f;//color[2];//(float)Math.random();
            interleavedData[numAttributes * i + 3] = 1.0f;//color[3];//(float)Math.random();

            Random rand = new Random();
            //direction vectors
            float[] direction = new float[3];
            direction[0] = xRadius - 2 * xRadius * rand.nextFloat();//direction[0]; //x direction
            direction[1] = yRadius * rand.nextFloat();//direction[1]; //y direction
            direction[2] = zRadius - 2 * zRadius * rand.nextFloat();//direction[2]; //y direction
            float magnitude = (float) (Math.sqrt(direction[0] * direction[0]
                    + direction[1] * direction[1]
                    + direction[2] * direction[2]));
            //normalize vector to get spherical explosion
            interleavedData[numAttributes * i + 4] = direction[0] / magnitude;
            interleavedData[numAttributes * i + 5] = direction[1] / magnitude;
            interleavedData[numAttributes * i + 6] = direction[2] / magnitude;

            //speed
            interleavedData[numAttributes * i + 7] = rand.nextFloat() / 2.0f;//currentParticle.getSpeed();
            //interleavedData[10*i+9] = 1.0f;//(float)Math.random();
            vertexOrder[i] = (short) i;
        }
    }


    @Override
    public float[] createTransformationMatrix()
    {
        float[] mvp = new float[16];
        Matrix.setIdentityM(mvp, 0);
        long time = SystemClock.uptimeMillis();// % 4000L;
        float angle = 0.40f * ((int) time);

        //copy modelMatrix to separate matrix for return, (returning modelMatrix doesn't work)
        Matrix.multiplyMM(mvp, 0, modelMatrix, 0, mvp, 0);
        Matrix.translateM(mvp, 0, 0, yUp, 0);

        Matrix.rotateM(mvp, 0, angle, 0, -1, 0);


        return mvp;
    }

    @Override
    public void initializeMatrices(float[] viewMatrix, float[] projectionMatrix, float[] lightPositionInEyeSpace)
    {
        super.initializeMatrices(viewMatrix, projectionMatrix, lightPositionInEyeSpace);
        if (!this.resuming)
        {
            //rotate 130 degrees about x-axis
            Matrix.rotateM(modelMatrix, 0, 10, 1, 0, 0);
        }

        //Log.e( "blowme", "xpos: " + xPos + " ypos " + yPos );
    }

    @Override
    public void updatePosition(float x, float y)
    {
        if (yUp <= 0.25f)
        {
            yUp += 0.002f;
            time += 0.003f;
        }

    }

    private float yUp;

    private float xRadius;
    private float yRadius;
    private float zRadius;
}
