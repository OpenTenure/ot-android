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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;

import org.fao.sola.clients.android.opentenure.filesystem.FileSystemUtilities;
import org.fao.sola.clients.android.opentenure.filesystem.json.JsonUtilities;
import org.fao.sola.clients.android.opentenure.filesystem.json.model.Attachment;
import org.fao.sola.clients.android.opentenure.filesystem.json.model.Claim;
import org.fao.sola.clients.android.opentenure.filesystem.json.model.Claimant;
import org.fao.sola.clients.android.opentenure.model.AttachmentStatus;
import org.fao.sola.clients.android.opentenure.model.Person;
import org.fao.sola.clients.android.opentenure.model.Vertex;
import org.fao.sola.clients.android.opentenure.network.API.CommunityServerAPI;
import android.util.Log;

public class GetClaims {

	public static boolean execute(
			org.fao.sola.clients.android.opentenure.network.response.Claim[] params) {

		boolean success = true;

		for (int i = 0; i < params.length; i++) {
			org.fao.sola.clients.android.opentenure.network.response.Claim claim = (org.fao.sola.clients.android.opentenure.network.response.Claim) params[i];

			Claim downloadedClaim = CommunityServerAPI.getClaim(claim.getId());

			if (downloadedClaim == null)
				success = false;

			/**
			 * 
			 * Parsing the downloaded Claim and saving it to DB
			 * 
			 **/

			List<org.fao.sola.clients.android.opentenure.model.Attachment> attachmentsDB = new ArrayList<org.fao.sola.clients.android.opentenure.model.Attachment>();
			List<org.fao.sola.clients.android.opentenure.model.AdditionalInfo> additionalInfoDBList = new ArrayList<org.fao.sola.clients.android.opentenure.model.AdditionalInfo>();


			/*
			 * Temporary disable
			 */
			// List<AdditionalInfo> metadataList;
			// if ((metadataList = claim.getAdditionaInfo()) != null) {
			//
			// for (Iterator iterator = metadataList.iterator(); iterator
			// .hasNext();) {
			// AdditionalInfo additionalInfo = (AdditionalInfo) iterator
			// .next();
			//
			// org.fao.sola.clients.android.opentenure.model.Metadata metadataDB
			// =
			// new Metadata();
			//
			// metadataDB.setClaimId(claim.getId());
			// metadataDB.setMetadataId(additionalInfo.getMetadataId());
			// metadataDB.setName(additionalInfo.getName());
			// metadataDB.setValue(additionalInfo.getValue());
			//
			// metadataDBList.add(metadataDB);
			//
			// }
			// }
			Claimant claimant = downloadedClaim.getClaimant();

			Person person = new Person();
			person.setContactPhoneNumber(claimant.getPhone());

			Date birth = null;
			try {
				// birth = df.parse(claimant.getBirthDate());

				Calendar cal = JsonUtilities
						.toCalendar(claimant.getBirthDate());
				birth = cal.getTime();

				if (birth != null)
					person.setDateOfBirth(new java.sql.Date(birth.getTime()));
				else
					person.setDateOfBirth(new java.sql.Date(2000, 2, 3));

				// *****Qui ho un problema perche' la data di nascita puo' nn
				// esserci.
				// Nn ci serebbe motivo contrario*************************////

			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();

				Log.d("CommunityServerAPI",
						"ERROR DOWNLOADING  CLAIM " + claim.getId());

				success = false;
			}

			try {

				person.setEmailAddress(claimant.getEmail());
				person.setFirstName(claimant.getName());
				person.setGender(claimant.getGenderCode());
				person.setLastName(claimant.getLastName());
				person.setMobilePhoneNumber(claimant.getMobilePhone());
				person.setPersonId(claimant.getId());
				// person.setPlaceOfBirth(claimant.getPlaceOfBirth());
				person.setPostalAddress(claimant.getAddress());

				org.fao.sola.clients.android.opentenure.model.Claim claimDB = new org.fao.sola.clients.android.opentenure.model.Claim();

				claimDB.setAttachments(attachmentsDB);

				/***
				 * 
				 * We should set the challenged claim but if is not in the right
				 * order it will be there a problem
				 * 
				 **/
				// claimDB.setChallengedClaim(org.fao.sola.clients.android.opentenure.model.Claim.getClaim(claim.getChallengedClaimId()));
				claimDB.setClaimId(downloadedClaim.getId());
				claimDB.setAdditionalInfo(additionalInfoDBList);
				claimDB.setName(downloadedClaim.getDescription());
				claimDB.setPerson(person);
				claimDB.setStatus(downloadedClaim.getStatusCode());

				Person.createPerson(person);

				org.fao.sola.clients.android.opentenure.model.Claim
						.createClaim(claimDB);

				if (downloadedClaim.getGpsGeometry().startsWith("POINT"))
					Vertex.storeWKT(claimDB.getClaimId(),
							downloadedClaim.getMappedGeometry(),
							downloadedClaim.getMappedGeometry());
				else
					Vertex.storeWKT(claimDB.getClaimId(),
							downloadedClaim.getMappedGeometry(),
							downloadedClaim.getGpsGeometry());
				
				
				List<Attachment> attachments = downloadedClaim.getAttachments();
				for (Iterator<Attachment> iterator = attachments.iterator(); iterator
						.hasNext();) {

					org.fao.sola.clients.android.opentenure.model.Attachment attachmentDB = new org.fao.sola.clients.android.opentenure.model.Attachment();
					Attachment attachment = (Attachment) iterator.next();

					attachmentDB.setAttachmentId(attachment.getId());
					attachmentDB.setClaimId(claim.getId());
					attachmentDB.setDescription(attachment.getDescription());
					attachmentDB.setFileName(attachment.getFileName());
					attachmentDB.setFileType(attachment.getMimeType());
					attachmentDB.setMD5Sum(attachment.getMd5());
					attachmentDB.setMimeType(attachment.getMimeType());
					attachmentDB.setPath("");
					attachmentDB.setStatus(AttachmentStatus._UPLOADED);
					attachmentDB.setSize(attachment.getSize());
					
					org.fao.sola.clients.android.opentenure.model.Attachment.createAttachment(attachmentDB);

					/*
					 * Here the creation of Folder for the claim
					 * */
					
					FileSystemUtilities.createClaimantFolder(claimant.getId());
					FileSystemUtilities.createClaimFileSystem(downloadedClaim.getId());					
					

				}

			}

			catch (Exception e) {
				Log.d("CommunityServerAPI", "ERROR SAVING DOWNLOADED  CLAIM "
						+ claim.getId());
				e.printStackTrace();
				success = false;
			}

		}

		return success;

	}

}
