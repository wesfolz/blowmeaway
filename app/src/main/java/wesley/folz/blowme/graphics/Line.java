package wesley.folz.blowme.graphics;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;

/**
 * Created by wesley on 7/3/2015.
 */
public class Line extends Model
{
    public Line()
    {
        super();
/*        vertexData = new float[]{
                - 0.5f, 0.25f, 0.0f,   // top
                0.5f, 0.25f, 0.0f}; // top right
*/
        vertexOrder = new short[]{0, 1};
    }

    @Override
    public void draw()
    {
        // Add program to OpenGL ES environment
        GLES20.glUseProgram( mProgram );

        // get handle to vertex vertexshader's vPosition member
        mPositionHandle = GLES20.glGetAttribLocation( mProgram, "a_Position" );

        // Prepare the triangle coordinate data
        vertexBuffer.position( 0 );
        GLES20.glVertexAttribPointer( mPositionHandle, COORDS_PER_VERTEX,
                GLES20.GL_FLOAT, false,
                vertexStride, vertexBuffer );
        // Enable a handle to the triangle vertices
        GLES20.glEnableVertexAttribArray( mPositionHandle );

        // get handle to fragment vertexshader's vColor member
        mColorHandle = GLES20.glGetAttribLocation( mProgram, "a_Color" );
        //colorBuffer.position( 0 );
       // GLES20.glVertexAttribPointer( mColorHandle, 4, GLES20.GL_FLOAT, false,
         //       4, colorBuffer );

        GLES20.glEnableVertexAttribArray( mColorHandle );

        // get handle to shape's transformation matrix
        mMVPMatrixHandle = GLES20.glGetUniformLocation( mProgram, "u_MVPMatrix" );

        float[] mMVPMatrix = createTransformationMatrix();

        // Pass the projection and view transformation to the vertexshader
        GLES20.glUniformMatrix4fv( mMVPMatrixHandle, 1, false, mMVPMatrix, 0 );

        // Draw the triangle
        //GLES20.glDrawArrays( GLES20.GL_TRIANGLES, 0, vertexData.length );
        GLES20.glDrawElements( GLES20.GL_LINES, vertexOrder.length, GLES20.GL_UNSIGNED_SHORT,
                drawListBuffer );
        //GLES20.glDrawArrays( GLES20.GL_TRIANGLES, 0, 3 );

        // Disable vertex array
        //GLES20.glDisableVertexAttribArray( mPositionHandle );
    }

    @Override
    public float[] createTransformationMatrix()
    {
        Matrix.translateM( mvpMatrix, 0, deltaX, deltaY, 0 );
        return mvpMatrix;
    }

    @Override
    public void updatePosition( float x, float y )
    {
        deltaX = x;

        deltaY = - y;
    }

    private float deltaX;

    private float deltaY;
}
