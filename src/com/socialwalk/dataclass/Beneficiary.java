package com.socialwalk.dataclass;

import java.util.Date;

public class Beneficiary
{
	public String Sequence, Name, GroupSequence;
	public int AreaCode, AreaSubCode, Gender, Age, Participants, Type;
	public String AreaName, AreaSubName;
	public String ProfileUrl, DescriptionUrl, ReviewsUrl, Description;
	public double TargetMoney, CurrentMoney;
	public Date StartDate, EndDate, BenefitDate;
	public boolean InProgress;
	
	public Beneficiary()
	{

	}

}
