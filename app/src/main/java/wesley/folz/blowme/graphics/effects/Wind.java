package wesley.folz.blowme.graphics.effects;

import android.opengl.GLES20;
import android.opengl.Matrix;

import java.util.Random;

import wesley.folz.blowme.R;
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

       /* interleavedData = new float[]{
                - 1.0f, 0.25f, 0.0f,    // top left
                 0.0f, 0.0f, 1.0f,      //normal
                 0.0f, 0.0f, 1.0f, 0.8f, //color
                - 1.0f, - 0.25f, 0.0f,  // bottom left
                0.0f, 0.0f, 1.0f,       //normal
                0.0f, 0.0f, 1.0f, 0.8f, //color
                1.0f, - 0.25f, 0.0f,    // bottom right
                0.0f, 0.0f, 1.0f,       //normal
                0.0f, 0.0f, 1.0f, 0.8f, //color
                1.0f, 0.25f, 0.0f,      // top right
                0.0f, 0.0f , 1.0f,      //normal
                0.0f, 0.0f, 1.0f, 0.8f};//color

        vertexOrder = new short[]{0, 1, 2, 0, 2, 3};
*/
        this.VERTEX_SHADER = R.raw.wind_vertex_shader;
        this.FRAGMENT_SHADER = R.raw.wind_fragment_shader;
        this.TEXTURE_RESOURCE = R.raw.yellow_circle;
        GraphicsUtilities.readShader(this);

        setSize( new float[]{2.0f, 0.5f} );
        setBounds( new Bounds( - 1.0f, - 0.25f, 1.0f, 0.25f ) );
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
            interleavedData[numAttributes * i] = 1.0f - 1.3f * rand.nextFloat();//direction[0]; //x direction
            interleavedData[numAttributes * i + 1] = 0.1f - 0.2f * rand.nextFloat();//direction[1]; //y direction
            interleavedData[numAttributes * i + 2] = 0;// 1 - 2 * rand.nextFloat();//direction[2]; //y direction

            //speed
            interleavedData[numAttributes * i + 3] = rand.nextFloat() / 2.0f;//currentParticle.getSpeed();
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

        int deltaYHandle = GLES20.glGetUniformLocation(programHandle, "deltaY");
        GLES20.glUniform1f(deltaYHandle, totalDelta);

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


    public void calculateWindForce()
    {
        xForce = (-1) * xPos * maxWindForce;
        yForce = (-1) * yPos * maxWindForce;
    }

    /**
     * TODO: Rotate fan so that it doesn't compete with blade rotation (make wind force a function of distance)
     * Rotate fan so that it's pointing towards the center of the screen
     *
     * @return - rotation matrix about z axis that points fan towards center of screen
     */
    private float[] calculateInwardRotation()
    {
        float[] rotationMatrix = new float[16];
        float inwardRotation;
        float cornerAngle = 45;//(float) (180 * Math.atan( GamePlayActivity.X_EDGE_POSITION /
        // GamePlayActivity.Y_EDGE_POSITION ) / Math
        //.PI);

        float xRatio = (90 - cornerAngle) / GamePlayActivity.X_EDGE_POSITION;
        float yRatio = cornerAngle / GamePlayActivity.Y_EDGE_POSITION;

        //on negative x edge
        if( xPos == (- GamePlayActivity.X_EDGE_POSITION) )
        {
            //inwardRotation = yRatio * ( yPos + (yPos/Math.abs(yPos))*(X_EDGE_POSITION + xPos) );
            inwardRotation = yRatio * yPos;
            //Log.e( "blowme", "negative x edge" );
        }
        //on positive x edge
        else if( xPos == GamePlayActivity.X_EDGE_POSITION )
        {
            inwardRotation = 180 - cornerAngle + yRatio * (GamePlayActivity.Y_EDGE_POSITION - yPos);
            //Log.e( "blowme", "negative x edge" );
        }
        //on y edge
        else
        {
            inwardRotation = (yPos / Math.abs( yPos )) * (xRatio * xPos + 90);
            //Log.e( "blowme", "negative y edge" );
        }

        Matrix.setRotateM( rotationMatrix, 0, inwardRotation, 0, 0, 1 );
        return rotationMatrix;
    }

    @Override
    public float[] createTransformationMatrix()
    {
        float[] transformation = new float[16];

        //Matrix.setIdentityM(modelMatrix, 0);
        Matrix.setIdentityM(transformation, 0);

        Matrix.translateM(modelMatrix, 0, 0, deltaY / 2, 0);
        Matrix.multiplyMM(transformation, 0, modelMatrix, 0, rotationMatrix, 0);
        //Matrix.translateM(transformation, 0, deltaX, deltaY, 0);

        //Log.e( "blowme", "xmin " + transformation[0] + " ymin " + transformation[1] + " xmax "
        // + transformation[4] + " ymax " + transformation[5]);

        //Matrix.multiplyMM(transformation, 0, modelMatrix, 0, transformation, 0);

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
        totalDelta += deltaY;
    }

    public void setRotationMatrix( float[] rotationMatrix )
    {
        this.rotationMatrix = rotationMatrix;
    }

    private float deltaX;
    private float deltaY;

    private float totalDelta = 0;

    private float xForce;

    private float yForce;

    private float maxWindForce = 1.8f;

    private float[] rotationMatrix;

}
