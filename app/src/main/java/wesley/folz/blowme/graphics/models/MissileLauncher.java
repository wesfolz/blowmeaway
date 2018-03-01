package wesley.folz.blowme.graphics.models;

import wesley.folz.blowme.graphics.effects.Explosion;
import wesley.folz.blowme.util.GraphicsUtilities;

/**
 * Created by Wesley on 10/14/2017.
 */

public class MissileLauncher extends Model {

    public MissileLauncher(float x, float y) {
        this(x, y, 2);
    }

    public MissileLauncher(float x, float y, float time) {
        super();
        x *= 0.465f;
        missile = new Missile(x, y);
        stand = new LauncherStand(x, y);
        tube = new LauncherTube(x, y);
        fuse = new Fuse(x, y, time);
        explosion = new Explosion();
    }

    @Override
    public void enableGraphics(GraphicsUtilities graphicsData) {
        missile.enableGraphics(graphicsData);
        stand.enableGraphics(graphicsData);
        tube.enableGraphics(graphicsData);
        fuse.enableGraphics(graphicsData);
        explosion.enableGraphics(graphicsData);
    }

    @Override
    public void initializeMatrices(float[] viewMatrix, float[] perspectiveMatrix,
            float[] orthographicMatrix, float[] lightPosInEyeSpace) {
        missile.initializeMatrices(viewMatrix, perspectiveMatrix, orthographicMatrix,
                lightPosInEyeSpace);
        stand.initializeMatrices(viewMatrix, perspectiveMatrix, orthographicMatrix,
                lightPosInEyeSpace);
        tube.initializeMatrices(viewMatrix, perspectiveMatrix, orthographicMatrix,
                lightPosInEyeSpace);
        fuse.initializeMatrices(viewMatrix, perspectiveMatrix, orthographicMatrix,
                lightPosInEyeSpace);
        explosion.initializeMatrices(viewMatrix, perspectiveMatrix, orthographicMatrix,
                lightPosInEyeSpace);
    }

    @Override
    public void draw() {
        missile.draw();
        if (!missile.isFlying() && !missile.isOffscreen()) {
            fuse.draw();
            tube.draw();
            stand.draw();
        }
        if (exploding) {
            explosion.draw();
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
        stand.updatePosition(x, y);
        tube.updatePosition(x, y);
        fuse.updatePosition(x, y);

        if (fuse.isBurnedOut() && !exploding) {
            missile.setFlying(true);
            explosion.reinitialize(stand.getxPos(), stand.getyPos());
            exploding = true;
        }

        if (missile.isOffscreen() && !exploding) {
            explosion.reinitialize(stand.getxPos(), stand.getyPos());
            exploding = true;
        }

        missile.updatePosition(x, y);
    }

    public Missile getMissile() {
        return missile;
    }

    private Missile missile;

    private LauncherStand stand;
    private LauncherTube tube;
    private Fuse fuse;

    private Explosion explosion;

    private boolean exploding = false;
}