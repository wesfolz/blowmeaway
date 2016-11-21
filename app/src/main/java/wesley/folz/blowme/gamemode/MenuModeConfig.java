package wesley.folz.blowme.gamemode;

import android.util.Log;

import wesley.folz.blowme.graphics.models.FallingObject;

/**
 * Created by Wesley on 11/20/2016.
 */

public class MenuModeConfig extends ModeConfig
{

    public void updatePositionsAndDrawModels()
    {
        background.updatePosition(0, 0);
        background.draw();

        fan.draw();

        dispenser.updatePosition(0, 0);
        dispenser.draw();

        int modelCount = 0;
        for (FallingObject falObj : fallingObjects)
        {
            if (falObj.isOffscreen())
            {
                Log.e("falobj", "offscreen");
                try
                {
                    models.remove(falObj);
                    //falObj = falObj.getClass().getConstructor(float.class).newInstance(dispenser.getxPos());
                    String type = falObj.getType();
                    falObj = new FallingObject(type, dispenser.getxPos());
                    fallingObjects.set(modelCount, falObj);
                    falObj.enableGraphics(graphicsData);
                    falObj.initializeMatrices(viewMatrix, projectionMatrix, lightPosInEyeSpace);
                    models.add(falObj);
                }
                catch (Exception e)
                {
                    Log.e("error", e.getMessage());
                }
            }

            falObj.updatePosition(0, 0);
            falObj.draw();
            modelCount++;
        }

        //line.updatePosition(0, 0);
        //line.draw();
    }
}
