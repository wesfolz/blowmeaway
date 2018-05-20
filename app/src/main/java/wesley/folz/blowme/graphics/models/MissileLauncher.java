package wesley.folz.blowme.graphics.models;

import wesley.folz.blowme.ui.GamePlayActivity;
import wesley.folz.blowme.util.GraphicsUtilities;
import wesley.folz.blowme.util.Physics;

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
        xPos = x;
        yPos = y;
        initialXPos = xPos + 0.2f * xPos / Math.abs(xPos);
        initialYPos = yPos;
        xDirection = x / Math.abs(x);
        missile = new Missile(initialXPos, y);
        stand = new LauncherStand(initialXPos, y);
        tube = new LauncherTube(initialXPos, y);
        fuse = new Fuse(initialXPos, y, time);
    }

    @Override
    public void enableGraphics(GraphicsUtilities graphicsData) {
        missile.enableGraphics(graphicsData);
        stand.enableGraphics(graphicsData);
        tube.enableGraphics(graphicsData);
        fuse.enableGraphics(graphicsData);
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
    }

    @Override
    public boolean initializationRoutine() {
        if (initialized) {
            return true;
        }
        if (initialTime == 0) {
            initRoutineDeltaX = xPos - initialXPos;
            initialTime = System.nanoTime();
            prevUpdateTime = initialTime;
        }
        long time = System.nanoTime();
        float deltaTime = (time - prevUpdateTime) / 1000000000.0f;
        prevUpdateTime = time;
        deltaX = initRoutineDeltaX * deltaTime / GamePlayActivity.INITIALIZATION_TIME;
        initialXPos += deltaX;
        if (((time - initialTime) / 1000000000.0f) >= GamePlayActivity.INITIALIZATION_TIME) {
            deltaX = initialXPos + deltaX - xPos;
            initialized = true;
            initialTime = 0;
            updatePosition(0,
                    0); //update to correct position, second update will set deltaX to 0 so that
            // launcher stops moving
            deltaX = 0;
        }

        updatePosition(0, 0);

        return initialized;
    }

    @Override
    public boolean removalRoutine() {
        if (initialTime == 0) {
            initialized = false;
            initialXPos = xPos - 0.2f * xPos / Math.abs(xPos);
        }
        return initializationRoutine();
    }

    @Override
    public void draw() {
        missile.draw();
        tube.draw();
        fuse.draw();
        stand.draw();
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

        if (move) {
            Physics.panUpDown(this, Physics.rise(this));
        }

        if (!missile.isFlying()) {
            x = deltaX;
            y = deltaY;
        }

        tube.setyDirection(getyDirection());
        tube.setyMotion(getyMotion());
        fuse.setyDirection(getyDirection());
        fuse.setyMotion(getyMotion());
        missile.setyDirection(getyDirection());
        missile.setyMotion(getyMotion());

        stand.updatePosition(deltaX, deltaY);
        tube.updatePosition(deltaX, deltaY);
        fuse.updatePosition(deltaX, deltaY);

        if (fuse.isBurnedOut()) {
            missile.setFlying(true);
        }

        missile.updatePosition(x, y);
    }

    public Missile getMissile() {
        return missile;
    }

    public Fuse getFuse() {
        return fuse;
    }

    public void setMove(boolean move) {
        this.move = move;
    }


    public void setInitialAngle(float initialAngle) {
        yDirection = (float) (xDirection / Math.tan(initialAngle));
    }

    private Missile missile;

    private LauncherStand stand;
    private LauncherTube tube;

    private Fuse fuse;

    private boolean move = true;

    private long initialTime = 0;

    private float initRoutineDeltaX = 0;
}