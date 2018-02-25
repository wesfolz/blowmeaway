package wesley.folz.blowme.graphics.effects;

import android.opengl.GLES20;
import android.opengl.Matrix;

import java.util.Random;

import wesley.folz.blowme.R;
import wesley.folz.blowme.util.GraphicsUtilities;
import wesley.folz.blowme.util.Physics;

/**
 * Created by Wesley on 12/27/2017.
 */

public class Sparkler extends ParticleSystem {
    public Sparkler(float x, float y) {
        super();
        this.VERTEX_SHADER = R.raw.sparkler_vertex_shader;
        this.FRAGMENT_SHADER = R.raw.sparkler_fragment_shader;
        this.TEXTURE_RESOURCE = R.drawable.yellow_circle;
        GraphicsUtilities.readShader(this);

        xPos = x;
        yPos = y;

        initialXPos = xPos;
        initialYPos = yPos;

        transformationMatrix = new float[16];
        Matrix.setIdentityM(transformationMatrix, 0);

        generateParticles();
    }

    //TODO: Add reinitialize method so that new particle systems can be created efficiently?
    public void reinitialize(float x, float y) {
        this.time = 0;
        //don't know why we have to multiply by 2
        xPos = 2.0f * x;
        yPos = 2.0f * y;
    }

    @Override
    protected void generateParticles() {
        vertexOrder = new short[NUM_PARTICLES];

        interleavedData = new float[NUM_ATTRIBUTES * NUM_PARTICLES];
        Random rand = new Random();

        for (int i = 0; i < NUM_PARTICLES; i++) {
            //direction vectors
            float[] direction = new float[3];
            direction[0] = 1 - 2 * rand.nextFloat();//direction[0]; //x direction
            direction[1] = 1 - 2 * rand.nextFloat();//direction[1]; //y direction
            direction[2] = 1 - 2 * rand.nextFloat();//direction[2]; //y direction
            float magnitude = (float) (Math.sqrt(direction[0] * direction[0]
                    + direction[1] * direction[1]
                    + direction[2] * direction[2]));

            float radius = 0.01f;
            //normalize vector to get spherical explosion
            interleavedData[NUM_ATTRIBUTES * i] = radius * direction[0] / magnitude;
            interleavedData[NUM_ATTRIBUTES * i + 1] = radius * direction[1] / magnitude;
            interleavedData[NUM_ATTRIBUTES * i + 2] = radius * direction[2] / magnitude;

            //speed
            interleavedData[NUM_ATTRIBUTES * i + 3] =
                    rand.nextFloat() / 2.0f;//currentParticle.getSpeed();
            //interleavedData[10*i+9] = 1.0f;//(float)Math.random();

            //visible
            interleavedData[NUM_ATTRIBUTES * i + 4] =
                    rand.nextFloat();//currentParticle.getSpeed();
            //interleavedData[10*i+9] = 1.0f;//(float)Math.random();
            vertexOrder[i] = (short) i;
        }
    }

    @Override
    public float[] createTransformationMatrix() {
        return this.transformationMatrix;
    }

    @Override
    public void draw() {
        // Add program to OpenGL ES environment
        GLES20.glUseProgram(programHandle);
        //bind buffers
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, dataVBO);
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, orderVBO);

        updateDataBuffer();

        //3 coords per vertex, 3 coords per normal, 4 coords per color, 2 coords per texture, 4
        // bytes per float
        final int stride = (NUM_ATTRIBUTES) * BYTES_PER_FLOAT;

        //pass in direction vector to shader
        int directionHandle = GLES20.glGetAttribLocation(programHandle, "direction");
        GLES20.glEnableVertexAttribArray(directionHandle);
        GLES20.glVertexAttribPointer(directionHandle, 3, GLES20.GL_FLOAT, false, stride, 0);

        //pass in speed to shader
        int speedHandle = GLES20.glGetAttribLocation(programHandle, "speed");
        GLES20.glEnableVertexAttribArray(speedHandle);
        GLES20.glVertexAttribPointer(speedHandle, 1, GLES20.GL_FLOAT, false, stride,
                3 * BYTES_PER_FLOAT);

        //pass in speed to shader
        int visibleHandle = GLES20.glGetAttribLocation(programHandle, "visible");
        GLES20.glEnableVertexAttribArray(visibleHandle);
        GLES20.glVertexAttribPointer(visibleHandle, 1, GLES20.GL_FLOAT, false, stride,
                4 * BYTES_PER_FLOAT);

        //texture
        int textureUniformHandle = GLES20.glGetUniformLocation(programHandle, "u_Texture");
        int secondTextureHandle = GLES20.glGetUniformLocation(programHandle, "second_Texture");

        // Set the active texture unit to texture unit 0.
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        // Bind the texture to this unit.
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureDataHandle);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, secondTexture);
        // Tell the texture uniform sampler to use this texture in the shader by binding to
        // texture unit 0.
        GLES20.glUniform1i(textureUniformHandle, 0);
        GLES20.glUniform1i(secondTextureHandle, 1);
        GLES20.glEnable(GLES20.GL_TEXTURE_2D);

        // get handle to shape's transformation matrix
        int mvpMatrixHandle = GLES20.glGetUniformLocation(programHandle, "u_MVPMatrix");

        //if resuming from a pause state, load previous matrix
        if (!resuming) {
            mvpMatrix = createTransformationMatrix();
            mvMatrix = new float[16];
            //creating model-view matrix
            Matrix.multiplyMM(mvMatrix, 0, viewMatrix, 0, mvpMatrix, 0);
            //creating model-view-projection matrix
            Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, mvMatrix, 0);
            //TODO: scaling mvMatrix messes up shader, so scaling must be done last, not sure why
            Matrix.scaleM(mvpMatrix, 0, scaleFactor, scaleFactor, scaleFactor);
        } else {
            this.resuming = false;
        }

        // Pass the projection and view transformation to the vertexshader
        GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false, mvpMatrix, 0);

        mLightPosHandle = GLES20.glGetUniformLocation(programHandle, "u_LightPos");

        int mMVMatrixHandle = GLES20.glGetUniformLocation(programHandle, "u_MVMatrix");
        GLES20.glUniformMatrix4fv(mMVMatrixHandle, 1, false, mvMatrix, 0);

        mLightPosHandle = GLES20.glGetUniformLocation(programHandle, "u_LightPos");

        // Pass in the light position in eye space.
        GLES20.glUniform3f(mLightPosHandle, lightPosInEyeSpace[0], lightPosInEyeSpace[1],
                lightPosInEyeSpace[2]);

        int timeHandle = GLES20.glGetUniformLocation(programHandle, "deltaT");
        GLES20.glUniform1f(timeHandle, time);

        int positionHandle = GLES20.glGetUniformLocation(programHandle, "position");
        GLES20.glUniform4f(positionHandle, initialXPos, initialYPos, 0, 1);

        int normalHandle = GLES20.glGetUniformLocation(programHandle, "normalVector");
        GLES20.glUniform3f(normalHandle, 0, 0, 1);

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
    public void updatePosition(float x, float y) {
        time = Physics.rise(this);
        Physics.panUpDown(this, time);

        //time = 0;
        //time += 0.1f;
        //time = time % 0.3f;
        Random rand = new Random();

        time = rand.nextFloat() - 0.5f;
        interval++;

        if (interval % 5 == 0) {
            for (int i = 0; i < NUM_PARTICLES; i++) {
                interleavedData[NUM_ATTRIBUTES * i + 3] = rand.nextFloat();
                interleavedData[NUM_ATTRIBUTES * i + 4] = rand.nextFloat();
            }
        }

    }

    public void setTransformationMatrix(float[] transMat) {
        this.transformationMatrix = transMat;
    }

    private float[] transformationMatrix;

    private int interval = 0;

    private static final int NUM_PARTICLES = 100;

    private static final int NUM_ATTRIBUTES = 5;

}