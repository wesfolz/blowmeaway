package wesley.folz.blowme.util;

import android.graphics.PointF;
import android.opengl.Matrix;

/**
 * Created by Wesley on 2/10/2018.
 */

public class BezierCurve {

    //https://en.wikipedia.org/wiki/Bezier_curve
    public BezierCurve(PointF[] controlPoints, float time) {
        this.controlPoints = controlPoints;
        this.totalTime = time;
        this.previousUpdateTime = 0;
    }

    public float[] computeBezierTranslation() {
        if (previousUpdateTime == 0) {
            previousUpdateTime = System.nanoTime();
        }
        long time = System.nanoTime();
        float deltaTime = (time - previousUpdateTime) / 1000000000.0f;

        float t = deltaTime / totalTime;

        if (t >= 1.0f) {
            t = 1.0f;
            pathComplete = true;
        }

        switch (controlPoints.length) {
            case 3:
                return quadraticCurve(t);

            case 4:
                return cubicCurve(t);

            default:
                float[] identity = new float[16];
                Matrix.setIdentityM(identity, 0);
                return identity;
        }
    }

    private float[] quadraticCurve(float time) {
        float[] bezierMatrix = new float[16];

        Matrix.setIdentityM(bezierMatrix, 0);

        float Bx = (1.0f - time) * (1.0f - time) * controlPoints[0].x
                + 2 * (1.0f - time) * time * controlPoints[1].x + time * time * controlPoints[2].x;

        float By = (1.0f - time) * (1.0f - time) * controlPoints[0].y
                + 2 * (1.0f - time) * time * controlPoints[1].y + time * time * controlPoints[2].y;

        Matrix.translateM(bezierMatrix, 0, Bx, By, 0);

        return bezierMatrix;
    }

    private float[] cubicCurve(float time) {
        float[] bezierMatrix = new float[16];

        Matrix.setIdentityM(bezierMatrix, 0);

        float Bx = (1.0f - time) * (1.0f - time) * (1.0f - time) * controlPoints[0].x
                + 3 * (1.0f - time) * (1.0f - time) * time * controlPoints[1].x
                + 3 * (1.0f - time) * time * time * controlPoints[2].x
                + time * time * time * controlPoints[3].x;

        float By = (1.0f - time) * (1.0f - time) * (1.0f - time) * controlPoints[0].y
                + 3 * (1.0f - time) * (1.0f - time) * time * controlPoints[1].y
                + 3 * (1.0f - time) * time * time * controlPoints[2].y
                + time * time * time * controlPoints[3].y;

        Matrix.translateM(bezierMatrix, 0, Bx, By, 0);

        return bezierMatrix;
    }

    public boolean isPathComplete() {
        return pathComplete;
    }

    private PointF[] controlPoints;

    private long previousUpdateTime;

    private float totalTime;

    private boolean pathComplete = false;
}
