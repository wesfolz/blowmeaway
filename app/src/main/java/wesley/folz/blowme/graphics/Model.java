package wesley.folz.blowme.graphics;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.SystemClock;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import wesley.folz.blowme.R;
import wesley.folz.blowme.ui.GamePlayRenderer;
import wesley.folz.blowme.util.Bounds;
import wesley.folz.blowme.util.GraphicsReader;

/**
 * Created by wesley on 5/11/2015.
 */
public abstract class Model
{
    public Model()
    {
        //GraphicsReader.readOBJFile( this );
        this.VERTEX_SHADER = R.raw.defaultvertexshader;
        this.FRAGMENT_SHADER = R.raw.defaultfragmentshader;
        GraphicsReader.readShader(this);
    }

    public void draw()
    {
        // Add program to OpenGL ES environment
        GLES20.glUseProgram(mProgram);

        float[] mLightPosInWorldSpace = new float[4];
        float[] mLightPosInModelSpace = new float[] {0.0f, 0.0f, 0.0f, 1.0f};
        float[] mLightModelMatrix = new float[16];

        // Calculate position of the light. Push into the distance.
        Matrix.setIdentityM(mLightModelMatrix, 0);
        Matrix.translateM(mLightModelMatrix, 0, 0.0f, 0.0f, 3.0f);

        Matrix.multiplyMV(mLightPosInWorldSpace, 0, mLightModelMatrix, 0, mLightPosInModelSpace, 0);
        Matrix.multiplyMV(mLightPosInEyeSpace, 0, viewMatrix, 0, mLightPosInWorldSpace, 0);

        final int stride = (3 + 3 + 4) * 4;

        mPositionHandle = GLES20.glGetAttribLocation( mProgram, "position" );
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, dataVBO);
        GLES20.glEnableVertexAttribArray(mPositionHandle);
        GLES20.glVertexAttribPointer(mPositionHandle, 3, GLES20.GL_FLOAT, false, stride, 0);

        mNormalHandle = GLES20.glGetAttribLocation(mProgram, "normalVector");
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, dataVBO);
        GLES20.glEnableVertexAttribArray(mNormalHandle);
        GLES20.glVertexAttribPointer(mNormalHandle, 3, GLES20.GL_FLOAT, false, stride, 3 * 4);

        mColorHandle = GLES20.glGetAttribLocation(mProgram, "color");
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, dataVBO);
        GLES20.glEnableVertexAttribArray(mColorHandle);
        GLES20.glVertexAttribPointer(mColorHandle, 4, GLES20.GL_FLOAT, false, stride, (3+4) * 4);

        // get handle to shape's transformation matrix
        mMVPMatrixHandle = GLES20.glGetUniformLocation( mProgram, "u_MVPMatrix" );

        float[] mMVPMatrix = createTransformationMatrix();
        float[] mvMatrix = new float[16];
        //creating model-view matrix
        Matrix.multiplyMM(mvMatrix, 0, viewMatrix, 0, mMVPMatrix, 0);
        //creating model-view-projection matrix
        Matrix.multiplyMM(mMVPMatrix, 0, projectionMatrix, 0, mvMatrix, 0);
        //TODO: scaling mvMatrix messes up shader, so scaling must be done last, not sure why
        Matrix.scaleM(mMVPMatrix, 0, scaleFactor, scaleFactor, scaleFactor);

        // Pass the projection and view transformation to the vertexshader
        GLES20.glUniformMatrix4fv( mMVPMatrixHandle, 1, false, mMVPMatrix, 0 );

        mLightPosHandle = GLES20.glGetUniformLocation(mProgram, "u_LightPos");

        int mMVMatrixHandle = GLES20.glGetUniformLocation(mProgram, "u_MVMatrix");
        GLES20.glUniformMatrix4fv( mMVMatrixHandle, 1, false, mvMatrix, 0 );

        mLightPosHandle = GLES20.glGetUniformLocation(mProgram, "u_LightPos");

        // Pass in the light position in eye space.
        GLES20.glUniform3f(mLightPosHandle, mLightPosInEyeSpace[0], mLightPosInEyeSpace[1], mLightPosInEyeSpace[2]);

        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, orderVBO);

        // Draw the triangle
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, vertexOrder.length, GLES20.GL_UNSIGNED_SHORT, 0);

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0);

//        GamePlayRenderer.checkGlError("glDrawElements");
    }

    public void enableGraphics()
    {
        dataBuffer = ByteBuffer.allocateDirect(interleavedData.length * 4)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        dataBuffer.put(interleavedData).position(0);

        // initialize byte buffer for the draw list
        ByteBuffer dlb = ByteBuffer.allocateDirect(
                // (# of coordinate values * 2 bytes per short)
                vertexOrder.length * 2 );
        dlb.order( ByteOrder.nativeOrder() );
        drawListBuffer = dlb.asShortBuffer();
        drawListBuffer.put(vertexOrder);
        drawListBuffer.position(0);

        vertexShader = GamePlayRenderer.loadShader( GLES20.GL_VERTEX_SHADER, vertexShaderCode );
        fragmentShader = GamePlayRenderer.loadShader( GLES20.GL_FRAGMENT_SHADER,
                fragmentShaderCode );

        // create empty OpenGL ES Program
        mProgram = createAndLinkProgram(vertexShader, fragmentShader,
                new String[]{"position", "color", "normalVector"});

        int[] buffers = new int[2];
        GLES20.glGenBuffers(2, buffers, 0);

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, buffers[0]);
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, dataBuffer.capacity() * 4,
                dataBuffer, GLES20.GL_STATIC_DRAW);

        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, buffers[1]);
        GLES20.glBufferData(GLES20.GL_ELEMENT_ARRAY_BUFFER, drawListBuffer.capacity() * 2, drawListBuffer,
                GLES20.GL_STATIC_DRAW);

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0);


        // IMPORTANT: Unbind from the buffer when we're done with it.
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0);

        dataVBO = buffers[0];
        orderVBO = buffers[1];
    }

    public void initializeMatrix()
    {
        // Set the camera position (View matrix)
        Matrix.setLookAtM( viewMatrix, 0, 0, 0, 5f, 0, 0, 0.0f, 0, 1.0f, 0 );
        //initialize model matrix
        Matrix.setIdentityM(modelMatrix, 0);
    }

    /**
     * Helper function to compile and link a program.
     *
     * @param vertexShaderHandle An OpenGL handle to an already-compiled vertex shader.
     * @param fragmentShaderHandle An OpenGL handle to an already-compiled fragment shader.
     * @param attributes Attributes that need to be bound to the program.
     * @return An OpenGL handle to the program.
     */
    private int createAndLinkProgram(final int vertexShaderHandle, final int fragmentShaderHandle, final String[] attributes)
    {
        int programHandle = GLES20.glCreateProgram();

        if (programHandle != 0)
        {
            // Bind the vertex shader to the program.
            GLES20.glAttachShader(programHandle, vertexShaderHandle);

            // Bind the fragment shader to the program.
            GLES20.glAttachShader(programHandle, fragmentShaderHandle);

            // Bind attributes
            if (attributes != null)
            {
                final int size = attributes.length;
                for (int i = 0; i < size; i++)
                {
                    GLES20.glBindAttribLocation(programHandle, i, attributes[i]);
                }
            }

            // Link the two shaders together into a program.
            GLES20.glLinkProgram(programHandle);

            // Get the link status.
            final int[] linkStatus = new int[1];
            GLES20.glGetProgramiv(programHandle, GLES20.GL_LINK_STATUS, linkStatus, 0);

            // If the link failed, delete the program.
            if (linkStatus[0] == 0)
            {
                Log.e("openGl", "Error compiling program: " + GLES20.glGetProgramInfoLog(programHandle));
                Log.e("openGl", "Error compiling program: " + GLES20.glGetShaderInfoLog(vertexShaderHandle));
                Log.e("openGl", "Error compiling program: " + GLES20.glGetShaderInfoLog(fragmentShaderHandle));

                GLES20.glDeleteProgram(programHandle);
                programHandle = 0;
            }
        }

        if (programHandle == 0)
        {
            throw new RuntimeException("Error creating program.");
        }

        return programHandle;
    }


    public Bounds getBounds()
    {
        return bounds;
    }

    public void setBounds( Bounds bounds )
    {
        this.bounds = bounds;
    }

    public float[] getSize()
    {
        return size;
    }

    public void setSize( float[] size )
    {
        this.size = size;
    }

    public void setProjectionMatrix( float[] projectionMatrix )
    {
        this.projectionMatrix = projectionMatrix;
    }

    public void setVertexOrder( short[] order )
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

    public abstract void updatePosition( float x, float y );

    public String fragmentShaderCode;
    public String vertexShaderCode;
    protected float[] colorData = { 0.63671875f, 0.76953125f, 0.22265625f, 0.0f };
    float color[] = { 0.63671875f, 0.76953125f, 0.22265625f, 0.0f };

    /**
     * Center x position of model
     */
    protected float xPos;

    /**
     * Center y position of model
     */
    protected float yPos;

    protected float[] modelMatrix = new float[16];

    protected float[] projectionMatrix;

    protected float[] interleavedData;

    protected final float[] viewMatrix = new float[16];

    protected FloatBuffer vertexBuffer;

    protected FloatBuffer dataBuffer;

    protected static final int COORDS_PER_VERTEX = 3;

    protected int mProgram;

    private int mLightPosHandle;
    private int vertexShader;
    private int fragmentShader;

    private int dataVBO;
    private int orderVBO;

    private float[] mLightPosInEyeSpace = new float[4];

    protected int mPositionHandle;

    protected int mColorHandle;

    protected int mNormalHandle;

    protected int mMVPMatrixHandle;

    protected final int vertexStride = COORDS_PER_VERTEX * 4; // 4 bytes per vertex

    protected float scaleFactor = 1.0f;

    protected short[] vertexOrder;

    protected ShortBuffer drawListBuffer;

    private Bounds bounds;

    private float[] size;

    public int OBJ_FILE_RESOURCE;

    public int VERTEX_SHADER;

    public int FRAGMENT_SHADER;
}
