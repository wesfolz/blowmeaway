package wesley.folz.blowme.graphics.models;

import wesley.folz.blowme.util.GraphicsUtilities;

/**
 * Created by Wesley on 2/6/2018.
 */

public class Spike extends DestructiveObstacle {
    public Spike(float x, float y) {
        super(x, y);
        scaleFactor = 0.025f;
    }

    @Override
    public void enableGraphics(GraphicsUtilities graphicsData) {
        //get dataVBO, orderVBO, program, texture handles
        dataVBO = graphicsData.modelVBOMap.get("spike");
        orderVBO = graphicsData.orderVBOMap.get("spike");
        numVertices = graphicsData.numVerticesMap.get("spike");
        programHandle = graphicsData.shaderProgramIdMap.get("texture");
        textureDataHandle = graphicsData.textureIdMap.get("spike_tex");
    }

    final static float WIDTH = 0.05f;
}