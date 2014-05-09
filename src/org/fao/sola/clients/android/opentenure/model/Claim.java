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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.fao.sola.clients.android.opentenure.OpenTenureApplication;

public class Claim {
	
	public enum Status{unmoderated, moderated, challenged, created, uploading};

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

	public Claim(){
		this.claimId = UUID.randomUUID().toString();
		this.status = ClaimStatus._CREATED;
	}

	@Override
	public String toString() {
		return "Claim [claimId=" + claimId
				+ ", status=" + status
				+ ", name=" + name
				+ ", person=" + person
				+ ", vertices=" + Arrays.toString(vertices.toArray())
				+ ", additionalInfo=" + Arrays.toString(additionalInfo.toArray())
				+ ", challengedClaim=" + challengedClaim
				+ ", challengingClaims=" + Arrays.toString(challengingClaims.toArray())
				+ ", attachments=" + Arrays.toString(attachments.toArray())+ "]";
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

	public static int createClaim(Claim claim){
		int result = 0;
		Connection localConnection = null;
		PreparedStatement statement = null;

		try {

			localConnection = OpenTenureApplication.getInstance().getDatabase().getConnection();
			statement = localConnection
					.prepareStatement("INSERT INTO CLAIM(CLAIM_ID, STATUS, NAME, PERSON_ID, CHALLENGED_CLAIM_ID) VALUES(?,?,?,?,?)");
			statement.setString(1, claim.getClaimId());
			statement.setString(2, claim.getStatus());
			statement.setString(3, claim.getName());
			statement.setString(4, claim.getPerson().getPersonId());
			if(claim.getChallengedClaim() != null){
				statement.setString(5, claim.getChallengedClaim().getClaimId());
			}else{
				statement.setString(5, null);
			}
				
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

	public int create(){
		int result = 0;
		Connection localConnection = null;
		PreparedStatement statement = null;

		try {

			localConnection = db.getConnection();
			statement = localConnection
					.prepareStatement("INSERT INTO CLAIM(CLAIM_ID, STATUS, NAME, PERSON_ID, CHALLENGED_CLAIM_ID) VALUES(?,?,?,?,?)");
			statement.setString(1, getClaimId());
			statement.setString(2, getStatus());
			statement.setString(3, getName());
			statement.setString(4, getPerson().getPersonId());
			if(getChallengedClaim() != null){
				statement.setString(5, getChallengedClaim().getClaimId());
			}else{
				statement.setString(5, null);
			}
				
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

	public static int updateClaim(Claim claim){
		int result = 0;
		Connection localConnection = null;
		PreparedStatement statement = null;

		try {

			localConnection = OpenTenureApplication.getInstance().getDatabase().getConnection();
			statement = localConnection
					.prepareStatement("UPDATE CLAIM SET STATUS=?, NAME=?, PERSON_ID=?, CHALLENGED_CLAIM_ID=? WHERE CLAIM_ID=?");
			statement.setString(1, claim.getStatus());
			statement.setString(2, claim.getName());
			statement.setString(3, claim.getPerson().getPersonId());
			if(claim.getChallengedClaim() != null){
				statement.setString(4, claim.getChallengedClaim().getClaimId());
			}else{
				statement.setString(4, null);
			}
			statement.setString(5, claim.getClaimId());
				
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

	public int update(){
		int result = 0;
		Connection localConnection = null;
		PreparedStatement statement = null;

		try {

			localConnection = db.getConnection();
			statement = localConnection
					.prepareStatement("UPDATE CLAIM SET STATUS=?, NAME=?, PERSON_ID=?, CHALLENGED_CLAIM_ID=? WHERE CLAIM_ID=?");
			statement.setString(1, getStatus());
			statement.setString(2, getName());
			statement.setString(3, getPerson().getPersonId());
			if(getChallengedClaim() != null){
				statement.setString(4, getChallengedClaim().getClaimId());
			}else{
				statement.setString(4, null);
			}
			statement.setString(5, getClaimId());
				
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

	public static Claim getClaim(String claimId){
		Claim claim = null;
		Connection localConnection = null;
		PreparedStatement statement = null;
		ResultSet rs = null;

		try {

			localConnection = OpenTenureApplication.getInstance().getDatabase().getConnection();
			statement = localConnection
					.prepareStatement("SELECT STATUS, NAME, PERSON_ID, CHALLENGED_CLAIM_ID FROM CLAIM WHERE CLAIM_ID=?");
			statement.setString(1, claimId);
			rs = statement.executeQuery();
			while (rs.next()) {
				claim = new Claim();
				claim.setClaimId(claimId);
				claim.setStatus(rs.getString(1));
				claim.setName(rs.getString(2));
				claim.setPerson(Person.getPerson(rs.getString(3)));
				claim.setChallengedClaim(Claim.getClaim(rs.getString(4)));
				claim.setVertices(Vertex.getVertices(claimId));
				claim.setAttachments(Attachment.getAttachments(claimId));
				claim.setAdditionalInfo(AdditionalInfo.getClaimAdditionalInfo(claimId));
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

			Claim challengedClaim = getClaim(claimId);
			localConnection = OpenTenureApplication.getInstance().getDatabase().getConnection();
			statement = localConnection
					.prepareStatement("SELECT CLAIM_ID, STATUS, NAME, PERSON_ID FROM CLAIM WHERE CHALLENGED_CLAIM_ID=?");
			statement.setString(1, claimId);
			rs = statement.executeQuery();
			while (rs.next()) {
				String challengingClaimId = rs.getString(1);
				Claim challengingClaim = new Claim();
				challengingClaim.setClaimId(challengingClaimId);
				challengingClaim.setStatus(rs.getString(2));
				challengingClaim.setName(rs.getString(3));
				challengingClaim.setPerson(Person.getPerson(rs.getString(4)));
				challengingClaim.setChallengedClaim(challengedClaim);
				challengingClaim.setVertices(Vertex.getVertices(challengingClaimId));
				challengingClaim.setAttachments(Attachment.getAttachments(challengingClaimId));
				challengingClaim.setAdditionalInfo(AdditionalInfo.getClaimAdditionalInfo(challengingClaimId));
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

	private void loadChallengingClaims() {
		challengingClaims = new ArrayList<Claim>();
		Connection localConnection = null;
		PreparedStatement statement = null;
		ResultSet rs = null;

		try {

			localConnection = db.getConnection();
			statement = localConnection
					.prepareStatement("SELECT CLAIM_ID, STATUS, NAME, PERSON_ID FROM CLAIM WHERE CHALLENGED_CLAIM_ID=?");
			statement.setString(1, claimId);
			rs = statement.executeQuery();
			while (rs.next()) {
				String challengingClaimId = rs.getString(1);
				Claim challengingClaim = new Claim();
				challengingClaim.setClaimId(challengingClaimId);
				challengingClaim.setStatus(rs.getString(2));
				challengingClaim.setName(rs.getString(3));
				challengingClaim.setPerson(Person.getPerson(rs.getString(4)));
				challengingClaim.setChallengedClaim(this);
				challengingClaim.setVertices(Vertex.getVertices(challengingClaimId));
				challengingClaim.setAttachments(Attachment.getAttachments(challengingClaimId));
				challengingClaim.setAdditionalInfo(AdditionalInfo.getClaimAdditionalInfo(challengingClaimId));
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
	}

	public static List<Claim> getAllClaims() {
		List<Claim> claims = new ArrayList<Claim>();
		Connection localConnection = null;
		PreparedStatement statement = null;
		ResultSet rs = null;

		try {

			localConnection = OpenTenureApplication.getInstance().getDatabase().getConnection();
			statement = localConnection
					.prepareStatement("SELECT CLAIM_ID, STATUS, NAME, PERSON_ID, CHALLENGED_CLAIM_ID FROM CLAIM");
			rs = statement.executeQuery();
			while (rs.next()) {
				String claimId = rs.getString(1);
				Claim claim = new Claim();
				claim.setClaimId(claimId);
				claim.setStatus(rs.getString(2));
				claim.setName(rs.getString(3));
				claim.setPerson(Person.getPerson(rs.getString(4)));
				claim.setChallengedClaim(Claim.getClaim(rs.getString(5)));
				claim.setVertices(Vertex.getVertices(claimId));
				claim.setAttachments(Attachment.getAttachments(claimId));
				claim.setAdditionalInfo(AdditionalInfo.getClaimAdditionalInfo(claimId));
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

	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}

	String claimId;
	String name;
	String status;
	Person person;
	Claim challengedClaim;
	List<Vertex> vertices;
	List<AdditionalInfo> additionalInfo;
	List<Claim> challengingClaims;
	List<Attachment> attachments;

}
