package com.socialwalk.dataclass;

import java.util.Vector;

public class Communities
{
	public int TotalCount, PageIndex;
	public Vector<Community> Items;

	public Communities()
	{
		this.TotalCount = 0;
		this.PageIndex = 0;
		this.Items = new Vector<Community>();
	}

}
