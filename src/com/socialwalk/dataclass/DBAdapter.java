package com.socialwalk.dataclass;

import java.util.Date;

import com.socialwalk.request.ServerRequestManager;

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
	private static final String KEY_START_TIME = "start_time";
	private static final String KEY_FILENAME = "filename";
	private static final String KEY_UPLOADED = "is_uploaded";
	private static final String KEY_DISTANCE = "distance";
	private static final String KEY_WEIGHT = "weight";
	private static final String KEY_HEART_STEP = "heart_step";
	private static final String KEY_AD_TOUCH = "ad_touch";
	private static final String TAG = "DBAdapter";
	
	private static final String DATABASE_NAME = "swDB";
	private static final String DATABASE_TABLE = "walk_history";
	private static final int DATABASE_VERSION = 3;
	
	private static final String DATABASE_CREATE =
			"CREATE TABLE walk_history (user_seq TEXT NOT NULL , "
			+ "start_time INTEGER NOT NULL , "
			+ "filename TEXT NOT NULL , "
			+ "is_uploaded BOOL NOT NULL , "
			+ "distance DOUBLE NOT NULL , "
			+ "weight INTEGER NOT NULL , "
			+ "heart_step INTEGER NOT NULL , "
			+ "ad_touch INTEGER NOT NULL )";
	
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
			db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE);
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
	
	public long insertWalkHistory(WalkHistory history)
	{
		if (null == history) return 0;
		
		if (isExistWalkHistory(history.FileName)) return 0;
		
		ContentValues initialValues = new ContentValues();
		initialValues.put(KEY_USER_SEQ, ServerRequestManager.LoginAccount.Sequence);
		initialValues.put(KEY_START_TIME, history.StartTime.getTime());
		initialValues.put(KEY_FILENAME, history.FileName);
		initialValues.put(KEY_UPLOADED, true == history.IsUploaded ? 1:0);
		initialValues.put(KEY_DISTANCE, history.ValidDistance);
		initialValues.put(KEY_WEIGHT, history.getWeight());
		initialValues.put(KEY_HEART_STEP, history.getHeartStepDistance());
		initialValues.put(KEY_AD_TOUCH, history.getAdTouchCount());
		
		try
		{
			long ret = db.insert(DATABASE_TABLE, null,  initialValues);
			return ret;
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
		
		return 0;
	}
	
	public boolean deleteWalkHistory(String filename)
	{
		String sqlWhere = KEY_USER_SEQ + "=" + ServerRequestManager.LoginAccount.Sequence + " AND "
				+ KEY_FILENAME + "='" + filename + "'";
		
		return db.delete(DATABASE_TABLE, sqlWhere, null) > 0;
	}
	
	public void clearWalkHistoreis()
	{
		String sql = "DELETE FROM " + DATABASE_TABLE
				+ " WHERE " + KEY_USER_SEQ + "=" + ServerRequestManager.LoginAccount.Sequence;
		db.execSQL(sql);
	}
	
	public Cursor getUserWalkHistories()
	{
		String[] columns = new String[] {KEY_START_TIME, KEY_FILENAME, KEY_UPLOADED, KEY_DISTANCE, KEY_WEIGHT, KEY_HEART_STEP, KEY_AD_TOUCH};
		String sqlWhere = KEY_USER_SEQ + "=" + ServerRequestManager.LoginAccount.Sequence;
		
		Cursor c = db.query(DATABASE_TABLE, columns, sqlWhere, null, null, null, KEY_START_TIME + " DESC");
		
		return c;
	}
	
	public Cursor getUserWalkHistory(String filename)
	{
		String[] columns = new String[] {KEY_START_TIME, KEY_FILENAME, KEY_UPLOADED, KEY_DISTANCE, KEY_WEIGHT, KEY_HEART_STEP, KEY_AD_TOUCH};
		String sqlWhere = KEY_USER_SEQ + "=" + ServerRequestManager.LoginAccount.Sequence + " AND "
				+ KEY_FILENAME + "='" + filename + "'";

		try
		{
			Cursor myCursor = db.query(true, DATABASE_TABLE, columns, sqlWhere, null, null, null, null, null);
			if (null != myCursor)
				myCursor.moveToFirst();
			
			return myCursor;		
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
		
		return null;
	}
	
	public boolean isExistWalkHistory(String filename)
	{
		Cursor c = getUserWalkHistory(filename);
		if (null == c) return false;
		
		return (0 < c.getCount()); 
	}
	
	public boolean updateWalkHistory(String filename, boolean uploaded)
	{
		ContentValues args = new ContentValues();
		args.put(KEY_UPLOADED, true == uploaded ? 1:0);
		
		String sqlWhere = KEY_USER_SEQ + "=" + ServerRequestManager.LoginAccount.Sequence + " AND "
				+ KEY_FILENAME + "='" + filename + "'";
		
		return db.update(DATABASE_TABLE, args, sqlWhere, null) > 0; 
	}
}
