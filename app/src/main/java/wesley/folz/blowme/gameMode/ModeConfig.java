package wesley.folz.blowme.gamemode;

import android.opengl.Matrix;
import android.util.Log;

import java.util.ArrayList;
import java.util.Random;

import wesley.folz.blowme.R;
import wesley.folz.blowme.graphics.Background;
import wesley.folz.blowme.graphics.Border;
import wesley.folz.blowme.graphics.effects.DustCloud;
import wesley.folz.blowme.graphics.effects.Explosion;
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
        numRicochetObstacles = 2;
        numDestructiveObstacles = 2;
        numFallingObjects = 2;
        numVortexes = 2;

        Random rand = new Random();
        models = new ArrayList<>();
        obstacles = new ArrayList<>();
        fallingObjects = new ArrayList<>();
        explosions = new ArrayList<>();
        vortexes = new ArrayList<>();
        dustClouds = new ArrayList<>();
        hazards = new ArrayList<>();

        fan = new Fan();
        models.add(fan);

        for (int i = 0; i < numFallingObjects; i++)
        {
            FallingObject fo = new FallingObject();
            models.add(fo);
            fallingObjects.add(fo);
        }

        for (int i = 0; i < numFallingObjects; i++)
        {
            Explosion explosion = new Explosion();
            models.add(explosion);
            explosions.add(explosion);
        }

        for (int i = 0; i < numRicochetObstacles; i++)
        {
            float pos[] = generateRandomLocation();
            RicochetObstacle ro = new RicochetObstacle(pos[0], pos[1]);
            models.add(ro);
            obstacles.add(ro);
        }

        for (int i = 0; i < numDestructiveObstacles; i++)
        {
            float pos[] = generateRandomLocation();
            DestructiveObstacle destObj = new DestructiveObstacle(pos[0], pos[1]);
            models.add(destObj);
            hazards.add(destObj);
        }

        //numVortexes=2 -> x1=-0.5
        for (int i = 0; i < numVortexes; i++)
        {
            Vortex v = new Vortex((float) (i + 1) * (2.0f / (numVortexes + 1.0f)) - 1);
            models.add(v);
            vortexes.add(v);
        }

        for (int i = 0; i < numVortexes; i++)
        {
            DustCloud dustCloud = new DustCloud((float) (i + 1) * (2.0f / (numVortexes + 1.0f)) - 1, 1f);
            models.add(dustCloud);
            dustClouds.add(dustCloud);
        }

        dispenser = new Dispenser();
        models.add(dispenser);

        background = new Background();
        models.add(background);

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
        graphicsData.storeTexture("grid", R.raw.grid);

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
        background.updatePosition(0, 0);
        background.draw();

        fan.draw();

        dispenser.updatePosition(0, 0);
        dispenser.draw();

        for (Vortex v : vortexes)
        {
            v.updatePosition(0, 0);
            v.draw();
        }

        int modelCount = 0;
        for (RicochetObstacle o : obstacles)
        {
            if (o.isOffscreen())
            {
                models.remove(o);
                float pos[] = generateRandomLocation();
                o = new RicochetObstacle(pos[0], pos[1]);
                obstacles.set(modelCount, o);
                o.enableGraphics(graphicsData);
                o.initializeMatrices(viewMatrix, projectionMatrix, lightPosInEyeSpace);
                models.add(o);
            }
            else
            {
                o.updatePosition(0, 0);
                o.draw();
            }
            modelCount++;
        }

        modelCount = 0;
        for (DestructiveObstacle h : hazards)
        {
            if (h.isOffscreen())
            {
                models.remove(h);
                float pos[] = generateRandomLocation();
                h = new DestructiveObstacle(pos[0], pos[1]);
                hazards.set(modelCount, h);
                h.enableGraphics(graphicsData);
                h.initializeMatrices(viewMatrix, projectionMatrix, lightPosInEyeSpace);
                models.add(h);
            }
            else
            {
                h.updatePosition(0, 0);
                h.draw();
            }
            modelCount++;
        }

        boolean objectEffected;
        modelCount = 0;
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
                falObj = new FallingObject(dispenser.getxPos());
                fallingObjects.set(modelCount, falObj);
                falObj.enableGraphics(graphicsData);
                falObj.initializeMatrices(viewMatrix, projectionMatrix, lightPosInEyeSpace);
                models.add(falObj);
                //Log.e("mode", "offscreen");
            }

            int vortexCount = 0;
            for (Vortex vortex : vortexes)
            {
                //vortex position - falling object position
                if (Physics.isCollision(vortex.getBounds(), falObj.getBounds())
                        || (falObj.isCollected() && vortex.isCollecting() && vortexCount == falObj.getCollectingVortexIndex()))
                {
                    vortex.setCollecting(true);
                    //falObj.travelOnVector(vortex.getxPos() - falObj.getxPos(), vortex.getyPos() - falObj.getyPos());
                    falObj.setCollectingVortexIndex(vortexCount);
                    falObj.spiralIntoVortex(vortex.getxPos());
                    objectEffected = true;
                    Log.e("mode", "Collection " + vortexCount + " xpos " + vortex.getxPos());
                    break;
                }
                else
                {
                    vortex.setCollecting(false);
                }
                vortexCount++;
            }
            //falling object is being dispensed
            if (falObj.getyPos() > 0.95f && !objectEffected)
            {
                //falObj.updatePosition(100 * dispenser.getDeltaX(), 0);
                xForce = 100 * dispenser.getDeltaX();
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

            falObj.draw();

            modelCount++;
        }

        for (Explosion e : explosions)
        {
            if (e.isExploding())
            {
                e.updatePosition(0, 0);
                e.draw();
            }
        }

        for (DustCloud dc : dustClouds)
        {
            dc.updatePosition(0, 0);
            dc.draw();
        }
    }

    private float[] generateRandomLocation()
    {
        Random rand = new Random();
        //screen dimensions: -0.5 <= x <= 0.5, -1 <= y <= 1
        //five x locations
        int numXLocations = 5;
        float cellWidth = (Border.XRIGHT - Border.XLEFT) / (float) numXLocations;

        int numYLocations = 8;
        //eight y locations
        float cellHeight = (Border.YTOP - Border.YBOTTOM) / (float) numYLocations;

        if (xLocations.isEmpty())
        {
            for (int i = 0; i < numXLocations; i++)
            {
                xLocations.add(i);
            }
        }

        if (yLocations.isEmpty())
        {
            for (int i = 0; i < numYLocations; i++)
            {
                yLocations.add(i);
            }
        }

        int xIndex = rand.nextInt(xLocations.size());
        int yIndex = rand.nextInt(yLocations.size());

        int xCell = xLocations.get(xIndex);
        int yCell = yLocations.get(yIndex);

        float locations[] = new float[2];
        //Border.XLEFT + cellWidth/2 <= locations[0] <= Border.XRIGHT - cellWidth/2
        locations[0] = (float) xCell * cellWidth + cellWidth / 2 + Border.XLEFT;
        //3*YTOP -> want objects not visible initially
        // [-1, -3]
        locations[1] = (float) yCell * cellHeight + cellHeight / 2 + 3 * Border.YBOTTOM;

        xLocations.remove(xIndex);
        yLocations.remove(yIndex);

        Log.e("locations", "locations " + locations[1] + " yCell " + yCell);

        return locations;
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
    private ArrayList<DustCloud> dustClouds;

    private Fan fan;
    private Background background;
    private Dispenser dispenser;

    private float[] viewMatrix = new float[16];
    private float[] projectionMatrix = new float[16];
    private float[] lightPosInEyeSpace = new float[16];

    private boolean draw;

    protected GraphicsUtilities graphicsData;

    private int explosionIndex = 0;

    private ArrayList<Integer> xLocations = new ArrayList<>();
    private ArrayList<Integer> yLocations = new ArrayList<>();

    //game parameters:
    private int numRicochetObstacles;
    private int numDestructiveObstacles;
    private int numFallingObjects;
    private int numVortexes;

}
