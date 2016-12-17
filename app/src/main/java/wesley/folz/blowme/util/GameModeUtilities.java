package wesley.folz.blowme.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import wesley.folz.blowme.ui.MainApplication;

/**
 * Created by Wesley on 12/4/2016.
 */
public class GameModeUtilities {
    /*-----------------------------------------Constructors---------------------------------------*/

    /*---------------------------------------Override Methods-------------------------------------*/

    /*---------------------------------------Public Methods---------------------------------------*/

    /*-------------------------------------Protected Methods--------------------------------------*/

    /*--------------------------------------Private Methods---------------------------------------*/

    public static String readAsset(String assetName) {
        StringBuilder buf = new StringBuilder();
        InputStream json;
        String str;

        try {
            json = MainApplication.getAppContext().getAssets().open(assetName);

            BufferedReader in =
                    new BufferedReader(new InputStreamReader(json, "UTF-8"));

            while ((str = in.readLine()) != null) {
                buf.append(str);
            }

            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return buf.toString();
    }

    /*--------------------------------------Getters and Setters-----------------------------------*/

    /*----------------------------------------Public Fields---------------------------------------*/

    /*--------------------------------------Protected Fields--------------------------------------*/

    /*---------------------------------------Private Fields---------------------------------------*/
}
