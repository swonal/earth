package uiviewsxml.myandroidhello.com.earthquakerssfeed.util;

import android.os.StrictMode;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by Ray Cheung on 21/11/2016.
 */

//class to download the rssfeed xml file from the website
public class RssRead {
    public String sourceListingString(String urlString) throws IOException {
        String result = "";
        InputStream anInStream;
        int response;
        URL url = new URL(urlString);
        URLConnection conn = url.openConnection();

        // Check that the connection can be opened
        if (!(conn instanceof HttpURLConnection))
            throw new IOException("Not an HTTP connection");
        try {
            // Open connection
            HttpURLConnection httpConn = (HttpURLConnection) conn;
            httpConn.setAllowUserInteraction(false);
            httpConn.setInstanceFollowRedirects(true);
            httpConn.setRequestMethod("GET");
            try {
                StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
                StrictMode.setThreadPolicy(policy);
                httpConn.connect();
            } catch (Exception ec) {
                ec.printStackTrace();
            }
            response = httpConn.getResponseCode();

            // Check that connection is Ok
            if (response == HttpURLConnection.HTTP_OK) {
                // Connection is Ok so open a reader
                anInStream = httpConn.getInputStream();
                InputStreamReader in = new InputStreamReader(anInStream);
                BufferedReader bin = new BufferedReader(in);

                // Read in the data from the RSS stream
                String line;
                // Keep reading until there is no more data
                while (((line = bin.readLine())) != null) {
                    result = result + line;
                }
            }
        } catch (Exception ex) {
            throw new IOException("Error connecting");
        }
        // Return result as a string for further processing
        return result;

    }
}
