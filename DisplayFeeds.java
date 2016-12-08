package uiviewsxml.myandroidhello.com.earthquakerssfeed;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.SQLException;
import android.location.Address;
import android.location.Geocoder;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import uiviewsxml.myandroidhello.com.earthquakerssfeed.data.RssFeedItem;
import uiviewsxml.myandroidhello.com.earthquakerssfeed.util.RssRead;
import uiviewsxml.myandroidhello.com.earthquakerssfeed.util.ShakeEventManager;
import uiviewsxml.myandroidhello.com.earthquakerssfeed.util.XMLPullParserHandler;
import uiviewsxml.myandroidhello.com.earthquakerssfeed.util.dbMgr;

public class DisplayFeeds extends AppCompatActivity implements ShakeEventManager.ShakeListener {
    Bundle extras;
    int option;
    String xml;
    ListView lv;
    private List<RssFeedItem> rssItems = new ArrayList<>();
    //getting country code from database
    ArrayList<Integer> ctc = new ArrayList<>();
    ArrayList<String> mag = new ArrayList<>();
    ArrayAdapter<RssFeedItem> adapter;
    private ShakeEventManager sd;
    int c;
    ProgressDialog progress;
    //get country from lat long
    Geocoder gcd;
    dbMgr mgr;
    AlertDialog dialog;
    FloatingActionButton fab;
    //for playing the sos morse code
    MediaPlayer mp;
    int drawableID;
    Boolean isSound = false;

    final String FEEDURL = "http://www.emsc-csem.org/service/rss/rss.php";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_feeds);

        lv = (ListView) findViewById(R.id.list);
        sd = new ShakeEventManager();
        sd.setListener(this);
        sd.init(this);

        //database
        mgr = new dbMgr(this, "countrys.s3db", null, 1);

        try {
            mgr.dbCreate();
        } catch
                (IOException e) {
            e.printStackTrace();
        }


        extras = getIntent().getExtras();
        option = extras.getInt("option");

        gcd = new Geocoder(getApplicationContext(), Locale.getDefault());

        new GetRssTask().execute();


        mp = MediaPlayer.create(this, R.raw.sos);
        mp.setLooping(true);
        mp.setVolume(1.0f, 1.0f);


        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Click action
                if (!isSound) {
                    Toast.makeText(getApplicationContext(), "Sounding SOS morse code ", Toast.LENGTH_SHORT).show();
                    fab.setImageResource(R.drawable.ic_alarm_off_black_24dp); //changing the icon
                    mp.start(); //play the sos sound
                    isSound = true;
                } else {
                    Toast.makeText(getApplicationContext(), "Sounding Stopped ", Toast.LENGTH_SHORT).show();
                    fab.setImageResource(R.drawable.ic_alarm_black_24dp);
                    mp.pause();
                    isSound = false;
                }

            }
        });


    }

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.feed_action, menu);


        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Take appropriate action for each action item click
        switch (item.getItemId()) {
            case R.id.action_refresh:
                onShake(); //uses the same function as shake to refresh
                return true;
            case R.id.action_help:
                AlertDialog.Builder builder = new AlertDialog.Builder(DisplayFeeds.this);
                builder.setTitle("Help");
                builder.setMessage("*Click on any feed to show map view\n*Click refresh or shake to reload feeds.\n*Click on floating action button for SOS rescue sound" +
                        "\n*Long press on a feed to load the context menu\n*Click the line graph button for most recent quakes in a line graph");
                builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

                builder.setIcon(android.R.drawable.ic_dialog_alert);
                dialog = builder.create();
                dialog.show();
                return true;
            case R.id.action_graph:
                Intent intent = new Intent(getApplicationContext(), Graph.class);
                intent.putStringArrayListExtra("mag", mag); //passing the magnitudes as array to the Graph class

                startActivity(intent);
                return true;
            case R.id.action_about:
                AlertDialog.Builder bdr = new AlertDialog.Builder(DisplayFeeds.this);
                bdr.setTitle("About");
                bdr.setMessage("This is an app aim to provide you the latest earthquake alert\nIt is also an Emergency Alert device to help rescue team to locate you  should you are trapped\n");
                bdr.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

                bdr.setIcon(R.drawable.ic_info_black_24dp);
                dialog = bdr.create();
                dialog.show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        if (v.getId() == R.id.list) {
            AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
            menu.setHeaderTitle("Long Press Menu");
            menu.add(Menu.NONE, 0, 0, "Save to Database");//setting up the context menu
        }
    }

    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        Toast.makeText(this, "It will soon be implemented", Toast.LENGTH_LONG).show();
        return true;
    }

    //setting up the addapter for the feeds
    private class MyListAdapter extends ArrayAdapter<RssFeedItem> {
        private List<RssFeedItem> feeds;
        private ArrayList<RssFeedItem> filtered;

        public MyListAdapter() {
            super(DisplayFeeds.this, R.layout.item, rssItems);

            this.feeds = rssItems;
            //this.filtered = new ArrayList<RssFeedItem>();
            //this.filtered.addAll(feeds);
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            //make sure there is a view
            View itemView = convertView;
            if (itemView == null) {
                itemView = getLayoutInflater().inflate(R.layout.item, parent, false);
            }

            //find the feed
            RssFeedItem currentFeed = rssItems.get(position);

            //Region
            TextView regionText = (TextView) itemView.findViewById(R.id.txtRegion);
            regionText.setText(currentFeed.getRegion());

            //Magnitude
            TextView magText = (TextView) itemView.findViewById(R.id.txtMag);
            magText.setText(currentFeed.getMagnitude());

            //Depth
            TextView depText = (TextView) itemView.findViewById(R.id.txtDepth);
            depText.setText(currentFeed.getDepth());

            //DateTime
            TextView dtText = (TextView) itemView.findViewById(R.id.txtDateTime);
            dtText.setText(currentFeed.getDateTime());

            //Status
            TextView statusText = (TextView) itemView.findViewById(R.id.txtStatus);
            statusText.setText(currentFeed.getStatus());

            //ImageView
            ImageView flag = (ImageView) itemView.findViewById(R.id.imgFlag);
            flag.setImageResource(ctc.get(position));


            return itemView;


        }
    }

    @Override
    public void onShake() { //called when phone is shaken on the DisplayFeeds class
        RssRead puller = new RssRead();
        String xml2 = null;
        try {
            xml2 = puller.sourceListingString(FEEDURL);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        if (!xml2.equals(xml)) { //check if there's any changes since last refresh
            new GetRssTask().execute(); //xml changed, refresh
            Toast.makeText(this, "Refreshed data ", Toast.LENGTH_SHORT).show();
        } else { //xml the same, notify user
            Toast.makeText(this, "Feed up to date ", Toast.LENGTH_SHORT).show();
        }

    }
    //Doing the RSS Read, Parsing and querying county code by using geocoder and dblite
    private class GetRssTask extends AsyncTask<Void, String, Void> {
        @Override
        protected void onPreExecute() {
            progress = new ProgressDialog(DisplayFeeds.this);
            progress.setTitle("Retrieving RSS");
            progress.setMessage("Loading RSS... ");
            progress.setCancelable(true);
            progress.setCanceledOnTouchOutside(true);
            progress.show();
        }

        @Override
        protected Void doInBackground(Void... params) {
            RssRead puller = new RssRead();
            try {
                xml = puller.sourceListingString(FEEDURL);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            publishProgress("Download Rss Feed Successful \nParsing...");

            XMLPullParserHandler parser = new XMLPullParserHandler(getApplicationContext());
            rssItems = parser.parse(xml);

            publishProgress("Download Rss Feed and parse Successful \nGetting country code...");
            for (RssFeedItem magnit : rssItems) { //storing all magnitude in mag arraylist to be used to draw to canvas
                mag.add(magnit.getMagnitude().substring(magnit.getMagnitude().lastIndexOf(" "), magnit.getMagnitude().length()));
                try {
                    Double lat = Double.parseDouble(magnit.getGeoLat());
                    Double lng = Double.parseDouble(magnit.getGeoLong());

                    try {
                        mgr.openDataBase();
                    } catch (SQLException s) {
                        throw s;
                    }

                    List<Address> addresses = gcd.getFromLocation(lat, lng, 1); //using Geocoder to get country name from coordinate
                    if (addresses.size() > 0) {
                        String country = addresses.get(0).getCountryName();
                        String cCode = mgr.findCountry(country);  //query the db to get the corresponding code for each country
                        drawableID = getResources().getIdentifier(cCode, "drawable", getPackageName()); //get the int for use to locate the pic in drawable
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
                ctc.add(drawableID); //adding all id to an arraylist
            }
            publishProgress("Parsing Successful");

            return null;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            progress.setMessage(values[0]);
        }

        @Override
        protected void onPostExecute(Void result) {
            adapter = new MyListAdapter();


            lv.setAdapter(adapter);
            registerForContextMenu(lv);


            progress.dismiss();

            Toast.makeText(DisplayFeeds.this, "DONE", Toast.LENGTH_LONG).show();

            //setting a listen for redirection to map
            lv.setOnItemClickListener(new OnItemClickListener() {
                public void onItemClick(AdapterView<?> parent, View view, int pos, long rowid) {
                    parent.getItemAtPosition(pos);
                    String tempRegion = adapter.getItem(pos).getRegion();

                    Intent intent = new Intent(getApplicationContext(), Map.class);
                    intent.putExtra("lat", adapter.getItem(pos).getGeoLat());
                    intent.putExtra("long", adapter.getItem(pos).getGeoLong());
                    intent.putExtra("mag", adapter.getItem(pos).getMagnitude());
                    intent.putExtra("stat", adapter.getItem(pos).getStatus());
                    intent.putExtra("reg", tempRegion);


                    startActivity(intent);
                }
            });


        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        sd.register();
        if (isSound) {
            mp.start(); //resume playing when back in focus, should it be playing before pause.
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        sd.deregister();
        if (mp.isPlaying()) {//pause the SOS sound when onpause, e.g. goes out of focus
            mp.pause();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putInt("MyInt", c); //keep track of refresh count, through different orientation
        savedInstanceState.putBoolean("IsPlaying", isSound);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        c = savedInstanceState.getInt("MyInt");
        isSound = savedInstanceState.getBoolean("IsPlaying");
    }

}
