package wesley.folz.blowme.gamemode;

import android.opengl.Matrix;
import android.util.Log;

import java.util.ArrayList;

import wesley.folz.blowme.graphics.Background;
import wesley.folz.blowme.graphics.effects.DustCloud;
import wesley.folz.blowme.graphics.effects.ParticleSystem;
import wesley.folz.blowme.graphics.models.Dispenser;
import wesley.folz.blowme.graphics.models.FallingObject;
import wesley.folz.blowme.graphics.models.Fan;
import wesley.folz.blowme.graphics.models.Model;
import wesley.folz.blowme.graphics.models.RicochetObstacle;
import wesley.folz.blowme.graphics.models.Vortex;
import wesley.folz.blowme.util.Physics;

/**
 * Created by Wesley on 9/24/2016.
 */

public abstract class ModeConfig
{
    public ModeConfig()
    {
        //read config file to determine game play parameters

        models = new ArrayList<>();
        obstacles = new ArrayList<>();
        fallingObjects = new ArrayList<>();
        vortexes = new ArrayList<>();

        fan = new Fan();
        models.add(fan);

        FallingObject fo = new FallingObject();
        models.add(fo);
        fallingObjects.add(fo);
        RicochetObstacle ro = new RicochetObstacle();
        models.add(ro);
        obstacles.add(ro);

        Vortex v = new Vortex();
        models.add(v);
        vortexes.add(v);

        dispenser = new Dispenser();
        models.add(dispenser);

        background = new Background();
        models.add(background);

        //TODO: Determine best method for creating, storing and displaying Effects
        particleSystem = new DustCloud(0, 1f);
        models.add(particleSystem);
    }

    public void enableModelGraphics()
    {
        for (Model m : models)
        {
            m.enableGraphics();
        }
        fan.getWind().enableGraphics();
    }

    public void surfaceGraphicsChanged(int width, int height)
    {
        float ratio = (float) width / height;

        // Set the camera position (View matrix)
        Matrix.setLookAtM(viewMatrix, 0, 0, 0, 5f, 0, 0, 0.0f, 0, 1.0f, 0);

        // this projection matrix is applied to object coordinates
        // in the onDrawFrame() method
        //Matrix.frustumM( projectionMatrix, 0, - ratio, ratio, - 1, 1, 1, 10 );
        Matrix.orthoM(projectionMatrix, 0, -ratio, ratio, -1, 1, 1, 10);

        float[] mLightPosInWorldSpace = new float[4];
        float[] mLightPosInModelSpace = new float[]{0.0f, 0.0f, 0.0f, 1.0f};
        float[] mLightModelMatrix = new float[16];

        // Calculate position of the light. Push into the distance.
        Matrix.setIdentityM(mLightModelMatrix, 0);
        Matrix.translateM(mLightModelMatrix, 0, 0.0f, 0.0f, 3.0f);

        Matrix.multiplyMV(mLightPosInWorldSpace, 0, mLightModelMatrix, 0, mLightPosInModelSpace, 0);
        Matrix.multiplyMV(lightPosInEyeSpace, 0, viewMatrix, 0, mLightPosInWorldSpace, 0);
        //Matrix.multiplyMV(lightPosInEyeSpace, 0, projectionMatrix, 0, lightPosInEyeSpace, 0);

        for (Model m : models)
        {
            m.initializeMatrices(viewMatrix, projectionMatrix, lightPosInEyeSpace);
        }
        fan.getWind().initializeMatrices(viewMatrix, projectionMatrix, lightPosInEyeSpace);
    }


    public void updatePositionsAndDrawModels()
    {
        background.draw();

        fan.draw();


        dispenser.updatePosition(0, 0);
        dispenser.draw();

        for (Vortex v : vortexes)
        {
            v.updatePosition(0, 0);
            v.draw();
        }

        for (RicochetObstacle o : obstacles)
        {
            o.updatePosition(0, 0);
            o.draw();
        }

        boolean objectEffected;
        for (FallingObject falObj : fallingObjects)
        {
            float xForce = 0;
            float yForce = 0;

            objectEffected = false;
            if (falObj.isOffscreen())
            {
                models.remove(falObj);
                fallingObjects.remove(falObj);
                falObj = new FallingObject(dispenser.getxPos());
                falObj.enableGraphics();
                falObj.initializeMatrices(viewMatrix, projectionMatrix, lightPosInEyeSpace);
                models.add(falObj);
                fallingObjects.add(falObj);
                //Log.e("mode", "offscreen");
            }

            for (Vortex vortex : vortexes)
            {
                //vortex position - falling object position
                if (Physics.isCollision(vortex.getBounds(), falObj.getBounds()) || falObj.isCollected())
                {
                    //falObj.travelOnVector(vortex.getxPos() - falObj.getxPos(), vortex.getyPos() - falObj.getyPos());
                    falObj.spiralIntoVortex(vortex.getxPos());
                    objectEffected = true;
                    Log.e("mode", "Collection");
                }
            }
            //falling object is being dispensed
            if (falObj.getyPos() < -0.95f && !objectEffected)
            {
                //falObj.updatePosition(100 * dispenser.getDeltaX(), 0);
                xForce = 100 * dispenser.getDeltaX();
                objectEffected = true;
                //Log.e("mode", "dispense");
            }

            //determine forces due to collisions with obstacles
            falObj.calculateRicochetCollisions(obstacles);

            //calculate wind influence
            if (Physics.isCollision(fan.getWind().getBounds(), falObj.getBounds()) && !objectEffected)
            {
                Physics.calculateWindForce(fan.getWind(), falObj);
                //Log.e("wind", "xforce " + fan.getWind().getxForce() + " yforce " + fan.getWind().getyForce());
                //Log.e("mode", "wind collision");
                //falObj.updatePosition(fan.getWind().getxForce(), fan.getWind().getyForce());
                xForce = fan.getWind().getxForce();
                yForce = fan.getWind().getyForce();
                objectEffected = true;
            }

            if (!falObj.isCollected())
            {
                falObj.updatePosition(xForce, yForce);
            }

            falObj.draw();

            particleSystem.updatePosition(0, 0);
            particleSystem.draw();
        }
    }

    public void handleFanMovementDown(float x, float y)
    {
        fan.setInitialX(x);
        fan.setInitialY(y);
    }

    public void handleFanMovementMove(float x, float y)
    {
        fan.updatePosition(x, y);

        // obstacles.get(0).updatePosition(x, y);
        // fallingObjects.get(0).updatePosition(x, y);
    }


    public void pauseGame()
    {
        for (Model m : models)
        {
            m.pauseGame();
        }
    }

    public void resumeGame()
    {
        for (Model m : models)
        {
            m.resumeGame();
        }
    }

    private ArrayList<Model> models;
    private ArrayList<RicochetObstacle> obstacles;
    private ArrayList<FallingObject> fallingObjects;
    private ArrayList<Vortex> vortexes;

    private Fan fan;
    private Background background;
    private Dispenser dispenser;

    private float[] viewMatrix = new float[16];
    private float[] projectionMatrix = new float[16];
    private float[] lightPosInEyeSpace = new float[16];

    private ParticleSystem particleSystem;
}
