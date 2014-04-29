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
package org.fao.sola.clients.android.opentenure.network;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.fao.sola.clients.android.opentenure.filesystem.json.model.AdditionalInfo;
import org.fao.sola.clients.android.opentenure.filesystem.json.model.Attachment;
import org.fao.sola.clients.android.opentenure.filesystem.json.model.Claim;
import org.fao.sola.clients.android.opentenure.filesystem.json.model.Claimant;
import org.fao.sola.clients.android.opentenure.model.Metadata;
import org.fao.sola.clients.android.opentenure.model.Person;
import org.fao.sola.clients.android.opentenure.model.Vertex;
import org.fao.sola.clients.android.opentenure.network.API.CommunityServerAPI;

import android.os.AsyncTask;

public class GetClaimTask extends AsyncTask<String, Void, Claim> {

	@Override
	protected Claim doInBackground(String... params) {
		// TODO Auto-generated method stub
		return CommunityServerAPI.getClaim(params[0]);
	}

	@Override
	protected void onPostExecute(final Claim claim) {

		if (claim == null)
			return;

		/**
		 * 
		 * Write
		 * 
		 **/

		List<org.fao.sola.clients.android.opentenure.model.Attachment> attachmentsDB = new ArrayList<org.fao.sola.clients.android.opentenure.model.Attachment>();
		List<org.fao.sola.clients.android.opentenure.model.Metadata> metadataDBList = new ArrayList<org.fao.sola.clients.android.opentenure.model.Metadata>();
		List<org.fao.sola.clients.android.opentenure.model.Vertex> vertexDBList = Vertex
				.verticesFromWKT(claim.getMappedGeometry(),
						claim.getGpsGeometry());

		List<Attachment> attachments = claim.getAttachments();
		for (Iterator iterator = attachments.iterator(); iterator.hasNext();) {

			org.fao.sola.clients.android.opentenure.model.Attachment attachmentDB = new org.fao.sola.clients.android.opentenure.model.Attachment();
			Attachment attachment = (Attachment) iterator.next();

			attachmentDB.setAttachmentId(attachment.getId());
			attachmentDB.setClaimId(claim.getId());
			attachmentDB.setDescription(attachment.getDescription());
			attachmentDB.setFileName(attachment.getFileName());
			attachmentDB.setFileType(attachment.getFileType());
			attachmentDB.setMD5Sum(attachment.getMD5Sum());
			attachmentDB.setMimeType(attachment.getMimeType());
			attachmentDB.setStatus(attachment.getStatus());


			attachmentsDB.add(attachmentDB);

		}

		List<AdditionalInfo> metadataList;
		if ((metadataList = claim.getAdditionaInfo()) != null) {

			for (Iterator iterator = metadataList.iterator(); iterator
					.hasNext();) {
				AdditionalInfo additionalInfo = (AdditionalInfo) iterator
						.next();

				org.fao.sola.clients.android.opentenure.model.Metadata metadataDB = new Metadata();

				metadataDB.setClaimId(claim.getId());
				metadataDB.setMetadataId(additionalInfo.getMetadataId());
				metadataDB.setName(additionalInfo.getName());
				metadataDB.setValue(additionalInfo.getValue());

				metadataDBList.add(metadataDB);

			}
		}
		Claimant claimant = claim.getClaimant();

		Person person = new Person();
		person.setContactPhoneNumber(claimant.getPhone());

		Date birth = claimant.getBirthDate();
		if (birth != null)
			person.setDateOfBirth(new java.sql.Date(birth.getTime()));
		else
			person.setDateOfBirth(new java.sql.Date(2000, 2, 3));

		// *****Qui ho un problema perche' la data di nascita puo' nn esserci.
		// Nn ci serebbe motivo contrario*************************////

		person.setEmailAddress(claimant.getEmail());
		person.setFirstName(claimant.getName());
		person.setGender(claimant.getGenderCode());
		person.setLastName(claimant.getLastName());
		person.setMobilePhoneNumber(claimant.getMobilePhone());
		person.setPersonId(claimant.getId());
		person.setPlaceOfBirth(claimant.getPlace_of_birth());
		person.setPostalAddress(claimant.getAddress());

		org.fao.sola.clients.android.opentenure.model.Claim claimDB = new org.fao.sola.clients.android.opentenure.model.Claim();

		claimDB.setAttachments(attachmentsDB);

		/***
		 * 
		 * We should set the challenged claim but if is not in the right order
		 * it will be there a problem
		 * 
		 **/
		// claimDB.setChallengedClaim(org.fao.sola.clients.android.opentenure.model.Claim.getClaim(claim.getChallengedClaimId()));
		claimDB.setClaimId(claim.getId());
		claimDB.setMetadata(metadataDBList);
		claimDB.setName(claim.getDescription());
		claimDB.setVertices(vertexDBList);
		claimDB.setPerson(person);

		Person.createPerson(person);

		org.fao.sola.clients.android.opentenure.model.Claim
				.createClaim(claimDB);

	}

}
