package com.socialwalk.dataclass;

import java.util.Date;
import java.util.Vector;
import java.util.concurrent.TimeUnit;

public class NeoClickItems
{
	public Vector<NeoClickItem> Items;
	private int currentIndex;

	public NeoClickItems()
	{
		this.Items = new Vector<NeoClickItems.NeoClickItem>();
		currentIndex = 0;
	}
	
	public void SetItems(Vector<NeoClickItem> items)
	{
		this.Items.clear();
		this.Items.addAll(items);
	}
	
	public NeoClickItem GetNextItem()
	{
		NeoClickItem item = Items.get(this.currentIndex++);
		this.currentIndex = this.currentIndex % this.Items.size();
		
		return item;
	}

	
	
	public class NeoClickItem
	{
		public String Sequence;
		public String TargetUrl;
		public String ThumbnailUrl;		
		private Date firstAccess;
		private int accessCount;
		
		private static final int accessLimit = 3;
		
		public NeoClickItem()
		{
			accessCount = 0;
		}
		
		public void SetAccessStamp()
		{
			if (0 == accessCount)
			{
				firstAccess = new Date();
			}
			else
			{
				Date now = new Date();			
				long diffInMs = now.getTime() - this.firstAccess.getTime();
				long diffInHour = TimeUnit.MILLISECONDS.toHours(diffInMs);
				if (1 <=  diffInHour)
				{
					accessCount = 0;
					firstAccess = new Date();
				}
			}
			accessCount++;
		}

		public boolean IsBillingAvailable()
		{
			if (accessLimit >= accessCount) return true;
			
			Date now = new Date();			
			long diffInMs = now.getTime() - this.firstAccess.getTime();
			long diffInHour = TimeUnit.MILLISECONDS.toHours(diffInMs);
			if (1 > diffInHour)
				return (accessLimit >= accessCount);
			else if (1 <= diffInHour)
				return true;
			
			return false;
		}

	}


}
