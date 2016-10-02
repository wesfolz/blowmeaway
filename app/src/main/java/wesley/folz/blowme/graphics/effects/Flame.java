package wesley.folz.blowme.graphics.effects;

import java.util.Random;

import wesley.folz.blowme.R;
import wesley.folz.blowme.util.GraphicsReader;

/**
 * Created by Wesley on 10/1/2016.
 */

public class Flame extends ParticleSystem
{
    public Flame(float x, float y)
    {
        super();
        this.VERTEX_SHADER = R.raw.flame_vertex_shader;
        this.FRAGMENT_SHADER = R.raw.flame_fragment_shader;
        this.TEXTURE_RESOURCE = R.raw.yellow_circle;
        GraphicsReader.readShader(this);
        xPos = x;
        yPos = y;//GamePlayActivity.Y_EDGE_POSITION;

        initialXPos = xPos;
        initialYPos = -yPos;

        xRadius = 0.02f;
        yRadius = 0.2f;
        zRadius = 0.0f;

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

    private float xRadius;
    private float yRadius;
    private float zRadius;
}
