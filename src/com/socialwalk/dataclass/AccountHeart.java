package com.socialwalk.dataclass;

public class AccountHeart
{
	private int greenPoint, redPoint, redPointTotal;
	
	public AccountHeart()
	{

	}

	public int getGreenPoint()
	{
		return greenPoint;
	}

	public void setGreenPoint(int greenPoint)
	{
		this.greenPoint = greenPoint;
	}

	public int getRedPoint()
	{
		return redPoint;
	}

	public void setRedPoint(int redPoint)
	{
		this.redPoint = redPoint;
	}

	public int getRedPointTotal()
	{
		return redPointTotal;
	}

	public void setRedPointTotal(int redPointTotal)
	{
		this.redPointTotal = redPointTotal;
	}

	public void addGreenPoint(int point)
	{
		this.greenPoint += point;
	}
	
	public void addRedPoint(int point)
	{
		this.redPoint += point;
		this.redPointTotal += point;
	}
	
	public void Copy(AccountHeart hearts)
	{
		this.greenPoint = hearts.getGreenPoint();
		this.redPoint = hearts.getRedPoint();
		this.redPointTotal = hearts.getRedPointTotal();
	}
}

