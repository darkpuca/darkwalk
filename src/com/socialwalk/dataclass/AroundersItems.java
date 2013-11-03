package com.socialwalk.dataclass;

import java.util.Date;
import java.util.Vector;
import java.util.concurrent.TimeUnit;

public class AroundersItems
{
	public Vector<AroundersItem> Items;
	private int currentIndex = 0;
	
	public AroundersItems()
	{
		this.Items = new Vector<AroundersItems.AroundersItem>();
	}

	public AroundersItem GetItem()
	{
		if (0 == this.Items.size()) return null;
		
		AroundersItem validItem = null;
		
		for (int i = this.currentIndex; i < this.Items.size(); i++)
		{
			AroundersItem item = this.Items.get(i);
			if (item.IsBillingAvailable())
			{
				validItem = item;
				this.currentIndex++;
				this.currentIndex = this.currentIndex % this.Items.size();
				break;
			}
		}
		
		if (null == validItem)
			validItem = this.Items.get(0);
		
		return validItem;
	}

	public class AroundersItem
	{
		public String Company;
		public String Promotion;
		public String IconURL;
		public String BannerURL;
		public int Distance;
		public double Latitude;
		public double Longitude;
		
		private String sequence;
		private String targetURL;
		private Date firstAccess;
		
		public AroundersItem()
		{
			this.Distance = 0;
			this.Latitude = 0;
			this.Longitude = 0;
		}
		
		public String getSequence()
		{
			return this.sequence;
		}

		public String getTargetURL()
		{
			return this.targetURL;
		}

		public void setTargetURL(String targetURL)
		{
			this.sequence = GetSequenceFromUrl(targetURL);
			this.targetURL = targetURL;
		}

		public void SetAccessStamp()
		{
			if (null == this.firstAccess)
			{
				this.firstAccess = new Date();
			}
			else
			{
				Date now = new Date();			
				long diffInMs = now.getTime() - this.firstAccess.getTime();
				long diffInDays = TimeUnit.MILLISECONDS.toDays(diffInMs);
				if (1 <= diffInDays)
					this.firstAccess = new Date();
			}
		}
		
		public boolean IsBillingAvailable()
		{	
			if (null == this.firstAccess) return true;
			
			Date now = new Date();			
			long diffInMs = now.getTime() - this.firstAccess.getTime();
			long diffInDays = TimeUnit.MILLISECONDS.toDays(diffInMs);
			if (1 <= diffInDays)
				return true;
			
			return false;

		}
		
		private String GetSequenceFromUrl(String url)
		{
			if (null == url) return null;
			if (0 == url.length()) return null;
			
//			String[] strSplit1 = url.split("?");
//			String[] strSplit2 = strSplit1[strSplit1.length-1].split("=");
			String[] strSplit2 = url.split("=");
			
			String seq = strSplit2[strSplit2.length-1];
			if (null != seq && 0 != seq.length())
				return seq;
			
			return null;
		}
		
	}
}
