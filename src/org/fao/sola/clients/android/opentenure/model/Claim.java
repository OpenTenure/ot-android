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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import org.fao.sola.clients.android.opentenure.OpenTenureApplication;

public class Claim {
	Database db = OpenTenureApplication.getInstance().getDatabase();

	@Override
	public String toString() {
		return "Claim [claimId=" + claimId + ", dateCreated=" + dateCreated
				+ ", person=" + person + ", boundary=" + boundary
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
	public java.sql.Timestamp getDateCreated() {
		return dateCreated;
	}
	public void setDateCreated(java.sql.Timestamp dateCreated) {
		this.dateCreated = dateCreated;
	}
	public Person getPerson() {
		return person;
	}
	public void setPerson(Person person) {
		this.person = person;
	}
	public Boundary getBoundary() {
		return boundary;
	}
	public void setBoundary(Boundary boundary) {
		this.boundary = boundary;
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
	public List<Document> getAttachments() {
		return attachments;
	}
	public void setAttachments(List<Document> attachments) {
		this.attachments = attachments;
	}
	
	public static int update(Claim claim){
		Person.updatePerson(claim.getPerson());
		Boundary.updateBoundary(claim.getBoundary());
		return OpenTenureApplication.getInstance().getDatabase().update("UPDATE CLAIM SET DATE_CREATED='"
				+ new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(claim.getDateCreated())
				+ "', PERSON_ID='"
				+ claim.getPerson().getPersonId()
				+ "', BOUNDARY_ID='"
				+ claim.getBoundary().getBoundaryId()
				+ "', CHALLENGED_CLAIM_ID='"
				+ claim.getChallengedClaim()
				+ "' WHERE CLAIM_ID='"
				+ claim.getClaimId() + "'");
	}

	public int update(){
		person.update();
		boundary.update();
		return OpenTenureApplication.getInstance().getDatabase().update("UPDATE CLAIM SET DATE_CREATED='"
				+ new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(getDateCreated())
				+ "', PERSON_ID='"
				+ getPerson().getPersonId()
				+ "', BOUNDARY_ID='"
				+ getBoundary().getBoundaryId()
				+ "', CHALLENGED_CLAIM_ID='"
				+ getChallengedClaim()
				+ "' WHERE CLAIM_ID='"
				+ getClaimId() + "'");
	}

	public static Claim getClaim(String claimId){
		Claim claim = null;

		Connection localConnection = null;
		try {

			localConnection = OpenTenureApplication.getInstance().getDatabase().getConnection();
			PreparedStatement statement = localConnection
					.prepareStatement("SELECT DATE_CREATED, PERSON_ID, BOUNDARY_ID, CHALLENGED_CLAIM_ID FROM CLAIM WHERE CLAIM_ID=?");
			statement.setString(1, claimId);
			ResultSet rs = statement.executeQuery();
			while (rs.next()) {
				claim = new Claim();
				claim.setClaimId(claimId);
				claim.setDateCreated(rs.getTimestamp(1));
				claim.setPerson(Person.getPerson(rs.getString(2)));
				claim.setBoundary(Boundary.getBoundary(rs.getString(3)));
				claim.setChallengedClaim(Claim.getClaim(rs.getString(4)));
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
		return claim;
	}

	public List<Document> getAttachments(String claimId) {

		List<Document> documents = null;

		Connection localConnection = null;
		try {

			localConnection = db.getConnection();
			PreparedStatement statement = localConnection
					.prepareStatement("SELECT DOCUMENT_ID, FILE_NAME, MD5SUM, PATH FROM DOCUMENT DOC WHERE DOC.CLAIM_ID=?");
			statement.setString(1, claimId);
			ResultSet rs = statement.executeQuery();
			while (rs.next()) {
				documents = new ArrayList<Document>();
				Document document = new Document();
				document.setDocumentId(rs.getString(1));
				document.setClaimId(claimId);
				document.setFileName(rs.getString(2));
				document.setMD5Sum(rs.getString(3));
				document.setPath(rs.getString(4));
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
		return documents;
	}

	public List<Claim> getChallengingClaims(String claimId) {

		List<Claim> challengingClaims = null;

		Connection localConnection = null;
		try {

			localConnection = db.getConnection();
			PreparedStatement statement = localConnection
					.prepareStatement("SELECT CLAIM_ID, DATE_CREATED, PERSON_ID, BOUNDARY_ID FROM CLAIM WHERE CHALLENGED_CLAIM_ID=?");
			statement.setString(1, claimId);
			ResultSet rs = statement.executeQuery();
			while (rs.next()) {
				challengingClaims = new ArrayList<Claim>();
				Claim claim = new Claim();
				claim.setClaimId(rs.getString(1));
				claim.setDateCreated(rs.getTimestamp(2));
				claim.setPerson(Person.getPerson(rs.getString(3)));
				claim.setBoundary(Boundary.getBoundary(rs.getString(4)));
				claim.setChallengedClaim(this);
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
		return challengingClaims;
	}

	String claimId;
	java.sql.Timestamp dateCreated;
	Person person;
	Boundary boundary;
	Claim challengedClaim;
	List<Claim> challengingClaims;
	List<Document> attachments;

}
