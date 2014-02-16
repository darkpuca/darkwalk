package com.socialwalk.dataclass;

import java.util.Vector;

import com.socialwalk.request.ServerRequestManager;


public class WalkHistoryManager
{
	private static Vector<WalkHistory> histories = new Vector<WalkHistory>();

	public WalkHistoryManager()
	{

	}
	
	public static WalkHistory StartNewLog()
	{
		if (null == ServerRequestManager.LoginAccount) return null;
		
		int weight = ServerRequestManager.LoginAccount.Weight;
		WalkHistory history = new WalkHistory(weight);
		histories.add(history);
		return history;
	}
	
	public static WalkHistory LastWalking()
	{
		if (0 == histories.size()) return null;
		
		return histories.get(histories.size()-1);
	}
	
	public static void AddWalking(WalkHistory history)
	{
		if (null == history) return;
		histories.add(history);
	}

}
