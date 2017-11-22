package wesley.folz.blowme.graphics;

import wesley.folz.blowme.graphics.models.Model;
import wesley.folz.blowme.graphics.models.WormholeDistortion;
import wesley.folz.blowme.util.GraphicsUtilities;

/**
 * Created by Wesley on 11/20/2017.
 */

public class Wormhole extends Model {

    public Wormhole(float x1, float y1, float x2, float y2) {
        distortion1 = new WormholeDistortion(x1, y1);
        distortion2 = new WormholeDistortion(x2, y2);
    }

    @Override
    public void draw() {
        distortion1.draw();
        distortion2.draw();
    }

    @Override
    public float[] createTransformationMatrix() {
        return new float[0];
    }

    @Override
    public void enableGraphics(GraphicsUtilities graphicsData) {
        distortion1.enableGraphics(graphicsData);
        distortion2.enableGraphics(graphicsData);
    }

    @Override
    public void initializeMatrices(float[] viewMatrix, float[] projectionMatrix,
            float[] lightPositionInEyeSpace) {
        distortion1.initializeMatrices(viewMatrix, projectionMatrix, lightPositionInEyeSpace);
        distortion2.initializeMatrices(viewMatrix, projectionMatrix, lightPositionInEyeSpace);
    }

    @Override
    public boolean initializationRoutine() {
        remainingTime = timeout;
        distortion1.updatePosition(0, background.getyPos());
        distortion2.updatePosition(0, background.getyPos());
        return distortion1.initializationRoutine() & distortion2.initializationRoutine();
    }

    @Override
    public boolean removalRoutine() {
        distortion1.updatePosition(0, background.getyPos());
        distortion2.updatePosition(0, background.getyPos());
        if (distortion1.removalRoutine() || distortion2.removalRoutine()) {
            offscreen = true;
            return true;
        }

        return false;
    }

    @Override
    public void updatePosition(float x, float y) {
        if (prevUpdateTime == 0) {
            prevUpdateTime = System.nanoTime();
        }
        if (remainingTime <= 0 && timeout > 0) {
            removalRoutine();
        } else {
            distortion1.updatePosition(0, background.getyPos());
            distortion2.updatePosition(0, background.getyPos());
            long time = System.nanoTime();
            float deltaTime = (time - prevUpdateTime) / 1000000000.0f;
            prevUpdateTime = time;
            remainingTime -= deltaTime;
        }
    }

    public void setBackground(Background background) {
        this.background = background;
    }

    public void setTimeout(float timeout) {
        this.timeout = timeout;
    }

    public WormholeDistortion getDistortion1() {
        return distortion1;
    }

    public WormholeDistortion getDistortion2() {
        return distortion2;
    }

    private WormholeDistortion distortion1;

    private WormholeDistortion distortion2;

    private Background background;

    private float timeout = 15.0f;

    private float remainingTime = 0;
}