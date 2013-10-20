package com.socialwalk.dataclass;

import java.util.Vector;

import com.socialwalk.MainApplication;
import com.socialwalk.request.ServerRequestManager;


public class WalkHistoryManager
{
	private static Vector<WalkHistory> Histories = new Vector<WalkHistory>();

	public WalkHistoryManager()
	{

	}
	
	public static WalkHistory StartNewLog()
	{
		if (null == ServerRequestManager.LoginAccount) return null;
		
		int weight = ServerRequestManager.LoginAccount.Weight;
		WalkHistory history = new WalkHistory(weight);
		Histories.add(history);
		return history;
	}
	
	public static WalkHistory LastWalking()
	{
		if (0 == Histories.size()) return null;
		
		return Histories.get(Histories.size()-1);
	}

}
