package wesley.folz.blowme.graphics;

import android.opengl.Matrix;

import wesley.folz.blowme.R;
import wesley.folz.blowme.util.GraphicsReader;

/**
 * Created by Wesley on 9/24/2016.
 */

public class RicochetObstacle extends Model
{

    public RicochetObstacle()
    {
        super();
        this.OBJ_FILE_RESOURCE = R.raw.cube;
        this.VERTEX_SHADER = R.raw.fan_vertex_shader;
        this.FRAGMENT_SHADER = R.raw.fan_fragment_shader;
        GraphicsReader.readOBJFile(this);
        GraphicsReader.readShader(this);
        xPos = 0;//+.01f;
        yPos = 1.1f;//GamePlayActivity.Y_EDGE_POSITION;

        scaleFactor = 0.2f;

        initialXPos = xPos;
        initialYPos = -yPos;
    }


    @Override
    public float[] createTransformationMatrix()
    {
        float[] mvp = new float[16];
        Matrix.setIdentityM(mvp, 0);

        //translate model matrix to new position
        Matrix.translateM(modelMatrix, 0, 0, deltaY, 0);

        //copy modelMatrix to separate matrix for return, (returning modelMatrix doesn't work)
        Matrix.multiplyMM(mvp, 0, modelMatrix, 0, mvp, 0);

        return mvp;
    }

    @Override
    public void updatePosition(float x, float y)
    {

        float deltaTime = (System.nanoTime() - previousTime) / 1000000000.0f;
        previousTime = System.nanoTime();

        deltaY = deltaTime * risingSpeed;
        yPos += deltaY;
    }

    private float deltaY;

    private float previousTime;

    private float risingSpeed = 0.1f;
}
