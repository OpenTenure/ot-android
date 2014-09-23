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

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.fao.sola.clients.android.opentenure.OpenTenureApplication;
import org.fao.sola.clients.android.opentenure.R;
import org.fao.sola.clients.android.opentenure.filesystem.FileSystemUtilities;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class Person {

	Database db = OpenTenureApplication.getInstance().getDatabase();

	public static String _GROUP = "group";
	public static String _PHYSICAL = "physical";

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

	@Override
	public boolean equals(Object v) {
		boolean retVal = false;

		if (v instanceof Person) {

			retVal = this.getPersonId().equals(((Person) v).getPersonId());

		}

		return retVal;
	}

	public boolean hasUploadedClaims() {
		Connection localConnection = null;
		PreparedStatement statement = null;
		ResultSet rs = null;
		boolean result = false;

		try {

			localConnection = db.getConnection();
			statement = localConnection
					.prepareStatement("SELECT CLAIM_ID FROM CLAIM WHERE PERSON_ID=? AND STATUS <> 'created' UNION SELECT CLAIM_ID FROM OWNER WHERE PERSON_ID=?");
			statement.setString(1, personId);
			statement.setString(2, personId);
			rs = statement.executeQuery();
			while (rs.next()) {
				result = true;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (Exception exception) {
			exception.printStackTrace();
		} finally {
			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException e) {
				}
			}
			if (statement != null) {
				try {
					statement.close();
				} catch (SQLException e) {
				}
			}
			if (localConnection != null) {
				try {
					localConnection.close();
				} catch (SQLException e) {
				}
			}
		}
		return result;
	}

	public static boolean hasUploadedClaims(String personId) {
		Connection localConnection = null;
		PreparedStatement statement = null;
		ResultSet rs = null;
		boolean result = false;

		try {

			localConnection = OpenTenureApplication.getInstance().getDatabase()
					.getConnection();
			statement = localConnection
					.prepareStatement("SELECT CLAIM_ID FROM CLAIM WHERE PERSON_ID=? AND STATUS <> 'created' UNION SELECT CLAIM_ID FROM OWNER WHERE PERSON_ID=?");
			statement.setString(1, personId);
			statement.setString(2, personId);
			rs = statement.executeQuery();
			while (rs.next()) {
				result = true;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (Exception exception) {
			exception.printStackTrace();
		} finally {
			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException e) {
				}
			}
			if (statement != null) {
				try {
					statement.close();
				} catch (SQLException e) {
				}
			}
			if (localConnection != null) {
				try {
					localConnection.close();
				} catch (SQLException e) {
				}
			}
		}
		return result;
	}

	public Person() {
		this.personId = UUID.randomUUID().toString();
	}

	@Override
	public String toString() {
		return "Person [" + "personId=" + personId + ", firstName=" + firstName
				+ ", lastName=" + lastName + ", dateOfBirth=" + dateOfBirth
				+ ", placeOfBirth=" + placeOfBirth + ",gender=" + gender
				+ ", emailAddress=" + emailAddress + ", postalAddress="
				+ postalAddress + ", personType=" + personType
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

	public String getGender() {
		return gender;
	}

	public void setGender(String gender) {
		this.gender = gender;
	}

	public String getIdType() {
		return idType;
	}

	public void setIdType(String idType) {
		this.idType = idType;
	}

	public String getIdNumber() {
		return idNumber;
	}

	public void setIdNumber(String idNumber) {
		this.idNumber = idNumber;
	}

	public static int createPerson(Person person) {
		int result = 0;
		Connection localConnection = null;
		PreparedStatement statement = null;

		try {

			localConnection = OpenTenureApplication.getInstance().getDatabase()
					.getConnection();
			statement = localConnection
					.prepareStatement("INSERT INTO PERSON(PERSON_ID, FIRST_NAME, LAST_NAME, DATE_OF_BIRTH, PLACE_OF_BIRTH, EMAIL_ADDRESS, POSTAL_ADDRESS, MOBILE_PHONE_NUMBER, CONTACT_PHONE_NUMBER, GENDER, ID_TYPE, ID_NUMBER, PERSON_TYPE) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)");
			statement.setString(1, person.getPersonId());
			statement.setString(2, person.getFirstName());
			if (person.getLastName() != null)
				statement.setString(3, person.getLastName());
			else
				statement.setString(3, "");
			statement.setDate(4, person.getDateOfBirth());
			statement.setString(5, person.getPlaceOfBirth());
			statement.setString(6, person.getEmailAddress());
			statement.setString(7, person.getPostalAddress());
			statement.setString(8, person.getMobilePhoneNumber());
			statement.setString(9, person.getContactPhoneNumber());
			statement.setString(10, person.getGender());
			statement.setString(11, person.getIdType());
			statement.setString(12, person.getIdNumber());
			statement.setString(13, person.getPersonType());
			result = statement.executeUpdate();

			FileSystemUtilities.createClaimantFolder(person.getPersonId());
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (Exception exception) {
			exception.printStackTrace();
		} finally {
			if (statement != null) {
				try {
					statement.close();
				} catch (SQLException e) {
				}
			}
			if (localConnection != null) {
				try {
					localConnection.close();
				} catch (SQLException e) {
				}
			}
		}
		return result;
	}

	public int create() {
		int result = 0;
		Connection localConnection = null;
		PreparedStatement statement = null;

		try {

			localConnection = db.getConnection();
			statement = localConnection
					.prepareStatement("INSERT INTO PERSON(PERSON_ID, FIRST_NAME, LAST_NAME, DATE_OF_BIRTH, PLACE_OF_BIRTH, EMAIL_ADDRESS, POSTAL_ADDRESS, MOBILE_PHONE_NUMBER, CONTACT_PHONE_NUMBER, GENDER, ID_TYPE, ID_NUMBER, PERSON_TYPE) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)");
			statement.setString(1, getPersonId());
			statement.setString(2, getFirstName());
			statement.setString(3, getLastName());
			statement.setDate(4, getDateOfBirth());
			statement.setString(5, getPlaceOfBirth());
			statement.setString(6, getEmailAddress());
			statement.setString(7, getPostalAddress());
			statement.setString(8, getMobilePhoneNumber());
			statement.setString(9, getContactPhoneNumber());
			statement.setString(10, getGender());
			statement.setString(11, getIdType());
			statement.setString(12, getIdNumber());
			statement.setString(13, getPersonType());
			result = statement.executeUpdate();
			FileSystemUtilities.createClaimantFolder(getPersonId());
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (Exception exception) {
			exception.printStackTrace();
		} finally {
			if (statement != null) {
				try {
					statement.close();
				} catch (SQLException e) {
				}
			}
			if (localConnection != null) {
				try {
					localConnection.close();
				} catch (SQLException e) {
				}
			}
		}
		return result;
	}

	public static int deletePerson(Person person) {
		int result = 0;
		Connection localConnection = null;
		PreparedStatement statement = null;

		try {

			localConnection = OpenTenureApplication.getInstance().getDatabase()
					.getConnection();
			statement = localConnection
					.prepareStatement("DELETE FROM PERSON WHERE PERSON_ID=?");
			statement.setString(1, person.getPersonId());
			result = statement.executeUpdate();
			FileSystemUtilities.removeClaimantFolder(person.getPersonId());
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (Exception exception) {
			exception.printStackTrace();
		} finally {
			if (statement != null) {
				try {
					statement.close();
				} catch (SQLException e) {
				}
			}
			if (localConnection != null) {
				try {
					localConnection.close();
				} catch (SQLException e) {
				}
			}
		}
		return result;
	}

	public int delete() {
		int result = 0;
		Connection localConnection = null;
		PreparedStatement statement = null;

		try {
			localConnection = db.getConnection();
			statement = localConnection
					.prepareStatement("DELETE FROM PERSON WHERE PERSON_ID=?");
			statement.setString(1, getPersonId());
			result = statement.executeUpdate();
			FileSystemUtilities.removeClaimantFolder(getPersonId());
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (Exception exception) {
			exception.printStackTrace();
		} finally {
			if (statement != null) {
				try {
					statement.close();
				} catch (SQLException e) {
				}
			}
			if (localConnection != null) {
				try {
					localConnection.close();
				} catch (SQLException e) {
				}
			}
		}
		return result;
	}

	public static int updatePerson(Person person) {
		int result = 0;
		Connection localConnection = null;
		PreparedStatement statement = null;

		try {
			localConnection = OpenTenureApplication.getInstance().getDatabase()
					.getConnection();
			statement = localConnection
					.prepareStatement("UPDATE PERSON SET FIRST_NAME=?, LAST_NAME=?, DATE_OF_BIRTH=?, PLACE_OF_BIRTH=?, EMAIL_ADDRESS=?, POSTAL_ADDRESS=?, MOBILE_PHONE_NUMBER=?, CONTACT_PHONE_NUMBER=?, GENDER=?, ID_TYPE=?, ID_NUMBER=?, PERSON_TYPE=? WHERE PERSON_ID=?");
			statement.setString(1, person.getFirstName());
			statement.setString(2, person.getLastName());
			statement.setDate(3, person.getDateOfBirth());
			statement.setString(4, person.getPlaceOfBirth());
			statement.setString(5, person.getEmailAddress());
			statement.setString(6, person.getPostalAddress());
			statement.setString(7, person.getMobilePhoneNumber());
			statement.setString(8, person.getContactPhoneNumber());
			statement.setString(9, person.getGender());
			statement.setString(10, person.getIdType());
			statement.setString(11, person.getIdNumber());
			statement.setString(12, person.getPersonType());
			statement.setString(13, person.getPersonId());

			result = statement.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (Exception exception) {
			exception.printStackTrace();
		} finally {
			if (statement != null) {
				try {
					statement.close();
				} catch (SQLException e) {
				}
			}
			if (localConnection != null) {
				try {
					localConnection.close();
				} catch (SQLException e) {
				}
			}
		}
		return result;
	}

	public int update() {
		int result = 0;
		Connection localConnection = null;
		PreparedStatement statement = null;

		try {

			localConnection = db.getConnection();
			statement = localConnection
					.prepareStatement("UPDATE PERSON SET FIRST_NAME=?, LAST_NAME=?, DATE_OF_BIRTH=?, PLACE_OF_BIRTH=?, EMAIL_ADDRESS=?, POSTAL_ADDRESS=?, MOBILE_PHONE_NUMBER=?, CONTACT_PHONE_NUMBER=?, GENDER=?, ID_TYPE=?, ID_NUMBER=?, PERSON_TYPE=? WHERE PERSON_ID=?");
			statement.setString(1, getFirstName());
			statement.setString(2, getLastName());
			statement.setDate(3, getDateOfBirth());
			statement.setString(4, getPlaceOfBirth());
			statement.setString(5, getEmailAddress());
			statement.setString(6, getPostalAddress());
			statement.setString(7, getMobilePhoneNumber());
			statement.setString(8, getContactPhoneNumber());
			statement.setString(9, getGender());
			statement.setString(10, getIdType());
			statement.setString(11, getIdNumber());
			statement.setString(12, getPersonType());
			statement.setString(13, getPersonId());
			result = statement.executeUpdate();

		} catch (SQLException e) {

			e.printStackTrace();
		} catch (Exception exception) {

			exception.printStackTrace();
		} finally {
			if (statement != null) {
				try {
					statement.close();
				} catch (SQLException e) {
				}
			}
			if (localConnection != null) {
				try {
					localConnection.close();
				} catch (SQLException e) {
				}
			}
		}
		return result;
	}

	public static Person getPerson(String personId) {
		Person person = null;
		Connection localConnection = null;
		PreparedStatement statement = null;
		ResultSet rs = null;

		try {

			localConnection = OpenTenureApplication.getInstance().getDatabase()
					.getConnection();
			statement = localConnection
					.prepareStatement("SELECT FIRST_NAME, LAST_NAME, DATE_OF_BIRTH, PLACE_OF_BIRTH, EMAIL_ADDRESS, POSTAL_ADDRESS, MOBILE_PHONE_NUMBER, CONTACT_PHONE_NUMBER, GENDER, ID_TYPE, ID_NUMBER, PERSON_TYPE FROM PERSON PER WHERE PER.PERSON_ID=?");
			statement.setString(1, personId);
			rs = statement.executeQuery();
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
				person.setGender(rs.getString(9));
				person.setIdType(rs.getString(10));
				person.setIdNumber(rs.getString(11));
				person.setPersonType(rs.getString(12));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (Exception exception) {
			exception.printStackTrace();
		} finally {
			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException e) {
				}
			}
			if (statement != null) {
				try {
					statement.close();
				} catch (SQLException e) {
				}
			}
			if (localConnection != null) {
				try {
					localConnection.close();
				} catch (SQLException e) {
				}
			}
		}
		return person;
	}

	public static List<Person> getAllPersons() {
		List<Person> persons = new ArrayList<Person>();
		Connection localConnection = null;
		PreparedStatement statement = null;
		ResultSet rs = null;

		try {

			localConnection = OpenTenureApplication.getInstance().getDatabase()
					.getConnection();
			statement = localConnection
					.prepareStatement("SELECT PERSON_ID, FIRST_NAME, LAST_NAME, DATE_OF_BIRTH, PLACE_OF_BIRTH, EMAIL_ADDRESS, POSTAL_ADDRESS, MOBILE_PHONE_NUMBER, CONTACT_PHONE_NUMBER, GENDER, ID_TYPE, ID_NUMBER, PERSON_TYPE FROM PERSON");
			rs = statement.executeQuery();
			while (rs.next()) {

				Person person = new Person();
				person.setPersonId(rs.getString(1));
				person.setFirstName(rs.getString(2));
				person.setLastName(rs.getString(3));
				person.setDateOfBirth(rs.getDate(4));
				person.setPlaceOfBirth(rs.getString(5));
				person.setEmailAddress(rs.getString(6));
				person.setPostalAddress(rs.getString(7));
				person.setMobilePhoneNumber(rs.getString(8));
				person.setContactPhoneNumber(rs.getString(9));
				person.setGender(rs.getString(10));
				person.setIdType(rs.getString(11));
				person.setIdNumber(rs.getString(12));
				person.setPersonType(rs.getString(13));
				persons.add(person);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (Exception exception) {
			exception.printStackTrace();
		} finally {
			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException e) {
				}
			}
			if (statement != null) {
				try {
					statement.close();
				} catch (SQLException e) {
				}
			}
			if (localConnection != null) {
				try {
					localConnection.close();
				} catch (SQLException e) {
				}
			}
		}
		return persons;
	}

	public static ArrayList<String> getIdsWithSharesOrClaims() {
		ArrayList<String> ids = new ArrayList<String>();
		Connection localConnection = null;
		PreparedStatement statement = null;
		ResultSet rs = null;

		try {

			localConnection = OpenTenureApplication.getInstance().getDatabase()
					.getConnection();
			statement = localConnection
					.prepareStatement("SELECT DISTINCT PERSON_ID FROM ((SELECT PERSON_ID FROM OWNER) UNION (SELECT PERSON_ID FROM CLAIM))");
			rs = statement.executeQuery();
			while (rs.next()) {
				ids.add(rs.getString(1));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (Exception exception) {
			exception.printStackTrace();
		} finally {
			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException e) {
				}
			}
			if (statement != null) {
				try {
					statement.close();
				} catch (SQLException e) {
				}
			}
			if (localConnection != null) {
				try {
					localConnection.close();
				} catch (SQLException e) {
				}
			}
		}
		return ids;
	}

	public static File getPersonPictureFile(String personId) {
		return new File(FileSystemUtilities.getClaimantFolder(personId)
				+ File.separator + personId + ".jpg");
	}

	public static Bitmap getPersonPicture(Context context, String personId,
			int size) {
		return getPersonPicture(context, getPersonPictureFile(personId), size);
	}

	public static Bitmap getPersonPicture(Context context,
			File personPictureFile, int size) {
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inPreferredConfig = Bitmap.Config.ARGB_8888;

		Bitmap bitmap = BitmapFactory.decodeFile(personPictureFile.getPath(),
				options);
		if (bitmap == null) {
			bitmap = BitmapFactory.decodeResource(context.getResources(),
					R.drawable.ic_contact_picture);
		}

		int height = bitmap.getHeight();
		int width = bitmap.getWidth();
		int startOffset = 0;

		Bitmap croppedBitmap = null;

		if (height > width) {
			// Portrait
			startOffset = (height - width) / 2;
			croppedBitmap = Bitmap.createBitmap(bitmap, 0, startOffset, width,
					width);
		} else {
			// Landscape
			startOffset = (width - height) / 2;
			croppedBitmap = Bitmap.createBitmap(bitmap, startOffset, 0, height,
					height);
		}
		return Bitmap.createScaledBitmap(croppedBitmap, size, size, true);
	}

	public String getPersonType() {
		return personType;
	}

	public void setPersonType(String personType) {
		this.personType = personType;
	}

	public Person copy() {

		Person copy = new Person();

		copy.setContactPhoneNumber(this.contactPhoneNumber);
		copy.setDateOfBirth(this.dateOfBirth);
		copy.setEmailAddress(this.emailAddress);
		copy.setGender(this.gender);
		copy.setIdNumber(this.idNumber);
		copy.setIdType(this.idType);
		copy.setLastName(this.lastName);
		copy.setMobilePhoneNumber(this.mobilePhoneNumber);
		copy.setPersonType(this.personType);
		copy.setPlaceOfBirth(this.placeOfBirth);
		copy.setPostalAddress(this.postalAddress);
		copy.setFirstName(this.firstName);

		return copy;
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
	String gender;
	String idType;
	String idNumber;
	String personType;

}
