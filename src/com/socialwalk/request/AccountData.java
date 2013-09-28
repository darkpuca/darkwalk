package com.socialwalk.request;

import java.util.Date;

public class AccountData
{
	private String userId, userSequence, userPassword;
	
	private String name;
	private int userType;
	private int areaCode, areaSubCode;
	private int organizationCode, organizationSubCode;
	private String organizationKey;
	private int gender;
	private Date birthday;
	private int weight;
	private String recommendedId;
	private int communityId;
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

	public int getAreaCode() {
		return areaCode;
	}

	public void setAreaCode(int areaCode) {
		this.areaCode = areaCode;
	}

	public int getAreaSubCode() {
		return areaSubCode;
	}

	public void setAreaSubCode(int areaSubCode) {
		this.areaSubCode = areaSubCode;
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

	public int getUserType() {
		return userType;
	}

	public void setUserType(int userType) {
		this.userType = userType;
	}

	public String getRecommendedId() {
		return recommendedId;
	}

	public void setRecommendedId(String recommendedId) {
		this.recommendedId = recommendedId;
	}

	public int getOrganizationSubCode() {
		return organizationSubCode;
	}

	public void setOrganizationSubCode(int organizationSubCode) {
		this.organizationSubCode = organizationSubCode;
	}

	public String getOrganizationKey() {
		return organizationKey;
	}

	public void setOrganizationKey(String organizationKey) {
		this.organizationKey = organizationKey;
	}

	
}
