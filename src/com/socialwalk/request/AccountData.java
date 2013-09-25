package com.socialwalk.request;

import java.util.Date;

public class AccountData
{
	private String userId, userSequence, userPassword;
	
	private String name;
	private String area;
	private int areaCode;
	private String organization;
	private int organizationCode;
	private int gender;
	private Date birthday;
	private int weight;
	private int communityId;
	private String community;
	private int communityMasterSequence;
	private boolean verified;
	private int greenPoint;
	private int redPoint, redPointTotal;
	private Date regDate, modDate;
	
	public AccountData()
	{
		
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getArea() {
		return area;
	}

	public void setArea(String area) {
		this.area = area;
	}

	public int getAreaCode() {
		return areaCode;
	}

	public void setAreaCode(int areaCode) {
		this.areaCode = areaCode;
	}

	public String getOrganization() {
		return organization;
	}

	public void setOrganization(String organization) {
		this.organization = organization;
	}

	public int getOrganizationCode() {
		return organizationCode;
	}

	public void setOrganizationCode(int organizationCode) {
		this.organizationCode = organizationCode;
	}

	public int getGender() {
		return gender;
	}

	public void setGender(int gender) {
		this.gender = gender;
	}

	public Date getBirthday() {
		return birthday;
	}

	public void setBirthday(Date birthday) {
		this.birthday = birthday;
	}

	public int getWeight() {
		return weight;
	}

	public void setWeight(int weight) {
		this.weight = weight;
	}

	public int getCommunityId() {
		return communityId;
	}

	public void setCommunityId(int communityId) {
		this.communityId = communityId;
	}

	public String getCommunity() {
		return community;
	}

	public void setCommunity(String community) {
		this.community = community;
	}

	public int getCommunityMasterSequence() {
		return communityMasterSequence;
	}

	public void setCommunityMasterSequence(int communityMasterSequence) {
		this.communityMasterSequence = communityMasterSequence;
	}

	public boolean isVerified() {
		return verified;
	}

	public void setVerified(boolean verified) {
		this.verified = verified;
	}

	public int getGreenPoint() {
		return greenPoint;
	}

	public void setGreenPoint(int greenPoint) {
		this.greenPoint = greenPoint;
	}

	public int getRedPoint() {
		return redPoint;
	}

	public void setRedPoint(int redPoint) {
		this.redPoint = redPoint;
	}

	public int getRedPointTotal() {
		return redPointTotal;
	}

	public void setRedPointTotal(int redPointTotal) {
		this.redPointTotal = redPointTotal;
	}

	public Date getRegDate() {
		return regDate;
	}

	public void setRegDate(Date regDate) {
		this.regDate = regDate;
	}

	public Date getModDate() {
		return modDate;
	}

	public void setModDate(Date modDate) {
		this.modDate = modDate;
	}

	public String getUserSequence() {
		return userSequence;
	}

	public void setUserSequence(String userSequence) {
		this.userSequence = userSequence;
	}

	public String getUserPassword() {
		return userPassword;
	}

	public void setUserPassword(String userPassword) {
		this.userPassword = userPassword;
	}

	
}
