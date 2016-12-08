package uiviewsxml.myandroidhello.com.earthquakerssfeed;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    Button allFeed;
    Button localFeed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        allFeed = (Button) findViewById(R.id.btnFeed);
        localFeed = (Button) findViewById(R.id.btnLocal);

        allFeed.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), DisplayFeeds.class);
                intent.putExtra("option", 1);
                startActivity(intent);
            }
        });
        //show quake close to the user will be implemented in the future
        localFeed.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "It will soon be implemented", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
