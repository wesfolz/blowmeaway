package wesley.folz.blowme.graphics.models;

import android.graphics.PointF;
import android.opengl.Matrix;
import android.util.Log;

import wesley.folz.blowme.graphics.effects.Explosion;
import wesley.folz.blowme.graphics.effects.Sparkler;
import wesley.folz.blowme.util.BezierCurve;
import wesley.folz.blowme.util.GraphicsUtilities;

/**
 * Created by Wesley on 12/28/2017.
 */

public class Fuse extends RicochetObstacle {
    public Fuse(float x, float y, float time) {
        super(x, y);
        scaleFactor = 0.0225f;
        //scaleFactor = 2f;
        yDirection = 0;//xPos / Math.abs(xPos);
        xDirection = xPos / Math.abs(xPos);
        sparkler = new Sparkler(x, y);

        //ctx.bezierCurveTo(150, 100, 350, 100, 400, 250);
        //path = new BezierCurve(new PointF[]{new PointF(0.1f, 0.2f), new PointF(0.21f, 0.32f),
        //        new PointF(0.32f, 0.14f), new PointF(0.4f, 0.25f)});

        //path = new BezierCurve(new PointF[]{new PointF(x, y), new PointF(x+0.11f, y+0.12f),
        //        new PointF(x+0.22f, y-0.06f), new PointF(x+0.3f, y+0.05f)});

        //ctx.moveTo(340, 60);
        //ctx.bezierCurveTo(350, 150, 270, 145, 325, 225);
        //path = new BezierCurve(new PointF[]{new PointF(x, y), new PointF(x+0.01f, y-0.09f),
        //        new PointF(x-0.07f, y-0.085f), new PointF(x-0.015f, y-0.165f)});

        /*
        y+=0.19f;
        x+=0.06f;
        path = new BezierCurve(new PointF[]{new PointF(x, y), new PointF(x+0.01f, y-0.09f),
                new PointF(x-0.15f, y-0.085f), new PointF(x, y-0.4f)});
        */

        float scale = 0.225f;
        path = new BezierCurve(
                new PointF[]{new PointF(0, 0), new PointF(0.002f * scale, -0.018f * scale),
                        new PointF(-0.03f * scale, -0.017f * scale), new PointF(0, -0.08f * scale)},
                time);

        explosion = new Explosion();

    }

    @Override
    public void enableGraphics(GraphicsUtilities graphicsData) {
        //get dataVBO, orderVBO, program, texture handles
        dataVBO = graphicsData.modelVBOMap.get("fuse");
        orderVBO = graphicsData.orderVBOMap.get("fuse");
        numVertices = graphicsData.numVerticesMap.get("fuse");
        programHandle = graphicsData.shaderProgramIdMap.get("lighting");
        //textureDataHandle = graphicsData.textureIdMap.get("launcher_tube_tex");
        sparkler.enableGraphics(graphicsData);
        explosion.enableGraphics(graphicsData);
    }

    @Override
    public void draw() {
        if (!burnedOut) {
            super.draw();
            if (ignited) {
                sparkler.draw();
            }
        } else {
            explosion.draw();
        }
    }

    @Override
    public void initializeMatrices(float[] viewMatrix, float[] perspectiveMatrix,
            float[] orthographicMatrix, float[] lightPosInEyeSpace) {
        super.initializeMatrices(viewMatrix, perspectiveMatrix, orthographicMatrix,
                lightPosInEyeSpace);
        sparkler.initializeMatrices(viewMatrix, perspectiveMatrix, orthographicMatrix,
                lightPosInEyeSpace);
        explosion.initializeMatrices(viewMatrix, perspectiveMatrix, orthographicMatrix,
                lightPosInEyeSpace);
    }

    @Override
    public float[] createTransformationMatrix() {
        float[] rotation = new float[16];

        Matrix.setIdentityM(rotation, 0);

        Matrix.translateM(modelMatrix, 0, getDeltaX(), getDeltaY(), 0); //translate missile

        Matrix.multiplyMM(rotation, 0, modelMatrix, 0, rotation, 0); //spin missile

        float rotationAngle = 0;//-90.0f;
        if (initialXPos > 0) {
            rotationAngle = 180.0f;
        }
        if (yDirection != 0) {
            rotationAngle = 180.0f - (float) (180.0 * Math.atan2(yDirection, xDirection) / Math.PI);
        }

        Matrix.rotateM(rotation, 0, rotationAngle, 0, 0, 1);

        if (initialXPos > 0) {
            Matrix.rotateM(rotation, 0, 180, 1, 0, 0);
        }

        sparkler.setTransformationMatrix(calculateSparkMatrix());

        return rotation;
    }

    private float[] calculateSparkMatrix() {
        float[] rotation = new float[16];

        Matrix.setIdentityM(rotation, 0);

        Matrix.translateM(sparkler.modelMatrix, 0, getDeltaX(), getDeltaY(), 0); //translate missile

        Matrix.multiplyMM(rotation, 0, sparkler.modelMatrix, 0, rotation, 0);

        float rotationAngle = 0;//-90.0f;
        if (initialXPos > 0) {
            rotationAngle = 180.0f;
        }
        if (yDirection != 0) {
            rotationAngle = 180.0f - (float) (180.0 * Math.atan2(yDirection, xDirection) / Math.PI);
        }

        //rotationAngle = 0;
        Matrix.rotateM(rotation, 0, rotationAngle, 0, 0, 1);

        if (initialXPos > 0) {
            Matrix.rotateM(rotation, 0, 180, 1, 0, 0);
        }

        if (ignited && !burnedOut) {
            Matrix.multiplyMM(rotation, 0, rotation, 0, path.computeBezierTranslation(), 0);
        }

        Matrix.translateM(rotation, 0, -0.047f, 0.05f, 0);

        sparkler.setxPos(xPos - 0.047f);
        sparkler.setyPos(yPos + 0.05f);

        return rotation;
    }

    @Override
    public void updatePosition(float x, float y) {

        //Physics.panUpDown(this, Physics.rise(this));
        deltaY = y;
        yPos += deltaY;
        deltaX = x;
        xPos += deltaX;
        deltaZ = 0;

        if (ignited && !burnedOut) {
            //Log.e("ignited", "true");
            sparkler.updatePosition(x, y);
            burnedOut = path.isPathComplete();
            if (burnedOut) {
                Log.e("burnedOut", "ignited " + ignited);
                explosion.reinitialize(sparkler.getxPos(), sparkler.getyPos());
                ignited = false;
            }
        }
    }

    public boolean isBurnedOut() {
        return burnedOut;
    }


    public boolean isIgnited() {
        return ignited;
    }

    public void setIgnited(boolean ignited) {
        this.ignited = ignited;
        sparkler.setxPos(xPos);
    }

    private Sparkler sparkler;

    private BezierCurve path;

    private boolean burnedOut = false;

    private Explosion explosion;

    private boolean ignited = false;
}