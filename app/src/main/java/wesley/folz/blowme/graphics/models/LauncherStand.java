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
    public void initializeMatrices(float[] viewMatrix, float[] projectionMatrix,
            float[] lightPositionInEyeSpace) {
        super.initializeMatrices(viewMatrix, projectionMatrix, lightPositionInEyeSpace);
        if (!this.resuming) {
            //rotate 130 degrees about x-axis
            Matrix.rotateM(modelMatrix, 0, 90, 0, 1, 0);
            Matrix.rotateM(modelMatrix, 0, 10, 0, 0, 1);
            //Matrix.scaleM(modelMatrix, 0, 1.0f, 0.01f, 1.0f);
        }
    }
}