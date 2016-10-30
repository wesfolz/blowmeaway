package wesley.folz.blowme.graphics;

import android.opengl.GLES20;
import android.opengl.Matrix;

import wesley.folz.blowme.graphics.models.Model;
import wesley.folz.blowme.util.GraphicsUtilities;

/**
 * Created by wesley on 7/3/2015.
 */
public class Line extends Model
{
    public Line()
    {
        super();
        interleavedData = new float[]{
                -0.5f, 0.0f, 0.0f,   // left
                0.0f, 0.0f, 1.0f,     //normal
                0.5f, 0.0f, 0.0f,     // right
                0.0f, 0.0f, 1.0f};    //normal

        vertexOrder = new short[]{0, 1};
    }

    @Override
    public void enableGraphics(GraphicsUtilities graphicsData)
    {
        super.enableGraphics(graphicsData);
        programHandle = graphicsData.shaderProgramIdMap.get("lighting");
    }

    @Override
    public void draw()
    {
        // Add program to OpenGL ES environment
        GLES20.glUseProgram(programHandle);

        //3 coords per vertex, 3 coords per normal, 4 coords per color, 2 coords per texture, 4 bytes per float
        final int stride = (COORDS_PER_VERTEX + COORDS_PER_NORMAL) * BYTES_PER_FLOAT;

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
        GLES20.glDrawElements(GLES20.GL_LINES, numVertices, GLES20.GL_UNSIGNED_SHORT, 0);

        //unbind buffers
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0);
    }

    @Override
    public float[] createTransformationMatrix()
    {
        float[] mvp = new float[16];

        Matrix.setIdentityM(mvp, 0);

        //Matrix.translateM( modelMatrix, 0, deltaX, deltaY, 0 );

        Matrix.multiplyMM(mvp, 0, modelMatrix, 0, mvp, 0);

        return mvp;
    }

    @Override
    public void updatePosition( float x, float y )
    {
        //deltaX = x;

        //deltaY = y;
    }

    private float deltaX;

    private float deltaY;
}
