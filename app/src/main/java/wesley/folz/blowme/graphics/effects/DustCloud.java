package wesley.folz.blowme.graphics.effects;

import android.opengl.GLES20;
import android.opengl.Matrix;
import android.os.SystemClock;

import java.util.Random;

import wesley.folz.blowme.R;
import wesley.folz.blowme.util.GraphicsUtilities;

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
        this.TEXTURE_RESOURCE = R.drawable.grey_circle;
        GraphicsUtilities.readShader(this);
        xPos = 0;
        yPos = 0;//GamePlayActivity.Y_EDGE_POSITION;

        initialXPos = x;
        initialYPos = -y;// + 0.06f;

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
    public void draw()
    {
        // Add program to OpenGL ES environment
        GLES20.glUseProgram(programHandle);

        //3 coords per vertex, 3 coords per normal, 4 coords per color, 2 coords per texture, 4 bytes per float
        final int stride = (COORDS_PER_COLOR + 4) * BYTES_PER_FLOAT;

        int colorHandle = GLES20.glGetAttribLocation(programHandle, "color");
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, dataVBO);
        GLES20.glEnableVertexAttribArray(colorHandle);
        GLES20.glVertexAttribPointer(colorHandle, COORDS_PER_COLOR, GLES20.GL_FLOAT, false, stride, 0);

        //pass in direction vector to shader
        int directionHandle = GLES20.glGetAttribLocation(programHandle, "direction");
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, dataVBO);
        GLES20.glEnableVertexAttribArray(directionHandle);
        GLES20.glVertexAttribPointer(directionHandle, 3, GLES20.GL_FLOAT, false, stride, (COORDS_PER_COLOR) * BYTES_PER_FLOAT);

        //pass in speed to shader
        int speedHandle = GLES20.glGetAttribLocation(programHandle, "speed");
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, dataVBO);
        GLES20.glEnableVertexAttribArray(speedHandle);
        GLES20.glVertexAttribPointer(speedHandle, 1, GLES20.GL_FLOAT, false, stride, (COORDS_PER_COLOR + 3) * BYTES_PER_FLOAT);

        //texture
        int textureUniformHandle = GLES20.glGetUniformLocation(programHandle, "u_Texture");

        // Set the active texture unit to texture unit 0.
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        // Bind the texture to this unit.
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureDataHandle);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
        // Tell the texture uniform sampler to use this texture in the shader by binding to texture unit 0.
        GLES20.glUniform1i(textureUniformHandle, 0);

        // get handle to shape's transformation matrix
        int mvpMatrixHandle = GLES20.glGetUniformLocation(programHandle, "u_MVPMatrix");

        //if resuming from a pause state, load previous matrix
        if (!resuming)
        {
            mvpMatrix = createTransformationMatrix();
            mvMatrix = new float[16];
            //creating model-view matrix
            Matrix.multiplyMM(mvMatrix, 0, viewMatrix, 0, mvpMatrix, 0);
            //creating model-view-projection matrix
            Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, mvMatrix, 0);
            //TODO: scaling mvMatrix messes up shader, so scaling must be done last, not sure why
            Matrix.scaleM(mvpMatrix, 0, scaleFactor, scaleFactor, scaleFactor);
        }
        else
        {
            this.resuming = false;
        }

        // Pass the projection and view transformation to the vertexshader
        GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false, mvpMatrix, 0);

        mLightPosHandle = GLES20.glGetUniformLocation(programHandle, "u_LightPos");

        int mMVMatrixHandle = GLES20.glGetUniformLocation(programHandle, "u_MVMatrix");
        GLES20.glUniformMatrix4fv(mMVMatrixHandle, 1, false, mvMatrix, 0);

        mLightPosHandle = GLES20.glGetUniformLocation(programHandle, "u_LightPos");

        // Pass in the light position in eye space.
        GLES20.glUniform3f(mLightPosHandle, lightPosInEyeSpace[0], lightPosInEyeSpace[1], lightPosInEyeSpace[2]);

        int timeHandle = GLES20.glGetUniformLocation(programHandle, "spread");
        GLES20.glUniform1f(timeHandle, spread);

        int positionHandle = GLES20.glGetUniformLocation(programHandle, "deltaT");
        GLES20.glUniform4f(positionHandle, xPos, yPos, 0, 1);

        int normalHandle = GLES20.glGetUniformLocation(programHandle, "deltaT");
        GLES20.glUniform3f(normalHandle, 0, 0, 1);

        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, orderVBO);

        //blend particles
        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
        GLES20.glDisable(GLES20.GL_DEPTH_TEST);

        // Draw the triangle
        GLES20.glDrawElements(GLES20.GL_POINTS, vertexOrder.length, GLES20.GL_UNSIGNED_SHORT, 0);

        //unbind buffers
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0);

        GLES20.glEnable(GLES20.GL_DEPTH_TEST);

        GLES20.glDisable(GLES20.GL_BLEND);
        GLES20.glDisable(GLES20.GL_TEXTURE_2D);
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
    public void updatePosition(float x, float y)
    {
        if (y != 0)
        {
            yUp = y;
            spread = 2 * yUp;
        } else if (yUp < 0.25f) {
            yUp += 0.01;//0.25f;
        }
    }

    private float spread;

    private float yUp;

    private float xRadius;
    private float yRadius;
    private float zRadius;
}