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
import java.util.UUID;

import org.fao.sola.clients.android.opentenure.OpenTenureApplication;
import org.fao.sola.clients.android.opentenure.R;
import org.fao.sola.clients.android.opentenure.filesystem.FileSystemUtilities;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

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

	public Person() {
		this.personId = UUID.randomUUID().toString();
	}

	@Override
	public String toString() {
		return "Person [" + "personId=" + personId + ", firstName=" + firstName
				+ ", uploaded=" + uploaded + ", lastName=" + lastName
				+ ", dateOfBirth=" + dateOfBirth + ", placeOfBirth="
				+ placeOfBirth +",gender="+gender+ ", emailAddress=" + emailAddress
				+ ", postalAddress=" + postalAddress + ", mobilePhoneNumber="
				+ mobilePhoneNumber + ", contactPhoneNumber="
				+ contactPhoneNumber + "]";
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

	public static int markAsUploaded(Person person) {
		person.setUploaded(true);
		return Person.updatePerson(person);
	}

	public int markAsUploaded() {
		setUploaded(true);
		return update();
	}

	public static int createPerson(Person person) {
		int result = 0;
		Connection localConnection = null;
		PreparedStatement statement = null;

		try {

			localConnection = OpenTenureApplication.getInstance().getDatabase()
					.getConnection();
			statement = localConnection
					.prepareStatement("INSERT INTO PERSON(PERSON_ID, UPLOADED, FIRST_NAME, LAST_NAME, DATE_OF_BIRTH, PLACE_OF_BIRTH, EMAIL_ADDRESS, POSTAL_ADDRESS, MOBILE_PHONE_NUMBER, CONTACT_PHONE_NUMBER, GENDER) VALUES (?,?,?,?,?,?,?,?,?,?,?)");
			statement.setString(1, person.getPersonId());
			statement.setBoolean(2, person.getUploaded());
			statement.setString(3, person.getFirstName());
			statement.setString(4, person.getLastName());
			statement.setDate(5, person.getDateOfBirth());
			statement.setString(6, person.getPlaceOfBirth());
			statement.setString(7, person.getEmailAddress());
			statement.setString(8, person.getPostalAddress());
			statement.setString(9, person.getMobilePhoneNumber());
			statement.setString(10, person.getContactPhoneNumber());
			statement.setString(11, person.getGender());
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
					.prepareStatement("INSERT INTO PERSON(PERSON_ID, UPLOADED, FIRST_NAME, LAST_NAME, DATE_OF_BIRTH, PLACE_OF_BIRTH, EMAIL_ADDRESS, POSTAL_ADDRESS, MOBILE_PHONE_NUMBER, CONTACT_PHONE_NUMBER, GENDER) VALUES (?,?,?,?,?,?,?,?,?,?,?)");
			statement.setString(1, getPersonId());
			statement.setBoolean(2, getUploaded());
			statement.setString(3, getFirstName());
			statement.setString(4, getLastName());
			statement.setDate(5, getDateOfBirth());
			statement.setString(6, getPlaceOfBirth());
			statement.setString(7, getEmailAddress());
			statement.setString(8, getPostalAddress());
			statement.setString(9, getMobilePhoneNumber());
			statement.setString(10, getContactPhoneNumber());
			statement.setString(11, getGender());
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
					.prepareStatement("UPDATE PERSON SET UPLOADED=?, FIRST_NAME=?, LAST_NAME=?, DATE_OF_BIRTH=?, PLACE_OF_BIRTH=?, EMAIL_ADDRESS=?, POSTAL_ADDRESS=?, MOBILE_PHONE_NUMBER=?, CONTACT_PHONE_NUMBER=?, GENDER=? WHERE PERSON_ID=?");
			statement.setBoolean(1, person.getUploaded());
			statement.setString(2, person.getFirstName());
			statement.setString(3, person.getLastName());
			statement.setDate(4, person.getDateOfBirth());
			statement.setString(5, person.getPlaceOfBirth());
			statement.setString(6, person.getEmailAddress());
			statement.setString(7, person.getPostalAddress());
			statement.setString(8, person.getMobilePhoneNumber());
			statement.setString(9, person.getContactPhoneNumber());
			statement.setString(10, person.getPersonId());
			statement.setString(10, person.getGender());
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
					.prepareStatement("UPDATE PERSON SET UPLOADED=?, FIRST_NAME=?, LAST_NAME=?, DATE_OF_BIRTH=?, PLACE_OF_BIRTH=?, EMAIL_ADDRESS=?, POSTAL_ADDRESS=?, MOBILE_PHONE_NUMBER=?, CONTACT_PHONE_NUMBER=?, GENDER=? WHERE PERSON_ID=?");
			statement.setBoolean(1, getUploaded());
			statement.setString(2, getFirstName());
			statement.setString(3, getLastName());
			statement.setDate(4, getDateOfBirth());
			statement.setString(5, getPlaceOfBirth());
			statement.setString(6, getEmailAddress());
			statement.setString(7, getPostalAddress());
			statement.setString(8, getMobilePhoneNumber());
			statement.setString(9, getContactPhoneNumber());
			statement.setString(10, getPersonId());
			statement.setString(11, getGender());
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
					.prepareStatement("SELECT FIRST_NAME, LAST_NAME, DATE_OF_BIRTH, PLACE_OF_BIRTH, EMAIL_ADDRESS,POSTAL_ADDRESS, MOBILE_PHONE_NUMBER, CONTACT_PHONE_NUMBER, GENDER FROM PERSON PER WHERE PER.PERSON_ID=?");
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

	public Boolean getUploaded() {
		return uploaded;
	}

	public void setUploaded(Boolean uploaded) {
		this.uploaded = uploaded;
	}

	public static File getPersonPictureFile(String personId) {
		return new File(FileSystemUtilities.getClaimantFolder(personId)
				+ File.separator + personId + ".jpg");
	}

	public static Bitmap getPersonPicture(Context context, File personPictureFile, int size) {
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inPreferredConfig = Bitmap.Config.ARGB_8888;

		Bitmap bitmap = BitmapFactory.decodeFile(personPictureFile.getPath(),
					options);
		if(bitmap ==  null){
			bitmap = BitmapFactory.decodeResource(context.getResources(),R.drawable.ic_contact_picture);
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
		return Bitmap.createScaledBitmap(croppedBitmap, size, size, false);
	}

	String personId;
	Boolean uploaded = Boolean.valueOf(false);
	String firstName;
	String lastName;
	java.sql.Date dateOfBirth;
	String placeOfBirth;
	String emailAddress;
	String postalAddress;
	String mobilePhoneNumber;
	String contactPhoneNumber;
	String gender; 

}
