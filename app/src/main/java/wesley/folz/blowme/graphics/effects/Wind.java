package wesley.folz.blowme.graphics.effects;

import android.opengl.GLES20;
import android.opengl.Matrix;
import android.os.SystemClock;

import java.util.Random;

import wesley.folz.blowme.R;
import wesley.folz.blowme.graphics.models.WindBounds;
import wesley.folz.blowme.ui.GamePlayActivity;
import wesley.folz.blowme.util.Bounds;
import wesley.folz.blowme.util.GraphicsUtilities;

/**
 * Created by wesley on 7/3/2015.
 */
public class Wind extends ParticleSystem
{
    public Wind()
    {
        super();

        xPos = -GamePlayActivity.X_EDGE_POSITION;//+.01f;
        yPos = 0;

        initialXPos = xPos;
        initialYPos = yPos;

        this.VERTEX_SHADER = R.raw.wind_vertex_shader;
        this.FRAGMENT_SHADER = R.raw.wind_fragment_shader;
        this.TEXTURE_RESOURCE = R.drawable.yellow_circle;
        GraphicsUtilities.readShader(this);

        setSize(new float[]{3.0f, 0.3f});
        setBounds(new Bounds(-1.5f, -0.15f, 1.5f, 0.15f));

        wBounds = new WindBounds(getBounds());

        rotationMatrix = new float[16];
        Matrix.setIdentityM( rotationMatrix, 0 );
        generateParticles();
    }

    @Override
    protected void generateParticles()
    {
        int numParticles = 5000;
        vertexOrder = new short[numParticles];
        int numAttributes = 4;

        interleavedData = new float[numAttributes * numParticles];

        for (int i = 0; i < numParticles; i++)
        {
            Random rand = new Random();
            //direction vectors
            // [-0.3, 0.3]
//            interleavedData[numAttributes * i] = 1.0f - 1.3f * rand.nextFloat();//direction[0];
// x direction
            // [-0.1, 0.1]
            interleavedData[numAttributes * i + 1] =
                    0.15f - 0.3f * rand.nextFloat();//direction[1]; //y direction
//            interleavedData[numAttributes * i + 2] = 0;// 1 - 2 * rand.nextFloat();
// direction[2]; //y direction

            interleavedData[numAttributes * i] = 0;
//            interleavedData[numAttributes * i] = 1.0f - 1.3f * rand.nextFloat();//direction[0];
// x direction
            // [-0.1, 0.1]
/*
            float yFloat = rand.nextFloat();
            if(yFloat <= 0.25) {
                interleavedData[numAttributes * i + 1] = -0.1f;//
                interleavedData[numAttributes * i + 2] = 0.0f;
            }
            else if(yFloat <= 0.5) {
                interleavedData[numAttributes * i + 1] = -0.033f;//-0.033f;
                interleavedData[numAttributes * i + 2] = -0.1f;
            }
            else if(yFloat <= 0.75) {
                interleavedData[numAttributes * i + 1] = 0.033f;//0.033f;
                interleavedData[numAttributes * i + 2] = 0.1f;
            }
            else {
                interleavedData[numAttributes * i + 1] = 0.1f;//0.1f;
                interleavedData[numAttributes * i + 2] = 0.1f;
            }
*/
            interleavedData[numAttributes * i + 2] =
                    0;// 1 - 2 * rand.nextFloat();//direction[2]; //z direction

            //speed
            // [0, 0.5]
            interleavedData[numAttributes * i + 3] = rand.nextFloat() / 2.0f;//currentParticle.getSpeed();
            //interleavedData[10*i+9] = 1.0f;//(float)Math.random();
            vertexOrder[i] = (short) i;
        }
    }


    @Override
    public void draw()
    {
        //wBounds.draw();
        // Add program to OpenGL ES environment
        GLES20.glUseProgram(programHandle);

        //3 coords per vertex, 3 coords per normal, 4 coords per color, 2 coords per texture, 4 bytes per float
        final int stride = (4) * BYTES_PER_FLOAT;

        //pass in direction vector to shader
        int directionHandle = GLES20.glGetAttribLocation(programHandle, "direction");
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, dataVBO);
        GLES20.glEnableVertexAttribArray(directionHandle);
        GLES20.glVertexAttribPointer(directionHandle, 3, GLES20.GL_FLOAT, false, stride, 0);

        //pass in speed to shader
        int speedHandle = GLES20.glGetAttribLocation(programHandle, "speed");
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, dataVBO);
        GLES20.glEnableVertexAttribArray(speedHandle);
        GLES20.glVertexAttribPointer(speedHandle, 1, GLES20.GL_FLOAT, false, stride, 3 * BYTES_PER_FLOAT);

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

        int timeHandle = GLES20.glGetUniformLocation(programHandle, "deltaT");
        GLES20.glUniform1f(timeHandle, time);

        int positionHandle = GLES20.glGetUniformLocation(programHandle, "position");
        GLES20.glUniform4f(positionHandle, xPos, yPos, 0, 1);

        int normalHandle = GLES20.glGetUniformLocation(programHandle, "normalVector");
        GLES20.glUniform3f(normalHandle, 0, 0, 1);

//        int deltaYHandle = GLES20.glGetUniformLocation(programHandle, "deltaY");
//        GLES20.glUniform1f(deltaYHandle, totalDelta);

        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, orderVBO);

        //blend particles
        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
        GLES20.glDisable(GLES20.GL_DEPTH_TEST);

        // Draw the points
        GLES20.glDrawElements(GLES20.GL_POINTS, vertexOrder.length, GLES20.GL_UNSIGNED_SHORT, 0);

        //unbind buffers
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0);

        GLES20.glEnable(GLES20.GL_DEPTH_TEST);

        GLES20.glDisable(GLES20.GL_BLEND);
    }


    public float getxForce()
    {
        return xForce;
    }

    public void setxForce( float xForce )
    {
        this.xForce = xForce;
    }

    public float getyForce()
    {
        return yForce;
    }

    public void setyForce( float yForce )
    {
        this.yForce = yForce;
    }

    public float getMaxWindForce()
    {
        return maxWindForce;
    }

    public void setMaxWindForce(float maxWindForce)
    {
        this.maxWindForce = maxWindForce;
    }


    @Override
    public float[] createTransformationMatrix()
    {
        float[] transformation = new float[16];

        // Create a rotation transformation for the triangle
        long time = SystemClock.uptimeMillis() % 3600L; //modulo makes rotation look smooth
        float angle = 0.6f * (float) time;


        //Matrix.setIdentityM(modelMatrix, 0);
        Matrix.setIdentityM(transformation, 0);

//        Matrix.translateM(modelMatrix, 0, 0, deltaY / 2, 0);
        Matrix.translateM(modelMatrix, 0, deltaX, deltaY, 0);

        Matrix.multiplyMM(transformation, 0, modelMatrix, 0, rotationMatrix, 0);
        //  Matrix.rotateM(transformation, 0, -80, 0, 1, 0);
        //       Matrix.rotateM(transformation, 0, angle, 1, 0, 0);

        //Matrix.translateM(transformation, 0, deltaX, deltaY, 0);

        //Log.e( "blowme", "xmin " + transformation[0] + " ymin " + transformation[1] + " xmax "
        // + transformation[4] + " ymax " + transformation[5]);

        //Matrix.multiplyMM(transformation, 0, modelMatrix, 0, transformation, 0);

        getBounds().calculateBounds(transformation);
        wBounds.bnds = getBounds();
        wBounds.updatePosition(0, 0);

        return transformation;
    }

    @Override
    public void updatePosition(float x, float y)
    {
        //time = (System.nanoTime() - initialTime) / 1000000000.0f;
        //time = (System.nanoTime()) / 1000000000.0f;

        //time = 0;
        time += 0.01f;

        deltaX = x;
        deltaY = y;

        xPos += deltaX;
        yPos += deltaY;
        totalDelta += deltaY;
    }

    public void setRotationMatrix(float rotation)
    {
        inwardRotation = rotation;
        Matrix.setIdentityM(rotationMatrix, 0);
        Matrix.setRotateM(rotationMatrix, 0, rotation, 0, 0, 1);
        //getBounds().calculateBounds(rotationMatrix);
    }

    public float getInwardRotation() {
        return inwardRotation;
    }

    private float deltaX;
    private float deltaY;

    private float totalDelta = 0;

    private float xForce;

    private float yForce;

    private float maxWindForce = 2.0f;

    private float[] rotationMatrix;

    private float inwardRotation = 0;

    public WindBounds wBounds;

}
