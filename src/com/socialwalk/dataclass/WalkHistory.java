package com.socialwalk.dataclass;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.Vector;
import java.util.concurrent.TimeUnit;

import android.location.Location;
import android.location.LocationManager;

import com.socialwalk.Globals;
import com.socialwalk.MyXmlWriter;
import com.socialwalk.Utils;

public class WalkHistory
{
	public static final int WALK_SPEED_UNIT_MS = 1;
	public static final int WALK_SPEED_UNIT_KMH = 2;
	
	public float TotalDistance, TotalSpeed;
	public float ValidDistance;
	public Vector<WalkLogItem> LogItems;
	public boolean IsUploaded = true;
	public Date StartTime, EndTime;
	public String FileName;
	private int AdTouchCount;
	private int weight;
	private int heartStepDistance;
	
	private static final float METs_WALK = 3.5f;
	private static final float METs_RATIO_FOR_MINUTES = 0.0175f;
	private static final float METs_RATIO_FOR_SECONDS = METs_RATIO_FOR_MINUTES / 60.0f;
	private static final int RED_HEART_BY_DISTANCE = 20;

	public WalkHistory()
	{
		this.LogItems = new Vector<WalkLogItem>();
		this.TotalDistance = 0;
		this.ValidDistance = 0;
		this.TotalSpeed = 0;
		this.StartTime = new Date();
		this.weight = Globals.DEFAULT_WEIGHT;
		this.heartStepDistance = RED_HEART_BY_DISTANCE;
		this.AdTouchCount = 0;
	}
	
	public WalkHistory(int weight)
	{
		this.LogItems = new Vector<WalkLogItem>();
		this.TotalDistance = 0;
		this.ValidDistance = 0;
		this.TotalSpeed = 0;
		this.StartTime = new Date();
		this.weight = weight; 
		this.heartStepDistance = RED_HEART_BY_DISTANCE;
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
					(prevLog.LogLocation.getLongitude() != newLog.LogLocation.getLongitude()) &&
					(prevLog.LogLocation.getAltitude() != newLog.LogLocation.getAltitude()))
			{
				newLog.DistanceFromPrevious = newLog.LogLocation.distanceTo(prevLog.LogLocation);

				long diffInMs = newLog.LogTime.getTime() - prevLog.LogTime.getTime();
				float mpsSpeed = (float)(newLog.DistanceFromPrevious / diffInMs *1000L);

				// km/h로 속도 변환
				newLog.CurrentSpeed = mpsSpeed * 3.6f;
				
				// 걷기 속도 제한
				if (newLog.CurrentSpeed > 10)
				{
					newLog.IsValid = false;
					this.TotalDistance += newLog.DistanceFromPrevious;
				}
				else
				{
					newLog.IsValid = true;
					this.TotalDistance += newLog.DistanceFromPrevious;
					this.ValidDistance += newLog.DistanceFromPrevious;
				}
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
	}
	
	public WalkLogItem GetLastValidItem()
	{
		if (0 == this.LogItems.size())
			return null;
		
		for (int i = this.LogItems.size()-1; i >= 0; i--)
		{
			WalkLogItem item = this.LogItems.get(i);
			return item;
//			if (item.IsValid)
//				return item;
//			else
//				continue;
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
//		String strRet = "";
//		if (this.TotalDistance > 1000)
//			strRet = String.format(Locale.US, "%.2f km", this.TotalDistance / 1000);
//		else
//			strRet = String.format(Locale.US,"%.2f m", this.TotalDistance);
		
		String strRet = String.format(Locale.US,  "%.2f km", this.TotalDistance / 1000);
		
		return strRet;
	}

	public String ValidDistanceString()
	{
		String strRet = String.format(Locale.US,  "%.2f km", this.ValidDistance / 1000);
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
		String retVal = String.format(Locale.US, "%.2f km/h", speed_kmh);
		
		return retVal;
	}
	
	public String AverageSpeed()
	{
		if (1 >= this.LogItems.size()) return "0.0 km/h";
		WalkLogItem lastLog = this.LogItems.lastElement();
		return this.AverageSpeed(lastLog.LogTime);
	}
	
	public String AverageSpeedFromNow()
	{
		Date now = new Date();
		return this.AverageSpeed(now);
	}
	
	public void ReCalculate()
	{
		if (1 >= this.LogItems.size())
		{
			// 로그 기록이 없을 경우도 등록할 필요 없음.
			this.IsUploaded = true;
			return;
		}
		
		this.TotalDistance = 0;
		this.ValidDistance = 0;

		WalkLogItem prevLog = this.LogItems.get(0);
		for (int i = 1; i < this.LogItems.size(); i++)
		{
			WalkLogItem log = this.LogItems.get(i);
			
			float distance = log.LogLocation.distanceTo(prevLog.LogLocation);
			if (false == log.IsValid)
			{
				this.TotalDistance += distance;
			}
			else
			{
				this.TotalDistance += distance;
				this.ValidDistance += distance;
			}
			prevLog = log;
		}

		// 저장된 값이 없을 경우 기본값 설정.
		if (0 == this.weight)
			this.weight = Globals.DEFAULT_WEIGHT;
		
		if (0 == this.heartStepDistance)
			this.heartStepDistance = RED_HEART_BY_DISTANCE;
		
		// 하트가 0이면 등록할 필요 없으므로 등록됨으로 처리.
		if (0 == this.RedHearts())
			this.IsUploaded = true;
	}
	
	private long WalkingCalories(Date toDate)
	{
		long seconds = TotalWalkingSeconds(toDate);
		long carories = (long)(METs_WALK * this.weight * seconds * METs_RATIO_FOR_SECONDS);

		return carories;
	}
	
	public String WalkingCaloriesString(Date toDate)
	{
		long calories = WalkingCalories(toDate);
        String strCal = Utils.GetDefaultTool().DecimalNumberString(calories);
        return strCal;
	}

	public String WalkingCaloriesStringFromNow()
	{
		Date now = new Date();
		return WalkingCaloriesString(now);
	}
	
	public String TotalCalories()
	{
		return WalkingCaloriesString(this.EndTime);
	}
	
	public int RedHeartByWalk()
	{
		int hearts = (int)(this.ValidDistance / (float)this.heartStepDistance);
		return hearts;
	}
	
	public int RedHeartByTouch()
	{
		return this.AdTouchCount * Globals.AD_AROUNDERS_RED;
	}
	
	public int GreedHeartByTouch()
	{
		return this.AdTouchCount * Globals.AD_AROUNDERS_GREEN;
	}
	
	public long RedHearts()
	{
		return RedHeartByWalk() + RedHeartByTouch();
	}
	
	public String RedHeartString()
	{
		long hearts = RedHearts();
		return Utils.GetDefaultTool().DecimalNumberString(hearts);
	}
	
	public String RedHeartStringByWalk()
	{
		int hearts = RedHeartByWalk();
		return Utils.GetDefaultTool().DecimalNumberString(hearts);
	}

	public String RedHeartStringByTouch()
	{
		int hearts = RedHeartByTouch();
		return Utils.GetDefaultTool().DecimalNumberString(hearts);
	}

	public int getWeight()
	{
		return this.weight;
	}

	public void setWeight(int weight) 
	{
		this.weight = weight;
	}
	
	public int getHeartStepDistance() {
		return heartStepDistance;
	}

	public void setHeartStepDistance(int distance) {
		this.heartStepDistance = distance;
	}
	
	public int getAdTouchCount() {
		return AdTouchCount;
	}

	public void setAdTouchCount(int adTouchCount) {
		AdTouchCount = adTouchCount;
	}

	public void IncreaseAdTouch()
	{
		this.AdTouchCount++;
	}

	public String GetXML()
	{
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

