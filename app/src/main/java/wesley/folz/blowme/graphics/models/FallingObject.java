package wesley.folz.blowme.graphics.models;

import android.opengl.Matrix;
import android.os.SystemClock;
import android.util.Log;

import java.util.ArrayList;

import wesley.folz.blowme.R;
import wesley.folz.blowme.graphics.Border;
import wesley.folz.blowme.util.Bounds;
import wesley.folz.blowme.util.GraphicsReader;
import wesley.folz.blowme.util.Physics;

/**
 * Created by wesley on 7/3/2015.
 */
public class FallingObject extends Model
{
    public FallingObject()
    {
        this(0);
    }

    public FallingObject(float dispenserX)
    {
        super();

        setBounds(new Bounds());

        this.OBJ_FILE_RESOURCE = R.raw.cube;
        this.VERTEX_SHADER = R.raw.texture_vertex_shader;
        this.FRAGMENT_SHADER = R.raw.texture_fragment_shader;
        GraphicsReader.readShader(this);
        GraphicsReader.readOBJFile(this);

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

        yPos = -1.0f;

        xVelocity = 0;

        yVelocity = 0.5f;//0;//0.1f;

        previousTime = System.nanoTime();

        scaleFactor = 0.03f;

        initialXPos = xPos;
        initialYPos = -yPos;
        collected = false;
    }

    @Override
    public float[] createTransformationMatrix()
    {
        float[] transformation = new float[16];
        firstCall = false;

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
            Log.e("vortex", String.valueOf(vortexX) + " " + String.valueOf(vortexY));
            //Matrix.translateM(modelMatrix, 0, deltaX, 0, deltaZ);
            Matrix.scaleM(modelMatrix, 0, vortexX, vortexY, 1);
            prevRenderTime = time;
        }
        Matrix.translateM(modelMatrix, 0, deltaX, -deltaY, deltaZ);
        Matrix.setRotateM(transformation, 0, angle, 1, 1, 1);
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
        if (firstCall || collectedCount >= 10)
        {
            return true;
        }
        return this.getBounds().getyTop() > Border.YBOTTOM /*|| this.getBounds().getYCorners()
               [1] < Border.YTOP */;
    }

    @Override
    public void resumeGame()
    {
        super.resumeGame();
        previousTime = System.nanoTime();
    }

    public void spiralIntoVortex(float vortexX)
    {
        if (!collected)
        {
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

        float arcLength = (float) Math.PI / 5.0f;//Math.abs(deltaY) + Math.abs(deltaX);

        float spiralIncrement = 0.01f;

        float radius = initialRadius - spiralFactor;
        float newX;
        float newZ;

        newX = radius * (float) Math.cos(parametricAngle + initialAngle);
        newZ = radius * (float) Math.sin(parametricAngle + initialAngle);

        //counter clockwise rotation
        parametricAngle += arcLength;// / slowdown;

        deltaY = 0;
        deltaX = newX - xPos;
        deltaZ = newZ - zPos;
        xPos = newX;//xPos + deltaX;
        zPos = newZ;//zPos + deltaZ;

        getBounds().setBounds(xPos - scaleFactor, yPos - scaleFactor, xPos + scaleFactor, yPos + scaleFactor);

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

        collected = true;
    }


    public void travelOnVector(float xComponent, float yComponent)
    {
        float time = (System.nanoTime() - previousTime) / 1000000000.0f;
        previousTime = System.nanoTime();
        float fallingTime = MASS * time;
        deltaX = 5 * fallingTime * xComponent;
        //deltaY = 5 * fallingTime * yComponent;
        deltaY = 0.000001f;//fallingTime * yComponent;

        xPos += deltaX;
        yPos += deltaY;

        float normalizedX = Math.abs(xComponent / (Math.max(Math.abs(xComponent), Math.abs(yComponent))));

        float normalizedY = Math.abs(yComponent / (Math.max(Math.abs(xComponent), Math.abs(yComponent))));

        float stretchFactor = 0.5f * (normalizedY - normalizedX);

        vortexX = 1.0f - stretchFactor;
        vortexY = 1.0f + stretchFactor;
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
        float time = (System.nanoTime() - previousTime) / 1000000000.0f;
        previousTime = System.nanoTime();
        float fallingTime = MASS * time;

        float[] force = Physics.sumOfForces(x, y);
        float elasticityCoefficient = 0.9f;

        xVelocity += force[0] * fallingTime;

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

            //reflection of falling object off of top border, yVelocity must be positive
            else
            {
                if (Physics.isTopBorderCollision(this.getBounds()) || collision == Physics.COLLISION.TOP_BOTTOM)
                {
                    yVelocity = elasticityCoefficient * Math.abs(yVelocity);
                }
                else
                {
                    if (collision == Physics.COLLISION.BOTTOM_TOP)
                    {
                        yVelocity = -elasticityCoefficient * Math.abs(yVelocity);
                    }
                }
            }
        }

        yVelocity += force[1] * fallingTime;

        deltaX = fallingTime * xVelocity;

        deltaY = fallingTime * yVelocity;

        xPos += deltaX;
        yPos += deltaY;

        getBounds().setBounds(xPos - scaleFactor, yPos - scaleFactor, xPos + scaleFactor, yPos + scaleFactor);

        //Log.e("blowme", "xpos " + xPos + " ypos " + yPos);
        //Log.e("blowme", "ycorners0 " + getBounds().getYCorners()[0] + " ycorners1 " + getBounds().getYCorners()[1]);
    }

    public boolean isCollected()
    {
        return collected;
    }

    private boolean firstCall = true;

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

    private float vortexX = 1.0f;

    private float vortexY = 1.0f;

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

    private boolean collected;

    private float initialRadius;
    private float initialAngle;

    private int spiralCount = 0;

}
