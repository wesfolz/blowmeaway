package wesley.folz.blowme.graphics;

import android.opengl.GLES20;
import android.opengl.Matrix;

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

        // get handle to vertex vertexshader's vPosition member
        mPositionHandle = GLES20.glGetAttribLocation( mProgram, "position" );

        // Prepare the triangle coordinate data
        vertexBuffer.position( 0 );

        GLES20.glVertexAttribPointer(mPositionHandle, COORDS_PER_VERTEX,
                GLES20.GL_FLOAT, false,
                vertexStride, vertexBuffer);
        // Enable a handle to the triangle vertices
        GLES20.glEnableVertexAttribArray(mPositionHandle);

        if(normalData != null)
        {
            mNormalHandle = GLES20.glGetAttribLocation(mProgram, "normalVector");

            GLES20.glVertexAttribPointer(mNormalHandle, COORDS_PER_VERTEX,
                    GLES20.GL_FLOAT, false,
                    vertexStride, normalBuffer);
            // Enable a handle to the triangle vertices
            GLES20.glEnableVertexAttribArray(mNormalHandle);
        }
        // get handle to fragment vertexshader's vColor member
        //mColorHandle = GLES20.glGetAttribLocation( mProgram, "color" );
        //colorBuffer.position(0);
        //GLES20.glVertexAttribPointer(mColorHandle, 4, GLES20.GL_FLOAT, false,
        //        4, colorBuffer);

        // get handle to fragment vertexshader's vColor member
        mColorHandle = GLES20.glGetUniformLocation(mProgram, "color");
        //GLES20.glGenTextures();

        // Set color for drawing the triangle
        GLES20.glUniform4fv(mColorHandle, 1, color, 0);

        //GLES20.glEnableVertexAttribArray( mColorHandle );

        // get handle to shape's transformation matrix
        mMVPMatrixHandle = GLES20.glGetUniformLocation( mProgram, "u_MVPMatrix" );

        float[] mMVPMatrix = createTransformationMatrix();

        // Pass the projection and view transformation to the vertexshader
        GLES20.glUniformMatrix4fv( mMVPMatrixHandle, 1, false, mMVPMatrix, 0 );

        // Draw the triangle
        //GLES20.glDrawArrays( GLES20.GL_TRIANGLES, 0, vertexData.length );
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, vertexOrder.length, GLES20.GL_UNSIGNED_SHORT,
                drawListBuffer);

//        GamePlayRenderer.checkGlError("glDrawElements");
        //GLES20.glDrawArrays( GLES20.GL_TRIANGLES, 0, 3 );

        // Disable vertex array
        //GLES20.glDisableVertexAttribArray( mPositionHandle );
    }

    public void enableGraphics()
    {
        ByteBuffer bb = ByteBuffer.allocateDirect( vertexData.length * 4 );
        bb.order( ByteOrder.nativeOrder() );
        vertexBuffer = bb.asFloatBuffer();
        vertexBuffer.put( vertexData );
        vertexBuffer.position(0);

        // initialize byte buffer for the draw list
        ByteBuffer dlb = ByteBuffer.allocateDirect(
                // (# of coordinate values * 2 bytes per short)
                vertexOrder.length * 2 );
        dlb.order( ByteOrder.nativeOrder() );
        drawListBuffer = dlb.asShortBuffer();
        drawListBuffer.put(vertexOrder);
        drawListBuffer.position(0);

        if (normalData != null)
        {
            ByteBuffer nb = ByteBuffer.allocateDirect(normalData.length * 4);
            nb.order(ByteOrder.nativeOrder());
            normalBuffer = nb.asFloatBuffer();
            normalBuffer.put(normalData);
            normalBuffer.position(0);
        }

        ByteBuffer cb = ByteBuffer.allocateDirect( colorData.length * 4 );
        cb.order( ByteOrder.nativeOrder() );
        colorBuffer = cb.asFloatBuffer();
        colorBuffer.put(colorData);
        colorBuffer.position( 0 );

//        Log.e("shader", vertexShaderCode);
//        Log.e("shader", fragmentShaderCode);

        int vertexShader = GamePlayRenderer.loadShader( GLES20.GL_VERTEX_SHADER, vertexShaderCode );
        int fragmentShader = GamePlayRenderer.loadShader( GLES20.GL_FRAGMENT_SHADER,
                fragmentShaderCode );

        // create empty OpenGL ES Program
        mProgram = GLES20.glCreateProgram();

        // add the vertex vertexshader to program
        GLES20.glAttachShader( mProgram, vertexShader );

        // add the fragment vertexshader to program
        GLES20.glAttachShader( mProgram, fragmentShader );

        // Bind attributes
        GLES20.glBindAttribLocation( mProgram, 0, "position" );
        GLES20.glBindAttribLocation( mProgram, 1, "normalVector" );

        //GLES20.glBindAttribLocation( mProgram, 1, "color" );

        // creates OpenGL ES program executables
        GLES20.glLinkProgram( mProgram );
    }


    public void initializeMatrix()
    {
        // Set the camera position (View matrix)
        Matrix.setLookAtM( viewMatrix, 0, 0, 0, 5, 0, 0, 0, 0, 1.0f, 0 );
        //set model view projection matrix
        Matrix.multiplyMM( mvpMatrix, 0, projectionMatrix, 0, viewMatrix, 0 );
    }

    public void setNormalData( float[] data )
    {
        normalData = data;
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

    public void setNormalOrder( short[] normalOrder )
    {
        this.normalOrder = normalOrder;
    }

    public void setProjectionMatrix( float[] projectionMatrix )
    {
        this.projectionMatrix = projectionMatrix;
    }

    public void setVertexData( float[] data )
    {
        vertexData = data;
    }


    public void setVertexOrder( short[] order )
    {
        vertexOrder = order;
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
    protected float[] colorData = {
            1.0f, 0.0f, 0.0f, 1.0f,
            0.0f, 0.0f, 1.0f, 1.0f,
            0.0f, 1.0f, 0.0f, 1.0f
    };
    float color[] = { 0.63671875f, 0.76953125f, 0.22265625f, 0.0f };

    /**
     * Center x position of model
     */
    protected float xPos;

    /**
     * Center y position of model
     */
    protected float yPos;

    protected final float[] mvpMatrix = new float[16];

    protected float[] normalData;

    protected float[] projectionMatrix;

    protected float[] vertexData;

    protected final float[] viewMatrix = new float[16];

    protected FloatBuffer colorBuffer;

    protected FloatBuffer normalBuffer;

    protected FloatBuffer vertexBuffer;

    protected static final int COORDS_PER_VERTEX = 3;

    protected int mProgram;

    protected int mPositionHandle;

    protected int mColorHandle;

    protected int mNormalHandle;

    protected int mMVPMatrixHandle;

    protected final int vertexStride = COORDS_PER_VERTEX * 4; // 4 bytes per vertex

    private short[] normalOrder;

    protected short[] vertexOrder;

    protected ShortBuffer drawListBuffer;

    private Bounds bounds;

    private float[] size;

    public int OBJ_FILE_RESOURCE;

    public int VERTEX_SHADER;

    public int FRAGMENT_SHADER;
}
