package wesley.folz.blowme.graphics.effects;

import android.opengl.GLES20;
import android.opengl.Matrix;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import wesley.folz.blowme.R;
import wesley.folz.blowme.graphics.models.Model;
import wesley.folz.blowme.ui.GamePlayRenderer;
import wesley.folz.blowme.ui.MainApplication;
import wesley.folz.blowme.util.GraphicsUtilities;

/**
 * Created by Wesley on 9/25/2016.
 */

public abstract class ParticleSystem extends Model
{

    public ParticleSystem()
    {
        super();
        initialTime = System.nanoTime() / 1000000000.0f;
    }

    protected abstract void generateParticles();

    //must be called after glBindBuffer(GLES20.GL_ARRAY_BUFFER, dataVBO);
    protected void updateDataBuffer() {
        dataBuffer = ByteBuffer.allocateDirect(interleavedData.length * 4)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        dataBuffer.put(interleavedData).position(0);

        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, dataBuffer.capacity() * 4,
                dataBuffer, GLES20.GL_STATIC_DRAW);
    }

    @Override
    public void draw()
    {
        // Add program to OpenGL ES environment
        GLES20.glUseProgram(programHandle);
        //bind buffers
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, dataVBO);
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, orderVBO);

        //3 coords per vertex, 3 coords per normal, 4 coords per color, 2 coords per texture, 4 bytes per float
        final int stride = (COORDS_PER_COLOR + 4) * BYTES_PER_FLOAT;

        int colorHandle = GLES20.glGetAttribLocation(programHandle, "color");
        GLES20.glEnableVertexAttribArray(colorHandle);
        GLES20.glVertexAttribPointer(colorHandle, COORDS_PER_COLOR, GLES20.GL_FLOAT, false, stride, 0);

        //pass in direction vector to shader
        int directionHandle = GLES20.glGetAttribLocation(programHandle, "direction");
        GLES20.glEnableVertexAttribArray(directionHandle);
        GLES20.glVertexAttribPointer(directionHandle, 3, GLES20.GL_FLOAT, false, stride, (COORDS_PER_COLOR) * BYTES_PER_FLOAT);

        //pass in speed to shader
        int speedHandle = GLES20.glGetAttribLocation(programHandle, "speed");
        GLES20.glEnableVertexAttribArray(speedHandle);
        GLES20.glVertexAttribPointer(speedHandle, 1, GLES20.GL_FLOAT, false, stride, (COORDS_PER_COLOR + 3) * BYTES_PER_FLOAT);

        //texture
        int textureUniformHandle = GLES20.glGetUniformLocation(programHandle, "u_Texture");
        int secondTextureHandle = GLES20.glGetUniformLocation(programHandle, "second_Texture");

        // Set the active texture unit to texture unit 0.
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        // Bind the texture to this unit.
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureDataHandle);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, secondTexture);
        // Tell the texture uniform sampler to use this texture in the shader by binding to texture unit 0.
        GLES20.glUniform1i(textureUniformHandle, 0);
        GLES20.glUniform1i(secondTextureHandle, 1);
        GLES20.glEnable(GLES20.GL_TEXTURE_2D);

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

    /*
        public void resumeGame()
        {
            resuming = paused;
            paused = false;
        }
    */
    @Override
    public void enableGraphics(GraphicsUtilities graphicsData) {
        dataBuffer = ByteBuffer.allocateDirect(interleavedData.length * 4)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        dataBuffer.put(interleavedData).position(0);

        // initialize byte buffer for the draw list
        ByteBuffer dlb = ByteBuffer.allocateDirect(
                // (# of coordinate values * 2 bytes per short)
                vertexOrder.length * 2);
        dlb.order(ByteOrder.nativeOrder());
        drawListBuffer = dlb.asShortBuffer();
        drawListBuffer.put(vertexOrder);
        drawListBuffer.position(0);

        vertexShader = GamePlayRenderer.loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode);
        fragmentShader = GamePlayRenderer.loadShader(GLES20.GL_FRAGMENT_SHADER,
                fragmentShaderCode);

        // create empty OpenGL ES Program
        programHandle = GamePlayRenderer.createAndLinkProgram(vertexShader, fragmentShader,
                new String[]{"position", "direction", "normalVector", "speed", "color"});

        int[] buffers = new int[2];
        GLES20.glGenBuffers(2, buffers, 0);

        //vertex coordinates, normal vectors, color, texture coordinates
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, buffers[0]);
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, dataBuffer.capacity() * 4,
                dataBuffer, GLES20.GL_STATIC_DRAW);

        //vertex order
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, buffers[1]);
        GLES20.glBufferData(GLES20.GL_ELEMENT_ARRAY_BUFFER, drawListBuffer.capacity() * 2, drawListBuffer,
                GLES20.GL_STATIC_DRAW);

        //texture data
        textureDataHandle = GraphicsUtilities.loadTexture(MainApplication.getAppContext(), this.TEXTURE_RESOURCE);
        secondTexture = GraphicsUtilities.loadTexture(MainApplication.getAppContext(),
                R.drawable.grey_circle);

        // IMPORTANT: Unbind from the buffer when we're done with it.
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0);

        //get references to gl buffers
        dataVBO = buffers[0];
        orderVBO = buffers[1];
    }

    @Override
    public float[] createTransformationMatrix()
    {
        float[] mvp = new float[16];
        Matrix.setIdentityM(mvp, 0);

        //copy modelMatrix to a separate matrix for return, (returning modelMatrix doesn't work)
        Matrix.multiplyMM(mvp, 0, modelMatrix, 0, mvp, 0);

        return mvp;
    }

    @Override
    public void updatePosition(float x, float y)
    {
        //time = (System.nanoTime() - initialTime) / 1000000000.0f;
        //time = (System.nanoTime()) / 1000000000.0f;

        Log.e("time ", "time " + time + "initial time " + initialTime);
        //time = 0;
        time += 0.01f;
    }

    private float initialTime;

    protected float time;

    int secondTexture;
}
