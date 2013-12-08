	package com.socialwalk;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Locale;
import java.util.Vector;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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
//	private Vector<File> m_logFiles;
	private HistoryAdapter m_historyAdapter;
	ProgressDialog progDlg;
	
	private final Comparator<WalkHistory> myComparator = new Comparator<WalkHistory>(){
		@Override
		public int compare(WalkHistory lhs, WalkHistory rhs)
		{
			return rhs.EndTime.compareTo(lhs.EndTime);
		}
	};


	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_walk_history);
		
		this.server = new ServerRequestManager();

		m_historyList = (ListView)findViewById(R.id.historyList);

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
		
		registerForContextMenu(m_historyList);
		
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
		
		progDlg = new ProgressDialog(WalkHistoryActivity.this);
		progDlg.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		progDlg.setCancelable(false);

		
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
			this.uploadHistory = history;
			if (!progDlg.isShowing()) progDlg.show();

			reqType = REQUEST_WALK_RESULT;
			server.WalkResult(this, this, this.uploadHistory);
			return true;
		case R.id.action_detail:
			Intent i = new Intent(getBaseContext(), WalkDetailActivity.class);
			i.putExtra(Globals.EXTRA_KEY_FILENAME, history.FileName);
			startActivity(i);
			return true;
			
		case R.id.action_delete:
			Log.d(TAG, "selected log file " + history.FileName);
			File dataDir = getApplicationContext().getDir(ServerRequestManager.LoginAccount.Sequence, Context.MODE_PRIVATE);
			File logFile = new File(dataDir.getPath(), history.FileName);
			logFile.delete();

			m_historyAdapter.remove(m_historyAdapter.getItem(menuInfo.position));
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

		for (WalkHistory history : m_historyAdapter.m_histories)
		{
			File logFile = new File(dataDir.getPath(), history.FileName);
			logFile.delete();
		}
		
		m_historyAdapter.clear();
	}

	private boolean BuildHistoryAdapter()
	{
		Vector<WalkHistory> histories = new Vector<WalkHistory>();
		
		Vector<File> files = GetLogFiles();

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
		
		if (0 < histories.size())
		{
			m_historyAdapter = new HistoryAdapter(this, histories);
			m_historyAdapter.sort(myComparator);
		}
		
		return true;
	}

	private Vector<File> GetLogFiles()
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
			viewContainer.distance.setText(history.TotalDistanceString());
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
				
				this.uploadHistory.IsUploaded = true;
				SaveUploadedHistory();
				
				ServerRequestManager.LoginAccount.Hearts.addRedPointByWalk(this.uploadHistory.RedHeartByWalk());
				ServerRequestManager.LoginAccount.Hearts.addRedPointByTouch(this.uploadHistory.RedHeartByTouch());
				ServerRequestManager.LoginAccount.Hearts.addGreenPoint(this.uploadHistory.GreedHeartByTouch());
				
				this.uploadHistory = null;
				
				this.m_historyAdapter.notifyDataSetChanged();
			}
		}
	}

	private void SaveUploadedHistory()
	{
		if (null == this.uploadHistory) return;
		
		String strXml = this.uploadHistory.GetXML();
		try
		{
			File logDir = this.getDir(ServerRequestManager.LoginAccount.Sequence, Context.MODE_PRIVATE);
			File logFile = new File(logDir, this.uploadHistory.FileName);

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
