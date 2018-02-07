package wesley.folz.blowme.graphics.models;

import wesley.folz.blowme.util.GraphicsUtilities;

/**
 * Created by Wesley on 2/6/2018.
 */

public class SpikeStrip extends DestructiveObstacle {

    public SpikeStrip(float x, float y) {
        this(x, y, 3, true);
    }

    public SpikeStrip(float x, float y, int numSpikes, boolean vertical) {
        super(x, y);
        spikes = new Spike[numSpikes];
        for (int i = 0; i < numSpikes; i++) {
            //0, width, -width, 2*width, -2*width, 3*width, -3*width, ...
            float position = (float) Math.ceil((double) i / 2.0) * ((i % 2 == 0) ? -1 : 1)
                    * Spike.WIDTH;
            if (vertical) {
                spikes[i] = new Spike(x, y + position);
            } else {
                spikes[i] = new Spike(x + position, y);
            }
        }

        xRadius = 0.06f;
        getBounds().setBounds(xPos - xRadius,
                yPos - Spike.WIDTH * numSpikes / 2.0f - Spike.WIDTH / 2, xPos + xRadius,
                yPos + Spike.WIDTH * numSpikes / 2.0f - Spike.WIDTH / 2);
    }

    @Override
    public void enableGraphics(GraphicsUtilities graphicsData) {
        for (Spike s : spikes) {
            s.enableGraphics(graphicsData);
        }
    }

    @Override
    public void draw() {
        for (Spike s : spikes) {
            s.draw();
        }
    }

    @Override
    public void initializeMatrices(float[] viewMatrix, float[] projectionMatrix,
            float[] lightPosInEyeSpace) {
        for (Spike s : spikes) {
            s.initializeMatrices(viewMatrix, projectionMatrix, lightPosInEyeSpace);
        }
    }

    @Override
    public void updatePosition(float x, float y) {
        super.updatePosition(x, y);
        for (Spike s : spikes) {
            s.updatePosition(x, y);
        }
        getBounds().setBounds(xPos - xRadius,
                yPos - Spike.WIDTH * spikes.length / 2.0f - Spike.WIDTH / 2,
                xPos + xRadius, yPos + Spike.WIDTH * spikes.length / 2.0f - Spike.WIDTH / 2);
    }

    private Spike[] spikes;
}