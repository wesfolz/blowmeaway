package wesley.folz.blowme.gamemode;

import android.util.Log;

import java.util.ArrayList;

import wesley.folz.blowme.graphics.Background;
import wesley.folz.blowme.graphics.effects.Explosion;
import wesley.folz.blowme.graphics.models.Dispenser;
import wesley.folz.blowme.graphics.models.FallingObject;
import wesley.folz.blowme.graphics.models.Fan;
import wesley.folz.blowme.util.Physics;


/**
 * Created by Wesley on 11/20/2016.
 */

public class MenuModeConfig extends ModeConfig
{

    public MenuModeConfig()
    {
        super();
        positionsInitialized = true;
        initializeGameObjects();
    }

    @Override
    protected void initializeGameObjects() {
        int numRicochetObstacles = 2;
        int numDestructiveObstacles = 2;
        int numFallingObjects = 2;
        int numRings = 1;
        int numCubes = 1;
        int numVortexes = 2;
        int numRingVortexes = 1;
        int numCubeVortexes = 1;
        numCubesRemaining = 1;
        numRingsRemaining = 1;
        models = new ArrayList<>();
        obstacles = new ArrayList<>();
        fallingObjects = new ArrayList<>();
        explosions = new ArrayList<>();
        vortexes = new ArrayList<>();
        hazards = new ArrayList<>();

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

        background = new Background();
        models.add(background);

        positionsInitialized = false;

        //line =  new Line();
        //models.add(line);
        Log.e("pause", "constructor mode");
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
            if (Physics.isCollision(fan.getWind().getBounds(), falObj.getBounds())) {
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
        background.draw();
        fan.draw();

        dispenser.draw();

        for (FallingObject falObj : fallingObjects)
        {
            falObj.draw();
        }
        //line.draw();
    }
}
