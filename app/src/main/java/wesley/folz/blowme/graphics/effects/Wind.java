package wesley.folz.blowme.graphics.effects;

import android.opengl.GLES20;
import android.opengl.Matrix;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
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
    public Wind() {
        super();

        xPos = -GamePlayActivity.X_EDGE_POSITION;//+.01f;
        yPos = 0;

        initialXPos = xPos;
        initialYPos = yPos;

        this.VERTEX_SHADER = R.raw.wind_vertex_shader;
        this.FRAGMENT_SHADER = R.raw.wind_fragment_shader;
        GraphicsUtilities.readShader(this);

        setSize(new float[]{3.0f, 0.3f});
        setBounds(new Bounds(-1.5f, -0.15f, 1.5f, 0.15f));

        rotationMatrix = new float[16];
        Matrix.setIdentityM( rotationMatrix, 0 );
        generateParticles();
    }

    @Override
    protected void generateParticles() {
        vertexOrder = new short[NUM_PARTICLES];

        interleavedData = new float[NUM_ATTRIBUTES * NUM_PARTICLES];
        Random rand = new Random();

        float[] identity = new float[16];
        Matrix.setIdentityM(identity, 0);

        for (int i = 0; i < NUM_PARTICLES; i++) {
            //direction vectors
            //x direction
            interleavedData[NUM_ATTRIBUTES * i] = 0;//2.0f*rand.nextFloat();//0;

            //y direction [-size/2, size/2]
            interleavedData[NUM_ATTRIBUTES * i + 1] =
                    getSize()[1] / 2.0f - getSize()[1] * rand.nextFloat();

            //z direction
            interleavedData[NUM_ATTRIBUTES * i + 2] = 0;

            //speed [0.5, 1.0]
            interleavedData[NUM_ATTRIBUTES * i + 3] =
                    1.0f - rand.nextFloat() / 2.0f;//currentParticle.getSpeed();

            //add 4x4 matrix
            for (int j = 0; j < 16; j++) {
                interleavedData[NUM_ATTRIBUTES * i + 4 + j] = identity[j];
            }

            vertexOrder[i] = (short) i;
        }
    }

    //must be called after glBindBuffer(GLES20.GL_ARRAY_BUFFER, dataVBO);
    private void updateDataBuffer() {
        dataBuffer = ByteBuffer.allocateDirect(interleavedData.length * 4)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        dataBuffer.put(interleavedData).position(0);

        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, dataBuffer.capacity() * 4,
                dataBuffer, GLES20.GL_STATIC_DRAW);
    }

    @Override
    public void draw() {
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

        //wBounds.draw();
        // Add program to OpenGL ES environment
        GLES20.glUseProgram(programHandle);
        //bind buffers
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, dataVBO);
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, orderVBO);

        updateDataBuffer();

        //3 coords per vertex, 3 coords per normal, 4 coords per color, 2 coords per texture, 4
        // bytes per float
        final int stride = (20) * BYTES_PER_FLOAT;

        //pass in direction vector to shader
        int directionHandle = GLES20.glGetAttribLocation(programHandle, "direction");
        //GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, dataVBO);
        GLES20.glEnableVertexAttribArray(directionHandle);
        GLES20.glVertexAttribPointer(directionHandle, 3, GLES20.GL_FLOAT, false, stride, 0);

        //pass in speed to shader
        int speedHandle = GLES20.glGetAttribLocation(programHandle, "speed");
        //GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, dataVBO);
        GLES20.glEnableVertexAttribArray(speedHandle);
        GLES20.glVertexAttribPointer(speedHandle, 1, GLES20.GL_FLOAT, false, stride,
                3 * BYTES_PER_FLOAT);

        //pass in per-vertex transformation matrix to shader
        int matrixHandle = GLES20.glGetAttribLocation(programHandle, "transMatrix");
        int pos2 = matrixHandle + 1;
        int pos3 = matrixHandle + 2;
        int pos4 = matrixHandle + 3;
        GLES20.glEnableVertexAttribArray(matrixHandle);
        GLES20.glEnableVertexAttribArray(pos2);
        GLES20.glEnableVertexAttribArray(pos3);
        GLES20.glEnableVertexAttribArray(pos4);
        GLES20.glVertexAttribPointer(matrixHandle, 4, GLES20.GL_FLOAT, false, stride,
                BYTES_PER_FLOAT * 4);
        GLES20.glVertexAttribPointer(pos2, 4, GLES20.GL_FLOAT, false, stride, BYTES_PER_FLOAT * 8);
        GLES20.glVertexAttribPointer(pos3, 4, GLES20.GL_FLOAT, false, stride, BYTES_PER_FLOAT * 12);
        GLES20.glVertexAttribPointer(pos4, 4, GLES20.GL_FLOAT, false, stride, BYTES_PER_FLOAT * 16);

        int mMVMatrixHandle = GLES20.glGetUniformLocation(programHandle, "u_MVMatrix");
        GLES20.glUniformMatrix4fv(mMVMatrixHandle, 1, false, mvMatrix, 0);

        mLightPosHandle = GLES20.glGetUniformLocation(programHandle, "u_LightPos");
        // Pass in the light position in eye space.
        GLES20.glUniform3f(mLightPosHandle, lightPosInEyeSpace[0], lightPosInEyeSpace[1], lightPosInEyeSpace[2]);

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

    public float getxForce() {
        return xForce;
    }

    public void setxForce( float xForce ) {
        this.xForce = xForce;
    }

    public float getyForce() {
        return yForce;
    }

    public void setyForce( float yForce ) {
        this.yForce = yForce;
    }

    public float getMaxWindForce() {
        return maxWindForce;
    }

    public void setMaxWindForce(float maxWindForce) {
        this.maxWindForce = maxWindForce;
    }


    @Override
    public float[] createTransformationMatrix() {
        float[] transformation = new float[16];

        Matrix.setIdentityM(transformation, 0);

        Matrix.translateM(modelMatrix, 0, deltaX, deltaY, 0);

        Matrix.multiplyMM(transformation, 0, modelMatrix, 0, rotationMatrix, 0);

        getBounds().calculateBounds(transformation);

        return transformation;
    }

    @Override
    public void updatePosition(float x, float y) {
        deltaX = x;
        deltaY = y;

        xPos += deltaX;
        yPos += deltaY;

        if (firstUpdate) {
            setPrevUpdateTime(System.nanoTime());
            firstUpdate = false;
        }

        long currTime = System.nanoTime();
        time = (currTime - getPrevUpdateTime()) / 1000000000.0f;// += 0.01f;
        setPrevUpdateTime(currTime);

        float speedMultiplier = 2.0f;
        float windLength = 2.0f;
        //float lagPosition = mod((direction.x + lagTime*speedMultiplier*speed), windLength);
        for (int i = 0; i < NUM_PARTICLES; i++) {
            float speed = interleavedData[NUM_ATTRIBUTES * i + 3];

            float particleXpos = (speedMultiplier * speed * time) + interleavedData[NUM_ATTRIBUTES
                    * i];//particle moves laterally
            if (particleXpos >= windLength) { //if particle has reached the end of the wind stream
                interleavedData[NUM_ATTRIBUTES * i] =
                        particleXpos - windLength;//put particle back at start of wind stream
                //update the particles mvp matrix
                for (int j = 0; j < 16; j++) {
                    interleavedData[NUM_ATTRIBUTES * i + 4 + j] =
                            mvpMatrix[j];//displacementHistory.get(matrixIndex)[j];
                }
            } else {
                interleavedData[NUM_ATTRIBUTES * i] =
                        particleXpos; //update particle position in VBO
            }
        }
    }

    public void setRotationMatrix(float rotation) {
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

    private float[] lagMatrix = new float[16];

    private float xForce;

    private float yForce;

    private float maxWindForce = 2.0f;

    private float[] rotationMatrix;

    private float inwardRotation = 0;

    private boolean firstUpdate = true;

    private static final int NUM_PARTICLES = 5000;

    private static final int NUM_ATTRIBUTES = 20;
}