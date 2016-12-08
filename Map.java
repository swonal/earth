package uiviewsxml.myandroidhello.com.earthquakerssfeed;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

/**
 * Created by Ray Cheung on 24/11/2016.
 */

public class Map extends Activity implements OnMapReadyCallback {

    Bundle bundle;
    int drawableId;
    String sLat, sLng, stat, mag, reg;
    Double lat, lng;
    private GoogleMap map;
    private LatLng loc;
    TextView region;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map);
        region = (TextView) findViewById(R.id.mapTitle);

        bundle = getIntent().getExtras();
        sLat = bundle.getString("lat");
        sLng = bundle.getString("long");
        stat = bundle.getString("stat");
        mag = bundle.getString("mag");
        reg = bundle.getString("reg");
        drawableId = bundle.getInt("drawableId");


        lat = Double.parseDouble(sLat);
        lng = Double.parseDouble(sLng);
        loc = new LatLng(lat, lng);
        region.setText(reg);

        ((MapFragment) getFragmentManager().findFragmentById(R.id.map))
                .getMapAsync(this);


    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        map.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                //changing map type by clicking on the marker
                if (map.getMapType() == GoogleMap.MAP_TYPE_HYBRID) {
                    map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                    Toast.makeText(getApplicationContext(), "Map changed to Normal", Toast.LENGTH_SHORT).show();
                } else { //to be implemented, get user location and calculate distance
                    map.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                    Toast.makeText(getApplicationContext(), "Map changed to Hybrid", Toast.LENGTH_SHORT).show();
                }
                return false;
            }
        });
        map.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                Toast.makeText(getApplicationContext(), "Checking distance! (To be implemented)", Toast.LENGTH_LONG).show();
            }
        });
        setUp();
    }

    private void setUp() {
        map.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        map.getUiSettings().setMapToolbarEnabled(true);
        map.getUiSettings().setMyLocationButtonEnabled(true);


        Bitmap.Config conf = Bitmap.Config.ARGB_8888;
        Bitmap bmp = Bitmap.createBitmap(500, 500, conf);
        Canvas canvas1 = new Canvas(bmp);

// paint defines the text color, stroke width and size
        Paint color = new Paint();
        color.setTextSize(35);
        color.setColor(Color.BLACK);

// modify canvas
        canvas1.drawBitmap(BitmapFactory.decodeResource(getResources(),
                R.drawable.ic_quake_2s), 0, 0, color);
        //canvas1.drawText(mag, 30, 40, color);

// add marker to Map
        map.addMarker(new MarkerOptions().position(loc)
                .icon(BitmapDescriptorFactory.fromBitmap(bmp))
                // Specifies the anchor to be at a particular point in the marker image.
                .anchor(0, 0).infoWindowAnchor(0.15f, 0).snippet("Click on marker to change map type\nClick on this to get distance from quake").title(stat + " " + mag)).showInfoWindow();
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(loc, 14));
    }

}
