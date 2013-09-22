package com.socialwalk;

import java.util.Vector;

public class WalkHistoryManager
{
	private static Vector<WalkHistory> Histories = new Vector<WalkHistory>();

	public WalkHistoryManager()
	{

	}
	
	public static WalkHistory StartNewLog()
	{
		WalkHistory history = new WalkHistory();
		Histories.add(history);
		return history;
	}
	
	public static WalkHistory LastWalking()
	{
		if (0 == Histories.size()) return null;
		
		return Histories.get(Histories.size()-1);
	}

}
