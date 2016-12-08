package uiviewsxml.myandroidhello.com.earthquakerssfeed;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.ArrayList;

public class Graph extends AppCompatActivity {

    ArrayList<String> mag = new ArrayList<>();
    String temp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.graph);

        mag = getIntent().getStringArrayListExtra("mag");


        GraphView graph = (GraphView) findViewById(R.id.graph);
        graph.getViewport().setScalable(true);
        graph.getViewport().setScrollable(true);
        graph.setTitle("50 Recent Earthquakes' Magnitude");
        graph.setTitleTextSize(50);

        //writing to DataPoint[], base on the intent carried over from DisplayFeeds Class
        DataPoint[] values = new DataPoint[mag.size()];
        for (int i = 0; i < mag.size(); i++) {
            Double x = i + 1d;
            Double y = Double.valueOf(mag.get(i));
            DataPoint v = new DataPoint(x, y);
            values[i] = v;
        }
        LineGraphSeries<DataPoint> series = new LineGraphSeries<>(values);
        graph.addSeries(series);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }
}
