	package com.socialwalk;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Vector;

import com.socialwalk.dataclass.WalkHistory;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
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

public class WalkHistoryActivity extends Activity
{
	private static final String TAG = "SW-HISTORY";
	private ListView m_historyList;
	private Vector<File> m_logFiles;
	private HistoryAdapter m_historyAdapter;
	ProgressDialog m_progDlg;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_walk_history);
		
		
		m_historyList = (ListView)findViewById(R.id.historyList);

		m_historyList.setOnItemClickListener(new OnItemClickListener()
		{
			@Override
			public void onItemClick(AdapterView<?> adpater, View view, int position, long id)
			{
				File logFile = m_logFiles.get(position);
//				Toast.makeText(getBaseContext(), logFile.getName(), Toast.LENGTH_SHORT).show();
				
				Intent i = new Intent(getBaseContext(), WalkDetailActivity.class);
				i.putExtra(Globals.EXTRA_KEY_FILENAME, logFile.getName());
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
		
		m_progDlg = new ProgressDialog(WalkHistoryActivity.this);
		m_progDlg.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		m_progDlg.setCancelable(false);

		
		Handler handler = new Handler();
		handler.post(new Runnable()
		{			
			@Override
			public void run()
			{
				m_progDlg.setMessage(getResources().getString(R.string.MSG_LOADING));
				m_progDlg.show();
			}
		});
		
		handler.postDelayed(PrepareDataRunnable, 1000);

	}
	
	private Runnable PrepareDataRunnable = new Runnable()
	{		
		@Override
		public void run()
		{
			m_logFiles = GetLogFiles();

			if (BuildHistoryAdapter(m_logFiles))
				m_historyList.setAdapter(m_historyAdapter);
			
			m_progDlg.dismiss();
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
		File logFile = m_logFiles.get(menuInfo.position);

		switch (item.getItemId())
		{
		case R.id.action_detail:
			Intent i = new Intent(getBaseContext(), WalkDetailActivity.class);
			i.putExtra(Globals.EXTRA_KEY_FILENAME, logFile.getName());
			startActivity(i);
			return true;
			
		case R.id.action_delete:
			Log.d(TAG, "selected log file " + logFile.getPath());
			m_historyAdapter.remove(m_historyAdapter.getItem(menuInfo.position));
			logFile.delete();
			m_logFiles.remove(logFile);
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
		super.onCreateContextMenu(menu, v, menuInfo);
		getMenuInflater().inflate(R.menu.walk_history_popup, menu);
	}
	
	private void ClearHistories()
	{
		for (File file : m_logFiles)
			file.delete();
	}

	private boolean BuildHistoryAdapter(Vector<File> files)
	{
		Vector<WalkHistory> histories = new Vector<WalkHistory>();
		for (File file : files)
		{
			MyXmlParser parser = new MyXmlParser(file);
			WalkHistory history = parser.GetWalkHistory();
			history.FileName = file.getName();
			
			Log.d(TAG, "history log count: " + history.LogItems.size());
			histories.add(history);
		}
		
		if (0 < histories.size())
			m_historyAdapter = new HistoryAdapter(this, histories);
		
		return true;
	}

	private Vector<File> GetLogFiles()
	{
		File dataDir = getApplicationContext().getFilesDir();
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
		public TextView hearts;
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
				
				rowView.setTag(viewContainer);
			}
			else
			{
				viewContainer = (HistoryViewContainer)rowView.getTag();
			}
			
			WalkHistory history = m_histories.get(position);
			
			DateFormat sdf = new SimpleDateFormat("yyyy. MM.dd HH:mm", Locale.US);
			String dateString = sdf.format(history.StartTime);
			viewContainer.logDate.setText(dateString);
			viewContainer.hearts.setText(history.RedHeartString() + getResources().getString(R.string.HEART));
			viewContainer.distance.setText(history.TotalDistanceString());
			
			return rowView;
		}
		
	}
}
