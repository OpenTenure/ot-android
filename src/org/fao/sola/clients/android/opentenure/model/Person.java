/**
 * ******************************************************************************************
 * Copyright (C) 2014 - Food and Agriculture Organization of the United Nations (FAO).
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 *    1. Redistributions of source code must retain the above copyright notice,this list
 *       of conditions and the following disclaimer.
 *    2. Redistributions in binary form must reproduce the above copyright notice,this list
 *       of conditions and the following disclaimer in the documentation and/or other
 *       materials provided with the distribution.
 *    3. Neither the name of FAO nor the names of its contributors may be used to endorse or
 *       promote products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT
 * SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,PROCUREMENT
 * OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,STRICT LIABILITY,OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * *********************************************************************************************
 */
package org.fao.sola.clients.android.opentenure.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.UUID;

import org.fao.sola.clients.android.opentenure.OpenTenureApplication;

public class Person {
	
	Database db = OpenTenureApplication.getInstance().getDatabase();
	
	public java.sql.Date getDateOfBirth() {
		return dateOfBirth;
	}

	public void setDateOfBirth(java.sql.Date dateOfBirth) {
		this.dateOfBirth = dateOfBirth;
	}

	public String getPlaceOfBirth() {
		return placeOfBirth;
	}

	public void setPlaceOfBirth(String placeOfBirth) {
		this.placeOfBirth = placeOfBirth;
	}

	Person(){
		this.personId = UUID.randomUUID().toString();
	}

	@Override
	public String toString() {
		return "Person [personId=" + personId + ", firstName=" + firstName
				+ ", lastName=" + lastName + ", dateOfBirth=" + dateOfBirth
				+ ", placeOfBirth=" + placeOfBirth + ", emailAddress="
				+ emailAddress + ", postalAddress=" + postalAddress
				+ ", mobilePhoneNumber=" + mobilePhoneNumber
				+ ", contactPhoneNumber=" + contactPhoneNumber + "]";
	}
	public String getPersonId() {
		return personId;
	}
	public void setPersonId(String personId) {
		this.personId = personId;
	}
	public String getFirstName() {
		return firstName;
	}
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}
	public String getLastName() {
		return lastName;
	}
	public void setLastName(String lastName) {
		this.lastName = lastName;
	}
	public String getEmailAddress() {
		return emailAddress;
	}
	public void setEmailAddress(String emailAddress) {
		this.emailAddress = emailAddress;
	}
	public String getPostalAddress() {
		return postalAddress;
	}
	public void setPostalAddress(String postalAddress) {
		this.postalAddress = postalAddress;
	}
	public String getContactPhoneNumber() {
		return contactPhoneNumber;
	}
	public void setContactPhoneNumber(String contactPhoneNumber) {
		this.contactPhoneNumber = contactPhoneNumber;
	}
	public String getMobilePhoneNumber() {
		return mobilePhoneNumber;
	}
	public void setMobilePhoneNumber(String mobilePhoneNumber) {
		this.mobilePhoneNumber = mobilePhoneNumber;
	}

	public static int createPerson(Person person) {
		return OpenTenureApplication.getInstance().getDatabase().update("INSERT INTO PERSON(PERSON_ID, FIRST_NAME, LAST_NAME, DATE_OF_BIRTH, PLACE_OF_BIRTH, EMAIL_ADDRESS, POSTAL_ADDRESS, MOBILE_PHONE_NUMBER, CONTACT_PHONE_NUMBER) VALUES ('"
				+ person.getPersonId()
				+ "', '"
				+ person.getFirstName()
				+ "', '"
				+ person.getLastName()
				+ "', '"
				+ new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(person.getDateOfBirth())
				+ "', '"
				+ person.getPlaceOfBirth()
				+ "', '"
				+ person.getEmailAddress()
				+ "', '"
				+ person.getPostalAddress()
				+ "', '" 
				+ person.getMobilePhoneNumber()
				+ "', '" 
				+ person.getContactPhoneNumber()
				+ "')");
	}

	public int create() {
		return db.update("INSERT INTO PERSON(PERSON_ID, FIRST_NAME, LAST_NAME, DATE_OF_BIRTH, PLACE_OF_BIRTH, EMAIL_ADDRESS, POSTAL_ADDRESS, MOBILE_PHONE_NUMBER, CONTACT_PHONE_NUMBER) VALUES ('"
				+ getPersonId()
				+ "', '"
				+ getFirstName()
				+ "', '"
				+ getLastName()
				+ "', '"
				+ new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(getDateOfBirth())
				+ "', '"
				+ getPlaceOfBirth()
				+ "', '"
				+ getEmailAddress()
				+ "', '"
				+ getPostalAddress()
				+ "', '" 
				+ getMobilePhoneNumber()
				+ "', '" 
				+ getContactPhoneNumber()
				+ "')");
	}

	public static int deletePerson(Person person) {
		return OpenTenureApplication.getInstance().getDatabase().update("DELETE FROM PERSON WHERE PERSON_ID='"
				+ person.getPersonId()
				+ "'");
	}

	public int delete() {
		return OpenTenureApplication.getInstance().getDatabase().update("DELETE FROM PERSON WHERE PERSON_ID='"
				+ getPersonId()
				+ "'");
	}

	public static int updatePerson(Person person) {
		return OpenTenureApplication.getInstance().getDatabase().update("UPDATE PERSON SET FIRST_NAME='"
				+ person.getFirstName()
				+ "', LAST_NAME='"
				+ person.getLastName()
				+ "', DATE_OF_BIRTH='"
				+ new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(person.getDateOfBirth())
				+ "', PLACE_OF_BIRTH='"
				+ person.getPlaceOfBirth()
				+ "', EMAIL_ADDRESS='"
				+ person.getEmailAddress()
				+ "', POSTAL_ADDRESS='"
				+ person.getPostalAddress()
				+ "', MOBILE_PHONE_NUMBER='"
				+ person.getMobilePhoneNumber()
				+ "', CONTACT_PHONE_NUMBER='"
				+ person.getContactPhoneNumber()
				+ "' WHERE PERSON_ID='"
				+ person.getPersonId() + "'");
	}

	public int update() {
		return db.update("UPDATE PERSON SET FIRST_NAME='"
				+ getFirstName()
				+ "', LAST_NAME='"
				+ getLastName()
				+ "', DATE_OF_BIRTH='"
				+ new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(getDateOfBirth())
				+ "', PLACE_OF_BIRTH='"
				+ getPlaceOfBirth()
				+ "', EMAIL_ADDRESS='"
				+ getEmailAddress()
				+ "', POSTAL_ADDRESS='"
				+ getPostalAddress()
				+ "', MOBILE_PHONE_NUMBER='"
				+ getMobilePhoneNumber()
				+ "', CONTACT_PHONE_NUMBER='"
				+ getContactPhoneNumber()
				+ "' WHERE PERSON_ID='"
				+ getPersonId() + "'");
	}

	public static Person getPerson(String personId) {

		Person person = null;

		Connection localConnection = null;
		try {

			localConnection = OpenTenureApplication.getInstance().getDatabase().getConnection();
			PreparedStatement statement = localConnection
					.prepareStatement("SELECT FIRST_NAME, LAST_NAME, DATE_OF_BIRTH, PLACE_OF_BIRTH, EMAIL_ADDRESS,POSTAL_ADDRESS, MOBILE_PHONE_NUMBER, CONTACT_PHONE_NUMBER FROM PERSON PER WHERE PER.PERSON_ID=?");
			statement.setString(1, personId);
			ResultSet rs = statement.executeQuery();
			while (rs.next()) {
				person = new Person();
				person.setPersonId(personId);
				person.setFirstName(rs.getString(1));
				person.setLastName(rs.getString(2));
				person.setDateOfBirth(rs.getDate(3));
				person.setPlaceOfBirth(rs.getString(4));
				person.setEmailAddress(rs.getString(5));
				person.setPostalAddress(rs.getString(6));
				person.setMobilePhoneNumber(rs.getString(7));
				person.setContactPhoneNumber(rs.getString(8));
			}
			rs.close();
			statement.close();
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (Exception exception) {
			exception.printStackTrace();
		} finally {
			if (localConnection != null) {
				try {
					localConnection.close();
				} catch (SQLException e) {
				}
			}
		}
		return person;
	}

	String personId;
	String firstName;
	String lastName;
	java.sql.Date dateOfBirth;
	String placeOfBirth;
	String emailAddress;
	String postalAddress;
	String mobilePhoneNumber;
	String contactPhoneNumber;

}
