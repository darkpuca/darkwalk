package com.socialwalk.dataclass;

import java.util.Date;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DBAdapter
{
	private static final String KEY_USER_SEQ = "user_seq";
	private static final String KEY_DATE = "_date";
	private static final String KEY_FILENAME = "filename";
	private static final String KEY_REGISTED = "registed";
	private static final String KEY_DISTANCE = "distance";
	private static final String KEY_HEARTS = "hearts";
	private static final String TAG = "DBAdapter";
	
	private static final String DATABASE_NAME = "swDB";
	private static final String DATABASE_TABLE = "walk_history";
	private static final int DATABASE_VERSION = 1;
	
	private static final String DATABASE_CREATE = 
			"CREATE  TABLE walk_history (user_seq TEXT PRIMARY KEY NOT NULL UNIQUE, "
			+ "_date INTEGER NOT NULL, "
			+ "filename TEXT NOT NULL, "
			+ "registed BOOL NOT NULL, "
			+ "distance DOUBLE NOT NULL, "
			+ "hearts INTEGER NOT NULL)";
	
	private final Context context;
	
	private DatabaseHelper DBHelper;
	private SQLiteDatabase db;
	
	public DBAdapter(Context ctx)
	{
		this.context = ctx;
		DBHelper = new DatabaseHelper(context);
	}

	
	private static class DatabaseHelper extends SQLiteOpenHelper
	{
		DatabaseHelper(Context context)
		{
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db)
		{
			try
			{
				db.execSQL(DATABASE_CREATE);
			}
			catch (SQLException e)
			{
				e.printStackTrace();
			}
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
		{
			Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
					+ newVersion + ", which will destroy all old data");
			db.execSQL("DROP TABLE IF EXISTS contacts");
			onCreate(db);
		}
	}

	
	public DBAdapter open() throws SQLException
	{
		db = DBHelper.getWritableDatabase();
		return this;
	}
	
	public void close()
	{
		DBHelper.close();
	}
	
	public long insertWalkHistory(String user_seq, Date date, String filename, boolean registed, double distance, int hearts)
	{
		ContentValues initialValues = new ContentValues();
		initialValues.put(KEY_USER_SEQ, user_seq);
		initialValues.put(KEY_DATE, date.getTime());
		initialValues.put(KEY_FILENAME, filename);
		initialValues.put(KEY_REGISTED, registed);
		initialValues.put(KEY_DISTANCE, distance);
		initialValues.put(KEY_HEARTS, hearts);
		
		return db.insert(DATABASE_TABLE, null,  initialValues);
	}
	
	public boolean deleteWalkHistory(String user_seq, Date date)
	{
		String sqlWhere = KEY_USER_SEQ + "=" + user_seq + " AND "
				+ KEY_DATE + "=" + Long.toString(date.getTime());
		
		return db.delete(DATABASE_TABLE, sqlWhere, null) > 0;
	}
	
	public Cursor getAllWalkHistories()
	{
		String[] columns = new String[] {KEY_USER_SEQ, KEY_DATE, KEY_FILENAME, KEY_REGISTED, KEY_DISTANCE, KEY_HEARTS};
		return db.query(DATABASE_TABLE, columns, null, null, null, null, KEY_DATE + " DESC");
	}
	
	public boolean updateWalkHistory(String user_seq, Date date, boolean registed)
	{
		ContentValues args = new ContentValues();
		args.put(KEY_REGISTED, registed);
		
		String sqlWhere = KEY_USER_SEQ + "=" + user_seq + " AND "
				+ KEY_DATE + "=" + Long.toString(date.getTime());
		
		return db.update(DATABASE_TABLE, args, sqlWhere, null) > 0; 
	}
}
