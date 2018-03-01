package wesley.folz.blowme.graphics.models;

import android.opengl.Matrix;

import wesley.folz.blowme.util.GraphicsUtilities;

/**
 * Created by Wesley on 10/15/2017.
 */

public class LauncherStand extends RicochetObstacle {
    public LauncherStand(float x, float y) {
        super(x, y);
        scaleFactor = 0.0225f;
    }

    @Override
    public void enableGraphics(GraphicsUtilities graphicsData) {
        //get dataVBO, orderVBO, program, texture handles
        dataVBO = graphicsData.modelVBOMap.get("launcher_stand");
        orderVBO = graphicsData.orderVBOMap.get("launcher_stand");
        numVertices = graphicsData.numVerticesMap.get("launcher_stand");
        programHandle = graphicsData.shaderProgramIdMap.get("texture");
        textureDataHandle = graphicsData.textureIdMap.get("launcher_stand_tex");
    }

    @Override
    public float[] createTransformationMatrix() {
        float[] mvp = new float[16];
        Matrix.setIdentityM(mvp, 0);

        //translate model matrix to new position
        Matrix.translateM(modelMatrix, 0, deltaX, deltaY, deltaZ);

        //copy modelMatrix to separate matrix for return, (returning modelMatrix doesn't work)
        Matrix.multiplyMM(mvp, 0, modelMatrix, 0, mvp, 0);

        if (initialXPos > 0) {
            Matrix.rotateM(mvp, 0, 180, 0, 1, 0);
        }

        return mvp;
    }
}