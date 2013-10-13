package com.socialwalk.dataclass;

import java.util.Date;

public class AccountData
{
	public String UserId, Sequence, Password, Name;
	public int UserType, AreaCode, AreaSubCode, OrganizationCode, OrganizationSubCode;
	public String OrganizationKey, RecommendedUserId;
	public int Gender, Weight, CommunitySeq;
	public Date Birthday, RegDate, ModifyDate;
	public boolean Verified;
	public AccountHeart Hearts;
	
	public AccountData()
	{
		this.Hearts = new AccountHeart();
	}
}
