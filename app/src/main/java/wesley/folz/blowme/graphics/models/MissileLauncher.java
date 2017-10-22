package wesley.folz.blowme.graphics.models;

import wesley.folz.blowme.util.GraphicsUtilities;

/**
 * Created by Wesley on 10/14/2017.
 */

public class MissileLauncher extends Model {

    public MissileLauncher(float x, float y) {
        super();
        missile = new Missile(x, y);
        stand = new LauncherStand(x, y);
        tube = new LauncherTube(x, y);
    }

    @Override
    public void enableGraphics(GraphicsUtilities graphicsData) {
        missile.enableGraphics(graphicsData);
        stand.enableGraphics(graphicsData);
        tube.enableGraphics(graphicsData);
    }

    @Override
    public void initializeMatrices(float[] viewMatrix, float[] projectionMatrix,
            float[] lightPosInEyeSpace) {
        missile.initializeMatrices(viewMatrix, projectionMatrix, lightPosInEyeSpace);
        stand.initializeMatrices(viewMatrix, projectionMatrix, lightPosInEyeSpace);
        tube.initializeMatrices(viewMatrix, projectionMatrix, lightPosInEyeSpace);
    }

    @Override
    public void draw() {
        missile.draw();
        stand.draw();
        tube.draw();
    }

    @Override
    public void pauseGame() {
        missile.pauseGame();
        stand.pauseGame();
        tube.pauseGame();
    }

    @Override
    public void resumeGame() {
        missile.resumeGame();
        stand.resumeGame();
        tube.resumeGame();
    }

    @Override
    public float[] createTransformationMatrix() {
        return new float[0];
    }

    @Override
    public void updatePosition(float x, float y) {
        stand.updatePosition(x, y);
        tube.updatePosition(x, y);
        fireTimer++;
        if (fireTimer >= 150) missile.setFlying(true);
        missile.updatePosition(x, y);
    }

    public Missile getMissile() {
        return missile;
    }

    private Missile missile;

    private LauncherStand stand;
    private LauncherTube tube;

    private int fireTimer = 0;
}