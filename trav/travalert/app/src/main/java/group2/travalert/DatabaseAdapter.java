package group2.travalert;

/**
 * Created by Gigi on 4/17/16.
 */

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseAdapter {

    public static final String DESTINATION_ID = "destination_id";   // column 0
    public static final String DESTINATION_ADDRESS = "destination_address";   // column 0
    public static final String[] DESTINATION_COLS = {DESTINATION_ID, DESTINATION_ADDRESS};
    private static final String DB_NAME = "recentDestinations.db";
    private static final String DESTINATION_TABLE = "destination_table";
    private static int dbVersion = 1;
    private final Context context;
    private SQLiteDatabase db;
    private DestinationDBhelper dbHelper;

    public DatabaseAdapter(Context ctx) {
        context = ctx;
        dbHelper = new DestinationDBhelper(context, DB_NAME, null, dbVersion);

    }

    public void open() throws SQLiteException {
        try {
            db = dbHelper.getWritableDatabase();
            Cursor curse = getAllItems();
            curse.moveToLast();
        } catch (SQLiteException ex) {
            db = dbHelper.getReadableDatabase();
            Cursor curse = getAllItems();
            curse.moveToLast();
        }
    }

    public void close() {
        db.close();
    }

    public void clear() {
        dbHelper.onUpgrade(db, dbVersion, dbVersion + 1);  // change version to dump old data
        dbVersion++;
    }



    // database update methods

    public long insertItem(String destinationEntry) {
        // create a new row of values to insert
        ContentValues cvalues = new ContentValues();
        // assign values for each col
        cvalues.put(DESTINATION_ADDRESS, destinationEntry);

        return db.insertWithOnConflict(DESTINATION_TABLE, null, cvalues, SQLiteDatabase.CONFLICT_REPLACE);
    }

    public boolean removeItem(long lid) {
        return db.delete(DESTINATION_TABLE, "LOG_ID=" + lid, null) > 0;
    }



    // database query methods
    public Cursor getAllItems() {
        return db.query(DESTINATION_TABLE, DESTINATION_COLS, null, null, null, null, null);
    }

    public Cursor getItemCursor(long lid) throws SQLException {
        Cursor result = db.query(true, DESTINATION_TABLE, DESTINATION_COLS, DESTINATION_ID + "=" + lid, null, null, null, null, null);
        if ((result.getCount() == 0) || !result.moveToFirst()) {
            throw new SQLException("No log entry found for row: " + lid);
        }
        return result;
    }

    public String getJobItem(long lid) throws SQLException {
        Cursor cursor = db.query(true, DESTINATION_TABLE, DESTINATION_COLS, DESTINATION_ID+"="+lid, null, null, null, null, null);
        if ((cursor.getCount() == 0) || !cursor.moveToFirst()) {
            throw new SQLException("No destination found for row: " + lid);
        }
        return (cursor.getString(1));
    }


    private static class DestinationDBhelper extends SQLiteOpenHelper {

        // SQL statement to create a new database
        private static final String DB_CREATE = "CREATE TABLE " + DESTINATION_TABLE
                + " (" + DESTINATION_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + DESTINATION_ADDRESS + " TEXT UNIQUE);";

        public DestinationDBhelper(Context context, String name, SQLiteDatabase.CursorFactory fct, int version) {
            super(context, name, fct, version);
        }

        @Override
        public void onCreate(SQLiteDatabase adb) {
            // TODO Auto-generated method stub
            adb.execSQL(DB_CREATE);
        }


        @Override
        public void onUpgrade(SQLiteDatabase adb, int oldVersion, int newVersion) {
            // TODO Auto-generated method stub
            Log.w("LogDB", "upgrading from version " + oldVersion + " to "
                    + newVersion + ", destroying old data");
            // drop old table if it exists, create new one
            // better to migrate existing data into new table
            adb.execSQL("DROP TABLE IF EXISTS " + DESTINATION_TABLE);
            onCreate(adb);


        }

    }

}