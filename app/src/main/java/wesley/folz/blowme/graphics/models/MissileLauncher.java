package wesley.folz.blowme.graphics.models;

import wesley.folz.blowme.util.GraphicsUtilities;

/**
 * Created by Wesley on 10/14/2017.
 */

public class MissileLauncher extends Model {

    public MissileLauncher(float x, float y) {
        this(x, y, 5);
    }

    public MissileLauncher(float x, float y, float time) {
        super();
        x *= 0.465f;
        missile = new Missile(x, y);
        stand = new LauncherStand(x, y);
        tube = new LauncherTube(x, y);
        fuse = new Fuse(x, y);
        timeToFire = time;
    }

    @Override
    public void enableGraphics(GraphicsUtilities graphicsData) {
        missile.enableGraphics(graphicsData);
        stand.enableGraphics(graphicsData);
        tube.enableGraphics(graphicsData);
        fuse.enableGraphics(graphicsData);
    }

    @Override
    public void initializeMatrices(float[] viewMatrix, float[] projectionMatrix,
            float[] lightPosInEyeSpace) {
        missile.initializeMatrices(viewMatrix, projectionMatrix, lightPosInEyeSpace);
        stand.initializeMatrices(viewMatrix, projectionMatrix, lightPosInEyeSpace);
        tube.initializeMatrices(viewMatrix, projectionMatrix, lightPosInEyeSpace);
        fuse.initializeMatrices(viewMatrix, projectionMatrix, lightPosInEyeSpace);
    }

    @Override
    public void draw() {
        missile.draw();
        if (!missile.isFlying()) {
            stand.draw();
            tube.draw();
            fuse.draw();
        }
    }

    @Override
    public void pauseGame() {
        super.pauseGame();
        missile.pauseGame();
        stand.pauseGame();
        tube.pauseGame();
        fuse.pauseGame();
    }

    @Override
    public void resumeGame() {
        super.resumeGame();
        missile.resumeGame();
        stand.resumeGame();
        tube.resumeGame();
        fuse.resumeGame();
    }

    @Override
    public float[] createTransformationMatrix() {
        return new float[0];
    }

    @Override
    public void updatePosition(float x, float y) {
        if (getPrevUpdateTime() == 0) {
            setPrevUpdateTime(System.nanoTime());
        }
        float deltaTime = (System.nanoTime() - getPrevUpdateTime()) / 1000000000.0f;

        stand.updatePosition(x, y);
        tube.updatePosition(x, y);
        fuse.updatePosition(x, y);
        if (deltaTime >= timeToFire) missile.setFlying(true);
        missile.updatePosition(x, y);
    }

    public Missile getMissile() {
        return missile;
    }

    private Missile missile;

    private LauncherStand stand;
    private LauncherTube tube;
    private Fuse fuse;

    private float timeToFire;
}