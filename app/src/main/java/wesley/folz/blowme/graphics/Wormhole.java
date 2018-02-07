package wesley.folz.blowme.graphics;

import wesley.folz.blowme.graphics.models.Model;
import wesley.folz.blowme.graphics.models.WormholeCore;
import wesley.folz.blowme.graphics.models.WormholeDistortion;
import wesley.folz.blowme.util.GraphicsUtilities;

/**
 * Created by Wesley on 11/20/2017.
 */

public class Wormhole extends Model {

    public Wormhole(float x1, float y1, float x2, float y2) {
        distortion1 = new WormholeDistortion(x1, y1);
        distortion2 = new WormholeDistortion(x2, y2);
        core1 = new WormholeCore(x1, y1, x2, y2);
        core2 = new WormholeCore(x2, y2, x1, y1);
    }

    @Override
    public void draw() {
        distortion1.draw();
        distortion2.draw();
        core1.draw();
        core2.draw();
    }

    @Override
    public float[] createTransformationMatrix() {
        return new float[0];
    }

    @Override
    public void enableGraphics(GraphicsUtilities graphicsData) {
        distortion1.enableGraphics(graphicsData);
        distortion2.enableGraphics(graphicsData);
        core1.enableGraphics(graphicsData);
        core2.enableGraphics(graphicsData);
    }

    @Override
    public void initializeMatrices(float[] viewMatrix, float[] projectionMatrix,
            float[] lightPositionInEyeSpace) {
        distortion1.initializeMatrices(viewMatrix, projectionMatrix, lightPositionInEyeSpace);
        distortion2.initializeMatrices(viewMatrix, projectionMatrix, lightPositionInEyeSpace);
        core1.initializeMatrices(viewMatrix, projectionMatrix, lightPositionInEyeSpace);
        core2.initializeMatrices(viewMatrix, projectionMatrix, lightPositionInEyeSpace);
    }

    @Override
    public boolean initializationRoutine() {
        remainingTime = timeout;
        distortion1.updatePosition(0, background.getyPos());
        distortion2.updatePosition(0, background.getyPos());
        return distortion1.initializationRoutine() & distortion2.initializationRoutine()
                & core1.initializationRoutine() & core2.initializationRoutine();
    }

    @Override
    public boolean removalRoutine() {
        distortion1.updatePosition(0, background.getyPos());
        distortion2.updatePosition(0, background.getyPos());
        core1.updatePosition(0, background.getyPos());
        core2.updatePosition(0, background.getyPos());
        boolean removed =
                distortion1.removalRoutine() & distortion2.removalRoutine() & core1.removalRoutine()
                        & core2.removalRoutine();
        if (removed) {
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
        if (remove) {
            //removalRoutine();
            distortion1.updatePosition(0, background.getyPos());
            distortion2.updatePosition(0, background.getyPos());
            core1.updatePosition(0, background.getyPos());
            core2.updatePosition(0, background.getyPos());
        } else {
            distortion1.updatePosition(0, background.getyPos());
            distortion2.updatePosition(0, background.getyPos());
            core1.updatePosition(0, background.getyPos());
            core2.updatePosition(0, background.getyPos());
            long time = System.nanoTime();
            float deltaTime = (time - prevUpdateTime) / 1000000000.0f;
            prevUpdateTime = time;
            remainingTime -= deltaTime;
        }
    }

    public boolean isRemove() {
        return remove;
    }

    public void setRemove(boolean remove) {
        this.remove = remove;
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

    private WormholeCore core1;

    private WormholeCore core2;

    private Background background;

    private float timeout = -1;//15.0f;

    private float remainingTime = 0;

    private boolean remove = false;
}