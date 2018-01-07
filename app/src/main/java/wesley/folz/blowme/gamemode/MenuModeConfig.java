package wesley.folz.blowme.gamemode;

import android.util.Log;

import java.util.ArrayList;

import wesley.folz.blowme.graphics.Background;
import wesley.folz.blowme.graphics.effects.Explosion;
import wesley.folz.blowme.graphics.models.Dispenser;
import wesley.folz.blowme.graphics.models.FallingObject;
import wesley.folz.blowme.graphics.models.Fan;
import wesley.folz.blowme.ui.GamePlayActivity;
import wesley.folz.blowme.util.Physics;


/**
 * Created by Wesley on 11/20/2016.
 */

public class MenuModeConfig extends ModeConfig
{
    public MenuModeConfig()
    {
        super();
        initializeGameObjects();
        TARGET_Y_ANGLE = 0;
        TARGET_X = 0;
        TARGET_Y = GamePlayActivity.Y_EDGE_POSITION;
    }

    @Override
    protected void initializeGameObjects() {

        int numFallingObjects = 2;
        int numRings = 0;
        int numCubes = 1;

        numCubesRemaining = 1;
        numRingsRemaining = 1;
        models = new ArrayList<>();
        obstacles = new ArrayList<>();
        fallingObjects = new ArrayList<>();
        explosions = new ArrayList<>();
        vortexes = new ArrayList<>();
        hazards = new ArrayList<>();

        background = new Background();

        models.add(background);
        fan = new Fan();
        models.add(fan);

        for (int i = 0; i < numRings; i++) {
            FallingObject fo = new FallingObject("ring");
            models.add(fo);
            fallingObjects.add(fo);
        }

        for (int i = 0; i < numCubes; i++) {
            FallingObject fo = new FallingObject("cube");
            models.add(fo);
            fallingObjects.add(fo);
        }

        for (int i = 0; i < numFallingObjects; i++) {
            Explosion explosion = new Explosion();
            models.add(explosion);
            explosions.add(explosion);
        }

        dispenser = new Dispenser();
        models.add(dispenser);

        positionsInitialized = false;

        //line =  new Line();
        //models.add(line);
    }

    @Override
    protected void updateModelPositions()
    {
        background.updatePosition(0, 0);

        dispenser.updatePosition(0, 0);

        int modelCount = 0;
        for (FallingObject falObj : fallingObjects)
        {
            float xForce = 0;
            float yForce = 0;
            if (falObj.isOffscreen())
            {
                Log.e("falobj", "offscreen");
                try
                {
                    models.remove(falObj);
                    //falObj = falObj.getClass().getConstructor(float.class).newInstance(dispenser.getxPos());
                    String type = falObj.getType();
                    falObj = new FallingObject(type, dispenser.getxPos());
                    fallingObjects.set(modelCount, falObj);
                    falObj.enableGraphics(graphicsData);
                    falObj.initializeMatrices(viewMatrix, projectionMatrix, lightPosInEyeSpace);
                    models.add(falObj);
                }
                catch (Exception e)
                {
                    Log.e("error", e.getMessage());
                }
            }

            //calculate wind influence
            if (Physics.isCollision(falObj.getBounds(), fan.getWind().getBounds())) {
                Physics.calculateWindForce(fan.getWind(), falObj);
                //Log.e("wind", "xforce " + fan.getWind().getxForce() + " yforce " + fan.getWind
                // ().getyForce());
                //Log.e("mode", "wind collision");
                //falObj.updatePosition(fan.getWind().getxForce(), fan.getWind().getyForce());
                xForce = fan.getWind().getxForce();
                yForce = fan.getWind().getyForce();
            }

            falObj.updatePosition(xForce, yForce);
            modelCount++;
        }

        //line.updatePosition(0, 0);
        //line.draw();
    }

    @Override
    protected void drawModels()
    {
        fan.draw();
        background.draw();
        dispenser.draw();

        for (FallingObject falObj : fallingObjects)
        {
            falObj.draw();
        }
        //line.draw();
    }

    public static final float FAN_TARGET_X = 0;
    public static final float FAN_TARGET_Y = GamePlayActivity.Y_EDGE_POSITION;
    public static final float FAN_TARGET_Y_ANGLE = 0;
    public static final float FAN_TARGET_Z_ANGLE = 0;
}
