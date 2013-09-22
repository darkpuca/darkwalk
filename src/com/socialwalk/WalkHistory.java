package com.socialwalk;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.Vector;
import java.util.concurrent.TimeUnit;

import android.annotation.SuppressLint;
import android.location.Location;
import android.location.LocationManager;

public class WalkHistory
{
	public static final int WALK_SPEED_UNIT_MS = 1;
	public static final int WALK_SPEED_UNIT_KMH = 2;
	
	public float TotalDistance, TotalSpeed;
	public Vector<WalkLogItem> LogItems;
	public boolean IsUploaded = false, IsWalking = false;
	public Date StartTime, EndTime;
	public String FileName;
	
	private static final String TAG = "SW-WALK";

	public WalkHistory()
	{
		this.LogItems = new Vector<WalkLogItem>();
		this.TotalDistance = 0;
		this.TotalSpeed = 0;
		this.StartTime = new Date();
		this.IsWalking = true;
	}
	
	public WalkLogItem AddLog(Location location)
	{
		if (0 == location.getLatitude() || 0 == location.getLongitude())
			return null;
		
		WalkLogItem newLog = new WalkLogItem(location);
		
		WalkLogItem prevLog = GetLastValidItem();
		if (null != prevLog)
		{
			if ((prevLog.LogLocation.getLatitude() != newLog.LogLocation.getLatitude()) &&
					(prevLog.LogLocation.getLongitude() != newLog.LogLocation.getLongitude()))
			{
				newLog.DistanceFromPrevious = newLog.LogLocation.distanceTo(prevLog.LogLocation);
				this.TotalDistance += newLog.DistanceFromPrevious;

				long diffInMs = newLog.LogTime.getTime() - prevLog.LogTime.getTime();
				float speed = (float)(newLog.DistanceFromPrevious / diffInMs *1000L);

				newLog.CurrentSpeed = speed;
//				if (newLog.CurrentSpeed > 20)
//					newLog.IsValid = false;
			}
			else
			{
				return null;
			}
		}
		else
		{
			newLog.DistanceFromPrevious = 0;
			newLog.CurrentSpeed = 0;
		}
		
		this.LogItems.add(newLog);
		
		return newLog;
	}
	
	public void Finish()
	{
		this.EndTime = new Date();
		this.IsWalking = false;
	}
	
	public WalkLogItem GetLastValidItem()
	{
		if (0 == this.LogItems.size())
			return null;
		
		for (int i = this.LogItems.size()-1; i >= 0; i--)
		{
			WalkLogItem item = this.LogItems.get(i);
			if (item.IsValid)
				return item;
			else
				continue;
		}
		
		return null;
	}
	
	public long TotalWalkingSeconds(Date toDate)
	{
		if (this.StartTime.getTime() > toDate.getTime()) return 0;
		
		long diffInMs = toDate.getTime() - this.StartTime.getTime();
		long diffInSec = TimeUnit.MILLISECONDS.toSeconds(diffInMs);
		
		return diffInSec;
	}
	
	public String TotalDistanceString()
	{
		String strRet = "";
		
		if (this.TotalDistance > 1000)
			strRet = String.format(Locale.US, "%.2f km", this.TotalDistance / 1000);
		else
			strRet = String.format(Locale.US,"%.2f m", this.TotalDistance);
		
		return strRet;
	}
	
	public long TotalWalkingSecondsFromNow()
	{
		Date now = new Date();
		return this.TotalWalkingSeconds(now);
	}

	public String TotalWalkingTimeString(Date toDate)
	{
		long totalSeconds = this.TotalWalkingSeconds(toDate);
		
		SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss", Locale.US);
		formatter.setTimeZone(TimeZone.getTimeZone("GMT"));
		String timeString = formatter.format(new Date(totalSeconds * 1000L));
		
		return timeString;		
	}
	
	public String TotalWalkingTimeString()
	{
		if (1 >= this.LogItems.size()) return "00:00:00";
		WalkLogItem lastLog = this.LogItems.lastElement();
		return this.TotalWalkingTimeString(lastLog.LogTime);
	}
	
	public String TotalWalkingTimeStringFromNow()
	{
		Date now = new Date();
		return this.TotalWalkingTimeString(now);
	}
	
	public String AverageSpeed(Date toDate)
	{
		float speed_mps = this.TotalDistance / (float)this.TotalWalkingSeconds(toDate);
		float speed_kmh = speed_mps * 3.6f;
		String retVal = String.format(Locale.US, "%.2fkm/h", speed_kmh);
		
		return retVal;
	}
	
	public String AverageSpeed()
	{
		if (1 >= this.LogItems.size()) return "0.0km/h";
		WalkLogItem lastLog = this.LogItems.lastElement();
		return this.AverageSpeed(lastLog.LogTime);
	}
	
	public String AverageSpeedFromNow()
	{
		Date now = new Date();
		return this.AverageSpeed(now);
	}
	
	public void ReCalculateDistance()
	{
		this.TotalDistance = 0;
		if (1 >= this.LogItems.size()) return;
		
		WalkLogItem prevLog = this.LogItems.get(0);
		for (int i = 1; i < this.LogItems.size(); i++)
		{
			WalkLogItem log = this.LogItems.get(i);
			this.TotalDistance += log.LogLocation.distanceTo(prevLog.LogLocation);
			prevLog = log;
		}
	}
	
	public String GetXML()
	{
		if (false != this.IsWalking) return "";
		
		String xml = MyXmlWriter.GetWalkingDataXML(this);
		if (0 < xml.length())
			return xml;
		
		return "";
	}

	public class WalkLogItem
	{
		public Location LogLocation;
		public Date LogTime;
		public float DistanceFromPrevious, CurrentSpeed;
		public boolean IsValid = true;
		
		public WalkLogItem()
		{
			this.LogTime= new Date(); 
			this.LogLocation = new Location(LocationManager.GPS_PROVIDER);
		}
		
		public WalkLogItem(Location location)
		{
			this.LogTime= new Date(); 
			this.LogLocation = new Location(location);
		}
	}

	
	
}

