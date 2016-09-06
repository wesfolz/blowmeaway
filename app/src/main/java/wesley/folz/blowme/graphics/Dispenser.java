package wesley.folz.blowme.graphics;

import android.opengl.GLSurfaceView;
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
        yPos = GamePlayActivity.Y_EDGE_POSITION;
        initialRotation = new float[16];
        motionMultiplier = 1;
    }

    @Override
    public float[] createTransformationMatrix()
    {
        Matrix.translateM( modelMatrix, 0, deltaX, 0, 0 );
        Matrix.translateM( mvMatrix, 0, deltaX, 0, 0 );
        Matrix.translateM( mvpMatrix, 0, deltaX, 0, 0 );

        return mvpMatrix;
    }

    @Override
    public void initializeMatrix()
    {
        super.initializeMatrix();
        //rotate 180 degrees about x-axis
        Matrix.setRotateM( initialRotation, 0, 180, 1, 0, 0 );
        //Matrix.setIdentityM( secondRotation, 0 );
        //Matrix.scaleM( initialTransformationMatrix, 0, 0.1f, 0.1f, 0.1f );
        Matrix.translateM( mvpMatrix, 0, 0, yPos, 0 );

        Matrix.setIdentityM(modelMatrix, 0);
        Matrix.translateM(modelMatrix, 0, 0, yPos, 0);

 //       Matrix.translateM(modelMatrix, 0, 0.0f, 0, -5.0f);

        Matrix.multiplyMM(mvMatrix, 0, viewMatrix, 0, modelMatrix, 0);

        //Matrix.translateM(mvpMatrix, 0, 0.0f, 0, -5.0f);

        Matrix.scaleM( modelMatrix, 0, 0.2f, 0.2f, 0.2f );
        Matrix.scaleM( mvpMatrix, 0, 0.2f, 0.2f, 0.2f );

        //Log.e( "blowme", "xpos: " + xPos + " ypos " + yPos );
    }

    @Override
    public void updatePosition( float x, float y )
    {
        //long time = SystemClock.uptimeMillis()% 10000L;
        deltaX = 0.05f;//* ((int) time);

        if( xPos >= 5 * GamePlayActivity.X_EDGE_POSITION )
            motionMultiplier = - 1;
        if( xPos <= - 5 * GamePlayActivity.X_EDGE_POSITION )
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
