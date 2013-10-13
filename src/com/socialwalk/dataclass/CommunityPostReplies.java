package com.socialwalk.dataclass;

import java.util.Date;
import java.util.Vector;

public class CommunityPostReplies
{
	public int TotalCount;
	public Vector<CommunityPostReply> Items;

	public CommunityPostReplies()
	{
		this.TotalCount = 0;
		this.Items = new Vector<CommunityPostReply>();
	}
	
	
	public class CommunityPostReply
	{
		public int Sequence;
		public String UserSequence, Writer, Contents;
		public Date RegDate;
	}
}
