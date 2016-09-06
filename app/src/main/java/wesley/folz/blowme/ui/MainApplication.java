package wesley.folz.blowme.ui;

import android.app.Application;
import android.content.Context;

/**
 * Created by wesley on 5/11/2015.
 */
public class MainApplication extends Application
{
    private static Context context;

    public void onCreate()
    {
        super.onCreate();
        MainApplication.context = getApplicationContext();
    }

    public static Context getAppContext()
    {
        return MainApplication.context;
    }
}
