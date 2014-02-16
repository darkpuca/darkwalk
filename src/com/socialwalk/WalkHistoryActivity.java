	package com.socialwalk;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Vector;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.socialwalk.MyXmlParser.SWResponse;
import com.socialwalk.dataclass.DBAdapter;
import com.socialwalk.dataclass.WalkHistory;
import com.socialwalk.request.ServerRequestManager;

public class WalkHistoryActivity extends Activity
implements Response.Listener<String>, Response.ErrorListener
{
	private static final String TAG = "SW-HISTORY";

	private ServerRequestManager server;
	private int reqType;
	private static final int REQUEST_WALK_RESULT = 100;

	private WalkHistory uploadHistory;

	private ListView m_historyList;
	private HistoryAdapter m_historyAdapter;
	ProgressDialog progDlg;
	
	private DBAdapter db;
	
//	private final Comparator<WalkHistory> myComparator = new Comparator<WalkHistory>(){
//		@Override
//		public int compare(WalkHistory lhs, WalkHistory rhs)
//		{
//			return rhs.StartTime.compareTo(lhs.StartTime);
//		}
//	};


	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_walk_history);
		
		this.server = new ServerRequestManager();
		
		this.db = new DBAdapter(this);

		m_historyList = (ListView)findViewById(R.id.historyList);

		// 히스토리 항목 선택하면 히스토리 상세 전환.
		m_historyList.setOnItemClickListener(new OnItemClickListener()
		{
			@Override
			public void onItemClick(AdapterView<?> adpater, View view, int position, long id)
			{
				WalkHistory history = m_historyAdapter.getItem(position);
				
				Intent i = new Intent(getBaseContext(), WalkDetailActivity.class);
				i.putExtra(Globals.EXTRA_KEY_FILENAME, history.FileName);
				startActivity(i);
			}
		});
		
		// 리스트 항목 팝업 등록.
		registerForContextMenu(m_historyList);
		
		// 랭킹보기 화면 전환.
		RelativeLayout rankingLayout = (RelativeLayout)findViewById(R.id.rankButtonLayout);
		rankingLayout.setOnClickListener(new OnClickListener()
		{			
			@Override
			public void onClick(View v)
			{
				Intent i = new Intent(getBaseContext(), RankingActivity.class);
				startActivity(i);
			}
		});
		
		// prepare progress dialog 
		progDlg = new ProgressDialog(WalkHistoryActivity.this);
		progDlg.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		progDlg.setCancelable(false);

		// 히스토리 항목 준비중 progress 표시.
		Handler handler = new Handler();
		handler.post(new Runnable()
		{			
			@Override
			public void run()
			{
				progDlg.setMessage(getResources().getString(R.string.MSG_LOADING));
				progDlg.show();
			}
		});
		
		handler.postDelayed(PrepareDataRunnable, 1000);

	}
	
	private Runnable PrepareDataRunnable = new Runnable()
	{		
		@Override
		public void run()
		{
			if (BuildHistoryAdapter())
				m_historyList.setAdapter(m_historyAdapter);
			
			progDlg.dismiss();
		}
	};

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.walk_history, menu);
        return true;
    }

    

	@Override
	public boolean onContextItemSelected(MenuItem item)
	{
		AdapterContextMenuInfo menuInfo = (AdapterContextMenuInfo)item.getMenuInfo();
		WalkHistory history = m_historyAdapter.getItem(menuInfo.position);
		if (null == history) return false;

		switch (item.getItemId())
		{
		case R.id.action_upload:
			if (!progDlg.isShowing()) progDlg.show();

			reqType = REQUEST_WALK_RESULT;
			WalkHistory targetHistory = Utils.GetDefaultTool().WalkHistoryFromFile(this, history.FileName);
			this.uploadHistory = history;
			server.WalkResult(this, this, targetHistory);
			return true;
		case R.id.action_detail:
			Intent i = new Intent(getBaseContext(), WalkDetailActivity.class);
			i.putExtra(Globals.EXTRA_KEY_FILENAME, history.FileName);
			startActivity(i);
			return true;
			
		case R.id.action_delete:
			Log.d(TAG, "selected log file " + history.FileName);
			DeleteHistory(history);
			return true;
		}
		return false;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		if (R.id.action_delete_all == item.getItemId())
		{
			AlertDialog.Builder dlg = new AlertDialog.Builder(this);
			dlg.setCancelable(false);
			dlg.setTitle(R.string.TITLE_INFORMATION);
			dlg.setMessage(R.string.MSG_CLEAR_HISTORY_CONFIRM);
			dlg.setPositiveButton(R.string.CONTINUE, new DialogInterface.OnClickListener()
			{	
				@Override
				public void onClick(DialogInterface dialog, int which)
				{
//					Toast.makeText(getBaseContext(), "[DELETE ALL]", Toast.LENGTH_SHORT).show();
					ClearHistories();
				}
			});
			
			dlg.setNegativeButton(R.string.CANCEL, new DialogInterface.OnClickListener()
			{				
				@Override
				public void onClick(DialogInterface dialog, int which)
				{
					dialog.dismiss();
				}
			});
			dlg.show();

		}
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo)
	{
		//super.onCreateContextMenu(menu, v, menuInfo);
		
		AdapterContextMenuInfo info = (AdapterContextMenuInfo)menuInfo;
		WalkHistory history = this.m_historyAdapter.getItem(info.position);
		if (null == history) return;

		getMenuInflater().inflate(R.menu.walk_history_popup, menu);
		
		if (history.IsUploaded)
			menu.getItem(0).setEnabled(false);
	}
	
	private void ClearHistories()
	{
		File dataDir = getApplicationContext().getDir(ServerRequestManager.LoginAccount.Sequence, Context.MODE_PRIVATE);

		// 히스토리 파일 삭제.
		for (WalkHistory history : m_historyAdapter.m_histories)
		{
			File logFile = new File(dataDir.getPath(), history.FileName);
			logFile.delete();
		}
		
		// db항목 삭제.
		db.open();
		db.clearWalkHistoreis();
		db.close();
		
		// 아답타 등록 항목 삭제.
		m_historyAdapter.clear();
	}
	
	private void DeleteHistory(WalkHistory history)
	{
		db.open();
		db.deleteWalkHistory(history.FileName);
		db.close();
		
		File dataDir = getApplicationContext().getDir(ServerRequestManager.LoginAccount.Sequence, Context.MODE_PRIVATE);
		File logFile = new File(dataDir.getPath(), history.FileName);
		logFile.delete();
		
		m_historyAdapter.remove(history);
	}

	private boolean BuildHistoryAdapter()
	{
		Vector<WalkHistory> histories = new Vector<WalkHistory>();
				
//		getHistoriesFromFiles(histories);
		getHistoriesFromDB(histories);
		
		if (0 < histories.size())
		{
			m_historyAdapter = new HistoryAdapter(this, histories);
//			m_historyAdapter.sort(myComparator);
		}
		
		return true;
	}

	private void getHistoriesFromFiles(Vector<WalkHistory> histories)
	{
		Vector<File> files = getLogFiles();

		for (File file : files)
		{
			MyXmlParser parser = new MyXmlParser(file);
			WalkHistory history = parser.GetWalkHistory();
			if (null == history)
			{
				file.delete();
				continue;
			}
			history.FileName = file.getName();
			history.ReCalculate();			
			
			Log.d(TAG, "history log count: " + history.LogItems.size());
			histories.add(history);
		}
	}

	private Vector<File> getLogFiles()
	{
		File dataDir = getApplicationContext().getDir(ServerRequestManager.LoginAccount.Sequence, Context.MODE_PRIVATE);
		File[] dataFiles = dataDir.listFiles();
		
		Vector<File> logFiles = new Vector<File>();
		
		for (File file : dataFiles)
		{
			Log.d(TAG, file.getPath());

			String path = file.getPath();
			String ext =  path.substring(path.lastIndexOf(".") + 1, path.length());
			if (ext.equalsIgnoreCase("log"))
				logFiles.add(file);
		}
		
		return logFiles;
	}
	
	private void getHistoriesFromDB(Vector<WalkHistory> histories)
	{
		if (false == isValidHistoryRecords())
			updateHistoryRecordsFromFiles();
		
		db.open();
		
//		db.clearWalkHistoreis();
		Cursor c = db.getUserWalkHistories();
		
		if (c.moveToFirst())
		{
			do
			{
				WalkHistory history = new WalkHistory();
				history.StartTime.setTime(c.getLong(0));
				history.FileName = c.getString(1);
				history.IsUploaded = (c.getInt(2) == 1);
				history.ValidDistance = c.getFloat(3);
				history.setWeight(c.getInt(4));
				history.setHeartStepDistance(c.getInt(5));
				history.setAdTouchCount(c.getInt(6));
				
				histories.add(history);
				
			} while (c.moveToNext());
		}
		else
		{
			// 파일 정보와 비교하여 db 정보 갱신.
			updateHistoryRecordsFromFiles();
		}
		
		db.close();
	}
	
	private boolean isValidHistoryRecords()
	{
		File dataDir = getApplicationContext().getDir(ServerRequestManager.LoginAccount.Sequence, Context.MODE_PRIVATE);
		File[] dataFiles = dataDir.listFiles();
		
		// 파일이 없을 경우는 정상.
		if (0 == dataFiles.length) return true;
		
		db.open();
		
		Cursor c = db.getUserWalkHistories();
		boolean ret = (c.getCount() == dataFiles.length);
		
		db.close();
		
		return ret;
	}

	private void updateHistoryRecordsFromFiles()
	{
		File dataDir = getApplicationContext().getDir(ServerRequestManager.LoginAccount.Sequence, Context.MODE_PRIVATE);
		File[] dataFiles = dataDir.listFiles();

		if (0 == dataFiles.length) return;
	
		db.open();
		
		for (File file : dataFiles)
		{
			String path = file.getPath();
			String ext =  path.substring(path.lastIndexOf(".") + 1, path.length());
			if (ext.equalsIgnoreCase("log"))
			{
				file.getName();
				Cursor c = db.getUserWalkHistory(file.getName());
				if (null == c || 0 == c.getCount())	// 레코드가 없을 경우에만 추가.
				{
					MyXmlParser parser = new MyXmlParser(file);
					WalkHistory history = parser.GetWalkHistory();
					if (null == history)
					{
						file.delete();
						continue;
					}
					history.FileName = file.getName();
					history.ReCalculate();			

					db.insertWalkHistory(history);
				}				
			}
		}
		
		db.close();
	}



	private static class HistoryViewContainer
	{
		public TextView logDate;
		public TextView distance;
		public TextView hearts, unregisted;
	}

	public class HistoryAdapter extends ArrayAdapter<WalkHistory>
	{
		private final Activity m_context;
		private final Vector<WalkHistory> m_histories;
		
		public HistoryAdapter(Activity context, Vector<WalkHistory> histories)
		{
			super(context, R.layout.listitem_walk_history, histories);
			
			this.m_context = context;
			this.m_histories = histories;			
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent)
		{
			HistoryViewContainer viewContainer;
			View rowView = convertView;
			
			if (null == rowView)
			{
				LayoutInflater inflater = m_context.getLayoutInflater();
				rowView = inflater.inflate(R.layout.listitem_walk_history, null, true);

				viewContainer = new HistoryViewContainer();

				viewContainer.logDate = (TextView)rowView.findViewById(R.id.logDate);
				viewContainer.hearts = (TextView)rowView.findViewById(R.id.hearts);
				viewContainer.distance = (TextView)rowView.findViewById(R.id.distance);
				viewContainer.unregisted = (TextView)rowView.findViewById(R.id.unregisted);
				
				rowView.setTag(viewContainer);
			}
			else
			{
				viewContainer = (HistoryViewContainer)rowView.getTag();
			}
			
			WalkHistory history = m_histories.get(position);
			
			DateFormat sdf = new SimpleDateFormat(Globals.DATETIME_FORMAT_FOR_UI, Locale.US);
			String dateString = sdf.format(history.StartTime);
			viewContainer.logDate.setText(dateString);
			viewContainer.hearts.setText(history.RedHeartString() + getResources().getString(R.string.HEART));
			viewContainer.distance.setText(history.ValidDistanceString());
			viewContainer.unregisted.setVisibility(history.IsUploaded ? View.INVISIBLE : View.VISIBLE);
			
			return rowView;
		}
	}

	public void onErrorResponse(VolleyError e)
	{
		if (progDlg.isShowing())
			progDlg.dismiss();

		e.printStackTrace();
	}

	@Override
	public void onResponse(String response)
	{
		if (progDlg.isShowing())
			progDlg.dismiss();

		if (0 == response.length()) return;
		
		MyXmlParser parser = new MyXmlParser(response);
		SWResponse result = parser.GetResponse();
		if (null == result) return;
		
		if (REQUEST_WALK_RESULT == this.reqType)
		{
			if (Globals.ERROR_NONE == result.Code)
			{
				if (null == this.uploadHistory) return;
				
				db.open();
				db.updateWalkHistory(this.uploadHistory.FileName, true);
				db.close();
				
				this.uploadHistory.IsUploaded = true;
				
				WalkHistory targetHistory = Utils.GetDefaultTool().WalkHistoryFromFile(this, this.uploadHistory.FileName);
				targetHistory.IsUploaded = true;
				
//				SaveUploadedHistory(targetHistory);
				
				ServerRequestManager.LoginAccount.Hearts.addRedPointByWalk(targetHistory.RedHeartByWalk());
				ServerRequestManager.LoginAccount.Hearts.addRedPointByTouch(targetHistory.RedHeartByTouch());
				ServerRequestManager.LoginAccount.Hearts.addGreenPoint(targetHistory.GreedHeartByTouch());
				
				this.uploadHistory = null;
				
				this.m_historyAdapter.notifyDataSetChanged();
			}
		}
	}

	private void SaveUploadedHistory(WalkHistory history)
	{
		if (null == history) return;
		
		String strXml = history.GetXML();
		try
		{
			File logDir = this.getDir(ServerRequestManager.LoginAccount.Sequence, Context.MODE_PRIVATE);
			File logFile = new File(logDir, history.FileName);

			FileOutputStream fos = new FileOutputStream(logFile);
			OutputStreamWriter osw = new OutputStreamWriter(fos);
			osw.write(strXml);
			osw.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}	
}
