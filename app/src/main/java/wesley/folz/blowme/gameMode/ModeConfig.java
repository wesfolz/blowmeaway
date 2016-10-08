package wesley.folz.blowme.gamemode;

import android.opengl.Matrix;
import android.util.Log;

import java.util.ArrayList;

import wesley.folz.blowme.R;
import wesley.folz.blowme.graphics.Background;
import wesley.folz.blowme.graphics.effects.DustCloud;
import wesley.folz.blowme.graphics.effects.Explosion;
import wesley.folz.blowme.graphics.effects.ParticleSystem;
import wesley.folz.blowme.graphics.models.DestructiveObstacle;
import wesley.folz.blowme.graphics.models.Dispenser;
import wesley.folz.blowme.graphics.models.FallingObject;
import wesley.folz.blowme.graphics.models.Fan;
import wesley.folz.blowme.graphics.models.Model;
import wesley.folz.blowme.graphics.models.RicochetObstacle;
import wesley.folz.blowme.graphics.models.Vortex;
import wesley.folz.blowme.util.GraphicsUtilities;
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
        explosions = new ArrayList<>();
        vortexes = new ArrayList<>();
        hazards = new ArrayList<>();

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

        DestructiveObstacle destObj = new DestructiveObstacle();
        models.add(destObj);
        hazards.add(destObj);

        Explosion explosion = new Explosion();
        models.add(explosion);
        explosions.add(explosion);
    }

    public void enableModelGraphics()
    {
        graphicsData = new GraphicsUtilities();

        //initialize data
        graphicsData.storeModelData("fan", R.raw.fan);
        graphicsData.storeModelData("vortex", R.raw.vortex_open_top);
        graphicsData.storeModelData("cube", R.raw.cube);
        graphicsData.storeModelData("dispenser", R.raw.triangle_collector);

        graphicsData.storeTexture("wood", R.raw.wood_texture);
        graphicsData.storeTexture("yellow_circle", R.raw.yellow_circle);
        graphicsData.storeTexture("sky", R.raw.sky_texture);
        graphicsData.storeTexture("grey_circle", R.raw.grey_circle);

        String[] particleAttributes = new String[]{"position", "direction", "normalVector", "speed", "color"};
        String[] modelAttributes = new String[]{"position", "color", "normalVector"};

        graphicsData.storeShader("dust_cloud", R.raw.dust_cloud_vertex_shader, R.raw.dust_cloud_fragment_shader, particleAttributes);
        graphicsData.storeShader("explosion", R.raw.particle_vertex_shader, R.raw.particle_fragment_shader, particleAttributes);
        graphicsData.storeShader("texture", R.raw.texture_vertex_shader, R.raw.texture_fragment_shader, modelAttributes);
        graphicsData.storeShader("lighting", R.raw.lighting_vertex_shader, R.raw.lighting_fragment_shader, modelAttributes);

        for (Model m : models)
        {
            m.enableGraphics(graphicsData);
        }
        fan.getWind().enableGraphics(graphicsData);
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

        for (DestructiveObstacle h : hazards)
        {
            h.updatePosition(0, 0);
            h.draw();
        }

        boolean objectEffected;
        int explosionIndex = 0;
        for (FallingObject falObj : fallingObjects)
        {
            float xForce = 0;
            float yForce = 0;

            objectEffected = false;

            Explosion objectExplosion = explosions.get(explosionIndex);

            for(DestructiveObstacle h: hazards)
            {
                if (Physics.isCollision(h.getBounds(), falObj.getBounds()))
                {
                    objectExplosion.reinitialize(falObj.getxPos(), falObj.getyPos());
                    falObj.setOffscreen(true);
                    //rotate through all explosions
                    //this is done to avoid creating new explosion objects during gameplay
                    //since generating the particles is costly
                    if (explosionIndex < explosions.size() - 1)
                    {
                        explosionIndex++;
                    }
                    else
                    {
                        explosionIndex = 0;
                    }

                    break;
                }
            }

            if (falObj.isOffscreen())
            {
                models.remove(falObj);
                fallingObjects.remove(falObj);
                falObj = new FallingObject(dispenser.getxPos());
                falObj.enableGraphics(graphicsData);
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
            if (falObj.getyPos() > 0.95f && !objectEffected)
            {
                //falObj.updatePosition(100 * dispenser.getDeltaX(), 0);
                //xForce = 100 * dispenser.getDeltaX();
                //objectEffected = true;
                Log.e("mode", "dispense");
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

            if (objectExplosion.isExploding())
            {
                objectExplosion.updatePosition(0, 0);
                objectExplosion.draw();
            }

            falObj.draw();

        }

        particleSystem.updatePosition(0, 0);
        particleSystem.draw();
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
    private ArrayList<DestructiveObstacle> hazards;
    private ArrayList<FallingObject> fallingObjects;
    private ArrayList<Explosion> explosions;
    private ArrayList<Vortex> vortexes;

    private Fan fan;
    private Background background;
    private Dispenser dispenser;

    private float[] viewMatrix = new float[16];
    private float[] projectionMatrix = new float[16];
    private float[] lightPosInEyeSpace = new float[16];

    private ParticleSystem particleSystem;

    protected GraphicsUtilities graphicsData;
}
