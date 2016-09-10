package wesley.folz.blowme.graphics;

import android.opengl.Matrix;

import wesley.folz.blowme.R;
import wesley.folz.blowme.ui.GamePlayActivity;
import wesley.folz.blowme.util.GraphicsReader;

/**
 * Created by wesley on 7/25/2015.
 */
public class Dispenser extends Model
{
    public Dispenser()
    {
        super();
        this.OBJ_FILE_RESOURCE = R.raw.triangle_collector;
        this.VERTEX_SHADER = R.raw.fan_vertex_shader;
        this.FRAGMENT_SHADER = R.raw.fan_fragment_shader;
        GraphicsReader.readOBJFile(this);
        GraphicsReader.readShader(this);
        xPos = 0;//+.01f;
        yPos = 0.935f;//GamePlayActivity.Y_EDGE_POSITION;
        initialRotation = new float[16];
        motionMultiplier = 1;

        scaleFactor = 0.2f;

        initialXPos = xPos;
        initialYPos = yPos;
    }

    @Override
    public float[] createTransformationMatrix()
    {
        float[] mvp = new float[16];

        Matrix.setIdentityM(mvp, 0);

        //translate model matrix to new position
        Matrix.translateM( modelMatrix, 0, deltaX, 0, 0 );

        //copy modelMatrix to separate matrix for return, (returning modelMatrix doesn't work)
        Matrix.multiplyMM(mvp, 0, modelMatrix, 0, mvp, 0);

        return mvp;
    }

    @Override
    public void initializeMatrix()
    {
        super.initializeMatrix();

        if (!this.resuming)
        {
            //rotate 180 degrees about x-axis
            Matrix.setRotateM(initialRotation, 0, 180, 1, 0, 0);

            Matrix.scaleM(modelMatrix, 0, 1.0f, 0.5f, 1.0f);
        }

        //Log.e( "blowme", "xpos: " + xPos + " ypos " + yPos );
    }

    @Override
    public void updatePosition( float x, float y )
    {
        //long time = SystemClock.uptimeMillis()% 10000L;
        deltaX = 0.01f;//* ((int) time);

        if( xPos >= GamePlayActivity.X_EDGE_POSITION )
            motionMultiplier = - 1;
        if( xPos <= -GamePlayActivity.X_EDGE_POSITION )
            motionMultiplier = 1;
        deltaX *= motionMultiplier;
        xPos += deltaX;//*motionMultiplier;
        //Log.e( "blowme", "motionmultiplier " + motionMultiplier + " deltax " + deltaX + " xpos "
        //       + xPos );
    }

    private float deltaX;

    private int motionMultiplier;

    private float[] initialRotation;
}
