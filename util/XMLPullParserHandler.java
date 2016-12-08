package uiviewsxml.myandroidhello.com.earthquakerssfeed.util;

import android.content.Context;
import android.database.SQLException;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import uiviewsxml.myandroidhello.com.earthquakerssfeed.data.RssFeedItem;


/**
 * Created by Ray Cheung on 22/11/2016.
 */

public class XMLPullParserHandler {
    List<RssFeedItem> rssItems;
    private RssFeedItem rssItem;
    private String temp;
    dbMgr mgr;


    public XMLPullParserHandler(Context c) {
        rssItems = new ArrayList<>();
        mgr = new dbMgr(c, "countrys.s3db", null, 1);
    }

    public List<RssFeedItem> getRssItems() {
        return rssItems;
    }


    public List<RssFeedItem> parse(String xml) {
        XmlPullParserFactory factory;
        XmlPullParser parser;
        try {
            factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(true);
            parser = factory.newPullParser();

            parser.setInput(new StringReader(xml));

            int eventType = parser.getEventType();

            rssItem = new RssFeedItem();

            boolean inItem = false;
        //read into the actual xml content
            while (eventType != XmlPullParser.END_DOCUMENT) {
                String tagname = parser.getName();

                switch (eventType) {
                    case XmlPullParser.START_TAG:
                        if (tagname.equalsIgnoreCase("item")) {
                            // create a new instance of rssFeedItem
                            rssItem = new RssFeedItem();
                            inItem = true;
                        }
                        break;

                    case XmlPullParser.TEXT:
                        temp = parser.getText();
                        break;

                    case XmlPullParser.END_TAG:
                        if (inItem) {
                            if (tagname.equalsIgnoreCase("item")) {
                                // add rss object to list
                                rssItems.add(rssItem);
                                try {
                                    mgr.writeDataBase(rssItem);
                                } catch (SQLException s) {
                                    throw s;
                                }
                            } else if (tagname.equalsIgnoreCase("title")) {
                                rssItem.setTitle(temp);
                                //substring the region out of the title
                                int end = temp.length();
                                String titleTemp = temp.substring(8, end);
                                rssItem.setRegion(titleTemp);

                            } else if (tagname.equalsIgnoreCase("magnitude")) {
                                rssItem.setMagnitude(temp.toUpperCase());
                            } else if (tagname.equalsIgnoreCase("time")) {
                                rssItem.setDateTime(temp);
                            } else if (tagname.equalsIgnoreCase("depth")) {
                                rssItem.setDepth(temp.substring(0, temp.indexOf(" ")));
                            } else if (tagname.equalsIgnoreCase("long")) {
                                rssItem.setGeoLong(temp);
                            } else if (tagname.equalsIgnoreCase("lat")) {
                                rssItem.setGeoLat(temp);
                            } else if (tagname.equalsIgnoreCase("status")) {
                                rssItem.setStatus(temp);
                            }
                        }
                        break;

                    default:
                        break;
                }
                eventType = parser.next();
            }

        } catch (XmlPullParserException es) {
            es.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return rssItems;
    }

}
