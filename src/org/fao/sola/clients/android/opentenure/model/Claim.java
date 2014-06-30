/**
 * ******************************************************************************************
 * Copyright (C) 2014 - Food and Agriculture Organization of the United Nations (FAO).
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,281
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
import java.util.ArrayList;
import java.util.Arrays;
import java.sql.Date;
import java.util.List;
import java.util.UUID;

import org.fao.sola.clients.android.opentenure.OpenTenureApplication;
import org.fao.sola.clients.android.opentenure.R;

import android.content.Context;

public class Claim {

	public enum Status {
		unmoderated, moderated, challenged, created, uploading, upload_incomplete, upload_error
	};

	public static final int MAX_SHARES_PER_CLAIM = 100;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<AdditionalInfo> getAdditionalInfo() {
		return additionalInfo;
	}

	public void setAdditionalInfo(List<AdditionalInfo> additionalInfo) {
		this.additionalInfo = additionalInfo;
	}

	Database db = OpenTenureApplication.getInstance().getDatabase();

	public Claim() {
		this.claimId = UUID.randomUUID().toString();
		this.status = ClaimStatus._CREATED;
		this.availableShares = MAX_SHARES_PER_CLAIM;
	}

	public int getAvailableShares() {
		return availableShares;
	}

	public void setAvailableShares(int availableShares) {
		this.availableShares = availableShares;
	}

	@Override
	public String toString() {
		return "Claim [" + "claimId=" + claimId + ", status=" + status
				+ ", type=" + type + ", name=" + name + ", person=" + person
				+ ", propertyLocations="
				+ Arrays.toString(propertyLocations.toArray()) + ", vertices="
				+ Arrays.toString(vertices.toArray()) + ", additionalInfo="
				+ Arrays.toString(additionalInfo.toArray())
				+ ", challengedClaim=" + challengedClaim
				+ ", challengeExpiryDate=" + challengeExpiryDate
				+ ", dateOfStart=" + dateOfStart + ", challengingClaims="
				+ Arrays.toString(challengingClaims.toArray())
				+ ", attachments=" + Arrays.toString(attachments.toArray())
				+ ", owners=" + Arrays.toString(owners.toArray())
				+ ", availableShares=" + availableShares + "]";
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getClaimId() {
		return claimId;
	}

	public void setClaimId(String claimId) {
		this.claimId = claimId;
	}

	public Person getPerson() {
		return person;
	}

	public void setPerson(Person person) {
		this.person = person;
	}

	public List<Vertex> getVertices() {
		return vertices;
	}

	public void setVertices(List<Vertex> vertices) {
		this.vertices = vertices;
	}

	public List<PropertyLocation> getPropertyLocations() {
		return propertyLocations;
	}

	public void setPropertyLocations(List<PropertyLocation> propertyLocations) {
		this.propertyLocations = propertyLocations;
	}

	public Claim getChallengedClaim() {
		return challengedClaim;
	}

	public void setChallengedClaim(Claim challengedClaim) {
		this.challengedClaim = challengedClaim;
	}

	public List<Claim> getChallengingClaims() {
		return challengingClaims;
	}

	public void setChallengingClaims(List<Claim> challengingClaims) {
		this.challengingClaims = challengingClaims;
	}

	public List<Attachment> getAttachments() {
		return attachments;
	}

	public void setAttachments(List<Attachment> attachments) {
		this.attachments = attachments;
	}

	public List<Owner> getOwners() {
		return owners;
	}

	public void setOwners(List<Owner> owners) {
		availableShares = MAX_SHARES_PER_CLAIM;
		for (Owner owner : owners) {
			availableShares -= owner.getShares();
		}
		this.owners = owners;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Date getChallengeExpiryDate() {
		return challengeExpiryDate;
	}

	public void setChallengeExpiryDate(Date challengeExpiryDate) {
		this.challengeExpiryDate = challengeExpiryDate;
	}

	public String getLandUse() {
		return landUse;
	}

	public void setLandUse(String landUse) {
		this.landUse = landUse;
	}

	public java.sql.Date getDateOfStart() {
		return dateOfStart;
	}

	public void setDateOfStart(java.sql.Date dateOfStart) {
		this.dateOfStart = dateOfStart;
	}

	public static int createClaim(Claim claim) {
		int result = 0;
		Connection localConnection = null;
		PreparedStatement statement = null;

		try {

			localConnection = OpenTenureApplication.getInstance().getDatabase()
					.getConnection();
			statement = localConnection
					.prepareStatement("INSERT INTO CLAIM(CLAIM_ID, STATUS, NAME, TYPE, PERSON_ID, CHALLENGED_CLAIM_ID, CHALLANGE_EXPIRY_DATE,DATE_OF_START, LAND_USE) VALUES(?,?,?,?,?,?,?,?,?)");
			statement.setString(1, claim.getClaimId());
			statement.setString(2, claim.getStatus());
			statement.setString(3, claim.getName());
			statement.setString(4, claim.getType());
			statement.setString(5, claim.getPerson().getPersonId());
			if (claim.getChallengedClaim() != null) {
				statement.setString(6, claim.getChallengedClaim().getClaimId());

			} else {
				statement.setString(6, null);
			}
			statement.setDate(7, claim.getChallengeExpiryDate());
			statement.setDate(8, claim.getDateOfStart());
			statement.setString(9, claim.getLandUse());

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

	public int create() {
		int result = 0;
		Connection localConnection = null;
		PreparedStatement statement = null;

		try {

			localConnection = db.getConnection();
			statement = localConnection
					.prepareStatement("INSERT INTO CLAIM(CLAIM_ID, STATUS, NAME, TYPE, PERSON_ID, CHALLENGED_CLAIM_ID, CHALLANGE_EXPIRY_DATE,DATE_OF_START, LAND_USE) VALUES(?,?,?,?,?,?,?,?,?)");
			statement.setString(1, getClaimId());
			statement.setString(2, getStatus());
			statement.setString(3, getName());
			statement.setString(4, getType());
			statement.setString(5, getPerson().getPersonId());
			if (getChallengedClaim() != null) {
				statement.setString(6, getChallengedClaim().getClaimId());
			} else {
				statement.setString(6, null);
			}
			statement.setDate(7, getChallengeExpiryDate());
			statement.setDate(8, getDateOfStart());
			statement.setString(9, getLandUse());
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

	public static int updateClaim(Claim claim) {
		int result = 0;
		Connection localConnection = null;
		PreparedStatement statement = null;

		try {

			localConnection = OpenTenureApplication.getInstance().getDatabase()
					.getConnection();
			statement = localConnection
					.prepareStatement("UPDATE CLAIM SET STATUS=?, NAME=?, PERSON_ID=?, TYPE=?,CHALLENGED_CLAIM_ID=?, CHALLANGE_EXPIRY_DATE=?, DATE_OF_START=?, LAND_USE=? WHERE CLAIM_ID=?");
			statement.setString(1, claim.getStatus());
			statement.setString(2, claim.getName());
			statement.setString(3, claim.getPerson().getPersonId());
			statement.setString(4, claim.getType());
			if (claim.getChallengedClaim() != null) {
				statement.setString(5, claim.getChallengedClaim().getClaimId());
			} else {
				statement.setString(5, null);
			}
			statement.setDate(6, claim.getChallengeExpiryDate());
			statement.setDate(7, claim.getChallengeExpiryDate());
			statement.setString(8, claim.getLandUse());
			statement.setString(9, claim.getClaimId());

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
					.prepareStatement("UPDATE CLAIM SET STATUS=?, NAME=?, PERSON_ID=?, TYPE=?, CHALLENGED_CLAIM_ID=?, CHALLANGE_EXPIRY_DATE=?, DATE_OF_START=?, LAND_USE=? WHERE CLAIM_ID=?");
			statement.setString(1, getStatus());
			statement.setString(2, getName());
			statement.setString(3, getPerson().getPersonId());
			statement.setString(4, getType());
			if (getChallengedClaim() != null) {
				statement.setString(5, getChallengedClaim().getClaimId());

			} else {
				statement.setString(5, null);
			}
			statement.setDate(6, getChallengeExpiryDate());
			statement.setDate(7, getDateOfStart());
			statement.setString(8, getLandUse());
			statement.setString(9, getClaimId());

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

	public static Claim getClaim(String claimId) {
		Claim claim = null;
		Connection localConnection = null;
		PreparedStatement statement = null;
		ResultSet rs = null;

		try {

			localConnection = OpenTenureApplication.getInstance().getDatabase()
					.getConnection();
			statement = localConnection
					.prepareStatement("SELECT STATUS, NAME, PERSON_ID, TYPE, CHALLENGED_CLAIM_ID, CHALLANGE_EXPIRY_DATE, DATE_OF_START, LAND_USE FROM CLAIM WHERE CLAIM_ID=?");
			statement.setString(1, claimId);
			rs = statement.executeQuery();
			while (rs.next()) {
				claim = new Claim();
				claim.setClaimId(claimId);
				claim.setStatus(rs.getString(1));
				claim.setName(rs.getString(2));
				claim.setPerson(Person.getPerson(rs.getString(3)));
				claim.setType((rs.getString(4)));
				claim.setChallengedClaim(Claim.getClaim(rs.getString(5)));
				claim.setChallengeExpiryDate(rs.getDate(6));
				claim.setDateOfStart(rs.getDate(7));
				claim.setLandUse(rs.getString(8));
				claim.setVertices(Vertex.getVertices(claimId));
				claim.setPropertyLocations(PropertyLocation
						.getPropertyLocations(claimId));
				claim.setAttachments(Attachment.getAttachments(claimId));
				claim.setOwners(Owner.getOwners(claimId));
				claim.setAdditionalInfo(AdditionalInfo
						.getClaimAdditionalInfo(claimId));

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
		return claim;
	}

	public static List<Claim> getChallengingClaims(String claimId) {
		List<Claim> challengingClaims = new ArrayList<Claim>();
		Connection localConnection = null;
		PreparedStatement statement = null;
		ResultSet rs = null;

		try {
			localConnection = OpenTenureApplication.getInstance().getDatabase()
					.getConnection();
			statement = localConnection
					.prepareStatement("SELECT CLAIM_ID FROM CLAIM WHERE CHALLENGED_CLAIM_ID=?");
			statement.setString(1, claimId);
			rs = statement.executeQuery();
			while (rs.next()) {
				Claim challengingClaim = Claim.getClaim(rs.getString(1));
				challengingClaims.add(challengingClaim);
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
		return challengingClaims;
	}

	public static int addOwner(String claimId, String personId, int shares) {
		return Claim.getClaim(claimId).addOwner(personId, shares);
	}

	public static int removeOwner(String claimId, String personId) {
		return Claim.getClaim(claimId).removeOwner(personId);
	}

	public int addOwner(String personId, int shares) {

		Owner own = new Owner(false);
		own.setClaimId(claimId);
		own.setPersonId(personId);
		own.setOwnerId(personId);
		own.setShares(shares);

		int result = own.create();

		if (result == 1) {
			availableShares -= shares;
		}
		return result;
	}

	public int removeOwner(String personId) {

		Owner own = Owner.getOwner(claimId, personId);
		int shares = own.getShares();

		int result = own.delete();

		if (result == 1) {
			availableShares += shares;
		}
		return result;
	}

	public static List<Claim> getAllClaims() {
		List<Claim> claims = new ArrayList<Claim>();
		Connection localConnection = null;
		PreparedStatement statement = null;
		ResultSet rs = null;

		try {

			localConnection = OpenTenureApplication.getInstance().getDatabase()
					.getConnection();
			statement = localConnection
					.prepareStatement("SELECT CLAIM_ID FROM CLAIM");
			rs = statement.executeQuery();
			while (rs.next()) {
				Claim claim = Claim.getClaim(rs.getString(1));
				claims.add(claim);
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
		return claims;
	}

	public int delete() {
		int result = 0;
		Connection localConnection = null;
		PreparedStatement statement = null;

		try {

			localConnection = OpenTenureApplication.getInstance().getDatabase()
					.getConnection();
			statement = localConnection
					.prepareStatement("DELETE CLAIM WHERE CLAIM_ID=?");
			statement.setString(1, getClaimId());

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

	public String getSlogan(Context context) {
		String claimName = getName().equalsIgnoreCase("") ? context
				.getString(R.string.default_claim_name) : getName();
		return claimName + ", " + context.getString(R.string.by) + ": "
				+ getPerson().getFirstName() + " " + getPerson().getLastName();
	}

	private String claimId;
	private String name;
	private String type;
	private String status;
	private Person person;
	private Date dateOfStart;
	private Claim challengedClaim;
	private List<Vertex> vertices;
	private List<PropertyLocation> propertyLocations;
	private List<AdditionalInfo> additionalInfo;
	private List<Claim> challengingClaims;
	private List<Attachment> attachments;
	private List<Owner> owners;
	private Date challengeExpiryDate;
	private int availableShares;
	private String landUse;

}
