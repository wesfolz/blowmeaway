package wesley.folz.blowme.graphics;

import android.opengl.Matrix;
import android.os.SystemClock;

import wesley.folz.blowme.R;
import wesley.folz.blowme.ui.GamePlayActivity;
import wesley.folz.blowme.util.Bounds;
import wesley.folz.blowme.util.GraphicsReader;

/**
 * Created by Wesley on 9/11/2016.
 */
public class Vortex extends Model {
    public Vortex() {
        super();
        setBounds(new Bounds());

        this.OBJ_FILE_RESOURCE = R.raw.vortex_open_top;
        this.VERTEX_SHADER = R.raw.fan_vertex_shader;
        this.FRAGMENT_SHADER = R.raw.fan_fragment_shader;
        this.TEXTURE_RESOURCE = R.raw.tornado_texture;
        GraphicsReader.readOBJFile(this);
        GraphicsReader.readShader(this);
        xPos = 0;//+.01f;
        yPos = 0.8f;//0.935f;//GamePlayActivity.Y_EDGE_POSITION;
        motionMultiplier = 1;

        scaleFactor = 0.04f;

        initialXPos = xPos;
        initialYPos = -yPos;
        yPos -= 0.07f;
    }

    //TODO: Apply rotation transformation about the z-axis to vertices only above a certain y-value to "bend vortex towards falling object"
    @Override
    public float[] createTransformationMatrix() {
        float[] mvp = new float[16];

        long time = SystemClock.uptimeMillis();// % 4000L;
        float angle = 0.40f * ((int) time);

        Matrix.setIdentityM(mvp, 0);

        //translate model matrix to new position
        Matrix.translateM(modelMatrix, 0, deltaX, 0, 0);

        //copy modelMatrix to separate matrix for return, (returning modelMatrix doesn't work)
        Matrix.multiplyMM(mvp, 0, modelMatrix, 0, mvp, 0);

        Matrix.rotateM(mvp, 0, angle, 0, -1, 0);

        return mvp;
    }

    @Override
    public void initializeMatrices(float[] viewMatrix, float[] projectionMatrix, float[] lightPositionInEyeSpace) {
        super.initializeMatrices(viewMatrix, projectionMatrix, lightPositionInEyeSpace);

        if (!this.resuming) {
            //rotate 130 degrees about x-axis
            Matrix.rotateM(modelMatrix, 0, 10, 1, 0, 0);
            //Matrix.scaleM(modelMatrix, 0, 1.0f, 0.5f, 1.0f);
        }

        //Log.e( "blowme", "xpos: " + xPos + " ypos " + yPos );
    }

    @Override
    public void updatePosition(float x, float y) {
        //long time = SystemClock.uptimeMillis()% 10000L;
        //deltaX = 0.01f;//* ((int) time);

        if (xPos >= GamePlayActivity.X_EDGE_POSITION)
            motionMultiplier = -1;
        if (xPos <= -GamePlayActivity.X_EDGE_POSITION)
            motionMultiplier = 1;
        //deltaX *= motionMultiplier;
        //xPos += deltaX;//*motionMultiplier;

        getBounds().setBounds(xPos - 0.1f, yPos - 0.15f, xPos + 0.1f, yPos);
    }

    public float getDeltaX() {
        return deltaX;
    }

    private float deltaX;

    private int motionMultiplier;

}
