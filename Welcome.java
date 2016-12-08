package uiviewsxml.myandroidhello.com.earthquakerssfeed;

import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Ray Cheing on 24/11/2016.
 */

public class Welcome extends Activity {

    MediaPlayer mp;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.welcome_page);

        mp = MediaPlayer.create(getApplicationContext(), R.raw.quake);
        mp.start();

        new Timer().schedule(new TimerTask() {

            @Override
            public void run() {

                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                finish();
                mp.stop();
            }
        }, 3500); //setting time for the greeting page auto re-direct to main page, as well as stopping the greeting music


    }

}
