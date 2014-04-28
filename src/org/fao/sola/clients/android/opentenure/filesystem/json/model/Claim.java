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
package org.fao.sola.clients.android.opentenure.filesystem.json.model;

import java.util.List;




public class Claim {
	
	String id;
	
	String nr;
	
	String name;
	
	String statusCode;
	
	String description;
	
	String challengeExpiryDate;
	
	String lodgementDate;
	
	String mappedGeometry;
	
	String gpsGeometry;	
	
	String challengedClaimId;
	
	List<Vertex> verteces;
	
	List<Attachment> attachments;
	
	List<AdditionalInfo> additionaInfo;
	
	Claimant claimant;
	
	

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getChallenged_id_claim() {
		return challengedClaimId;
	}

	public void setChallenged_id_claim(String challengedClaimId) {
		this.challengedClaimId = challengedClaimId;
	}

	public org.fao.sola.clients.android.opentenure.filesystem.json.model.Claimant getPerson() {
		return claimant;
	}

	public void setPerson(
			org.fao.sola.clients.android.opentenure.filesystem.json.model.Claimant claimant) {
		this.claimant = claimant;
	}

	public List<Vertex> getVerteces() {
		return verteces;
	}

	public void setVerteces(List<Vertex> verteces) {
		this.verteces = verteces;
	}

	public List<Attachment> getAttachments() {
		return attachments;
	}

	public void setAttachments(List<Attachment> attachments) {
		this.attachments = attachments;
	}



	public String getLodgementDate() {
		return lodgementDate;
	}

	public void setLodgementDate(String lodgementDate) {
		this.lodgementDate = lodgementDate;
	}

	public List<AdditionalInfo> getAdditionaInfo() {
		return additionaInfo;
	}

	public void setAdditionaInfo(List<AdditionalInfo> additionaInfo) {
		this.additionaInfo = additionaInfo;
	}

	public String getNr() {
		return nr;
	}

	public void setNr(String nr) {
		this.nr = nr;
	}

	public String getStatusCode() {
		return statusCode;
	}

	public void setStatusCode(String statusCode) {
		this.statusCode = statusCode;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getChallengeExpiryDate() {
		return challengeExpiryDate;
	}

	public void setChallengeExpiryDate(String challengeExpiryDate) {
		this.challengeExpiryDate = challengeExpiryDate;
	}

	public String getMappedGeometry() {
		return mappedGeometry;
	}

	public void setMappedGeometry(String mappedGeometry) {
		this.mappedGeometry = mappedGeometry;
	}

	public String getGpsGeometry() {
		return gpsGeometry;
	}

	public void setGpsGeometry(String gpsGeometry) {
		this.gpsGeometry = gpsGeometry;
	}

	public String getChallengedClaimId() {
		return challengedClaimId;
	}

	public void setChallengedClaimId(String challengedClaimId) {
		this.challengedClaimId = challengedClaimId;
	}

	public Claimant getClaimant() {
		return claimant;
	}

	public void setClaimant(Claimant claimant) {
		this.claimant = claimant;
	}


	

}
