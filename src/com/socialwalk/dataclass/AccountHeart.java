package com.socialwalk.dataclass;

public class AccountHeart
{
	private int greenPoint, redPointWalk, redPointTouch, redPointTotal;
	
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
		return redPointWalk + redPointTouch;
	}

	public void setRedPoint(int walkPoint, int touchPoint)
	{
		this.redPointWalk = walkPoint;
		this.redPointTouch = touchPoint;
	}
	
	public int getRedPointByWalk()
	{
		return this.redPointWalk;
	}
	
	public int getRedPointByTouch()
	{
		return this.redPointTouch;
	}
	
	public void setRedPointByWalk(int point)
	{
		this.redPointWalk = point;
	}
	
	public void setRedPointByTouch(int point)
	{
		this.redPointTouch = point;
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
	
	public void addRedPointByWalk(int point)
	{
		this.redPointWalk += point;
		this.redPointTotal += point;
	}

	public void addRedPointByTouch(int point)
	{
		this.redPointTouch += point;
		this.redPointTotal += point;
	}

	public void Copy(AccountHeart hearts)
	{
		this.greenPoint = hearts.getGreenPoint();
		this.redPointWalk = hearts.getRedPointByWalk();
		this.redPointTouch = hearts.getRedPointByTouch();
		this.redPointTotal = hearts.getRedPointTotal();
	}
}

