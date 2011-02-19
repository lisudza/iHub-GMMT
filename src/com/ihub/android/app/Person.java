package com.ihub.android.app;

public class Person {
	private String firstName;
	private String lastName;
	private String profilePic;
	private String cloudUserID;
	private String telephone;
	private String emailAddress;
	private String country;
	private String profilePicURL;
	private String qrCode;
	private String occupation;

	public Person() {

	}

	public String getProfilePic() {
		return this.profilePic;
	}

	public void setProfilePic(String profilePic) {
		this.profilePic = profilePic;
	}

	public String getCloudUserID() {
		return this.cloudUserID;
	}

	public void setCloudUserID(String userID) {
		this.cloudUserID = userID;
	}

	public String getTelephone() {
		return this.telephone;
	}

	public void setTelephone(String telephone) {
		this.telephone = telephone;
	}

	public String getCountry() {
		return this.country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public String getEmailAddress() {
		return this.emailAddress;
	}

	public void setEmailAddress(String emailAddress) {
		this.emailAddress = emailAddress;
	}

	public String getFirstName() {
		return this.firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return this.lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getProfilePicURL() {
		return this.profilePicURL;
	}
	
	public void setProfilePicURL(String profilePicURL) {
		this.profilePicURL = profilePicURL;
	}
	
	public String getQrCode() {
		return this.qrCode;
	}
	
	public void setQrCode(String qrCode) {
		this.qrCode = qrCode;  
	}
	
	public String getOccupation() {
		 return this.occupation;
	}
	
	public void setOccupation(String occupation) {
		this.occupation = occupation;
	}
}
