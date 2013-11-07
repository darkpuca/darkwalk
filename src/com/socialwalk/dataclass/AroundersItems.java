package com.socialwalk.dataclass;

import java.util.Date;
import java.util.Vector;
import java.util.concurrent.TimeUnit;

import com.socialwalk.Utils;

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
			if (Utils.GetDefaultTool().IsBillingArounders(item.sequence))
			{
				validItem = item;
				this.currentIndex++;
				break;
			}
		}
		
		if (null == validItem)
			validItem = this.Items.get(0);

		if (this.currentIndex >= this.Items.size())
			this.currentIndex = 0;

		return validItem;
	}

	public class AroundersItem
	{
		public String Company;
		public String Promotion;
		public String BannerURL;
		public int Distance;
		public double Latitude;
		public double Longitude;
		
		private String sequence;
		private String iconURL;
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

		public String getIconURL()
		{
			return iconURL;
		}

		public void setIconURL(String iconURL)
		{
			this.sequence = GetSequenceFromUrl(iconURL);
			this.iconURL = iconURL;
		}

		public String getTargetURL()
		{
			return this.targetURL;
		}

		public void setTargetURL(String targetURL)
		{
//			this.sequence = GetSequenceFromUrl(targetURL);
			this.targetURL = targetURL;
		}

		
		private String GetSequenceFromUrl(String url)
		{
			if (null == url) return null;
			if (0 == url.length()) return null;
			
			String[] strSplit = url.split("/");
			String filename = strSplit[strSplit.length-1];
			
			
			String seq = filename.replace(".jpg", "");
			if (null != seq && 0 != seq.length())
				return seq;
			
			return null;
		}
		
	}
}
