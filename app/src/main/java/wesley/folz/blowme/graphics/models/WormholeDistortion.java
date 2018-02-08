package wesley.folz.blowme.graphics.models;

import android.opengl.GLES20;
import android.opengl.Matrix;

import wesley.folz.blowme.ui.GamePlayActivity;
import wesley.folz.blowme.util.Bounds;
import wesley.folz.blowme.util.GraphicsUtilities;

/**
 * Created by Wesley on 10/24/2017.
 */

public class WormholeDistortion extends Model {
    public WormholeDistortion() {
        this(0.0f, 0.0f);
    }

    public WormholeDistortion(float x, float y) {
        super();
        xPos = x;
        yPos = 0;
        initialXPos = x;
        initialYPos = y;
        initialZPos = -0.5f;
        scaleFactor = 0.1f;
        setBounds(new Bounds());
        getBounds().setBounds(initialXPos - scaleFactor, initialYPos - scaleFactor,
                initialXPos + scaleFactor, initialYPos + scaleFactor);
        scaleFactor = 0.3f;
    }

    @Override
    public void draw() {
        // Add program to OpenGL ES environment
        GLES20.glUseProgram(programHandle);

        //3 coords per vertex, 3 coords per normal, 4 coords per color, 2 coords per texture, 4
        // bytes per float
        final int stride =
                (COORDS_PER_VERTEX + COORDS_PER_NORMAL + COORDS_PER_TEX) * BYTES_PER_FLOAT;

        //vertices
        int vertexPositionHandle = GLES20.glGetAttribLocation(programHandle, "position");
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, dataVBO);
        GLES20.glEnableVertexAttribArray(vertexPositionHandle);
        GLES20.glVertexAttribPointer(vertexPositionHandle, COORDS_PER_VERTEX, GLES20.GL_FLOAT,
                false, stride, 0);

        //normal vectors
        int normalVectorHandle = GLES20.glGetAttribLocation(programHandle, "normalVector");
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, dataVBO);
        GLES20.glEnableVertexAttribArray(normalVectorHandle);
        GLES20.glVertexAttribPointer(normalVectorHandle, COORDS_PER_NORMAL, GLES20.GL_FLOAT, false,
                stride, (COORDS_PER_VERTEX + COORDS_PER_TEX) * BYTES_PER_FLOAT);

        //texture
        int textureUniformHandle = GLES20.glGetUniformLocation(programHandle, "u_Texture");

        // Set the active texture unit to texture unit 0.
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);

        // Bind the texture to this unit.
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureDataHandle);

        // Tell the texture uniform sampler to use this texture in the shader by binding to
        // texture unit 0.
        GLES20.glUniform1i(textureUniformHandle, 0);

        int textureCoordinateHandle = GLES20.glGetAttribLocation(programHandle, "a_TexCoordinate");
        GLES20.glEnableVertexAttribArray(textureCoordinateHandle);
        GLES20.glVertexAttribPointer(textureCoordinateHandle, COORDS_PER_TEX, GLES20.GL_FLOAT,
                false, stride, (COORDS_PER_VERTEX) * BYTES_PER_FLOAT);

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
            //Matrix.scaleM(mvpMatrix, 0, 3, 3, 3);
        } else {
            this.resuming = false;
        }

        // Pass the projection and view transformation to the vertexshader
        GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false, mvpMatrix, 0);

        int textureMatrixHandle = GLES20.glGetUniformLocation(programHandle, "tex_TransMatrix");
        GLES20.glUniformMatrix4fv(textureMatrixHandle, 1, false, calculateTextureMatrix(), 0);

        mLightPosHandle = GLES20.glGetUniformLocation(programHandle, "u_LightPos");

        int mMVMatrixHandle = GLES20.glGetUniformLocation(programHandle, "u_MVMatrix");
        GLES20.glUniformMatrix4fv(mMVMatrixHandle, 1, false, mvMatrix, 0);

        mLightPosHandle = GLES20.glGetUniformLocation(programHandle, "u_LightPos");

        // Pass in the light position in eye space.
        GLES20.glUniform3f(mLightPosHandle, lightPosInEyeSpace[0], lightPosInEyeSpace[1],
                lightPosInEyeSpace[2]);

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, dataVBO);
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, orderVBO);

        // Draw the triangles
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, numVertices, GLES20.GL_UNSIGNED_SHORT, 0);

        //unbind buffers
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0);
    }

    protected float[] calculateTextureMatrix() {
        float[] texMat = new float[16];
        Matrix.setIdentityM(texMat, 0);
        //scale factor = 0.05, 0.05*5=0.25, wormhole is about 1/4 the size of the background
        //Matrix.scaleM(texMat, 0, 0.0125f/scaleFactor, 0.0125f/scaleFactor, 1.0f);

        //x = initialXPos + 1.5, y = -1.5 - 2*ini + 2*tex
        //texture coordinates are inverted from screen coordinates that's why initalYPos is
        // multiplied by -1
        //y dimension is approximately twice the size of the x dimension, but the texture
        // coordinates are equal in both directions
        //that's why initialYPos and texYPos are multiplied by 2
        //Matrix.translateM(texMat, 0, initialXPos + (1 / (scaleFactor * 10.0f) - 0.5f),
        //       -(1 / (scaleFactor * 10.0f) - 0.5f) - 2.0f * initialYPos + 2 * texYPos, 0);

        //Matrix.translateM(texMat, 0, initialXPos + 1.5f,
        //        -2.0f * initialYPos + 2 * texYPos - 1.5f, 0);
        Matrix.scaleM(texMat, 0, scaleFactor, scaleFactor, 1.0f);
        //If the texture coordinates are scaled, only [0,1] is mapped so the center needs to be
        // shifted over, see 2 examples below:
        //scaleFactor = 0.5 -> scale = 1/scaleFactor = 2 -> center needs to shift from 0.5 to 1
        // -> add 0.5
        //scaleFactor = 0.25 -> scale = 1/scaleFactor = 4 -> center needs to shift from 0.5 to 2
        // -> add 1.5
        //When the wormhole and background texture are the same size (i.e. scaleFactor = 1),
        //but background coords are in [-1,1] and tex coords are in [0,1], so tex coords =
        // 0.5*background coords
        //When the tex coords are scaled, then the ratio between tex coords and background coords
        // changes by 1/scaleFactor
        //texture coordinates are inverted from screen coordinates that's why initalYPos is
        // multiplied by -1
        Matrix.translateM(texMat, 0,
                0.5f * initialXPos / scaleFactor + (1 / (2.0f * scaleFactor) - 0.5f),
                -0.5f * initialYPos / scaleFactor + 0.5f * texYPos / scaleFactor - (
                        1 / (2.0f * scaleFactor) - 0.5f), 0);

        //Matrix.multiplyMM(texMat, 0, background.modelMatrix, 0, texMat, 0);
        return texMat;
    }

    @Override
    public void enableGraphics(GraphicsUtilities graphicsData) {
        dataVBO = graphicsData.modelVBOMap.get("wormhole");
        orderVBO = graphicsData.orderVBOMap.get("wormhole");
        numVertices = graphicsData.numVerticesMap.get("wormhole");
        programHandle = graphicsData.shaderProgramIdMap.get("distortion");
        textureDataHandle = graphicsData.textureIdMap.get("planet");
    }

    @Override
    public float[] createTransformationMatrix() {
        float[] mvp = new float[16];
        Matrix.setIdentityM(mvp, 0);
        Matrix.multiplyMM(mvp, 0, modelMatrix, 0, mvp, 0);
        Matrix.scaleM(mvp, 0, scaleCount, scaleCount, scaleCount);
        return mvp;
    }

    @Override
    public boolean initializationRoutine() {
        if (initialized) {
            updatePosition(0, 0);
            return true;
        }
        if (initialTime == 0) {
            initialTime = System.currentTimeMillis();
        }
        long time = System.currentTimeMillis();
        float deltaTime = (time - initialTime) / 1000.0f;

        if (deltaTime < GamePlayActivity.INITIALIZATION_TIME) {
            scaleCount = (deltaTime / GamePlayActivity.INITIALIZATION_TIME);
        } else {
            scaleCount = 1.0f;
            initialTime = 0;
            initialized = true;
        }
        return deltaTime >= GamePlayActivity.INITIALIZATION_TIME;
    }

    @Override
    public boolean removalRoutine() {
        if (offscreen) {
            return true;
        }
        if (initialTime == 0) {
            initialTime = System.currentTimeMillis();
        }
        long time = System.currentTimeMillis();
        float deltaTime = (time - initialTime) / 1000.0f;

        if (deltaTime < GamePlayActivity.INITIALIZATION_TIME) {
            scaleCount = ((GamePlayActivity.INITIALIZATION_TIME - deltaTime)
                    / GamePlayActivity.INITIALIZATION_TIME);
        } else {
            scaleCount = 0.0f;
            initialTime = 0;
            offscreen = true;
        }
        return deltaTime >= GamePlayActivity.INITIALIZATION_TIME;
    }

    @Override
    public void updatePosition(float x, float y) {
        texYPos = y;
    }

    //the y position of the texture should be the y position of background
    float texYPos = 0;

    private long initialTime = 0;

    private float scaleCount = 1.0f;
}