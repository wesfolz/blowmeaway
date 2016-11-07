package wesley.folz.blowme.graphics.models;

import android.opengl.Matrix;
import android.os.SystemClock;
import android.util.Log;

import java.util.ArrayList;

import wesley.folz.blowme.graphics.Border;
import wesley.folz.blowme.util.Bounds;
import wesley.folz.blowme.util.GraphicsUtilities;
import wesley.folz.blowme.util.Physics;

/**
 * Created by wesley on 7/3/2015.
 */
public class FallingObject extends Model
{
    public FallingObject(String modelType)
    {
        this(modelType, 0);
    }

    public FallingObject(String modelType, float dispenserX)
    {
        super();

        this.type = modelType;
        setBounds(new Bounds());

        xPos = (float) (Math.random() - 0.5);

        if (xPos > 0.35)
        {
            xPos = 0.35f;
        }
        if (xPos <= -0.35)
        {
            xPos = -0.35f;
        }

        xPos = dispenserX;

        yPos = 1.0f;

        xVelocity = 0;

        yVelocity = -1.0f;//-0.3f;//0;//0.1f;

        previousTime = System.nanoTime();

        scaleFactor = 0.03f;

        initialXPos = xPos;
        initialYPos = yPos;
        collected = false;
    }

    @Override
    public void enableGraphics(GraphicsUtilities graphicsData)
    {
        //get dataVBO, orderVBO, program, texture handles
        dataVBO = graphicsData.modelVBOMap.get(type);
        orderVBO = graphicsData.orderVBOMap.get(type);
        numVertices = graphicsData.numVerticesMap.get(type);
        programHandle = graphicsData.shaderProgramIdMap.get("texture");
        textureDataHandle = graphicsData.textureIdMap.get("wood");
    }

    @Override
    public float[] createTransformationMatrix()
    {
        float[] transformation = new float[16];

        //float[] result = new float[16];

        long time = SystemClock.uptimeMillis();// % 4000L;
        float angle = 0.1f * ((int) time);
        long deltaT = time - prevRenderTime;

        //updatePosition( 0, 0 );

        //Matrix.setIdentityM( transformation, 0 );

        //Matrix.translateM( transformation, 0, deltaX, deltaY, 0 );

        //Matrix.multiplyMM( result, 0, mvpMatrix, 0, transformation, 0 );
        if (deltaT > 50 && collected)
        {
            Log.e("vortex", String.valueOf(stretchX) + " " + String.valueOf(stretchY));
            //Matrix.translateM(modelMatrix, 0, deltaX, 0, deltaZ);
            Matrix.scaleM(modelMatrix, 0, stretchX, stretchY, 1);
            prevRenderTime = time;
        }
        Matrix.translateM(modelMatrix, 0, deltaX, deltaY, deltaZ);
        Matrix.setRotateM(transformation, 0, angle, 1, 1, 1);
        //Matrix.translateM(transformation, 0, xPos, yPos, zPos);
        Matrix.multiplyMM(transformation, 0, modelMatrix, 0, transformation, 0);

        return transformation;
    }

    public float getDeltaX()
    {
        return deltaX;
    }

    public float getDeltaY()
    {
        return deltaY;
    }

    public boolean isOffscreen()
    {
//        return false;
        if (collectedCount >= 10)
        {
            return true;
        }
        return this.getBounds().getyTop() < Border.YBOTTOM || offscreen;
    }

    @Override
    public void resumeGame()
    {
        super.resumeGame();
        previousTime = System.nanoTime();
        spiralTime = System.currentTimeMillis();
    }

    public void spiralOutOfVortex(Vortex vortex)
    {
        float vortexX = vortex.getxPos();
        if (!spiraling)
        {
            spiralY = yPos;
            spiralTime = System.currentTimeMillis();
            initialRadius = vortex.getSize()[0] / 2.0f;//Math.abs(xPos - vortexX);
            if (xPos - vortexX > 0)
            {
                initialAngle = 0;
            }
            else
            {
                initialAngle = (float) Math.PI;
            }
        }

        long time = System.currentTimeMillis();
        float arcLength = 2.0f * (float) (Math.PI / 180.0f) * (float) (time - spiralTime); //(float) Math.PI / 5.0f;//Math.abs(deltaY) + Math.abs(deltaX);
        spiralTime = time;

        float spiralIncrement = arcLength / 50.0f;//0.01f;

        float radius = initialRadius - spiralFactor;
        float newX;
        float newZ;

        newX = radius * (float) Math.cos(parametricAngle + initialAngle) + vortexX;
        newZ = radius * (float) Math.sin(parametricAngle + initialAngle);

        //counter clockwise rotation
        parametricAngle += arcLength;// / slowdown;

        deltaX = newX - xPos;
        deltaY = arcLength / 50.0f;
        deltaZ = newZ - zPos;
        xPos = newX;//xPos + deltaX;
        //yPos += deltaY;
        spiralY += deltaY;
        zPos = newZ;//zPos + deltaZ;

        spiraling = true;

        //move inwards
        spiralCount++;
        if (spiralFactor < initialRadius && spiralCount % 10 == 0)
        {
            spiralFactor += spiralIncrement;
            if (initialAngle == 0)
            {
                deltaX -= spiralIncrement;
                xPos -= spiralIncrement;
            }
            else
            {
                deltaX += spiralIncrement;
                xPos += spiralIncrement;
            }
        }
        else
        {
            if (spiralFactor >= initialRadius)
            {
                spiralFactor = initialRadius;
                deltaX += vortexX - xPos;
                xPos = vortexX;
                //deltaZ = -zPos;
                deltaZ = 0;
                zPos = 0;
                deltaY = 0;
                yPos = spiralY;
                spiralY = 0;
                spiraling = false;
                vortex.setCollecting(false);
                spiralFactor = 0;
                parametricAngle = 0;
                yVelocity = 0;
                previousTime = System.nanoTime();
                //travelOnVector(0, 0.001f);
            }
        }
        //updatePosition(deltaX, deltaY);
        getBounds().setBounds(xPos - scaleFactor, yPos - scaleFactor, xPos + scaleFactor, yPos + scaleFactor);
    }


    public void spiralIntoVortex(float vortexX)
    {
        if (!spiraling)
        {
            spiralTime = System.currentTimeMillis();
            initialRadius = Math.abs(xPos - vortexX);
            if (xPos - vortexX > 0)
            {
                initialAngle = 0;
            }
            else
            {
                initialAngle = (float) Math.PI;
            }
        }

        long time = System.currentTimeMillis();
        float arcLength = 2.0f * (float) (Math.PI / 180.0f) * (float) (time - spiralTime); //(float) Math.PI / 5.0f;//Math.abs(deltaY) + Math.abs(deltaX);
        spiralTime = time;

        float spiralIncrement = arcLength / 50.0f;//0.01f;

        float radius = initialRadius - spiralFactor;
        float newX;
        float newZ;

        newX = radius * (float) Math.cos(parametricAngle + initialAngle) + vortexX;
        newZ = radius * (float) Math.sin(parametricAngle + initialAngle);

        //counter clockwise rotation
        parametricAngle += arcLength;// / slowdown;

        deltaY = 0;
        deltaX = newX - xPos;
        deltaZ = newZ - zPos;
        xPos = newX;//xPos + deltaX;
        //yPos = yPos + deltaY;
        zPos = newZ;//zPos + deltaZ;

        //move inwards
        spiralCount++;
        if (spiralFactor < initialRadius && spiralCount % 10 == 0)
        {
            spiralFactor += spiralIncrement;
            if (initialAngle == 0)
            {
                deltaX -= spiralIncrement;
                xPos -= spiralIncrement;
            }
            else
            {
                deltaX += spiralIncrement;
                xPos += spiralIncrement;
            }
        }
        else
        {
            if (spiralFactor >= initialRadius)
            {
                spiralFactor = initialRadius;
                deltaX += vortexX - xPos;
                xPos = vortexX;
                deltaZ = -zPos;
                zPos = 0;
                previousTime = System.nanoTime();
                travelOnVector(0, 0.001f);
            }
        }
        spiraling = true;

        getBounds().setBounds(xPos - scaleFactor, yPos - scaleFactor, xPos + scaleFactor, yPos + scaleFactor);
    }


    public void travelOnVector(float xComponent, float yComponent)
    {
        float time = (System.nanoTime() - previousTime) / 1000000000.0f;
        previousTime = System.nanoTime();
        float fallingTime = MASS * time;
        deltaX = 5 * fallingTime * xComponent;
        //deltaY = 5 * fallingTime * yComponent;
        deltaY = -0.001f;//fallingTime * yComponent;

        xPos += deltaX;
        yPos += deltaY;

        float normalizedX = Math.abs(xComponent / (Math.max(Math.abs(xComponent), Math.abs(yComponent))));

        float normalizedY = Math.abs(yComponent / (Math.max(Math.abs(xComponent), Math.abs(yComponent))));

        float stretchFactor = 0.5f * (normalizedY - normalizedX);

        stretchX = 1.0f - stretchFactor;
        stretchY = 1.0f + stretchFactor;
        if (collectedCount == 0)
        {
            collected = true;
        }
        collectedCount++;
    }

    public void calculateRicochetCollisions(ArrayList<RicochetObstacle> obstacles)
    {
        for (RicochetObstacle o : obstacles)
        {
            //Log.e("obstacle", "ybottom " + o.getBounds().getyBottom() + " ytop " + o.getBounds().getyTop());
            collision = Physics.calculateCollision(o.getBounds(), getBounds());
            if (collision != Physics.COLLISION.NONE)
            {
                Log.e("titans", "Collision: " + collision);
                break;
            }
        }
    }

    /**
     * @param x - x component of wind force
     * @param y - y component of wind force
     */
    @Override
    public void updatePosition(float x, float y)
    {
        long time = System.nanoTime();
        float deltaTime = (time - previousTime) / 1000000000.0f;
        previousTime = time;//System.nanoTime();
        float fallingTime = MASS * deltaTime;

        float[] force = Physics.sumOfForces(x, y);
        float elasticityCoefficient = 0.6f;

        xVelocity += force[0] * fallingTime;
        yVelocity += force[1] * fallingTime;

        //reflection of falling object off of side border
        //if object collides of off right border, xVelocity must be negative
        if (this.getBounds().getxRight() >= Border.XRIGHT || collision == Physics.COLLISION.RIGHT_LEFT)
        {
            xVelocity = -elasticityCoefficient * Math.abs(xVelocity);
        }
        //if object collides of off left border, xVelocity must be positive
        else
        {
            if (this.getBounds().getxLeft() <= Border.XLEFT || collision == Physics.COLLISION.LEFT_RIGHT)
            {
                xVelocity = elasticityCoefficient * Math.abs(xVelocity);
            }
            //reflection of falling object off of top border, yVelocity must be negative
            else
            {
                if (Physics.isTopBorderCollision(this.getBounds()) || collision == Physics.COLLISION.TOP_BOTTOM)
                {
                    yVelocity = -elasticityCoefficient * Math.abs(yVelocity);
                }
                else
                {
                    if (collision == Physics.COLLISION.BOTTOM_TOP)
                    {
                        yVelocity = elasticityCoefficient * Math.abs(yVelocity);
                    }
                }
            }
        }

        Log.e("falobj", "force y " + force[1] + " collision " + collision + " delta time "
                + deltaTime + " yvelocity " + yVelocity + " y " + y);


        deltaX = fallingTime * xVelocity;

        deltaY = fallingTime * yVelocity;

        xPos += deltaX;
        yPos += deltaY;

        getBounds().setBounds(xPos - scaleFactor, yPos - scaleFactor, xPos + scaleFactor, yPos + scaleFactor);
//        getBounds().setBounds(xPos - scaleFactor, yPos + scaleFactor, xPos + scaleFactor, yPos - scaleFactor );

        //Log.e("blowme", "xpos " + xPos + " ypos " + yPos);
        //Log.e("blowme", "ycorners0 " + getBounds().getYCorners()[0] + " ycorners1 " + getBounds().getYCorners()[1]);
    }

    public String getType()
    {
        return type;
    }

    public int getCollectingVortexIndex()
    {
        return collectingVortexIndex;
    }

    public void setCollectingVortexIndex(int collectingVortexIndex)
    {
        this.collectingVortexIndex = collectingVortexIndex;
    }

    public void setOffscreen(boolean offscreen)
    {
        this.offscreen = offscreen;
    }

    public boolean isCollected()
    {
        return collected;
    }

    public void setCollected(boolean collected)
    {
        this.collected = collected;
    }
    public boolean isSpiraling()
    {
        return spiraling;
    }

    private int collectedCount = 0;

    private long previousTime;

    private long prevRenderTime;

    private float deltaX;

    private float deltaY;

    private float deltaZ;

    private float zPos;

    private float xVelocity;

    private float yVelocity;

    private static final float MASS = 2.0f;

    private float stretchX = 1.0f;

    private float stretchY = 1.0f;

    private Physics.COLLISION collision;

    public void setInitialY(float initialY)
    {
        this.initialY = initialY;
    }

    public void setInitialX(float initialX)
    {
        this.initialX = initialX;
    }

    private float initialX;
    private float initialY;

    private float parametricAngle = 0;
    private float spiralFactor = 0;

    private long spiralTime = 0;

    private boolean collected = false;

    private boolean spiraling = false;

    private float initialRadius;
    private float initialAngle;

    private int spiralCount = 0;

    private int collectingVortexIndex = -1;

    private boolean offscreen = false;

    private String type;

    private float spiralY = 0;
}
