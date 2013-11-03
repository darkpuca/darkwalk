package com.socialwalk.dataclass;

import java.util.Vector;


public class CommunityMembers
{
	public int TotalCount;
	public Vector<CommunityMemberItem> Items;
	
	public CommunityMembers()
	{
		this.Items = new Vector<CommunityMembers.CommunityMemberItem>();
	}
	
	
	
	public class CommunityMemberItem
	{
		public int Sequence, MemberSequence;
		public String Name, Email, Message;
		public boolean IsAllowed;
	}
}

