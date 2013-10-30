package com.socialwalk.dataclass;

import java.util.Vector;

public class HeartRanks
{
	public Vector<RankItem> PersonalItems, GroupSumItems, GroupAvgItems;
	
	public HeartRanks()
	{
		this.PersonalItems = new Vector<HeartRanks.RankItem>();
		this.GroupSumItems = new Vector<HeartRanks.RankItem>();
		this.GroupAvgItems = new Vector<HeartRanks.RankItem>();
	}
	
	
	public class RankItem
	{
		public int UserPoint, GroupPoint, AveragePoints, Members;
		public String Name, CommunityName, LocalName;
	}

}
