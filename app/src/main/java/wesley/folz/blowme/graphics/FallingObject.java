package wesley.folz.blowme.graphics;

import android.opengl.GLSurfaceView;
import android.opengl.Matrix;

import wesley.folz.blowme.R;
import wesley.folz.blowme.ui.GamePlayActivity;
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
        super();
        setBounds(new Bounds());

        this.OBJ_FILE_RESOURCE = R.raw.cube;
        this.VERTEX_SHADER = R.raw.fan_vertex_shader;
        this.FRAGMENT_SHADER = R.raw.fan_fragment_shader;
        GraphicsReader.readShader(this);
        GraphicsReader.readOBJFile(this);

/*
        // Front face
        vertexData = new float[]{
                -1.0f, 1.0f, 1.0f,
                -1.0f, -1.0f, 1.0f,
                1.0f, 1.0f, 1.0f,
                -1.0f, -1.0f, 1.0f,
                1.0f, -1.0f, 1.0f,
                1.0f, 1.0f, 1.0f,

                // Right face
                1.0f, 1.0f, 1.0f,
                1.0f, -1.0f, 1.0f,
                1.0f, 1.0f, -1.0f,
                1.0f, -1.0f, 1.0f,
                1.0f, -1.0f, -1.0f,
                1.0f, 1.0f, -1.0f,

                // Back face
                1.0f, 1.0f, -1.0f,
                1.0f, -1.0f, -1.0f,
                -1.0f, 1.0f, -1.0f,
                1.0f, -1.0f, -1.0f,
                -1.0f, -1.0f, -1.0f,
                -1.0f, 1.0f, -1.0f,

                // Left face
                -1.0f, 1.0f, -1.0f,
                -1.0f, -1.0f, -1.0f,
                -1.0f, 1.0f, 1.0f,
                -1.0f, -1.0f, -1.0f,
                -1.0f, -1.0f, 1.0f,
                -1.0f, 1.0f, 1.0f,

                // Top face
                -1.0f, 1.0f, -1.0f,
                -1.0f, 1.0f, 1.0f,
                1.0f, 1.0f, -1.0f,
                -1.0f, 1.0f, 1.0f,
                1.0f, 1.0f, 1.0f,
                1.0f, 1.0f, -1.0f,

                // Bottom face
                1.0f, -1.0f, -1.0f,
                1.0f, -1.0f, 1.0f,
                -1.0f, -1.0f, -1.0f,
                1.0f, -1.0f, 1.0f,
                -1.0f, -1.0f, 1.0f,
                -1.0f, -1.0f, -1.0f,
        };

                colorData = new float[]
                {
                        // Front face (red)
                        1.0f, 0.0f, 0.0f, 1.0f,
                        1.0f, 0.0f, 0.0f, 1.0f,
                        1.0f, 0.0f, 0.0f, 1.0f,
                        1.0f, 0.0f, 0.0f, 1.0f,
                        1.0f, 0.0f, 0.0f, 1.0f,
                        1.0f, 0.0f, 0.0f, 1.0f,

                        // Right face (green)
                        0.0f, 1.0f, 0.0f, 1.0f,
                        0.0f, 1.0f, 0.0f, 1.0f,
                        0.0f, 1.0f, 0.0f, 1.0f,
                        0.0f, 1.0f, 0.0f, 1.0f,
                        0.0f, 1.0f, 0.0f, 1.0f,
                        0.0f, 1.0f, 0.0f, 1.0f,

                        // Back face (blue)
                        0.0f, 0.0f, 1.0f, 1.0f,
                        0.0f, 0.0f, 1.0f, 1.0f,
                        0.0f, 0.0f, 1.0f, 1.0f,
                        0.0f, 0.0f, 1.0f, 1.0f,
                        0.0f, 0.0f, 1.0f, 1.0f,
                        0.0f, 0.0f, 1.0f, 1.0f,

                        // Left face (yellow)
                        1.0f, 1.0f, 0.0f, 1.0f,
                        1.0f, 1.0f, 0.0f, 1.0f,
                        1.0f, 1.0f, 0.0f, 1.0f,
                        1.0f, 1.0f, 0.0f, 1.0f,
                        1.0f, 1.0f, 0.0f, 1.0f,
                        1.0f, 1.0f, 0.0f, 1.0f,

                        // Top face (cyan)
                        0.0f, 1.0f, 1.0f, 1.0f,
                        0.0f, 1.0f, 1.0f, 1.0f,
                        0.0f, 1.0f, 1.0f, 1.0f,
                        0.0f, 1.0f, 1.0f, 1.0f,
                        0.0f, 1.0f, 1.0f, 1.0f,
                        0.0f, 1.0f, 1.0f, 1.0f,

                        // Bottom face (magenta)
                        1.0f, 0.0f, 1.0f, 1.0f,
                        1.0f, 0.0f, 1.0f, 1.0f,
                        1.0f, 0.0f, 1.0f, 1.0f,
                        1.0f, 0.0f, 1.0f, 1.0f,
                        1.0f, 0.0f, 1.0f, 1.0f,
                        1.0f, 0.0f, 1.0f, 1.0f
                };
        normalData = new float[]
                {
                        // Front face
                        0.0f, 0.0f, 1.0f,
                        0.0f, 0.0f, 1.0f,
                        0.0f, 0.0f, 1.0f,
                        0.0f, 0.0f, 1.0f,
                        0.0f, 0.0f, 1.0f,
                        0.0f, 0.0f, 1.0f,

                        // Right face
                        1.0f, 0.0f, 0.0f,
                        1.0f, 0.0f, 0.0f,
                        1.0f, 0.0f, 0.0f,
                        1.0f, 0.0f, 0.0f,
                        1.0f, 0.0f, 0.0f,
                        1.0f, 0.0f, 0.0f,

                        // Back face
                        0.0f, 0.0f, -1.0f,
                        0.0f, 0.0f, -1.0f,
                        0.0f, 0.0f, -1.0f,
                        0.0f, 0.0f, -1.0f,
                        0.0f, 0.0f, -1.0f,
                        0.0f, 0.0f, -1.0f,

                        // Left face
                        -1.0f, 0.0f, 0.0f,
                        -1.0f, 0.0f, 0.0f,
                        -1.0f, 0.0f, 0.0f,
                        -1.0f, 0.0f, 0.0f,
                        -1.0f, 0.0f, 0.0f,
                        -1.0f, 0.0f, 0.0f,

                        // Top face
                        0.0f, 1.0f, 0.0f,
                        0.0f, 1.0f, 0.0f,
                        0.0f, 1.0f, 0.0f,
                        0.0f, 1.0f, 0.0f,
                        0.0f, 1.0f, 0.0f,
                        0.0f, 1.0f, 0.0f,

                        // Bottom face
                        0.0f, -1.0f, 0.0f,
                        0.0f, -1.0f, 0.0f,
                        0.0f, -1.0f, 0.0f,
                        0.0f, -1.0f, 0.0f,
                        0.0f, -1.0f, 0.0f,
                        0.0f, -1.0f, 0.0f
                };

                */
        /*
        vertexData = new float[]{
                0.0f, 0.2f, 0.0f,   // top
                - 0.1f, 0.0f, 0.0f,   // bottom left
                0.1f, 0.0f, 0.0f}; // top right
        */
        vertexOrder = new short[24];//{0, 1, 2};

        for (short i=0; i< 24; i++)
        {
            vertexOrder[i] = i;
        }

        xPos = (float) (Math.random() - 0.5);

        if( xPos > 0.35 )
            xPos = 0.35f;
        if( xPos <= - 0.35 )
            xPos = - 0.35f;

        yPos = - 1.0f;

        xVelocity = 0;

        yVelocity = 0;//0.1f;

        previousTime = System.nanoTime();
    }

    @Override
    public float[] createTransformationMatrix()
    {
        float[] transformation = new float[16];

        float[] result = new float[16];

        //updatePosition( 0, 0 );

        //Matrix.setIdentityM( transformation, 0 );

        //Matrix.translateM( transformation, 0, deltaX, deltaY, 0 );

        //Matrix.multiplyMM( result, 0, mvpMatrix, 0, transformation, 0 );


        Matrix.translateM(modelMatrix, 0, deltaX, -deltaY, 0);
        Matrix.rotateM(modelMatrix, 0, 1.0f, 1.0f, 1.0f, 1.0f);

        Matrix.multiplyMM(mvMatrix, 0, viewMatrix, 0, modelMatrix, 0);

        Matrix.translateM(mvpMatrix, 0, deltaX, -deltaY, 0);
        Matrix.rotateM(mvpMatrix, 0, 1.0f, 1.0f, 1.0f, 1.0f);

        return mvpMatrix;
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
        return false;
//        return this.getBounds().getYCorners()[0] > Border.YMAX /*|| this.getBounds().getYCorners()
//               [1] < Border.YMIN */;
    }

    @Override
    public void initializeMatrix()
    {
        super.initializeMatrix();

        Matrix.setIdentityM(modelMatrix, 0);
        Matrix.translateM(modelMatrix, 0, xPos, -yPos, 0);
        Matrix.scaleM(modelMatrix, 0, 0.1f, 0.1f, 0.1f);

        Matrix.translateM(mvpMatrix, 0, xPos, -yPos, 0);
        Matrix.scaleM(mvpMatrix, 0, 0.1f, 0.1f, 0.1f);
    }

    /**
     * TODO: make movement after wind collision look behave correctly
     *
     * @param x - x component of wind force
     * @param y - y component of wind force
     */
    @Override
    public void updatePosition( float x, float y )
    {
        float time = (System.nanoTime() - previousTime) / 1000000000.0f;
        previousTime = System.nanoTime();
        float fallingTime = MASS * time;


        float[] force = Physics.sumOfForces( x, y );

        xVelocity += force[0] * fallingTime;

        //reflection of falling object off of side border simply changes sign of xVelocity
        if( Physics.isBorderCollision( this.getBounds() ) )
        {
            xVelocity = - xVelocity;
        }

        yVelocity += force[1] * fallingTime;

        deltaX = fallingTime * xVelocity;

        deltaY = fallingTime * yVelocity;


        xPos += deltaX;
        yPos += deltaY;


        getBounds().setBounds( xPos - 0.1f, yPos - 0.1f, xPos + 0.1f, yPos + 0.1f );

        //  Log.e( "blowme", "xpos " + xPos + " ypos " + yPos );

    }

    private long previousTime;


    private float deltaX;

    private float deltaY;

    private float xVelocity;

    private float yVelocity;

    private static final float MASS = 2.0f;
}
