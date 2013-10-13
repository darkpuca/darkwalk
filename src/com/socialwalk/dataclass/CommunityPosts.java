package com.socialwalk.dataclass;

import java.util.Date;
import java.util.Vector;

public class CommunityPosts
{
	public int TotalCount;
	public Vector<CommunityPostItem> Items;
	
	public CommunityPosts()
	{
		this.Items = new Vector<CommunityPostItem>();
	}
	
	
	public class CommunityPostItem
	{
		public int Sequence;
		public String UserSequence, Writer, Contents, ImageUrl;
		public int ViewCount, ReplyCount;
		public Date RegDate, ModifyDate;
		public boolean Verified;
		
		public CommunityPostItem()
		{
			this.Sequence = 0;
			this.ViewCount = 0;
			this.ReplyCount = 0;
			this.Verified = false;
		}
	}
	
	

}



