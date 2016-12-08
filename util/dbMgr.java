package uiviewsxml.myandroidhello.com.earthquakerssfeed.util;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Locale;

import uiviewsxml.myandroidhello.com.earthquakerssfeed.data.RssFeedItem;

/**
 * Created by Ray Cheung on 28/11/2016.
 */

public class dbMgr extends SQLiteOpenHelper {

    private static final int DB_VER = 1;
    private static final String DB_PATH = "/data/data/uiviewsxml.myandroidhello.com.earthquakerssfeed/databases/";
    private static final String DB_NAME = "countrys.s3db";
    private static final String TBL_FLAG = "flags";
    private static final String TBL_DATA = "datas";
    private static final String TBL_RAW = "raw";
    private SQLiteDatabase myDataBase;

    public static final String COL_COUNTRY = "country";
    public static final String COL_QID = "qId";
    public static final String COL_MAGNITUDE = "magnitude";
    public static final String COL_REGION = "region";
    public static final String COL_DATETIME = "dateTime";
    public static final String COL_DEPTH = "depth";
    public static final String COL_LAT = "lat";
    public static final String COL_LONG = "long";
    public static final String COL_STATUS = "status";
    public static final String COL_RID = "rID";
    public static final String COL_RSS = "rss";


    private final Context appContext;

    public dbMgr(Context context, String name,
                 SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
        this.appContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        //creating new table on the pre-populated db
        String CREATE_DATA_TABLE = "CREATE TABLE IF NOT EXISTS " +
                TBL_DATA + "("
                + COL_QID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,"
                + COL_MAGNITUDE
                + " TEXT," + COL_REGION
                + " TEXT," + COL_DATETIME
                + " TEXT," + COL_DEPTH
                + " TEXT," + COL_LAT
                + " TEXT," + COL_LONG
                + " TEXT," + COL_STATUS
                + " TEXT" + ")";
        db.execSQL(CREATE_DATA_TABLE);

        String CREATE_RAW_TABLE = "CREATE TABLE IF NOT EXISTS " +
                TBL_RAW + "("
                + COL_RID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,"
                + COL_RSS
                + " TEXT" + ")";
        db.execSQL(CREATE_RAW_TABLE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (newVersion > oldVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + TBL_FLAG);
            onCreate(db);
        }
    }

    // ================================================================================
    // Creates a empty database on the system and rewrites it with your own database.
    // ================================================================================
    public void dbCreate() throws IOException {

        boolean dbExist = dbCheck();

        if (!dbExist) {
            //a db will be created in the system folder
            this.getReadableDatabase();

            try {

                copyDBFromAssets();

            } catch (IOException e) {

                throw new Error("Error copying database");

            }
        }

    }

    // ============================================================================================
    // Check if the database already exist to avoid re-copying the file each time you open the application.
    // @return true if it exists, false if it doesn't
    // ============================================================================================
    private boolean dbCheck() {

        SQLiteDatabase db = null;

        try {
            String dbPath = DB_PATH + DB_NAME;
            db = SQLiteDatabase.openDatabase(dbPath, null, SQLiteDatabase.OPEN_READONLY);
            db.setLocale(Locale.getDefault());
            db.setVersion(1);

        } catch (SQLiteException e) {

            Log.e("SQLHelper", "Database not Found!");

        }

        if (db != null) {

            db.close();

        }

        return db != null;
    }

    // ============================================================================================
    // Copying db from asset to system folder, with bytestream
    // ============================================================================================
    private void copyDBFromAssets() throws IOException {

        InputStream dbInput;
        OutputStream dbOutput;
        String dbFileName = DB_PATH + DB_NAME;

        try {

            dbInput = appContext.getAssets().open(DB_NAME);
            dbOutput = new FileOutputStream(dbFileName);
            //transfer bytes from the dbInput to the dbOutput
            byte[] buffer = new byte[1024];
            int length;
            while ((length = dbInput.read(buffer)) > 0) {
                dbOutput.write(buffer, 0, length);
            }

            //Close the streams
            dbOutput.flush();
            dbOutput.close();
            dbInput.close();
        } catch (IOException e) {
            throw new Error("Problems copying DB!");
        }
    }

    public void openDataBase() throws SQLException {

        //Open Database
        String myPath = DB_PATH + DB_NAME;
        myDataBase = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY);
    }

    public void writeDataBase(RssFeedItem fItem) throws SQLException {

        //Write to database
        ContentValues values = new ContentValues();
        values.put(COL_MAGNITUDE, fItem.getMagnitude());
        values.put(COL_REGION, fItem.getRegion());
        values.put(COL_DATETIME, fItem.getDateTime());
        values.put(COL_DEPTH, fItem.getDepth());
        values.put(COL_LAT, fItem.getGeoLat());
        values.put(COL_LONG, fItem.getGeoLong());
        values.put(COL_STATUS, fItem.getStatus());

        SQLiteDatabase db = this.getWritableDatabase();

        db.insert(TBL_DATA, null, values);
        db.close();
    }

    @Override
    public synchronized void close() {

        if (myDataBase != null)
            myDataBase.close();

        super.close();

    }

    /*public int checkTable() {
        SQLiteDatabase db = this.getReadableDatabase();
        int cou = 0;
        Cursor cursor = db.rawQuery("select DISTINCT datas from countrys where data = '" + TBL_DATA + "'", null);
        if (cursor != null) {
            if (cursor.getCount() > 0) {
                cursor.close();
            }
            cursor.close();
        }
        return cou;
    }*/


    public String findCountry(String cCountry) {
        String query = "Select * FROM " + TBL_FLAG + " WHERE " + COL_COUNTRY.toLowerCase() + " LIKE  \"%" + cCountry + "%\";";

        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(query, null);
        Boolean rowExists;

        rowExists = cursor.moveToFirst();
        String cCode = cursor.getString(2);
        cursor.close();

        db.close();
        return cCode.toLowerCase();
    }

}