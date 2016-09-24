package wesley.folz.blowme.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import wesley.folz.blowme.R;


public class MainMenuActivity extends Activity
{

    @Override
    protected void onCreate( Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_main_menu );
    }

    public void startGame(View view)
    {
        Intent gameIntent = new Intent( this, GamePlayActivity.class );
        startActivity( gameIntent );
    }

}
