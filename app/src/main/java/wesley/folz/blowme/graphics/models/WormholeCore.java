package wesley.folz.blowme.graphics.models;

import android.opengl.Matrix;

import wesley.folz.blowme.util.Bounds;
import wesley.folz.blowme.util.GraphicsUtilities;

/**
 * Created by Wesley on 1/8/2018.
 */

public class WormholeCore extends WormholeDistortion {

    public WormholeCore(float x, float y, float texX, float texY) {
        super();
        xPos = x;//+.01f;
        yPos = y;//GamePlayActivity.Y_EDGE_POSITION;

        this.texX = texX;
        this.texY = texY;

        deltaY = 0;

        setBounds(new Bounds());

        scaleFactor = 0.3f;

        this.orthographicProjection = true;

        initialXPos = xPos;
        initialYPos = yPos;
        initialZPos = -5f;
    }

    @Override
    public void enableGraphics(GraphicsUtilities graphicsData) {
        //get dataVBO, orderVBO, program, texture handles
        dataVBO = graphicsData.modelVBOMap.get("wormhole_core");
        orderVBO = graphicsData.orderVBOMap.get("wormhole_core");
        numVertices = graphicsData.numVerticesMap.get("wormhole_core");
        programHandle = graphicsData.shaderProgramIdMap.get("distortion");
        textureDataHandle = graphicsData.textureIdMap.get("planet");
    }

    @Override
    protected float[] calculateTextureMatrix() {
        float[] texMat = new float[16];
        Matrix.setIdentityM(texMat, 0);
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
        Matrix.translateM(texMat, 0, 0.5f * texX / scaleFactor + (1 / (2.0f * scaleFactor) - 0.5f),
                -0.5f * texY / scaleFactor + 0.5f * texYPos / scaleFactor - (
                        1 / (2.0f * scaleFactor) - 0.5f), 0);
        return texMat;
    }

    private float texX;
    private float texY;
}