package wesley.folz.blowme.graphics.models;

import android.opengl.GLES20;
import android.opengl.Matrix;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import wesley.folz.blowme.R;
import wesley.folz.blowme.util.Bounds;
import wesley.folz.blowme.util.GraphicsUtilities;

/**
 * Created by wesley on 5/11/2015.
 */
public abstract class Model
{
    public Model()
    {
        //GraphicsUtilities.readOBJFile( this );
        this.VERTEX_SHADER = R.raw.defaultvertexshader;
        this.FRAGMENT_SHADER = R.raw.defaultfragmentshader;
        this.TEXTURE_RESOURCE = R.raw.wood_texture;
        GraphicsUtilities.readShader(this);
    }

    public void draw()
    {
        // Add program to OpenGL ES environment
        GLES20.glUseProgram(programHandle);

        //3 coords per vertex, 3 coords per normal, 4 coords per color, 2 coords per texture, 4 bytes per float
        final int stride = (COORDS_PER_VERTEX + COORDS_PER_NORMAL + COORDS_PER_COLOR + COORDS_PER_TEX) * BYTES_PER_FLOAT;

        //vertices
        int vertexPositionHandle = GLES20.glGetAttribLocation(programHandle, "position");
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, dataVBO);
        GLES20.glEnableVertexAttribArray(vertexPositionHandle);
        GLES20.glVertexAttribPointer(vertexPositionHandle, COORDS_PER_VERTEX, GLES20.GL_FLOAT, false, stride, 0);

        //normal vectors
        int normalVectorHandle = GLES20.glGetAttribLocation(programHandle, "normalVector");
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, dataVBO);
        GLES20.glEnableVertexAttribArray(normalVectorHandle);
        GLES20.glVertexAttribPointer(normalVectorHandle, COORDS_PER_NORMAL, GLES20.GL_FLOAT, false, stride, COORDS_PER_VERTEX * BYTES_PER_FLOAT);

        //colors
        int colorHandle = GLES20.glGetAttribLocation(programHandle, "color");
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, dataVBO);
        GLES20.glEnableVertexAttribArray(colorHandle);
        GLES20.glVertexAttribPointer(colorHandle, COORDS_PER_COLOR, GLES20.GL_FLOAT, false, stride, (COORDS_PER_VERTEX + COORDS_PER_NORMAL) * BYTES_PER_FLOAT);

        //texture
        int textureUniformHandle = GLES20.glGetUniformLocation(programHandle, "u_Texture");

        // Set the active texture unit to texture unit 0.
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);

        // Bind the texture to this unit.
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureDataHandle);

        // Tell the texture uniform sampler to use this texture in the shader by binding to texture unit 0.
        GLES20.glUniform1i(textureUniformHandle, 0);

        int textureCoordinateHandle = GLES20.glGetAttribLocation(programHandle, "a_TexCoordinate");
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, dataVBO);
        GLES20.glEnableVertexAttribArray(textureCoordinateHandle);
        GLES20.glVertexAttribPointer(textureCoordinateHandle, COORDS_PER_TEX, GLES20.GL_FLOAT, false, stride, (COORDS_PER_VERTEX + COORDS_PER_NORMAL + COORDS_PER_COLOR) * BYTES_PER_FLOAT);

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

        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, orderVBO);

        // Draw the triangle
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, numVertices, GLES20.GL_UNSIGNED_SHORT, 0);

        //unbind buffers
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0);
    }

    public void enableGraphics(GraphicsUtilities graphicsData)
    {
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


        // IMPORTANT: Unbind from the buffer when we're done with it.
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0);

        //get references to gl buffers
        dataVBO = buffers[0];
        orderVBO = buffers[1];

        numVertices = vertexOrder.length;
    }

    public void initializeMatrices(float[] viewMatrix, float[] projectionMatrix, float[] lightPosInEyeSpace)
    {
        this.setViewMatrix(viewMatrix);
        this.setProjectionMatrix(projectionMatrix);
        this.setLightPosInEyeSpace(lightPosInEyeSpace);

        //only call if resuming from pause state
        if (!resuming)
        {
            //initialize model matrix
            Matrix.setIdentityM(modelMatrix, 0);
            //translate model to initial position
            Matrix.translateM(modelMatrix, 0, initialXPos, initialYPos, 0);
        }
    }

    public void pauseGame()
    {
        paused = true;
    }

    public void resumeGame()
    {
        //Log.e("blowme", String.valueOf(paused));
        resuming = paused;
        paused = false;
    }

    public Bounds getBounds()
    {
        return bounds;
    }

    public void setBounds(Bounds bounds)
    {
        this.bounds = bounds;
    }

    public float[] getSize()
    {
        return size;
    }

    public void setSize(float[] size)
    {
        this.size = size;
    }

    public void setProjectionMatrix(float[] projectionMatrix)
    {
        this.projectionMatrix = projectionMatrix;
    }

    public void setViewMatrix(float[] viewMatrix)
    {
        this.viewMatrix = viewMatrix;
    }

    public void setLightPosInEyeSpace(float[] lightPosInEyeSpace)
    {
        this.lightPosInEyeSpace = lightPosInEyeSpace;
    }

    public void setVertexOrder(short[] order)
    {
        vertexOrder = order;
    }

    public void setInterleavedData(float[] data)
    {
        this.interleavedData = data;
    }

    public float getxPos()
    {
        return xPos;
    }

    public float getyPos()
    {
        return yPos;
    }

    public abstract float[] createTransformationMatrix();

    public abstract void updatePosition(float x, float y);

    public boolean initializationRoutine()
    {
        return true;
    }

    public String fragmentShaderCode;
    public String vertexShaderCode;

    /**
     * Center x position of model
     */
    protected float xPos;

    /**
     * Center y position of model
     */
    protected float yPos;

    protected float initialXPos;

    protected float initialYPos;

    protected float[] modelMatrix = new float[16];

    protected float[] projectionMatrix;

    protected float[] interleavedData;

    protected float[] viewMatrix = new float[16];

    protected FloatBuffer dataBuffer;

    protected static final int COORDS_PER_VERTEX = 3;

    protected static final int COORDS_PER_NORMAL = 3;

    protected static final int COORDS_PER_COLOR = 4;

    protected static final int COORDS_PER_TEX = 2;

    protected static final int BYTES_PER_FLOAT = 4;

    protected int programHandle;

    protected int mLightPosHandle;
    protected int vertexShader;
    protected int fragmentShader;

    protected int dataVBO;
    protected int orderVBO;

    private boolean paused = false;

    protected boolean resuming = false;

    protected float[] mvMatrix = new float[16];

    protected float[] mvpMatrix = new float[16];

    protected float[] lightPosInEyeSpace = new float[4];

    protected int textureDataHandle;

    protected float scaleFactor = 1.0f;

    protected short[] vertexOrder;

    protected ShortBuffer drawListBuffer;

    protected int numVertices;

    private Bounds bounds;

    private float[] size;

    public int TEXTURE_RESOURCE;

    public int OBJ_FILE_RESOURCE;

    public int VERTEX_SHADER;

    public int FRAGMENT_SHADER;
}
