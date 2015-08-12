package com.apress.gerber.reminders;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.sql.SQLException;

/**
 * Created by shashank.kumar on 8/4/2015.
 */
public class RemindersDBAdapter {

    public static final String COL_ID = "_id";
    public static final String COL_CONTENT = "content";
    public static final String COL_IMPORTANT = "important";

    public static final int INDEX_ID = 0;
    public static final int INDEX_CONTENT = INDEX_ID+1;
    public static final int INDEX_IMPORTANCE = INDEX_ID+2;

    private static final String TAG = "RemindersDBAdapter";

    private static final String DATABASE_NAME  = "dba_reminders";
    private static final String TABLE_NAME = "tbl_reminders";
    private static final int DATABASE_VERSION = 1;

    private final Context mCtx;

    private DatabaseHelper mDBHelper;
    private SQLiteDatabase mDB;

    private static final String DATABASE_CREATE = "CREATE TABLE if not exists "+TABLE_NAME+" ( "+COL_ID+" INTEGER PRIMARY KEY autoincrement, "+COL_CONTENT+
                                                    " TEXT, "+ COL_IMPORTANT+" INTEGER);";

    public RemindersDBAdapter(Context Ctx){
        this.mCtx = Ctx;
    }

    public void open() throws SQLException {
        mDBHelper = new DatabaseHelper(mCtx);
        mDB = mDBHelper.getWritableDatabase();
    }

    public void close(){
        if(mDBHelper!=null)
            mDBHelper.close();
    }

    //CREATE
    public void createReminder(String name, boolean imp){
        ContentValues values = new ContentValues();
        values.put(COL_CONTENT,name);
        values.put(COL_IMPORTANT,imp ? 1:0);
        mDB.insert(TABLE_NAME,null,values);
    }

    public long createReminder(RemindersDAO reminder){
        ContentValues values = new ContentValues();
        values.put(COL_CONTENT,reminder.getmContent());
        values.put(COL_IMPORTANT, reminder.getmImportant());
        return mDB.insert(TABLE_NAME,null,values);
    }

    // READ
    public RemindersDAO fetchReminderById(int id){
        Cursor cursor = mDB.query(TABLE_NAME,new String[]{COL_ID,COL_CONTENT,COL_IMPORTANT},COL_ID
                                    + "=?",new String[]{String.valueOf(id)},null,null,null,null);
        if (cursor!=null)
            cursor.moveToFirst();
        return new RemindersDAO(cursor.getInt(INDEX_ID),cursor.getString(INDEX_CONTENT),cursor.getInt(INDEX_IMPORTANCE));
    }

    public Cursor fetchAllReminders(){
        Cursor mCursor = mDB.query(TABLE_NAME, new String[]{COL_ID,COL_CONTENT,COL_IMPORTANT},null,null,null,null,null);
        if (mCursor!=null)
            mCursor.moveToFirst();
        return mCursor;
    }

    // UPDATE
    public void updateReminder(RemindersDAO reminder) {
        ContentValues values = new ContentValues();
        values.put(COL_CONTENT, reminder.getmContent());
        values.put(COL_IMPORTANT, reminder.getmImportant());
        mDB.update(TABLE_NAME, values,
                COL_ID + "=?", new String[]{String.valueOf(reminder.getmId())});
    }
    //DELETE
    public void deleteReminderById(int id){
        mDB.delete(TABLE_NAME, COL_ID + "=?", new String[]{String.valueOf(id)});
    }
    public void deleteAllReminders() {
        mDB.delete(TABLE_NAME, null, null);
    }
    // DBHELPER INNER CLASS
    private static class DatabaseHelper extends SQLiteOpenHelper {
        DatabaseHelper (Context context){
            super(context,DATABASE_NAME,null,DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            Log.w(TAG,DATABASE_CREATE);
            db.execSQL(DATABASE_CREATE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(TAG,"Upgrading database from version "+ oldVersion+" to "+newVersion+" which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
            onCreate(db);
        }
    }
}
